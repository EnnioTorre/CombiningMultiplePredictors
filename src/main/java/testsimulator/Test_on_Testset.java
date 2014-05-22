/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package testsimulator;

import CsvOracles.params.CsvRgParams;
import eu.cloudtm.Knn.Knearestneighbourg;

import eu.cloudtm.Configuration.KnnConfiguration;
import csv.CsvReader;
import csv.PrintDataOnCsv;

import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.boosting.Boosting;
import java.io.File;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */
public class Test_on_Testset {
    private int boost=0;
    public static boolean Cmodels,Tmodels;
    Test_on_Testset(){}
    
public void addBosting(boolean models){

    boost=1;
    if(models){
    String model1="lib/MorphR1";
    String model2="conf/tas/gmu/tpc/cubist/demo_all";
    if(CheckFileExistence(model1)!=4){
        Cmodels=true;
    
        System.out.println("MOdels have not been Already created for Cubist");
    }
    
     else
    {System.out.println("Cubist MOdels have  been Already created");
        Cmodels=false;
            
            }
    
    if(CheckFileExistence(model2)!=3){
        Tmodels=true;
    
        System.out.println("MOdels have not been Already created for Tas");
    }
    
    else
    {System.out.println("Tas MOdels have  been Already created");
        Tmodels=false;
            
            } 
    } 
}

    
public void test(String Directory_path) throws ClassNotFoundException, InstantiationException, IllegalAccessException, Exception{
        Oracle oracles[]=new Oracle[KnnConfiguration.getInstance().getOracles().length+1+boost];
        
        int NumOracles=0;
        
        for (Class c:KnnConfiguration.getInstance().getOracles()){
                  oracles[NumOracles++]=(Oracle)c.newInstance();
                   System.out.println(c);
                 }
        
        oracles[NumOracles]=new Knearestneighbourg();
        
        if(boost>0)
            oracles[NumOracles+1]=new Boosting();
        
        OutputOracle result;
        CsvReader Input;
        String OutputOracles="";
        File dir = new File(Directory_path);
      for (File nextdir : dir.listFiles()) {
         if (nextdir.isDirectory()) {
            for (File csv : nextdir.listFiles()) {
               if (!csv(csv)) {
                  continue;
                  
               }
               else{
                   
                    Input=new CsvReader(new CsvRgParams(csv.getPath()));
                    
                    OutputOracles="csvPath,"+csv.getAbsolutePath()+",";
                   for (Oracle o:oracles){
                      result=o.forecast(Input);
                      OutputOracles=OutputOracles+","+OutputOracleToString(result,o)+",";
                      
                               }
                   OutputOracles=OutputOracles+","+OutputOracleToString(Input)+",";
                  
               }
             PrintDataOnCsv.PrintStringOnCsvFile("csv_testset.csv", OutputOracles);  
            }
            
            
         }
      }
      

    }
    
    
    public String OutputOracleToString(OutputOracle o,Oracle or){
               
        return or.toString().split("@")[0]+"throughputRO,"+o.throughput(0)+","+
               or.toString().split("@")[0]+"throughputWO,"+o.throughput(1)+","+
               or.toString().split("@")[0]+"abortRateRO,"+o.abortRate(0)+","+
               or.toString().split("@")[0]+"abortRateWO,"+o.abortRate(1)+","+
               or.toString().split("@")[0]+"responseTimeRO,"+o.responseTime(0)+","+
               or.toString().split("@")[0]+"responseTimeWO,"+o.responseTime(1);
              
    }
    
    public String OutputOracleToString(CsvReader o){
               
        return "throughputRO,"+o.throughput(0)+","+
               "throughputWO,"+o.throughput(1)+","+
               "abortRateRO,"+o.abortRate(0)+","+
               "abortRateWO,"+o.abortRate(1)+","+
               "responseTimeRO,"+o.responseTime(0)+","+
               "responseTimeWO,"+o.responseTime(1);
              
    }

 public boolean csv(File f) {
      
      return f.toString().endsWith("csv");
   }
 
 private int CheckFileExistence(String d){
     
         File dir=new File(d);
         int i=0;
         
        if(!dir.exists()||dir.listFiles().length<1)
            return 0;
     
        for (File f:dir.listFiles()){
        
            if(f.getName().endsWith(".model"))
                i+=1;
        }
    
        return i;
    
    }
 
}
