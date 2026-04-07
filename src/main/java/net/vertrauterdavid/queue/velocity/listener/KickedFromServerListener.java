package net.vertrauterdavid.queue.velocity.listener;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.KickedFromServerEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;

public class KickedFromServerListener {

    @Subscribe
    public void onKickedFromServer(KickedFromServerEvent event) {
        final Player player = event.getPlayer();
        if (player.getCurrentServer().isEmpty()) return;
        if (!CrazyQueueVelocity.getInstance().getQueueConfig().isAutoRequeueOnKick()) return;

        final RegisteredServer oldServer = CrazyQueueVelocity.getInstance().getOldServers().getOrDefault(player, null);
        final ServerQueue serverQueue = CrazyQueueVelocity.getInstance().getQueueManager().getQueue(oldServer == null ? null : oldServer.getServerInfo().getName());
        if (serverQueue == null) return;

        serverQueue.add(player);
    }

}
