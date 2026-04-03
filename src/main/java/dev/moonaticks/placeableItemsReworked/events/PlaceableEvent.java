package dev.moonaticks.placeableItemsReworked.events;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;

public class PlaceableEvent extends Event {
    protected final Player player;
    protected final ItemDisplay display;
    protected final PlaceableData data;
    private static final HandlerList handlers = new HandlerList();

    public PlaceableEvent(Player player, ItemDisplay display, PlaceableData data) {
        this.player = player;
        this.display = display;
        this.data = data;
    }

    public Player getPlayer() {
        return player;
    }

    public ItemDisplay getDisplay() {
        return display;
    }

    public PlaceableData getData() {
        return data;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
