package com.xkball.panoramic_screenshot;

import net.minecraft.client.Minecraft;

public interface IExtendedWindow {
    
    void setOverrideSize(int w, int h);
    
    void resetOverrideSize();
    
    default void enableOverride(int w, int h){
        setOverrideSize(w, h);
        Minecraft.getInstance().resizeDisplay();
    }
    
    default void disableOverride(){
        resetOverrideSize();
        Minecraft.getInstance().resizeDisplay();
    }
    
    static IExtendedWindow cast(Object obj){
        return (IExtendedWindow) obj;
    }
    
    static IExtendedWindow get(){
        return cast(Minecraft.getInstance().getWindow());
    }
}
