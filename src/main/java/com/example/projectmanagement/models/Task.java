package com.example.projectmanagement.models;

public class Task {
    private int id;
    private int projectId;
    private int responsibleId;
    private String taskName;
    private String startDate;
    private int duration;
    private boolean finished;
    private String responsibleFullName;
    private String phone;
    private String email;

    public Task(int id, int projectId, int responsibleId, String taskName,
                String startDate, int duration, boolean finished,
                String responsibleFullName, String phone, String email) {
        this.id = id;
        this.projectId = projectId;
        this.responsibleId = responsibleId;
        this.taskName = taskName;
        this.startDate = startDate;
        this.duration = duration;
        this.finished = finished;
        this.responsibleFullName = responsibleFullName;
        this.phone = phone;
        this.email = email;
    }

    public String getResponsibleFullName() {
        return responsibleFullName;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public int getId() {
        return id;
    }

    public int getProjectId() {
        return projectId;
    }

    public int getResponsibleId() {
        return responsibleId;
    }

    public String getTaskName() {
        return taskName;
    }

    public String getStartDate() {
        return startDate;
    }

    public int getDuration() {
        return duration;
    }

    public boolean isFinished() {
        return finished;
    }

    @Override
    public String toString() {
        String status = finished ? "Завершена" : "В работе";
        return taskName + " (Старт: " + startDate + ", длительность: " + duration + " дней, статус: " + status + ")";
    }
}
