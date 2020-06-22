package com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class Chromosome implements Comparable{
    public int[] chromosome;
    public double fitness;

    @Override
    public int compareTo(Object o) {
        Chromosome c = (Chromosome)o;
        if(c.fitness > this.fitness){
            return 1;
        }else if(c.fitness < this.fitness){
            return -1;
        }else {
            return 0;
        }
    }
}
