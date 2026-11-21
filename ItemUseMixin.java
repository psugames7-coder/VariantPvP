package com.variantsmp.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.variantsmp.Stages;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Item.class)
public abstract class ItemUseMixin {

    @WrapMethod(method = "finishUsing(Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Lnet/minecraft/entity/LivingEntity;)Lnet/minecraft/item/ItemStack;")
    private ItemStack variantsmp$finishUsing(ItemStack stack, World world, LivingEntity user, Operation<ItemStack> original) {
        boolean previous = Stages.potionBoost();
        boolean boost = false;
        try {
            boost = stack.isOf(Items.POTION) && Stages.isOwner(user);
        } catch (Throwable ignored) {
        }
        if (boost) Stages.potionBoost(true);
        try {
            return original.call(stack, world, user);
        } finally {
            Stages.potionBoost(previous);
        }
    }
}
