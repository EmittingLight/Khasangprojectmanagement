package com.example.projectmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:projectmanagement.db";

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DB_URL);
        conn.setAutoCommit(true); // Включаем автоматическое подтверждение изменений

        // Включаем поддержку внешних ключей для SQLite
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("PRAGMA foreign_keys = ON");
        }

        return conn;
    }

    // Инициализация базы данных (создание таблиц, вставка стартовых данных)
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {

            // Создаём таблицу projects (проекты)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS projects ("
                    + "id INTEGER PRIMARY KEY, "
                    + "name TEXT UNIQUE"
                    + ")");

            // Создаём таблицу responsibles (ответственные)
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS responsibles (" +
                    "id INTEGER PRIMARY KEY, " +
                    "name TEXT, " +  // убрали UNIQUE!
                    "contact TEXT" +
                    ")");


            // Создаём таблицу tasks (задачи) с внешними ключами на projects и responsibles
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasks ("
                    + "id INTEGER PRIMARY KEY, "
                    + "project_id INTEGER, "
                    + "responsible_id INTEGER, "
                    + "task_name TEXT, "
                    + "start_date TEXT, "
                    + "duration INTEGER, "
                    + "finished INTEGER, "
                    + "UNIQUE(task_name, start_date), "
                    + "FOREIGN KEY (project_id) REFERENCES projects(id), "
                    + "FOREIGN KEY (responsible_id) REFERENCES responsibles(id)"
                    + ")");

            // Вставка стартовых данных в таблицу projects
            stmt.executeUpdate("INSERT OR IGNORE INTO projects (name) VALUES ('Разработка ПО')");
            stmt.executeUpdate("INSERT OR IGNORE INTO projects (name) VALUES ('Строительство')");
            stmt.executeUpdate("INSERT OR IGNORE INTO projects (name) VALUES ('Маркетинг')");
            stmt.executeUpdate("INSERT OR IGNORE INTO projects (name) VALUES ('Образование')");
            stmt.executeUpdate("INSERT OR IGNORE INTO projects (name) VALUES ('Здравоохранение')");

            // Вставка стартовых данных в таблицу responsibles
            stmt.executeUpdate("INSERT OR IGNORE INTO responsibles (name, contact) VALUES ('Иван Петров', '+7-901-234-56-78, ivan@example.com')");
            stmt.executeUpdate("INSERT OR IGNORE INTO responsibles (name, contact) VALUES ('Анна Смирнова', '+7-902-345-67-89, anna@example.com')");
            stmt.executeUpdate("INSERT OR IGNORE INTO responsibles (name, contact) VALUES ('Сергей Козлов', '+7-903-456-78-90, sergey@example.com')");
            stmt.executeUpdate("INSERT OR IGNORE INTO responsibles (name, contact) VALUES ('Мария Иванова', '+7-904-567-89-01, maria@example.com')");
            stmt.executeUpdate("INSERT OR IGNORE INTO responsibles (name, contact) VALUES ('Алексей Сидоров', '+7-905-678-90-12, alexey@example.com')");

            System.out.println("База данных успешно инициализирована.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
