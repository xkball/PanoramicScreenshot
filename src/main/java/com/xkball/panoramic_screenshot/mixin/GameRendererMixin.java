package com.xkball.panoramic_screenshot.mixin;

import com.xkball.panoramic_screenshot.PanoramicScreenShotHelper;
import com.xkball.panoramic_screenshot.utils.TickSequenceHandler;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(GameRenderer.class)
public class GameRendererMixin {
    
    @Inject(method = "render",at = @At("RETURN"))
    public void afterRender(DeltaTracker deltaTracker, boolean renderLevel, CallbackInfo ci){
        if(PanoramicScreenShotHelper.INSTANCE.takeScreenShot && !PanoramicScreenShotHelper.INSTANCE.takingScreenShot){
            PanoramicScreenShotHelper.INSTANCE.takingScreenShot = true;
            Screenshot.takeScreenshot(Minecraft.getInstance().getMainRenderTarget(),(i) -> {
                PanoramicScreenShotHelper.INSTANCE.writeImageSection(i);
                PanoramicScreenShotHelper.INSTANCE.takeScreenShot = false;
                PanoramicScreenShotHelper.INSTANCE.takingScreenShot = false;
            });
        }
        TickSequenceHandler.CLIENT_HANDLER.accept("after game render");
    }
}
