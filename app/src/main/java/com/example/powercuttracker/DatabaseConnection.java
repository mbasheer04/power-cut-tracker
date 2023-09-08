package com.example.powercuttracker;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;

public class DatabaseConnection {

    private Connection c;

    public DatabaseConnection(){
        try {
            c = getConnection();
            System.out.println("Connection Successful.");
        } catch (SQLException e){
            System.out.println(e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        String jdbcUrl = "jdbc:postgresql://localhost:5433/postgres";
        String username = "postgres";
        String password = "12345678";

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (Exception e) {
            System.out.println("1");
            e.printStackTrace();
            throw new SQLException("JDBC Driver not found.");
        }
    }

    public void closeConnection(){
        try {
            c.close();
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }

    public ResultSet selectSingle(String table, String condition, String conditionValue){
        ResultSet rs = null;
        try {
            Statement stmt = c.createStatement();
            rs = stmt.executeQuery("SELECT * FROM " + table + " WHERE " +
                                condition + "='" + conditionValue + "';");
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
        return rs;
    }

    public void insertSingle(String table, String fields, String values){
        try {
            Statement stmt = c.createStatement();
            stmt.executeQuery("INSERT INTO " + table + "(" + fields + ")"
            + "VALUES (" + values + ");");
        } catch (SQLException e) {
            System.out.println(e.getErrorCode());
        }
    }
}
