package com.xkball.panoramic_screenshot.mixin;

import com.mojang.blaze3d.platform.Window;
import com.xkball.panoramic_screenshot.IExtendedWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Window.class)
public abstract class WindowMixin implements IExtendedWindow {
    
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;
    
    @Shadow
    protected abstract void setMode();
    
    @Shadow
    public abstract void setWindowed(int width, int height);
    
    @Shadow
    private boolean fullscreen;
    
    @Shadow
    protected abstract void updateFullscreen(boolean enableVsync);
    
    @Shadow
    private boolean vsync;
    @Unique
    private int panoramicScreenShot$wOld;
    @Unique
    private int panoramicScreenShot$hOld;
    @Unique
    private boolean panoramicScreenShot$wasFullScreen = false;
    
    @Override
    public void setOverrideSize(int w, int h) {
        this.panoramicScreenShot$wOld = this.framebufferWidth;
        this.panoramicScreenShot$hOld = this.framebufferHeight;
        this.panoramicScreenShot$wasFullScreen = this.fullscreen;
        this.setWindowed(w,h);
    }
    
    @Override
    public void resetOverrideSize() {
        if(this.panoramicScreenShot$wasFullScreen) this.updateFullscreen(this.vsync);
        else this.setWindowed(panoramicScreenShot$wOld,panoramicScreenShot$hOld);
    }
}
