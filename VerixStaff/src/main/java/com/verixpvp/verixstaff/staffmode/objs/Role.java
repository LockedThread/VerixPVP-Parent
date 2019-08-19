package com.verixpvp.verixstaff.staffmode.objs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.Objects;

public class Role {

    private final String formattedName, permission;
    private final Material staffListMaterial;
    private final double priority;
    private final Integer staffListData;
    private final ChatColor chatColor;

    public Role(ConfigurationSection section) {
        this(section.getString("name"), section.getString("permission"), ChatColor.valueOf(section.getString("color").toUpperCase()), Material.matchMaterial(section.getString("staff-list.material")), section.getDouble("priority"), section.getBoolean("staff-list.data.enabled") ? section.getInt("staff-list.data.value") : null);
    }

    private Role(String formattedName, String permission, ChatColor chatColor, Material staffListMaterial, double priority, Integer staffListData) {
        this.formattedName = formattedName;
        this.permission = permission;
        this.chatColor = chatColor;
        this.staffListMaterial = staffListMaterial;
        this.priority = priority;
        this.staffListData = staffListData;
    }

    public String getFormattedName() {
        return formattedName;
    }

    public String getPermission() {
        return permission;
    }

    public Material getStaffListMaterial() {
        return staffListMaterial;
    }

    public Integer getStaffListData() {
        return staffListData;
    }

    public double getPriority() {
        return priority;
    }

    public boolean playerHasPermission(Player player) {
        return player.hasPermission(permission);
    }

    public boolean itemHasData() {
        return staffListData != null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Role role = (Role) o;

        if (!formattedName.equals(role.formattedName)) return false;
        if (!permission.equals(role.permission)) return false;
        if (staffListMaterial != role.staffListMaterial) return false;
        if (chatColor != role.chatColor) return false;
        return Objects.equals(staffListData, role.staffListData);
    }

    @Override
    public int hashCode() {
        int result = formattedName.hashCode();
        result = 31 * result + permission.hashCode();
        result = 31 * result + staffListMaterial.hashCode();
        result = 31 * result + (staffListData != null ? staffListData.hashCode() : 0);
        result = 31 * result + chatColor.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Role{" +
                "formattedName='" + formattedName + '\'' +
                ", permission='" + permission + '\'' +
                ", staffListMaterial=" + staffListMaterial +
                ", chatColor=" + chatColor +
                ", priority=" + priority +
                ", staffListData=" + staffListData +
                '}';
    }

    public ChatColor getChatColor() {
        return chatColor;
    }
}
