package de.saschat.scatterspawn.mixin.respawn;

import de.saschat.scatterspawn.ScatterSpawn;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.storage.ServerLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
    /**
     * @author Sascha T.
     * @reason Redundant method.
     */
    @Overwrite
    private static void setInitialSpawn(ServerLevel serverLevel, ServerLevelData serverLevelData, boolean bl, boolean bl2) {
        serverLevelData.setSpawn(new BlockPos((int) 29e6, (int) 0, (int) 29e6), 0.0f);
    }

    @Redirect(method = "prepareLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;addRegionTicket(Lnet/minecraft/server/level/TicketType;Lnet/minecraft/world/level/ChunkPos;ILjava/lang/Object;)V"))
    public void redirectAddRegionTicket(ServerChunkCache instance, TicketType<Object> ticketType, ChunkPos chunkPos, int i, Object object) {
        // Do nothing. Spawn isn't real.
    }

    @Redirect(method = "prepareLevels", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerChunkCache;getTickingGenerated()I"))
    public int getTickingGenerated(ServerChunkCache instance) {
        return 441; // Exit immediately.
    }
}
