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
    private JTextField responsibleField; // для ввода имени ответственного

    public MainWindow() {
        queryService = new QueryService();
        setTitle("Система управления проектами");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        initUI();
    }

    private void initUI() {
        // Создаем панель для кнопок с вертикальным расположением
        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new BoxLayout(panelButtons, BoxLayout.Y_AXIS));

        // Создаем кнопку "Активные проекты"
        JButton btnActiveProjects = new JButton("Активные проекты");
        btnActiveProjects.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnActiveProjects.addActionListener(e -> showActiveProjects());
        panelButtons.add(btnActiveProjects);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10))); // Добавляем вертикальный отступ

        // Создаем кнопку "Незавершённые задачи по проекту"
        JButton btnUnfinishedCount = new JButton("Незавершённые задачи по проекту");
        btnUnfinishedCount.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnUnfinishedCount.addActionListener(e -> showUnfinishedTaskCount());
        panelButtons.add(btnUnfinishedCount);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        // Создаем кнопку "Незавершённые задачи для ответственного"
        JButton btnTasksForResponsible = new JButton("Незавершённые задачи для ответственного");
        btnTasksForResponsible.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTasksForResponsible.addActionListener(e -> showUnfinishedTasksForResponsible());
        panelButtons.add(btnTasksForResponsible);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        // Создаем кнопку "Задачи на сегодня"
        JButton btnTasksToday = new JButton("Задачи на сегодня");
        btnTasksToday.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTasksToday.addActionListener(e -> showTasksForToday());
        panelButtons.add(btnTasksToday);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        // Создаем кнопку "Просроченные задачи"
        JButton btnOverdue = new JButton("Просроченные задачи");
        btnOverdue.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOverdue.addActionListener(e -> showOverdueResponsibles());
        panelButtons.add(btnOverdue);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        // Можно добавить поле ввода имени ответственного в эту же панель или разместить его отдельно
        panelButtons.add(new JLabel("Ответственный:"));
        responsibleField = new JTextField(15);
        responsibleField.setMaximumSize(new Dimension(200, 25));
        panelButtons.add(responsibleField);

        // Создаем текстовую область для вывода результатов
        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        // Настраиваем основное окно: текстовая область по центру, панель с кнопками справа
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

    private void showUnfinishedTaskCount() {
        String projectName = JOptionPane.showInputDialog(this, "Введите название проекта:");
        if (projectName != null && !projectName.isEmpty()) {
            int count = queryService.getUnfinishedTaskCount(projectName);
            textArea.setText("Проект " + projectName + " имеет " + count + " незавершённых задач.");
        }
    }

    private void showUnfinishedTasksForResponsible() {
        String name = responsibleField.getText().trim();
        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Введите имя ответственного в поле!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }
        List<Task> tasks = queryService.getUnfinishedTasksForResponsible(name);
        StringBuilder sb = new StringBuilder("Незавершённые задачи для " + name + ":\n");
        for (Task t : tasks) {
            sb.append(t).append("\n");
        }
        textArea.setText(sb.toString());
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
