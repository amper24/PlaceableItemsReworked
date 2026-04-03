package dev.moonaticks.placeableItemsReworked.events;

import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;

/**
 * Событие вызывается когда игрок начинает или перестает смотреть на Placeable предмет
 */
public class PlaceableHoverEvent extends PlaceableEvent {
    private static final HandlerList handlers = new HandlerList();
    private final HoverType hoverType;
    private final ItemDisplay previousDisplay;

    public enum HoverType {
        HOVER_START,    // начал смотреть
        HOVER_END,      // перестал смотреть
        HOVER_CHANGE    // сменил предмет (сразу HOVER_END на старом + HOVER_START на новом)
    }

    public PlaceableHoverEvent(Player player, ItemDisplay display, PlaceableData data, HoverType hoverType) {
        this(player, display, data, hoverType, null);
    }

    public PlaceableHoverEvent(Player player, ItemDisplay display, PlaceableData data, HoverType hoverType, ItemDisplay previousDisplay) {
        super(player, display, data);
        this.hoverType = hoverType;
        this.previousDisplay = previousDisplay;
    }

    public HoverType getHoverType() {
        return hoverType;
    }

    public boolean isHoverStart() {
        return hoverType == HoverType.HOVER_START;
    }

    public boolean isHoverEnd() {
        return hoverType == HoverType.HOVER_END;
    }

    public boolean isHoverChange() {
        return hoverType == HoverType.HOVER_CHANGE;
    }
    public Player getPlayer() {
        return player;
    }

    /**
     * @return Предыдущий дисплей на который смотрел игрок (только для HOVER_CHANGE)
     */
    public ItemDisplay getPreviousDisplay() {
        return previousDisplay;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}