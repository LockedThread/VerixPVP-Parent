package com.verixpvp.verixstaff.staffmode.objs;

import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.gameservergroup.gsgcore.utils.Text;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.staffmode.units.UnitStaffMode;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class StaffPlayer {

    private final UUID uuid;
    private transient OfflinePlayer offlinePlayer;
    private final String serverContext;
    private boolean staffMode, staffChat;
    private Role role;

    public StaffPlayer(Player player) {
        this.offlinePlayer = player;
        this.uuid = player.getUniqueId();
        Role role = null;
        for (Map.Entry<String, Role> entry : UnitStaffMode.getInstance().getPermissionToRoleMap().entrySet()) {
            if (player.isPermissionSet(entry.getKey())) {
                role = entry.getValue();
                break;
            }
        }
        this.role = role;
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
                    itemMeta.setDisplayName(Text.toColor(itemMeta.getDisplayName().replace("{color}", color)
                            .replace("{player}", getOfflinePlayer().getName())
                            .replace("{server}", serverContext)
                            .replace("{role}", role.getFormattedName())));
                    itemMeta.setLore(itemMeta.getLore()
                            .stream()
                            .map(s -> Text.toColor(s.replace("{color}", color)
                                    .replace("{player}", getOfflinePlayer().getName())
                                    .replace("{server}", serverContext)
                                    .replace("{role}", role.getFormattedName())))
                            .collect(Collectors.toList()));
                }).setMaterial(material)
                .build();

        return MenuItem.of(itemStack);
    }

    public OfflinePlayer getOfflinePlayer() {
        if (offlinePlayer == null) offlinePlayer = Bukkit.getOfflinePlayer(uuid);
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

    public UUID getUuid() {
        return uuid;
    }

    public double getPriority() {
        return role.getPriority();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        StaffPlayer that = (StaffPlayer) o;

        if (staffMode != that.staffMode) return false;
        if (staffChat != that.staffChat) return false;
        if (!uuid.equals(that.uuid)) return false;
        if (!serverContext.equals(that.serverContext)) return false;
        return role.equals(that.role);
    }

    @Override
    public int hashCode() {
        int result = uuid.hashCode();
        result = 31 * result + serverContext.hashCode();
        result = 31 * result + (staffMode ? 1 : 0);
        result = 31 * result + (staffChat ? 1 : 0);
        result = 31 * result + role.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "StaffPlayer{" +
                "offlinePlayer=" + offlinePlayer +
                ", uuid=" + uuid +
                ", serverContext='" + serverContext + '\'' +
                ", staffMode=" + staffMode +
                ", staffChat=" + staffChat +
                ", role=" + role +
                '}';
    }
}
