package com.xkball.panoramic_screenshot;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xkball.panoramic_screenshot.utils.ThrowableSupplier;
import com.xkball.panoramic_screenshot.utils.VanillaUtils;
import com.xkball.panoramic_screenshot.utils.WeightLootTable;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class SkyboxLoader extends SimplePreparableReloadListener<List<Pair<ResourceLocation,SkyboxLoader.SkyboxData>>> {
    
    public static SkyboxLoader INSTANCE = new SkyboxLoader();
    
    @Override
    protected List<Pair<ResourceLocation,SkyboxLoader.SkyboxData>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        var resources = resourceManager.listResources("textures/gui/title/background",rl -> rl.getPath().endsWith(".json"));
        return resources.entrySet().stream()
                .map( (entry) -> Pair.of(entry.getKey(), ThrowableSupplier.getOrThrow(() -> VanillaUtils.readJsonFromResource(entry.getValue()))))
                .map((pair) -> Pair.of(pair.getFirst(),SkyboxData.CODEC.decode(JsonOps.INSTANCE, pair.getSecond())))
                .map(pair -> Pair.of(pair.getFirst(),pair.getSecond().getOrThrow(false,(s) -> {}).getFirst()))
                .toList();
    }
    
    @Override
    @SuppressWarnings("removal")
    protected void apply(List<Pair<ResourceLocation,SkyboxLoader.SkyboxData>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if(object.isEmpty()) return;
        var loot = new WeightLootTable<>(object, o -> o.getSecond().weight);
        var data = loot.roll(RandomSource.create());
        var rl = data.getFirst();
        TitleScreen.CUBE_MAP = new CubeMap(new ResourceLocation(rl.getNamespace(),rl.getPath().substring(0,rl.getPath().lastIndexOf('/')+1) + data.getSecond().name));
    }
    
    public record SkyboxData(String name, int weight){
        
        public static final Codec<SkyboxData> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                Codec.STRING.fieldOf("name").forGetter(SkyboxData::name),
                Codec.INT.fieldOf("weight").forGetter(SkyboxData::weight)
        ).apply(ins, SkyboxData::new));
        
    }
    
    @SubscribeEvent
    public static void onDataPackReload(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(INSTANCE);
    }
}
