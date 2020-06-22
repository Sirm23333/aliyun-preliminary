package com.contest.ali.pilotlb.service.impl.iter3_syp_0611;
import com.alibaba.fastjson.JSONObject;
import com.contest.ali.pilotlb.constant.GlobalConstant;
import com.contest.ali.pilotlb.constant.ObjectFactory;
import com.contest.ali.pilotlb.service.DataHandler;
import com.contest.ali.pilotlb.service.MainRun;
import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.*;
import com.contest.ali.pilotlb.service.impl.iter3_syp_0611.model.App;
import com.contest.ali.pilotlb.util.NumberUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class Iter3MainRunImpl_1 implements MainRun {
    // 将前idxRatio的app相似度大于similarity的合并
    private double idxRange = 0.25 ;
    private double similarity = 0.9;
    // GA种群规模
    private int chromosomeSum = 100;
    // 染色体长度
    private int chromosomeLength = 0;
    // 迭代次数
    private int iterations = 100;
    // 交叉率
    private double crossoverRate = 0.8;
    // 变异率
    private double variationRate = 0.2;


    @Override
    public Map<String, List<String>> stage1Run(List<String> pilotNames, String dataPath) {
        Map<String , List<String>> result = new HashMap<>();
        for(String pilotName : GlobalContain.PILOT_NAME){
            result.put(pilotName,new ArrayList<>());
        }
        // 读data.json文件并初始化数据
        initData(pilotNames,dataPath);
        // 将app按照依赖服务数量排序
        sortAppByDependencies();
        // 将前idxRatio的app相似度大于similarity的合并
        List<App> mergedApps = mergeAppBySimilarity(idxRange, similarity);
        // 将mergedApps通过GA算法 分配到pilot中
        GARun(mergedApps);
        // 将剩余的app分配到pilot中
        disRestApp((int)(GlobalContain.APP_SUM * idxRange) + 1, result);
        return result;
    }

    /**
     * 取前idxRatio的app,并将相似度大于similarity的app合并,返回一个合并后的app列表
     * 这个app列表中的元素仍为App对象,但name属性为app1-name;app2-name的格式
     */
    List<App>  mergeAppBySimilarity(double idxRatio , double similarity) {
        List<App> mergedAppList = new ArrayList<>();
        int length = (int) (GlobalContain.APP_SUM * idxRatio);
        // 记录app是否已经合并
        boolean[] merged = new boolean[length];
        Queue<Integer> mergeQueue = new ArrayDeque<>();
        App app1 , app2;
        int same; // 统计某一个比较中的重复的service的数量
        for(int i = 0; i < length; ++i){
            if(!merged[i]){
                StringBuffer mergedAppName = new StringBuffer();
                int mergedAppCount = 0;
                Set<Service> mergedAppService = new HashSet<>();
                mergeQueue.add(i);
                merged[i] = true;
                while(!mergeQueue.isEmpty()){
                    int tmp = mergeQueue.poll();
                    app1 = GlobalContain.APP_LIST.get(tmp);
                    mergedAppName.append(app1.name+";");
                    mergedAppCount += app1.count;
                    mergedAppService.addAll(app1.services);
                    for(int j = i + 1; j < length; ++j){
                        if(!merged[j]){
                            same = 0;
                            app2 = GlobalContain.APP_LIST.get(j);
                            for(Service srv : app2.services){
                                if(app1.services.contains(srv)){
                                    same++;
                                }
                            }
                            if((double) same / (app1.services.size() + app2.services.size() - same) > similarity){
                                mergeQueue.add(j);
                                merged[j] = true;
                            }
                        }
                    }
                }
                App app = new App(mergedAppName.toString(),mergedAppCount,mergedAppService);
                mergedAppList.add(app);
            }
        }
        //-----------------以下是计算合并后app之间的相似度--------------------
        for(App app : mergedAppList){
            GlobalContain.similarityArr.put(app.name,new ArrayList<>());
        }
        for(int i = 0; i < mergedAppList.size(); ++i){
            for(int j = i + 1; j < mergedAppList.size();++j){
                App appi = mergedAppList.get(i);
                App appj = mergedAppList.get(j);
                int cnt = 0;
                for(Service service : appi.services){
                    if(appj.services.contains(service)){
                        cnt++;
                    }
                }
                GlobalContain.similarityArr.get(appi.name).add(new Sim(appj , (double)cnt / (appi.services.size() + appj.services.size() - cnt)));
                GlobalContain.similarityArr.get(appj.name).add(new Sim(appi , (double)cnt / (appi.services.size() + appj.services.size() - cnt)));

//                GlobalContain.similarityArr.get(appi.name).add(new Sim(appj , (double)cnt / appi.services.size()));
//                cnt = 0;
//                for(Service service : appj.services){
//                    if(appi.services.contains(service)){
//                        cnt++;
//                    }
//                }
//                GlobalContain.similarityArr.get(appj.name).add(new Sim(appi , (double)cnt / appj.services.size()));
            }
        }
        for(String key : GlobalContain.similarityArr.keySet()){
            GlobalContain.similarityArr.get(key).sort((sim1,sim2)->{
                if(sim2.similarity > sim1.similarity){
                    return 1;
                }else if(sim2.similarity < sim1.similarity){
                    return -1;
                }else{
                    return 0;
                }
            });
            log.info("{}  sim = {}" , key , GlobalContain.similarityArr.get(key));
        }
        //-----------------以下是计算加载内存--------------------
        Set<Service> has = new HashSet<>();
        for(App app : mergedAppList){
            for(Service service : app.services){
                if(has.add(service)){
                    totalMem += service.count * 0.01;
                }
            }
        }

        return  mergedAppList;
    }

    /**
     * 该方法先从dataPath读取data.json文件,其格式为
     * {
     *      apps:{app-name:app-count,...},
     *      dependencies:{app-name:{srv-name:srv-count,...},...}
     * }
     * 读取以后,保存到GlobalContain中相应数据结构
     */
    private void initData(List<String> pilotNames, String dataPath) {
        GlobalContain.PILOT_NAME = pilotNames;
        GlobalContain.PILOT_SUM = pilotNames.size();
        for(String pilotName : pilotNames){
            GlobalContain.PILOT_LIST.add(new Pilot(pilotName , new ArrayList<>() , new HashSet<>()));
        }
        DataHandler dataHandler = ObjectFactory.dataHandler;
        JSONObject data = dataHandler.dataReader(dataPath);
        JSONObject apps = data.getJSONObject(GlobalConstant.KEY_APPS);
        JSONObject dependencies = data.getJSONObject(GlobalConstant.KEY_DEPENDENCIES);
        JSONObject appDependencies ; // 某一个app的依赖,做遍历时临时变量用
        Set<String> serviceNames ; // 某一个app的依赖集合,做遍历时临时变量用
        Set<String> appNames = apps.keySet();
        for(String appName : appNames){
            App app = new App(appName,apps.getInteger(appName),new HashSet<>());
            appDependencies = dependencies.getJSONObject(appName);
            serviceNames = appDependencies.keySet();
            for(String serviceName : serviceNames){
                if(!GlobalContain.SERVICE_NAME_MAP.containsKey(serviceName)){
                    GlobalContain.SERVICE_NAME_MAP.put(serviceName , new Service(serviceName , appDependencies.getInteger(serviceName)));
                }
                app.services.add(GlobalContain.SERVICE_NAME_MAP.get(serviceName));
            }
            GlobalContain.APP_LIST.add(app);
        }
        GlobalContain.APP_SUM = GlobalContain.APP_LIST.size();
        GlobalContain.SERVICE_SUM = GlobalContain.SERVICE_NAME_MAP.size();
    }

    /**
     * 根据app的依赖服务数,由多到少对app排序
     */
    private void sortAppByDependencies() {
        GlobalContain.APP_LIST.sort((app1 , app2)->{
            return app2.services.size() - app1.services.size();
        });
    }

    /**
     * GA算法
     */
    private void GARun(List<App> mergedApps) {
        chromosomeLength = GlobalContain.PILOT_SUM;
        // 生成第一代群体
        List<Chromosome> firstGen = genFirst(mergedApps);
        List<Chromosome> father = firstGen , child = firstGen; // 父代,子代
        // 开始迭代进化
        for(int i = 0; i < iterations; ++i){
            if(i == (int)(iterations * 0.5))
                crossoverRate = 0.7;
            if(i == (int)(iterations * 0.8))
                crossoverRate = 0.6;
            father = child;
            child = evolve(father , i);
        }
        // 找子代适应度最高的个体
        Chromosome maxChromo = getBestChromosome(child);
        // 保存这个状态
        for(int i = 0; i < maxChromo.chromosome.size(); ++i){
            for(App app : maxChromo.chromosome.get(i)){
                GlobalContain.PILOT_LIST.get(i).addApp(app);
            }
        }
        printPilot();
    }

    private void printPilot(){
        log.info("交叉率={},变异率={},迭代={}",crossoverRate,variationRate,iterations);
        double[] loadMen = new double[GlobalContain.PILOT_SUM]; // 总加载内存
        int i = -1;
        for(Pilot pilot : GlobalContain.PILOT_LIST){
            i++;
            int con = 0;
            long srvCnt = 0;
            long srvMem = 0;
            Set<Service> load = new HashSet<>();
            for(App app : pilot.apps){
                con += app.count;
                load.addAll(app.services);
            }
            srvCnt = load.size();
            for(Service service : load){
                srvMem += service.count;
            }
            loadMen[i] = srvMem * 0.01 ;
            log.info("pilot{}:加载服务数量={},加载服务内存={},连接数={}",i,srvCnt,loadMen[i],con);
        }
        long loadMenSum = 0;
        long loadMenSquSum = 0;
        for(double l : loadMen){
            loadMenSum += l;
            loadMenSquSum += l*l;
        }
        double std = Math.sqrt((double)loadMenSquSum / loadMen.length
                - ((double) loadMenSum / loadMen.length) * ((double) loadMenSum / loadMen.length));
        log.info("总内存={},总加载内存={},比例={},标准差={}",totalMem,loadMenSum,(double) loadMenSum / totalMem,std);
    }

    private List<Chromosome> evolve(List<Chromosome> father , int cnt) {
        // 计算父代的适应度
        calAllFitness(father, 0,father.size());
        // 找父代最优秀的个体
        Chromosome bestChromosome = getBestChromosome(father);
        // 选择并复制
        List<Chromosome> child;
        child = rouletteSelect(father, chromosomeSum - (int)(chromosomeSum * 0.1));
        // 交叉
        crossoverChromo(child);
        // 变异
        variationChromo(child);
        // 精英策略
        for(int i = 0; i < (int)(chromosomeSum * 0.1); i++){
            child.add(bestChromosome.copy());
        }
        return child;
    }

    private Chromosome getBestChromosome(List<Chromosome> chromosomes){
        Chromosome best = null;
        double maxFitness = -2;
        for(Chromosome chromosome : chromosomes){
            if(maxFitness < chromosome.fitness){
                best = chromosome;
                maxFitness = chromosome.fitness;
            }
        }
        return best;
    }
    /**
     * 变异
     */
    private void variationChromo(List<Chromosome> chromosomes){
        for(Chromosome chromosome : chromosomes){
            if(Math.random() < variationRate){
                chromosome.variation(NumberUtil.getRandomNum(0,chromosome.chromosome.size()),
                        NumberUtil.getRandomNum(0,chromosome.chromosome.size()));
            }
        }
    }
    /**
     * 交叉
     */
    private void crossoverChromo(List<Chromosome> chromosomes){
        int[] randomNum = NumberUtil.getRandomNum(0, chromosomes.size(), (int) (crossoverRate * chromosomes.size()));
        for(int i = 0; i < randomNum.length; i += 2){
            Chromosome father1 = chromosomes.get(i);
            Chromosome father2 = chromosomes.get(i+1);
            father1.crossover(
                    NumberUtil.getRandomNum(0,father1.chromosome.size()),
                    father2,
                    NumberUtil.getRandomNum(0,father2.chromosome.size()));
        }
    }


    /**
     * 等差方式选择size个染色体
     */
    private List<Chromosome> diffSelect(List<Chromosome> father , int size , int diffArr[]){
        List<Chromosome> choice = new ArrayList<>();
        father.sort((c1,c2)->{
            if(c2.fitness > c1.fitness)return 1;
            else if(c2.fitness < c1.fitness) return -1;
            else return 0;
        });
        int i = 0;
        for(Chromosome chromosome : father){
            for(int j = 0; j < diffArr[i]; j++){
                choice.add(chromosome.copy());
            }
            i++;
            if(i >= diffArr.length){
                break;
            }
        }
        return choice;
    }
    /**
     * 构建一个等差数列
     */
    private int[] buildDiffArr(int sum){
        int cnt = 0 , tmp = 0;
        do{
            tmp++;
            cnt += tmp;
        }while (cnt < sum);
        int rest = sum - (cnt - tmp);
        tmp--;
        int[] result = new int[tmp + rest];
        int i = 0;
        for(; i < tmp; ++i){
            result[i] = tmp - i ;
        }
        for(; i < result.length; ++i){
            result[i] = 1;
        }
        return result;
    }

    /**
     * 轮盘赌的方式选择size个染色体
     */
    private List<Chromosome> rouletteSelect(List<Chromosome> father , int size){
        List<Chromosome> choice = new ArrayList<>();
        double[] accumFitness = new double[father.size()]; // 累计适应度
        accumFitness[0] = father.get(0).fitness;
        for(int i = 1; i < father.size(); ++i){
            accumFitness[i] = accumFitness[i - 1] + father.get(i).fitness;
        }
        for(int i = 0; i < size; ++i){
            double random = NumberUtil.getRandomDouble(0.0,accumFitness[father.size()-1]);
            int j = 0;
            for(; accumFitness[j] < random; ++j);
            choice.add(father.get(j).copy());
        }
        return choice;
    }

    /**
     * 计算群体中 [start,start+len)的适应度
     */
    private void calAllFitness(List<Chromosome> firstGen, int start, int len) {
        for(int i = start ; i < start + len; ++i){
            Chromosome tmpChrom = firstGen.get(i);
            if(tmpChrom.fitness < 0){
                tmpChrom.fitness = calFitness(tmpChrom.chromosome);
            }
            log.info("fitness{} = {} , 目标函数={} " , i , tmpChrom.fitness , (double) 1 / tmpChrom.fitness);
        }
    }

    private double totalMem;
    /**
     * 计算一个个体的适应度
     */
    private double calFitness(List<Set<App>> chromosome){
        // 记录每个pilot的加载内存
        double[] loadMems = new double[GlobalContain.PILOT_SUM];
        // 记录每个pilot已经加载的服务
        Set<String> srvs = new HashSet<>();
        // 遍历chromosome  构建loadMem和srvs
        for(int i = 0; i < chromosome.size(); ++i){
            srvs.clear();
            for(App app : chromosome.get(i)){
                for(Service service : app.services){
                    if(srvs.add(service.name)){
                        loadMems[i] += service.count;
                    }
                }
            }
            loadMems[i] *= 0.01;
        }

        // 总加载内存
        double totalLoadMem = 0.0;
        // 加载内存平方和
        double squareLoadMen = 0.0;
        for(double loadMem : loadMems){
            totalLoadMem += loadMem ;
            squareLoadMen += loadMem * loadMem ;
        }
        // 加载内存方差
        double stdLoadMem = Math.sqrt(squareLoadMen / GlobalContain.PILOT_SUM
                - (totalLoadMem / GlobalContain.PILOT_SUM) * (totalLoadMem / GlobalContain.PILOT_SUM));
        double fitness = totalMem / (totalLoadMem * stdLoadMem ) ;
        return fitness;
    }


    /**
     * 把剩余的app分配到pilot
     */
    private void disRestApp(int start, Map<String, List<String>> result) {
    }

    /**
     * 随机生成第一代种群
     */
    private List<Chromosome> genFirst(List<App> mergedApps){
        List<Chromosome> firstGeneration = new ArrayList<>();
        for(int i = 0; i < chromosomeSum; ++i){
            List<Set<App>> chromosome = new ArrayList<>();
            Map<String,Integer> appMap = new HashMap<>();
            for(int j = 0; j < chromosomeLength; ++j){
                chromosome.add(new HashSet<>());
            }
            for(App app : mergedApps){
                int idx = NumberUtil.getRandomNum(0,chromosomeLength);
                chromosome.get(idx).add(app);
                appMap.put(app.name,idx);
            }
            firstGeneration.add(new Chromosome(chromosome , appMap , -1));
        }
        return firstGeneration;
    }



    // ----------------------华丽的分割线--------------------
    @Override
    public Map<String, List<String>> stage2Run(JSONObject data) {
        return null;
    }

}
