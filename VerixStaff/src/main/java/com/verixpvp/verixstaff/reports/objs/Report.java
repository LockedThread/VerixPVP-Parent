package com.verixpvp.verixstaff.reports.objs;

import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.reports.units.UnitReport;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Date;
import java.util.UUID;
import java.util.stream.Collectors;

public class Report {

    private final UUID playerUUID, reporterUUID;
    private final String serverContext;
    private final Date time;
    private final String reason;

    public Report(UUID playerUUID, String serverContext, Date time, String reason, UUID reporterUUID) {
        this.playerUUID = playerUUID;
        this.serverContext = serverContext;
        this.time = time;
        this.reason = reason;
        this.reporterUUID = reporterUUID;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getServerContext() {
        return serverContext;
    }

    public Date getTime() {
        return time;
    }

    public String getReason() {
        return reason;
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(playerUUID);
    }

    public UUID getReporterUUID() {
        return reporterUUID;
    }

    public OfflinePlayer getReporter() {
        return Bukkit.getOfflinePlayer(reporterUUID);
    }

    public ItemStack getIncidentItemStack() {
        UnitReport unitReport = VerixStaff.getInstance().getUnitReport();
        ItemStack item = unitReport.getPlainIncidentItemStack().clone();
        ItemMeta itemMeta = item.getItemMeta();
        itemMeta.setLore(itemMeta.getLore().stream().map(s -> s.replace("{time}", unitReport.getDateFormat().format(getTime()))
                .replace("{reporterUUID}", getPlayer().getName())
                .replace("{server-context}", serverContext)
                .replace("{reason}", reason)
                .replace("{reporter}", getReporter().getName()))
                .collect(Collectors.toList()));
        item.setItemMeta(itemMeta);
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Report report = (Report) o;

        return time == report.time && playerUUID.equals(report.playerUUID) && serverContext.equals(report.serverContext) && reason.equals(report.reason) && reporterUUID.equals(report.reporterUUID);
    }

    @Override
    public int hashCode() {
        int result = playerUUID.hashCode();
        result = 31 * result + serverContext.hashCode();
        result = 31 * result + time.hashCode();
        result = 31 * result + reason.hashCode();
        result = 31 * result + reporterUUID.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Report{" +
                "playerUUID=" + playerUUID +
                ", reporterUUID=" + reporterUUID +
                ", serverContext='" + serverContext + '\'' +
                ", time=" + time +
                ", reason='" + reason + '\'' +
                '}';
    }
}
