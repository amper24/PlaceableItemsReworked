package dev.moonaticks.placeableItemsReworked.commands;

import dev.moonaticks.placeableItemsReworked.managers.ConfigManager;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Класс команды Toggle. 
 * Позволяет игроку включать/выключать возможность размещения предмета, который он держит в руке.
 */
public class Toggle implements CommandExecutor {
    
    private final ConfigManager configManager;
    private final DataManager dataManager;

    public Toggle(ConfigManager configManager, DataManager dataManager) {
        this.configManager = configManager;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        
        // Проверяем, что команду ввел игрок, а не консоль
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду!");
            return true;
        }

        // Получаем предмет в основной руке игрока
        ItemStack item = player.getInventory().getItemInMainHand();

        // Проверка: рука не должна быть пустой
        if (item.getType().isAir()) {
            this.configManager.getLangManager().sendMessageWithPrefix(player, "general.no-item");
            return true;
        }

        // Пытаемся получить данные о возможности размещения из метаданных предмета
        PlaceableData data = this.dataManager.getDataFromItem(item);

        // СЛУЧАЙ 1: Данных на предмете нет (он еще не помечен как размещаемый)
        if (data == null) {
            // Создаем новые данные на основе конфига по умолчанию
            PlaceableData newData = this.configManager.getDefaultTgData();
            newData.setToggle(true);
            
            // Сохраняем данные в предмет и уведомляем игрока
            this.dataManager.saveDataToItem(item, newData);
            this.configManager.getLangManager().sendMessageWithPrefix(player, "tg.added");
            return true;
        } 
        
        // СЛУЧАЙ 2: Предмет уже помечен (toggle включен) — снимаем пометку
        else if (data.isToggle()) {
            this.dataManager.removeDataFromItem(item);
            this.configManager.getLangManager().sendMessageWithPrefix(player, "tg.removed");
            return true;
        } 
        
        // СЛУЧАЙ 3: Предмет имеет данные, но его нельзя переключать (например, защищенный предмет)
        else {
            this.configManager.getLangManager().sendMessageWithPrefix(player, "tg.cannot-toggle");
            return true;
        }
    }
}
