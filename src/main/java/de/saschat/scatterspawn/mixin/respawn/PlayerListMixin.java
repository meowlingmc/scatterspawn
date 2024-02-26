package de.saschat.scatterspawn.mixin.respawn;

import de.saschat.scatterspawn.ScatterSpawn;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerList.class)
public class PlayerListMixin {
    @Inject(at = @At("HEAD"), method = "respawn")
    public void beforeRespawn(ServerPlayer serverPlayer, boolean bl, CallbackInfoReturnable<ServerPlayer> cir) {
        ScatterSpawn.INSTANCE.registerRespawn(serverPlayer.getUUID());
    }
}
