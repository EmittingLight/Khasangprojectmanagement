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
        List<Task> tasks = new ArrayList<>();
        String sql = "SELECT DISTINCT t.id, t.project_id, t.responsible_id, t.task_name, t.start_date, t.duration, t.finished " +
                "FROM tasks t " +
                "JOIN responsibles r ON t.responsible_id = r.id " +
                "WHERE LOWER(TRIM(r.name)) COLLATE NOCASE = LOWER(TRIM(?)) COLLATE NOCASE " +
                "AND (LOWER(TRIM(t.finished)) COLLATE NOCASE = 'нет' OR t.finished = 0)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responsibleName.trim());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                tasks.add(new Task(
                        rs.getInt("id"),
                        rs.getInt("project_id"),
                        rs.getInt("responsible_id"),
                        rs.getString("task_name"),
                        rs.getString("start_date"),
                        rs.getInt("duration"),
                        rs.getInt("finished") == 1
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }

    public List<Task> getTasksForToday() {
        List<Task> tasks = new ArrayList<>();
        Set<String> uniqueTasks = new HashSet<>();
        String today = LocalDate.now().format(formatter);

        String sql = "SELECT DISTINCT t.id, t.project_id, t.responsible_id, t.task_name, t.start_date, t.duration, t.finished " +
                "FROM tasks t WHERE DATE(t.start_date) = DATE(?)";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskIdentifier = rs.getString("task_name") + rs.getString("start_date");
                if (!uniqueTasks.contains(taskIdentifier)) {
                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getInt("project_id"),
                            rs.getInt("responsible_id"),
                            rs.getString("task_name"),
                            rs.getString("start_date"),
                            rs.getInt("duration"),
                            rs.getInt("finished") == 1
                    ));
                    uniqueTasks.add(taskIdentifier);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
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
}