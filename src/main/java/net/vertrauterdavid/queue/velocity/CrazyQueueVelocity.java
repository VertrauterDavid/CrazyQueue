package net.vertrauterdavid.queue.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import net.vertrauterdavid.queue.velocity.command.CrazyQueueCommand;
import net.vertrauterdavid.queue.velocity.command.LeaveQueueCommand;
import net.vertrauterdavid.queue.velocity.command.QueueCommand;
import net.vertrauterdavid.queue.velocity.config.QueueConfig;
import net.vertrauterdavid.queue.velocity.listener.DisconnectListener;
import net.vertrauterdavid.queue.velocity.listener.KickedFromServerListener;
import net.vertrauterdavid.queue.velocity.listener.PluginMessageListener;
import net.vertrauterdavid.queue.velocity.listener.ServerConnectedListener;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;

import java.nio.file.Path;
import java.util.WeakHashMap;

@Plugin(
        id = "crazyqueue",
        name = "CrazyQueue",
        version = "1.0",
        authors = {"VertrauterDavid", "JavaMio"}
)
@Getter
public class CrazyQueueVelocity {

    @Getter
    private static CrazyQueueVelocity instance;
    private final ProxyServer proxyServer;
    private final Path dataDirectory;
    private QueueConfig queueConfig;
    private QueueManager queueManager;

    private final WeakHashMap<Player, RegisteredServer> oldServers = new WeakHashMap<>();

    @Inject
    public CrazyQueueVelocity(ProxyServer server, @DataDirectory Path dataDirectory) {
        instance = this;
        proxyServer = server;
        this.dataDirectory = dataDirectory;
        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("crazyqueue:tobukkit"));
        proxyServer.getChannelRegistrar().register(MinecraftChannelIdentifier.from("crazyqueue:toproxy"));
    }

    @Subscribe
    public void onInitialize(ProxyInitializeEvent event) {
        queueConfig = QueueConfig.load(dataDirectory);
        queueManager = new QueueManager();

        new CrazyQueueCommand("crazyqueue");
        new LeaveQueueCommand("leavequeue");
        new QueueCommand("queue");

        proxyServer.getEventManager().register(this, new DisconnectListener());
        proxyServer.getEventManager().register(this, new KickedFromServerListener());
        proxyServer.getEventManager().register(this, new PluginMessageListener());
        proxyServer.getEventManager().register(this, new ServerConnectedListener());
    }

}
