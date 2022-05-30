package net.spacetivity.world.configuration;

import lombok.Data;

@Data
public class ConfigurationData {

    private String bypassPermission;
    private String mainWorldName;
    private boolean saveOnShutdown;
    private boolean stopLag;
    private boolean showCurrentWorldActionBar;
    private boolean showScoreboard;
    private boolean giveWorldItemOnJoin;
    private String worldItemMaterial;

}
