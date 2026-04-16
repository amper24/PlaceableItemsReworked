package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import dev.moonaticks.placeableItemsReworked.utils.HitBox;
import dev.moonaticks.placeableItemsReworked.utils.PlaceType;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import dev.moonaticks.placeableItemsReworked.utils.Sounds;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private File configFile;
    private FileConfiguration config;
    
    private LangManager langManager;
    private PresetManager presetManager;
    
    // Настройки
    private String language;
    private PlaceableData defaultTgData;
    
    boolean asciiArt = false;
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfigFile();
        loadManagers();
        loadSettings();
    }
    
    /**
     * Загружает config.yml из папки плагина или копирует из ресурсов
     */
    private void loadConfigFile() {
        configFile = new File(plugin.getDataFolder(), "config.yml");
        
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        
        config = YamlConfiguration.loadConfiguration(configFile);
    }
    
    /**
     * Загружает остальные менеджеры
     */
    private void loadManagers() {
        this.langManager = new LangManager(plugin, this); // Теперь это безопасно
        this.presetManager = new PresetManager(plugin);
        this.langManager.reload();
    }
    
    /**
     * Загружает настройки из config.yml
     */
    private void loadSettings() {
        // Язык
        language = config.getString("language", "en");
        asciiArt = config.getBoolean("enable-ascii-art", true);
        
        // Default пресет для /tg
        boolean fixed = config.getBoolean("default-fallback-for-tg.default.fixed", false);
        boolean centered = config.getBoolean("default-fallback-for-tg.default.centered", false);
        float scale = (float) config.getDouble("default-fallback-for-tg.default.scale", 1.0);
        float offset = (float) config.getDouble("default-fallback-for-tg.default.offset", 0.5);
        int breakCount = config.getInt("default-fallback-for-tg.default.breakCount", 3);
        
        float hitboxX = (float) config.getDouble("default-fallback-for-tg.default.hitbox.x", 0.5);
        float hitboxY = (float) config.getDouble("default-fallback-for-tg.default.hitbox.y", 0.5);
        float hitboxZ = (float) config.getDouble("default-fallback-for-tg.default.hitbox.z", 0.5);
        HitBox hitBox = new HitBox(hitboxX, hitboxY, hitboxZ);
        
        String placeTypeStr = config.getString("default-fallback-for-tg.default.placeType", "ALL");
        PlaceType placeType;
        try {
            placeType = PlaceType.valueOf(placeTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            placeType = PlaceType.ALL;
            plugin.getLogger().warning("Invalid placeType in config: " + placeTypeStr);
        }
        
        // Загрузка звуков
        String placeSound = config.getString("default-fallback-for-tg.default.sounds.place", "minecraft:block.wool.place");
        String breakSound = config.getString("default-fallback-for-tg.default.sounds.break", "minecraft:block.wool.break");
        String interactSound = config.getString("default-fallback-for-tg.default.sounds.interact", "minecraft:block.wool.hit");
        
        Sounds sounds = new Sounds(placeSound, breakSound, interactSound);
        
        defaultTgData = new PlaceableData(fixed, centered, hitBox, placeType, scale, offset, sounds, breakCount, false);
    }
    
    /**
     * Перезагружает все конфиги
     */
    public void reloadAll() {
        loadConfigFile();
        langManager.reload();
        presetManager.reloadPresets();
        loadSettings();
    }
    
    /**
     * Сохраняет config.yml
     */
    public void saveConfig() {
        if (config == null || configFile == null) return;
        
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save config.yml", e);
        }
    }
    
    /**
     * Получает LangManager
     */
    public LangManager getLangManager() {
        return langManager;
    }
    
    /**
     * Получает PresetManager
     */
    public PresetManager getPresetManager() {
        return presetManager;
    }
    
    /**
     * Получает текущий язык
     */
    public String getLanguage() {
        return language;
    }
    
    /**
     * Устанавливает язык
     */
    public void setLanguage(String language) {
        this.language = language;
        config.set("language", language);
        saveConfig();
        langManager.setLocale(language);
    }
    
    /**
     * Получает дефолтные настройки для команды /tg
     */
    public PlaceableData getDefaultTgData() {
        return new PlaceableData(defaultTgData);
    }
    
    /**
     * Получает сырой конфиг
     */
    public FileConfiguration getConfig() {
        return config;
    }
    public boolean getAsciiArst() {
        return asciiArt;
    }
}