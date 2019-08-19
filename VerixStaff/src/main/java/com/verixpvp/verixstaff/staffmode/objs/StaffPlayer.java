package com.verixpvp.verixstaff.staffmode.objs;

import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.staffmode.units.UnitStaffMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class StaffPlayer {

    private final OfflinePlayer offlinePlayer;
    private final String serverContext;
    private boolean staffMode, staffChat;
    private Role role;

    public StaffPlayer(Player player) {
        this.offlinePlayer = player;
        this.role = UnitStaffMode.getInstance().getPermissionToRoleMap()
                .entrySet()
                .stream()
                .filter(entry -> player.hasPermission(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
        this.serverContext = VerixStaff.getInstance().getServerContext();
    }

    public MenuItem getMenuItem() {
        Material material = role.getStaffListMaterial();
        if (material == null) {
            material = Material.matchMaterial(VerixStaff.getInstance().getConfig().getString("staff-list.menu.items.staff-player-item.default-material"));
        }
        ItemStack itemStack = ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("staff-list.menu.items.staff-player-item"))
                .consumeItemMeta(itemMeta -> {
                    String color = "&" + role.getChatColor().getChar();
                    System.out.println("color = " + color);
                    itemMeta.setDisplayName(itemMeta.getDisplayName().replace("{color}", color)
                            .replace("{player}", offlinePlayer.getName())
                            .replace("{server}", serverContext)
                            .replace("{role}", role.getFormattedName()));
                    itemMeta.setLore(itemMeta.getLore()
                            .stream()
                            .map(s -> s.replace("{color}", color)
                                    .replace("{player}", offlinePlayer.getName())
                                    .replace("{server}", serverContext)
                                    .replace("{role}", role.getFormattedName()))
                            .collect(Collectors.toList()));
                }).setMaterial(material)
                .build();

        return MenuItem.of(itemStack);
    }

    public OfflinePlayer getOfflinePlayer() {
        return offlinePlayer;
    }

    public boolean isStaffMode() {
        return staffMode;
    }

    public void setStaffMode(boolean staffMode) {
        this.staffMode = staffMode;
    }

    public boolean isStaffChat() {
        return staffChat;
    }

    public void setStaffChat(boolean staffChat) {
        this.staffChat = staffChat;
    }

    public String getServerContext() {
        return serverContext;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaffPlayer that = (StaffPlayer) o;

        if (staffMode != that.staffMode) return false;
        if (staffChat != that.staffChat) return false;
        if (!offlinePlayer.equals(that.offlinePlayer)) return false;
        if (!serverContext.equals(that.serverContext)) return false;
        return Objects.equals(role, that.role);
    }

    @Override
    public int hashCode() {
        int result = offlinePlayer.hashCode();
        result = 31 * result + (staffMode ? 1 : 0);
        result = 31 * result + (staffChat ? 1 : 0);
        result = 31 * result + serverContext.hashCode();
        result = 31 * result + (role != null ? role.hashCode() : 0);
        return result;
    }
}
