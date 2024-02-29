package de.saschat.scatterspawn.mixin;

import com.mojang.authlib.GameProfile;
import de.saschat.scatterspawn.ScatterSpawn;
import de.saschat.scatterspawn.ducks.ServerPlayerDuck;
import de.saschat.scatterspawn.logic.Scatterer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.*;

/*
    The amount of mixin-fu is actually crazy
    Well it would've been if mixin was nearly as powerful as ASM
 */
@Mixin(ServerPlayer.class)
public abstract class ServerPlayerMixin extends Player implements ServerPlayerDuck {
    @Shadow
    public abstract ServerLevel serverLevel();

    @Shadow @Final public MinecraftServer server;

    @Unique
    private Scatterer.Output lastScatter;

    public ServerPlayerMixin(Level level, BlockPos blockPos, float f, GameProfile gameProfile) {
        super(level, blockPos, f, gameProfile);
    }

    @Override
    public void scatterSpawn$invokeFudgeSpawnLocation() {
        fudgeSpawnLocation(serverLevel());
    }

    /**
     * @author Sascha T.
     * @reason Determining server spawn locations is the responsibility of ScatterSpawn
     */
    @Overwrite
    private void fudgeSpawnLocation(ServerLevel serverLevel) {
        if(ScatterSpawn.INSTANCE.isRespawning(getUUID())) {
            lastScatter = ScatterSpawn.INSTANCE.getSpawnLocation((ServerPlayer) (Object) this);

            System.out.println("Last scatter:");
            System.out.println("- "+lastScatter.position().x);
            System.out.println("- "+lastScatter.position().y);
            System.out.println("- "+lastScatter.position().z);

            setPos(lastScatter.position());
            if (lastScatter.level() != null)
                setLevel(server.getLevel(lastScatter.level()));
            // serverLevel.getChunkSource().addRegionTicket(TicketType.POST_TELEPORT, new ChunkPos(new BlockPos((int) getX(), (int) getY(), (int) getZ())), 1, getId());
            while (!noCollision(serverLevel) && this.getY() < (double) (serverLevel.getMaxBuildHeight() - 1)) {
                this.setPos(this.getX(), this.getY() + 1.0, this.getZ());
            }
            ScatterSpawn.INSTANCE.unregisterRespawn(getUUID());
        }
    }

    @Unique
    private boolean noCollision(ServerLevel level) {
        BlockPos[] pos = new BlockPos[] {
            new BlockPos((int) getX(), (int) getY(), (int) getZ()),
            new BlockPos((int) getX(), (int) getY()+1, (int) getZ())
        };
        for (BlockPos p : pos)
            if(!level.getBlockState(p).isAir())
                return false;
        return true;
    }
}
