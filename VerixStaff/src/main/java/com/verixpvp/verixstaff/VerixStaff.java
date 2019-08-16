package com.verixpvp.verixstaff;

import com.gameservergroup.gsgcore.plugin.Module;
import com.gameservergroup.gsgcore.utils.CallBack;
import com.verixpvp.verixstaff.reports.Report;
import com.verixpvp.verixstaff.reports.UnitReport;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class VerixStaff extends Module {

    private static VerixStaff instance;

    private HikariDataSource hikariDataSource;

    private PreparedStatement stmtInsertReport;
    private PreparedStatement stmtGetReports;

    private UnitReport unitReport;

    public static VerixStaff getInstance() {
        return instance;
    }

    @Override
    public void enable() {
        instance = this;
        saveDefaultConfig();

        if (!setupMysql()) {
            getLogger().severe("Unable to start plugin, there was a problem initializing mysql!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerUnits(unitReport = new UnitReport());
    }

    @Override
    public void disable() {
        hikariDataSource.close();
        instance = null;
    }

    private boolean setupMysql() {
        try {
            String host = getConfig().getString("database.mysql.host");
            int portIndex = host.indexOf(":");
            int port = portIndex != -1 ? Integer.parseInt(host.substring(portIndex + 1)) : 3306;

            HikariConfig config = new HikariConfig();
            String jdbcUrl = "jdbc:mariadb://" + (portIndex == -1 ? host : host.substring(0, portIndex - 1)) + ":" + port;
            System.out.println("jdbcUrl = " + jdbcUrl);
            config.setJdbcUrl(jdbcUrl);
            config.setUsername(getConfig().getString("database.mysql.username"));
            config.setPassword(getConfig().getString("database.mysql.password"));
            config.setMaximumPoolSize(10);
            config.setConnectionTimeout(60000);
            config.addDataSourceProperty("databaseName", getConfig().getString("database.mysql.database"));
            Class.forName("org.mariadb.jdbc.MariaDbDataSource");
            config.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
            this.hikariDataSource = new HikariDataSource(config);

            Connection connection = getConnection();

            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS reports (player_uuid varchar(36) NOT NULL, time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, context varchar(256) NOT NULL, reason varchar(1024) NOT NULL, reporter varchar(36) NOT NULL)");
            this.stmtInsertReport = connection.prepareStatement("INSERT INTO reports (player_uuid, context, reason, reporter) VALUES(?,?,?,?)");
            this.stmtGetReports = connection.prepareStatement("SELECT * FROM reports WHERE player_uuid = ?");
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    public void getReports(Player player, CallBack<List<Report>> callBack) {
        getReports(player.getUniqueId(), callBack);
    }

    public void getReports(UUID uuid, CallBack<List<Report>> callBack) {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            try {
                List<Report> reports = new ArrayList<>();
                stmtGetReports.setString(1, uuid.toString());
                ResultSet resultSet = stmtGetReports.executeQuery();
                while (resultSet.next()) {
                    String playerUuid = resultSet.getString("player_uuid");
                    Date date = resultSet.getDate("time");
                    String context = resultSet.getString("context");
                    String reason = resultSet.getString("reason");
                    String reporter = resultSet.getString("reporter");
                    Report report = new Report(UUID.fromString(playerUuid), context, date, reason, UUID.fromString(reporter));
                    reports.add(report);
                }
                callBack.call(reports);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void createReport(Player player, String reason, Player reporter, CallBack<Boolean> responseCallBack) {
        createReport(player.getUniqueId(), reason, reporter.getUniqueId(), responseCallBack);
    }

    public void createReport(UUID uuid, String reason, UUID reporter, CallBack<Boolean> responseCallBack) {
        getServer().getScheduler().runTaskAsynchronously(this, () -> {
            boolean response = true;
            try {
                stmtInsertReport.setString(1, uuid.toString());
                stmtInsertReport.setString(2, getServerContext());
                stmtInsertReport.setString(3, reason);
                stmtInsertReport.setString(4, reporter.toString());
                stmtInsertReport.execute();
            } catch (SQLException e) {
                e.printStackTrace();
                response = false;
            }
            responseCallBack.call(response);
        });
    }

    public HikariDataSource getHikariDataSource() {
        return hikariDataSource;
    }

    public Connection getConnection() throws SQLException {
        return hikariDataSource.getConnection();
    }

    public String getServerContext() {
        return getConfig().getString("server-context");
    }

    public UnitReport getUnitReport() {
        return unitReport;
    }
}
