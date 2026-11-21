package com.variantsmp;

import com.variantsmp.mixin.VaultServerDataInvoker;
import com.variantsmp.mixin.VaultServerInvoker;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.vault.VaultConfig;
import net.minecraft.block.vault.VaultServerData;
import net.minecraft.block.vault.VaultSharedData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class VaultPicker {

    private VaultPicker() {}

    private static final int SAMPLES = 200;
    private static final int OPTION_SLOTS = 45;

    private record Session(ServerWorld world, BlockPos pos, VaultConfig config, VaultServerData serverData,
                           VaultSharedData sharedData, int picks, List<List<ItemStack>> options) {}

    private static Session session;
    private static boolean bypass;
    private static List<ItemStack> override;
    private static UUID overrideFor;

    public static boolean intercept(ServerWorld world, BlockPos pos, BlockState state, VaultConfig config,
                                    VaultServerData serverData, VaultSharedData sharedData, PlayerEntity player, ItemStack stack) {
        if (bypass) return false;
        try {
            if (!(player instanceof ServerPlayerEntity p) || !Stages.isOwner(p)) return false;
            if (!stack.isOf(Items.OMINOUS_TRIAL_KEY)) return false;
            if (!VaultServerInvoker.variantsmp$isValidKey(config, stack)) return false;
            if (!Stages.vaultReady(p)) return false;
            if (((VaultServerDataInvoker) (Object) serverData).variantsmp$hasRewardedPlayer(p)) return false;

            int picks = VaultServerInvoker.variantsmp$generateLoot(world, config, pos, p, stack).size();
            if (picks <= 0) return false;
            Map<Item, List<ItemStack>> found = new LinkedHashMap<>();
            for (int i = 0; i < SAMPLES; i++) {
                for (ItemStack s : VaultServerInvoker.variantsmp$generateLoot(world, config, pos, p, stack)) {
                    if (s.isEmpty()) continue;
                    found.computeIfAbsent(s.getItem(), k -> new ArrayList<>()).add(s.copy());
                }
            }
            if (found.isEmpty()) return false;
            List<List<ItemStack>> options = new ArrayList<>(found.values());
            if (options.size() > OPTION_SLOTS) options = new ArrayList<>(options.subList(0, OPTION_SLOTS));
            session = new Session(world, pos, config, serverData, sharedData, picks, options);
            Session opened = session;
            p.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                    (syncId, inv, ignored) -> new Handler(syncId, inv, new SimpleInventory(54), opened, p),
                    Text.translatable("block.minecraft.vault")));
            return true;
        } catch (Throwable t) {
            session = null;
            return false;
        }
    }

    public static List<ItemStack> loot(List<ItemStack> original, PlayerEntity player) {
        if (override == null || player == null || !player.getUuid().equals(overrideFor)) return original;
        List<ItemStack> out = new ArrayList<>();
        for (ItemStack s : override) out.add(s.copy());
        override = null;
        overrideFor = null;
        return out;
    }

    private static void finish(ServerPlayerEntity p, Session s, List<ItemStack> chosen) {
        if (session != s) return;
        session = null;
        try {
            if (p.isRemoved() || p.getEntityWorld() != s.world()) return;
            BlockState state = s.world().getBlockState(s.pos());
            if (!state.isOf(Blocks.VAULT)) return;
            ItemStack key = p.getMainHandStack().isOf(Items.OMINOUS_TRIAL_KEY) ? p.getMainHandStack()
                    : p.getOffHandStack().isOf(Items.OMINOUS_TRIAL_KEY) ? p.getOffHandStack() : ItemStack.EMPTY;
            if (key.isEmpty()) return;
            override = chosen;
            overrideFor = p.getUuid();
            bypass = true;
            try {
                VaultServerInvoker.variantsmp$tryUnlock(s.world(), s.pos(), state, s.config(), s.serverData(), s.sharedData(), p, key);
            } finally {
                bypass = false;
                override = null;
                overrideFor = null;
            }
            if (((VaultServerDataInvoker) (Object) s.serverData()).variantsmp$hasRewardedPlayer(p)) Stages.vaultUsed(p);
        } catch (Throwable ignored) {
        }
    }

    private static final class Handler extends GenericContainerScreenHandler {

        private final SimpleInventory inv;
        private final Session s;
        private final ServerPlayerEntity owner;
        private final List<ItemStack> chosen = new ArrayList<>();
        private boolean done;

        Handler(int syncId, PlayerInventory playerInv, SimpleInventory inv, Session s, ServerPlayerEntity owner) {
            super(ScreenHandlerType.GENERIC_9X6, syncId, playerInv, inv, 6);
            this.inv = inv;
            this.s = s;
            this.owner = owner;
            render();
        }

        @Override
        public void onSlotClick(int slot, int button, SlotActionType action, PlayerEntity player) {
            if (done || slot < 0 || slot >= 54) return;
            if (action != SlotActionType.PICKUP && action != SlotActionType.QUICK_MOVE) return;
            if (slot < OPTION_SLOTS) {
                if (slot >= s.options().size()) return;
                List<ItemStack> samples = s.options().get(slot);
                chosen.add(samples.get(owner.getRandom().nextInt(samples.size())).copy());
            } else {
                int idx = slot - OPTION_SLOTS;
                if (idx < chosen.size()) chosen.remove(idx);
            }
            if (chosen.size() >= s.picks()) {
                done = true;
                List<ItemStack> result = new ArrayList<>(chosen);
                MinecraftServer server = s.world().getServer();
                owner.closeHandledScreen();
                server.execute(() -> finish(owner, s, result));
                return;
            }
            render();
            syncState();
        }

        @Override
        public ItemStack quickMove(PlayerEntity player, int slot) {
            return ItemStack.EMPTY;
        }

        @Override
        public void onClosed(PlayerEntity player) {
            super.onClosed(player);
            if (!done && session == s) session = null;
        }

        private void render() {
            for (int i = 0; i < 54; i++) inv.setStack(i, ItemStack.EMPTY);
            for (int i = 0; i < s.options().size() && i < OPTION_SLOTS; i++) {
                inv.setStack(i, s.options().get(i).get(0).copy());
            }
            for (int i = 0; i < chosen.size() && i < 9; i++) {
                inv.setStack(OPTION_SLOTS + i, chosen.get(i).copy());
            }
        }
    }
}
