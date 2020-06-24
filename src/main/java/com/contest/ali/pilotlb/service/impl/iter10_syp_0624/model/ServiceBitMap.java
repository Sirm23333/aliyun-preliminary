package com.contest.ali.pilotlb.service.impl.iter10_syp_0624.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 用来记录一个service集合
 */
public class ServiceBitMap implements Cloneable{

    private final int LONG_BYTE = 64;
    // 记录所有的服务,为了方便算内存
    private List<Service> srvList;
    // 辅助保存的bitmap
    private long[] srvArr;

    public ServiceBitMap(List<Service> srvList){
        this.srvList = srvList;
        this.srvArr = new long[srvList.size() / LONG_BYTE + 1];
    }
    public ServiceBitMap(List<Service> srvList , long[] srvArr){
        this.srvList = srvList;
        this.srvArr = srvArr;
    }
    public void initSrvArr(){
        for(int i = 0; i < srvArr.length; ++i){
            srvArr[i] = 0;
        }
    }
    public void resetSrvList(List<Service> newSrvList){
        this.srvList = newSrvList;
        this.srvArr = Arrays.copyOf(srvArr , newSrvList.size() / LONG_BYTE + 1);
    }

    @Override
    protected Object clone()  {
        return new ServiceBitMap(srvList , Arrays.copyOf(this.srvArr,this.srvArr.length));
    }

    /**
     * 计算依赖服务内存
     * @return
     */
    public long calMem(){
        return calMem(this.srvArr);
    }
    /**
     * 计算依赖服务数
     * @return
     */
    public long calSrvCnt(){
        long tmp;
        int cnt = 0;
        for(int i = 0 , j = 0; i < this.srvArr.length; ++i , j = 0){
            tmp = this.srvArr[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    cnt++;
                }
                tmp >>>= 1;
                j++;
            }
        }
        return cnt;
    }
    /**
     * 添加一个服务
     */
    public void addService(int srvIdx){
        int idx = srvIdx / LONG_BYTE;
        int pos = srvIdx % LONG_BYTE;
        this.srvArr[idx] |= (1L << pos);
    }

    /**
     * 添加多个服务
     * @param another
     */
    public void addAllService(ServiceBitMap another){
        int m = Math.min(another.srvArr.length , this.srvArr.length);
        for(int i = 0; i < m; ++i){
            this.srvArr[i] |= another.srvArr[i];
        }
    }

    /**
     * 计算两个ServiceBitmap重叠的内存量
     * @param another
     * @return
     */
    public long calSameMem(ServiceBitMap another){
        long[] re = new long[this.srvArr.length];
        for(int i = 0; i < re.length; ++i){
            re[i] = this.srvArr[i] & another.srvArr[i];
        }
        return calMem(re);
    }

    /**
     * 输出服务列表
     * @return
     */
    public List<Service> getSrvList(){
        List<Service> list = new ArrayList<>();
        long tmp;
        for(int i = 0 , j = 0; i < this.srvArr.length; ++i , j = 0){
            tmp = this.srvArr[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    list.add(srvList.get(i * LONG_BYTE + j));
                }
                tmp >>>= 1;
                j++;
            }
        }
        return list;
    }

    /**
     * 由服务列表构建服务bitmap
     * @param list
     */
    public void setSrvBitMap(List<Service> list){
        initSrvArr();
        for(Service service : list){
            addService(service.id);
        }
    }






    private long calMem(long[] arr){
        long tmp;
        long mem = 0;
        for(int i = 0 , j = 0; i < arr.length; ++i , j = 0){
            tmp = arr[i];
            while(tmp != 0){
                if((tmp & 1) == 1){
                    mem += srvList.get(i * LONG_BYTE + j).count;
                }
                tmp >>>= 1;
                j++;
            }
        }
        return mem;
    }
}
