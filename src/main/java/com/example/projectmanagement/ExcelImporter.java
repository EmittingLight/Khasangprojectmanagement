package com.example.projectmanagement;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ExcelImporter {

    /**
     * Импортирует данные из excel-файла в базу данных.
     * @param excelFilePath путь к файлу (например, "data.xlsx")
     */
    public static void importFromExcel(String excelFilePath) {
        try (FileInputStream fis = new FileInputStream(excelFilePath);
             Workbook workbook = new XSSFWorkbook(fis);
             Connection conn = DatabaseManager.getConnection()) {

            Sheet sheet = workbook.getSheetAt(0);
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String projectName = getCellValue(row.getCell(0));
                String taskName = getCellValue(row.getCell(1));
                String responsibleName = getCellValue(row.getCell(2));
                String contact = getCellValue(row.getCell(3));
                String startDate = getFormattedDate(row.getCell(4));
                String durationStr = getCellValue(row.getCell(5));
                String finishedStr = getCellValue(row.getCell(6));

                int duration = Integer.parseInt(durationStr);
                int finished = (finishedStr.trim().equalsIgnoreCase("да") || finishedStr.trim().equals("1")) ? 1 : 0;

                int projectId = getOrInsertProject(conn, projectName);
                int responsibleId = getOrInsertResponsible(conn, responsibleName, contact);
                insertTask(conn, projectId, responsibleId, taskName, startDate, duration, finished);
            }
            System.out.println("Данные успешно импортированы из Excel!");
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    private static String getCellValue(Cell cell) {
        if (cell == null) return "";
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                return String.valueOf((int) cell.getNumericCellValue());
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            default:
                return "";
        }
    }

    private static String getFormattedDate(Cell cell) {
        if (cell == null) return "";
        if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
            Date date = cell.getDateCellValue();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        }
        return cell.toString().trim();
    }

    private static int getOrInsertProject(Connection conn, String projectName) throws SQLException {
        String selectSQL = "SELECT id FROM projects WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, projectName);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        String insertSQL = "INSERT INTO projects (name) VALUES (?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, projectName);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Не удалось вставить проект: " + projectName);
    }

    private static int getOrInsertResponsible(Connection conn, String name, String contact) throws SQLException {
        String selectSQL = "SELECT id FROM responsibles WHERE name = ?";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSQL)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("id");
            }
        }
        String insertSQL = "INSERT INTO responsibles (name, contact) VALUES (?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, name);
            pstmt.setString(2, contact);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Не удалось вставить ответственного: " + name);
    }

    private static void insertTask(Connection conn, int projectId, int responsibleId, String taskName,
                                   String startDate, int duration, int finished) throws SQLException {
        String checkIfExistsSQL = "SELECT COUNT(*) FROM tasks WHERE task_name = ? AND start_date = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkIfExistsSQL)) {
            checkStmt.setString(1, taskName);
            checkStmt.setString(2, startDate);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return;
            }
        }

        String insertSQL = "INSERT INTO tasks (project_id, responsible_id, task_name, start_date, duration, finished) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = conn.prepareStatement(insertSQL)) {
            pstmt.setInt(1, projectId);
            pstmt.setInt(2, responsibleId);
            pstmt.setString(3, taskName);
            pstmt.setString(4, startDate);
            pstmt.setInt(5, duration);
            pstmt.setInt(6, finished);
            pstmt.executeUpdate();
        }
    }
}
