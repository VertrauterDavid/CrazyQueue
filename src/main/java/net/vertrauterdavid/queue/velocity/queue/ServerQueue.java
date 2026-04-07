package net.vertrauterdavid.queue.velocity.queue;

import com.velocitypowered.api.proxy.ConnectionRequestBuilder;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.vertrauterdavid.queue.velocity.CrazyQueueVelocity;
import net.vertrauterdavid.queue.velocity.util.ColorUtil;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

@RequiredArgsConstructor
@Getter
public class ServerQueue {

    private final RegisteredServer registeredServer;
    private final Queue<Player> playerQueue = new ConcurrentLinkedQueue<>();
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    @Setter
    private boolean processing = false;

    public void startScheduler() {
        CrazyQueueVelocity.getInstance().getProxyServer().getScheduler().buildTask(CrazyQueueVelocity.getInstance(), this::process).repeat((long) CrazyQueueVelocity.getInstance().getQueueConfig().getProcessIntervalMillis(), TimeUnit.MILLISECONDS).schedule();
    }

    private void process() {
        synchronized (playerQueue) {
            if (playerQueue.isEmpty()) return;
            playerQueue.removeIf(player -> player.getCurrentServer().map(serverConnection -> serverConnection.getServer().getServerInfo().getName().equalsIgnoreCase(registeredServer.getServerInfo().getName())).orElse(false));
            playerQueue.removeIf(player -> !player.isActive());

            sendActionbar();

            if (playerQueue.isEmpty()) return;
            Player player = playerQueue.peek();

            connect(player).thenAccept(remove -> {
                if (remove) {
                    playerQueue.poll();
                }
            });
        }
    }

    private void sendActionbar() {
        int position = 1;
        for (Player player : playerQueue) {
            player.sendActionBar(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("actionbar.queue-position", Map.of(
                    "position", String.valueOf(position),
                    "server", registeredServer.getServerInfo().getName(),
                    "waiting", String.valueOf(playerQueue.size())
            ))));
            position++;
        }
    }

    public void add(Player player) {
        synchronized (playerQueue) {
            if (playerQueue.contains(player)) return;

            if (player.hasPermission("crazyqueue.queue.priority")) {
                Queue<Player> tempQueue = new ConcurrentLinkedQueue<>();

                boolean added = false;
                for (Player queuedPlayer : playerQueue) {
                    if (!added && !queuedPlayer.hasPermission("crazyqueue.queue.priority")) {
                        tempQueue.add(player);
                        added = true;
                    }
                    tempQueue.add(queuedPlayer);
                }

                if (!added) {
                    tempQueue.add(player);
                }

                playerQueue.clear();
                playerQueue.addAll(tempQueue);
            } else {
                playerQueue.add(player);
            }

            player.sendMessage(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.added-to-queue", Map.of("server", registeredServer.getServerInfo().getName()))));
        }
    }

    public void remove(Player player) {
        synchronized (playerQueue) {
            if (!playerQueue.contains(player)) return;
            playerQueue.remove(player);
            player.sendActionBar(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("actionbar.queue-clear", Map.of())));
        }
    }

    public void clear() {
        synchronized (playerQueue) {
            while (!playerQueue.isEmpty()) {
                playerQueue.poll().sendActionBar(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("actionbar.queue-clear", Map.of())));
            }
        }
    }

    public void connect(int amount) {
        executorService.execute(() -> {
            for (int i = 0; i < amount; i++) {
                Player player;
                synchronized (playerQueue) {
                    if (playerQueue.isEmpty()) return;
                    player = playerQueue.peek();
                }

                if (player != null && player.isActive()) {
                    boolean remove = connect(player).join();
                    if (remove) {
                        synchronized (playerQueue) {
                            playerQueue.poll();
                        }
                    }
                }
            }
        });
    }

    // returns true if the player should be removed from the queue
    private CompletableFuture<Boolean> connect(Player player) {
        final CompletableFuture<Boolean> future = new CompletableFuture<>();

        player.createConnectionRequest(registeredServer).connect().thenAccept(result -> {
            ConnectionRequestBuilder.Status status = result.getStatus();

            if (status == ConnectionRequestBuilder.Status.SUCCESS) {
                player.sendActionBar(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("actionbar.connected", Map.of("server", registeredServer.getServerInfo().getName()))));
                future.complete(true);
                return;
            }

            // server disconnected the player
            if (status == ConnectionRequestBuilder.Status.SERVER_DISCONNECTED) {
                final Component reasonComponent = result.getReasonComponent().orElseGet(() -> ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("messages.backend-no-reason", Map.of())));
                final String reason = PlainTextComponentSerializer.plainText().serialize(reasonComponent);

                // removes the player from the queue if the player is banned on the server
                // why not disconnect the player from the server for any reason? example: the server could be whitelisted for a few seconds just to fix some bugs, we don't want to remove the player from the queue in this case
                if (CrazyQueueVelocity.getInstance().getQueueConfig().shouldRemoveOnKickReason(reason)) {
                    player.sendMessage(ColorUtil.translate(" "));
                    player.sendMessage(reasonComponent);
                    player.sendMessage(ColorUtil.translate(" "));

                    player.sendActionBar(ColorUtil.translate(CrazyQueueVelocity.getInstance().getQueueConfig().format("actionbar.failed-to-connect", Map.of("server", registeredServer.getServerInfo().getName()))));
                    future.complete(true);
                    return;
                }
            }

            future.complete(false);
        });

        return future;
    }

}
