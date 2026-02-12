package com.xkball.panoramic_screenshot;

import com.mojang.datafixers.util.Pair;
import com.mojang.logging.annotations.MethodsReturnNonnullByDefault;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.xkball.panoramic_screenshot.utils.ThrowableSupplier;
import com.xkball.panoramic_screenshot.utils.VanillaUtils;
import com.xkball.panoramic_screenshot.utils.WeightLootTable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.CubeMap;
import net.minecraft.client.renderer.PanoramaRenderer;
import net.minecraft.client.renderer.texture.CubeMapTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.RandomSource;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.AddClientReloadListenersEvent;


import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
public class SkyboxLoader extends SimplePreparableReloadListener<List<Pair<Identifier,SkyboxLoader.SkyboxData>>> {
    
    public static SkyboxLoader INSTANCE = new SkyboxLoader();
    
    @Override
    protected List<Pair<Identifier,SkyboxLoader.SkyboxData>> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        var resources = resourceManager.listResources("textures/gui/title/background",rl -> rl.getPath().endsWith(".json"));
        return resources.entrySet().stream()
                .map( (entry) -> Pair.of(entry.getKey(), ThrowableSupplier.getOrThrow(() -> VanillaUtils.readJsonFromResource(entry.getValue()))))
                .map((pair) -> Pair.of(pair.getFirst(),SkyboxData.CODEC.decode(JsonOps.INSTANCE, pair.getSecond())))
                .map(pair -> Pair.of(pair.getFirst(),pair.getSecond().getOrThrow().getFirst()))
                .toList();
    }
    
    @Override
    protected void apply(List<Pair<Identifier,SkyboxLoader.SkyboxData>> object, ResourceManager resourceManager, ProfilerFiller profiler) {
        if(object.isEmpty()) return;
        var loot = new WeightLootTable<>(object, o -> o.getSecond().weight);
        var data = loot.roll(RandomSource.create());
        var rl = data.getFirst();
        var cubeMapRl = Identifier.fromNamespaceAndPath(rl.getNamespace(),rl.getPath().substring(0,rl.getPath().lastIndexOf('/')+1) + data.getSecond().name);
        var cubeMap = new CubeMap(cubeMapRl);
        Minecraft.getInstance().getTextureManager().registerAndLoad(cubeMapRl,new CubeMapTexture(cubeMapRl));
        Minecraft.getInstance().gameRenderer.cubeMap.close();
        Minecraft.getInstance().gameRenderer.cubeMap = cubeMap;
        Minecraft.getInstance().gameRenderer.panorama = new PanoramaRenderer(cubeMap);
    }
    
    public record SkyboxData(String name, int weight){
        
        public static final Codec<SkyboxData> CODEC = RecordCodecBuilder.create(ins -> ins.group(
                Codec.STRING.fieldOf("name").forGetter(SkyboxData::name),
                Codec.INT.fieldOf("weight").forGetter(SkyboxData::weight)
        ).apply(ins, SkyboxData::new));
        
    }
    
    @SubscribeEvent
    public static void onDataPackReload(AddClientReloadListenersEvent event) {
        event.addListener(VanillaUtils.modRL("skybox"),INSTANCE);
    }
}
