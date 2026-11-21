package com.variantsmp;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class SpearAbility {

    private SpearAbility() {}

    private static final RegistryKey<Enchantment> LUNGE =
            RegistryKey.of(RegistryKeys.ENCHANTMENT, Identifier.ofVanilla("lunge"));

    private static ServerPlayerEntity stabbing;
    private static ServerPlayerEntity lunging;
    private static boolean lunged;

    public static boolean allowLunge(Entity attacker) {
        try {
            if (!(attacker instanceof ServerPlayerEntity player)) return true;
            VariantSMP mod = VariantSMP.get();
            if (mod == null) return true;
            if (!holdsLunge(player)) return true;
            PlayerState s = mod.state(player);
            int now = mod.now();
            boolean cooling = s.spearCooling && now < s.spearCooldownUntil;
            boolean sneakSave = mod.config.spearSneakSave && player.isSneaking();
            if (cooling) s.spearShowUntil = now + Abilities.SHOW_TICKS;
            if (cooling || sneakSave) s.spearFreeJabTick = now;
            if (player.hasVehicle() || player.isGliding() || player.isTouchingWater()) return true;
            if (sneakSave) return false;
            if (cooling) return false;
            if (s.spearAirChain) return true;
            if (player.isOnGround()) {
                startCooldown(s, now);
                return true;
            }
            s.spearAirChain = true;
            return true;
        } catch (Throwable t) {
            VariantSMP.LOGGER.warn("variantSMP: lunge check failed", t);
            return true;
        }
    }

    public static boolean beginStab(ServerPlayerEntity player) {
        if (!holdsLunge(player)) return false;
        stabbing = player;
        lunged = false;
        return true;
    }

    public static boolean endStab() {
        boolean result = lunged;
        stabbing = null;
        lunged = false;
        return result;
    }

    public static void beginLunge(Entity attacker) {
        lunging = attacker instanceof ServerPlayerEntity player ? player : null;
    }

    public static void endLunge() {
        lunging = null;
    }

    public static boolean freeExhaustion(ServerPlayerEntity player) {
        if (player == lunging) {
            if (player == stabbing) lunged = true;
            return false;
        }
        if (player == stabbing) return true;
        VariantSMP mod = VariantSMP.get();
        if (mod == null) return false;
        PlayerState s = mod.peek(player);
        return s != null && s.spearFreeJabTick == mod.now();
    }

    static boolean holdsLunge(ServerPlayerEntity player) {
        RegistryEntry<Enchantment> lunge = Abilities.enchant(player, LUNGE);
        if (lunge == null) return false;
        return EnchantmentHelper.getLevel(lunge, player.getMainHandStack()) > 0
                || EnchantmentHelper.getLevel(lunge, player.getOffHandStack()) > 0;
    }

    static void startCooldown(PlayerState s, int now) {
        int ticks = Math.max(0, VariantSMP.get().config.spearCooldownSeconds) * 20;
        s.spearAirChain = false;
        s.spearCooldownUntil = now + ticks;
        s.spearCooling = ticks > 0;
    }

    static void tick(ServerPlayerEntity player, PlayerState s, int now) {
        if (s.spearAirChain && player.isOnGround()) {
            startCooldown(s, now);
            return;
        }
        if (s.spearCooling && now >= s.spearCooldownUntil) {
            s.spearCooling = false;
            Abilities.ready(player, "Lunge available");
        }
    }

    static String cooldownText(PlayerState s, int now) {
        if (s.spearAirChain) return "§bAir lunges";
        if (!s.spearCooling || now >= s.spearCooldownUntil) return null;
        return "§cLunge " + Abilities.secondsLeft(s.spearCooldownUntil, now) + "s";
    }
}
