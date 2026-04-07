package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

public class DisconnectListener {

    @Subscribe
    public void onDisconnect(DisconnectEvent event) {
        final Player player = event.getPlayer();

        CrazyQueueVelocity.getInstance().getQueueManager().leaveAllQueues(player);
        CrazyQueueVelocity.getInstance().getOldServers().remove(player);
    }

}
