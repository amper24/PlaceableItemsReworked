package dev.moonaticks.placeableItemsReworked.utils;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.util.Vector;

public class RayTraceData {
    private final Location hitLocation;      // Точка попадания в мире
    private final Block hitBlock;            // Блок, в который попали
    private final BlockFace hitFace;         // Сторона блока
    private final Vector hitPosition;        // Вектор точки попадания
    
    public RayTraceData(Location hitLocation, Block hitBlock, BlockFace hitFace, Vector hitPosition) {
        this.hitLocation = hitLocation;
        this.hitBlock = hitBlock;
        this.hitFace = hitFace;
        this.hitPosition = hitPosition;
    }
    
    // Геттеры
    public Location getHitLocation() {
        return hitLocation;
    }
    
    public Block getHitBlock() {
        return hitBlock;
    }
    
    public BlockFace getHitFace() {
        return hitFace;
    }
    
    public Vector getHitPosition() {
        return hitPosition;
    }
    
    /**
     * Получить место для размещения в зависимости от режима fixed
     * @param fixed true - центр блока, false - точка попадания
     * @param offset смещение по Y
     */
    public Location getPlaceLocation(boolean fixed, float offset) {
        if (fixed) {
            // Fixed режим: центр целевого блока
            return hitBlock.getRelative(hitFace)
                .getLocation()
                .add(0.5, 0.5 + offset, 0.5);
        } else {
            // Free режим: точка попадания + offset
            return hitLocation.clone().add(0, offset, 0);
        }
    }
    
    /**
     * Получить место для размещения (с учетом данных PlaceableData)
     */
    public Location getPlaceLocation(PlaceableData data) {
        return getPlaceLocation(data.isFixed(), data.getOffset());
    }
    
    /**
     * Проверить, свободно ли место для размещения в fixed режиме
     */
    public boolean isPlaceLocationFree() {
        Block targetBlock = hitBlock.getRelative(hitFace);
        return targetBlock.isEmpty();
    }
    
    @Override
    public String toString() {
        return String.format("RayTraceData{block=%s, face=%s, pos=%s}", 
            hitBlock.getType(), hitFace, hitPosition);
    }
}