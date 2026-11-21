package com.variantsmp.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.variantsmp.VaultPicker;
import net.minecraft.block.BlockState;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(targets = "net.minecraft.block.entity.VaultBlockEntity$Server")
public abstract class VaultServerMixin {

    @Inject(method = "tryUnlock", at = @At("HEAD"), cancellable = true)
    private static void variantsmp$tryUnlock(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config,
                                             VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player,
                                             ItemStack stack, CallbackInfo ci) {
        if (VaultPicker.intercept(world, pos, state, config, serverData, sharedData, player, stack)) ci.cancel();
    }

    @ModifyReturnValue(method = "generateLoot", at = @At("RETURN"))
    private static List<ItemStack> variantsmp$generateLoot(List<ItemStack> original, ServerWorld world, VaultConfig config,
                                                           BlockPos pos, PlayerEntity player, ItemStack key) {
        return VaultPicker.loot(original, player);
    }
}
