package de.saschat.scatterspawn.logic;

import com.google.gson.JsonObject;
import de.saschat.scatterspawn.ScatterSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class DefaultScatterer implements Scatterer {
    public DefaultScatterer() {}
    public static final ResourceLocation ID = new ResourceLocation(ScatterSpawn.MOD_ID, "default");
    @Override
    public Output scatter(ServerPlayer player, JsonObject playerConfig, JsonObject scatterConfig) {
        return new Output(null, new Vec3(0, 60, 0));
    }

    @Override
    public List<String> scattererConfigNames() {
        return List.of();
    }

    @Override
    public List<String> playerConfigNames() {
        return List.of();
    }

}
