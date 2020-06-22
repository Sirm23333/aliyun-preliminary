package com.contest.ali.pilotlb.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class NumberUtil {
    /**
     *  生成一个[s,e)的随机整数
     */
    public static int getRandomNum(int s , int e){
        return (int) (Math.random() * (e - s) + s);
    }

    /**
     * 生成一个[s,e)的随机浮点数
     */
    public static double getRandomDouble(double s, double e){
        return Math.random() * (e - s) + s;
    }

    /**
     * 在[s,e)中产生size个不重复的随机数
     */
    public static int[] getRandomNum(int s,int e,int size){
        if(size > e - s){
            return null;
        }
        int[] result = new int[size];
        List<Integer> rest = new ArrayList<>();
        for(int i = 0; i < e; ++i){
            rest.add(i);
        }
        for(int i = 0; i < size; ++i){
            int select = getRandomNum(0,rest.size());
            result[i] = rest.get(select);
            rest.remove(select);
        }
        return result;
    }
    /**
     * 在[s,e)中产生size次随机数,并去重
     */
    public static int[] getRandomNumRep(int s,int e,int size){
        if(size > e - s){
            return null;
        }
        int[] result = new int[size];
        List<Integer> rest = new ArrayList<>();
        for(int i = 0; i < e; ++i){
            rest.add(i);
        }
        for(int i = 0; i < size; ++i){
            int select = getRandomNum(0,rest.size());
            result[i] = rest.get(select);
            rest.remove(select);
        }
        return result;
    }
    /**
     * 计算方差
     */
    public static double calStd(long[] data){
        long sum = 0; // 和
        long sqSum = 0; // 平方和
        for(long i : data){
            sum += i;
            sqSum += i * i;
        }
        double sqSumAvg = (double)sqSum / data.length;
        double sumAvg = (double)sum / data.length;
        return Math.sqrt(sqSumAvg - sumAvg * sumAvg);
    }
    public static double calStd(double[] data){
        double sum = 0; // 和
        double sqSum = 0; // 平方和
        for(double i : data){
            sum += i;
            sqSum += i * i;
        }
        double sqSumAvg = sqSum / data.length;
        double sumAvg = sum / data.length;
        return Math.sqrt(sqSumAvg - sumAvg * sumAvg);
    }
    public static double[] calStdAndSum(long[] data){
        long sum = 0; // 和
        long sqSum = 0; // 平方和
        for(long i : data){
            sum += i;
            sqSum += i * i;
        }
        double sqSumAvg = (double)sqSum / data.length;
        double sumAvg = (double)sum / data.length;
        double[] re = {Math.sqrt(sqSumAvg - sumAvg * sumAvg) , sum};
        return re;
    }

    /**
     * 在data[idx]上增加add后的方差
     */
    public static double calStd(long[] data , int idx , int add){
        double sum = 0; // 和
        double sqSum = 0; // 平方和
        for(double i : data){
            sum += i;
            sqSum += i * i;
        }
        sum += add;
        sqSum += 2 * add * data[idx] + add * add;
        double sqSumAvg = sqSum / data.length;
        double sumAvg = sum / data.length;
        return Math.sqrt(sqSumAvg - sumAvg * sumAvg);
    }
}
