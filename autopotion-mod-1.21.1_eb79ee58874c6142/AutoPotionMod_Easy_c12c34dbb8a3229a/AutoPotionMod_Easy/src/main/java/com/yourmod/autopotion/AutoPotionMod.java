package com.yourmod.autopotion;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import org.lwjgl.glfw.GLFW;

public class AutoPotionMod implements ModInitializer {
    private static KeyBinding toggleKey;
    private static boolean modEnabled = false;
    private static boolean potionUsed = false;
    private static int cooldownTicks = 0;
    private static final int COOLDOWN_TIME = 20;

    @Override
    public void onInitialize() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.autopotion.toggle",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_P,
            "category.autopotion"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (toggleKey.wasPressed()) {
                modEnabled = !modEnabled;
                if (client.player != null) {
                    String status = modEnabled ? "§aENABLED" : "§cDISABLED";
                    client.player.sendMessage(Text.literal("§6AutoPotion §7» " + status), true);
                    client.player.playSound(SoundEvents.BLOCK_NOTE_BLOCK_PLING, 1.0F, modEnabled ? 2.0F : 0.5F);
                }
            }

            if (modEnabled && client.player != null && !client.player.isDead()) {
                if (cooldownTicks > 0) {
                    cooldownTicks--;
                    return;
                }

                float health = client.player.getHealth();
                float maxHealth = client.player.getMaxHealth();
                float healthPercentage = (health / maxHealth) * 100;

                if (healthPercentage <= 35 && !potionUsed) {
                    useHealthPotion(client);
                } else if (healthPercentage > 50) {
                    potionUsed = false;
                }
            }
        });
    }

    private void useHealthPotion(MinecraftClient client) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getStack(i);

            if (stack.getItem() == Items.SPLASH_POTION || stack.getItem() == Items.POTION) {
                if (isHealthPotion(stack)) {
                    int previousSlot = client.player.getInventory().selectedSlot;
                    client.player.getInventory().selectedSlot = i;

                    if (stack.getItem() == Items.SPLASH_POTION) {
                        client.interactionManager.interactItem(client.player, Hand.MAIN_HAND);
                    } else {
                        client.options.useKey.setPressed(true);
                        ClientTickEvents.END_CLIENT_TICK.register(c -> {
                            if (c.player.isUsingItem()) {
                                c.options.useKey.setPressed(false);
                            }
                        });
                    }

                    client.player.getInventory().selectedSlot = previousSlot;
                    client.player.sendMessage(Text.literal("§6AutoPotion §7» §aUsed health potion!"), true);
                    potionUsed = true;
                    cooldownTicks = COOLDOWN_TIME;
                    return;
                }
            }
        }

        client.player.sendMessage(Text.literal("§6AutoPotion §7» §cNo health potions in hotbar!"), true);
        potionUsed = true;
    }

    private boolean isHealthPotion(ItemStack stack) {
        if (stack.hasNbt() && stack.getNbt().contains("CustomPotionEffects")) {
            var effects = stack.getNbt().getList("CustomPotionEffects", 10);
            for (int i = 0; i < effects.size(); i++) {
                var effect = effects.getCompound(i);
                if (effect.getString("Id").equals("minecraft:instant_health") || 
                    effect.getInt("Id") == 6) {
                    return true;
                }
            }
        }

        String potionName = stack.getName().getString().toLowerCase();
        return potionName.contains("healing") || potionName.contains("health");
    }
}