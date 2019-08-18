package com.verixpvp.verixstaff.reports.units;

import com.gameservergroup.gsgcore.commands.post.CommandPost;
import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.gameservergroup.gsgcore.units.Unit;
import com.gameservergroup.gsgcore.utils.CallBack;
import com.google.common.base.Joiner;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.enums.Messages;
import com.verixpvp.verixstaff.reports.menus.MenuReport;
import com.verixpvp.verixstaff.reports.objs.Report;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

public class UnitReport extends Unit {

    private static UnitReport ourInstance;

    private SimpleDateFormat dateFormat;

    private ItemStack plainIncidentItemStack;
    private MenuItem nextPageItem;
    private MenuItem previousPageItem;

    private PreparedStatement stmtInsertReport;
    private PreparedStatement stmtGetReports;

    private UnitReport() {
    }

    public static UnitReport getInstance() {
        return ourInstance == null ? ourInstance = new UnitReport() : ourInstance;
    }

    @Override
    public void setup() {
        try (Connection connection = VerixStaff.getInstance().getConnection()) {
            connection.createStatement().execute("CREATE TABLE IF NOT EXISTS reports (player_uuid varchar(36) NOT NULL, time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, context varchar(256) NOT NULL, reason varchar(1024) NOT NULL, reporter varchar(36) NOT NULL)");
            this.stmtInsertReport = connection.prepareStatement("INSERT INTO reports (player_uuid, context, reason, reporter) VALUES(?,?,?,?)");
            this.stmtGetReports = connection.prepareStatement("SELECT * FROM reports WHERE player_uuid = ?");
        } catch (SQLException ex) {
            ex.printStackTrace();
            VerixStaff.getInstance().getServer().getPluginManager().disablePlugin(VerixStaff.getInstance());
            return;
        }

        this.dateFormat = new SimpleDateFormat(VerixStaff.getInstance().getConfig().getString("reports.date-format"));
        this.plainIncidentItemStack = ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("reports.menu.incident-item")).build();
        this.nextPageItem = MenuItem.of(ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("reports.menu.next-page-item")).build())
                .setInventoryClickEventConsumer(event -> {
                    if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof MenuReport) {
                        ((MenuReport) event.getClickedInventory().getHolder()).nextPage();
                        event.setCancelled(true);
                    }
                });
        this.previousPageItem = MenuItem.of(ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("reports.menu.previous-page-item")).build())
                .setInventoryClickEventConsumer(event -> {
                    if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof MenuReport) {
                        ((MenuReport) event.getClickedInventory().getHolder()).previousPage();
                        event.setCancelled(true);
                    }
                });
        CommandPost.create()
                .builder()
                .assertPermission("verixstaff.report")
                .assertPlayer()
                .handler(c -> {
                    if (c.getRawArgs().length <= 1) {
                        c.reply("&c&lREPORTS &8» &f/report [player] [reason]");
                    } else {
                        Optional<OfflinePlayer> offlinePlayerOptional = c.getArg(0).parse(OfflinePlayer.class);
                        if (offlinePlayerOptional.isPresent()) {
                            OfflinePlayer offlinePlayer = offlinePlayerOptional.get();
                            if (c.isPlayer() && !VerixStaff.getInstance().getConfig().getBoolean("reports.can-report-yourself")) {
                                if (offlinePlayer.getName().equalsIgnoreCase(c.getSender().getName())) {
                                    c.reply(Messages.COMMAND_REPORT_CANT_REPORT_YOURSELF);
                                    return;
                                }
                            }
                            String reason = Joiner.on(" ").skipNulls().join(Arrays.copyOfRange(c.getRawArgs(), 1, c.getRawArgs().length));

                            if (VerixStaff.getInstance().getConfig().getBoolean("reports.minimum-character-reason.enabled")) {
                                if (reason.length() < VerixStaff.getInstance().getConfig().getInt("reports.minimum-character-reason.length")) {
                                    c.reply(Messages.COMMAND_REPORT_REASON_MUST_BE_LONGER);
                                    return;
                                }
                            }
                            createReport(c.getSender().getUniqueId(), reason, offlinePlayer.getUniqueId(), new CallBack<Boolean>() {
                                @Override
                                public void call(Boolean aBoolean) {
                                    if (aBoolean) {
                                        c.reply(Messages.COMMAND_REPORT_SUCCESS.getMessage().replace("{player}", offlinePlayer.getName()).replace("{reason}", reason));
                                    } else {
                                        c.reply(Messages.SQL_ERROR);
                                    }
                                }
                            });
                        } else {
                            c.reply(Messages.PLAYER_OFFLINE.getRawMessage().replace("{player}", c.getRawArg(0)));
                        }
                    }
                }).post(VerixStaff.getInstance(), "report", "createreport");

        CommandPost.create()
                .builder()
                .assertPermission("verixstaff.viewreports")
                .handler(c -> {
                    if (c.getRawArgs().length == 1) {
                        Optional<OfflinePlayer> offlinePlayerOptional = c.getArg(0).parse(OfflinePlayer.class);
                        if (offlinePlayerOptional.isPresent()) {
                            OfflinePlayer offlinePlayer = offlinePlayerOptional.get();
                            c.reply("&ePlease wait a moment whilst we collect reports for " + offlinePlayer.getName() + "...");
                            getReports(offlinePlayer.getUniqueId(), new CallBack<List<Report>>() {
                                @Override
                                public void call(List<Report> reports) {
                                    if (c.isPlayer()) {
                                        ((Player) c.getSender()).openInventory(new MenuReport(offlinePlayer, reports).getInventory());
                                    } else {
                                        c.reply("Reports for " + offlinePlayer.getName());
                                        for (Report report : reports) {
                                            c.reply(dateFormat.format(report.getTime()) + " - " + report.getServerContext() + " - " + report.getReporter() + " - " + report.getReason());
                                        }
                                    }
                                }
                            });
                        } else {
                            c.reply(Messages.PLAYER_OFFLINE.toString().replace("{player}", c.getRawArg(0)));
                        }
                    } else {
                        c.reply("&c&lREPORTS &8» &f/reports [player]");
                    }
                }).post(VerixStaff.getInstance(), "reports", "viewreports", "getreports");
    }

    public void getReports(UUID uuid, CallBack<List<Report>> callBack) {
        Bukkit.getScheduler().runTaskAsynchronously(VerixStaff.getInstance(), () -> {
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

    public void createReport(UUID uuid, String reason, UUID reporter, CallBack<Boolean> responseCallBack) {
        Bukkit.getScheduler().runTaskAsynchronously(VerixStaff.getInstance(), () -> {
            boolean response = true;
            try {
                stmtInsertReport.setString(1, uuid.toString());
                stmtInsertReport.setString(2, VerixStaff.getInstance().getServerContext());
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

    public ItemStack getPlainIncidentItemStack() {
        return plainIncidentItemStack;
    }

    public SimpleDateFormat getDateFormat() {
        return dateFormat;
    }

    public MenuItem getNextPageItem() {
        return nextPageItem;
    }

    public MenuItem getPreviousPageItem() {
        return previousPageItem;
    }
}
