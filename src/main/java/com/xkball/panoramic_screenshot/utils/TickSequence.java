package com.xkball.panoramic_screenshot.utils;

import com.mojang.datafixers.util.Pair;

import java.util.LinkedList;
import java.util.Queue;
import java.util.function.BooleanSupplier;

public class TickSequence {

    //todo 改成Pair<TickPhase<T>, BooleanSupplier>
    private final Queue<Pair<String, BooleanSupplier>> tickSequence;
    
    private TickSequence(Queue<Pair<String, BooleanSupplier>> tickSequence) {
        this.tickSequence = tickSequence;
    }
    
    public void accept(String tickPhase){
        if(this.isEmpty()) return;
        if (tickSequence.peek().getFirst().equals(tickPhase)) {
            var task = tickSequence.peek();
            if(task.getSecond().getAsBoolean()){
                tickSequence.poll();
            }
        }
    }
    
    public boolean isEmpty(){
        return tickSequence.isEmpty();
    }
    
    public static Builder builder(){
        return new Builder();
    }
    
    public static class Builder{
        private final Queue<Pair<String,BooleanSupplier>> tickSequence = new LinkedList<>();
        
        public Builder append(String tickPhase, Runnable runnable){
            tickSequence.add(Pair.of(tickPhase, () -> {
                runnable.run();
                return true;
            }
            ));
            return this;
        }
        
        public Builder append(Runnable runnable){
            return this.append("tick pre",runnable);
        }
        
        public Builder repeat(String tickPhase, Runnable runnable, int count){
            for (int i = 0; i < count; i++) {
                this.append(tickPhase,runnable);
            }
            return this;
        }
        
        public Builder repeatUntil(String tickPhase, BooleanSupplier task){
            this.tickSequence.add(Pair.of(tickPhase,task));
            return this;
        }
        
        public Builder waitTicks(int i){
            for (int j = 0; j < i; j++) {
                tickSequence.add(Pair.of("tick pre",() -> true));
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
