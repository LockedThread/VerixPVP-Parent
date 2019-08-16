package com.verixpvp.verixstaff.reports;

import com.gameservergroup.gsgcore.commands.post.CommandPost;
import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.gameservergroup.gsgcore.units.Unit;
import com.gameservergroup.gsgcore.utils.CallBack;
import com.google.common.base.Joiner;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.enums.Messages;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class UnitReport extends Unit {

    private SimpleDateFormat dateFormat;

    private ItemStack plainIncidentItemStack;
    private MenuItem nextPageItem;
    private MenuItem previousPageItem;

    @Override
    public void setup() {
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
                            VerixStaff.getInstance().createReport(c.getSender().getUniqueId(), reason, offlinePlayer.getUniqueId(), new CallBack<Boolean>() {
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
                            VerixStaff.getInstance().getReports(offlinePlayer.getUniqueId(), new CallBack<List<Report>>() {
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
