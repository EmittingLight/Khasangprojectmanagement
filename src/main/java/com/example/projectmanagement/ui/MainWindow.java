package com.example.projectmanagement.ui;

import com.example.projectmanagement.models.Project;
import com.example.projectmanagement.models.Responsible;
import com.example.projectmanagement.models.Task;
import com.example.projectmanagement.services.QueryService;

import javax.swing.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

        // === Панель для поиска ответственного ===
        JPanel responsiblePanel = new JPanel();
        responsiblePanel.setLayout(new BoxLayout(responsiblePanel, BoxLayout.Y_AXIS));
        responsiblePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel responsibleLabel = new JLabel("<html><div style='text-align:center;'><u><font color='red'>Незавершённые проекты</font></u></div></html>", SwingConstants.CENTER);
        responsibleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);


        JTextField responsibleSearchField = new JTextField("Введите ФИО ответственного", 15);
        responsibleSearchField.setMaximumSize(new Dimension(200, 30));
        responsibleSearchField.setAlignmentX(Component.CENTER_ALIGNMENT);
        responsibleSearchField.setForeground(Color.GRAY);

// Поведение плейсхолдера
        responsibleSearchField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent e) {
                if (responsibleSearchField.getText().equals("Введите ФИО ответственного")) {
                    responsibleSearchField.setText("");
                    responsibleSearchField.setForeground(Color.BLACK);
                }
            }

            public void focusLost(java.awt.event.FocusEvent e) {
                if (responsibleSearchField.getText().isEmpty()) {
                    responsibleSearchField.setForeground(Color.GRAY);
                    responsibleSearchField.setText("Введите ФИО ответственного");
                }
            }
        });


        JButton btnSearchResponsible = new JButton("Поиск проектов");
        btnSearchResponsible.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSearchResponsible.addActionListener(e -> {
            String inputName = responsibleSearchField.getText().trim();
            if (!inputName.isEmpty()) {
                showUnfinishedTasksForResponsible(inputName);
            } else {
                JOptionPane.showMessageDialog(this, "Введите имя ответственного!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            }
        });

        responsiblePanel.add(responsibleLabel);
        responsiblePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        responsiblePanel.add(responsibleSearchField);
        responsiblePanel.add(Box.createRigidArea(new Dimension(0, 5)));
        responsiblePanel.add(btnSearchResponsible);

        panelButtons.add(responsiblePanel);
        panelButtons.add(Box.createRigidArea(new Dimension(0, 10)));

        JButton btnTasksForResponsible = new JButton("Выбрать ответственного из списка");
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


    private void showUnfinishedTasksForResponsible(String responsibleName) {
        List<Task> tasks = queryService.getUnfinishedTasksForResponsible(responsibleName);

        if (tasks.isEmpty()) {
            textArea.setText("Незавершённых задач по запросу \"" + responsibleName + "\" не найдено.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Set<Integer> printedResponsibleIds = new HashSet<>();

        for (Task task : tasks) {
            int responsibleId = task.getResponsibleId();
            String fullName = task.getResponsibleFullName().trim();
            String phone = task.getPhone();
            String email = task.getEmail();

            // Проверяем по ID, а не по имени
            if (!printedResponsibleIds.contains(responsibleId)) {
                if (!printedResponsibleIds.isEmpty()) {
                    sb.append("\n"); // Отделяем блоки
                }
                sb.append("Незавершённые задачи для: ").append(fullName).append("\n");
                sb.append("Телефон: ").append(phone).append("\n");
                sb.append("Email: ").append(email).append("\n\n");
                printedResponsibleIds.add(responsibleId);
            }

            sb.append(task).append("\n");
        }

        textArea.setText(sb.toString());
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
        List<Responsible> responsibles = queryService.getAllResponsibles();
        if (responsibles.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Нет доступных ответственных!", "Ошибка", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Преобразуем в массив строк для отображения в списке
        String[] displayNames = responsibles.stream()
                .map(r -> r.getName() + " (" + r.getContact() + ")")
                .toArray(String[]::new);

        String selectedDisplay = (String) JOptionPane.showInputDialog(
                this,
                "Выберите ответственного:",
                "Выбор ответственного",
                JOptionPane.QUESTION_MESSAGE,
                null,
                displayNames,
                displayNames[0]
        );

        if (selectedDisplay != null) {
            int selectedIndex = -1;
            for (int i = 0; i < displayNames.length; i++) {
                if (displayNames[i].equals(selectedDisplay)) {
                    selectedIndex = i;
                    break;
                }
            }

            if (selectedIndex != -1) {
                int selectedId = responsibles.get(selectedIndex).getId();
                showUnfinishedTasksForResponsibleById(selectedId); // используем новый метод
            }
        }
    }

    private void showUnfinishedTasksForResponsibleById(int responsibleId) {
        List<Task> tasks = queryService.getUnfinishedTasksByResponsibleId(responsibleId);

        if (tasks.isEmpty()) {
            textArea.setText("Незавершённых задач для выбранного ответственного не найдено.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        Task firstTask = tasks.get(0);
        sb.append("Незавершённые задачи для: ").append(firstTask.getResponsibleFullName()).append("\n");
        sb.append("Телефон: ").append(firstTask.getPhone()).append("\n");
        sb.append("Email: ").append(firstTask.getEmail()).append("\n\n");

        for (Task task : tasks) {
            sb.append(task).append("\n");
        }

        textArea.setText(sb.toString());
    }



    private void showTasksForToday() {
        List<Task> tasks = queryService.getTasksForToday();
        StringBuilder sb = new StringBuilder("Задачи на сегодня:\n");
        for (Task t : tasks) {
            sb.append(t.getResponsibleFullName()).append("\n");
            sb.append("Телефон: ").append(t.getPhone()).append("\n");
            sb.append("Email: ").append(t.getEmail()).append("\n");
            sb.append(t).append("\n\n");
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
