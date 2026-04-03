package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import dev.moonaticks.placeableItemsReworked.utils.HitBox;
import dev.moonaticks.placeableItemsReworked.utils.PlaceType;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import dev.moonaticks.placeableItemsReworked.utils.Sounds;

import java.io.File;
import java.util.*;

public class PresetManager {
    
    private final JavaPlugin plugin;
    private File presetsFile;
    private FileConfiguration presetsConfig;
    private final Map<String, PlaceableData> presets;
    
    public PresetManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.presets = new HashMap<>();
        loadPresetsFile();
        loadPresets();
    }
    
    /**
     * Загружает presets.yml из папки плагина или копирует из ресурсов
     */
    private void loadPresetsFile() {
        presetsFile = new File(plugin.getDataFolder(), "presets.yml");
        
        // Если файла нет - копируем из ресурсов
        if (!presetsFile.exists()) {
            plugin.saveResource("presets.yml", false);
        }
        
        presetsConfig = YamlConfiguration.loadConfiguration(presetsFile);
    }
    
    /**
     * Перезагружает пресеты из файла
     */
    public void reloadPresets() {
        presetsFile = new File(plugin.getDataFolder(), "presets.yml");
        
        // Если файла нет - копируем из ресурсов
        if (!presetsFile.exists()) {
            plugin.saveResource("presets.yml", false);
        }
        
        presetsConfig = YamlConfiguration.loadConfiguration(presetsFile);
        loadPresets();
    }
    
    /**
     * Загружает все пресеты из presets.yml в память
     */
    private void loadPresets() {
        presets.clear();
        
        ConfigurationSection presetsSection = presetsConfig.getConfigurationSection("presets");
        if (presetsSection == null) {
            plugin.getLogger().warning("No presets section found in presets.yml");
            return;
        }
        
        for (String presetName : presetsSection.getKeys(false)) {
            ConfigurationSection section = presetsSection.getConfigurationSection(presetName);
            if (section == null) continue;
            
            // Читаем параметры
            boolean fixed = section.getBoolean("fixed", false);
            boolean centered = section.getBoolean("centered", false);
            float scale = (float) section.getDouble("scale", 1.0);
            float offset = (float) section.getDouble("offset", 0.5);
            
            // Читаем HitBox
            ConfigurationSection hitboxSection = section.getConfigurationSection("hitbox");
            HitBox hitBox;
            if (hitboxSection != null) {
                float x = (float) hitboxSection.getDouble("x", 0.5);
                float y = (float) hitboxSection.getDouble("y", 0.5);
                float z = (float) hitboxSection.getDouble("z", 0.5);
                hitBox = new HitBox(x, y, z);
            } else {
                hitBox = new HitBox(0.5f, 0.5f, 0.5f);
            }
            
            // Читаем PlaceType
            String placeTypeStr = section.getString("placeType", "ALL");
            PlaceType placeType;
            try {
                placeType = PlaceType.valueOf(placeTypeStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                placeType = PlaceType.ALL;
                plugin.getLogger().warning("Invalid placeType in preset '" + presetName + "': " + placeTypeStr);
            }
            // Читаем звуки
            ConfigurationSection soundsSection = section.getConfigurationSection("sounds");
            Sounds sounds;
            
            if (soundsSection != null) {
                String placeSound = soundsSection.getString("place", "minecraft:block.wool.place");
                String breakSound = soundsSection.getString("break", "minecraft:block.wool.break");
                String interactSound = soundsSection.getString("interact", "minecraft:block.wool.hit");
                
                sounds = new Sounds(placeSound, breakSound, interactSound);
            } else {
                // Если секции звуков нет, используем значения по умолчанию
                sounds = new Sounds(
                    "minecraft:block.wool.place",
                    "minecraft:block.wool.break",
                    "minecraft:block.wool.hit"
                );
            }
            //Поломка
            int breakCout = section.getInt("breakCount", 3);
            
            // Сохраняем пресет
            presets.put(presetName.toLowerCase(), new PlaceableData(fixed, centered, hitBox, placeType, scale, offset, sounds, breakCout, false));
        }
    }
    
    /**
     * Получает пресет по имени
     */
    public PlaceableData getPreset(String name) {
        if (name == null) return null;
        PlaceableData preset = presets.get(name.toLowerCase());
        return preset != null ? new PlaceableData(preset) : null;
    }
    
    /**
     * Получает список имен всех пресетов
     */
    public Set<String> getPresetNames() {
        return new HashSet<>(presets.keySet());
    }
    
    /**
     * Проверяет существует ли пресет
     */
    public boolean hasPreset(String name) {
        return name != null && presets.containsKey(name.toLowerCase());
    }
}