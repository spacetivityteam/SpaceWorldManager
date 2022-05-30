package net.spacetivity.world.message;

import net.spacetivity.world.SpaceWorldManager;
import net.spacetivity.world.utils.FileUtils;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MessageFileManager {

    private final FileUtils fileUtils = SpaceWorldManager.getInstance().getFileUtils();

    @Getter
    private final Path messageFilePath = Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath() + "/messages.json");

    public void createMessagesFile() {
        if (Files.exists(messageFilePath)) return;

        try {
            if (!Files.exists(Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath())))
                Files.createDirectory(Paths.get(SpaceWorldManager.getInstance().getDataFolder().getPath()));
        } catch (IOException e) {
            e.printStackTrace();
        }

        File configFile = messageFilePath.toFile();
        MessagesData messagesData = new MessagesData();
        messagesData.setMessages(getDefaultMessage());

        fileUtils.saveFile(configFile, messagesData);
    }

    private List<Message> getDefaultMessage() {
        List<Message> messages = new ArrayList<>();

        messages.add(Message.prefix("§3SWM §7|"));
        messages.add(Message.of("world.item.displayName", "§b§lWorld Manager", ChatMessageType.CHAT, false, true));
        messages.add(Message.of("world.item.alreadyInInventory", "§cYou already have the world item in your inventory!", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.usage.title", "All available commands:", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.usage.prefix", "§f- §7/", ChatMessageType.CHAT, false, true));

        messages.add(Message.of("messages.join.world", "§7Entered world §f{0}§7.", ChatMessageType.CHAT, true, true));

        return messages;
    }

    public MessagesData getFile() {
        return fileUtils.readFile(messageFilePath.toFile(), MessagesData.class);
    }

}
