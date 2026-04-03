package dev.moonaticks.placeableItemsReworked.utils;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Sounds {
    private String placeSound;
    private String breakSound;
    private String interactSound;

    // ===== КОНСТРУКТОРЫ =====
    public Sounds(String placeSound, String breakSound, String interactSound) {
        this.placeSound = placeSound;
        this.breakSound = breakSound;
        this.interactSound = interactSound;
    }

    public Sounds(Sound placeSound, Sound breakSound, Sound interactSound) {
        this.placeSound = placeSound != null ? placeSound.toString() : null;
        this.breakSound = breakSound != null ? breakSound.toString() : null;
        this.interactSound = interactSound != null ? interactSound.toString() : null;
    }
    // ===== ГЕТТЕРЫ =====
    public String getPlaceSound() { return placeSound; }
    public String getBreakSound() { return breakSound; }
    public String getInteractSound() { return interactSound; }

    // ===== СЕТТЕРЫ =====
    public void setPlaceSound(String placeSound) { this.placeSound = placeSound; }
    public void setPlaceSound(Sound placeSound) { this.placeSound = placeSound.toString(); }

    public void setBreakSound(String breakSound) { this.breakSound = breakSound; }
    public void setBreakSound(Sound breakSound) { this.breakSound = breakSound.toString(); }

    public void setInteractSound(String interactSound) { this.interactSound = interactSound; }
    public void setInteractSound(Sound interactSound) { this.interactSound = interactSound.toString(); }
    // ===== ВОСПРОИЗВЕДЕНИЕ =====
    public void playPlaceSound(Location loc) {
        playSound(loc, placeSound);
    }

    public void playPlaceSound(Player player) {
        playSound(player, placeSound);
    }

    public void playBreakSound(Location loc) {
        playSound(loc, breakSound);
    }

    public void playBreakSound(Player player) {
        playSound(player, breakSound);
    }

    public void playInteractSound(Location loc) {
        playSound(loc, interactSound);
    }

    public void playInteractSound(Player player) {
        playSound(player, interactSound);
    }

    private void playSound(Location loc, String sound) {
        if (sound == null || sound.isEmpty() || loc == null || loc.getWorld() == null) return;
        
        // Рандомный питч: 0.9 + (от 0.0 до 0.2)
        float randomPitch = (float) (0.9 + Math.random() * 0.2);
        
        loc.getWorld().playSound(loc, sound, 1.0f, randomPitch);
    }

    private void playSound(Player player, String sound) {
        if (sound == null || sound.isEmpty() || player == null) return;
        
        float randomPitch = (float) (0.9 + Math.random() * 0.2);
        
        // Используем player.getLocation(), чтобы звук шел от игрока
        player.playSound(player.getLocation(), sound, 1.0f, randomPitch);
    }


    // ===== ПРОВЕРКИ =====
    public boolean hasPlaceSound() { return placeSound != null; }
    public boolean hasBreakSound() { return breakSound != null; }
    public boolean hasInteractSound() { return interactSound != null; }
}