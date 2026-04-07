package net.vertrauterdavid.queue.velocity.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;
import net.vertrauterdavid.queue.velocity.util.CommandUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class QueueCommand implements RawCommand {

    private final String name;
    private final QueueManager queueManager;

    public QueueCommand(String name) {
        this.name = name;
        this.queueManager = CrazyQueueVelocity.getInstance().getQueueManager();

        CrazyQueueVelocity.getInstance().getProxyServer().getCommandManager().register(name, this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof Player player)) return;
        String[] args = CommandUtil.getArgs(invocation);

        // remove first if empty
        if (args.length > 0 && args[0].replaceAll(" ", "").equalsIgnoreCase("")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length == 1) {
            final String server = args[0];
            final ServerQueue serverQueue = queueManager.getQueue(server);
            if (serverQueue == null) {
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.server-not-found", Map.of("server", server))));
                return;
            }

            if (serverQueue.getPlayerQueue().contains(player)) {
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.already-in-queue", Map.of("server", server))));
                return;
            }

            if (player.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(server)).orElse(false)) {
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.already-on-server", Map.of("server", server))));
                return;
            }

            queueManager.leaveAllQueues(player);
            serverQueue.add(player);
            return;
        }

        player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-usage", Map.of("command", name))));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        final List<String> list = new ArrayList<>();
        final String[] args = CommandUtil.getArgs(invocation);

        if (args.length == 1) {
            list.addAll(queueManager.getAllServerNames());
        }

        return CommandUtil.finishComplete(list, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.queue");
    }

}
