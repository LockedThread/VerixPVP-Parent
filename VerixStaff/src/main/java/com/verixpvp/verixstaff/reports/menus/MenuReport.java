package com.verixpvp.verixstaff.reports.menus;

import com.gameservergroup.gsgcore.menus.Menu;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.reports.objs.Report;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class MenuReport extends Menu {

    private static List<Integer> blockedSlots;

    private List<Report> reportList;
    private int page = 0;
    private int totalPages;

    public MenuReport(OfflinePlayer player, List<Report> reports) {
        super(VerixStaff.getInstance().getConfig().getString("reports.menu.name").replace("{player}", player.getName()), VerixStaff.getInstance().getConfig().getInt("reports.menu.size"));

        this.reportList = reports;
        int mathSlots = getInventory().getSize() - getBlockedSlots().size();
        this.totalPages = reportList.size() / mathSlots;
        initialize();
    }

    private static List<Integer> getBlockedSlots() {
        return blockedSlots == null ? blockedSlots = VerixStaff.getInstance().getConfig().getIntegerList("reports.menu.blocked-slots") : blockedSlots;
    }

    @Override
    public void initialize() {
        MenuItem previous = VerixStaff.getInstance().getUnitReport().getPreviousPageItem();
        int previousSlot = VerixStaff.getInstance().getConfig().getInt("reports.menu.previous-page-item.slot");
        setItem(previousSlot, previous);
        MenuItem nextPageItem = VerixStaff.getInstance().getUnitReport().getNextPageItem();
        setItem(VerixStaff.getInstance().getConfig().getInt("reports.menu.next-page-item.slot"), nextPageItem);

        int startIndex = getStartIndex();

        int endIndex = getEndIndex();
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            if (!blockedSlots.contains(slot)) {
                setItem(slot, reportList.get(i).getIncidentItemStack());
            }
            slot++;
        }
    }

    private int getStartIndex() {
        if (page == 0) {
            return 0;
        }
        return 54 * page;
    }

    private int getEndIndex() {
        if (page == 0) {
            return getInventory().getSize() - getBlockedSlots().size();
        }

        int reportListSize = reportList.size();
        int size = (page + 1) * (getInventory().getSize() - getBlockedSlots().size());
        if (reportListSize > size) {
            return size;
        }
        return reportListSize;
    }

    public void nextPage() {
        if (page == totalPages) {
            return;
        }
        page++;
        update();
    }

    public void previousPage() {
        if (page == 0) {
            return;
        }
        page--;
        update();
    }

    private void update() {
        clear();
        initialize();
    }
}
