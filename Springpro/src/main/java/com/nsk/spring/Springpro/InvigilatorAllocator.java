//package com.nsk.spring.Springpro;
//
//import java.io.BufferedReader;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//public class InvigilatorAllocator {
//
//    private static final String DB_URL = "jdbc:mysql://localhost:3306/faculty_db";
//    private static final String USER = "root";
//    private static final String PASSWORD = "root";
//
//    public static void main(String[] args) {
//        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
//            InputStream inputStream = System.in; // For simplicity, replace with actual input file stream
//
//            processCsv(inputStream, connection);
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static void processCsv(InputStream inputStream, Connection connection) {
//        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
//            br.readLine(); // Skip header
//            String line;
//            while ((line = br.readLine()) != null) {
//                String[] values = line.split(",");
//                String date = values[0];
//                String slot = values[1]; // "M" or "A" for Morning or Afternoon
//                int required = Integer.parseInt(values[2]);
//
//                // Get the day of the week from the date (Assuming date format is MM/DD/YYYY or adjust accordingly)
//                String dayOfWeek = getDayOfWeek(date); 
//
//                // Convert slot (M/A) and dayOfWeek into a valid column name
//                String column = mapDaySlotToColumn(dayOfWeek, slot);
//                if (column == null) {
//                    System.out.println("Invalid slot or day: " + dayOfWeek + " " + slot);
//                    continue;
//                }
//
//                // Fetch and assign faculty
//                List<String> assignedFaculty = assignFaculty(connection, column, required);
//
//                // Store the result in the `final_faculty_allocation` table
//                storeAllocation(connection, date, slot, String.join(", ", assignedFaculty));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    // Helper to map day and slot to a valid column in the 'faculty' table
//    private static String mapDaySlotToColumn(String dayOfWeek, String slot) {
//        switch (dayOfWeek.toUpperCase()) {
//            case "MONDAY":
//                return slot.equals("M") ? "MONDAYM" : "MONDAYA";
//            case "TUESDAY":
//                return slot.equals("M") ? "TUESDAYM" : "TUESDAYA";
//            case "WEDNESDAY":
//                return slot.equals("M") ? "WEDNESDAYM" : "WEDNESDAYA";
//            case "THURSDAY":
//                return slot.equals("M") ? "THURSDAYM" : "THURSDAYA";
//            case "FRIDAY":
//                return slot.equals("M") ? "FRIDAYM" : "FRIDAYA";
//            case "SATURDAY":
//                return slot.equals("M") ? "SATURDAYM" : "SATURDAYA";
//            default:
//                return null; // Invalid day
//        }
//    }
//
//    // Helper to assign faculty based on slot availability and required count
//    private static List<String> assignFaculty(Connection connection, String slotColumn, int required) throws SQLException {
//        List<String> assignedFaculty = new ArrayList<>();
//        String query = "SELECT id, name FROM faculty WHERE " + slotColumn + " = ? ORDER BY allocation_count ASC";
//        PreparedStatement stmt = connection.prepareStatement(query);
//        stmt.setString(1, "Yes");
//        ResultSet rs = stmt.executeQuery();
//
//        while (rs.next() && assignedFaculty.size() < required) {
//            String name = rs.getString("name");
//            assignedFaculty.add(name);
//            // Update allocation count (you can implement this part based on how you want to handle faculty allocation counts)
//        }
//        return assignedFaculty;
//    }
//
//    // Helper to store the allocation in `final_faculty_allocation` table
//    private static void storeAllocation(Connection connection, String date, String slot, String facultyNames) throws SQLException {
//        String insertSQL = "INSERT INTO final_faculty_allocation (date, slot, assigned_faculty) VALUES (?, ?, ?)";
//        PreparedStatement stmt = connection.prepareStatement(insertSQL);
//        stmt.setString(1, date);
//        stmt.setString(2, slot);
//        stmt.setString(3, facultyNames);
//        stmt.executeUpdate();
//    }
//
//    // Helper to convert a date to a day of the week (you can adjust the date parsing format)
//    private static String getDayOfWeek(String date) {
//        // Assuming date format is MM/DD/YYYY
//        // Add logic here to parse the date and return the day of the week
//        // For example, you can use `LocalDate` and `DayOfWeek` in Java 8+
//        // This is a placeholder implementation
//        return "MONDAY"; // Replace this with actual date-to-day conversion logic
//    }
//}

package com.nsk.spring.Springpro;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class InvigilatorAllocator {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/faculty_db";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            InputStream inputStream = System.in; // For simplicity, replace with actual input file stream
            processCsv(inputStream, connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void processCsv(InputStream inputStream, Connection connection) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            br.readLine(); // Skip header
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                String date = values[0].trim();
                String slot = values[1].trim(); // "M" for Morning, "A" for Afternoon
                int required = Integer.parseInt(values[2].trim());

                // Get the day of the week from the date (Assuming date format is MM/DD/YYYY)
                String dayOfWeek = getDayOfWeek(date); 

                // Convert slot (M/A) and dayOfWeek into a valid column name
                String column = mapDaySlotToColumn(dayOfWeek, slot);
                if (column == null) {
                    System.out.println("Invalid slot or day: " + dayOfWeek + " " + slot);
                    continue;
                }

                // Fetch and assign faculty
                List<String> assignedFaculty = assignFaculty(connection, column, required);

                // Store the result in the `final_faculty_allocation` table
                storeAllocation(connection, date, slot, String.join(", ", assignedFaculty));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Helper to convert date to day of the week
    private static String getDayOfWeek(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M/d/yyyy");
        LocalDate localDate = LocalDate.parse(date, formatter);
        return localDate.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH).toUpperCase();
    }

    // Helper to map day and slot to a valid column in the 'faculty' table
    private static String mapDaySlotToColumn(String dayOfWeek, String slot) {
        switch (dayOfWeek) {
            case "MONDAY":
                return slot.equals("M") ? "MONDAYM" : "MONDAYA";
            case "TUESDAY":
                return slot.equals("M") ? "TUESDAYM" : "TUESDAYA";
            case "WEDNESDAY":
                return slot.equals("M") ? "WEDNESDAYM" : "WEDNESDAYA";
            case "THURSDAY":
                return slot.equals("M") ? "THURSDAYM" : "THURSDAYA";
            case "FRIDAY":
                return slot.equals("M") ? "FRIDAYM" : "FRIDAYA";
            case "SATURDAY":
                return slot.equals("M") ? "SATURDAYM" : "SATURDAYA";
            default:
                return null; // Invalid day
        }
    }

    // Helper to assign faculty based on slot availability and required count
    private static List<String> assignFaculty(Connection connection, String slotColumn, int required) throws SQLException {
        List<String> assignedFaculty = new ArrayList<>();
        String query = "SELECT id, name, allocation_count FROM faculty WHERE " + slotColumn + " = ? ORDER BY allocation_count ASC";
        PreparedStatement stmt = connection.prepareStatement(query);
        stmt.setString(1, "Yes");
        ResultSet rs = stmt.executeQuery();

        while (rs.next() && assignedFaculty.size() < required) {
            String name = rs.getString("name");
            int facultyId = rs.getInt("id");
            assignedFaculty.add(name);
            
            // Update allocation count for the assigned faculty
            updateAllocationCount(connection, facultyId);
        }
        return assignedFaculty;
    }

    // Helper to update the allocation count of the assigned faculty
    private static void updateAllocationCount(Connection connection, int facultyId) throws SQLException {
        String updateQuery = "UPDATE faculty SET allocation_count = allocation_count + 1 WHERE id = ?";
        PreparedStatement updateStmt = connection.prepareStatement(updateQuery);
        updateStmt.setInt(1, facultyId);
        updateStmt.executeUpdate();
    }

    // Helper to store the allocation in the `final_faculty_allocation` table
    private static void storeAllocation(Connection connection, String date, String slot, String facultyNames) throws SQLException {
        String insertSQL = "INSERT INTO faculty_allocations (date, slot, name) VALUES (?, ?, ?)";
        PreparedStatement stmt = connection.prepareStatement(insertSQL);
        stmt.setString(1, date);
        stmt.setString(2, slot);
        stmt.setString(3, facultyNames); // Using "name" instead of "assigned_faculty"
        stmt.executeUpdate();
    }
}

