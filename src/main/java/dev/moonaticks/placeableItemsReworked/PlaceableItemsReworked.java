package dev.moonaticks.placeableItemsReworked;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

import dev.moonaticks.placeableItemsReworked.commands.CPlace;
import dev.moonaticks.placeableItemsReworked.commands.Toggle;
import dev.moonaticks.placeableItemsReworked.handlers.DisplayHitboxHandler;
import dev.moonaticks.placeableItemsReworked.handlers.InteractionHandler;
import dev.moonaticks.placeableItemsReworked.listeners.BlockListener;
import dev.moonaticks.placeableItemsReworked.listeners.ClickListener;
import dev.moonaticks.placeableItemsReworked.listeners.DisplaysClickListener;
import dev.moonaticks.placeableItemsReworked.managers.ConfigManager;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.managers.PlaceManager;
import dev.moonaticks.placeableItemsReworked.managers.RotateManager;
import dev.moonaticks.placeableItemsReworked.utils.RayCastHandler;

public final class PlaceableItemsReworked extends JavaPlugin {

    private static PlaceableItemsReworked instance;
    private ConfigManager configManager;
    private DataManager dataManager;
    private PlaceManager placeManager;
    private RayCastHandler rayCastHandler;
    private DisplayHitboxHandler displayHitboxHandler;
    private RotateManager rotateManager;

    private InteractionHandler interactionHandler;
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация менеджеров
        //Работа с данными
        this.configManager = new ConfigManager(this);
        this.dataManager = new DataManager(new NamespacedKey(this, "placeable"));
        //Работа с установкой и вращением
        this.rotateManager = new RotateManager();
        this.placeManager = new PlaceManager(dataManager, rotateManager);
        //Работа с интеракциями с игроком
        this.rayCastHandler = new RayCastHandler();
        this.displayHitboxHandler = new DisplayHitboxHandler(dataManager);
        this.interactionHandler = new InteractionHandler(instance, dataManager, displayHitboxHandler);

        //Работа с инитом команд
        CPlace cplace = new CPlace(configManager, dataManager, displayHitboxHandler);
        getCommand("cplace").setExecutor(cplace);
        getCommand("cplace").setTabCompleter(cplace);
        Toggle toggle = new Toggle(configManager, dataManager);
        getCommand("tg").setExecutor(toggle);
        
        // Регистрация слушателей
        getServer().getPluginManager().registerEvents(new ClickListener(rayCastHandler, dataManager, placeManager, displayHitboxHandler), instance);
        getServer().getPluginManager().registerEvents(new DisplaysClickListener(instance, rotateManager, dataManager), instance);
        getServer().getPluginManager().registerEvents(new BlockListener(dataManager, instance), instance);
        getServer().getPluginManager().registerEvents(interactionHandler, instance);
        
        
        getLogger().info("PlaceableItemsReworked включен!");
        if(configManager.getAsciiArst()) {
            printSkinAscii();
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("PlaceableItemsReworked выключен!");
    }
    
    public static PlaceableItemsReworked getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public DataManager getDataManager() {
        return dataManager;
    }
    public static void printSkinAscii() {
        String art = """
                                                                                            
                                                                                            
                                                                                            
                                                                                            
                                                                                            
                                                                                            
                                                                                            
                                                ░▒▒░░                                        
                                            ▒▓▓▓▓▓▓▓▓▓▓▓▒░  ░▒▒▓▓▓▓▒▒▒▒░                      
                                ░▒▒▒░▒▒▓▓▓▓▒▓▓▓▒▒▒░░▒░░░▒▒▒▓▓▓▓▓▓▓▓▓▓▒▒░                      
                            ░▒███████▓▒▒▒▓▒░░░░░░░▒▒░░░░░░▒░░▒▓▓▓▒▒▓▒░                      
                            ░██▓▓▓█▓▒▒░░░░░░░░░░░░▒▓██▓▓▒░▒░░░░░░░░▒▒▒▒▒░░                  
                            ░██░░░░░░▒░░░░░░░░░░░░▒▓▓▓▓▓█▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒▒                  
                            ░██░░░░░░░▒▒▒▒▒▒░░░░░░░░░░░░▓▒░░░░░░░░▒▒░░░░▒▒                  
                            ░██░░░░░░░▒▒▒░░░▒▒▒▒▒░░░░░░░▓▒░▒▒░░░░░░░░░░░▒▒                  
                            ░▓██▓░░░░░░░░░░░░░░░░░░░░░░░▓▒░░░░░░░░░░░░░░▒▒                  
                                ▒▒░▒▒░░░░░░░░░░░░░░░░░░░░░▒▓▒░░░░░░░░░░░░░░▒░                  
                            ░▓██▓▓░░░░░░░░░░░░░░░░░░░░░▒▒░░░░░░░░░░░░░░░▒▒                  
                            ░█▓░░▒░░░░░░░░░░░░░░░░░░░░░▒▒░░░░░░░░░░░░░░░▒░                  
                            ░█▓░░░░░░░░░░░░▒▓▓▒░░░░░░░░▒▓░░░░░░░░░░░░░▒▒▒░                  
                            ░█▓░░░░░░░░▒▒▓▓▓▒▒▓▓▒░░░░░░░▓▒░░░░░░░░░░░▒▒░                    
                            ░█▓░░░░░░░▒▓▓▓▒░░░▒▓▓▓▒░░░░░▓▒░░░░ ░░░░░░▒▒                     
                            ░█▓░░▒▒▒▒▒▓█▓▓░░░░░░░▒▒░░░░░▓▒░░░░░░░░░░░▒▒                     
                            ░██ ░▓▒█▓▒░▒▓█▒░░░▒▓█▓▒░░░░░▓▒░░░░░░░░░░░▒▒                     
                            ░█████▒▓████▓█▒░░░▒█▒░░▒░░░░▓▒░░░░░░░░░░░▒▒                     
                            ░▒████░░▒▓▓▓▓▓░░░░▒▓▓▓███▓░░▓▒░░░░░░░░░░░▒▒                     
                                ▒██░░▒░░▒▒▒░░░░░▒▒▒▒▒▓▓░░▓▒░░░░░░░░░░▒▒░                     
                                ░██████▒░░░░░░░░░▒░░░▒▓▓▓▓▒░▒▓▓▓▓▓▒▒▒▒░░                     
                                ░▓██████▒▓█▓▓▓████▓░░▒▒▓█▓█████▓▒▒▓▓░░                       
                                    ░▒░░▓█▓███████████░░░▓█▓█████▓▓▓▓▒░                        
                                    ▒██▓░██▒▓▒▒▓██▓░▓█▓░▒▓█▒█▓░▒██▓▓▒░▒░                       
                                ▒█████░█▓░▓█▓▒▓▒░▓█▓▓██████▓▒██▓▓▓██▓░                      
                                ░███▓███▓███▒███▓▓█▒████▓▒▓██▒█████▓██▓░                     
                                ▒███▓░██░▒█████░▒█▓███▓░░░▒██▒████▒░▒██▒                     
                                ▒▒▓█████████▓▒▓▒░▒█▒█▓▒▓▓▒▒▓██▓░░▓█▓▓█▓█▓                     
                    ░░▒▒░░░░░░▒▒░░▒███▒░▓████████▓██████▓▒▒██████████▓▒░                    
                    ░▒▒▒▒░░░░░░▒▒░░░▒░▓█▓▒▒▒▓███▓▓▓░▒▒▒▒░░▒██████▒▒▒▓▓▓▓▒                   
                    ░▒░░░░░░░░░░░░▒▒▓░▓▓█▓▒▓▒░░▓███▓▓███▓▒▒▒▒▒▓██████▓▓██▒                  
                    ░▒░░░░░░░    ░░░▓░▓▒████▓░░███▒░░░░░▒░▒░░▓██████▒░░▒█▓                  
                    ░▒░░░░░░░      ░▓░▓░▓▒▒██▒▒██░░░░░░░░▒░▒░▓██░░▒▓▓░▒▓█▓                  
                    ░▒░░░░░░░░░░░░░░▓▓▓▒▓▒▓██████░░░░░░▒░░░░░▒██▒███████▓░                  
                    ░▒▒▒▒▒░░░░░░░░░░▒▓▒▒████████▒░░░░░░▒░▒▒▒░░████████▓▒░                   
                    ░░▒▒▒░░░░░░░     ░░░░     ░░░░░░░░▒▒░░░░░ ▒███▒                         
                                                ░░▒▒▒▒▒▒▒░░                                   
                                                ░░░░░░░                                     
                                                                                            
                                                                                            
                                                                                            
                                                                                            
    """;

        for (String line : art.split("\n")) {
            org.bukkit.Bukkit.getLogger().info(line);
        }
    }
}
