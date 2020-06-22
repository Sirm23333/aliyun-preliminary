package com.contest.ali.pilotlb.service.impl.iter4_syp_0614;

import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.App;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Chromosome;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Pilot;
import com.contest.ali.pilotlb.service.impl.iter4_syp_0614.model.Service;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 *
 */
@Slf4j
public class GA {
    // 带分配的app
    private List<App> apps;
    // 染色体长度
    private int CHROMOSOME_LEN ;
    // 杂交位数
    private int CRO_SIZE = 1;
    // 种群规模
    private int CHROMOSOME_SIZE = 30;
    // 轮赌选择的染色体个数
    private int CHOICE_SIZE = 28;
    // 精英策略保留的染色体个数
    private int RETAIN = 2;
    // 交叉率
    private double CRO_RATE = 0.8;
    // 变异率
    private double VAR_RATE = 0.1;
    // 迭代次数
    private int ITERATIONS = 20;

    private Chromosome bestChromo = null;

    public  GA(List<App> apps){
        this.apps = apps;
        CHROMOSOME_LEN = apps.size();
    }

    public int[] GARun(){

        // 生成第一代
        List<Chromosome> chromosomes = genFirst();
        List<Chromosome> father = chromosomes , child = chromosomes;

        // 开始进化
        for(int i = 0; i < ITERATIONS; ++i){
            if(i == 10){
                VAR_RATE = 0.3;
                CRO_RATE = 0.6;
            }
            father = child;
            child = evolve(father);
            double sum = 0;
            for(Chromosome c : child){
                sum += c.fitness;
            }
            log.info("第{}次迭代,fitness avg={},最优个体fitness={}",i , sum / CHROMOSOME_SIZE , bestChromo.fitness);
            printAna(bestChromo.chromosome , apps);
        }
        return bestChromo.chromosome;
    }


    private List<Chromosome> evolve(List<Chromosome> father ) {
        List<Chromosome> child = new ArrayList<>();
        // 按适应度排序
        Collections.sort(father);
        // 精英个体
        bestChromo = father.get(0);
        // 轮赌选择要保留的染色体
        double[] accumFitness = new double[father.size()]; // 累计适应度
        accumFitness[0] = father.get(0).fitness;
        for(int i = 1; i < father.size(); ++i){
            accumFitness[i] = accumFitness[i - 1] + father.get(i).fitness;
        }
        for(int i = 0; i < CHOICE_SIZE; ++i){
            double random = NumberUtil.getRandomDouble(0.0,accumFitness[father.size()-1]);
            int j = 0;
            for(; accumFitness[j] < random; ++j);
            child.add(copy(father.get(j)));
        }
        // 交叉
        int[] croChromos = NumberUtil.getRandomNum(0, CHOICE_SIZE, (int) (CHOICE_SIZE * CRO_RATE));
        for(int i = 0; i < croChromos.length; i += 2){
            crossover(child.get(croChromos[i]) , child.get(croChromos[i + 1]));
        }
        // 变异
        for(Chromosome chromosome : child){
            if(Math.random() < VAR_RATE){
                variation(chromosome);
            }
        }
        // 精英保留
        for(int i = 0; i < RETAIN; ++i){
            child.add(copy(bestChromo));
        }
        for(Chromosome chromosome : child){
            log.info("child : {}" , chromosome.chromosome);
        }
        log.info("-------------------------------");
        return child;

    }

    /**
     *  产生第一代种群
     */
    private List<Chromosome> genFirst(){
        List<Chromosome> firstGeneration = new ArrayList<>();
        for(int i = 0; i < CHROMOSOME_SIZE; ++i){
            int[] chromosome = new int[CHROMOSOME_LEN];
            for(int j = 0; j < CHROMOSOME_LEN; ++j){
                chromosome[j] = NumberUtil.getRandomNum(0,GlobalContain.PILOT_SUM);
            }
            Chromosome ch = new Chromosome(chromosome,calFitness(chromosome));
            standardization(ch);
            firstGeneration.add(ch);
        }
        return firstGeneration;
    }

    private Chromosome copy(Chromosome chromosome){
        Chromosome copyChromo = new Chromosome(new int[CHROMOSOME_LEN],chromosome.fitness);
        System.arraycopy(chromosome.chromosome,0,copyChromo.chromosome,0,CHROMOSOME_LEN);
        return copyChromo;
    }

    /**
     * 计算染色体适应度
     */
    private double calFitness(int[] chromosome){
        // 每个pilot加载的服务集合
        Set<Service>[] srvs = new HashSet[GlobalContain.PILOT_SUM];
        // 每个pilot的加载内存
        double[] mems = new double[GlobalContain.PILOT_SUM];
        for(int i = 0; i < srvs.length; ++i){
            srvs[i] = new HashSet<>();
        }
        int pilotIdx;
        for(int appIdx = 0 ; appIdx < CHROMOSOME_LEN; appIdx++){
            pilotIdx = chromosome[appIdx];
            if(pilotIdx >= 0){
                srvs[pilotIdx].addAll(apps.get(appIdx).services);
            }
        }
        int i = 0;
        double totalMem = 0;
        for(Set<Service> srv : srvs){
            for(Service s : srv){
                mems[i] += s.count * 0.01;
                totalMem += s.count * 0.01;
            }
            i++;
        }
        return 1 / (NumberUtil.calStd(mems));
    }

    /**
     * ch1和ch2发生交换
     */
    private void crossover(Chromosome ch1 , Chromosome ch2 ){

        // 产生杂交位及杂交基因
        int s1 = NumberUtil.getRandomNum(0,CHROMOSOME_LEN);
        Set<Integer> ch1Gene = new HashSet<>(); // ch1选中的基因
        for(int i = 0; i < CRO_SIZE; ++i){
            ch1Gene.add(ch1.chromosome[(s1 + i) % CHROMOSOME_LEN]);
        }
        int J = ch1Gene.size();
//        int s2 = NumberUtil.getRandomNum(Math.max(0 , s1 - J),Math.min(CHROMOSOME_LEN , s1 + J));
        int s2 = NumberUtil.getRandomNum(0,CHROMOSOME_LEN);
        log.info("s1={},s2={}",s1,s2);
        Set<Integer> ch2Gene = new HashSet<>(); // ch2选中的基因
        for(int i = 0; i < CRO_SIZE; ++i){
            ch2Gene.add(ch2.chromosome[(s2 + i) % CHROMOSOME_LEN]);
        }

        // 交叉插入,复制同组,复制源父代
        int[] ch1New = new int[CHROMOSOME_LEN];
        int[] ch2New = new int[CHROMOSOME_LEN];
        for(int i = 0 ; i < CHROMOSOME_LEN; ++i){
            if(ch1Gene.contains(ch1.chromosome[i])){
                ch2New[i] = ch1.chromosome[i];
            }else if(!ch1Gene.contains(ch2.chromosome[i])){
                ch2New[i] = ch2.chromosome[i];
            }else{
                ch2New[i] = -1;
            }
            if(ch2Gene.contains(ch2.chromosome[i])){
                ch1New[i] = ch2.chromosome[i];
            }else if(!ch2Gene.contains(ch1.chromosome[i])){
                ch1New[i] = ch1.chromosome[i];
            }else{
                ch1New[i] = -1;
            }
        }

        // 基因补遗
        int bestPilotIdx = 0;
        double maxFitness ,tmpFitness;
        for(int appIdx = 0;  appIdx < ch1New.length; ++appIdx){
            if(ch1New[appIdx] < 0){
                maxFitness = 0;
                for(int j = 0; j < GlobalContain.PILOT_SUM; ++j){
                    ch1New[appIdx] = j;
                    tmpFitness = calFitness(ch1New);
                    if(maxFitness < tmpFitness){
                        maxFitness = tmpFitness;
                        bestPilotIdx = j;
                    }
                }
                ch1New[appIdx] = bestPilotIdx;
            }
        }
        for(int appIdx = 0;  appIdx < ch2New.length; ++appIdx){
            if(ch2New[appIdx] < 0){
                maxFitness = 0;
                for(int j = 0; j < GlobalContain.PILOT_SUM; ++j){
                    ch2New[appIdx] = j;
                    tmpFitness = calFitness(ch2New);
                    if(maxFitness < tmpFitness){
                        maxFitness = tmpFitness;
                        bestPilotIdx = j;
                    }
                }
                ch2New[appIdx] = bestPilotIdx;
            }
        }

        // 交叉结束
        ch1.chromosome = ch1New;
        ch2.chromosome = ch2New;
        ch1.fitness = calFitness(ch1New);
        ch2.fitness = calFitness(ch2New);
        standardization(ch1);
        standardization(ch2);
    }

    /**
     * ch发生变异
     */
    private void variation(Chromosome chromosome){
        int maxPilotIdx = -1; // 加载内存最大的pilot
//        long maxMem = 0;
//        // 每个pilot加载的服务集合
//        Set<Service>[] srvs = new HashSet[GlobalContain.PILOT_SUM];
//        // 每个pilot的加载内存
//        long[] mems = new long[GlobalContain.PILOT_SUM];
//        for(int i = 0; i < srvs.length; ++i){
//            srvs[i] = new HashSet<>();
//        }
//        int pilotIdx;
//        for(int appIdx = 0 ; appIdx < CHROMOSOME_LEN; appIdx++){
//            pilotIdx = chromosome.chromosome[appIdx];
//            if(pilotIdx > 0){
//                srvs[pilotIdx].addAll(apps.get(appIdx).services);
//            }
//        }
//        int i = 0;
//        for(Set<Service> srv : srvs){
//            for(Service s : srv){
//                mems[i] += s.count;
//            }
//            if(maxMem < mems[i]){
//                maxMem = mems[i];
//                maxPilotIdx = i;
//            }
//            i++;
//        }
        maxPilotIdx = NumberUtil.getRandomNum(0,GlobalContain.PILOT_SUM);
        for(int k = 0; k < CHROMOSOME_LEN; ++k){
            if(chromosome.chromosome[k] == maxPilotIdx){
                chromosome.chromosome[k] = -1;
            }
        }
        double maxFitness , tmpFitness;
        int bestPilotIdx = 0;
        for(int appIdx = 0; appIdx < CHROMOSOME_LEN; ++appIdx){
            if(chromosome.chromosome[appIdx] < 0){
                maxFitness = 0;
                for(int j = 0; j < GlobalContain.PILOT_SUM; ++j){
                    chromosome.chromosome[appIdx] = j;
                    tmpFitness = calFitness(chromosome.chromosome);
                    if(maxFitness < tmpFitness){
                        maxFitness = tmpFitness;
                        bestPilotIdx = j;
                    }
                }
//                bestPilotIdx = NumberUtil.getRandomNum(0,GlobalContain.PILOT_SUM);
                chromosome.chromosome[appIdx] = bestPilotIdx;
            }
        }
        chromosome.fitness = calFitness(chromosome.chromosome);
        standardization(chromosome);
    }

    /**
     * 标准化
     * 3 2 1 3 5 5 4 1
     * 0 1 2 0 3 3 4 2
     */
    private void standardization(Chromosome chromosome){
        int[] newPilotIdx = new int[6];
        for (int i = 0; i < newPilotIdx.length; ++i){
                newPilotIdx[i] = -1;
        }
        int tmp = -1;
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            if(newPilotIdx[chromosome.chromosome[i]] < 0){
                tmp++;
                newPilotIdx[chromosome.chromosome[i]] = tmp;
            }
        }
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            chromosome.chromosome[i] = newPilotIdx[chromosome.chromosome[i]];
        }
    }

    public static void printAna(int[] chromosome , List<App> apps){
        List<Pilot> pilots = new ArrayList<>();
        for(int i = 0; i < GlobalContain.PILOT_SUM; ++i){
            pilots.add(new Pilot(i,"pilot"+i,new ArrayList<>(),new HashSet<>()));
        }
        for(int i = 0 ; i < chromosome.length; ++i){
            pilots.get(chromosome[i]).addApp(apps.get(i));
        }
        // 实际加载内存的和
        double memSum = 0;
        double  minMem = 0;
        // 加载内存的平方和
        double sqMemSum = 0;
        Set<Service> has = new HashSet<>();
        for(Pilot pilot : pilots){
            double tmp = 0;
            for(Service service : pilot.services){
                tmp += service.count * 0.01;
                if(has.add(service)){
                    minMem += service.count * 0.01;
                }
            }
            memSum += tmp;
            sqMemSum += tmp * tmp;
        }
        log.info("memSum = {} , minMem = {} , memSum/minMem = {}" , memSum , minMem , memSum / minMem);
        log.info("std={}", Math.sqrt((sqMemSum / GlobalContain.PILOT_SUM) - (memSum / GlobalContain.PILOT_SUM) * (memSum / GlobalContain.PILOT_SUM)));
    }

}
