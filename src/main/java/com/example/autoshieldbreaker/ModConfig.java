package com.example.autoshieldbreaker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = FabricLoader.getInstance().getConfigDir().resolve("autoshieldbreaker.json").toFile();
    private static ModConfig INSTANCE;

    public boolean enabled = true;
    public double cooldownSeconds = 0.5;

    public static ModConfig getInstance() {
        if (INSTANCE == null) INSTANCE = load();
        return INSTANCE;
    }

    private static ModConfig load() {
        if (FILE.exists()) {
            try (FileReader r = new FileReader(FILE)) {
                return GSON.fromJson(r, ModConfig.class);
            } catch (IOException e) { e.printStackTrace(); }
        }
        return new ModConfig();
    }

    public void save() {
        try (FileWriter w = new FileWriter(FILE)) {
            GSON.toJson(this, w);
        } catch (IOException e) { e.printStackTrace(); }
    }
}