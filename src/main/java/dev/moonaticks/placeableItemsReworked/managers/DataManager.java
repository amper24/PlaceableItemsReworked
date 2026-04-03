package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
import dev.moonaticks.placeableItemsReworked.utils.HitBox;
import dev.moonaticks.placeableItemsReworked.utils.PlaceType;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import dev.moonaticks.placeableItemsReworked.utils.Sounds;

public class DataManager {
    final NamespacedKey key;
    final String strKey;
    /**
    * @param key - сюда ключ плагина для определения CompaundKey
    */
    public DataManager(NamespacedKey key) {
        this.key = key;
        strKey = key.asString();
    }
    /**
     * @param data перевести данный в NBT
     * @return Компиллер
     */
    public ReadWriteNBT dataToNBT(PlaceableData data) {
        // Основной объект
        ReadWriteNBT root = NBT.createNBTObject();
        
        // Создаем тот самый "ключевой" тег
        ReadWriteNBT nbt = root.getOrCreateCompound(strKey);

        // Теперь записываем всё в переменную 'nbt' (она внутри freePlacebleItem)
        nbt.setBoolean("fixed", data.isFixed());
        nbt.setBoolean("centered", data.isCentered());
        nbt.setFloat("scale", data.getScale());
        nbt.setFloat("offset", data.getOffset());
        nbt.setInteger("breakCount", data.getBreakCount());
        nbt.setBoolean("canToggle", data.isToggle());

        // HitBox
        ReadWriteNBT hitBoxTag = nbt.getOrCreateCompound("hitBox");
        hitBoxTag.setFloat("x", data.getHitBox().x);
        hitBoxTag.setFloat("y", data.getHitBox().y);
        hitBoxTag.setFloat("z", data.getHitBox().z);

        // PlaceType
        ReadWriteNBT placeTypeTag = nbt.getOrCreateCompound("placeTypeTag");
        PlaceType placeType = data.getPlaceType();

        placeTypeTag.setBoolean("onFloor", placeType.canPlaceOnFloor()); // или placeType.isOnFloor()
        placeTypeTag.setBoolean("onCellar", placeType.canPlaceOnCeiling()); // или placeType.isOnCellar()
        placeTypeTag.setBoolean("onWalls", placeType.canPlaceOnWalls()); // или placeType.isOnWalls()

        ReadWriteNBT soundTag = nbt.getOrCreateCompound("sounds");
        Sounds sounds = data.getSounds();

        soundTag.setString("placeSound", sounds.getPlaceSound());
        soundTag.setString("breakSound", sounds.getBreakSound());
        soundTag.setString("interactSound", sounds.getInteractSound());

        return root;
    }
    /**
     * @param root перевести NBT в дату
     * @return Декомпилер
     */
    public PlaceableData nbtToData(ReadWriteNBT root) {
        // 1. Заходим в основной тег
        ReadWriteNBT nbt = root.getCompound(strKey);
        
        // Проверка на случай, если тега нет
        if (nbt == null) return null; 

        // 2. Извлекаем простые данные
        boolean fixed = nbt.getBoolean("fixed");
        boolean centered = nbt.getBoolean("centered");
        float scale = nbt.getFloat("scale");
        float offset = nbt.getFloat("offset");
        boolean canToggle = nbt.getBoolean("canToggle");
        int breakCount = nbt.getInteger("breakCount");

        // 3. Извлекаем HitBox
        ReadWriteNBT hitBoxTag = nbt.getCompound("hitBox");
        HitBox hitBox = hitBoxTag == null ? new HitBox(0.5f, 0.5f, 0.5f) 
        : new HitBox(
            hitBoxTag.hasTag("x") ? hitBoxTag.getFloat("x") : 0.5f,
            hitBoxTag.hasTag("y") ? hitBoxTag.getFloat("y") : 0.5f,
            hitBoxTag.hasTag("z") ? hitBoxTag.getFloat("z") : 0.5f
        );
        
        // 4. Извлекаем PlaceType
        ReadWriteNBT ptTag = nbt.getCompound("placeTypeTag");
        PlaceType placeType;

        if (ptTag == null) {
            // Значение по умолчанию
            placeType = PlaceType.ALL; // или PlaceType.FLOOR, в зависимости от ваших потребностей
        } else {
            boolean onFloor = ptTag.getBoolean("onFloor");
            boolean onCellar = ptTag.getBoolean("onCellar");
            boolean onWalls = ptTag.getBoolean("onWalls");
            
            // Находим соответствующий enum по комбинации булевых значений
            placeType = PlaceType.fromBooleans(onFloor, onCellar, onWalls);
        }
        ReadWriteNBT soundTag = nbt.getCompound("sounds");
        Sounds sounds;
        if(soundTag == null) {
            sounds = new Sounds("minecraft:block.wool.place", "minecraft:block.wool.break", null);
        } else {
            String placeSound = soundTag.getString("placeSound");
            String breakSound = soundTag.getString("breakSound");
            String interactSound = soundTag.getString("interactSound");

            sounds = new Sounds(placeSound, breakSound, interactSound);
        }

        // 5. Собираем объект (конструктор зависит от вашей реализации)
        return new PlaceableData(fixed, centered, hitBox, placeType, scale, offset, sounds, breakCount, canToggle);
    } 
     /**
     * @param item эта прекдмет
     * @param data эта data
     * @return в предмет сует свой данные
     */
    public void saveDataToItem(ItemStack item, PlaceableData data) {
        if (item == null || data == null) return; // или выбросить исключение
        
        ReadWriteNBT readWriteNBT = dataToNBT(data);
        NBT.modify(item, nbt -> {
            nbt.mergeCompound(readWriteNBT);
            nbt.modifyMeta((readOnlyNbt, meta) -> {});
        });
    }
    public void removeDataFromItem(ItemStack item) {
            if (item == null || item.getType().isAir()) return;

            NBT.modify(item, nbt -> {
                // Проверяем наличие ключа перед удалением
                if (nbt.hasTag(strKey)) {
                    nbt.removeKey(strKey);
                    
                    // Синхронизируем изменения с компонентами предмета
                    nbt.modifyMeta((readOnlyNbt, meta) -> {});
                }
            });
        }
    public PlaceableData getDataFromItem(ItemStack item) {
        if (item == null || item.getType().isAir()) return null;
        ReadWriteNBT rootCopy = NBT.createNBTObject();
        NBT.get(item, nbt -> {
            rootCopy.mergeCompound(nbt);
        });
        return nbtToData(rootCopy); // Теперь внутри nbtToData вызов getCompound(strKey) сработает
    }
    public PlaceableData getDataFromDisplay(ItemDisplay display) {
        if (display == null) return null;

        // 1. Получаем предмет, который крутится в дисплее
        ItemStack displayedItem = display.getItemStack();
        
        if (displayedItem == null || displayedItem.getType().isAir()) return null;

        // 2. Используем твой проверенный метод чтения из ItemStack
        return getDataFromItem(displayedItem); 
    }
    /**
     * Проверяет, является ли предмет "устанавливаемым" (есть ли в нем нужный NBT тег)
     */
    public boolean isPlaceable(ItemStack item) {
        if (item == null || item.getType().isAir()) return false;
        return NBT.get(item, nbt -> {
            return nbt.hasTag(strKey);
        });
    }
    /**
     * Проверяет, содержит ли ItemDisplay предмет с нужным NBT тегом
     */
    public boolean isPlaceable(ItemDisplay display) {
        if (display == null) return false;
        ItemStack displayedItem = display.getItemStack();
        return isPlaceable(displayedItem);
    }
    /**
     * Сохраняет BlockFace в ItemDisplay с использованием PersistentDataContainer
     * @param display ItemDisplay entity
     * @param face BlockFace для сохранения
     */
    public void saveFaceToDisplay(ItemDisplay display, BlockFace face) {
        if (display == null || face == null) return;
        
        PersistentDataContainer container = display.getPersistentDataContainer();
        
        // Сохраняем ordinal как целое число (0-5 для 6 BlockFace)
        container.set(key, PersistentDataType.INTEGER, face.ordinal());
    }
    
    /**
     * Получает сохраненный BlockFace из ItemDisplay
     * @param display ItemDisplay entity
     * @return BlockFace или null, если не найден
     */
    public BlockFace getFaceFromDisplay(ItemDisplay display) {
        if (display == null) return null;
        
        PersistentDataContainer container = display.getPersistentDataContainer();
        
        // Проверяем наличие данных
        if (container.has(key, PersistentDataType.INTEGER)) {
            Integer ordinal = container.get(key, PersistentDataType.INTEGER);
            
            // Проверяем что ordinal в допустимом диапазоне (0-5)
            if (ordinal != null && ordinal >= 0 && ordinal < BlockFace.values().length) {
                return BlockFace.values()[ordinal];
            }
        }
        
        return null;
    }
    
    /**
     * Проверяет наличие сохраненного BlockFace
     * @param display ItemDisplay entity
     * @return true если данные существуют
     */
    public boolean hasFace(ItemDisplay display) {
        if (display == null) return false;
        return display.getPersistentDataContainer().has(key, PersistentDataType.INTEGER);
    }
    
    /**
     * Удаляет сохраненный BlockFace
     * @param display ItemDisplay entity
     */
    public void removeFace(ItemDisplay display) {
        if (display == null) return;
        display.getPersistentDataContainer().remove(key);
    }
}
