package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import dev.moonaticks.placeableItemsReworked.utils.RayTraceData;

public class PlaceManager {

    private final DataManager dataManager;
    private final RotateManager rotateManager;

    public PlaceManager(DataManager dataManager, RotateManager rotateManager) {
        this.dataManager = dataManager;
        this.rotateManager = rotateManager;
    }

    /**
     * Рассчитывает позицию спавна
     */
    private Location calculateSpawnLocation(RayTraceData rayTraceData, PlaceableData data) {

        World world = rayTraceData.getHitLocation().getWorld();
        if (world == null) return null;

        if (data.isCentered()) {

            Block targetBlock = rayTraceData
                    .getHitBlock()
                    .getRelative(rayTraceData.getHitFace());

            return targetBlock.getLocation().add(0.5, 0.5, 0.5);
        }

        Location baseLocation = rayTraceData.getHitPosition().toLocation(world);
        Vector normal = rayTraceData.getHitFace().getDirection();

        return baseLocation.clone().add(normal.multiply(data.getOffset()));
    }

    ItemDisplay createDisplay(
            ItemStack itemStack,
            RayTraceData rayTraceData,
            PlaceableData data,
            Player player
    ) {

        ItemStack displayItem = itemStack.clone();
        displayItem.setAmount(1);

        Location spawnLocation = calculateSpawnLocation(rayTraceData, data);
        if (spawnLocation == null) return null;

        World world = spawnLocation.getWorld();

        return world.spawn(spawnLocation, ItemDisplay.class, itemDisplay -> {

            itemDisplay.setItemStack(displayItem);

            Transformation transform = new Transformation(
                    new Vector3f(),
                    new Quaternionf(),
                    new Vector3f(data.getScale(), data.getScale(), data.getScale()),
                    new Quaternionf()
            );
            itemDisplay.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.NONE);
            itemDisplay.setTransformation(transform);

            dataManager.saveFaceToDisplay(itemDisplay, rayTraceData.getHitFace());
        });
    }
    /**
     * Поставить дисплей
     * @param itemStack
     * @param rayTraceData
     * @param data
     * @param player
     * @return
     */
    public ItemDisplay placeDisplay(
        ItemStack itemStack, 
        RayTraceData rayTraceData, 
        PlaceableData data, 
        Player player) {

    ItemDisplay itemDisplay = createDisplay(itemStack, rayTraceData, data, player);
    if (data.isFixed()) {
            rotateManager.setSnappedRotation(
                    rayTraceData.getHitFace(),
                    itemDisplay,
                    player
            );
        } else {
            rotateManager.setFreeRotation(
                    rayTraceData.getHitFace(),
                    itemDisplay,
                    player
            );
        }
        return itemDisplay;
    }
}