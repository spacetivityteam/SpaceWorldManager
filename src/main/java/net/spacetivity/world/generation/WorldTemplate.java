package net.spacetivity.world.generation;

import lombok.Getter;
import org.bukkit.World;
import org.bukkit.WorldType;
import org.bukkit.generator.ChunkGenerator;

@Getter
public enum WorldTemplate {

    NORMAL_WORLD(WorldType.NORMAL, World.Environment.NORMAL, null),
    NETHER_WORLD(WorldType.NORMAL, World.Environment.NETHER, null),
    END_WORLD(WorldType.NORMAL, World.Environment.THE_END, null),
    VOID_WORLD(WorldType.FLAT, World.Environment.NORMAL, new VoidChunkGenerator()),
    FLAT_WORLD(WorldType.FLAT, World.Environment.NORMAL, null);

    private final WorldType type;
    private final World.Environment environment;
    private final ChunkGenerator generator;

    WorldTemplate(WorldType type, World.Environment environment, ChunkGenerator generator) {
        this.type = type;
        this.environment = environment;
        this.generator = generator;
    }
}
