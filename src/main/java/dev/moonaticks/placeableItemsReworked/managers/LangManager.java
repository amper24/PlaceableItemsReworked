package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LangManager {
    
    private final JavaPlugin plugin;
    private final ConfigManager configManager;
    private FileConfiguration messages;
    private String currentLang;
    
    // Кэш для сообщений с префиксом (для оптимизации)
    private final Map<String, Component> componentCache = new HashMap<>();
    private final Map<String, Component> prefixedComponentCache = new HashMap<>();
    
    public LangManager(JavaPlugin plugin, ConfigManager configManager) {
        this.plugin = plugin;
        this.configManager = configManager;
        this.currentLang = "en"; // Временно ставим en по умолчанию
        
        // Загружаем язык ПОСЛЕ инициализации
        loadLanguage();
    }
    
    /**
     * Загружает язык (теперь без параметра)
     */
    public void loadLanguage() {
        // Получаем язык из ConfigManager (теперь он уже должен быть инициализирован)
        String lang = configManager.getLanguage();
        if (lang == null || lang.isEmpty()) {
            lang = "en";
        }
        
        this.currentLang = lang;
        clearCache();
        
        File langFile = new File(plugin.getDataFolder() + "/lang/" + lang, "messages.yml");
        
        // Сначала проверяем в папке плагина
        if (langFile.exists()) {
            messages = YamlConfiguration.loadConfiguration(langFile);
        } else {
            // Если нет - копируем из ресурсов
            try {
                // Проверяем существует ли ресурс перед сохранением
                if (plugin.getResource("lang/" + lang + "/messages.yml") != null) {
                    plugin.saveResource("lang/" + lang + "/messages.yml", false);
                    messages = YamlConfiguration.loadConfiguration(langFile);
                } else {
                    plugin.getLogger().warning("Language resource for '" + lang + "' not found, falling back to en");
                    fallbackToEnglish();
                    return;
                }
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Language '" + lang + "' not found in resources, falling back to en");
                fallbackToEnglish();
                return;
            }
        }
        
        if (messages.getKeys(true).isEmpty()) {
            plugin.getLogger().warning("Language file for '" + lang + "' is empty or invalid!");
            fallbackToEnglish();
            return;
        }
        
        // Загружаем стандартные сообщения если их нет
        ensureDefaultMessages();
    }
    
    /**
     * Падает на английский как запасной вариант
     */
    private void fallbackToEnglish() {
        this.currentLang = "en";
        File langFile = new File(plugin.getDataFolder() + "/lang/en", "messages.yml");
        
        if (!langFile.exists()) {
            plugin.saveResource("lang/en/messages.yml", false);
        }
        
        messages = YamlConfiguration.loadConfiguration(langFile);
        ensureDefaultMessages();
    }
    
    /**
     * Очищает кэш компонентов
     */
    private void clearCache() {
        componentCache.clear();
        prefixedComponentCache.clear();
    }
    
    /**
     * Проверяет наличие стандартных сообщений
     */
    private void ensureDefaultMessages() {
        // Проверяем общие сообщения
        if (!messages.contains("general.no-permission")) {
            messages.set("general.no-permission", "<red>You don't have permission to use this command!");
        }
        if (!messages.contains("general.player-only")) {
            messages.set("general.player-only", "<red>Only players can use this command!");
        }
        if (!messages.contains("general.no-item")) {
            messages.set("general.no-item", "<red>You need to hold an item in your hand!");
        }
        
        // Проверяем сообщения для /tg
        if (!messages.contains("tg.added")) {
            messages.set("tg.added", "<green>Item can now be placed! Use /tg again to remove.");
        }
        if (!messages.contains("tg.removed")) {
            messages.set("tg.removed", "<green>Placement properties removed from item!");
        }
        if (!messages.contains("tg.cannot-toggle")) {
            messages.set("tg.cannot-toggle", "<red>This item cannot be toggled with /tg!");
        }
        
        // Проверяем сообщения для /cplace
        if (!messages.contains("cplace.preset-applied")) {
            messages.set("cplace.preset-applied", "<green>Preset '<gold>%s<green>' applied to your item!");
        }
        if (!messages.contains("cplace.preset-not-found")) {
            messages.set("cplace.preset-not-found", "<red>Preset '<gold>%s<red>' not found!");
        }
        if (!messages.contains("cplace.preset-list-empty")) {
            messages.set("cplace.preset-list-empty", "<red>No presets available");
        }
        if (!messages.contains("cplace.reload")) {
            messages.set("cplace.reload", "<green>Configuration reloaded!");
        }
        
        // Префикс по умолчанию
        if (!messages.contains("prefix")) {
            messages.set("prefix", "<dark_gray>[<gold>Placeable<dark_gray>] <reset>");
        }
    }
    
    /**
     * Перезагружает текущий язык
     */
    public void reload() {
        loadLanguage();
    }
    
    /**
     * Устанавливает язык
     */
    public void setLocale(String lang) {
        if (!lang.equals(currentLang)) {
            this.currentLang = lang;
            loadLanguage();
            configManager.setLanguage(lang);
        }
    }
    
    /**
     * Получает сырое сообщение по ключу
     */
    private String getRawMessage(String path) {
        String message = messages.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Missing message key: " + path + " for language: " + currentLang);
            return "<red>Missing message: " + path;
        }
        return message;
    }
    
    /**
     * Получает компонент сообщения (с кэшированием)
     */
    public Component getComponent(String path) {
        return componentCache.computeIfAbsent(path, p -> 
            MiniMessage.miniMessage().deserialize(getRawMessage(p))
        );
    }
    
    /**
     * Получает компонент сообщения с параметрами (без кэширования из-за параметров)
     */
    public Component getComponent(String path, Object... args) {
        String msg = getRawMessage(path);
        try {
            msg = String.format(msg, args);
        } catch (Exception e) {
            plugin.getLogger().warning("Error formatting message '" + path + "': " + e.getMessage());
        }
        return MiniMessage.miniMessage().deserialize(msg);
    }
    
    /**
     * Получает компонент с префиксом (с кэшированием для версий без параметров)
     */
    public Component getComponentWithPrefix(String path) {
        return prefixedComponentCache.computeIfAbsent(path, p -> {
            String prefix = messages.getString("prefix", "<dark_gray>[<gold>Placeable<dark_gray>] <reset>");
            return MiniMessage.miniMessage().deserialize(prefix + getRawMessage(p));
        });
    }
    
    /**
     * Получает компонент с префиксом и параметрами (без кэширования)
     */
    public Component getComponentWithPrefix(String path, Object... args) {
        String prefix = messages.getString("prefix", "<dark_gray>[<gold>Placeable<dark_gray>] <reset>");
        String msg = getRawMessage(path);
        try {
            msg = String.format(msg, args);
        } catch (Exception e) {
            plugin.getLogger().warning("Error formatting message '" + path + "': " + e.getMessage());
        }
        return MiniMessage.miniMessage().deserialize(prefix + msg);
    }
    
    /**
     * Отправляет сообщение игроку
     */
    public void sendMessage(Player player, String path) {
        player.sendMessage(getComponent(path));
    }
    
    /**
     * Отправляет сообщение с параметрами
     */
    public void sendMessage(Player player, String path, Object... args) {
        player.sendMessage(getComponent(path, args));
    }
    
    /**
     * Отправляет сообщение с префиксом
     */
    public void sendMessageWithPrefix(Player player, String path) {
        player.sendMessage(getComponentWithPrefix(path));
    }
    
    /**
     * Отправляет сообщение с префиксом и параметрами
     */
    public void sendMessageWithPrefix(Player player, String path, Object... args) {
        player.sendMessage(getComponentWithPrefix(path, args));
    }
    
    /**
     * Проверяет, загружен ли язык
     */
    public boolean isLoaded() {
        return messages != null && !messages.getKeys(true).isEmpty();
    }
    
    /**
     * Получает текущий язык
     */
    public String getCurrentLang() {
        return currentLang;
    }
    
    /**
     * Получает сырой messages конфиг
     */
    public FileConfiguration getMessages() {
        return messages;
    }
}