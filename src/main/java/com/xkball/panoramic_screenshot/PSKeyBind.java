package com.xkball.panoramic_screenshot;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
public class PSKeyBind {
    
    public static final Lazy<KeyMapping> PANORAMIC_KEY = Lazy.of(() -> new KeyMapping("keys.panoramic_screenshot.take_panoramic_screenshot", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F8, KeyMapping.CATEGORY_MISC));
    public static final Lazy<KeyMapping> SKYBOX_KEY = Lazy.of(() -> new KeyMapping("keys.panoramic_screenshot.take_skybox_screenshot", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, KeyMapping.CATEGORY_MISC));
    
    @SubscribeEvent
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(PANORAMIC_KEY.get());
        event.register(SKYBOX_KEY.get());
    }
    
    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class Event{
        
        @SubscribeEvent
        public static void onKeyInput(InputEvent.Key event){
            if(event.getAction() != InputConstants.PRESS) return;
            if(PANORAMIC_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKey(),event.getScanCode()))){
                var fov = Minecraft.getInstance().options.fov().get();
                if(fov >65){
                    PanoramicScreenShotHelper.INSTANCE.startDefault(PanoramicScreenShotHelper.Mode.PRECISE);
                }
                else {
                    PanoramicScreenShotHelper.INSTANCE.startDefault(PanoramicScreenShotHelper.Mode.FAST);
                }
            }
            else if(SKYBOX_KEY.get().isActiveAndMatches(InputConstants.getKey(event.getKey(),event.getScanCode()))){
                var co = PanoramicScreenshot.grabPanoramixScreenshot("skybox",2048,2048);
                Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(co));
            }
        }
    }
    
}
