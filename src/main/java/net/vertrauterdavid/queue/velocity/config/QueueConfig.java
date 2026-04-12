package net.vertrauterdavid.queue.velocity.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurations;
import lombok.Getter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@Configuration
@Getter
public class QueueConfig {

    private QueueSettings queue = new QueueSettings();
    private Colors colors = new Colors();
    private Messages messages = new Messages();
    private Actionbar actionbar = new Actionbar();

    public static QueueConfig load(Path dataDirectory) {
        try {
            Files.createDirectories(dataDirectory);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create Velocity config directory", exception);
        }

        return YamlConfigurations.update(dataDirectory.resolve("config.yml"), QueueConfig.class);
    }

    public String getString(String key) {
        return switch (key) {
            case "messages.queue-info-header" -> messages.queueInfoHeader;
            case "messages.queue-info-entry-players" -> messages.queueInfoEntryPlayers;
            case "messages.queue-info-entry" -> messages.queueInfoEntry;
            case "messages.queue-info-time" -> messages.queueInfoTime;
            case "messages.server-not-found" -> messages.serverNotFound;
            case "messages.queue-cleared" -> messages.queueCleared;
            case "messages.connecting-players" -> messages.connectingPlayers;
            case "messages.no-sendall-permission" -> messages.noSendallPermission;
            case "messages.player-not-online" -> messages.playerNotOnline;
            case "messages.players-added-to-queue" -> messages.playersAddedToQueue;
            case "messages.already-in-queue" -> messages.alreadyInQueue;
            case "messages.already-on-server" -> messages.alreadyOnServer;
            case "messages.queue-usage" -> messages.queueUsage;
            case "messages.added-to-queue" -> messages.addedToQueue;
            case "messages.backend-no-reason" -> messages.backendNoReason;
            case "actionbar.queue-position" -> actionbar.queuePosition;
            case "actionbar.queue-clear" -> actionbar.queueClear;
            case "actionbar.connected" -> actionbar.connected;
            case "actionbar.failed-to-connect" -> actionbar.failedToConnect;
            default -> "";
        };
    }

    public List<String> getStringList(String key) {
        if (key.equals("messages.crazyqueue-usages")) {
            return messages.crazyqueueUsages;
        }

        return List.of();
    }

    public double getProcessIntervalMillis() {
        return queue.processIntervalSeconds * 1000;
    }

    public double getPingIntervalMillis() {
        return queue.pingIntervalSeconds * 1000;
    }

    public boolean isAutoRequeueOnKick() {
        return queue.autoRequeueOnKick;
    }

    public boolean shouldRemoveOnKickReason(String reason) {
        String normalizedReason = reason.toLowerCase();
        return queue.removeOnKickReasonContains.stream().anyMatch(value -> normalizedReason.contains(value.toLowerCase()));
    }

    public boolean isQueueDisabled(String server) {
        return queue.disabledServers.stream().anyMatch(disabledServer -> disabledServer.equalsIgnoreCase(server));
    }

    @Configuration
    public static class QueueSettings {

        @Comment("How often queues try to move the first player to the target server, in seconds.")
        private double processIntervalSeconds = 0.25;

        @Comment("How often queue servers are pinged to update their online/offline state, in seconds.")
        private double pingIntervalSeconds = 5;

        @Comment("If enabled, players are added back to the previous server queue when they get kicked.")
        private boolean autoRequeueOnKick = false;

        @Comment("Kick reason fragments that remove a player from the queue instead of retrying later.")
        private List<String> removeOnKickReasonContains = List.of("banned");

        @Comment("Velocity server names that should not get an automatic queue.")
        private List<String> disabledServers = List.of(
                "main",
                "lobby-01",
                "lobby-02",
                "lobby-03",
                "lobby-04",
                "lobby-05",
                "lobby-prem-01",
                "lobby-prem-02",
                "lobby-prem-03",
                "economy-dev",
                "event",
                "Event",
                "BedWars"
        );

    }

    @Configuration
    public static class Colors {
        private String blue = "&#559eff";
        private String green = "&#7cfc00";
        private String red = "&#ff0000";
    }

    @Configuration
    public static class Messages {
        private String prefix = "&8| %blue%TrySmp &8> &7";
        private String serverNotFound = "%prefix%The server %red%%server% &7does not exist.";
        private String queueCleared = "%prefix%The queue of the server %red%%server% &7has been cleared.";
        private String connectingPlayers = "%prefix%%amount% player%plural% will be connected.";
        private String noSendallPermission = "%prefix%You do not have permission to send all players";
        private String playerNotOnline = "%prefix%The player %red%%player% &7is not online.";
        private String playersAddedToQueue = "%prefix%%green%%amount% &7player%plural% have been added to the queue for %green%%server%&7.";
        private String alreadyInQueue = "%prefix%You are already in the queue for %red%%server%&7.";
        private String alreadyOnServer = "%prefix%You are already on the server %red%%server%&7.";
        private String queueUsage = "%prefix%Please use: %red%/%command% <server>";
        private List<String> crazyqueueUsages = List.of(
                "%prefix%Please use: %red%/%command% info",
                "%prefix%Please use: %red%/%command% info --players",
                "%prefix%Please use: %red%/%command% clear <server>",
                "%prefix%Please use: %red%/%command% queue <server>",
                "%prefix%Please use: %red%/%command% connect <queue> <amount>",
                "%prefix%Please use: %red%/%command% send <playerName / all / current / hub> <server>"
        );
        private String queueInfoHeader = "&8|";
        private String queueInfoEntryPlayers = "&8| %green%%server% &8(&7%amount%&8): &7%players%";
        private String queueInfoEntry = "&8| %green%%server%&8: &7%amount% players";
        private String queueInfoTime = "&8| &7Queue Time: %green%%time%ms";
        private String addedToQueue = "%prefix%You have been added to the queue for %green%%server%";
        private String backendNoReason = "no reason";
    }

    @Configuration
    public static class Actionbar {
        private String queuePosition = "%green%#%position%&7 in the queue to %green%&n%server%&r &8(&7Waiting: %waiting%&8)";
        private String queueClear = " ";
        private String connected = "%green%Successfully connected to %server%";
        private String failedToConnect = "%red%Failed to connect to %server%";
    }

    public String format(String key, Map<String, String> placeholders) {
        return formatRaw(getString(key), placeholders);
    }

    public String formatRaw(String message, Map<String, String> placeholders) {
        String formatted = message
                .replace("%prefix%", messages.prefix)
                .replace("%blue%", colors.blue)
                .replace("%green%", colors.green)
                .replace("%red%", colors.red);

        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            formatted = formatted.replace("%" + entry.getKey() + "%", entry.getValue());
        }

        return formatted;
    }
}
