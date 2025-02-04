package com.example.projectmanagement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:projectmanagement.db";

    // Получить подключение к базе
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    // Инициализация базы данных (создание таблиц, вставка стартовых данных)
    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Создание таблицы проектов
            String sqlProjects = "CREATE TABLE IF NOT EXISTS projects (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE" +
                    ");";
            stmt.execute(sqlProjects);

            // Создание таблицы ответственных
            String sqlResponsibles = "CREATE TABLE IF NOT EXISTS responsibles (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "name TEXT NOT NULL UNIQUE," +
                    "contact TEXT" +
                    ");";
            stmt.execute(sqlResponsibles);

            // Создание таблицы задач
            String sqlTasks = "CREATE TABLE IF NOT EXISTS tasks (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "project_id INTEGER NOT NULL," +
                    "responsible_id INTEGER NOT NULL," +
                    "task_name TEXT NOT NULL," +
                    "start_date TEXT NOT NULL," +
                    "duration INTEGER NOT NULL," +
                    "finished INTEGER NOT NULL," +
                    "FOREIGN KEY (project_id) REFERENCES projects(id)," +
                    "FOREIGN KEY (responsible_id) REFERENCES responsibles(id)" +
                    ");";
            stmt.execute(sqlTasks);

            // Вставка стартовых данных для проектов и ответственных,
            // если их ещё нет (например, можно проверить с помощью INSERT OR IGNORE)
            String insertProjects = "INSERT OR IGNORE INTO projects (name) VALUES " +
                    "('Проект A'), ('Проект B'), ('Проект C');";
            stmt.execute(insertProjects);

            String insertResponsibles = "INSERT OR IGNORE INTO responsibles (name, contact) VALUES " +
                    "('Иван Иванов', 'ivan@example.com'), " +
                    "('Петр Петров', 'petr@example.com'), " +
                    "('Сергей Сергеев', 'sergey@example.com');";
            stmt.execute(insertResponsibles);

            // Можно добавить несколько задач для тестирования:
            String insertTasks = "INSERT OR IGNORE INTO tasks (project_id, responsible_id, task_name, start_date, duration, finished) VALUES " +
                    // Предположим, что Проект A имеет id=1, Проект B id=2, и т.д.
                    "(1, 1, 'Задача 1 проекта A', '2025-02-01', 5, 0)," +
                    "(1, 2, 'Задача 2 проекта A', '2025-01-28', 3, 1)," +
                    "(2, 3, 'Задача 1 проекта B', '2025-02-03', 4, 0);" ;
            stmt.execute(insertTasks);

            System.out.println("База данных и таблицы инициализированы.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

