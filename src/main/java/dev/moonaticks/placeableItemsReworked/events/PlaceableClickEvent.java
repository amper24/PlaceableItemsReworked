package dev.moonaticks.placeableItemsReworked.events;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;

/**
 * Событие вызывается при клике по Placeable предмету
 */
public class PlaceableClickEvent extends PlaceableEvent {
    private static final HandlerList handlers = new HandlerList();
    private final ClickType clickType;

    public enum ClickType {
        LEFT,
        RIGHT
    }

    public PlaceableClickEvent(Player player, ItemDisplay display, PlaceableData data, ClickType clickType) {
        super(player, display, data);
        this.clickType = clickType;
    }

    public ClickType getClickType() {
        return clickType;
    }

    public boolean isLeftClick() {
        return clickType == ClickType.LEFT;
    }

    public boolean isRightClick() {
        return clickType == ClickType.RIGHT;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
    public Player getPlayer() {
        return player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}