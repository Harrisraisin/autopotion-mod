package com.autopotion;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.PotionItem;
import net.minecraft.item.SwordItem;
import net.minecraft.potion.PotionUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class AutoPotionMod implements ClientModInitializer {
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static boolean enabled = false;
    private static int cooldownTimer = 0;
    private static int previousSlot = -1;
    private static KeyBinding toggleKey;

    // Configuration
    private static final float HEALTH_THRESHOLD = 10.0f; // 5 hearts (each heart = 2 health)
    private static final int COOLDOWN_TICKS = 20; // 1 second cooldown between uses

    @Override
    public void onInitializeClient() {
        // Register toggle keybinding (default: R)
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autopotion.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "category.autopotion"
        ));

        // Register tick event
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.world == null) {
                return;
            }

            // Check for toggle key press
            if (toggleKey.wasPressed()) {
                enabled = !enabled;
                String status = enabled ? "§aEnabled" : "§cDisabled";
                client.player.sendMessage(Text.literal("§6[AutoPotion] " + status), true);
            }

            if (!enabled) {
                return;
            }

            // Update cooldown
            if (cooldownTimer > 0) {
                cooldownTimer--;
                return;
            }

            // Check health
            float currentHealth = client.player.getHealth();
            if (currentHealth >= HEALTH_THRESHOLD) {
                return;
            }

            // Find and use potion
            int potionSlot = findHealthPotion();
            if (potionSlot != -1) {
                usePotion(potionSlot);
            }
        });
    }

    private static int findHealthPotion() {
        if (mc.player == null) return -1;

        // Search hotbar for health potions (slots 0-8)
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);

            if (stack.isEmpty()) continue;

            // Check for splash potions
            if (stack.getItem() == Items.SPLASH_POTION) {
                List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
                for (StatusEffectInstance effect : effects) {
                    if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                        return i;
                    }
                }
            }

            // Check for regular potions
            if (stack.getItem() == Items.POTION) {
                List<StatusEffectInstance> effects = PotionUtil.getPotionEffects(stack);
                for (StatusEffectInstance effect : effects) {
                    if (effect.getEffectType() == StatusEffects.INSTANT_HEALTH) {
                        return i;
                    }
                }
            }
        }

        return -1;
    }

    private static void usePotion(int slot) {
        if (mc.player == null || mc.interactionManager == null) return;

        // Save current slot if it's a sword
        int currentSlot = mc.player.getInventory().selectedSlot;
        ItemStack currentItem = mc.player.getInventory().getStack(currentSlot);

        if (currentItem.getItem() instanceof SwordItem) {
            previousSlot = currentSlot;
        }

        // Switch to potion slot
        mc.player.getInventory().selectedSlot = slot;

        ItemStack potionStack = mc.player.getInventory().getStack(slot);

        // Use the potion
        if (potionStack.getItem() == Items.SPLASH_POTION) {
            // Look down for splash potion
            float originalPitch = mc.player.getPitch();
            mc.player.setPitch(90.0f);
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            mc.player.setPitch(originalPitch);
        } else {
            // Use regular potion
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        }

        // Schedule return to sword
        if (previousSlot != -1) {
            ClientTickEvents.END_CLIENT_TICK.register(new SwordSwitchHandler(previousSlot));
        }

        // Set cooldown
        cooldownTimer = COOLDOWN_TICKS;
    }

    // Handler to switch back to sword after potion use
    private static class SwordSwitchHandler implements ClientTickEvents.EndTick {
        private final int swordSlot;
        private int ticksWaited = 0;

        public SwordSwitchHandler(int slot) {
            this.swordSlot = slot;
        }

        @Override
        public void onEndTick(MinecraftClient client) {
            ticksWaited++;

            // Wait 5 ticks before switching back
            if (ticksWaited >= 5) {
                if (client.player != null) {
                    client.player.getInventory().selectedSlot = swordSlot;
                }
                // Unregister this handler
                ClientTickEvents.END_CLIENT_TICK.unregister(this);
            }
        }
    }
}
