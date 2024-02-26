package de.saschat.scatterspawn.logic;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Map;

public interface Scatterer {
    /**
     * Determines the respawn point of a player.
     * @return Respawn point
     */
    Output scatter(ServerPlayer player, JsonObject playerConfig, JsonObject scatterConfig);

    /**
     * Scatterer output
     * @param level The level to send to, may be null for overworld
     * @param position The position to send to, must not be null
     */
    public record Output(ResourceKey<Level> level, Vec3 position) {}

    public List<String> scattererConfigNames();
    public List<String> playerConfigNames();
}
