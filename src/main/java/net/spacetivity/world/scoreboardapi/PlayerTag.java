package net.spacetivity.world.scoreboardapi;

import java.util.function.Function;
import java.util.function.Predicate;

public record PlayerTag(int priority, String name, PlayerTagPosition position, Function<PlayerPair, String> value, Predicate<PlayerPair> filter) {
}
