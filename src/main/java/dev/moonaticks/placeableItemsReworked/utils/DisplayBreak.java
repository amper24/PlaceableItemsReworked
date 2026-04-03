package dev.moonaticks.placeableItemsReworked.utils;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

public class DisplayBreak {
    public static void BreakDisplay(ItemDisplay display) {
        ItemStack stack = display.getItemStack();
        display.getWorld().dropItemNaturally(display.getLocation(), stack);
        display.remove();
    }
}
