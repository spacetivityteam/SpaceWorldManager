package net.spacetivity.world.message;

import net.spacetivity.world.SpaceWorldManager;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.text.MessageFormat;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public class MessageUtil {

    public static Optional<Message> get(String key) {
        return SpaceWorldManager.getInstance().getMessageFileManager().getFile().getMessages().stream()
                .filter(message -> message.getKey().equalsIgnoreCase(key))
                .findFirst();
    }

    public static String prefix() {
        return Objects.requireNonNull(SpaceWorldManager.getInstance().getMessageFileManager().getFile().getMessages().stream()
                .filter(message -> message.getKey().equalsIgnoreCase("message.prefix"))
                .findFirst().orElse(null)).getText();
    }

    public static void send(Player player, String key, Object... toReplace) {
        validateMessage(player, key, message -> {
            String text = message.getText().replace("%PREFIX%", prefix());
            sendToPlayer(player, MessageFormat.format(text, toReplace), message.getType());
        });
    }

    public static void sendAndAppend(Player player, String key, String append, Object... toReplace) {
        validateMessage(player, key, message -> {
            String text = message.getText().replace("%PREFIX%", prefix());
            sendToPlayer(player, MessageFormat.format(text, toReplace) + append, message.getType());
        });
    }

    private static void validateMessage(Player requester, String key, Consumer<Message> result) {
        Optional<Message> optionalMessage = get(key);

        if (optionalMessage.isEmpty()) {
            requester.sendMessage("§cMessage §e" + key + " §cnot found.");
            return;
        }

        Message message = optionalMessage.get();
        if (!message.isActive()) return;
        result.accept(message);
    }

    private static void sendToPlayer(Player player, String message, String type) {
        player.spigot().sendMessage(ChatMessageType.valueOf(type), new TextComponent(message));
    }

}
