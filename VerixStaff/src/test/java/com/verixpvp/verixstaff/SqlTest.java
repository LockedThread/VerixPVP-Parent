package com.verixpvp.verixstaff;

import com.verixpvp.verixstaff.reports.Report;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

public class SqlTest {

    private static final String HOST = "localhost", DATABASE = "verixstaff", USERNAME = "verixstaff", PASSWORD = "password";
    private HikariDataSource hikariDataSource;
    private Connection connection;
    private PreparedStatement stmtInsertReport;
    private PreparedStatement stmtGetReports;

    public void createReportsTable() {
        try {
            connect();
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS reports (player_uuid varchar(36) NOT NULL, time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, context varchar(256) NOT NULL, reason varchar(1024) NOT NULL)");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void insertReport() {
        try {
            connect();
            stmtInsertReport.setString(1, "b348a07c-8846-42a6-a673-dd3a8925e7f2");
            stmtInsertReport.setString(2, "skyblock");
            stmtInsertReport.setString(3, "Being a retard");
            stmtInsertReport.setString(4, "Some staff member uuid");
            stmtInsertReport.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void getReports() {
        try {
            connect();
            long millis = System.currentTimeMillis();
            stmtGetReports.setString(1, "b348a07c-8846-42a6-a673-dd3a8925e7f2");
            ResultSet resultSet = stmtGetReports.executeQuery();
            while (resultSet.next()) {
                String playerUuid = resultSet.getString("player_uuid");
                Date date = resultSet.getDate("time");
                String context = resultSet.getString("context");
                String reason = resultSet.getString("reason");
                Report report = new Report(UUID.fromString(playerUuid), context, date, reason, UUID.fromString("b348a07c-8846-42a6-a673-dd3a8925e7f2"));
                System.out.println("report.toString() = " + report.toString());
            }
            System.out.println(System.currentTimeMillis() - millis);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void connect() throws SQLException {
        if (connection == null) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl("jdbc:mariadb://" + HOST + ":" + 3306 + "/" + DATABASE);
            config.setUsername(USERNAME);
            config.setPassword(PASSWORD);
            this.hikariDataSource = new HikariDataSource(config);
            this.connection = hikariDataSource.getConnection();

            this.stmtInsertReport = connection.prepareStatement("INSERT INTO reports (player_uuid, context, reason, reporter) VALUES(?,?,?,?)");
            this.stmtGetReports = connection.prepareStatement("SELECT * FROM reports WHERE player_uuid = ?");
        }
    }
}
