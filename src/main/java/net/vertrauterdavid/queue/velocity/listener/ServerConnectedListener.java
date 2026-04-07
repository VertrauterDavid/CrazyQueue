package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;

public class ServerConnectedListener {

    @Subscribe
    public void onServerConnect(ServerConnectedEvent event) {
        final Player player = event.getPlayer();
        final RegisteredServer targetServer = event.getServer();

        if (CrazyQueueVelocity.getInstance().getQueueConfig().isQueueDisabled(targetServer.getServerInfo().getName())) {
            CrazyQueueVelocity.getInstance().getOldServers().remove(player);
            return;
        }

        CrazyQueueVelocity.getInstance().getOldServers().put(player, targetServer);
    }

}
