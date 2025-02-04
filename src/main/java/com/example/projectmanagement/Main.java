package com.example.projectmanagement;

import com.example.projectmanagement.ui.MainWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Инициализация базы данных
        DatabaseManager.initializeDatabase();

        // Импорт данных из Excel
        ExcelImporter.importFromExcel("project_tasks_filled_corrected_updated.xlsx");


        // Запуск графического интерфейса
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}


