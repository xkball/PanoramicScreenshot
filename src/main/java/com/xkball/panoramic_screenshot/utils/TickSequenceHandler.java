package com.xkball.panoramic_screenshot.utils;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.ArrayList;
import java.util.List;

@EventBusSubscriber(Dist.CLIENT)
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
    public static void onTick(ClientTickEvent.Pre event){
        CLIENT_HANDLER.accept("tick pre");
    }
}
