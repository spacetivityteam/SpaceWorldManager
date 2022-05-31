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
        messages.add(Message.of("world.command.usage.title", "§7All available commands:", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.usage.prefix", "§f- §7/", ChatMessageType.CHAT, false, true));
        messages.add(Message.of("world.command.already.trust", "This player is already trusted.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.trust.player", "Player §f{0} §7was trusted for world §f{1}§7.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.player.isnotrusted", "§7This player is not trusted.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.untrust.player", "§7Player §f{0} §7was untrusted for world §f{1}§7.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.create.isExist", "§7This world doesn't exist.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.unlock.isNotLocked", "§7This world is not locked.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.unlock.successful", "§7Password from world §f{0} §7was removed.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.lock.isLocked", "§7This world is already locked.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.lock", "§7This world is §clocked§7.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.import.successful", "World §f{0} §7was successfully imported.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.import.isNoWorldFolder", "§7There is no world folder existing with that name.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.import.isAlreadyLoaded", "§7This world is already imported and loaded.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.unload.successful", "§7World §f{0} §7was unloaded.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.unload.successful.suffix", "§7World §f{0} §7was deleted. Storage file was also destroyed.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.delete.mainWorld", "§7You can't delete the main world ingame.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.delete.confirm", "Are you sure that you want delete the world §f{0}§7?", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.delete.confirm.suffix", "§7If your decision is made, type this command again. (lasts §f60 §7seconds)", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.info", "§7You are in the world: §f{0}", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.template", "§7All available world-templates:", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.command.template.suffix", "§7{ß}", ChatMessageType.CHAT, true, true));

        messages.add(Message.of("messages.needInteger", "§7Please enter a integer!", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("messages.noPermissions", "§cYou are not permitted to execute this action.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("messages.playerOffline", "This player is currently offline!", ChatMessageType.CHAT, true,true));
        messages.add(Message.of("messages.join.world", "§7Entered world §f{0}§7.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("messages.denied.join", "§7Permission denied! Please use the correct password to proceed.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("messages.denied.password", "§7Wrong password. Entry denied.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("messages.successful.password", "§7Access granted!", ChatMessageType.CHAT, true, true));

        messages.add(Message.of("world.join.click.message.prefix", "§7Click§7.", ChatMessageType.CHAT, true, true));
        messages.add(Message.of("world.join.click.message.body", "§f§lHERE", ChatMessageType.CHAT, false, true));
        messages.add(Message.of("world.join.click.message.suffix", "§7to teleport yourself to the new world.", ChatMessageType.CHAT, false, true));

        return messages;
    }

    public MessagesData getFile() {
        return fileUtils.readFile(messageFilePath.toFile(), MessagesData.class);
    }

}
