package net.spacetivity.world.utils;

import net.spacetivity.world.SpaceWorldManager;

import java.io.*;

public class FileUtils {

    public <T> T readFile(File file, Class<T> cls) {
        try {
            return SpaceWorldManager.GSON.fromJson(new FileReader(file), cls);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void saveFile(File file, Object object) {
        try {
            FileWriter fileWriter = new FileWriter(file);
            SpaceWorldManager.GSON.toJson(object, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
