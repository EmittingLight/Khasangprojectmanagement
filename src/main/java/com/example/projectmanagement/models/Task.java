package com.example.projectmanagement.models;

public class Task {
    private int id;
    private int projectId;
    private int responsibleId;
    private String taskName;
    private String startDate; // формат "yyyy-MM-dd"
    private int duration; // в днях
    private boolean finished;

    public Task(int id, int projectId, int responsibleId, String taskName, String startDate, int duration, boolean finished) {
        this.id = id;
        this.projectId = projectId;
        this.responsibleId = responsibleId;
        this.taskName = taskName;
        this.startDate = startDate;
        this.duration = duration;
        this.finished = finished;
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
        return taskName + " (Старт: " + startDate + ", длительность: " + duration + " дней, завершена: " + finished + ")";
    }
}

