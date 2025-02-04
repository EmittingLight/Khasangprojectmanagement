package com.example.projectmanagement;

import com.example.projectmanagement.ui.MainWindow;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Инициализируем базу данных
        DatabaseManager.initializeDatabase();

        // Запускаем GUI (Swing-объекты следует создавать в Event Dispatch Thread)
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}

