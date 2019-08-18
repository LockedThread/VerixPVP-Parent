package com.verixpvp.verixstaff.staffmode.units;

import com.gameservergroup.gsgcore.commands.post.CommandPost;
import com.gameservergroup.gsgcore.units.Unit;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.enums.Messages;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class UnitRandomTeleport extends Unit {

    @Override
    public void setup() {
        CommandPost.create()
                .builder()
                .assertPermission("verixstaff.randomtp")
                .assertPlayer()
                .handler(c -> {
                    if (VerixStaff.getInstance().getConfig().getBoolean("staffmode.random-teleport.require-staffmode")) {
                        if (!UnitStaffMode.getInstance().getStaffPlayerMap().containsKey(c.getSender().getUniqueId())) {
                            c.reply(Messages.COMMAND_RANDOMTP_MUST_BE_IN_STAFFMODE);
                            return;
                        }
                    }
                    List<Player> players = Bukkit.getOnlinePlayers() instanceof List ? new ArrayList<>(Bukkit.getOnlinePlayers()) : new ArrayList<>();
                    Player target = players.get(ThreadLocalRandom.current().nextInt(0, players.size() - 1));

                    c.getSender().teleport(target, PlayerTeleportEvent.TeleportCause.COMMAND);
                    c.reply(Messages.COMMAND_RANDOMTP_SENT.toString().replace("{player}", target.getName()));
                }).post(VerixStaff.getInstance(), "randomtp", "rtp");
    }
}
