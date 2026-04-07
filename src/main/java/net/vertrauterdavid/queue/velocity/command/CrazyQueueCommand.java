package net.vertrauterdavid.queue.velocity.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.queue.QueueManager;
import net.vertrauterdavid.queue.velocity.queue.ServerQueue;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;
import net.vertrauterdavid.queue.velocity.util.CommandUtil;

import java.util.*;

public class CrazyQueueCommand implements RawCommand {

    private final String name;
    private final ProxyServer proxyServer;
    private final QueueManager queueManager;

    public CrazyQueueCommand(String name) {
        this.name = name;
        this.proxyServer = CrazyQueueVelocity.getInstance().getProxyServer();
        this.queueManager = CrazyQueueVelocity.getInstance().getQueueManager();

        proxyServer.getCommandManager().register(name, this);
    }

    @Override
    public void execute(Invocation invocation) {
        if (!(invocation.source() instanceof final Player player)) return;
        String[] args = CommandUtil.getArgs(invocation);

        // remove first if empty
        if (args.length > 0 && args[0].replaceAll(" ", "").equalsIgnoreCase("")) {
            args = Arrays.copyOfRange(args, 1, args.length);
        }

        if (args.length == 1 || args.length == 2) {
            if (args[0].equalsIgnoreCase("info")) {
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                if (args.length == 2 && args[1].equalsIgnoreCase("--players")) {
                    queueManager.getServerQueues().forEach((server, serverQueue) -> {
                        final Queue<Player> players = serverQueue.getPlayerQueue();
                        player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-entry-players", Map.of(
                                "server", server,
                                "amount", String.valueOf(players.size()),
                                "players", players.stream().map(Player::getUsername).reduce((a, b) -> a + ", " + b).orElse("")
                        ))));
                    });
                } else {
                    queueManager.getServerQueues().forEach((server, serverQueue) -> {
                        final Queue<Player> players = serverQueue.getPlayerQueue();
                        player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-entry", Map.of("server", server, "amount", String.valueOf(players.size())))));
                    });
                }
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-time", Map.of("time", String.valueOf(CrazyQueueVelocity.getInstance().getQueueConfig().getProcessIntervalMillis())))));
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-info-header", Map.of())));
                return;
            }
        }

        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("clear")) {
                final String server = args[1];
                final ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.server-not-found", Map.of("server", server))));
                    return;
                }
                serverQueue.clear();
                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.queue-cleared", Map.of("server", server))));
                return;
            }
            if (args[0].equalsIgnoreCase("queue")) {
                proxyServer.getCommandManager().executeAsync(player, "queue " + args[1]);
                return;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("connect")) {
                final String server = args[1];
                final ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.server-not-found", Map.of("server", server))));
                    return;
                }
                try {
                    final int amount = Math.max(0, Math.min(Integer.parseInt(args[2]), serverQueue.getPlayerQueue().size()));
                    serverQueue.connect(amount);
                    player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.connecting-players", Map.of(
                            "amount", String.valueOf(amount),
                            "plural", amount != 1 ? "s" : ""
                    ))));
                    return;
                } catch (NumberFormatException ignored) { }
            }
            if (args[0].equalsIgnoreCase("send")) {
                String targetS = args[1];

                String server = args[2];
                final ServerQueue serverQueue = queueManager.getQueue(server);
                if (serverQueue == null) {
                    player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.server-not-found", Map.of(
                            "server", server
                    ))));
                    return;
                }

                List<Player> targets = new ArrayList<>();

                switch (targetS) {
                    case "all" -> {
                        if (!player.hasPermission("crazyqueue.command.sendall")) {
                            player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.no-sendall-permission", Map.of())));
                            return;
                        }
                        targets.addAll(proxyServer.getAllPlayers().stream().filter(target -> !target.getUsername().equalsIgnoreCase(player.getUsername())).toList());
                    }
                    case "current" -> player.getCurrentServer().ifPresent(serverConnection -> targets.addAll(serverConnection.getServer().getPlayersConnected().stream().filter(target -> !target.getUsername().equalsIgnoreCase(player.getUsername())).toList()));
                    case "hub" -> targets.addAll(proxyServer.getAllPlayers().stream().filter(target -> target.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().toLowerCase().contains("hub")).orElse(false)).toList());
                    default -> {
                        final Player target = proxyServer.getPlayer(targetS).orElse(null);
                        if (target == null) {
                            player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.player-not-online", Map.of("player", targetS))));
                            return;
                        }
                        targets.add(target);
                    }
                }

                targets.removeIf(target -> target.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(server)).orElse(false));
                targets.forEach(serverQueue::add);

                player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.players-added-to-queue", Map.of(
                        "amount", String.valueOf(targets.size()),
                        "plural", targets.size() != 1 ? "s" : "",
                        "server", server))));
                return;
            }
        }

        CrazyQueueVelocity.getInstance().getQueueConfig().getStringList("messages.crazyqueue-usages").forEach(usage -> player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().formatRaw(usage, Map.of("command", name)))));
    }

    @Override
    public List<String> suggest(Invocation invocation) {
        final List<String> list = new ArrayList<>();
        final String[] args = CommandUtil.getArgs(invocation);

        if ((args.length == 2 && (args[0].equalsIgnoreCase("clear") || args[0].equalsIgnoreCase("queue") || args[0].equalsIgnoreCase("connect"))) || (args.length == 3 && (args[0].equalsIgnoreCase("send")))) {
            list.addAll(queueManager.getAllServerNames());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("send")) {
            if (invocation.source().hasPermission("crazyqueue.command.sendall")) {
                list.add("all");
            }
            list.add("current");
            list.add("hub");
            proxyServer.getAllPlayers().forEach(player -> list.add(player.getUsername()));
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("info")) {
            list.add("--players");
        }

        if (args.length == 1) {
            list.addAll(Arrays.asList("info", "clear", "queue", "connect", "send"));
        }

        return CommandUtil.finishComplete(list, args);
    }

    @Override
    public boolean hasPermission(Invocation invocation) {
        return invocation.source().hasPermission("crazyqueue.command");
    }

}
