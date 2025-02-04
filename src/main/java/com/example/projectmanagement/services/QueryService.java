package com.example.projectmanagement.services;

import com.example.projectmanagement.DatabaseManager;
import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.models.Responsible;

import javax.swing.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class QueryService {

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    // 1. Проекты в работе (есть хотя бы одна незавершённая задача)
    public List<Project> getActiveProjects() {
        List<Project> projects = new ArrayList<>();
        String sql = "SELECT DISTINCT p.id, p.name " +
                "FROM projects p " +
                "JOIN tasks t ON p.id = t.project_id " +
                "WHERE t.finished = 0";
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

    // 2. Количество незавершённых задач по проекту
    public int getUnfinishedTaskCount(String projectName) {
        // Проверяем, что введено латинское название проекта
        if (!projectName.matches("^[A-Za-z0-9 ]+$")) {
            JOptionPane.showMessageDialog(null,
                    "Ошибка: Название проекта должно содержать только латинские буквы и цифры!",
                    "Некорректный ввод", JOptionPane.ERROR_MESSAGE);
            return -1; // Возвращаем -1, чтобы показать, что произошла ошибка
        }

        // Приводим название проекта к нижнему регистру и убираем лишние пробелы
        projectName = projectName.trim().toLowerCase();

        String sql = "SELECT COUNT(t.id) as count " +
                "FROM tasks t " +
                "JOIN projects p ON t.project_id = p.id " +
                "WHERE LOWER(TRIM(p.name)) = ? AND t.finished = 0";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, projectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("count"); // Возвращаем количество незавершённых задач
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0; // Если ничего не найдено
    }


    public List<Task> getUnfinishedTasksForResponsible(String responsibleName) {
        List<Task> tasks = new ArrayList<>();
        Set<String> uniqueTasks = new HashSet<>();

        String sql = "SELECT DISTINCT t.task_name, t.start_date, t.duration, t.finished, t.project_id, t.responsible_id, t.id " +
                "FROM tasks t " +
                "JOIN responsibles r ON t.responsible_id = r.id " +
                "WHERE LOWER(TRIM(r.name)) = LOWER(TRIM(?)) AND t.finished = 0";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, responsibleName.trim().toLowerCase());
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



    // 4. Задачи на сегодня (предположим, что задачи «на сегодня» – те, у которых start_date = сегодня)
    public List<Task> getTasksForToday() {
        List<Task> tasks = new ArrayList<>();
        Set<String> uniqueTasks = new HashSet<>(); // Для отслеживания уникальных задач
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        String sql = "SELECT DISTINCT t.task_name, t.start_date, t.duration, t.finished, t.project_id, t.responsible_id, t.id " +
                "FROM tasks t WHERE t.start_date = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, today);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                String taskIdentifier = rs.getString("task_name") + rs.getString("start_date"); // Уникальный ключ
                if (!uniqueTasks.contains(taskIdentifier)) { // Проверяем, есть ли такая задача уже
                    tasks.add(new Task(
                            rs.getInt("id"),
                            rs.getInt("project_id"),
                            rs.getInt("responsible_id"),
                            rs.getString("task_name"),
                            rs.getString("start_date"),
                            rs.getInt("duration"),
                            rs.getInt("finished") == 1
                    ));
                    uniqueTasks.add(taskIdentifier); // Добавляем уникальный ключ
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tasks;
    }





    // 5. Ответственные с просроченными задачами
    // Здесь мы считаем, что задача просрочена, если: current_date > (start_date + duration)
    public List<Responsible> getOverdueResponsibles() {
        List<Responsible> responsibles = new ArrayList<>();
        String sql = "SELECT DISTINCT r.id, r.name, r.contact, t.start_date, t.duration " +
                "FROM responsibles r " +
                "JOIN tasks t ON r.id = t.responsible_id " +
                "WHERE t.finished = 0";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String startDateStr = rs.getString("start_date");
                int duration = rs.getInt("duration");
                LocalDate startDate = LocalDate.parse(startDateStr, formatter);
                LocalDate deadline = startDate.plusDays(duration);
                if (LocalDate.now().isAfter(deadline)) {
                    // Если ответственный уже есть в списке, то можно не добавлять повторно.
                    Responsible r = new Responsible(rs.getInt("id"), rs.getString("name"), rs.getString("contact"));
                    if (!responsibles.contains(r)) {
                        responsibles.add(r);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return responsibles;
    }
}

