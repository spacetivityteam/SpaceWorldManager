package net.spacetivity.world.settings;

import lombok.Data;

import java.util.List;

@Data
public class WorldSettings {

    private String worldName;

    private String creatorName;

    private String forceGameMode;

    private WorldState state;

    private List<String> trustedBuilders;

}
