package com.xkball.panoramic_screenshot;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.xkball.panoramic_screenshot.utils.TickSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.command.EnumArgument;
import org.slf4j.Logger;

import java.io.File;


@Mod(PanoramicScreenshot.MODID)
@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class PanoramicScreenshot {
    public static final String MODID = "panoramic_screenshot";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PanoramicScreenshot(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        context.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
    @SubscribeEvent
    public static void onRegClientCommand(RegisterClientCommandsEvent event){
        event.getDispatcher().register(
                Commands.literal("screenshot")
                        .then(Commands.literal("normal")
                                .executes(c -> {
                                    Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), Minecraft.getInstance().getMainRenderTarget(), (co) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(co)));
                                    return 0;
                                })
                                .then(Commands.argument("width",IntegerArgumentType.integer(1,16384))
                                        .then(Commands.argument("height", IntegerArgumentType.integer(1,16384))
                                                .executes(PanoramicScreenshot::screenshotWithSize))))
                        .then(Commands.literal("panoramic")
                                .then(Commands.argument("mode", EnumArgument.enumArgument(PanoramicScreenShotHelper.Mode.class))
                                        .executes(PanoramicScreenShotHelper.INSTANCE::startDefault)
                                        .then(Commands.argument("height", IntegerArgumentType.integer(1,16384))
                                                .then(Commands.argument("fov", IntegerArgumentType.integer(1,179))
                                                        .then(Commands.argument("yaw_start", IntegerArgumentType.integer(0,360))
                                                                .then(Commands.argument("frame_delay", IntegerArgumentType.integer(0,1000))
                                                                        .executes(PanoramicScreenShotHelper.INSTANCE::start)))))))
                        .then(Commands.literal("skybox")
                                .executes((c) -> {
                                    var co = PanoramicScreenshot.grabPanoramixScreenshot("skybox",2048,2048);
                                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(co));
                                    return 0;
                                })
                                .then(Commands.argument("name", StringArgumentType.string())
                                        .then(Commands.argument("size", IntegerArgumentType.integer(1,16384))
                                                .executes((c) ->{
                                                    var name = StringArgumentType.getString(c,"name");
                                                    var size = IntegerArgumentType.getInteger(c,"size");
                                                    var co = PanoramicScreenshot.grabPanoramixScreenshot(name,size,size);
                                                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(co));
                                                    return 0;
                                                }))))
        
        );
    }
    
    public static int screenshotWithSize(CommandContext<CommandSourceStack> context){
        var width = IntegerArgumentType.getInteger(context,"width");
        var height = IntegerArgumentType.getInteger(context,"height");
        TickSequence.builder()
                .append(() -> IExtendedWindow.get().enableOverride(width,height))
                .waitTicks(1)
                .append("after game render",() -> Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), Minecraft.getInstance().getMainRenderTarget(), (co) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addMessage(co))))
                .waitTicks(1)
                .append(() -> IExtendedWindow.get().disableOverride())
                .buildInClient();
        return 0;
    }
    
    public static Component grabPanoramixScreenshot(String name, int width, int height) {
        var mc = Minecraft.getInstance();
        var window = mc.getWindow();
        var player = mc.player;
        var gameDirectory = FMLPaths.GAMEDIR.get().toFile();
        int i = window.getWidth();
        int j = window.getHeight();
        RenderTarget rendertarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
        float f = mc.player.getXRot();
        float f1 = mc.player.getYRot();
        float f2 = mc.player.xRotO;
        float f3 = mc.player.yRotO;
        mc.gameRenderer.setRenderBlockOutline(false);
        
        MutableComponent mutablecomponent;
        try {
            mc.gameRenderer.setPanoramicMode(true);
            mc.levelRenderer.graphicsChanged();
            window.setWidth(width);
            window.setHeight(height);
            
            for(int k = 0; k < 6; ++k) {
                switch (k) {
                    case 0:
                        player.setYRot(f1);
                        player.setXRot(0.0F);
                        break;
                    case 1:
                        player.setYRot((f1 + 90.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 2:
                        player.setYRot((f1 + 180.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 3:
                        player.setYRot((f1 - 90.0F) % 360.0F);
                        player.setXRot(0.0F);
                        break;
                    case 4:
                        player.setYRot(f1);
                        player.setXRot(-90.0F);
                        break;
                    case 5:
                    default:
                        player.setYRot(f1);
                        player.setXRot(90.0F);
                }
                
                player.yRotO = player.getYRot();
                player.xRotO = player.getXRot();
                rendertarget.bindWrite(true);
                mc.gameRenderer.renderLevel(1.0F, 0L, new PoseStack());
                
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedexception) {
                }
                
                Screenshot.grab(gameDirectory, name + "_" + k + ".png", rendertarget, (p_231415_) -> {
                });
            }
            
            Component component = Component.literal(gameDirectory.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((p_231426_) -> {
                return p_231426_.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, gameDirectory.getAbsolutePath()));
            });
            return Component.translatable("screenshot.success", component);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save image", (Throwable)exception);
            mutablecomponent = Component.translatable("screenshot.failure", exception.getMessage());
        } finally {
            player.setXRot(f);
            player.setYRot(f1);
            player.xRotO = f2;
            player.yRotO = f3;
            mc.gameRenderer.setRenderBlockOutline(true);
            window.setWidth(i);
            window.setHeight(j);
            rendertarget.destroyBuffers();
            mc.gameRenderer.setPanoramicMode(false);
            mc.levelRenderer.graphicsChanged();
            mc.getMainRenderTarget().bindWrite(true);
        }
        
        return mutablecomponent;
    }
    
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
    
    }
    
}
