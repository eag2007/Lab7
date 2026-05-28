package org.example.server.managers;


import org.example.packet.collection.Coordinates;
import org.example.packet.collection.Location;
import org.example.packet.collection.Route;
import org.example.packet.collection.RouteClient;
import org.example.server.logger.ServerLogger;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Properties;

public class ManagerDataBaseShard {
    private static ManagerDataBaseShard instance;

    private static final int SHARD_COUNT = 3;

    private final Object[] shardLock = new Object[SHARD_COUNT];
    private final List<Connection> shardsConnection = new ArrayList<>(SHARD_COUNT);

    private final String[] hosts = new String[SHARD_COUNT];
    private final String[] ports = new String[SHARD_COUNT];
    private final String[] names = new String[SHARD_COUNT];
    private final String[] users = new String[SHARD_COUNT];
    private final String[] passwords = new String[SHARD_COUNT];

    private ManagerDataBaseShard() {
        for (int i = 0; i < SHARD_COUNT; i++) {
            shardLock[i] = new Object();
        }
        try {
            loadConfig();
            connectAll();
            createTableAll();
        } catch (SQLException e) {
            ServerLogger.error("Шардированная БД неинициализирована: {}", e.getMessage());
        } catch (RuntimeException e) {
            ServerLogger.error("Ошибка инициализация шардов: {}", e.getMessage());
        }
    }

    private void loadConfig() {
        try {
            InputStream in = new FileInputStream("shards.properties");
            Properties p = new Properties();
            p.load(in);
            for (int i = 0; i < SHARD_COUNT; i++) {
                hosts[i] = p.getProperty("shard." + i + ".host", "localhost");
                ports[i] = p.getProperty("shard." + i + ".port", String.valueOf(5432 + i));
                names[i] = p.getProperty("shard." + i + ".name", "route" + i);
                users[i] = p.getProperty("shard." + i + ".user", "postgres");
                passwords[i] = "1234567890";
            }
            ServerLogger.info("Конфиг шардов загружен из shards.propetries");
        } catch (IOException e) {
            ServerLogger.error("Файл shards.properties не найден, использую значения по умолчанию");
            for (int i = 0; i < SHARD_COUNT; i++) {
                hosts[i] = "localhost";
                ports[i] = String.valueOf(5432 + i);
                names[i] = "route" + i;
                users[i] = "postgres";
                passwords[i] = "1234567890";
            }
        }
    }

    public static ManagerDataBaseShard getInstance() {
        if (instance == null) {
            instance = new ManagerDataBaseShard();
        }
        return instance;
    }

    private String buildUrlToDB(int i) {
        return String.format("jdbc:postgresql://%s:%s/%s", hosts[i], ports[i], names[i]);
    }

    private Connection openConnectionWithDB(int i) throws SQLException {
        return DriverManager.getConnection(buildUrlToDB(i), users[i], passwords[i]);
    }

    private void connectAll() throws SQLException {
        for (int i = 0; i < SHARD_COUNT; i++) {
            try {
                shardsConnection.add(openConnectionWithDB(i));
                ServerLogger.info("Подключён шард {}: {}", i, buildUrlToDB(i));
            } catch (SQLException e) {
                if ("3D000".equals(e.getSQLState())) {
                    createDataBase(i);
                    shardsConnection.add(openConnectionWithDB(i));
                    ServerLogger.info("Создана и подключена БД для шарда {}", i);
                } else {
                    throw e;
                }
            }
        }
    }

    private void createDataBase(int i) throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/postgres", hosts[i], ports[i]);

        Connection connection = DriverManager.getConnection(url, users[i], passwords[i]);
        Statement stmt = connection.createStatement();
        stmt.execute("CREATE DATABASE " + names[i]);
        ServerLogger.info("Создана БД {} для шарда {}", names[i], i);

    }

    private void createTableAll() {
        String createUsers = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    login VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(300) NOT NULL,
                    salt VARCHAR(64) NOT NULL,
                    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;
        String createRoutes = """
                CREATE TABLE IF NOT EXISTS routes (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    coordinates_x BIGINT CHECK (coordinates_x <= 108),
                    coordinates_y BIGINT CHECK (coordinates_y <= 20),
                    creationDate TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    from_x FLOAT,
                    from_y DOUBLE PRECISION,
                    from_z INTEGER NOT NULL,
                    to_x FLOAT,
                    to_y DOUBLE PRECISION NOT NULL,
                    to_z INTEGER NOT NULL,
                    distance INTEGER CHECK (distance > 1),
                    price DECIMAL(15, 2),
                    author VARCHAR(50) NOT NULL
                );
                """;

        for (int i = 0; i < shardsConnection.size(); i++) {
            try {
                Statement stmt = shardsConnection.get(i).createStatement();
                if (i == 0) {
                    stmt.execute(createUsers);
                }
                ServerLogger.info("Таблицы инициализированы в шарде {}", i);
            } catch (SQLException e) {
                ServerLogger.error("Ошибка создания таблиц в шарде {}: {}", i, e.getMessage());
            }
        }
    }

    private int shardOf(String login) {
        int id_shard = Math.abs(login.hashCode() % SHARD_COUNT);
        ServerLogger.info("Пользователь {} -> шард {}", login, id_shard);
        return id_shard;
    }

    private boolean isValid(Connection conn) {
        try {
            return conn != null && !conn.isClosed() && conn.isValid(2);
        } catch (SQLException e) {
            return false;
        }
    }

    private boolean ensureShard(int i) {
        try {
            Connection conn = i < shardsConnection.size() ? shardsConnection.get(i) : null;
            if (!isValid(conn)) {
                ServerLogger.debug("Шард {} требует переподключения", i);
                Connection fresh = openConnectionWithDB(i);
                shardsConnection.set(i, fresh);
                ServerLogger.info("Шард {} переподключён", i);
            }
            return true;
        } catch (SQLException e) {
            ServerLogger.error("Не удалось подключить шард {}: {}", i, e.getMessage());
            return false;
        }
    }

    private Route mapRoute(ResultSet rs) throws SQLException {
        return new Route(
                rs.getLong("id"),
                rs.getString("name"),
                new Coordinates(rs.getLong("coordinates_x"), rs.getLong("coordinates_y")),
                rs.getTimestamp("creationDate").toLocalDateTime().atZone(ZoneId.systemDefault()),
                new Location(rs.getFloat("from_x"), rs.getDouble("from_y"), rs.getInt("from_z")),
                new Location(rs.getFloat("to_x"),   rs.getDouble("to_y"),   rs.getInt("to_z")),
                rs.getInt("distance"),
                rs.getBigDecimal("price"),
                rs.getString("author")
        );
    }

    public Route addRouteInDBFull(RouteClient routeClient, String author) {
        int idx = shardOf(author);
        synchronized (shardLock[idx]) {
            if (!ensureShard(idx)) {
                ServerLogger.error("Нет подключения к БД, шард {}", idx);
                throw new RuntimeException("DB don't have connect");
            }

            Connection conn = shardsConnection.get(idx);

            String sql = """
                    INSERT INTO routes (name, coordinates_x, coordinates_y, from_x, from_y, from_z,
                                        to_x, to_y, to_z, distance, price, author)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id, creationDate, name, coordinates_x, coordinates_y,
                              from_x, from_y, from_z, to_x, to_y, to_z, distance, price, author;
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, routeClient.getName());
                pstmt.setLong(2, routeClient.getCoordinates().getX());
                pstmt.setLong(3, routeClient.getCoordinates().getY());
                pstmt.setFloat(4, routeClient.getFrom().getX());
                pstmt.setDouble(5, routeClient.getFrom().getY());
                pstmt.setInt(6, routeClient.getFrom().getZ());
                pstmt.setFloat(7, routeClient.getTo().getX());
                pstmt.setDouble(8, routeClient.getTo().getY());
                pstmt.setInt(9, routeClient.getTo().getZ());
                pstmt.setInt(10, routeClient.getDistance());
                pstmt.setBigDecimal(11, routeClient.getPrice());
                pstmt.setString(12, author);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        long id = rs.getLong("id");
                        ServerLogger.info("Элемент ID={} добавлен в шард {} ({})", id, idx, author);
                        return mapRoute(rs);
                    }
                }
                return null;
            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
                    throw new RuntimeException("DB don't have connnect");
                }
                ServerLogger.error("Ошибка при sql запросе: {}", e.getMessage());
                return null;
            }
        }
    }

    public Route updateRouteInDBFull(long id, RouteClient newData, String author) {
        int idx = shardOf(author);
        synchronized (shardLock[idx]) {
            if (!ensureShard(idx)) {
                ServerLogger.error("Нет подключения к БД");
                throw new RuntimeException("DB_UNAVAILABLE");
            }
            Connection conn = shardsConnection.get(idx);

            String sql = """
                    UPDATE routes SET
                        name = ?, coordinates_x = ?, coordinates_y = ?,
                        from_x = ?, from_y = ?, from_z = ?,
                        to_x = ?, to_y = ?, to_z = ?,
                        distance = ?, price = ?
                    WHERE id = ? AND author = ?
                    RETURNING id, creationDate, name, coordinates_x, coordinates_y,
                              from_x, from_y, from_z, to_x, to_y, to_z, distance, price, author;
                    """;

            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1,      newData.getName());
                pstmt.setLong(2,        newData.getCoordinates().getX());
                pstmt.setLong(3,        newData.getCoordinates().getY());
                pstmt.setFloat(4,       newData.getFrom().getX());
                pstmt.setDouble(5,      newData.getFrom().getY());
                pstmt.setInt(6,         newData.getFrom().getZ());
                pstmt.setFloat(7,       newData.getTo().getX());
                pstmt.setDouble(8,      newData.getTo().getY());
                pstmt.setInt(9,         newData.getTo().getZ());
                pstmt.setInt(10,        newData.getDistance());
                pstmt.setBigDecimal(11, newData.getPrice());
                pstmt.setLong(12,       id);
                pstmt.setString(13,     author);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ServerLogger.info("Маршрут ID={} обновлён в шарде {}", id, idx);
                        return mapRoute(rs);
                    }
                }
                ServerLogger.debug("Маршрут ID={} не найден у пользователя {}", id, author);
                return null;
            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("08")) {
                    throw new RuntimeException("DB_UNAVAILABLE");
                }
                ServerLogger.error("Ошибка обновления маршрута: {}", e.getMessage());
                return null;
            }
        }
    }

    public long deleteRouteInDB(long id, String author) {
        int idx = shardOf(author);
        synchronized (shardLock[idx]) {
            if (!ensureShard(idx)) {
                ServerLogger.error("Нет подключения к БД");
                return -3;
            }
            Connection conn = shardsConnection.get(idx);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "DELETE FROM routes WHERE id = ? AND author = ? RETURNING id;")) {
                pstmt.setLong(1, id);
                pstmt.setString(2, author);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ServerLogger.info("Маршрут ID={} удалён из шарда {}", id, idx);
                        return rs.getLong("id");
                    }
                }
                ServerLogger.info("Маршрут ID={} не найден в шарде {} у {}", id, idx, author);
                return 0;
            } catch (SQLException e) {
                if (e.getSQLState() != null && e.getSQLState().startsWith("08")) return -3;
                ServerLogger.error("Ошибка удаления маршрута из БД: {}", e.getMessage());
                return -1;
            }
        }
    }

    public Route getRouteInDB(long id, String login) {
        int idx = shardOf(login);
        synchronized (shardLock[idx]) {
            if (!ensureShard(idx)) {
                ServerLogger.error("Нет подключения к БД");
                return null;
            }
            Connection conn = shardsConnection.get(idx);

            try (PreparedStatement pstmt = conn.prepareStatement(
                    "SELECT * FROM routes WHERE id = ? AND author = ?;")) {
                pstmt.setLong(1, id);
                pstmt.setString(2, login);

                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        ServerLogger.info("Маршрут ID={} получен из шарда {}", id, idx);
                        return mapRoute(rs);
                    }
                }
                ServerLogger.error("Маршрут ID={} не найден в шарде {} у {}", id, idx, login);
                return null;
            } catch (SQLException e) {
                ServerLogger.error("Ошибка получения маршрута из БД: {}", e.getMessage());
                return null;
            }
        }
    }

    public PriorityQueue<Route> getRoutesInDB() {
        PriorityQueue<Route> all = new PriorityQueue<>();
        for (int i = 0; i < SHARD_COUNT; i++) {
            synchronized (shardLock[i]) {
                if (!ensureShard(i)) {
                    ServerLogger.error("Шард {} недоступен, пропускаем", i);
                    continue;
                }
                Connection connection = shardsConnection.get(i);

                try {
                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery("SELECT * FROM routes ORDER BY id");
                    while (rs.next()) {
                        all.add(mapRoute(rs));
                    }
                    ServerLogger.info("Загружены маршруты из шарда {}", i);
                } catch (SQLException e) {
                    ServerLogger.error("Ошибка загрузки {} маршрутов из {} шардов", all.size(), SHARD_COUNT);
                }
            }
        }
        return all;
    }

    public int clearRoutesInDB(String author) {
        int total = 0;
        for (int i = 0; i < SHARD_COUNT; i++) {
            synchronized (shardLock[i]) {
                if (!ensureShard(i)) continue;
                Connection conn = shardsConnection.get(i);

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM routes WHERE author = ?;")) {
                    pstmt.setString(1, author);
                    int rows = pstmt.executeUpdate();
                    total += rows;
                    if (rows > 0) ServerLogger.info("Удалено {} маршрутов {} из шарда {}", rows, author, i);
                } catch (SQLException e) {
                    ServerLogger.error("Ошибка clearRoutes в шарде {}: {}", i, e.getMessage());
                    return -1;
                }
            }
        }
        ServerLogger.info("Всего удалено {} маршрутов пользователя {}", total, author);
        return total;
    }

    public int deleteRouteDistanceInDB(int distance, String author) {
        int total = 0;
        for (int i = 0; i < SHARD_COUNT; i++) {
            synchronized (shardLock[i]) {
                if (!ensureShard(i)) continue;
                Connection conn = shardsConnection.get(i);

                try (PreparedStatement pstmt = conn.prepareStatement(
                        "DELETE FROM routes WHERE distance = ? AND author = ?")) {
                    pstmt.setInt(1, distance);
                    pstmt.setString(2, author);
                    int rows = pstmt.executeUpdate();
                    total += rows;
                    if (rows > 0) ServerLogger.info("Удалено {} маршрутов distance={} у {} из шарда {}", rows, distance, author, i);
                } catch (SQLException e) {
                    ServerLogger.error("Ошибка deleteDistance в шарде {}: {}", i, e.getMessage());
                    return -1;
                }
            }
        }
        return total;
    }

    public boolean checkUserPasswordInDB(String login, String password) {
        synchronized (shardLock[0]) {
            if (!ensureShard(0)) {
                ServerLogger.error("БД недоступна. Проверка пароля пропущена.");
                return false;
            }
            Connection conn = shardsConnection.get(0);

            try (PreparedStatement saltStmt = conn.prepareStatement(
                    "SELECT salt FROM users WHERE login = ?")) {
                saltStmt.setString(1, login);
                try (ResultSet saltRs = saltStmt.executeQuery()) {
                    if (!saltRs.next()) return false;
                    String salt = saltRs.getString("salt");
                    String inputHash = ManagerHasher.hash(password, salt);

                    try (PreparedStatement pstmt = conn.prepareStatement(
                            "SELECT 1 FROM users WHERE login = ? AND password = ?")) {
                        pstmt.setString(1, login);
                        pstmt.setString(2, inputHash);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            return rs.next();
                        }
                    }
                }
            } catch (SQLException e) {
                String sqlState = e.getSQLState();
                if (sqlState != null && sqlState.startsWith("08")) {
                    ServerLogger.debug("Соединение с БД потеряно");
                    ensureShard(0);
                } else {
                    ServerLogger.error("Ошибка проверки пароля [{}]: {}", sqlState, e.getMessage());
                }
                return false;
            }
        }
    }

    public boolean repeatConnect() {
        boolean flag = true;
        for (int i = 0; i < SHARD_COUNT; i++) {
            synchronized (shardLock[i]) {
                if (!ensureShard(i)) {
                    flag = false;
                }
            }
        }
        return flag;
    }

    public void close() {
        for (int i = 0; i < shardsConnection.size(); i++) {
            synchronized (shardLock[i]) {
                try {
                    Connection connection = shardsConnection.get(i);
                    if (connection != null && !connection.isClosed()) {
                        connection.close();
                        ServerLogger.info("Соединение с шардом {} закрыто", i);
                    }
                } catch (SQLException e) {
                    ServerLogger.info("Ошибка закрытия шарда {}: {}", i, e.getMessage());
                }
            }
        }
    }

    public Connection getConnection() {
        synchronized (shardLock[0]) {
            if (!ensureShard(0)) {
                ServerLogger.debug("БД недоступна");
                return null;
            }
            return shardsConnection.get(0);
        }
    }
}