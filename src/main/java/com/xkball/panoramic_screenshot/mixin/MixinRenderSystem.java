package com.xkball.panoramic_screenshot.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import com.xkball.panoramic_screenshot.PanoramicScreenShotHelper;
import com.xkball.panoramic_screenshot.utils.TickSequenceHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(RenderSystem.class)
public class MixinRenderSystem {
    
    @Inject(method = "flipFrame", at = @At("HEAD"))
    private static void onFlipFrame(long windowId, CallbackInfo ci){
        if(PanoramicScreenShotHelper.INSTANCE.takeScreenShot && !PanoramicScreenShotHelper.INSTANCE.takingScreenShot){
            PanoramicScreenShotHelper.INSTANCE.takingScreenShot = true;
            Screenshot.takeScreenshot(Minecraft.getInstance().gameRenderer.mainRenderTarget(),(i) -> {
                PanoramicScreenShotHelper.INSTANCE.writeImageSection(i);
                PanoramicScreenShotHelper.INSTANCE.takeScreenShot = false;
                PanoramicScreenShotHelper.INSTANCE.takingScreenShot = false;
            });
        }
        TickSequenceHandler.CLIENT_HANDLER.accept("after game render");
    }
}
