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
        return conn;
    }


    // Инициализация базы данных (создание таблиц, вставка стартовых данных)
    public static void initializeDatabase() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            // Создаём таблицы, если их нет
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS projects (id INTEGER PRIMARY KEY, name TEXT UNIQUE)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS responsibles (id INTEGER PRIMARY KEY, name TEXT UNIQUE, contact TEXT)");
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS tasks (id INTEGER PRIMARY KEY, project_id INTEGER, responsible_id INTEGER, task_name TEXT, start_date TEXT, duration INTEGER, finished INTEGER, UNIQUE(task_name, start_date))");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

