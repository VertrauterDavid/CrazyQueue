package net.vertrauterdavid.queue.bukkit;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.Objects;

@SuppressWarnings("unused")
public class CrazyQueueBukkit extends JavaPlugin implements PluginMessageListener {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        Bukkit.getMessenger().registerIncomingPluginChannel(this, "crazyqueue:tobukkit", this);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "crazyqueue:toproxy");

        Objects.requireNonNull(getCommand("leavequeue")).setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return false;
            sendMessage(player, "command leavequeue");
            return true;
        });

        Objects.requireNonNull(getCommand("queue")).setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return false;
            if (!(args.length == 1)) return false;
            sendMessage(player, "command queue " + args[0]);
            return true;
        });

        Objects.requireNonNull(getCommand("server")).setExecutor((sender, command, label, args) -> {
            if (!(sender instanceof Player player)) return false;
            if (!(args.length == 1)) return false;
            sendMessage(player, "command server " + args[0]);
            return true;
        });
    }

    private void sendMessage(Player player, String data) {
        ByteArrayDataOutput byteArrayDataOutput = ByteStreams.newDataOutput();
        byteArrayDataOutput.writeUTF(data);

        player.sendPluginMessage(this, "crazyqueue:toproxy", byteArrayDataOutput.toByteArray());
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte @NonNull [] bytes) { }

}
