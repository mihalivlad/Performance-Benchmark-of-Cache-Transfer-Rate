package sample;

import java.awt.image.AreaAveragingScaleFilter;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Controller implements Runnable {
    private int numberOfCores;
    private int numberOfProcessors;
    private int lineSize;
    private CacheInfo cache1;
    private CacheInfo cache2;
    private CacheInfo cache3;
    private int ramSize;
    private double ramLatency;
    private double amat;
    private int benchmarkNr;

    public Controller() {
        this.numberOfCores = 0;
        this.numberOfProcessors = 0;
        this.lineSize = 0;
        this.cache1 = new CacheInfo();
        this.cache2 = new CacheInfo();
        this.cache3 = new CacheInfo();
        this.ramSize = 0;
        this.ramLatency = 0;
        this.amat = 0;
        benchmarkNr = 0;
    }

    public int getNumberOfCores() {
        return numberOfCores;
    }

    public int getNumberOfProcessors() {
        return numberOfProcessors;
    }

    public int getLineSize() {
        return lineSize;
    }

    public CacheInfo getCache1() {
        return cache1;
    }

    public CacheInfo getCache2() {
        return cache2;
    }

    public CacheInfo getCache3() {
        return cache3;
    }

    public int getRamSize() {
        return ramSize;
    }

    public double getRamLatency() {
        return ramLatency;
    }

    public int getBenchmarkNr() {
        return benchmarkNr;
    }

    public void setBenchmarkNr(int benchmarkNr) {
        this.benchmarkNr = benchmarkNr;
    }

    public double getAmat() {
        return amat;
    }

    public void setAmat(double amat) {
        this.amat = amat;
    }

    public boolean benchmark1(){
        try {
            Process process = Runtime.getRuntime().exec("scs11");
            process.waitFor();
            process = Runtime.getRuntime().exec("scs1");
            process.waitFor();
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        List<Integer> list = doubleToIntList(getNumbersFromFile("scs11.out"));
        numberOfCores = list.get(list.size()-2);
        numberOfProcessors = list.get(list.size()-3);
        list = doubleToIntList(getNumbersFromFile("scs1.out"));
        if(list.size() == 7) {
            cache1.setSize(list.get(0));
            cache2.setSize(list.get(2));
            cache3.setSize(list.get(4));
            ramSize = list.get(6);
            lineSize = list.get(1);
        }else{
            return false;
        }
        return true;
    }

    public boolean benchmark2(){
        if(!benchmark1()){
            return false;
        }
        try {
            Process process = Runtime.getRuntime().exec("scs2 "
                    +cache1.getSize()+" "
                    +cache2.getSize()+" "
                    +cache3.getSize()+" "
                    +numberOfProcessors+" "
                    +numberOfCores+" "
                    +lineSize+" ");
            process.waitFor();
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        List<Double> list = getNumbersFromFile("scs2.out");
        if(list.size()==4){
            cache1.setLatency(list.get(0));
            cache2.setLatency(list.get(1));
            cache3.setLatency(list.get(2));
            ramLatency = list.get(3);
            System.out.println(ramLatency);
        }else{
            return false;
        }

        return true;
    }

    public boolean benchmark3(){
        if(!benchmark2()){
            return false;
        }
        try {
            Process process = Runtime.getRuntime().exec("cmd /c start /wait scs3.exe "
                    +cache1.getSize()+" "
                    +cache2.getSize()+" "
                    +cache3.getSize()+" "
                    +numberOfProcessors+" "
                    +numberOfCores+" "
                    +lineSize+" "
                    +cache1.getLatency()+" "
                    +cache2.getLatency()+" "
                    +cache3.getLatency()+" ");
            process.waitFor();
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        List<Double> list = getNumbersFromFile("scs3.out");
        if(list.size()==3){
            cache1.setMissRatio(list.get(0));
            cache2.setMissRatio(list.get(1));
            cache3.setMissRatio(list.get(2));
        }else{
            return false;
        }

        try {
            Process process = Runtime.getRuntime().exec("cmd /c start /wait scs3.exe "
                    +cache1.getSize()+" "
                    +cache2.getSize()+" "
                    +cache3.getSize()+" "
                    +numberOfProcessors+" "
                    +numberOfCores+" "
                    +lineSize+" "
                    +(cache1.getLatency()*2.0+1.0)+" "
                    +(cache2.getLatency()*2.0+1.0)+" "
                    +(cache3.getLatency()*2.0+1.0)+" ");
            process.waitFor();
        } catch (InterruptedException e) {
            return false;
        } catch (IOException e) {
            return false;
        }
        list = getNumbersFromFile("scs3.out");
        if(list.size()==3){
            cache1.setMissRatio((cache1.getMissRatio()+list.get(0))/2.0);
            cache2.setMissRatio((cache2.getMissRatio()+list.get(1))/2.0);
            cache3.setMissRatio((cache3.getMissRatio()+list.get(2))/2.0);
        }else{
            return false;
        }

        return true;
    }

    public boolean benchmark4(){
        if(!benchmark3()){
            return false;
        }
        cache3.setMissPenalty(ramLatency);
        cache2.setMissPenalty(cache3.getLatency()+cache3.getMissRatio()/100*cache3.getMissPenalty());
        cache1.setMissPenalty(cache2.getLatency()+cache2.getMissRatio()/100*cache2.getMissPenalty());
        amat = cache1.getLatency()+cache1.getMissRatio()/100*cache1.getMissPenalty();
        System.out.println(amat);
        return true;
    }

    private List<Double> getNumbersFromFile(String path){
        List<Double> list = new ArrayList<>();
        File file = new File(path);
        BufferedReader reader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            String text = null;

            while ((text = reader.readLine()) != null) {
                List<String> split = Arrays.asList(text.split("[ ,/:]+"));
                for (String maybeInt: split) {
                    try {
                        list.add(Double.parseDouble(maybeInt));
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
            }
        }

//print out the list
        System.out.println(list);
        return list;
    }

    private List<Integer> doubleToIntList(List<Double> doubleList){
        List<Integer> integerList = new ArrayList<>();
        for (Double d: doubleList) {
            integerList.add(d.intValue());
        }
        return integerList;
    }

    @Override
    public void run() {
        switch (benchmarkNr){
            case 1:
                benchmark1();
                break;
            case 2:
                benchmark2();
                break;
            case 3:
                benchmark3();
                break;
            case 4:
                benchmark4();
                break;
        }
    }
}
