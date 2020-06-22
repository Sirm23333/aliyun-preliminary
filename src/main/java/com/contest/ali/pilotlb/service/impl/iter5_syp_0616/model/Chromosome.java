package com.contest.ali.pilotlb.service.impl.iter5_syp_0616.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 *
 */
@AllArgsConstructor
@NoArgsConstructor
public class Chromosome implements Comparable{
    // 染色体(加载的app)
    public long[][] chromosome;
    // 适应度
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
