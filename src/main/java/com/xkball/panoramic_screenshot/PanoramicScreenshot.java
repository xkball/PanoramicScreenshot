package com.xkball.panoramic_screenshot;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.logging.LogUtils;
import com.xkball.panoramic_screenshot.utils.TickSequence;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.renderer.PanoramicScreenshotParameters;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.joml.Vector3f;
import org.slf4j.Logger;


@Mod(value = PanoramicScreenshot.MODID,dist = Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class PanoramicScreenshot {
    public static final String MODID = "panoramic_screenshot";
    private static final Logger LOGGER = LogUtils.getLogger();

    public PanoramicScreenshot(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
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
        int l = window.getWidth();
        int i1 = window.getHeight();
        RenderTarget rendertarget = mc.getMainRenderTarget();
        float f = player.getXRot();
        float f1 = player.getYRot();
        float f2 = player.xRotO;
        float f3 = player.yRotO;
        mc.gameRenderer.setRenderBlockOutline(false);
        
        MutableComponent mutablecomponent;
        try {
            mc.gameRenderer
                    .setPanoramicScreenshotParameters(new PanoramicScreenshotParameters(new Vector3f(mc.gameRenderer.getMainCamera().forwardVector())));
            window.setWidth(width);
            window.setHeight(height);
            rendertarget.resize(width,height);
            
            for (int j1 = 0; j1 < 6; j1++) {
                switch (j1) {
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
                mc.gameRenderer.updateCamera(DeltaTracker.ONE);
                mc.gameRenderer.renderLevel(DeltaTracker.ONE);
                
                try {
                    Thread.sleep(10L);
                } catch (InterruptedException interruptedexception) {
                }
                
                Screenshot.grab(gameDirectory, name + "_" + j1 + ".png", rendertarget, 1, p_231415_ -> {});
            }
            
            Component component = Component.literal(gameDirectory.getName())
                    .withStyle(ChatFormatting.UNDERLINE)
                    .withStyle(p_392492_ -> p_392492_.withClickEvent(new ClickEvent.OpenFile(gameDirectory.getAbsoluteFile())));
            return Component.translatable("screenshot.success", component);
        } catch (Exception exception) {
            LOGGER.error("Couldn't save image", exception);
            mutablecomponent = Component.translatable("screenshot.failure", exception.getMessage());
        } finally {
            player.setXRot(f);
            player.setYRot(f1);
            player.xRotO = f2;
            player.yRotO = f3;
            mc.gameRenderer.setRenderBlockOutline(true);
            window.setWidth(l);
            window.setHeight(i1);
            rendertarget.resize(l, i1);
            mc.gameRenderer.setPanoramicScreenshotParameters(null);
        }
        
        return mutablecomponent;
    }
    
}
