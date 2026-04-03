// Исходный код декомпилирован из .class файла с помощью FernFlower decompiler (из Intellij IDEA)
package dev.moonaticks.placeableItemsReworked.commands;

// Импорты для работы с NBT (библиотека tr7zw)
import de.tr7zw.nbtapi.iface.ReadWriteNBT;
// Импорты хендлеров и менеджеров плагина
import dev.moonaticks.placeableItemsReworked.handlers.DisplayHitboxHandler;
import dev.moonaticks.placeableItemsReworked.managers.ConfigManager;
import dev.moonaticks.placeableItemsReworked.managers.DataManager;
// Импорты утилит плагина
import dev.moonaticks.placeableItemsReworked.utils.HitBox;
import dev.moonaticks.placeableItemsReworked.utils.PlaceType;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;
// Импорты для работы с буфером обмена (AWT)
import java.awt.Toolkit;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
// Импорты Bukkit/Spigot API
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Класс команды /cplace
 * Реализует CommandExecutor (обработку команды) и TabCompleter (автодополнение)
 */
public class CPlace implements CommandExecutor, TabCompleter {
    
    // Поля класса - зависимости и конфигурации
    private final ConfigManager configManager;           // Менеджер конфигурации (языки, пресеты и т.д.)
    private final DataManager dataManager;               // Менеджер данных (сохранение/загрузка данных в предметы)
    private final DisplayHitboxHandler displayHitboxHandler; // Обработчик отображения хитбоксов
    
    // Список всех доступных подкоманд для автодополнения
    private static final List<String> SUBCOMMANDS = Arrays.asList(
        "help", "info", "fixed", "centered", "scale", "offset", 
        "hitbox", "placetype", "preset", "export", "nbt", "reload", "remove", "debug"
    );
    
    // Список типов размещения (из перечисления PlaceType)
private static final List<String> PLACE_TYPES = Arrays.stream(PlaceType.values())
        .map(Enum::name)
        .toList();  // Без .collect()

    /**
     * Конструктор - инициализирует зависимости
     */
    public CPlace(ConfigManager configManager, DataManager dataManager, DisplayHitboxHandler displayHitboxHandler) {
        this.configManager = configManager;
        this.dataManager = dataManager;
        this.displayHitboxHandler = displayHitboxHandler;
    }

    /**
     * Основной метод обработки команды
     * @param sender - отправитель команды (игрок или консоль)
     * @param command - объект команды
     * @param label - псевдоним команды
     * @param args - аргументы команды
     * @return true - чтобы не показывать стандартное сообщение об использовании
     */
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                             @NotNull String label, @NotNull String @NotNull [] args) {
        
        // Проверка: команду может использовать только игрок
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cТолько игрок может использовать эту команду!");
            return true;
        }
        
        // Получаем предмет в основной руке игрока
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Особый случай: подкоманда debug не требует предмета
        // Иначе проверяем, что предмет существует и не является воздухом
        if (args[0] == "debug" || (item != null && !item.getType().isAir())) {
            
            // Пытаемся получить данные размещения из предмета
            PlaceableData data = this.dataManager.getDataFromItem(item);
            boolean hasData = data != null; // Есть ли данные у предмета
            
            // Если данных нет, используем значения по умолчанию из конфига
            if (!hasData) {
                data = this.configManager.getDefaultTgData();
            }
            
            // Если аргументов нет - показываем справку
            if (args.length == 0) {
                this.sendHelp(player, hasData, data);
                return true;
            }
            
            // Обработка подкоманд через switch (Java 14+ синтаксис)
            switch (args[0].toLowerCase()) {
                case "help":
                    this.sendHelp(player, hasData, data);
                    break;
                    
                case "info":
                    this.sendInfo(player, hasData, data);
                    break;
                    
                case "fixed":
                    this.handleBoolean(player, item, data, "fixed", args);
                    break;
                    
                case "centered":
                    this.handleBoolean(player, item, data, "centered", args);
                    break;
                    
                case "scale":
                    this.handleNumber(player, item, data, "scale", args, 0.1F, 5.0F);
                    break;
                    
                case "offset":
                    this.handleNumber(player, item, data, "offset", args, -2.0F, 2.0F);
                    break;
                    
                case "hitbox":
                    this.handleHitbox(player, item, data, args);
                    break;
                    
                case "placetype":
                    this.handlePlaceType(player, item, data, args);
                    break;
                    
                case "preset":
                    this.handlePreset(player, item, args);
                    break;
                    
                case "export":
                    this.handleExport(player, data, args);
                    break;
                    
                case "nbt":
                    this.handleNBT(player, item, data);
                    break;
                    
                case "remove":
                    this.handleRemove(player, item);
                    break;
                    
                case "debug":
                    // Отладка - показывает хитбоксы с учетом дистанции взаимодействия игрока
                    this.displayHitboxHandler.debugHitBox(player, 
                        player.getAttribute(Attribute.ENTITY_INTERACTION_RANGE).getValue());
                    break;
                    
                case "reload":
                    // Перезагрузка конфигов - требует права администратора
                    if (player.hasPermission("placeable.admin")) {
                        this.configManager.reloadAll();
                        this.configManager.getLangManager().sendMessageWithPrefix(player, "cplace.reload");
                    } else {
                        this.configManager.getLangManager().sendMessageWithPrefix(player, "general.no-permission");
                    }
                    break;
                    
                default:
                    player.sendMessage("§cНеизвестная подкоманда. Используйте /cplace help");
            }
            return true;
            
        } else {
            // Если в руке нет предмета
            this.configManager.getLangManager().sendMessageWithPrefix(player, "general.no-item");
            return true;
        }
    }

    /**
     * Отправляет справку по командам
     */
    private void sendHelp(Player player, boolean hasData, PlaceableData data) {
        player.sendMessage("§6=== CPlace Commands ===");
        player.sendMessage("§7/cplace help §f- показать это сообщение");
        player.sendMessage("§7/cplace info §f- информация о предмете");
        player.sendMessage("§7/cplace fixed <true/false> §f- установить fixed");
        player.sendMessage("§7/cplace centered <true/false> §f- установить centered");
        player.sendMessage("§7/cplace scale <число> §f- установить масштаб (0.1-5.0)");
        player.sendMessage("§7/cplace offset <число> §f- установить смещение (-2.0-2.0)");
        player.sendMessage("§7/cplace hitbox <x> <y> <z> §f- установить хитбокс");
        player.sendMessage("§7/cplace placetype <тип> §f- установить тип размещения");
        player.sendMessage("§7/cplace preset <имя> §f- применить пресет");
        player.sendMessage("§7/cplace export <имя> §f- экспортировать как пресет");
        player.sendMessage("§7/cplace nbt §f- скопировать NBT в буфер");
        player.sendMessage("§7/cplace remove §f- удалить все данные с предмета");
        player.sendMessage("§7/cplace reload §f- перезагрузить конфиги");
        
        // Если у предмета есть данные - показываем текущие настройки
        if (hasData) {
            player.sendMessage("§6\nТекущие настройки:");
            player.sendMessage("§7fixed: §f" + data.isFixed());
            player.sendMessage("§7centered: §f" + data.isCentered());
            player.sendMessage("§7scale: §f" + data.getScale());
            player.sendMessage("§7offset: §f" + data.getOffset());
            player.sendMessage("§7hitbox: §f" + data.getHitBox().x + " " + data.getHitBox().y + " " + data.getHitBox().z);
            player.sendMessage("§7placeType: §f" + data.getPlaceType().name());
        } else {
            player.sendMessage("§c\nУ предмета нет данных. Используйте команды для создания.");
        }
    }

    /**
     * Отправляет подробную информацию о предмете
     */
    private void sendInfo(Player player, boolean hasData, PlaceableData data) {
        if (!hasData) {
            player.sendMessage("§cУ предмета нет данных размещения!");
            return;
        }
        
        player.sendMessage("§6=== Информация о предмете ===");
        player.sendMessage("§7fixed: §f" + data.isFixed());           // Фиксированное размещение?
        player.sendMessage("§7centered: §f" + data.isCentered());     // По центру блока?
        player.sendMessage("§7scale: §f" + data.getScale());          // Масштаб модели
        player.sendMessage("§7offset: §f" + data.getOffset());        // Смещение
        player.sendMessage("§7breakCout: §f" + data.getBreakCount()); // Счетчик разрушений
        player.sendMessage("§7placeType: §f" + data.getPlaceType().name()); // Тип размещения
        player.sendMessage("§7hitbox: §f" + data.getHitBox().x + " " + data.getHitBox().y + " " + data.getHitBox().z);
        player.sendMessage("§7Sounds: §f");
        player.sendMessage("   §7Place: §f" + data.getSounds().getPlaceSound().toString());    // Звук размещения
        player.sendMessage("   §7Break: §f" + data.getSounds().getBreakSound().toString());    // Звук разрушения
        player.sendMessage("   §7Interract: §f" + data.getSounds().getInteractSound().toString()); // Звук взаимодействия
    }

    /**
     * Удаляет все данные размещения с предмета
     */
    private void handleRemove(Player player, ItemStack item) {
        if (!this.dataManager.isPlaceable(item)) {
            player.sendMessage("§cУ предмета нет данных для удаления!");
            return;
        }
        this.dataManager.removeDataFromItem(item);
        player.sendMessage("§a✓ Все данные размещения удалены с предмета!");
    }

    /**
     * Обрабатывает булевы параметры (fixed, centered)
     */
    private void handleBoolean(Player player, ItemStack item, PlaceableData data, String param, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /cplace " + param + " <true/false>");
            return;
        }
        
        boolean value;
        if (args[1].equalsIgnoreCase("true")) {
            value = true;
        } else if (args[1].equalsIgnoreCase("false")) {
            value = false;
        } else {
            player.sendMessage("§cИспользуйте true или false!");
            return;
        }
        
        // Создаем копию данных, изменяем нужный параметр и сохраняем
        PlaceableData newData = new PlaceableData(data);
        switch (param) {
            case "fixed" -> newData.setFixed(value);
            case "centered" -> newData.setCentered(value);
        }
        
        this.dataManager.saveDataToItem(item, newData);
        player.sendMessage("§a✓ " + param + " установлен в: §f" + value);
    }

    /**
     * Обрабатывает числовые параметры (scale, offset) с проверкой диапазона
     * @param min - минимальное допустимое значение
     * @param max - максимальное допустимое значение
     */
    private void handleNumber(Player player, ItemStack item, PlaceableData data, 
                              String param, String[] args, float min, float max) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /cplace " + param + " <число>");
            return;
        }
        
        try {
            float value = Float.parseFloat(args[1]);
            if (value < min || value > max) {
                player.sendMessage("§cЗначение должно быть от " + min + " до " + max);
                return;
            }
            
            PlaceableData newData = new PlaceableData(data);
            switch (param) {
                case "scale" -> newData.setScale(value);
                case "offset" -> newData.setOffset(value);
            }
            
            this.dataManager.saveDataToItem(item, newData);
            player.sendMessage("§a✓ " + param + " установлен в: §f" + value);
            
        } catch (NumberFormatException var12) {
            player.sendMessage("§cВведите число!");
        }
    }

    /**
     * Устанавливает размер хитбокса (области взаимодействия)
     */
    private void handleHitbox(Player player, ItemStack item, PlaceableData data, String[] args) {
        if (args.length < 4) {
            player.sendMessage("§cИспользование: /cplace hitbox <x> <y> <z>");
            player.sendMessage("§7Пример: /cplace hitbox 1.0 1.0 1.0");
            return;
        }
        
        try {
            float x = Float.parseFloat(args[1]);
            float y = Float.parseFloat(args[2]);
            float z = Float.parseFloat(args[3]);
            
            // Проверка допустимого диапазона размеров хитбокса
            if (x < 0.1F || x > 3.0F || y < 0.1F || y > 3.0F || z < 0.1F || z > 3.0F) {
                player.sendMessage("§cЗначения должны быть от 0.1 до 3.0");
                return;
            }
            
            PlaceableData newData = new PlaceableData(data);
            newData.setHitBox(new HitBox(x, y, z));
            this.dataManager.saveDataToItem(item, newData);
            player.sendMessage("§a✓ Хитбокс установлен в: §f" + x + " " + y + " " + z);
            
        } catch (NumberFormatException var9) {
            player.sendMessage("§cВведите числа!");
        }
    }

    /**
     * Устанавливает тип размещения предмета
     */
    private void handlePlaceType(Player player, ItemStack item, PlaceableData data, String[] args) {
        if (args.length < 2) {
            player.sendMessage("§cИспользование: /cplace placetype <тип>");
            player.sendMessage("§7Доступные типы: " + String.join(", ", PLACE_TYPES));
            return;
        }
        
        try {
            PlaceType type = PlaceType.valueOf(args[1].toUpperCase());
            PlaceableData newData = new PlaceableData(data);
            newData.setPlaceType(type);
            this.dataManager.saveDataToItem(item, newData);
            player.sendMessage("§a✓ PlaceType установлен в: §f" + type.name());
            
        } catch (IllegalArgumentException var7) {
            player.sendMessage("§cНеизвестный тип. Доступные: " + String.join(", ", PLACE_TYPES));
        }
    }

    /**
     * Применяет предустановленный пресет к предмету
     */
    private void handlePreset(Player player, ItemStack item, String[] args) {
        if (args.length < 2) {
            Set<String> presetNames = this.configManager.getPresetManager().getPresetNames();
            if (presetNames.isEmpty()) {
                player.sendMessage("§cНет доступных пресетов");
            } else {
                player.sendMessage("§6Доступные пресеты:");
                presetNames.stream().sorted().forEach((name) -> player.sendMessage("§7- §f" + name));
                player.sendMessage("§7Использование: /cplace preset <имя>");
            }
            return;
        }
        
        String presetName = args[1].toLowerCase();
        PlaceableData preset = this.configManager.getPresetManager().getPreset(presetName);
        
        if (preset == null) {
            player.sendMessage("§cПресет '" + presetName + "' не найден!");
            return;
        }
        
        // Применяем пресет к предмету
        this.dataManager.saveDataToItem(item, preset);
        player.sendMessage("§a✓ Пресет '" + presetName + "' применен!");
        
        // Показываем примененные настройки
        player.sendMessage("§7fixed: §f" + preset.isFixed());
        player.sendMessage("§7centered: §f" + preset.isCentered());
        player.sendMessage("§7scale: §f" + preset.getScale());
        player.sendMessage("§7offset: §f" + preset.getOffset());
        player.sendMessage("§7hitbox: §f" + preset.getHitBox().x + " " + preset.getHitBox().y + " " + preset.getHitBox().z);
        player.sendMessage("§7placeType: §f" + preset.getPlaceType().name());
    }

    /**
     * Экспортирует текущие настройки предмета как YAML-пресет
     * Копирует в буфер обмена готовую секцию для presets.yml
     */
    private void handleExport(Player player, PlaceableData data, String[] args) {
        // Генерируем имя пресета (пользовательское или временное)
        String presetName = args.length > 1 ? args[1].toLowerCase() : "custom_" + System.currentTimeMillis();
        
        // Формируем YAML-строку для пресета
        String yamlPreset = String.format(
            "  %s:\n    fixed: %b\n    centered: %b\n    scale: %.2f\n    offset: %.2f\n" +
            "    hitbox:\n      x: %.2f\n      y: %.2f\n      z: %.2f\n    placeType: %s",
            presetName, data.isFixed(), data.isCentered(), data.getScale(), data.getOffset(),
            data.getHitBox().x, data.getHitBox().y, data.getHitBox().z, data.getPlaceType().name()
        );
        
        // Копируем в системный буфер обмена
        StringSelection selection = new StringSelection(yamlPreset);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, (ClipboardOwner)null);
        
        player.sendMessage("§a✓ Пресет скопирован в буфер обмена!");
        player.sendMessage("§7Добавьте в presets.yml:");
        player.sendMessage("§f" + yamlPreset);
    }

    /**
     * Копирует NBT-данные предмета в буфер обмена
     */
    private void handleNBT(Player player, ItemStack item, PlaceableData data) {
        if (data == null) {
            player.sendMessage("§cУ предмета нет данных размещения!");
            return;
        }
        
        // Конвертируем данные в NBT и получаем строковое представление
        ReadWriteNBT nbt = this.dataManager.dataToNBT(data);
        String nbtString = nbt.toString();
        
        // Копируем в буфер обмена
        StringSelection selection = new StringSelection(nbtString);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, (ClipboardOwner)null);
        
        player.sendMessage("§a✓ NBT скопирован в буфер обмена!");
        player.sendMessage("§7NBT: §f" + nbtString);
    }

    /**
     * Автодополнение (Tab Completion) для команды
     * @return список предложений для текущего аргумента
     */
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                  @NotNull String label, @NotNull String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            return Collections.emptyList();
        }
        
        // Аргумент 1: подкоманды
        if (args.length == 1) {
            return this.filterSuggestions(SUBCOMMANDS, args[0]);
        }
        
        // Аргумент 2: параметры в зависимости от подкоманды
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "fixed":
                case "centered":
                    return this.filterSuggestions(Arrays.asList("true", "false"), args[1]);
                    
                case "scale":
                case "offset":
                    return this.filterSuggestions(Arrays.asList("0.5", "1.0", "1.5", "2.0"), args[1]);
                    
                case "hitbox":
                    return this.filterSuggestions(Arrays.asList("0.5", "1.0", "1.5"), args[1]);
                    
                case "placetype":
                    return this.filterSuggestions(PLACE_TYPES, args[1]);
                    
                case "preset":
                    Set<String> presetNames = this.configManager.getPresetManager().getPresetNames();
                    return this.filterSuggestions(presetNames.stream().collect(Collectors.toList()), args[1]);
                    
                case "export":
                    return this.filterSuggestions(Arrays.asList("my_preset", "custom", "painting", "item_frame"), args[1]);
                    
                default:
                    return Collections.emptyList();
            }
        }
        
        // Аргумент 3 для hitbox (ось Y)
        if (args.length == 3 && args[0].equalsIgnoreCase("hitbox")) {
            return this.filterSuggestions(Arrays.asList("0.5", "1.0", "1.5"), args[2]);
        }
        
        // Аргумент 4 для hitbox (ось Z)
        if (args.length == 4 && args[0].equalsIgnoreCase("hitbox")) {
            return this.filterSuggestions(Arrays.asList("0.5", "1.0", "1.5"), args[3]);
        }
        
        return Collections.emptyList();
    }

    /**
     * Вспомогательный метод для фильтрации предложений автодополнения
     * @param suggestions - список всех возможных вариантов
     * @param input - текущий ввод пользователя
     * @return отфильтрованный список (начинающиеся с input)
     */
    private List<String> filterSuggestions(List<String> suggestions, String input) {
        if (input.isEmpty()) {
            return suggestions;
        }
        return suggestions.stream()
            .filter((s) -> s.toLowerCase().startsWith(input.toLowerCase()))
            .toList();
    }
}