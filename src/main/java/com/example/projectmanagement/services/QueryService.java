package com.example.projectmanagement.services;

import com.example.projectmanagement.DatabaseManager;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.Responsible;

import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // Получить все проекты
    public List<String> getAllProjects() {
        List<String> projects = new ArrayList<>();
        String sql = "SELECT name FROM projects";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    public List<Project> getActiveProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id, p.name " +
                "FROM projects p " +
                "JOIN tasks t ON p.id = t.project_id " +
                "WHERE LOWER(TRIM(t.finished)) COLLATE NOCASE = 'нет' OR t.finished = 0";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                projects.add(new Project(rs.getInt("id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return projects;
    }

    public int getUnfinishedTaskCount(String projectName) {
        if (projectName == null || projectName.trim().isEmpty()) {
            return 0;
        }

        String sql = "SELECT COUNT(t.id) as count " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "WHERE LOWER(TRIM(p.name)) COLLATE NOCASE = LOWER(TRIM(?)) COLLATE NOCASE " +
                "AND (LOWER(TRIM(t.finished)) COLLATE NOCASE = 'нет' OR t.finished = 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, projectName.trim());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return Math.max(rs.getInt("count"), 0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public List<Task> getUnfinishedTasksForResponsible(String responsibleName) {
        // Получаем все незавершённые задачи
        List<Task> allTasks = getAllUnfinishedTasks();
        List<Task> result = new ArrayList<>();

        // Приводим введённое имя к нижнему регистру
        String searchLower = responsibleName.toLowerCase();

        // Фильтруем в Java
        for (Task t : allTasks) {
            // Берём fullName из Task и тоже приводим к нижнему регистру
            String fullNameLower = t.getResponsibleFullName().toLowerCase();
            if (fullNameLower.contains(searchLower)) {
                result.add(t);
            }
        }

        return result;
    }


    public boolean isResponsibleExists(String responsibleName) {
        // Получаем всех ответственных из БД
        List<String> allResponsibles = getAllResponsibles(); // уже есть метод getAllResponsibles()

        // Приводим к нижнему регистру то, что ввёл пользователь
        String search = responsibleName.trim().toLowerCase();

        // Ищем совпадения
        for (String rName : allResponsibles) {
            // rName может быть "Анна Смирнова", "Иван Петров" и т.д.
            // Приводим к нижнему регистру
            if (rName.toLowerCase().contains(search)) {
                return true; // Нашли хоть одно совпадение
            }
        }
        // Если не нашли
        return false;
    }


    public List<Task> getTasksForToday() {
        List<Task> tasks = new ArrayList<>();
        Set<String> uniqueTasks = new HashSet<>();
        String today = LocalDate.now().format(formatter);

        String sql = "SELECT DISTINCT t.id, t.project_id, t.responsible_id, " +
                "t.task_name, t.start_date, t.duration, t.finished, " +
                "r.name, r.contact " + // Убрали r.email, его нет в таблице!
                "FROM tasks t " +
                "JOIN responsibles r ON t.responsible_id = r.id " +
                "WHERE DATE(t.start_date) = DATE(?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskIdentifier = rs.getString("task_name") + rs.getString("start_date");
                if (!uniqueTasks.contains(taskIdentifier)) {
                    String contact = rs.getString("contact");
                    String phone = contact.split(",")[0].trim();  // Телефон
                    String email = extractEmail(contact);  // Email

                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getInt("project_id"),
                            rs.getInt("responsible_id"),
                            rs.getString("task_name"),
                            rs.getString("start_date"),
                            rs.getInt("duration"),
                            rs.getInt("finished") == 1,
                            rs.getString("name"),  // ФИО
                            phone,  // Телефон
                            email   // Email
                    ));
                    uniqueTasks.add(taskIdentifier);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    private String extractEmail(String contact) {
        if (contact == null || !contact.contains(",")) return "Неизвестно";
        String[] parts = contact.split(",");
        return parts.length > 1 ? parts[1].trim() : "Неизвестно";
    }


    public List<Responsible> getOverdueResponsibles() {
        List<Responsible> responsibles = new ArrayList<>();
        String sql = "SELECT DISTINCT r.id, r.name, r.contact " +
                "FROM responsibles r " +
                "JOIN tasks t ON r.id = t.responsible_id " +
                "WHERE DATE(t.start_date) < DATE('now') " +
                "AND (LOWER(TRIM(t.finished)) COLLATE NOCASE = 'нет' OR t.finished = 0)";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                responsibles.add(new Responsible(
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("contact")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responsibles;
    }

    public List<String> getAllResponsibles() {
        List<String> responsibles = new ArrayList<>();
        String sql = "SELECT name FROM responsibles";

        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                responsibles.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responsibles;
    }

    public List<Task> getAllUnfinishedTasks() {
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT DISTINCT t.id, t.project_id, t.responsible_id, " +
                "r.name AS full_name, " +
                "CASE WHEN INSTR(r.contact, ',') > 0 THEN SUBSTR(r.contact, 1, INSTR(r.contact, ',') - 1) ELSE r.contact END AS phone, " +
                "CASE WHEN INSTR(r.contact, ',') > 0 THEN SUBSTR(r.contact, INSTR(r.contact, ',') + 2) ELSE 'Нет email' END AS email, " +
                "t.task_name, t.start_date, t.duration, t.finished " +
                "FROM tasks t " +
                "JOIN responsibles r ON t.responsible_id = r.id " +
                "WHERE (LOWER(TRIM(t.finished)) = 'нет' OR t.finished = 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String fullName = rs.getString("full_name").trim();
                String phone = rs.getString("phone").trim();
                String email = rs.getString("email").trim();

                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getInt("responsible_id"),
                        rs.getString("task_name"),
                        rs.getString("start_date"),
                        rs.getInt("duration"),
                        rs.getInt("finished") == 1,
                        fullName, phone, email
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }
}