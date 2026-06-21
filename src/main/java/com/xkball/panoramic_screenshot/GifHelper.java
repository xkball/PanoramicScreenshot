package com.xkball.panoramic_screenshot;

import com.xkball.panoramic_screenshot.utils.GifSequenceWriter;
import com.xkball.panoramic_screenshot.utils.ImageUtils;
import com.xkball.panoramic_screenshot.utils.TickSequence;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Util;
import net.neoforged.fml.loading.FMLPaths;

import javax.imageio.stream.FileImageOutputStream;
import javax.imageio.stream.ImageOutputStream;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

public class GifHelper {
    
    private static final Logger LOGGER = LogUtils.getLogger();
    public final List<BufferedImage> images = new ArrayList<>();
    public volatile double timeSec = 2;
    public volatile int frameRate = 10;
    public volatile boolean finished = false;
    private long startTime;
    private long lastFrameTime;
    private long frameTime;
    private volatile boolean started = false;
    
    public void start(){
        if(this.started){
            Minecraft.getInstance().execute(
                    () -> Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(
                            Component.literal("Already started !")));
            return;
        }
        this.started = true;
        TickSequence.builder()
                .append(() -> {
                    this.startTime = System.nanoTime();
                    this.frameTime = 1_000_000_000/frameRate;
                })
                .repeatUntil("after game render",() -> {
                    var current = System.nanoTime();
                    if(finished || current - startTime > timeSec * 1_000_000_000L) return true;
                    if(current - lastFrameTime > frameTime){
                        lastFrameTime = current;
                        Screenshot.takeScreenshot(Minecraft.getInstance().gameRenderer.mainRenderTarget(),(i) -> {
                            var ima = ImageUtils.toBufferedImage(i);
                            synchronized (images){
                                images.add(ima);
                            }
                            i.close();
                        });
                    }
                    return false;
                })
                .append(() -> save(FMLPaths.GAMEDIR.get().resolve("screenshots")))
                .buildInClient();
    }
    
    public void save(Path path){
        var file = path.resolve(Util.getFilenameFormattedDateTime() + ".gif");
        if(!path.toFile().exists()) path.toFile().mkdirs();
        try{
            ImageOutputStream output = new FileImageOutputStream(file.toFile());
            synchronized (images){
                GifSequenceWriter writer = new GifSequenceWriter(
                        output, images.getFirst().getType(), 1000/frameRate, true);
                for (BufferedImage img : images) {
                    writer.writeToSequence(img);
                }
                writer.close();
                images.clear();
            }
            output.close();
            Minecraft.getInstance().execute(
                    () -> Minecraft.getInstance().gui.hud.getChat().addClientSystemMessage(
                            Component.literal(file.toFile().getName())
                                    .withStyle(ChatFormatting.UNDERLINE)
                                    .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile(file.toFile().getAbsolutePath())))
                    )
            );
        } catch (IOException e) {
            LOGGER.error("Couldn't save gif", e);
            throw new RuntimeException(e);
        }
        this.started = false;
    }
    
    public boolean isStarted(){
        return started;
    }
}
