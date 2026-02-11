package com.xkball.panoramic_screenshot.utils;

import com.mojang.datafixers.util.Pair;

import java.util.LinkedList;
import java.util.Queue;

public class TickSequence {

    //todo 改成Pair<TickPhase<T>, BooleanSupplier>
    private final Queue<Pair<String,Runnable>> tickSequence;
    
    private TickSequence(Queue<Pair<String, Runnable>> tickSequence) {
        this.tickSequence = tickSequence;
    }
    
    public void accept(String tickPhase){
        if(this.isEmpty()) return;
        if (tickSequence.peek().getFirst().equals(tickPhase)) {
            tickSequence.poll().getSecond().run();
        }
    }
    
    public boolean isEmpty(){
        return tickSequence.isEmpty();
    }
    
    public static Builder builder(){
        return new Builder();
    }
    
    public static class Builder{
        private final Queue<Pair<String,Runnable>> tickSequence = new LinkedList<>();
        
        public Builder append(String tickPhase, Runnable runnable){
            tickSequence.add(Pair.of(tickPhase, runnable));
            return this;
        }
        
        public Builder append(Runnable runnable){
            tickSequence.add(Pair.of("tick pre", runnable));
            return this;
        }
        
        public Builder waitTicks(int i){
            for (int j = 0; j < i; j++) {
                tickSequence.add(Pair.of("tick pre",() -> {}));
            }
            return this;
        }
        
        public TickSequence buildInClient(){
            var result = new TickSequence(tickSequence);
            TickSequenceHandler.CLIENT_HANDLER.submit(result);
            return result;
        }
    }
}
