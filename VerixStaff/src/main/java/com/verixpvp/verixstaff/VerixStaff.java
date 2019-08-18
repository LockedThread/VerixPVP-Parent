package com.verixpvp.verixstaff;

import com.gameservergroup.gsgcore.maven.MavenLibraries;
import com.gameservergroup.gsgcore.maven.MavenLibrary;
import com.gameservergroup.gsgcore.plugin.Module;
import com.verixpvp.verixstaff.redis.pubsub.StaffPubSub;
import com.verixpvp.verixstaff.redis.threads.ThreadSubscriber;
import com.verixpvp.verixstaff.reports.units.UnitReport;
import com.verixpvp.verixstaff.staffmode.units.UnitRandomTeleport;
import com.verixpvp.verixstaff.staffmode.units.UnitStaffMode;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.Protocol;

import java.sql.Connection;
import java.sql.SQLException;

@MavenLibraries(value = {
        @MavenLibrary(groupId = "org.mariadb.jdbc", artifactId = "mariadb-java-client", version = "2.4.3"),
        @MavenLibrary(groupId = "com.zaxxer", artifactId = "HikariCP", version = "3.3.1"),
})
public class VerixStaff extends Module {

    private static VerixStaff instance;

    private HikariDataSource hikariDataSource;
    private JedisPool jedisPool;
    private ThreadSubscriber threadSubscriber;

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

        if (!setupRedis()) {
            getLogger().severe("Unable to start plugin, there was a problem initializing redis!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        registerUnits(UnitReport.getInstance(), UnitStaffMode.getInstance(), new UnitRandomTeleport());
    }

    @Override
    public void disable() {
        hikariDataSource.close();
        threadSubscriber.getStaffPubSub().unsubscribe();
        jedisPool.destroy();
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
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }

    private boolean setupRedis() {
        try {

            String host = getConfig().getString("database.redis.host");
            int port = getConfig().getInt("database.redis.port");

            ClassLoader previous = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(VerixStaff.class.getClassLoader());

            this.jedisPool = getConfig().getBoolean("database.redis.authentication.enabled") ? new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT, getConfig().getString("database.redis.authentication.password")) : new JedisPool(new JedisPoolConfig(), host, port, Protocol.DEFAULT_TIMEOUT);
            Thread.currentThread().setContextClassLoader(previous);

            threadSubscriber = new ThreadSubscriber(new StaffPubSub());
            threadSubscriber.start();
        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
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

    public JedisPool getJedisPool() {
        return jedisPool;
    }
}
