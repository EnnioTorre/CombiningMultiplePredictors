/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Configuration;


import org.apache.log4j.Logger;
import batching.offline.BatchingPaoloAnalyticalOracle;
/**
 *
 * @author ennio
 */
public class BatchingConfigurator extends Configuration{
     static Logger logger = Logger.getLogger(BatchingConfigurator.class.getName());
     static BatchingConfigurator myself;
     
    public BatchingConfigurator() {
        super("conf/Batching/Batching.properties");
    }

    @Override
    public String getCsvInputDir() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public String getCsvOutputDir() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public String getOracleInputDescription() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public Class[] getOracles() throws ClassNotFoundException {
       throw new UnsupportedOperationException() ;
    }

    @Override
    public boolean isCSVOutputenable() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public boolean isDAGSARFFenable() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public boolean isMorphRARFFenable() {
        throw new UnsupportedOperationException() ;
    }

    @Override
    public boolean isTasARFFenable() {
       throw new UnsupportedOperationException() ;
    }

    @Override
    public boolean isValidationSetARFFenable() {
        throw new UnsupportedOperationException() ;
    }
    
   public void SetAccuracy(){
   
       String q=this.defaultProps.getProperty("Quality");
       this.checkvalue(q);
       
      
       
       switch(q){
       
           case "good":
           {
                   BatchingPaoloAnalyticalOracle.mediumMAPEValues();
                    logger.info("Batch Setted good");                 
               break;
           }
           case "worst":{
               BatchingPaoloAnalyticalOracle.paperValues();
               logger.info("Batch Setted worst");
               break;
           }
           case "optimal":
           {
                   logger.info("Batch Setted optimal");
                   BatchingPaoloAnalyticalOracle.optimalMAPEValues();
               break;
           }
       }
       
   }
    
     public static BatchingConfigurator getInstance(){
        return (myself==null)?myself=new BatchingConfigurator():myself;
     }
     
     public String getCutoff(){
    
       String o= defaultProps.getProperty("CutoffValue");
        checkvalue(o);
        return o; 
    }
     
     public void setCutOff(){
     
       if(Double.parseDouble(getCutoff())<500){
                   
                       BatchingPaoloAnalyticalOracle.cutOff100();
                       logger.info("Batch cut off Setted to 100");
                   }
                   
       else {
           BatchingPaoloAnalyticalOracle.cutOff500();
           logger.info("Batch cut off Setted to 500");
       
       }
     }
}
