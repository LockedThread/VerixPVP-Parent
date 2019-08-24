package com.verixpvp.verixstaff.staffmode.units;

import com.gameservergroup.gsgcore.GSGCore;
import com.gameservergroup.gsgcore.commands.post.CommandPost;
import com.gameservergroup.gsgcore.events.EventPost;
import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.gameservergroup.gsgcore.units.Unit;
import com.google.common.reflect.TypeToken;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.staffmode.menus.MenuStaffList;
import com.verixpvp.verixstaff.staffmode.objs.Role;
import com.verixpvp.verixstaff.staffmode.objs.StaffPlayer;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import redis.clients.jedis.Jedis;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class UnitStaffMode extends Unit {

    private static UnitStaffMode ourInstance;

    private Map<UUID, StaffPlayer> staffPlayerMap;
    private Map<String, Role> permissionToRoleMap;

    private MenuItem nextPageItem;
    private MenuItem previousPageItem;

    private UnitStaffMode() {
    }

    public static UnitStaffMode getInstance() {
        return ourInstance == null ? ourInstance = new UnitStaffMode() : ourInstance;
    }

    @Override
    public void setup() {
        try (Jedis jedis = VerixStaff.getInstance().getJedisPool().getResource()) {
            GSG_CORE.getGson().fromJson(jedis.get("online-players"), new TypeToken<HashMap<UUID, StaffPlayer>>() {
            }.getType());
        }
        this.staffPlayerMap = new HashMap<>();

        ConfigurationSection staffRolesSection = VerixStaff.getInstance().getConfig().getConfigurationSection("staff-roles");
        this.permissionToRoleMap = staffRolesSection.getKeys(false)
                .stream()
                .collect(Collectors.toMap(key -> staffRolesSection.getString(key + ".permission"), key -> new Role(staffRolesSection.getConfigurationSection(key)), (a, b) -> b));

        this.nextPageItem = MenuItem.of(ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("staff-list.menu.items.next-page-item")).build())
                .setInventoryClickEventConsumer(event -> {
                    if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof MenuStaffList) {
                        ((MenuStaffList) event.getClickedInventory().getHolder()).nextPage();
                        event.setCancelled(true);
                    }
                });
        this.previousPageItem = MenuItem.of(ItemStackBuilder.of(VerixStaff.getInstance().getConfig().getConfigurationSection("staff-list.menu.items.previous-page-item")).build())
                .setInventoryClickEventConsumer(event -> {
                    if (event.getClickedInventory() != null && event.getClickedInventory().getHolder() instanceof MenuStaffList) {
                        ((MenuStaffList) event.getClickedInventory().getHolder()).previousPage();
                        event.setCancelled(true);
                    }
                });

        CommandPost.create()
                .builder()
                .assertPlayer()
                .assertPermission("verixstaff.staffmode")
                .handler(c -> {

                }).post(VerixStaff.getInstance(), "staffmode");

        CommandPost.create()
                .builder()
                .assertPlayer()
                .handler(c -> {
                    if (MenuStaffList.getInstance().isQueueUpdate()) {
                        MenuStaffList.getInstance().clear();
                        MenuStaffList.getInstance().initialize();
                        MenuStaffList.getInstance().setQueueUpdate(false);
                    }
                    c.getSender().openInventory(MenuStaffList.getInstance().getInventory());
                }).post(VerixStaff.getInstance(), "stafflist");

        EventPost.of(PlayerJoinEvent.class)
                .filter(event -> event.getPlayer().hasPermission("verixstaff.staff"))
                .handle(event -> {
                    staffPlayerMap.put(event.getPlayer().getUniqueId(), new StaffPlayer(event.getPlayer()));
                    MenuStaffList.getInstance().setQueueUpdate(true);
                    Bukkit.getScheduler().runTaskAsynchronously(VerixStaff.getInstance(), () -> {
                        try (Jedis jedis = VerixStaff.getInstance().getJedisPool().getResource()) {
                            jedis.publish("online", VerixStaff.getInstance().getServerContext() + ":" + GSG_CORE.getGson().toJson(staffPlayerMap));
                            jedis.set("online-players", GSGCore.getInstance().getGson().toJson(UnitStaffMode.getInstance().getStaffPlayerMap()));
                        }
                    });
                }).post(VerixStaff.getInstance());

        EventPost.of(PlayerQuitEvent.class)
                .filter(event -> event.getPlayer().hasPermission("verixstaff.staff"))
                .handle(event -> {
                    staffPlayerMap.remove(event.getPlayer().getUniqueId());
                    MenuStaffList.getInstance().setQueueUpdate(true);
                    Bukkit.getScheduler().runTaskAsynchronously(VerixStaff.getInstance(), () -> {
                        try (Jedis jedis = VerixStaff.getInstance().getJedisPool().getResource()) {
                            jedis.publish("online", VerixStaff.getInstance().getServerContext() + ":" + GSG_CORE.getGson().toJson(staffPlayerMap));
                            jedis.set("online-players", GSGCore.getInstance().getGson().toJson(UnitStaffMode.getInstance().getStaffPlayerMap()));
                        }
                    });
                }).post(VerixStaff.getInstance());
    }

    public Map<UUID, StaffPlayer> getStaffPlayerMap() {
        return staffPlayerMap;
    }

    public void setStaffPlayerMap(Map<UUID, StaffPlayer> staffPlayerMap) {
        this.staffPlayerMap = staffPlayerMap;
    }

    public Map<String, Role> getPermissionToRoleMap() {
        return permissionToRoleMap;
    }

    public MenuItem getNextPageItem() {
        return nextPageItem;
    }

    public MenuItem getPreviousPageItem() {
        return previousPageItem;
    }
}
