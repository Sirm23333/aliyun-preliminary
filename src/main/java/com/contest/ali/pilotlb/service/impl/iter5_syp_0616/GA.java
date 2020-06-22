package com.contest.ali.pilotlb.service.impl.iter5_syp_0616;

import com.contest.ali.pilotlb.service.impl.iter5_syp_0616.model.App;
import com.contest.ali.pilotlb.service.impl.iter5_syp_0616.model.Chromosome;
import com.contest.ali.pilotlb.service.impl.iter5_syp_0616.model.Pilot;
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
    private int CHROMOSOME_LEN  ;
    // 基因长度
    private int GENE_LEN;
    // 杂交位数
    private int CRO_SIZE = 1;
    // 种群规模,选择算子选择,随机加入,精英保留
    private int CHROMOSOME_SIZE = 100;
    private int SELECT_SIZE = 90;
    private int RANDOM_SIZE = 10;
    private int RETAIN_SIZE = 10;
    // 交叉率
    private double CRO_RATE = 0.8;
    // 变异率
    private double VAR_RATE = 0.1;
    private Chromosome bestChromo = null;
    // 迭代次数
    private int ITERATIONS = 20;
    private double[] WEIGHT = {0.99,0.01};

    public GA(){
        this.apps = GlobalContain.APP_LIST_MERGE;
        CHROMOSOME_LEN = GlobalContain.PILOT_SUM;
        GENE_LEN = apps.size() / BitMapUtil.LONG_BYTE + 1;
    }

    public void GARun(){
        List<Chromosome> chromosomes = genFirst();
        List<Chromosome> father = chromosomes , child = chromosomes;
        // 开始进化
        for(int i = 0; i < ITERATIONS; ++i){
            father = child;
            child = evolve(father);
            double sum = 0;
            for(Chromosome c : child){
                sum += c.fitness;
            }
            log.info("第{}次迭代,平均适应度:{},最好适应度{}",i,sum / CHROMOSOME_SIZE, bestChromo.fitness);
        }
        buildPilotListByChromosome(bestChromo);
//        printChromosomeFeature(bestChromo);
    }

    private List<Chromosome> evolve(List<Chromosome> father) {
        // 按适应度排序
        Collections.sort(father);
        // 精英个体
        bestChromo = father.get(0);
        // 选择阶段
//        List<Chromosome> child = selectByArr(father);
//        for(int i = 0; i < RANDOM_SIZE; ++i){
//            child.add(genRandom());
//        }
        List<Chromosome> child = selectByRoulette(father,SELECT_SIZE);
        for(int i = 0; i < RANDOM_SIZE; ++i){
            child.add(genRandom());
        }
        // 杂交
        int croSize = (int) (CHROMOSOME_SIZE * CRO_RATE);
        croSize = croSize % 2 == 0 ? croSize : croSize+1;
        int[] croChromos = NumberUtil.getRandomNum(0, CHROMOSOME_SIZE, croSize);
        for(int i = 0; i < croChromos.length; i += 2){
            crossover(child.get(croChromos[i]) , child.get(croChromos[i + 1]));
        }
        // 精英保留
        Collections.sort(child);
        bestChromo = child.get(0).fitness > bestChromo.fitness ? child.get(0) : bestChromo;
        for(int i = 0; i < RETAIN_SIZE; ++i){
            child.remove(child.size() - 1);
        }
        for(int i = 0; i < RETAIN_SIZE; ++i){
            child.add(copy(bestChromo));
        }
        log.info("-------------------------------");
        return child;
    }

    /**
     * 等差选择
     */
    private List<Chromosome> selectByArr(List<Chromosome> father){
        List<Chromosome> child = new ArrayList<>();
//        for(int i : SELECT_ARR){
//            for(int j = 0; j < i; ++j){
//                child.add(copy(father.get(i)));
//            }
//        }
        return child;
    }

    /**
     * 轮赌选择
     */
    private List<Chromosome> selectByRoulette(List<Chromosome> father , int cnt){
        List<Chromosome> child = new ArrayList<>();
        double[] accumFitness = new double[father.size()]; // 累计适应度
        accumFitness[0] = father.get(0).fitness;
        for(int i = 1; i < father.size(); ++i){
            accumFitness[i] = accumFitness[i - 1] + father.get(i).fitness;
        }
        for(int i = 0; i < cnt; ++i){
            double random = NumberUtil.getRandomDouble(0.0,accumFitness[father.size()-1]);
            int j = 0;
            for(; accumFitness[j] < random; ++j);
            child.add(copy(father.get(j)));
        }
        return child;
    }
    /**
     * 染色体复制
     */
    private Chromosome copy(Chromosome chromosome){
        long[][]nch = new long[CHROMOSOME_LEN][GENE_LEN];
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            System.arraycopy(chromosome.chromosome[i],0,nch[i],0,GENE_LEN);
        }
        return new Chromosome(nch,chromosome.fitness);
    }
    /**
     * 计算染色体适应度
     */
    private double calFitness(long[][] chromosome){
        long[] mem = new long[GlobalContain.PILOT_SUM]; // 记录每个pilot的内存
        long[] con = new long[GlobalContain.PILOT_SUM]; // 记录每个pilot的连接
        for(int pilotIdx = 0; pilotIdx < CHROMOSOME_LEN; ++pilotIdx){
            long[] longs = BitMapUtil.calAppConAndMem(chromosome[pilotIdx]);
            con[pilotIdx] = longs[0];
            mem[pilotIdx] = longs[1];
        }
        double stdCon = NumberUtil.calStd(con);
        double[] memInfo = NumberUtil.calStdAndSum(mem);
        double stdMem = memInfo[0];
        double sumMem = memInfo[1];
        return GlobalContain.SERVICE_MEM / (sumMem * (stdCon + 0.01 * stdMem) );
//        return 1 / (sumMem * (stdCon * 100 + stdMem));
//        return 1 / ( stdCon * 100 +  stdMem);
    }

    /**
     * 随机产生一个个体
     */
    private Chromosome genRandom(){
        int appSize = apps.size();
        long[][] chromosome = new long[CHROMOSOME_LEN][GENE_LEN];
        for(int j = 0; j < appSize; ++j){
            BitMapUtil.set(chromosome[NumberUtil.getRandomNum(0,GlobalContain.PILOT_SUM)],j);
        }
        Chromosome ch = new Chromosome(chromosome,calFitness(chromosome));
        standardization(ch);
        return ch;
    }
    /**
     *  产生第一代种群
     */
    private List<Chromosome> genFirst(){
        List<Chromosome> firstGeneration = new ArrayList<>();
        for(int i = 0; i < CHROMOSOME_SIZE; ++i){
            firstGeneration.add(genRandom());
        }
        return firstGeneration;
    }
    /**
     * 标准化
     */
    public void standardization(Chromosome chromosome){
        Arrays.sort(chromosome.chromosome,(l1,l2)->{
            // 返回正 l1排在后面
            long tmp1,tmp2;
            for(int i =0 ; i < l1.length; ++i){
                tmp1 = l1[i];
                tmp2 = l2[i];
                while(tmp1 != 0 && tmp2 != 0){
                    if((tmp1 & 1) > (tmp2 & 1)){
                        // tmp1先出现1,tmp1要排在tmp2前面
                        return -1;
                    }else if((tmp1 & 1) < (tmp2 & 1)){
                        return 1;
                    }
                    tmp1 >>>= 1;
                    tmp2 >>>= 1;
                }
                if(tmp1 == 0 && tmp2 != 0){
                    return 1;
                }else if(tmp1 != 0 && tmp2 == 0){
                    return -1;
                }
            }
            return 0;
        });
    }
    /**
     * ch1和ch2发生交换
     */
    private void crossover(Chromosome ch1 , Chromosome ch2 ){
        // 随机产生CRO_SIZE个交叉位
        int[] ch1Randoms = NumberUtil.getRandomNum(0, CHROMOSOME_LEN, CRO_SIZE);
        int[] ch2Randoms = NumberUtil.getRandomNum(0, CHROMOSOME_LEN, CRO_SIZE);
        boolean[] ch1Select = new boolean[CHROMOSOME_LEN];
        boolean[] ch2Select = new boolean[CHROMOSOME_LEN];
        long[] tmp,tmp1 = new long[GENE_LEN],tmp2 = new long[GENE_LEN];// tmp1和tmp2分别记录ch1和ch2移到对方的app
        List<Integer> ch1Lost = new ArrayList<>() , ch2Lost = new ArrayList<>(); // 分别记录ch1和ch2缺失的app索引
        for(int i = 0; i < CRO_SIZE; ++i){
            // ch1的ch1Randoms[i]与ch2的ch2Randoms[i]交换
            tmp1 = BitMapUtil.or(tmp1 , ch1.chromosome[ch1Randoms[i]]);
            tmp2 = BitMapUtil.or(tmp2 , ch2.chromosome[ch2Randoms[i]]);
            tmp = ch1.chromosome[ch1Randoms[i]];
            ch1.chromosome[ch1Randoms[i]] = ch2.chromosome[ch2Randoms[i]];
            ch2.chromosome[ch2Randoms[i]] = tmp;
            ch1Select[ch1Randoms[i]] = true;
            ch2Select[ch2Randoms[i]] = true;
        }
        // 找ch1和ch2中缺失的app
        long tmp1Rev,tmp2Rev,tmpAnd;
        for(int i = 0 , j = 0; i < GENE_LEN ; ++i , j = 0){
            tmp1Rev = ~tmp1[i];
            tmp2Rev = ~tmp2[i];
            tmpAnd = tmp1[i] & tmp2Rev;
            while(tmpAnd != 0){
                // ch1缺失的
                if((tmpAnd & 1)  == 1){
                    ch1Lost.add(i * BitMapUtil.LONG_BYTE + j);
                }
                tmpAnd >>>= 1;
                j++;
            }
            tmpAnd = tmp2[i] & tmp1Rev;
            j = 0;
            while(tmpAnd != 0){
                // ch2缺失的
                if((tmpAnd & 1)  == 1){
                    ch2Lost.add(i * BitMapUtil.LONG_BYTE + j);
                }
                tmpAnd >>>= 1;
                j++;
            }
            tmp1[i] = tmp1Rev;
            tmp2[i] = tmp2Rev;
        }
        // 去掉ch1和ch2中的重复app
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            if(!ch1Select[i]){
                ch1.chromosome[i] = BitMapUtil.and(ch1.chromosome[i] , tmp2);
            }
            if(!ch2Select[i]){
                ch2.chromosome[i] = BitMapUtil.and(ch2.chromosome[i] , tmp1);
            }
        }
        // 基因补遗 将缺失的app重新分配到pilot
        long[][] srvDepend = new long[CHROMOSOME_LEN][GlobalContain.SERVICE_SUM / BitMapUtil.LONG_BYTE + 1];// 用来记录各个pilot加载的服务情况
        long[] mem = new long[CHROMOSOME_LEN];// 记录各个pilot加载内存
        long[] con = new long[CHROMOSOME_LEN];// 记录各个pilot连接数
        double bestValue = Double.MAX_VALUE; // 放置app时,每个pilot的损耗值的最佳值
        int bestIdx = 0; // 放置app是,最佳pilot索引
        long bestSame = 0; // 对应bestIdx的重复内存大小
        // 对ch1补遗
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            long[] longs = BitMapUtil.calAppConAndMem(ch1.chromosome[i], srvDepend[i]);
            con[i] = longs[0];
            mem[i] = longs[1];
        }
        for(int appIdx : ch1Lost){
            App app = apps.get(appIdx);
            for(int pilotIdx = 0; pilotIdx < CHROMOSOME_LEN; ++pilotIdx){
                long same = BitMapUtil.calMem(BitMapUtil.and(srvDepend[pilotIdx] , app.srvDepend));
                mem[pilotIdx] += (app.srvMem - same);
                con[pilotIdx] += app.count;
                double[] doubles = NumberUtil.calStdAndSum(mem);
                double stdCon = NumberUtil.calStd(con);
                double stdMem = doubles[0];
                double sumMem = doubles[1];
//                double totalValue = doubles[1] * (stdCon * 100 +  doubles[0]);
                double totalValue = WEIGHT[0] * stdCon * 100 + WEIGHT[1] * stdMem;
                if(bestValue > totalValue){
                    bestValue = totalValue;
                    bestIdx = pilotIdx;
                    bestSame = same;
                }
                mem[pilotIdx] -= (app.srvMem - same);
                con[pilotIdx] -= app.count;
            }
            BitMapUtil.set(ch1.chromosome[bestIdx],appIdx);
            srvDepend[bestIdx] = BitMapUtil.or(srvDepend[bestIdx] , app.srvDepend);
            con[bestIdx] += app.count;
            mem[bestIdx] += (app.srvMem - bestSame);
        }
        // 对ch2补遗
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            for(int j = 0; j < GlobalContain.SERVICE_SUM / BitMapUtil.LONG_BYTE + 1; ++j){
                srvDepend[i][j] = 0;
            }
        }
        bestValue = Double.MAX_VALUE; // 放置app时,每个pilot的损耗值的最佳值
        bestIdx = 0; // 放置app是,最佳pilot索引
        bestSame = 0; // 对应bestIdx的重复内存大小
        for(int i = 0; i < CHROMOSOME_LEN; ++i){
            long[] longs = BitMapUtil.calAppConAndMem(ch2.chromosome[i], srvDepend[i]);
            con[i] = longs[0];
            mem[i] = longs[1];
        }
        for(int appIdx : ch2Lost){
            App app = apps.get(appIdx);
            for(int pilotIdx = 0; pilotIdx < CHROMOSOME_LEN; ++pilotIdx){
                long same = BitMapUtil.calMem(BitMapUtil.and(srvDepend[pilotIdx] , app.srvDepend));
                mem[pilotIdx] += (app.srvMem - same);
                con[pilotIdx] += app.count;
                double[] doubles = NumberUtil.calStdAndSum(mem);
                double stdCon = NumberUtil.calStd(con);
                double stdMem = doubles[0];
                double sumMem = doubles[1];
//                double totalValue = doubles[1] * (stdCon * 100 +  doubles[0]);
                double totalValue = WEIGHT[0] * stdCon * 100 + WEIGHT[1] * stdMem;
                if(bestValue > totalValue){
                    bestValue = totalValue;
                    bestIdx = pilotIdx;
                    bestSame = same;
                }
                mem[pilotIdx] -= (app.srvMem - same);
                con[pilotIdx] -= app.count;
            }
            BitMapUtil.set(ch2.chromosome[bestIdx],appIdx);
            srvDepend[bestIdx] = BitMapUtil.or(srvDepend[bestIdx] , app.srvDepend);
            con[bestIdx] += app.count;
            mem[bestIdx] += (app.srvMem - bestSame);
        }
        ch1.fitness = calFitness(ch1.chromosome);
        ch2.fitness = calFitness(ch2.chromosome);
        standardization(ch1);
        standardization(ch2);
    }
    private void buildPilotListByChromosome(Chromosome chromosome){
        for(int i = 0; i < chromosome.chromosome.length; ++i){
            Pilot pilot = GlobalContain.PILOT_LIST.get(i);
            long[] appsMap = chromosome.chromosome[i];
            for(int j = 0 , k = 0; j < appsMap.length; ++j , k = 0){
                long tmp = appsMap[j];
                while(tmp != 0){
                    if((tmp & 1) == 1){
                        pilot.apps.add(apps.get(j * BitMapUtil.LONG_BYTE + k));
                    }
                    k++;
                    tmp >>>= 1;
                }
            }
        }
    }

    private void printChromosomeFeature(Chromosome chromosome){
        // 每一个pilot的加载内存
        for(int i = 0; i < chromosome.chromosome.length; i++){
            long[] longs = BitMapUtil.calAppConAndMem(chromosome.chromosome[i]);
            log.info("pilot{} 连接{} 加载内存{}",i , longs[0] , longs[1] * 0.01);
        }
    }
}
class first implements Runnable{
    @Override
    public void run() {

    }
}