package com.verixpvp.verixstaff.reports;

import com.gameservergroup.gsgcore.menus.Menu;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.verixpvp.verixstaff.VerixStaff;
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

        System.out.println("reportList = " + reportList);
        System.out.println("mathSlots = " + mathSlots);
        System.out.println("getBlockedSlots() = " + getBlockedSlots());
        System.out.println("totalPages = " + totalPages);
        initialize();
    }

    private static List<Integer> getBlockedSlots() {
        return blockedSlots == null ? blockedSlots = VerixStaff.getInstance().getConfig().getIntegerList("reports.menu.blocked-slots") : blockedSlots;
    }

    @Override
    public void initialize() {
        MenuItem previous = VerixStaff.getInstance().getUnitReport().getPreviousPageItem();
        System.out.println("previous = " + previous.getItemStack());
        int previousSlot = VerixStaff.getInstance().getConfig().getInt("reports.menu.previous-page-item.slot");
        System.out.println("previousSlot = " + previousSlot);
        setItem(previousSlot, previous);
        MenuItem nextPageItem = VerixStaff.getInstance().getUnitReport().getNextPageItem();
        System.out.println("nextPageItem = " + nextPageItem.getItemStack());
        setItem(VerixStaff.getInstance().getConfig().getInt("reports.menu.next-page-item.slot"), nextPageItem);

        System.out.println("getInventory().getSize() - getBlockedSlots().size() = " + (getInventory().getSize() - getBlockedSlots().size()));
        int startIndex = getStartIndex();

        System.out.println("startIndex = " + startIndex);
        int endIndex = getEndIndex();
        System.out.println("endIndex = " + endIndex);
        int slot = 0;
        for (int i = startIndex; i < endIndex; i++) {
            System.out.println("i = " + i);
            System.out.println("slot = " + slot);
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
        System.out.println("reportListSize = " + reportListSize);
        int size = (page + 1) * (getInventory().getSize() - getBlockedSlots().size());
        System.out.println("size = " + size);
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
