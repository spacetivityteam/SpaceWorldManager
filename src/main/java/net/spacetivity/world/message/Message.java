package net.spacetivity.world.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.md_5.bungee.api.ChatMessageType;

@AllArgsConstructor
@Getter
public class Message {

    private String key;
    private String text;
    private String type;
    private boolean active;

    public static Message prefix(String prefixName) {
        return new Message("message.prefix", prefixName, null, true);
    }

    public static Message of(String key, String text, ChatMessageType type, boolean usePrefix, boolean active) {
        String validText;
        if (usePrefix) validText = "%PREFIX% " + text;
        else validText = text;
        return new Message(key, validText, type.name(), active);
    }
}
