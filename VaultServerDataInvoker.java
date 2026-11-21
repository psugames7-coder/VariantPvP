package com.variantsmp.mixin;

import net.minecraft.block.vault.VaultServerData;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(VaultServerData.class)
public interface VaultServerDataInvoker {

    @Invoker("hasRewardedPlayer")
    boolean variantsmp$hasRewardedPlayer(PlayerEntity player);
}
