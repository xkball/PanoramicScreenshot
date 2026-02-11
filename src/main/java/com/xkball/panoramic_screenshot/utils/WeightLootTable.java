package com.xkball.panoramic_screenshot.utils;

import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.ToIntFunction;

public class WeightLootTable<T> {
    private final List<T> loops = new ArrayList<>();
    private final int[] weightSums;
    private int weightCount;
    
    public WeightLootTable(List<T> loots, ToIntFunction<T> weightFunction) {
        this.loops.addAll(loots);
        int[] weights = new int[loots.size()];
        weightSums = new int[loots.size()];
        weights[0] = weightFunction.applyAsInt(loots.get(0));
        weightSums[0] = weights[0];
        weightCount = weights[0];
        for (int i = 1; i < weights.length; i++) {
            weights[i] = weightFunction.applyAsInt(loots.get(i));
            weightSums[i] = weightSums[i - 1] + weights[i];
            weightCount += weights[i];
        }
    }
    
   
    public T roll(RandomSource random) {
        var i = random.nextInt(weightCount+1);
        var index = Arrays.binarySearch(weightSums, i);
        if (index < 0) {
            return loops.get(Math.min(-(index + 1), loops.size() - 1));
        }
        return loops.get(index);
    }
    
}
