package dev.moonaticks.placeableItemsReworked.listeners;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import dev.moonaticks.placeableItemsReworked.handlers.DisplayHitboxHandler;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.managers.PlaceManager;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import dev.moonaticks.placeableItemsReworked.utils.RayCastHandler;
import dev.moonaticks.placeableItemsReworked.utils.RayTraceData;

import org.bukkit.event.block.Action;
import org.bukkit.GameMode;
import org.bukkit.attribute.Attribute;

public class ClickListener implements Listener {
    RayCastHandler castHandler;
    DataManager dataManager;
    PlaceManager placeManager;
    DisplayHitboxHandler hitboxHandler;
    public ClickListener(RayCastHandler cast, DataManager dataManager, PlaceManager placeManager, DisplayHitboxHandler hitboxHandler) {
        castHandler = cast;
        this.dataManager = dataManager;
        this.placeManager = placeManager;
        this.hitboxHandler = hitboxHandler;
    }
    @EventHandler
    void RightClickListener(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        double distance = player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE).getValue();
        RayTraceData rayTraceData = castHandler.RayCast(player, distance);

        ItemDisplay display = hitboxHandler.getLookedDisplay(player, player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue());
        if(display != null) return;

        if(rayTraceData == null) {
            return;
        }
        if(event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }



        ItemStack stack = event.getItem();
        boolean isPassed = HandleRightClick(event, rayTraceData, stack);
        if(isPassed) consumeItem(player, stack);
        event.setCancelled(isPassed);
    }
    
    boolean HandleRightClick(PlayerInteractEvent event, RayTraceData rayTraceData, ItemStack stack) {
        // Добавить проверку на пустую руку
        if (stack == null || stack.getType().isAir()) {
            return false;
        }
        //dataManager - это наши данные
        PlaceableData data = dataManager.getDataFromItem(stack);
        //проверка на тэг
        if(data == null) {
            return false;
        }
        //проверка на фэйс
        if(!data.getPlaceType().isValidFace(rayTraceData.getHitFace())) {
            return false;
        }
        //проверка на успешный дисплей
        ItemDisplay display = placeManager.placeDisplay(stack, rayTraceData, data, event.getPlayer());
        if(display == null) {
            return false;
        }
        event.getPlayer().swingHand(event.getHand());
        data.getSounds().playPlaceSound(rayTraceData.getHitLocation());
        return true;
    }
    
    private void consumeItem(Player player, ItemStack item) {
        if (player.getGameMode() == GameMode.CREATIVE) {
            return;
        }
        int amount = item.getAmount();
        item.setAmount(amount - 1);
        
        player.updateInventory();
    }
}
