package de.saschat.scatterspawn.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.SetWorldSpawnCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(SetWorldSpawnCommand.class)
public class SetWorldSpawnCommandMixin {

    /**
     * @author Sascha T.
     * @reason Redundant method.
     */
    @Overwrite
    private static int setSpawn(CommandSourceStack commandSourceStack, BlockPos blockPos, float f) {
        commandSourceStack.sendFailure(Component.literal("The functionality of this command has been made redundant. Please use /scatterspawn for configuration.").withStyle(ChatFormatting.RED));
        return 0;
    }
}
