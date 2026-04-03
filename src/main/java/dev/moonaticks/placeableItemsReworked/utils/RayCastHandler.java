package dev.moonaticks.placeableItemsReworked.utils;

import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.bukkit.util.RayTraceResult;

public class RayCastHandler {
    
    /**
     * Полный рейтрейс с возвратом RayTraceData
     */
    public RayTraceData RayCast(Player player, double distance) {
        Location eyeLoc = player.getEyeLocation();
        Vector direction = eyeLoc.getDirection();
        
        RayTraceResult result = player.getWorld().rayTraceBlocks(
            eyeLoc, direction, distance, 
            FluidCollisionMode.NEVER, true
        );
        
        if (result != null && result.getHitBlock() != null) {
            return new RayTraceData(
                result.getHitPosition().toLocation(player.getWorld()),
                result.getHitBlock(),
                result.getHitBlockFace(),
                result.getHitPosition()
            );
        }
        return null;
    }
    
    /**
     * Для обратной совместимости - только позиция
     */
    public Location GetRayCastLocation(Player player, double distance) {
        RayTraceData data = RayCast(player, distance);
        return data != null ? data.getHitLocation() : null;
    }
    
    /**
     * Для обратной совместимости - только нормаль
     */
    public BlockFace GetRayCastFace(Player player, double distance) {
        RayTraceData data = RayCast(player, distance);
        return data != null ? data.getHitFace() : null;
    }
}