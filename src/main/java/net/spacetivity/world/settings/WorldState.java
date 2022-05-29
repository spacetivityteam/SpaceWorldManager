package net.spacetivity.world.settings;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum WorldState {

    IN_PROGRESS("In progress"),
    FINISHED("Finished");

    private final String name;

}
