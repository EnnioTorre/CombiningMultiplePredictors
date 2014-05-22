/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.boosting;


import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.weka.filters.RemoveFacilities;
import weka.classifiers.AbstractClassifier;

import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author ennio
 */
public class Boosting implements Oracle {

    static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Boosting.class.getName()); 
    protected Instance instc;
    protected InputOracle inputOracle;
    protected AbstractClassifier classifier[];
  
    protected boolean throughput=false,abort=false,
            responsetimeRO=false,responsetimeWO=false;
    
    
    
   public Boosting() throws Exception{
       
       

       if(BoostingDataset.BoostingDataset==null)
           throw new Exception("Boosting Dataset not present");
       classifier=new AdditiveRegression[4];
      
       String  combiner=BoostingConfiguration.getInstance().getCombiner();
       String Iteration=BoostingConfiguration.getInstance().getIteration();
       
       String []Options={"-W",combiner,"-I",Iteration};
       
       
       for(int i=0;i<classifier.length;i++){
       
           classifier[i]=new AdditiveRegression();//new Ensemble());
           
           classifier[i].setOptions(Options.clone());
       }
       
   }
   
   
    @Override
    public OutputOracle forecast(InputOracle io) throws OracleException {
       
         if(BoostingDataset.BoostingDataset==null)
           throw new OracleException("Generic Learner Dataset not present");
        
        try {
            inputOracle=io;
            instc=DataConverter.FromInputOracleToInstance(io);
            
            return new BostingOutputOracle();
        } catch (Exception ex) {
           logger.error("Error During Boosting forecasting"+ ex);
        
           throw new OracleException(eu.cloudtm.boosting.Boosting.class.getName()+ex);
        }
    }
   
   
   
   
    private class BostingOutputOracle implements OutputOracle{

          
        @Override
        public double throughput(int i) {
            try {
                
                int displacement=OutputsMap.throughput.getArffDispacement();
                BoostingDataset.BoostingDataset.setClassIndex(BoostingDataset.BoostingDataset.numAttributes()-displacement);
                
                if(BoostingDataset.BoostingDataset.numDistinctValues(BoostingDataset.BoostingDataset.classAttribute())<2){
                    
                    logger.info("The Target class attribute has lesser than two different values");
                    
                    return BoostingDataset.BoostingDataset.firstInstance().classValue();
                
                }
                
                if(!throughput)// the learner is generated only once for each target;
                {
                    String Outputs[]=OutputsMap.valueOf("throughput").getOtherClassTargets();
                   
                    Instances Data=RemoveFacilities.remove(BoostingDataset.BoostingDataset,Outputs);
                    
                    classifier[0].buildClassifier(Data);
                    throughput=true;
                    logger.info("-----Generic Learner-----");
                    logger.info(classifier[0].getClass()+"-----created-----");
                }
                double writeperc=(Double)inputOracle.getParam(Param.PercentageSuccessWriteTransactions);
               
                if (i==0){
            
                    return classifier[0].classifyInstance(instc)*(1-writeperc);
                }
            
                if(i==1){
            
                    return classifier[0].classifyInstance(instc)*(writeperc);
                }
                
                 else{ throw new Exception("target class index wrong (1 or 0)");}
                
            } catch (Exception ex) {
                logger.error(this.getClass().getName()+ex);
                ex.printStackTrace();
               throw new RuntimeException("Error During Throughput forecasting");
            }
            
        }

        @Override
        public double abortRate(int i) {
            try {
                
                 int displacement=OutputsMap.abortRate.getArffDispacement();
                BoostingDataset.BoostingDataset.setClassIndex(BoostingDataset.BoostingDataset.numAttributes()-displacement);
                
                 if(BoostingDataset.BoostingDataset.numDistinctValues(BoostingDataset.BoostingDataset.classAttribute())<2){
                    
                    logger.info("The Target class attribute has lesser than two different values");
                    
                    return BoostingDataset.BoostingDataset.firstInstance().classValue();
                
                }
                
                
                if(!abort)// the learner is generated only once for each target;
                {
                    String Outputs[]=OutputsMap.valueOf("abortRate").getOtherClassTargets();
                   
                    Instances Data=RemoveFacilities.remove(BoostingDataset.BoostingDataset,Outputs);
                    classifier[1].buildClassifier(Data);
                    abort=true;
                    logger.info("-----Generic Learner-----");
                    logger.info("-----Oracle created-----");
                }
                
                
               
                if (i==0){
            
                    return 0D;
                }
            
                if(i==1){
            
                    return classifier[1].classifyInstance(instc);
                }
                
                 else{ throw new Exception("target class index wrong (1 or 0)");}
                
            } catch (Exception ex) {
                logger.error(this.getClass().getName()+ex);
               throw new RuntimeException("Error During AbortRate forecasting");
            }
            
        }

        @Override
        public double responseTime(int i) {
            try {
                
                                             
                if (i==0){
                    
                      int displacement=OutputsMap.responseTimeRO.getArffDispacement();
                BoostingDataset.BoostingDataset.setClassIndex(BoostingDataset.BoostingDataset.numAttributes()-displacement);
                
                if(BoostingDataset.BoostingDataset.numDistinctValues(BoostingDataset.BoostingDataset.classAttribute())<2){
                    
                    logger.info("The Target class attribute has lesser than two different values");
                    
                    return BoostingDataset.BoostingDataset.firstInstance().classValue();
                
                }
                    
                    if(!responsetimeRO)// the learner is generated only once for each target;
                {
                    String Outputs[]=OutputsMap.valueOf("responseTimeRO").getOtherClassTargets();
                   
                   
                    Instances Data=RemoveFacilities.remove(BoostingDataset.BoostingDataset,Outputs);
                    classifier[2].buildClassifier(Data);
                    
                    responsetimeRO=true;
                    logger.info("-----Generic Oracle-----");
                    logger.info("-----Oracle created-----");
                }
                    
                    return classifier[2].classifyInstance(instc);
                }
            
                if(i==1){
                    
                      int displacement=OutputsMap.responseTimeWO.getArffDispacement();
                BoostingDataset.BoostingDataset.setClassIndex(BoostingDataset.BoostingDataset.numAttributes()-displacement);
                
                if(BoostingDataset.BoostingDataset.numDistinctValues(BoostingDataset.BoostingDataset.classAttribute())<2){
                    
                    logger.info("The Target class attribute has lesser than two different values");
                    
                    return BoostingDataset.BoostingDataset.firstInstance().classValue();
                
                }
            
                     if(!responsetimeWO)// the learner is generated only once for each target;
                {
                    String Outputs[]=OutputsMap.valueOf("responseTimeWO").getOtherClassTargets();
                    
                   
                    Instances Data=RemoveFacilities.remove(BoostingDataset.BoostingDataset,Outputs);
                    //logger.info(Data);
                   classifier[3].buildClassifier(Data);
                    
                    responsetimeWO=true;
                    logger.info("-----Generic Learner-----");
                    logger.info("-----Oracle created-----");
                }
                    
                    return classifier[3].classifyInstance(instc);
                }
                
                 else{ throw new Exception("target class index wrong (1 or 0)");}
                
            } catch (Exception ex) {
                ex.printStackTrace();
                logger.error(this.getClass().getName()+ex);
               throw new RuntimeException("Error During ResponseTime forecasting");
            }
            
        }

        @Override
        public double getConfidenceThroughput(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getConfidenceAbortRate(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public double getConfidenceResponseTime(int i) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    
    
    }
    
    private String getStringOptions(String O[]){
    
        String opt="";
        for(String next:O){
        
            opt+=next+",";
        }
        return opt;
    }
}
    
   