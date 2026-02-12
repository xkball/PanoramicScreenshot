package com.xkball.panoramic_screenshot.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.xkball.panoramic_screenshot.PanoramicScreenshot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.phys.Vec2;
import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HexFormat;
import java.util.List;

public class VanillaUtils {
    
    public static final Direction[] DIRECTIONS = Direction.values();
    public static final Identifier MISSING_TEXTURE = Identifier.withDefaultNamespace("missingno");
    public static final int TRANSPARENT = VanillaUtils.getColor(255, 255, 255, 0);
    public static final int GUI_GRAY = VanillaUtils.getColor(30, 30, 30, 200);
    
    public static Identifier modRL(String path) {
        return rLOf(PanoramicScreenshot.MODID, path);
    }
    
    public static Identifier rLOf(String namespace, String path) {
        return Identifier.fromNamespaceAndPath(namespace, path);
    }
    
    public static EquipmentSlot equipmentSlotFromHand(InteractionHand hand) {
        return hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
    }
    
    //irrelevant vanilla(笑)
    public static int getColor(int r, int g, int b, int a) {
        return a << 24 | r << 16 | g << 8 | b;
    }
    
    public static int parseColorHEX(String color) throws IllegalArgumentException {
        if (color.length() == 6) {
            return getColor(
                    Integer.parseInt(color.substring(0, 2), 16),
                    Integer.parseInt(color.substring(2, 4), 16),
                    Integer.parseInt(color.substring(4, 6), 16),
                    255);
        }
        if (color.length() == 8) {
            return getColor(
                    Integer.parseInt(color.substring(0, 2), 16),
                    Integer.parseInt(color.substring(2, 4), 16),
                    Integer.parseInt(color.substring(4, 6), 16),
                    Integer.parseInt(color.substring(6, 8), 16)
            );
        }
        throw new IllegalArgumentException("Format of color must be RGB or RGBA digits");
    }
    
    public static String hexColorFromInt(int color) {
        var a = color >>> 24;
        var r = (color >> 16) & 0xFF;
        var g = (color >> 8) & 0xFF;
        var b = color & 0xFF;
        return String.format("%02X%02X%02X%02X", r, g, b, a).toUpperCase();
    }
    
    @SuppressWarnings("SuspiciousNameCombination")
    public static Vec2 rotate90FromBlockCenterYP(Vec2 point, int times) {
        times = times % 4;
        if (times == 0) return point;
        var x = point.x;
        var y = point.y;
        if (times == 1) return new Vec2(16 - y, x);
        if (times == 2) return new Vec2(16 - x, 16 - y);
        return new Vec2(y, 16 - x);
    }
    
    public static String md5(String input) {
        try {
            var md = MessageDigest.getInstance("MD5");
            var bytes = md.digest(input.getBytes());
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static String base64(byte[] bytes) {
        return Base64.encodeBase64String(bytes);
    }
    
    public static String removeAfterLastCharOf(String str,char c){
        return str.substring(0,str.lastIndexOf(c));
    }
    
    public static List<String> searchStartWith(String key, Collection<String> src){
        var startWithList = new ArrayList<String>();
        for (var str : src) {
            var searchEntry = str.toLowerCase();
            if (searchEntry.startsWith(key)) startWithList.add(str);
        }
        startWithList.sort(String::compareTo);
        return startWithList;
    }
    
    public static List<String> searchInLowerCase(String key, Collection<String> src) {
        key = key.toLowerCase();
        var startWithList = new ArrayList<String>();
        var containsList = new ArrayList<String>();
        for (var str : src) {
            var searchEntry = str.toLowerCase();
            if (searchEntry.startsWith(key)) startWithList.add(str);
            else if (searchEntry.contains(key)) containsList.add(str);
        }
        startWithList.sort(String::compareTo);
        containsList.sort(String::compareTo);
        startWithList.addAll(containsList);
        return startWithList;
    }
    
    public static List<String> search(String key, Collection<String> src) {
        var startWithList = new ArrayList<String>();
        var containsList = new ArrayList<String>();
        for (var str : src) {
            if (str.startsWith(key)) startWithList.add(str);
            else if (str.contains(key)) containsList.add(str);
        }
        startWithList.sort(String::compareTo);
        containsList.sort(String::compareTo);
        startWithList.addAll(containsList);
        return startWithList;
    }
    
    public static JsonElement readJsonFromResource(Resource resource) throws IOException {
        try(var reader = resource.openAsReader()){
            return JsonParser.parseReader(reader);
        }
    }

}
