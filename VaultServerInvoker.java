package com.variantsmp.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(targets = "net.minecraft.block.entity.VaultBlockEntity$Server")
public interface VaultServerInvoker {

    @Invoker("generateLoot")
    static List<ItemStack> variantsmp$generateLoot(ServerWorld world, VaultConfig config, BlockPos pos, PlayerEntity player, ItemStack key) {
        throw new AssertionError();
    }

    @Invoker("isValidKey")
    static boolean variantsmp$isValidKey(VaultConfig config, ItemStack stack) {
        throw new AssertionError();
    }

    @Invoker("tryUnlock")
    static void variantsmp$tryUnlock(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config,
                                     VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player, ItemStack stack) {
        throw new AssertionError();
    }
}
