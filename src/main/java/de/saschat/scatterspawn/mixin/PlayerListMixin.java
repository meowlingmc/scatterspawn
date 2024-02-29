package de.saschat.scatterspawn.mixin;

import de.saschat.scatterspawn.ScatterSpawn;
import de.saschat.scatterspawn.ducks.ServerPlayerDuck;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(at = @At("HEAD"), method = "respawn")
    public void beforeRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ScatterSpawn.INSTANCE.registerRespawn(serverPlayer.getUUID());
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;load(Lnet/minecraft/server/level/ServerPlayer;)Lnet/minecraft/nbt/CompoundTag;"), method = "placeNewPlayer")
    public CompoundTag redirectLoad(PlayerList instance, ServerPlayer serverPlayer) {
        CompoundTag tag = instance.load(serverPlayer);
        if(tag == null) {
            ScatterSpawn.INSTANCE.registerRespawn(serverPlayer.getUUID());
            ((ServerPlayerDuck)(Object) serverPlayer).scatterSpawn$invokeFudgeSpawnLocation();
        }
        return tag;
    }
}
