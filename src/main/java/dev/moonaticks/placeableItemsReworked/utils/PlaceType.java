package dev.moonaticks.placeableItemsReworked.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.block.BlockFace;

public enum PlaceType {
    FLOOR(true, false, false),
    CEILING(false, true, false),
    WALL(false, false, true),
    ALL(true, true, true),
    FLOOR_AND_WALL(true, false, true),
    CEILING_AND_WALL(false, true, true);
    
    private final boolean onFloor;
    private final boolean onCeiling;
    private final boolean onWalls;
    
    PlaceType(boolean onFloor, boolean onCeiling, boolean onWalls) {
        this.onFloor = onFloor;
        this.onCeiling = onCeiling;
        this.onWalls = onWalls;
    }
    
    // Геттеры
    public boolean canPlaceOnFloor() {
        return onFloor;
    }
    
    public boolean canPlaceOnCeiling() {
        return onCeiling;
    }
    
    public boolean canPlaceOnWalls() {
        return onWalls;
    }
    
    // Для обратной совместимости
    public boolean isOnFloor() {
        return onFloor;
    }
    
    public boolean isOnCellar() {
        return onCeiling;
    }
    
    public boolean isOnWalls() {
        return onWalls;
    }
    
    // =============== МЕТОДЫ С BlockFace (БЕЗ SurfaceType) ===============
    
    /**
     * Проверяет, можно ли разместить на данной стороне блока
     */
    public boolean canPlaceOn(BlockFace face) {
        return switch (face) {
            case UP -> onFloor;
            case DOWN -> onCeiling;
            case NORTH, SOUTH, EAST, WEST -> onWalls;
            default -> false;
        };
    }
    
    /**
     * Получает поверхность по BlockFace в виде строки
     */
    public String getSurfaceType(BlockFace face) {
        return switch (face) {
            case UP -> "FLOOR";
            case DOWN -> "CEILING";
            case NORTH, SOUTH, EAST, WEST -> "WALL";
            default -> "UNKNOWN";
        };
    }
    
    /**
     * Получение всех BlockFace, на которые можно разместить
     */
    public List<BlockFace> getAllowedFaces() {
        List<BlockFace> faces = new ArrayList<>();
        if (onFloor) faces.add(BlockFace.UP);
        if (onCeiling) faces.add(BlockFace.DOWN);
        if (onWalls) {
            faces.addAll(Arrays.asList(
                BlockFace.NORTH,
                BlockFace.SOUTH,
                BlockFace.EAST,
                BlockFace.WEST
            ));
        }
        return faces;
    }
    
    /**
     * Получает противоположную сторону для размещения
     * (например, если кликнули по стене, предмет появится с противоположной стороны блока)
     */
    public BlockFace getPlacementFace(BlockFace clickedFace) {
        if (!canPlaceOn(clickedFace)) return null;
        
        return switch (clickedFace) {
            case UP -> BlockFace.UP;      // На полу - размещаем на полу
            case DOWN -> BlockFace.DOWN;  // На потолке - размещаем на потолке
            case NORTH, SOUTH, EAST, WEST -> clickedFace.getOppositeFace(); // На стене - размещаем на противоположной стороне
            default -> null;
        };
    }
    
    /**
     * Проверяет, является ли BlockFace валидным для этого типа размещения
     */
    public boolean isValidFace(BlockFace face) {
        return getAllowedFaces().contains(face);
    }
    
    // =============== СТАТИЧЕСКИЕ МЕТОДЫ ===============
    
    // Статический метод для поиска по комбинации
    public static PlaceType fromBooleans(boolean onFloor, boolean onCeiling, boolean onWalls) {
        for (PlaceType type : values()) {
            if (type.onFloor == onFloor && 
                type.onCeiling == onCeiling && 
                type.onWalls == onWalls) {
                return type;
            }
        }
        return ALL;
    }
    
    // Статический метод для поиска по названию (игнорируя регистр)
    public static Optional<PlaceType> fromString(String name) {
        try {
            return Optional.of(PlaceType.valueOf(name.toUpperCase()));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
    
    /**
     * Получает все типы, которые можно разместить на конкретной стороне блока
     */
    public static List<PlaceType> getTypesForFace(BlockFace face) {
        return Arrays.stream(values())
                .filter(type -> type.canPlaceOn(face))
                .collect(Collectors.toList());
    }
    
    // Получение читаемого имени
    public String getDisplayName() {
        return name().replace('_', ' ').toLowerCase();
    }
    
    @Override
    public String toString() {
        return String.format("PlaceType{%s: floor=%s, ceiling=%s, walls=%s}", 
            name(), onFloor, onCeiling, onWalls);
    }
}