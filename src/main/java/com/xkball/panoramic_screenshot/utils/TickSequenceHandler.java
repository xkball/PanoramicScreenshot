package com.xkball.panoramic_screenshot.utils;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class TickSequenceHandler {
    
    public static TickSequenceHandler CLIENT_HANDLER = new TickSequenceHandler();
    
    private List<TickSequence> tickSequences = new ArrayList<>();
    
    public void submit(TickSequence tickSequence){
        tickSequences.add(tickSequence);
    }
    
    public void accept(String tickPhase){
        tickSequences.forEach(tickSequence -> tickSequence.accept(tickPhase));
        tickSequences.removeIf(TickSequence::isEmpty);
    }
    
    @SubscribeEvent
    public static void onTick(TickEvent.ClientTickEvent event){
        if(event.phase == TickEvent.Phase.START){
            CLIENT_HANDLER.accept("tick pre");
        }
    }
}
