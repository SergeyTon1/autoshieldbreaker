package com.example.autoshieldbreaker;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.lwjgl.glfw.GLFW;

public class AutoShieldBreakerClient implements ClientModInitializer {
    public static KeyBinding toggleKey;
    private static long lastActionTime = 0;

    @Override
    public void onInitializeClient() {
        ModConfig.getInstance().save();
        toggleKey = KeyBindingRegistry.register(
                "key.autoshieldbreaker.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_UNKNOWN,
                "key.categories.autoshieldbreaker"
        );
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(MinecraftClient client) {
        if (client.world == null || client.player == null || client.interactionManager == null) return;

        while (toggleKey.wasPressed()) {
            ModConfig cfg = ModConfig.getInstance();
            cfg.enabled = !cfg.enabled;
            cfg.save();
            client.inGameHud.getChatHud().addMessage(
                    net.minecraft.text.Text.literal("[AutoShieldBreaker] Статус: " + (cfg.enabled ? "✅ ВКЛ" : "❌ ВЫКЛ"))
            );
        }

        if (!ModConfig.getInstance().enabled) return;

        LivingEntity target = client.targetedEntity instanceof LivingEntity le ? le : null;
        if (target == null || !target.isBlocking()) return;

        ItemStack active = target.getActiveItem();
        if (active.isEmpty() || !active.isOf(Items.SHIELD)) return;

        long now = System.currentTimeMillis();
        double cooldownSec = ModConfig.getInstance().cooldownSeconds;
        double safeCooldown = Math.min(Math.max(cooldownSec, 0.0), 2.0);
        long cooldownMs = (long) (safeCooldown * 1000);

        if (now - lastActionTime < cooldownMs) return;

        int axeSlot = findAxeInHotbar(client);
        if (axeSlot == -1) return;

        int currentSlot = client.player.getInventory().selectedSlot;
        if (currentSlot != axeSlot) {
            client.player.getInventory().selectedSlot = axeSlot;
            lastActionTime = now;
        } else {
            client.interactionManager.attackEntity(client.player, target);
            lastActionTime = now;
        }
    }

    private int findAxeInHotbar(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getStack(i).getItem() instanceof AxeItem) return i;
        }
        return -1;
    }
}