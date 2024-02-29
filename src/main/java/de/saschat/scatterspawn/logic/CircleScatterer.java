package de.saschat.scatterspawn.logic;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.saschat.scatterspawn.ScatterSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

public class CircleScatterer implements Scatterer {
    public static final ResourceLocation ID = new ResourceLocation(ScatterSpawn.MOD_ID, "circle");
    @Override
    public Output scatter(ServerPlayer player, JsonObject playerConfigs, JsonObject scatterConfig) {
        JsonObject playerConfig = playerConfigs.getAsJsonObject(player.getUUID().toString());
        if (playerConfig == null) {
            playerConfig = new JsonObject();
        }
        double scatterY = Objects.requireNonNullElse(scatterConfig.get("spawnY"), new JsonPrimitive(60.0)).getAsDouble();

        if (playerConfig.get("lastX") != null && playerConfig.get("lastZ") != null) {
            return new Scatterer.Output(null, new Vec3(
                playerConfig.get("lastX").getAsFloat(),
                scatterY,
                playerConfig.get("lastZ").getAsFloat()
            ));
        }
        String[] bannedBiomes = Objects.requireNonNullElse(scatterConfig.get("bannedBiomes"), new JsonPrimitive("minecraft:ocean")).getAsString().split(",");

        // generate new position!
        double originX = Objects.requireNonNullElse(scatterConfig.get("originX"), new JsonPrimitive(0.0f)).getAsDouble();
        double originZ = Objects.requireNonNullElse(scatterConfig.get("originZ"), new JsonPrimitive(0.0f)).getAsDouble();

        int minimumLength = Objects.requireNonNullElse(scatterConfig.get("minimumLength"), new JsonPrimitive(5000)).getAsInt();
        int maximumLength = Objects.requireNonNullElse(scatterConfig.get("maximumLength"), new JsonPrimitive(10000)).getAsInt();

        int minimumDistance = Objects.requireNonNullElse(scatterConfig.get("minimumDistance"), new JsonPrimitive(-1)).getAsInt();

        double x, z, angle, distance;
        Random random = new Random(player.getUUID().getLeastSignificantBits() ^ player.getUUID().getMostSignificantBits());
        // :p
        int iteration = 0;
        search: while (true) {
            iteration++;

            angle = random.nextFloat(0, 360);
            if(maximumLength > minimumLength)
                distance = random.nextInt(minimumLength, maximumLength);
            else
                distance = minimumLength;

            x = Math.sin(angle * Math.PI / 360) * distance + originX;
            z = Math.cos(angle * Math.PI / 360) * distance + originZ;

            double minDist = Double.MAX_VALUE;
            for (Map.Entry<String, JsonElement> entry : playerConfigs.entrySet()) {
                JsonObject instance = entry.getValue().getAsJsonObject();
                if (!instance.has("lastX") || !instance.has("lastZ"))
                    continue;
                double lastX = instance.get("lastX").getAsDouble();
                double lastZ = instance.get("lastZ").getAsDouble();

                minDist = Math.min(Math.sqrt(
                    Math.pow(lastX - x, 2) + Math.pow(lastZ - z, 2)
                ), minDist);
            }
            if (iteration > 200)
                break;

            Holder<Biome> biome = player.level().getBiome(new BlockPos((int) x, (int) scatterY, (int) z));
            for (String s : bannedBiomes)
                if(biome.is(new ResourceLocation(s)))
                    continue search;


            if(minDist > minimumDistance || minimumDistance < 0)
                break;
        }

        playerConfig.add("lastX", new JsonPrimitive(x));
        playerConfig.add("lastZ", new JsonPrimitive(z));
        playerConfig.add("dbgAngle", new JsonPrimitive(angle));
        playerConfig.add("dbgDistance", new JsonPrimitive(distance));
        playerConfig.add("dbgIterations", new JsonPrimitive(iteration));

        playerConfigs.add(player.getUUID().toString(), playerConfig);
        return new Output(null, new Vec3(x, scatterY, z));
    }


    @Override
    public List<String> scattererConfigNames() {
        return List.of("spawnY", "originX", "originY", "minimumLength", "maximumLength", "minimumDistance", "bannedBiomes");
    }

    @Override
    public List<String> playerConfigNames() {
        return List.of("lastX", "lastZ", "dbgAngle", "dbgDistance", "dbgIterations");
    }

}
