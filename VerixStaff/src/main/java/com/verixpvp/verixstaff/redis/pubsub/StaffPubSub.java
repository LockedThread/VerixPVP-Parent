package com.verixpvp.verixstaff.redis.pubsub;

import com.gameservergroup.gsgcore.GSGCore;
import com.google.common.reflect.TypeToken;
import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.staffmode.objs.StaffPlayer;
import com.verixpvp.verixstaff.staffmode.units.UnitStaffMode;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;

import java.util.HashMap;
import java.util.UUID;

public class StaffPubSub extends JedisPubSub {

    @Override
    public void onMessage(String channel, String message) {
        System.out.println("channel = [" + channel + "], message = [" + message + "]");
        if (message.split(":")[0].equals(VerixStaff.getInstance().getServerContext())) {
            return;
        }
        HashMap<UUID, StaffPlayer> map = GSGCore.getInstance().getGson().fromJson(message, new TypeToken<HashMap<UUID, StaffPlayer>>() {
        }.getType());
        System.out.println("map = " + map);
        UnitStaffMode.getInstance().setStaffPlayerMap(map);
        try (Jedis jedis = VerixStaff.getInstance().getJedisPool().getResource()) {
            jedis.set("online-players", GSGCore.getInstance().getGson().toJson(UnitStaffMode.getInstance().getStaffPlayerMap()));
        }
    }

    @Override
    public void onPMessage(String pattern, String channel, String message) {
        super.onPMessage(pattern, channel, message);
    }
}
