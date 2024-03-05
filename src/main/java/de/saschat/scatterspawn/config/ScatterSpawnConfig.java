package de.saschat.scatterspawn.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.InstanceCreator;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;
import de.saschat.scatterspawn.ScatterSpawn;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Objects;

// @todo: make code look nice instead of "just" working
public class ScatterSpawnConfig {
    static {
       FabricLoader.getInstance().getConfigDir().toFile().mkdirs();
    }
    public static final ScatterSpawnConfig INSTANCE = new ScatterSpawnConfig();
    private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().setPrettyPrinting().registerTypeAdapter(ScatterSpawnConfig.class, (InstanceCreator<?>) type -> INSTANCE).create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), ScatterSpawn.MOD_ID + ".json");

    public void dirty() {
        try {
            FileWriter writer = new FileWriter(FILE);
            GSON.toJson(this, writer);
            writer.flush();
            writer.close();

            writer = new FileWriter(new File(FabricLoader.getInstance().getConfigDir().toFile(), getPlayerPath()));
            GSON.toJson(scattererPlayerConfigsCache, writer);
            writer.flush();
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void load() {
        try {
            if(!FILE.exists())
                dirty();
            FileReader reader = new FileReader(FILE);
            GSON.fromJson(reader, ScatterSpawnConfig.class);
            reader.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Expose
    String scatterer = new ResourceLocation(ScatterSpawn.MOD_ID, "default").toString();

    public ResourceLocation getScatterer() {
        return new ResourceLocation(scatterer);
    }

    public void setScatterer(ResourceLocation location) {
        if(!scatterer.equals(location.toString())) {
            scattererConfig = new JsonObject();
            scattererPlayerConfigsCache = new JsonObject();
        }
        scatterer = location.toString();
        dirty();
    }

    @Expose
    private JsonObject scattererConfig = new JsonObject();

    @Expose
    private String scattererPlayerConfigPath;
    private JsonObject scattererPlayerConfigsCache;

    public JsonObject getScattererConfig() {
        JsonObject ret;
        if((ret = scattererConfig) != null)
            return ret;
        scattererConfig = new JsonObject();
        return scattererConfig;
    }

    public JsonObject getScattererPlayerConfigs() {
        scattererPlayerConfigPath = getPlayerPath();
        dirty();

        if(scattererPlayerConfigsCache != null)
            return scattererPlayerConfigsCache;
        File readFile = new File(scattererPlayerConfigPath);
        if(!readFile.exists())
            return scattererPlayerConfigsCache;
        try {
            FileReader reader = new FileReader(FILE);
            scattererPlayerConfigsCache = GSON.fromJson(reader, JsonObject.class);
            reader.close();
        } catch (Exception ex) {}
        if(scattererPlayerConfigsCache == null)
            scattererPlayerConfigsCache = new JsonObject();
        return scattererPlayerConfigsCache;
    }

    private String getPlayerPath() {
        return Objects.requireNonNullElse(scattererPlayerConfigPath, "scatterspawn-players.json");
    }
}
