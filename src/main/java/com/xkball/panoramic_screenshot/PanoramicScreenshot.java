package com.xkball.panoramic_screenshot;

import com.mojang.brigadier.arguments.FloatArgumentType;
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
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.server.command.EnumArgument;
import org.slf4j.Logger;


@Mod(value = PanoramicScreenshot.MODID,dist = Dist.CLIENT)
@EventBusSubscriber(value = Dist.CLIENT)
public class PanoramicScreenshot {
    public static final String MODID = "panoramic_screenshot";
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final GifHelper globalGifHelper = new GifHelper();
    public static boolean takingSkyBox = false;

    public PanoramicScreenshot(ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }
    
    @SubscribeEvent
    public static void onRegClientCommand(RegisterClientCommandsEvent event){
            event.getDispatcher().register(
                    Commands.literal("screenshot")
                            .then(Commands.literal("normal")
                                    .executes(c -> {
                                        Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), Minecraft.getInstance().getMainRenderTarget(), (co) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addClientSystemMessage(co)));
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
                                        PanoramicScreenshot.screenshotSkyBox("skybox",2048, 2);
                                        return 0;
                                    })
                                    .then(Commands.argument("name", StringArgumentType.string())
                                            .then(Commands.argument("size", IntegerArgumentType.integer(1,16384))
                                                    .then(Commands.argument("frame_delay", IntegerArgumentType.integer(0,1000))
                                                            .executes((c) ->{
                                                                var name = StringArgumentType.getString(c,"name");
                                                                var size = IntegerArgumentType.getInteger(c,"size");
                                                                var delay =  IntegerArgumentType.getInteger(c,"frame_delay");
                                                                PanoramicScreenshot.screenshotSkyBox(name,size,delay);
                                                                return 0;
                                                            })))))
                            .then(Commands.literal("gif")
                                    .executes((c) -> {
                                        new GifHelper().start();
                                        return 0;
                                    })
                                    .then(Commands.literal("fix_time")
                                            .then(Commands.argument("time_second", FloatArgumentType.floatArg(0.01f))
                                                    .then(Commands.argument("frame_rate",IntegerArgumentType.integer(1))
                                                            .executes((c) -> {
                                                                var timeSec = FloatArgumentType.getFloat(c,"time_second");
                                                                var frameRate = IntegerArgumentType.getInteger(c,"frame_rate");
                                                                var helper = new GifHelper();
                                                                helper.timeSec = timeSec;
                                                                helper.frameRate = frameRate;
                                                                helper.start();
                                                                return 0;
                                                            }))))
                                    .then(Commands.literal("start")
                                            .then(Commands.argument("frame_rate",IntegerArgumentType.integer(1))
                                                    .executes((c) -> {
                                                        var frameRate = IntegerArgumentType.getInteger(c,"frame_rate");
                                                        globalGifHelper.timeSec = 107986702;
                                                        globalGifHelper.frameRate = frameRate;
                                                        globalGifHelper.start();
                                                        return 0;
                                                    })))
                                    .then(Commands.literal("end")
                                            .executes((c) -> {
                                                globalGifHelper.finished = true;
                                                return 0;
                                            })))
            
            );
    }
    
    public static int screenshotWithSize(CommandContext<CommandSourceStack> context){
        var width = IntegerArgumentType.getInteger(context,"width");
        var height = IntegerArgumentType.getInteger(context,"height");
        TickSequence.builder()
                .append(() -> IExtendedWindow.get().enableOverride(width,height))
                .waitTicks(1)
                .append("after game render",() -> Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), Minecraft.getInstance().getMainRenderTarget(), (co) -> Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addClientSystemMessage(co))))
                .waitTicks(1)
                .append(() -> IExtendedWindow.get().disableOverride())
                .buildInClient();
        return 0;
    }
    
    public static void screenshotSkyBox(String name, int size, int delay){
        var mc = Minecraft.getInstance();
        assert mc.player != null;
        float f = mc.player.getXRot();
        float f1 = mc.player.getYRot();
        float f2 = mc.player.xRotO;
        float f3 = mc.player.yRotO;
        var hideGui = mc.options.hideGui;
        mc.options.hideGui = true;
        takingSkyBox = true;
        TickSequence.builder()
                .waitTicks(1)
                .append(() -> {
                    IExtendedWindow.get().enableOverride(size,size);
                    mc.gameRenderer.setRenderBlockOutline(false);
                })
                .append(() -> setPlayerRot(0,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_0.png", Minecraft.getInstance().getMainRenderTarget(), 1, (p_231415_) -> {}))
                .append(() -> setPlayerRot(1,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_1.png", Minecraft.getInstance().getMainRenderTarget(), 1,  (p_231415_) -> {}))
                .append(() -> setPlayerRot(2,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_2.png", Minecraft.getInstance().getMainRenderTarget(), 1,  (p_231415_) -> {}))
                .append(() -> setPlayerRot(3,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_3.png", Minecraft.getInstance().getMainRenderTarget(), 1,  (p_231415_) -> {}))
                .append(() -> setPlayerRot(4,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_4.png", Minecraft.getInstance().getMainRenderTarget(), 1,  (p_231415_) -> {}))
                .append(() -> setPlayerRot(5,f1))
                .waitTicks(delay)
                .append("after game render", () ->  Screenshot.grab(FMLPaths.GAMEDIR.get().toFile(), name + "_5.png", Minecraft.getInstance().getMainRenderTarget(), 1,  (p_231415_) -> {}))
                .append(() -> {
                    var player = mc.player;
                    player.setXRot(f);
                    player.setYRot(f1);
                    player.xRotO = f2;
                    player.yRotO = f3;
                    mc.options.hideGui = hideGui;
                    mc.gameRenderer.setRenderBlockOutline(true);
                    var gameDirectory = FMLPaths.GAMEDIR.get().resolve("screenshots").toFile();
                    var message = Component.literal(gameDirectory.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle((p_231426_) -> p_231426_.withClickEvent(new ClickEvent.OpenFile(gameDirectory.getAbsolutePath())));
                    Minecraft.getInstance().execute(() -> Minecraft.getInstance().gui.getChat().addClientSystemMessage(message));
                    takingSkyBox = false;
                })
                .append(() -> IExtendedWindow.get().disableOverride())
                .buildInClient();
    }
    
    public static void setPlayerRot(int index, float f1){
        var player = Minecraft.getInstance().player;
        assert player != null;
        switch (index) {
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
    }
    
    @SubscribeEvent
    public static void onGetFov(ViewportEvent.ComputeFov event){
        if(takingSkyBox) event.setFOV(90);
    }
}
