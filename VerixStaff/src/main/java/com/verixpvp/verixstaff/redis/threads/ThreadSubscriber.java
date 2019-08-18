package com.verixpvp.verixstaff.redis.threads;

import com.verixpvp.verixstaff.VerixStaff;
import com.verixpvp.verixstaff.redis.pubsub.StaffPubSub;
import redis.clients.jedis.Jedis;

public class ThreadSubscriber extends Thread {

    private StaffPubSub staffPubSub;

    public ThreadSubscriber(StaffPubSub staffPubSub) {
        super("jedis-online-staff-subscriber");
        this.staffPubSub = staffPubSub;
    }

    @Override
    public void run() {
        try (Jedis jedis = VerixStaff.getInstance().getJedisPool().getResource()) {
            jedis.subscribe(staffPubSub, "online");
        }
    }

    public StaffPubSub getStaffPubSub() {
        return staffPubSub;
    }
}
