package com.verixpvp.verixstaff.staffmode.menus;

import com.gameservergroup.gsgcore.menus.Menu;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.staffmode.objs.StaffPlayer;
import com.verixpvp.verixstaff.staffmode.units.UnitStaffMode;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class MenuStaffList extends Menu {

    private static List<Integer> blockedSlots;
    private static MenuStaffList ourInstance;

    private int page = 0;
    private int totalPages;
    private boolean queueUpdate;

    private MenuStaffList() {
        super(VerixStaff.getInstance().getConfig().getString("staff-list.menu.name"), VerixStaff.getInstance().getConfig().getInt("staff-list.menu.size"));
        int mathSlots = getInventory().getSize() - getBlockedSlots().size();
        this.totalPages = UnitStaffMode.getInstance().getStaffPlayerMap().size() / mathSlots;
        this.queueUpdate = true;
    }

    private static List<Integer> getBlockedSlots() {
        return blockedSlots == null ? blockedSlots = VerixStaff.getInstance().getConfig().getIntegerList("staff-list.menu.blocked-slots") : blockedSlots;
    }

    public static MenuStaffList getInstance() {
        return ourInstance == null ? ourInstance = new MenuStaffList() : ourInstance;
    }

    @Override
    public void initialize() {
        Map<UUID, StaffPlayer> staffPlayerMap = UnitStaffMode.getInstance().getStaffPlayerMap();
        List<StaffPlayer> staffPlayers = staffPlayerMap.values().stream().sorted(Comparator.comparingDouble(StaffPlayer::getPriority)).collect(Collectors.toList());

        setItem(VerixStaff.getInstance().getConfig().getInt("staff-list.menu.items.previous-page-item.slot"), UnitStaffMode.getInstance().getPreviousPageItem());
        setItem(VerixStaff.getInstance().getConfig().getInt("staff-list.menu.items.next-page-item.slot"), UnitStaffMode.getInstance().getNextPageItem());

        int slot = 0;
        int startIndex = getStartIndex();
        while (startIndex < getEndIndex()) {
            if (staffPlayers.size() == startIndex) break;
            if (!blockedSlots.contains(slot)) {
                StaffPlayer staffPlayer = staffPlayers.get(startIndex);
                setItem(slot, staffPlayer.getMenuItem());
            }
            slot++;
            startIndex++;
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

        int reportListSize = UnitStaffMode.getInstance().getStaffPlayerMap().size();
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

    public boolean isQueueUpdate() {
        return queueUpdate;
    }

    public void setQueueUpdate(boolean queueUpdate) {
        this.queueUpdate = queueUpdate;
    }
}
