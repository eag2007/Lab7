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
import java.util.PriorityQueue;
import java.util.Properties;


public class ManagerDataBase {
    private static ManagerDataBase instance;
    private static Connection connection;

    private static String DB_HOST;
    private static String DB_PORT;
    private static String DB_NAME;
    private static String DB_USER;
    private static String DB_PASSWORD;

    private ManagerDataBase() {
        try {
            loadConfigDB();
            connectDB();
            createTableDB();
        } catch (SQLException e) {
            ServerLogger.error("База данных не инициализированна");
        } catch (RuntimeException e) {
            ServerLogger.error("Ошибка инициализации БД {}", e.getMessage());
        }
    }

    private void loadConfigDB() {
        try {
            InputStream input = new FileInputStream("db.properties");

            Properties properties = new Properties();

            properties.load(input);

            DB_HOST = properties.getProperty("db.host", "localhost");
            DB_PORT = properties.getProperty("db.port", "5432");
            DB_NAME = properties.getProperty("db.name", "route");
            DB_USER = properties.getProperty("db.user", "postgres");
            DB_PASSWORD = properties.getProperty("db.password", "1234567890");

        } catch (IOException e) {
            ServerLogger.error("Файл db.properties не найден, использую значения по умолчанию");
            DB_HOST = "localhost";
            DB_PORT = "5432";
            DB_NAME = "route";
            DB_USER = "postgres";
            DB_PASSWORD = "1234567890";
        }
    }

    public static synchronized ManagerDataBase getInstance() {
        if (instance == null) {
            instance = new ManagerDataBase();
        }
        return instance;
    }

    private void connectDB() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/%s", DB_HOST, DB_PORT, DB_NAME);

        try {
            connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            ServerLogger.info("Подключение к БД установллено");
        } catch (SQLException e) {
            if (e.getSQLState().equals("3D000")) {
                ServerLogger.info("Базы данных не существует");
                createDB();
                ServerLogger.info("Создаем базу данных");
                connection = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
                ServerLogger.info("Подключение к БД установллено");
            } else {
                throw e;
            }
        }
    }

    private void createDB() throws SQLException {
        String url = String.format("jdbc:postgresql://%s:%s/", DB_HOST, DB_PORT);

        try {
            Connection conn = DriverManager.getConnection(url, DB_USER, DB_PASSWORD);
            Statement s = conn.createStatement();

            s.execute("CREATE DATABASE " + DB_NAME);
            ServerLogger.info("База данных {} создана", DB_NAME);
        } catch (Exception e) {
            ServerLogger.error("Произошла ошибка при создании БД");
        }
    }

    private void createTableDB() throws SQLException {
        String createUsersTable = """
                CREATE TABLE IF NOT EXISTS users (
                    id SERIAL PRIMARY KEY,
                    login VARCHAR(50) UNIQUE NOT NULL,
                    password VARCHAR(300) NOT NULL,
                    salt VARCHAR(64) NOT NULL,
                    date_created TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                );
                """;


        Statement s = connection.createStatement();
        s.execute(createUsersTable);
        ServerLogger.info("Таблица users инициализирована");

        String createRoutesTable = """
                CREATE TABLE IF NOT EXISTS routes (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(255) NOT NULL,
                    coordinates_x BIGINT CHECK (coordinates_x <= 108),
                    coordinates_y BIGINT CHECK (coordinates_y <= 20) ,
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
        s.execute(createRoutesTable);
        ServerLogger.info("Таблица routes инициализирована");

    }

    public synchronized long addDB(RouteClient routeClient, String author) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return -3;
        }

        try {
            String addRoute = """
                    INSERT INTO routes (name, coordinates_x, coordinates_y, from_x, from_y, from_z,
                    to_x, to_y, to_z, distance, price, author) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id, creationDate;
                    """;

            PreparedStatement pstmt = connection.prepareStatement(addRoute);

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

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                long id = rs.getLong("id");
                Timestamp creationDate = rs.getTimestamp("creationDate");

                ServerLogger.info("Элемент с ID {} успешно добавлен в БД {}", id, creationDate);

                return id;
            }

            return -1;

        } catch (SQLException e) {
            ServerLogger.error("Ошибка при sql запросе: {}", e.getMessage());
            return -1;
        }
    }

    public synchronized boolean updateDB(Route route, String author) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return false;
        }

        String updateRoute = """
                UPDATE routes SET 
                name = ?, 
                coordinates_x = ?, 
                coordinates_y = ?,
                from_x = ?, 
                from_y = ?, 
                from_z = ?,
                to_x = ?, 
                to_y = ?, 
                to_z = ?,
                distance = ?, 
                price = ?
                WHERE id = ? AND author = ?;
        """;

        try {
            PreparedStatement pstmt = connection.prepareStatement(updateRoute);

            pstmt.setString(1, route.getName());
            pstmt.setLong(2, route.getCoordinates().getX());
            pstmt.setLong(3, route.getCoordinates().getY());
            pstmt.setFloat(4, route.getFrom().getX());
            pstmt.setDouble(5, route.getFrom().getY());
            pstmt.setInt(6, route.getFrom().getZ());
            pstmt.setFloat(7, route.getTo().getX());
            pstmt.setDouble(8, route.getTo().getY());
            pstmt.setInt(9, route.getTo().getZ());
            pstmt.setInt(10, route.getDistance());
            pstmt.setBigDecimal(11, route.getPrice());
            pstmt.setLong(12, route.getId());
            pstmt.setString(13, author);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ServerLogger.info("Маршрут с ID {} обновлён пользователем {}", route.getId(), author);
                return true;
            } else {
                ServerLogger.debug("Маршрут с ID {} не найден у пользователя {}", route.getId(), author);
                return false;
            }
        } catch (SQLException e) {
            ServerLogger.error("Ошибка обновления маршрута: {}", e.getMessage());
            return false;
        }
    }

    public synchronized long deleteDB(long id) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return -3;
        }

        String deleteRoute = """
                DELETE FROM routes WHERE id = ?;""";

        try {
            PreparedStatement pstmt = connection.prepareStatement(deleteRoute);

            pstmt.setLong(1, id);

            int rows = pstmt.executeUpdate();

            if (rows > 0) {
                ServerLogger.info("Маршрут с ID {} удалён из БД", id);
                return id;
            } else {
                ServerLogger.info("Маршрут с ID {} не найден в БД", id);
                return 0;
            }

        } catch (SQLException e) {
            ServerLogger.error("Ошибка удаления маршрута из БД: {}", e.getMessage());
            return -1;
        }
    }

    public Route getDB(long id, String login) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return null;
        }

        String getRoute = "SELECT * FROM routes WHERE id = ? AND author = ?;";

        try {
            PreparedStatement pstmt = connection.prepareStatement(getRoute);

            pstmt.setLong(1, id);
            pstmt.setString(2, login);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                Route route = new Route(
                        rs.getLong("id"),
                        rs.getString("name"),
                        new Coordinates(rs.getLong("coordinates_x"), rs.getLong("coordinates_y")),
                        rs.getTimestamp("creationDate").toLocalDateTime().atZone(java.time.ZoneId.systemDefault()),
                        new Location(rs.getFloat("from_x"), rs.getDouble("from_y"), rs.getInt("from_z")),
                        new Location(rs.getFloat("to_x"), rs.getDouble("to_y"), rs.getInt("to_z")),
                        rs.getInt("distance"),
                        rs.getBigDecimal("price"),
                        rs.getString("author")
                );
                ServerLogger.info("Маршрут с ID {} получен из БД", id);
                return route;
            } else {
                ServerLogger.error("Маршрут с ID {} не найден в БД пользователя {}", id, login);
                return null;
            }
        } catch (SQLException e) {
            ServerLogger.error("Ошибка получения маршрута из БД: {}", e.getMessage());
            return null;
        }
    }

    public synchronized int clearDB(String author) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return -3;
        }

        String clearRoute = "DELETE FROM routes WHERE author = ?;";

        try {
            PreparedStatement pstmt = connection.prepareStatement(clearRoute);

            pstmt.setString(1, author);

            int rows = pstmt.executeUpdate();
            ServerLogger.info("Удаллено {} маршрутов пользователя {} из БД", rows, author);
            return rows;
        } catch (SQLException e) {
            ServerLogger.error("Ошибка удаления маршрутов пользователя {} {}", author, e.getMessage());
            return -1;
        }
    }

    public PriorityQueue<Route> getRoutesDB() {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return new PriorityQueue<Route>();
        }

        String getRoutes = "SELECT * FROM routes ORDER BY id";
        PriorityQueue<Route> routes = new PriorityQueue<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(getRoutes)) {

            while (rs.next()) {
                Route route = new Route(
                        rs.getLong("id"),
                        rs.getString("name"),
                        new Coordinates(rs.getLong("coordinates_x"), rs.getLong("coordinates_y")),
                        rs.getTimestamp("creationDate").toLocalDateTime().atZone(java.time.ZoneId.systemDefault()),
                        new Location(rs.getFloat("from_x"), rs.getDouble("from_y"), rs.getInt("from_z")),
                        new Location(rs.getFloat("to_x"), rs.getDouble("to_y"), rs.getInt("to_z")),
                        rs.getInt("distance"),
                        rs.getBigDecimal("price"),
                        rs.getString("author")
                );
                routes.add(route);
            }
            ServerLogger.info("Загружено {} маршрутов из БД", routes.size());
        } catch (SQLException e) {
            ServerLogger.error("Ошибка загрузки маршрутов из БД: {}", e.getMessage());
        }
        return routes;
    }

    public synchronized int deleteDistanceDB(int distance, String author) {
        if (connection == null) {
            ServerLogger.error("Нет подключения к БД");
            return -3;
        }

        String deleteRoutesDistance = "DELETE FROM routes WHERE distance = ? AND author = ?";

        try {
            PreparedStatement pstmt = connection.prepareStatement(deleteRoutesDistance);

            pstmt.setInt(1, distance);
            pstmt.setString(2, author);

            int rows = pstmt.executeUpdate();
            ServerLogger.info("Удалено {} маршрутов с distance = {} у пользователя {}", rows, distance, author);
            return rows;
        } catch (SQLException e) {
            ServerLogger.error("Ошибка удаления {}", e.getMessage());
            return -1;
        }
    }

    public synchronized boolean checkUserPassword(String login, String password) {
        if (!repeatConnect()) {
            ServerLogger.error("БД недоступна. Проверка пароля пропущена.");
            return false;
        }

        try {
            String getSalt = "SELECT salt FROM users WHERE login = ?";

            PreparedStatement saltStmt = connection.prepareStatement(getSalt);
            saltStmt.setString(1, login);

            ResultSet saltRs = saltStmt.executeQuery();

            if (!saltRs.next()) {
                return false;
            }

            String salt = saltRs.getString("salt");

            String inputHash = ManagerHasher.hash(password, salt);

            String checkPassword = "SELECT 1 FROM users WHERE login = ? AND password = ?";
            PreparedStatement pstmt = connection.prepareStatement(checkPassword);

            pstmt.setString(1, login);
            pstmt.setString(2, inputHash);

            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            String sqlState = e.getSQLState();
            if (sqlState != null && sqlState.startsWith("08")) {
                ServerLogger.debug("Соединение с БД потеряно");
                repeatConnect();
            } else {
                ServerLogger.error("Ошибка проверки пароля [{}]: {}", sqlState, e.getMessage());
            }
            return false;
        }
    }

    public synchronized boolean repeatConnect() {
        try {
            if (connection == null || connection.isClosed()) {
                connectDB();
                return true;
            }
            if (!connection.isValid(3)) {
                ServerLogger.debug("Соединение невалидно, переподключаюсь...");
                connectDB();
                return true;
            }
            return true;
        } catch (SQLException e) {
            ServerLogger.error("Не удалось проверить/восстановить соединение: {}", e.getMessage());
            try {
                connectDB();
                return true;
            } catch (SQLException ex) {
                ServerLogger.error("БД недоступна после попытки переподключения");
                return false;
            }
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                ServerLogger.info("Соединение с БД закрыто");
            }
        } catch (SQLException e) {
            ServerLogger.info("Ошибка закрытия БД: {}", e.getMessage());
        }
    }

    public Connection getConnection() {
        if (!repeatConnect()) {
            ServerLogger.debug("БД недоступна Подключение getConnection");
            return null;
        }
        return connection;
    }
}
