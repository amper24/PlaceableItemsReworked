package dev.moonaticks.placeableItemsReworked.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bukkit.Color;
import org.bukkit.FluidCollisionMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import dev.moonaticks.placeableItemsReworked.managers.DataManager;
import dev.moonaticks.placeableItemsReworked.utils.HitBox;
import dev.moonaticks.placeableItemsReworked.utils.PlaceableData;

public class DisplayHitboxHandler {
    DataManager dataManager;

    public DisplayHitboxHandler(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public ItemDisplay getLookedDisplay(Player player, double maxDistance) {

        Location eye = player.getEyeLocation();
        Vector rayOrigin = eye.toVector();
        Vector rayDir = eye.getDirection().normalize();

        ItemDisplay closest = null;
        double closestDistance = maxDistance;
        Location closestHitLocation = null; // Сохраняем точку попадания как Bukkit Location

        List<ItemDisplay> displays = getNearbyPlaceableDisplays(player, closestDistance);

        for (ItemDisplay display : displays) {

            if (!dataManager.isPlaceable(display)) continue; // только наши предметы

            PlaceableData data = dataManager.getDataFromDisplay(display);
            if (data == null) continue;

            HitBox hitBox = data.getHitBox();
            if (hitBox == null) continue;

            Location displayLoc = display.getLocation();
            Vector forward = displayLoc.getDirection().normalize();
            Vector up = new Vector(0,1,0);
            Vector right = forward.clone().crossProduct(up).normalize();
            Vector center = displayLoc.toVector();

            // перевод луча в локальные координаты display
            Vector localOrigin = rayOrigin.clone().subtract(center);
            double ox = localOrigin.dot(right);
            double oy = localOrigin.dot(up);
            double oz = localOrigin.dot(forward);

            double dx = rayDir.dot(right);
            double dy = rayDir.dot(up);
            double dz = rayDir.dot(forward);

            BoundingBox box = new BoundingBox(
                -hitBox.x, -hitBox.y, -hitBox.z,
                hitBox.x,  hitBox.y, hitBox.z
            );

            RayTraceResult result = box.rayTrace(
                new Vector(ox, oy, oz),
                new Vector(dx, dy, dz),
                maxDistance
            );
            if (result != null) {
                double distance = eye.toVector().distance(center);
                Vector hitLocal = result.getHitPosition();
                
                Vector hitWorld = center.clone()
                    .add(right.clone().multiply(hitLocal.getX()))
                    .add(up.clone().multiply(hitLocal.getY()))
                    .add(forward.clone().multiply(hitLocal.getZ()));

                Location hitLocation = new Location(displayLoc.getWorld(), 
                    hitWorld.getX(), 
                    hitWorld.getY(), 
                    hitWorld.getZ());
                if (distance < closestDistance) {
                    closestDistance = distance;
                    closest = display;
                    closestHitLocation = hitLocation; // Сохраняем точку попадания
                }
            }
        }


        if(closest != null) {
            if(isBlockBeetween(eye, maxDistance, closestHitLocation)) {
                return null;
            }
            else {
                return closest;
            }
        }
        else {
            return closest;
        }
    }

    public boolean isBlockBeetween(Location eye, double maxDistance, Location displayLoc) {
        
        // Выполняем rayTrace от глаз игрока до позиции дисплея
        double distance = eye.distance(displayLoc);
        if (distance > maxDistance) {
            return false;
        }

        // Получаем направление от глаз до дисплея
        Vector direction = displayLoc.toVector().subtract(eye.toVector()).normalize();

        // Выполняем rayTrace от глаз игрока в направлении до дисплея
        RayTraceResult result = eye.getWorld().rayTraceBlocks(
            eye,
            direction,
            distance, // используем точное расстояние до дисплея
            FluidCollisionMode.NEVER
        );
        // Если результат не null, значит есть блок на пути
        if (result != null) {
            return true;
        }
        return false;
    }
    /**
     * Ищет все активные Placeable ItemDisplay вокруг игрока в радиусе radius блоков
     */
    public List<ItemDisplay> getNearbyPlaceableDisplays(Player player, double radius) {
        Collection<Entity> nearby = player.getNearbyEntities(radius, radius, radius);

        List<ItemDisplay> result = new ArrayList<>();

        for (Entity e : nearby) {
            if (!(e instanceof ItemDisplay display)) continue;

            if (!dataManager.isPlaceable(display)) continue; // проверяем, что это наш дисплей

            result.add(display);
        }

        return result;
    }

    public void debugHitBox(Player player, double maxDistance) {

        ItemDisplay looked = getLookedDisplay(player, maxDistance);
        if (looked == null) return;

        PlaceableData data = dataManager.getDataFromDisplay(looked);
        if (data == null) return;

        HitBox hitBox = data.getHitBox();
        if (hitBox == null) return;

        Location loc = looked.getLocation();
        Vector forward = loc.getDirection().normalize();
        Vector up = new Vector(0,1,0);
        Vector right = forward.clone().crossProduct(up).normalize();
        Vector center = loc.toVector();

        double x = hitBox.x;
        double y = hitBox.y;
        double z = hitBox.z;

        // 8 вершин коробки
        Vector[] corners = new Vector[] {
                new Vector(-x,-y,-z),
                new Vector(x,-y,-z),
                new Vector(x,y,-z),
                new Vector(-x,y,-z),

                new Vector(-x,-y,z),
                new Vector(x,-y,z),
                new Vector(x,y,z),
                new Vector(-x,y,z)
        };

        // Рисуем рёбра коробки (лучше, чем просто точки)
        int[][] edges = new int[][] {
                {0,1},{1,2},{2,3},{3,0}, // нижнее основание
                {4,5},{5,6},{6,7},{7,4}, // верхнее основание
                {0,4},{1,5},{2,6},{3,7}  // вертикальные рёбра
        };

        for (int[] edge : edges) {
            Vector start = corners[edge[0]];
            Vector end = corners[edge[1]];

            // переводим в мировые координаты
            Vector worldStart = center.clone()
                    .add(right.clone().multiply(start.getX()))
                    .add(up.clone().multiply(start.getY()))
                    .add(forward.clone().multiply(start.getZ()));

            Vector worldEnd = center.clone()
                    .add(right.clone().multiply(end.getX()))
                    .add(up.clone().multiply(end.getY()))
                    .add(forward.clone().multiply(end.getZ()));

            // рисуем линию частицами
            drawLine(player, worldStart, worldEnd, 5); // 5 точек на ребро
        }
    }
    
    private void drawLine(Player player, Vector start, Vector end, int points) {
        Vector dir = end.clone().subtract(start).multiply(1.0 / points);
        
        // Создаем DustOptions с цветом (например, красный) и размером
        Color color = Color.RED; // или любой другой цвет
        int size = 1; // размер частицы
        Particle.DustOptions dustOptions = new Particle.DustOptions(color, size);
        
        for (int i = 0; i <= points; i++) {
            Vector point = start.clone().add(dir.clone().multiply(i));
            
            // Правильный вызов с DustOptions
            player.spawnParticle(
                Particle.DUST, 
                point.getX(), 
                point.getY(), 
                point.getZ(), 
                1,  // количество
                0, 0, 0, // смещение
                dustOptions // обязательный параметр!
            );
        }
    }
}
