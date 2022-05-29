package net.spacetivity.world.scoreboardapi;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PlayerTagManager {

    private final Map<String, List<PlayerTag>> tagMap = new HashMap<>();

    public void setTag(String from, PlayerTag playerTag) {
        List<PlayerTag> playerTags = tagMap.getOrDefault(from, new ArrayList<>());

        for (PlayerTag tag : playerTags) {
            if (tag.name().equals(playerTag.name())) {
                playerTags.remove(tag);
                break;
            }
        }

        playerTags.add(playerTag);
        tagMap.put(from, playerTags);
    }

    public void clearTags(String from) {
        tagMap.remove(from);
    }

    public String getAllTags(String from, Player target) {
        if (!tagMap.containsKey(from)) return "";

        Player player = Bukkit.getPlayer(from);

        if (player == null) return "";

        List<PlayerTag> playerTags = tagMap.getOrDefault(from, new ArrayList<>());

        PlayerPair playerPair = new PlayerPair(player, target);

        List<PlayerTag> sortedTags = playerTags.stream().filter(playerTag -> playerTag.filter().test(playerPair))
                .sorted(Comparator.comparing(PlayerTag::priority)).toList();

        return " " + sortedTags.stream().map(PlayerTag::value).map(function -> function.apply(playerPair)).collect(Collectors.joining(" "));
    }

    public String getTagsFromPosition(String fromPlayer, Player target, PlayerTagPosition position) {
        if (!tagMap.containsKey(target.getName())) return "";

        Player player = Bukkit.getPlayer(fromPlayer);

        if (player == null) return "";

        PlayerPair playerPair = new PlayerPair(player, target);
        List<PlayerTag> playerTags = tagMap.getOrDefault(fromPlayer, new ArrayList<>());
        List<PlayerTag> sortedTags = playerTags.stream()
                .filter(playerTag -> playerTag.filter().test(playerPair))
                .filter(playerTag -> playerTag.position().equals(position))
                .sorted(Comparator.comparing(PlayerTag::priority)).toList();

        return sortedTags.stream().map(PlayerTag::value).map(function -> function.apply(playerPair)).collect(Collectors.joining(" "));
    }

    public void removeTag(String from, String tagName) {
        if (!tagMap.containsKey(from)) return;
        List<PlayerTag> playerTags = tagMap.getOrDefault(from, new ArrayList<>());
        playerTags.stream().filter(playerTag -> playerTag.name().equals(tagName)).findFirst().ifPresent(playerTags::remove);
        if (!playerTags.isEmpty()) tagMap.put(from, playerTags);
        else tagMap.remove(from);
    }

    public boolean hasTag(String from, String tagName) {
        if (!tagMap.containsKey(from)) return false;
        List<PlayerTag> playerTags = tagMap.getOrDefault(from, new ArrayList<>());
        return playerTags.stream().anyMatch(playerTag -> playerTag.name().equals(tagName));
    }
}