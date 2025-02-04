package com.example.projectmanagement;

import com.example.projectmanagement.ui.MainWindow;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        // Инициализация базы данных
        DatabaseManager.initializeDatabase();

        // Импорт данных из Excel (укажите корректный путь к файлу)
        ExcelImporter.importFromExcel("project_management_extended.xlsx");

        // Запуск графического интерфейса
        SwingUtilities.invokeLater(() -> {
            MainWindow window = new MainWindow();
            window.setVisible(true);
        });
    }
}


