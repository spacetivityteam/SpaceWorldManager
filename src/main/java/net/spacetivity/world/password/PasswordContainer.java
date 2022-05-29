package net.spacetivity.world.password;

import lombok.Data;

@Data
public class PasswordContainer {

    private String hashedPassword;

    private String salt;

}
