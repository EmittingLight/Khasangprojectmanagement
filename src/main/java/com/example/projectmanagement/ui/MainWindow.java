package com.example.projectmanagement.ui;

import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Responsible;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.services.QueryService;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class MainWindow extends JFrame {
    private QueryService queryService;
    private JTextArea textArea;

    public MainWindow() {
        queryService = new QueryService();
        setTitle("Система управления проектами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.Y_AXIS));

        JButton btnActiveProjects = new JButton("Активные проекты");
        btnActiveProjects.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnActiveProjects.addActionListener(e -> showActiveProjects());
        panelButtons.add(btnActiveProjects);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnUnfinishedCount = new JButton("Незавершённые задачи по проекту");
        btnUnfinishedCount.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUnfinishedCount.addActionListener(e -> showUnfinishedTaskCountForProject());
        panelButtons.add(btnUnfinishedCount);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnTasksForResponsible = new JButton("Незавершённые задачи для ответственного");
        btnTasksForResponsible.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTasksForResponsible.addActionListener(e -> showUnfinishedTasksForResponsible());
        panelButtons.add(btnTasksForResponsible);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnTasksToday = new JButton("Задачи на сегодня");
        btnTasksToday.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTasksToday.addActionListener(e -> showTasksForToday());
        panelButtons.add(btnTasksToday);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnOverdue = new JButton("Просроченные задачи");
        btnOverdue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOverdue.addActionListener(e -> showOverdueResponsibles());
        panelButtons.add(btnOverdue);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        setLayout(new BorderLayout());
        add(scrollPane, BorderLayout.CENTER);
        add(panelButtons, BorderLayout.EAST);
    }

    private void showActiveProjects() {
        List<Project> projects = queryService.getActiveProjects();
        StringBuilder sb = new StringBuilder("Активные проекты:\n");
        for (Project p : projects) {
            sb.append(p).append("\n");
        }
        textArea.setText(sb.toString());
    }

    private void showUnfinishedTaskCountForProject() {
        List<String> projectNames = queryService.getAllProjects();
        if (projectNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет доступных проектов!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedProject = (String) JOptionPane.showInputDialog(
                this,
                "Выберите проект:",
                "Выбор проекта",
                JOptionPane.QUESTION_MESSAGE,
                null,
                projectNames.toArray(),
                projectNames.get(0)
        );

        if (selectedProject != null) {
            int count = queryService.getUnfinishedTaskCount(selectedProject);
            textArea.setText("Проект " + selectedProject + " имеет " + count + " незавершённых задач.");
        }
    }

    private void showUnfinishedTasksForResponsible() {
        List<String> responsibleNames = queryService.getAllResponsibles();
        if (responsibleNames.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет доступных ответственных!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String selectedResponsible = (String) JOptionPane.showInputDialog(
                this,
                "Выберите ответственного:",
                "Выбор ответственного",
                JOptionPane.QUESTION_MESSAGE,
                null,
                responsibleNames.toArray(),
                responsibleNames.get(0)
        );

        if (selectedResponsible != null) {
            List<Task> tasks = queryService.getUnfinishedTasksForResponsible(selectedResponsible);
            StringBuilder sb = new StringBuilder("Незавершённые задачи для " + selectedResponsible + ":\n");
            for (Task t : tasks) {
                sb.append(t).append("\n");
            }
            textArea.setText(sb.toString());
        }
    }

    private void showTasksForToday() {
        List<Task> tasks = queryService.getTasksForToday();
        StringBuilder sb = new StringBuilder("Задачи на сегодня:\n");
        for (Task t : tasks) {
            sb.append(t).append("\n");
        }
        textArea.setText(sb.toString());
    }

    private void showOverdueResponsibles() {
        List<Responsible> responsibles = queryService.getOverdueResponsibles();
        StringBuilder sb = new StringBuilder("Ответственные с просроченными задачами:\n");
        for (Responsible r : responsibles) {
            sb.append(r).append("\n");
        }
        textArea.setText(sb.toString());
    }
}
