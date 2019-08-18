package com.verixpvp.verixstaff.staffmode.menus;

import com.gameservergroup.gsgcore.items.ItemStackBuilder;
import com.gameservergroup.gsgcore.menus.Menu;
import com.gameservergroup.gsgcore.menus.MenuItem;
import com.gameservergroup.gsgcore.utils.Utils;
import com.verixpvp.verixstaff.VerixStaff;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

public class MenuSpeedSelector extends Menu {

    private static MenuSpeedSelector ourInstance;

    private MenuSpeedSelector() {
        super(VerixStaff.getInstance().getConfig().getString("staffmode.speed-selector.menu.name"), VerixStaff.getInstance().getConfig().getInt("staffmode.speed-selector.menu.size"));
        initialize();
    }

    public static MenuSpeedSelector getInstance() {
        return ourInstance == null ? ourInstance = new MenuSpeedSelector() : ourInstance;
    }

    @Override
    public void initialize() {
        ConfigurationSection itemSection = VerixStaff.getInstance().getConfig().getConfigurationSection("staffmode.speed-selector.menu.items");
        for (String key : itemSection.getKeys(false)) {
            if (Utils.isInteger(key)) {
                int slot = Integer.parseInt(key);
                float speed = itemSection.getFloat(key + ".speed") / 10;

                setItem(slot, MenuItem.of(ItemStackBuilder.of(itemSection.getConfigurationSection(key)).build()).setInventoryClickEventConsumer(event -> {
                    ((Player) event.getWhoClicked()).setFlySpeed(speed);
                    ((Player) event.getWhoClicked()).setWalkSpeed(speed);
                    event.setCancelled(true);
                    if (VerixStaff.getInstance().getConfig().getBoolean("staffmode.speed-selector.menu.close-on-select")) {
                        event.getWhoClicked().closeInventory();
                    }
                }));
            }
        }
    }
}
