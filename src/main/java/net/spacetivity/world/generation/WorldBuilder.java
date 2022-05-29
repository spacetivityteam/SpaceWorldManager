package net.spacetivity.world.generation;

import net.spacetivity.world.SpaceWorldManager;
import lombok.SneakyThrows;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.jetbrains.annotations.NotNull;

public class WorldBuilder extends WorldCreator {

    public WorldBuilder(@NotNull String name, WorldTemplate pattern) {
        super(name);
        if (pattern.getGenerator() != null)
            type(pattern.getType()).generator(pattern.getGenerator()).generateStructures(false).environment(pattern.getEnvironment());
        else
            type(pattern.getType()).generateStructures(false).environment(pattern.getEnvironment());
    }

    public WorldBuilder toggleStructures(boolean value) {
        generateStructures(value);
        return this;
    }

    @SneakyThrows
    public World build(boolean usePassword, String... optionalPassword) {
        World world = createWorld();

        if (usePassword)
            SpaceWorldManager.getInstance().getWorldUtils().insertPasswordToWorld(name(), optionalPassword[0]);

        return world;
    }
}
