package com.xkball.panoramic_screenshot.mixin;

import com.mojang.blaze3d.platform.Window;
import com.xkball.panoramic_screenshot.IExtendedWindow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Window.class)
public class WindowMixin implements IExtendedWindow {
    
    @Shadow private int framebufferWidth;
    @Shadow private int framebufferHeight;
    @Unique
    private int panoramicScreenShot$wOld;
    @Unique
    private int panoramicScreenShot$hOld;
    
    @Override
    public void setOverrideSize(int w, int h) {
        this.panoramicScreenShot$wOld = this.framebufferWidth;
        this.panoramicScreenShot$hOld = this.framebufferHeight;
        this.framebufferHeight = h;
        this.framebufferWidth = w;
    }
    
    @Override
    public void resetOverrideSize() {
        this.framebufferHeight = this.panoramicScreenShot$hOld;
        this.framebufferWidth = this.panoramicScreenShot$wOld;
    }
}
