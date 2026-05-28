# Lab7 — Документация: установка, настройка, запуск, тестирование

---

## 1. Установка PostgreSQL

Удалить snap-версию если стоит:
```bash
sudo snap remove postgresql
```

Добавить официальный репозиторий и установить:
```bash
sudo apt update
sudo apt install -y postgresql postgresql-contrib
```

Проверить что установилось:
```bash
pg_lsclusters
```
Должна быть одна строка — кластер `main` на порту 5432, статус `online`.

---

## 2. Создание трёх шардов

Кластер `main` на порту 5432 уже есть. Создать ещё два:
```bash
sudo pg_createcluster 16 shard1
sudo pg_createcluster 16 shard2
```

Назначить порты:
```bash
sudo sed -i "s/^port = .*/port = 5433/" /etc/postgresql/16/shard1/postgresql.conf
sudo sed -i "s/^port = .*/port = 5434/" /etc/postgresql/16/shard2/postgresql.conf
```

Запустить оба:
```bash
sudo pg_ctlcluster 16 shard1 start
sudo pg_ctlcluster 16 shard2 start
```

Проверить что все три запущены:
```bash
pg_lsclusters
```

Ожидаемый вывод:
```
Ver Cluster Port Status Owner     Data directory
16  main    5432 online postgres  /var/lib/postgresql/16/main
16  shard1  5433 online postgres  /var/lib/postgresql/16/shard1
16  shard2  5434 online postgres  /var/lib/postgresql/16/shard2
```

---

## 3. Настройка доступа по паролю

Установить пароль `1234567890` на всех трёх шардах:
```bash
sudo -u postgres psql -p 5432 -c "ALTER USER postgres PASSWORD '1234567890';"
sudo -u postgres psql -p 5433 -c "ALTER USER postgres PASSWORD '1234567890';"
sudo -u postgres psql -p 5434 -c "ALTER USER postgres PASSWORD '1234567890';"
```

Разрешить подключение по паролю — выполнить для каждого кластера:
```bash
for cluster in main shard1 shard2; do
  sudo sed -i "s/^local   all             postgres                                peer/local   all             postgres                                md5/" \
    /etc/postgresql/16/$cluster/pg_hba.conf
  echo "host    all             all             127.0.0.1/32            md5" | \
    sudo tee -a /etc/postgresql/16/$cluster/pg_hba.conf
done
```

Перезапустить все кластеры:
```bash
sudo pg_ctlcluster 16 main   restart
sudo pg_ctlcluster 16 shard1 restart
sudo pg_ctlcluster 16 shard2 restart
```

Проверить что подключение работает (вводить пароль `1234567890`):
```bash
psql -h 127.0.0.1 -p 5432 -U postgres -c "\l"
psql -h 127.0.0.1 -p 5433 -U postgres -c "\l"
psql -h 127.0.0.1 -p 5434 -U postgres -c "\l"
```
Каждая команда должна показать список баз данных.

---

## 4. Настройка проекта

Файл `shards.properties` должен лежать рядом с `server-1.0.jar`.
Содержимое файла:
```properties
shard.0.host=localhost
shard.0.port=5432
shard.0.name=route0
shard.0.user=postgres
shard.0.password=1234567890

shard.1.host=localhost
shard.1.port=5433
shard.1.name=route1
shard.1.user=postgres
shard.1.password=1234567890

shard.2.host=localhost
shard.2.port=5434
shard.2.name=route2
shard.2.user=postgres
shard.2.password=1234567890
```

Базы данных `route0`, `route1`, `route2` создавать вручную не нужно —
сервер создаёт их автоматически при первом запуске.

---

## 5. Сборка проекта

```bash
cd ~/IdeaProjects/Lab7
./gradlew clean buildAll
```

Готовые JAR файлы появятся в `build/libs/`:
- `server-1.0.jar` — сервер
- `client-1.0.jar` — клиент

---

## 6. Запуск

### Запустить сервер
```bash
cd ~/IdeaProjects/Lab7
java -jar build/libs/server-1.0.jar
```

При первом запуске в логах будет:
```
Создана БД route0 для шарда 0
Создана и подключена БД для шарда 0
Создана БД route1 для шарда 1
...
Таблицы инициализированы в шарде 0
Таблицы инициализированы в шарде 1
Таблицы инициализированы в шарде 2
Сервер запущен на порту 8080
```

При повторных запусках:
```
Подключён шард 0: jdbc:postgresql://localhost:5432/route0
Подключён шард 1: jdbc:postgresql://localhost:5433/route1
Подключён шард 2: jdbc:postgresql://localhost:5434/route2
Сервер запущен на порту 8080
```

### Запустить клиент (в отдельном терминале)
```bash
cd ~/IdeaProjects/Lab7
java -jar build/libs/client-1.0.jar
```

### Остановить сервер
`Ctrl+C` в терминале где запущен сервер.

---

## 7. Управление шардами

### Запустить все шарды
```bash
sudo pg_ctlcluster 16 main   start
sudo pg_ctlcluster 16 shard1 start
sudo pg_ctlcluster 16 shard2 start
```

### Остановить все шарды
```bash
sudo pg_ctlcluster 16 main   stop
sudo pg_ctlcluster 16 shard1 stop
sudo pg_ctlcluster 16 shard2 stop
```

### Остановить один шард
```bash
sudo pg_ctlcluster 16 shard1 stop   # шард 1 (порт 5433)
```

### Запустить один шард
```bash
sudo pg_ctlcluster 16 shard1 start  # шард 1 (порт 5433)
```

### Проверить статус
```bash
pg_lsclusters
```

---

## 8. Просмотр данных в шардах

Подключиться к конкретному шарду и посмотреть данные (пароль `1234567890`):

```bash
# Шард 0
psql -h 127.0.0.1 -p 5432 -U postgres -d route0 \
  -c "SELECT id, name, author FROM routes;"

# Шард 1
psql -h 127.0.0.1 -p 5433 -U postgres -d route1 \
  -c "SELECT id, name, author FROM routes;"

# Шард 2
psql -h 127.0.0.1 -p 5434 -U postgres -d route2 \
  -c "SELECT id, name, author FROM routes;"
```

Посмотреть всех пользователей (хранятся только в шарде 0):
```bash
psql -h 127.0.0.1 -p 5432 -U postgres -d route0 \
  -c "SELECT id, login, date_created FROM users;"
```

Посмотреть количество маршрутов в каждом шарде:
```bash
psql -h 127.0.0.1 -p 5432 -U postgres -d route0 -c "SELECT COUNT(*) FROM routes;"
psql -h 127.0.0.1 -p 5433 -U postgres -d route1 -c "SELECT COUNT(*) FROM routes;"
psql -h 127.0.0.1 -p 5434 -U postgres -d route2 -c "SELECT COUNT(*) FROM routes;"
```

---

## 9. Тестирование шардирования

Шард для каждого пользователя определяется по логину.
Заранее известные шарды для тестовых логинов:
```
Charlie  →  шард 0  (порт 5432, база route0)
Alice    →  шард 1  (порт 5433, база route1)
Bob      →  шард 2  (порт 5434, база route2)
```

### Порядок теста

1. Запустить сервер и клиент.

2. Зарегистрировать и добавить маршрут от каждого пользователя:
```
register Charlie pass123
```
Войти, выполнить `add`, ввести данные маршрута, выйти (`logout`).
Повторить для Alice и Bob.

3. Проверить в psql что данные разложены по разным шардам:
```bash
psql -h 127.0.0.1 -p 5432 -U postgres -d route0 -c "SELECT id, name, author FROM routes;"
psql -h 127.0.0.1 -p 5433 -U postgres -d route1 -c "SELECT id, name, author FROM routes;"
psql -h 127.0.0.1 -p 5434 -U postgres -d route2 -c "SELECT id, name, author FROM routes;"
```
Маршрут Charlie будет только в route0, Alice только в route1, Bob только в route2.

4. Проверить что ID уникальны глобально — ни один ID не повторяется между шардами.

---

## 10. Тестирование реконнекта

### Порядок теста

1. Запустить сервер и клиент, войти как Alice.

2. Выполнить `show` — маршруты отображаются.

3. В отдельном терминале остановить шард 1:
```bash
sudo pg_ctlcluster 16 shard1 stop
```

4. В клиенте от Alice выполнить `add` с любыми данными.
   Клиент получит сообщение об ошибке.
   В логах сервера появится:
```
Шард 1 требует переподключения
Не удалось подключить шард 1: Connection refused
```

5. Войти как Charlie и выполнить `add`.
   Маршрут добавится успешно — шард 0 работает независимо.

6. Поднять шард 1 обратно:
```bash
sudo pg_ctlcluster 16 shard1 start
```

7. В клиенте от Alice снова выполнить `add`.
   Маршрут добавится. В логах сервера:
```
Шард 1 требует переподключения
Шард 1 переподключён
Элемент ID=X добавлен в шард 1 (Alice)
```

---

## 11. Сброс данных

Удалить все базы данных (сервер создаст их заново при следующем запуске):
```bash
psql -h 127.0.0.1 -p 5432 -U postgres -c "DROP DATABASE IF EXISTS route0;"
psql -h 127.0.0.1 -p 5433 -U postgres -d postgres -c "DROP DATABASE IF EXISTS route1;"
psql -h 127.0.0.1 -p 5434 -U postgres -d postgres -c "DROP DATABASE IF EXISTS route2;"
```