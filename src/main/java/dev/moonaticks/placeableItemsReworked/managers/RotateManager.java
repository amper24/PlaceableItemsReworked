package dev.moonaticks.placeableItemsReworked.managers;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RotateManager {

    public void calculateRotation(BlockFace face, ItemDisplay display, Player player, boolean snapped) {
        if(snapped) {
            rotateBySurfaceNormal(display, player, face);
        } else {
            rotateBySurfaceNormalFREE(display, player, face);
        }
    }

    public void setFreeRotation(BlockFace face, ItemDisplay display, Player player) {
        calculateRotation(face, display, player, false);
    }

    public void setSnappedRotation(BlockFace face, ItemDisplay display, Player player) {
        calculateRotation(face, display, player, true);
    }

    public static void lookAtPlayerSnap90(ItemDisplay entity, Player player) {
        Location entityLoc = entity.getLocation();
        Location playerLoc = player.getLocation();
        double dx = playerLoc.getX() - entityLoc.getX();
        double dz = playerLoc.getZ() - entityLoc.getZ();
        int snappedYaw = Math.abs(dx) > Math.abs(dz) ? (dx > 0.0d ? -90 : 90) : (dz > 0.0d ? 0 : 180);
        entityLoc.setYaw(snappedYaw);
        entity.teleport(entityLoc);
    }

    public static void lookAtPlayerYawOnly(ItemDisplay entity, Player player) {
        Location entityLoc = entity.getLocation();
        Location playerLoc = player.getLocation();
        double dx = playerLoc.getX() - entityLoc.getX();
        double dz = playerLoc.getZ() - entityLoc.getZ();
        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0d;
        entityLoc.setYaw((float) yaw);
        entity.teleport(entityLoc);
    }
    
    public static void lookAtPlayerFull(ItemDisplay entity, Player player) {
        Location entityLoc = entity.getLocation();
        Location playerEye = player.getEyeLocation();

        double dx = playerEye.getX() - entityLoc.getX();
        double dy = playerEye.getY() - entityLoc.getY();
        double dz = playerEye.getZ() - entityLoc.getZ();

        double yaw = Math.toDegrees(Math.atan2(dz, dx)) - 90.0d;
        double distanceXZ = Math.sqrt(dx * dx + dz * dz);
        double pitch = -Math.toDegrees(Math.atan2(dy, distanceXZ));

        entityLoc.setYaw((float) yaw);
        entityLoc.setPitch((float) pitch);
        entity.teleport(entityLoc);
    }

    public static void rotateBySurfaceNormal(ItemDisplay entity, Player player, BlockFace blockFace) {
        if (!(entity instanceof ItemDisplay display)) {
            return;
        }
        Transformation original = display.getTransformation();
        Quaternionf rotation;
        switch (blockFace) {
            case BlockFace.DOWN -> {
                lookAtPlayerSnap90(entity, player);
                rotation = new Quaternionf().rotationX((float) Math.PI);
            }
            case BlockFace.NORTH -> {
                rotation = new Quaternionf().rotationX(-1.5707964f).rotateY((float) Math.PI);
            }
            case BlockFace.SOUTH -> {
                rotation = new Quaternionf().rotationX(1.5707964f);
            }
            case BlockFace.WEST -> {
                rotation = new Quaternionf().rotationZ(1.5707964f).rotateY(-1.5707964f);
            }
            case BlockFace.EAST -> {
                rotation = new Quaternionf().rotationZ(-1.5707964f).rotateY(1.5707964f);
            }
            default -> {
                lookAtPlayerSnap90(entity, player);
                rotation = new Quaternionf();
            }
        }

        display.setTransformation(new Transformation(
                new Vector3f(original.getTranslation()),
                rotation,
                new Vector3f(original.getScale()),
                new Quaternionf(original.getRightRotation())
        ));
    }

    public static void rotateBySurfaceNormalFREE(ItemDisplay entity, Player player, BlockFace blockFace) {
        if (!(entity instanceof ItemDisplay display)) {
            return;
        }

        Transformation original = display.getTransformation();
        Quaternionf rotation;
        switch (blockFace) {
            case BlockFace.DOWN -> {
                lookAtPlayerYawOnly(entity, player);
                rotation = new Quaternionf().rotationX((float) Math.PI);
            }
            case BlockFace.NORTH -> {
                rotation = new Quaternionf().rotationX(-1.5707964f).rotateY((float) Math.PI);
            }
            case BlockFace.SOUTH -> {
                rotation = new Quaternionf().rotationX(1.5707964f);
            }
            case BlockFace.WEST -> {
                rotation = new Quaternionf().rotationZ(1.5707964f).rotateY(-1.5707964f);
            }
            case BlockFace.EAST -> {
                rotation = new Quaternionf().rotationZ(-1.5707964f).rotateY(1.5707964f);
            }
            default -> {
                lookAtPlayerYawOnly(entity, player);
                rotation = new Quaternionf();
            }
        }

        display.setTransformation(new Transformation(
                new Vector3f(original.getTranslation()),
                rotation,
                new Vector3f(original.getScale()),
                new Quaternionf(original.getRightRotation())
        ));
    }
}