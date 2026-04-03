package dev.moonaticks.placeableItemsReworked.handlers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;
import dev.moonaticks.placeableItemsReworked.events.PlaceableClickEvent;
import dev.moonaticks.placeableItemsReworked.events.PlaceableClickEvent.ClickType;
import dev.moonaticks.placeableItemsReworked.events.PlaceableHoverEvent;
import dev.moonaticks.placeableItemsReworked.events.PlaceableHoverEvent.HoverType;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;

public class InteractionHandler implements Listener {
    private final Plugin plugin;
    private final DataManager dataManager;
    private final DisplayHitboxHandler hitboxHandler;
    
    // Храним последний дисплей на который смотрел игрок
    private final Map<UUID, ItemDisplay> playerLastHover = new HashMap<>();
    
    private BukkitTask task;

    public InteractionHandler(Plugin plugin, DataManager dataManager, 
                               DisplayHitboxHandler hitboxHandler) {
        this.plugin = plugin;
        this.dataManager = dataManager;
        this.hitboxHandler = hitboxHandler;
    }

    /**
     * Запускает асинхронную задачу проверки (каждые 3-4 тика)
     */
    public void startTracking() {
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                checkHover(player);
            }
        }, 0L, 5L); // Каждые 3 тика
    }

    public void stopTracking() {
        if (task != null && !task.isCancelled()) {
            task.cancel();
        }
        playerLastHover.clear();
    }

    /**
     * Проверяет наведение игрока
     */
    private void checkHover(Player player) {
        UUID playerId = player.getUniqueId();
        
        // Получаем дисплей на который смотрит игрок
        ItemDisplay currentDisplay = hitboxHandler.getLookedDisplay(player, 5.0);
        
        ItemDisplay lastDisplay = playerLastHover.get(playerId);
        
        // Если ничего не изменилось
        if (currentDisplay == lastDisplay) {
            return;
        }
        
        // Если раньше смотрел на что-то, а теперь нет
        if (lastDisplay != null && currentDisplay == null) {
            PlaceableData lastData = dataManager.getDataFromDisplay(lastDisplay);
            if (lastData != null) {
                PlaceableHoverEvent event = new PlaceableHoverEvent(
                    player, lastDisplay, lastData, HoverType.HOVER_END
                );
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
            }
            playerLastHover.remove(playerId);
        }
        // Если раньше не смотрел, а теперь смотрит
        else if (lastDisplay == null && currentDisplay != null) {
            PlaceableData currentData = dataManager.getDataFromDisplay(currentDisplay);
            if (currentData != null) {
                PlaceableHoverEvent event = new PlaceableHoverEvent(
                    player, currentDisplay, currentData, HoverType.HOVER_START
                );
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(event));
            }
            playerLastHover.put(playerId, currentDisplay);
        }
        // Если сменил предмет
        else if (lastDisplay != null && currentDisplay != null && !lastDisplay.equals(currentDisplay)) {
            PlaceableData lastData = dataManager.getDataFromDisplay(lastDisplay);
            PlaceableData currentData = dataManager.getDataFromDisplay(currentDisplay);
            
            if (lastData != null) {
                PlaceableHoverEvent endEvent = new PlaceableHoverEvent(
                    player, lastDisplay, lastData, HoverType.HOVER_END
                );
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(endEvent));
            }
            
            if (currentData != null) {
                PlaceableHoverEvent changeEvent = new PlaceableHoverEvent(
                    player, currentDisplay, currentData, HoverType.HOVER_CHANGE, lastDisplay
                );
                Bukkit.getScheduler().runTask(plugin, () -> Bukkit.getPluginManager().callEvent(changeEvent));
            }
            
            playerLastHover.put(playerId, currentDisplay);
        }
    }

    @EventHandler (priority = EventPriority.HIGHEST)
    void PlayerInterractionClickEvent(PlayerInteractEvent event) {

        Action action = event.getAction();
        ClickType clickType;
        // Логика для ЛЕВОЙ кнопки мыши
        if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) {
            clickType = ClickType.LEFT;
        } 
        // Логика для ПРАВОЙ кнопки мыши
        else if(action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            clickType = ClickType.RIGHT;
            // код для правой кнопки
            if(event.getHand() != EquipmentSlot.HAND) {
                return;
            }
        } else {
            return;
        }

        Player player = event.getPlayer();
        if(player == null) return;

        ItemDisplay display = hitboxHandler.getLookedDisplay(player, player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue());
        if(display == null) return;

        PlaceableData data = dataManager.getDataFromDisplay(display);
        if(data == null) return;

        event.setCancelled(true);

        if(clickType.equals(ClickType.RIGHT)) player.swingMainHand();
        PlaceableClickEvent placeableClickEvent = new PlaceableClickEvent(player, display, data, clickType);
        Bukkit.getPluginManager().callEvent(placeableClickEvent);
    }
    /**
     * Вызывается при выходе игрока
     */
    public void handlePlayerQuit(Player player) {
        UUID playerId = player.getUniqueId();
        playerLastHover.remove(playerId);
    }
}