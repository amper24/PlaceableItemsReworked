package dev.moonaticks.placeableItemsReworked.listeners;

import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

import dev.moonaticks.placeableItemsReworked.PlaceableItemsReworked;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.utils.DisplayBreak;

public class BlockListener implements Listener {
    DataManager dataManager;
    PlaceableItemsReworked plugin;
    public BlockListener(DataManager dataManager, PlaceableItemsReworked plugin) {
        this.dataManager = dataManager;
        this.plugin = plugin;
    }
    @EventHandler
    void OnExplode(EntityExplodeEvent event) {
        Location center = event.getLocation();

        float yield = event.getYield();
        double radius = (yield > 0) ? yield * 1.5 : 4.0;

            // Ищем все сущности в радиусе поражения
        center.getWorld().getNearbyEntities(center, radius, radius, radius).forEach(entity -> {
            if (entity instanceof ItemDisplay display) {
                
                // Проверяем, что это НАШ дисплей (чтобы не сломать чужое)
                if (dataManager.isPlaceable(display)) {
                    
                    // Рассчитываем расстояние до эпицентра
                    double distance = display.getLocation().distance(center);
                    
                    // Если дисплей в зоне поражения
                    if (distance <= radius) {
                        DisplayBreak.BreakDisplay(display);
                    }
                }
            }
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onBlockPlace(BlockPlaceEvent event) {
        handleBlockCollision(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onEntityChangeBlock(EntityChangeBlockEvent event) {
        handleBlockCollision(event.getBlock());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPistonExtend(BlockPistonExtendEvent event) {
        // Проверяем блоки, которые ПОЯВЯТСЯ в новых позициях после сдвига
        event.getBlocks().forEach(block -> {
            // Блок сдвигается на одну позицию в направлении поршня
            Block targetBlock = block.getRelative(event.getDirection());
            handleBlockCollision(targetBlock);
        });
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    void onPistonRetract(BlockPistonRetractEvent event) {
        // При втягивании липкий поршень тоже может "наехать" блоком на мебель
        event.getBlocks().forEach(block -> {
            Block targetBlock = block.getRelative(event.getDirection());
            handleBlockCollision(targetBlock);
        });
    }
 
    private void handleBlockCollision(Block block) {
        if (block == null || !block.getType().isSolid() || !block.getType().isOccluding()) return;

        // Центр блока для поиска сущностей
        Location blockCenter = block.getLocation().add(0.5, 0.5, 0.5);
        
        // Ищем наши дисплеи в радиусе 1.5 блоков (хватает для одного куба)
        block.getWorld().getNearbyEntities(blockCenter, 1.2, 1.2, 1.2).forEach(entity -> {
            if (entity instanceof ItemDisplay display && dataManager.isPlaceable(display)) {
                
                // Если точка вставки дисплея пересекается с коллизией блока
                if (isLocationInsideBlock(display.getLocation(), block)) {
                    // Запускаем проверку с задержкой
                    scheduleBreakCheck(display, block);
                }
            }
        });
    }

    /**
     * Запланировать проверку дисплея через 3 секунды
     */
    private void scheduleBreakCheck(ItemDisplay display, Block block) {
        // Получаем уникальный идентификатор дисплея
        UUID displayId = display.getUniqueId();
        
        // Создаём задачу с задержкой
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            // Проверяем, существует ли дисплей до сих пор
            Entity entity = Bukkit.getEntity(displayId);
            if (entity instanceof ItemDisplay currentDisplay && !currentDisplay.isDead()) {
                
                // Повторно проверяем, находится ли дисплей внутри блока
                if (isLocationInsideBlock(currentDisplay.getLocation(), block)) {
                    // Если всё ещё внутри - ломаем
                    DisplayBreak.BreakDisplay(currentDisplay);
                }
            }
        }, 60L); // 60 тиков = 3 секунды (20 тиков = 1 секунда)
    }

    /**
     * Точная проверка: находится ли локация внутри физических границ блока
     */
    private boolean isLocationInsideBlock(Location loc, Block block) {
        // Проверка через BoundingBox учитывает форму блока (даже если это плита или забор)
        return block.getBoundingBox().contains(loc.getX(), loc.getY(), loc.getZ());
    }
}
