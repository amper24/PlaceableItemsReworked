package dev.moonaticks.placeableItemsReworked.listeners;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import dev.moonaticks.placeableItemsReworked.PlaceableItemsReworked;
import dev.moonaticks.placeableItemsReworked.events.PlaceableClickEvent;
import dev.moonaticks.placeableItemsReworked.events.PlaceableHoverEvent;
import dev.moonaticks.placeableItemsReworked.events.PlaceableClickEvent.ClickType;
import dev.moonaticks.placeableItemsReworked.events.PlaceableHoverEvent.HoverType;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.managers.RotateManager;
import dev.moonaticks.placeableItemsReworked.utils.DisplayBreak;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import net.kyori.adventure.text.Component;

public class DisplaysClickListener implements Listener {
    PlaceableItemsReworked plugin;
    RotateManager rotateManager;
    DataManager dataManager;
    public DisplaysClickListener(PlaceableItemsReworked plugin, RotateManager rotateManager, DataManager dataManager) {
        this.plugin = plugin;
        this.rotateManager = rotateManager;
        this.dataManager = dataManager;
    }
    private final Map<Player, BreakPair> breakMap = new ConcurrentHashMap<>();
    record BreakPair(UUID id, int count, int taskId) {}

    @EventHandler(priority = EventPriority.NORMAL)
    void onPlaceableClick(PlaceableClickEvent event) {

        ClickType type = event.getClickType();
        PlaceableData data = event.getData();
        ItemDisplay display = event.getDisplay();
        Player player = event.getPlayer();

        if(type.equals(ClickType.LEFT)) {
            BreakDisplayHandler(player, data, display);
        }
        if(type.equals(ClickType.RIGHT)) {
            rotateClickHandler(player, data, display);
            data.getSounds().playInteractSound(display.getLocation());
        }
    }
    @EventHandler
    void PlaceableHoverEvent(PlaceableHoverEvent event) {
        if(event.isHoverChange()) {
            HoverType type = event.getHoverType();
            if(type == HoverType.HOVER_START) {
                event.getPlayer().sendMessage(Component.text("Игрок навелся на дислдей: " + event.getDisplay().getName()));
            }
            else if(type == HoverType.HOVER_END) {
                event.getPlayer().sendMessage(Component.text("Игрок перестал наводиться на дислдей: " + event.getDisplay().getName()));
            }
        }
    }

    void BreakDisplayHandler(Player player, PlaceableData data, ItemDisplay display) {
        UUID currentId = display.getUniqueId();
        BreakPair breakPair = breakMap.get(player);

        // 1. Если переключился на другой дисплей или данных нет — сбрасываем всё
        if (breakPair == null || !breakPair.id().equals(currentId)) {
            if (breakPair != null) Bukkit.getScheduler().cancelTask(breakPair.taskId()); // Отмена старого таймера
            breakPair = new BreakPair(currentId, 0, -1);
        }

        // 2. Отменяем предыдущий таймер очистки, так как игрок ударил снова
        if (breakPair.taskId() != -1) {
            Bukkit.getScheduler().cancelTask(breakPair.taskId());
        }

        // 3. Планируем новый таймер очистки (через 3 секунды бездействия)
        int newTaskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            breakMap.remove(player);
        }, 60L).getTaskId();

        // 4. Логика подсчета
        breakPair = new BreakPair(currentId, breakPair.count() + 1, newTaskId);
        if (breakPair.count() < data.getBreakCount()) {
            breakMap.put(player, breakPair);
            spawnParticles(display);
            data.getSounds().playBreakSound(display.getLocation());
        } else {
            // Сломали — очищаем всё
            Bukkit.getScheduler().cancelTask(newTaskId);
            breakMap.remove(player);
            spawnParticles(display);
            data.getSounds().playBreakSound(display.getLocation());
            DisplayBreak.BreakDisplay(display);
        }
    }
    
    void spawnParticles(ItemDisplay display) {
        // 1. Достаем предмет из дисплея
        ItemStack item = display.getItemStack();
        if (item == null || item.getType().isAir()) return;

        // 2. Берем локацию (центр сущности)
        Location loc = display.getLocation();

        // 3. Спавним частицы на основе материала предмета
        // Particle.ITEM - идеален для предметов (дает эффект разлетающихся кусочков вещи)
        loc.getWorld().spawnParticle(
            Particle.ITEM, 
            loc, 
            12,              // количество
            0.2, 0.2, 0.2,   // разброс
            0.1,             // скорость
            item             // сам предмет (для текстуры)
        );

        // Добавим немного КРИТА для "веса" удара, как ты любишь
        //loc.getWorld().spawnParticle(Particle.CRIT, loc, 5, 0.2, 0.2, 0.2, 0.15);
    }

    void rotateClickHandler(Player player, PlaceableData data, ItemDisplay display) {
        if(dataManager.hasFace(display)) {
            if (data.isFixed()) {
                rotateManager.setSnappedRotation(
                    dataManager.getFaceFromDisplay(display),
                    display,
                    player
                );
            } else {
                rotateManager.setFreeRotation(
                    dataManager.getFaceFromDisplay(display),
                    display,
                    player
                );
            }
        }
    }
}
