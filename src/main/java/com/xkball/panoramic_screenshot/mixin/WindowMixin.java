package com.xkball.panoramic_screenshot.mixin;

import com.mojang.blaze3d.platform.Window;
import com.xkball.panoramic_screenshot.IExtendedWindow;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Window.class)
public abstract class WindowMixin implements IExtendedWindow {
    
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;
    
    @Shadow
    public abstract void setWindowed(int width, int height);
    
    @Shadow
    private boolean fullscreen;
    
    @Shadow
    public abstract void updateFullscreenIfChanged();

    @Shadow
    private int x;
    @Shadow
    private int y;
    @Shadow
    @Final
    private long handle;
    @Unique
    private int panoramicScreenShot$wOld;
    @Unique
    private int panoramicScreenShot$hOld;
    @Unique
    private boolean panoramicScreenShot$wasFullScreen = false;
    @Unique
    private int panoramicScreenShot$x;
    @Unique
    private int panoramicScreenShot$y;
    
    @Override
    public void setOverrideSize(int w, int h) {
        this.panoramicScreenShot$wOld = this.framebufferWidth;
        this.panoramicScreenShot$hOld = this.framebufferHeight;
        this.panoramicScreenShot$wasFullScreen = this.fullscreen;
        this.panoramicScreenShot$x = this.x;
        this.panoramicScreenShot$y = this.y;
        this.setWindowed(w,h);
    }
    
    @Override
    public void resetOverrideSize() {
        if(this.panoramicScreenShot$wasFullScreen) {
            this.fullscreen = true;
            this.updateFullscreenIfChanged();
        }
        else {
            this.setWindowed(panoramicScreenShot$wOld,panoramicScreenShot$hOld);
            GLFW.glfwSetWindowPos(this.handle, this.panoramicScreenShot$x, this.panoramicScreenShot$y);
        }

    }
}
