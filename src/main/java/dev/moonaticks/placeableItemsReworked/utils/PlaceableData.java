package dev.moonaticks.placeableItemsReworked.utils;

public class PlaceableData {
    /**
     * Флаг, определяющий, зафиксирована ли позиция элемента.
     * true — позиция привязана к сетке блоков (центр блока)
     * false — свободное размещение в любой точке
     */
    boolean fixed = false;
    
    /**
     * Флаг, определяющий, центрирован ли предмет относительно блока
     * true — предмет по центру блока
     * false — без центрирования
     */
    boolean centered = false;
    boolean canToggle = false;
    
    float scale = 1.0f;
    float offset = 0.5f;
    int breakCout = 3;
    
    /**
     * @return это форма хитбокса
     */
    HitBox hitBox = new HitBox(0.5f, 0.5f, 0.5f);

    Sounds sounds = new Sounds("minecraft:block.wool.place", "minecraft:block.wool.break", "minecraft:block.wool.hit");
    
    /**
     * @return На что можно будет поставить это
     */
    PlaceType placeType = PlaceType.ALL;
    
    // ============= КОНСТРУКТОРЫ =============
    
    public PlaceableData(boolean fixed, boolean centered, 
                         HitBox hitBox, PlaceType placeType, float scale, float offset, Sounds sounds, int breakCout, boolean canToggle) {
        this.fixed = fixed;
        this.centered = centered;
        this.hitBox = hitBox;
        this.placeType = placeType;
        this.scale = scale;
        this.offset = offset;
        this.breakCout = breakCout;
        this.canToggle = canToggle;
    }
    public PlaceableData(PlaceableData other) {
        this(other.fixed, other.centered, 
             other.hitBox, other.placeType, other.scale, other.offset, other.sounds, other.breakCout, other.canToggle);
    }
    
    // ============= ГЕТТЕРЫ И СЕТТЕРЫ =============
    
    public boolean isFixed() {
        return fixed;
    }

    public void setFixed(boolean fixed) {
        this.fixed = fixed;
    }
    
    public boolean isCentered() {
        return centered;
    }
    
    public void setCentered(boolean centered) {
        this.centered = centered;
    }

    public HitBox getHitBox() {
        return hitBox;
    }

    public void setHitBox(HitBox hitBox) {
        this.hitBox = hitBox;
    }

    public PlaceType getPlaceType() {
        return placeType;
    }

    public void setPlaceType(PlaceType placeType) {
        this.placeType = placeType;
    }
    
    public void setOffset(float offset) {
        this.offset = offset;
    }
    
    public void setScale(float scale) {
        this.scale = scale;
    }
    
    public float getScale() {
        return scale;
    }
    
    public float getOffset() {
        return offset;
    }

    public boolean isToggle() {
        return canToggle;
    }
    public void setToggle(boolean tg) {
        this.canToggle = tg;
    }

    public Sounds getSounds() {
        return sounds;
    }
    public void setSounds(Sounds sounds) {
        this.sounds = sounds;
    }

    public int getBreakCount() {
        return breakCout;
    }
    public void setBreakCount(int breakCount) {
        this.breakCout = breakCount;
    }
}