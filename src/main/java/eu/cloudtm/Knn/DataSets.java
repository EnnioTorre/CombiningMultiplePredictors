/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Knn;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.DataUtility.DataPrinting;
import CsvOracles.params.CsvRgParams;



import csv.CsvReader;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;


import java.io.File;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;

import weka.core.Instance;


import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import CsvOracles.params.CsvRgParams;
import csv.PrintDataOnCsv;
import csv.ReadDataFromCsv;
import eu.cloudtm.Configuration.BatchingConfigurator;
import eu.cloudtm.Configuration.Configuration;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.Configuration.KnnConfiguration;
import eu.cloudtm.Dataset.Dataset;
import eu.cloudtm.OracleHandlers.OracleHandlers;
import batching.offline.BatchingPaoloAnalyticalOracle;
import eu.cloudtm.learner.GenericLearner;
import eu.cloudtm.learner.GenericLearnerDataset;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author etorre
 */
public class DataSets implements Dataset{
    static Logger logger = Logger.getLogger(DataSets.class.getName());
    
    public static Instances ARFFDataSet;
    public static HashMap<Instance,OutputOracle>ValidationSet;
    public static HashMap<Oracle,HashMap<Instance,OutputOracle>> predictionResults;
    public static HashMap<String,Instance>InstancesMap=new HashMap<String,Instance>();
    
    
    private KnnConfiguration LK;
    
    public DataSets() throws Exception{
         
         int numFiles=0;
         logger.info("//******************************************");
         logger.info("          Start of Datasets Creation        ");
         logger.info("*******************************************//");
         
         try{
             
             
         init();
         
         File dir=new File(KnnConfiguration.getInstance().getCsvInputDir());
         
         if( dir.exists()&&dir.list().length>0) {
        
             logger.info("Datasets Creation from Csv File" );
                 numFiles=ImportDataset(dir.getName());
             }
         
         else{
             
                logger.error("Dataset does not exist" );
                throw new FileNotFoundException(dir.getName());
                
             }
         
        
       
          //only for data Analisis
          DataPrinting.PrintARFF(this);
          
          DataPrinting.PrintCombinedPrediction(this);
        }
    
       
        
        catch(Exception e){
          
            logger.error("Dataset Creation Failed "+e);
            e.printStackTrace();
        }
        
        finally{
          logger.info("//******************************************");
          logger.info("  Dataset Created "+numFiles+" File Readed");
          logger.info("******************************************//");
        }
            
         }
         
    
    public DataSets(String Directory_path) throws Exception{
        
         int numFiles=0;
         logger.info("//******************************************");
         logger.info("          Start of Datasets Creation        ");
         logger.info("*******************************************//");
         
         try{
             
             
            init(Directory_path);
            logger.info("Datasets Creation from Queries to Oracles" );
            numFiles=CreateDataset(Directory_path);
            PrintDataOnCsv.PrintCsvFile(this);
         
        
       
          //only for data Analisis
            DataPrinting.PrintARFF(this);
          
            DataPrinting.PrintCombinedPrediction(this);
        }
    
       
        
        catch(Exception e){
          
            logger.error("Dataset Creation Failed "+e);
            e.printStackTrace();
        }
        
        finally{
          logger.info("//******************************************");
          logger.info("  Dataset Created "+numFiles+" File Readed");
          logger.info("******************************************//");
        }
            
         }
         
      
    
    

    
    private static boolean csv(File f) {
      
      return f.toString().endsWith("csv");
   }
    
    private void init()throws Exception{
       
        LK=KnnConfiguration.getInstance();
        
        DataSource source = new DataSource(LK.getOracleInputDescription());
        
             ARFFDataSet = source.getStructure();

                        
            ValidationSet=new HashMap<Instance,OutputOracle>();
         
            predictionResults=new HashMap<Oracle,HashMap<Instance,OutputOracle>>(3);
            
           for (Class c:LK.getOracles()){
               Object j=c.newInstance();
               if(j instanceof OracleHandlers){
                   logger.info("*******KNN DATASET---> STARTING MODELS CREATION FOR "+c.getName()+"********");
                   
                   logger.info("*******KNN DATASET--->MODELS CREATED FOR "+c.getName()+"********");
                   //((OracleHandlers)j).BuildOracle();
                }
               else if(j instanceof BatchingPaoloAnalyticalOracle){
                   logger.info("*******KNN DATASET---> STARTING MODELS CREATION FOR "+c.getName()+"********");
                     BatchingConfigurator.getInstance().setCutOff();
                     BatchingConfigurator.getInstance().SetAccuracy();
                   logger.info("*******KNN DATASET--->MODELS CRETAED FOR "+c.getName()+"********");
                   //((OracleHandlers)j).BuildOracle();
                }
               else if(j instanceof GenericLearner){
                                   
                   logger.error("*******KNN DATASET--->Impossible to create models for  "+c.getName()+" function not yet implemented ********");
                   //((OracleHandlers)j).BuildOracle();
                }
               
              predictionResults.put((Oracle)j, new HashMap<Instance,OutputOracle>());
          }
            
         
             }
     private void init(String csvs)throws Exception{
       
        LK=KnnConfiguration.getInstance();
        
        DataSource source = new DataSource(LK.getOracleInputDescription());
        
             ARFFDataSet = source.getStructure();

                        
            ValidationSet=new HashMap<Instance,OutputOracle>();
         
            predictionResults=new HashMap<Oracle,HashMap<Instance,OutputOracle>>(3);
            
           for (Class c:LK.getOracles()){
               Object j=c.newInstance();
               if(j instanceof OracleHandlers){
                   logger.info("*******KNN DATASET---> STARTING MODELS CREATION FOR "+c.getName()+"********");
                   ((OracleHandlers)j).BuildOracle();
                   logger.info("*******KNN DATASET--->MODELS CREATED FOR "+c.getName()+"********");
                   //((OracleHandlers)j).BuildOracle();
                }
               else if(j instanceof BatchingPaoloAnalyticalOracle){
                   logger.info("*******KNN DATASET---> STARTING MODELS CREATION FOR "+c.getName()+"********");
                     BatchingConfigurator.getInstance().setCutOff();
                     BatchingConfigurator.getInstance().SetAccuracy();
                   logger.info("*******KNN DATASET--->MODELS CRETAED FOR "+c.getName()+"********");
                   //((OracleHandlers)j).BuildOracle();
                }
               else if(j instanceof GenericLearner){
                   new GenericLearnerDataset(csvs);                  
                   logger.info("*******KNN DATASET--->MODELS CRETAED FOR "+c.getName()+"********");
                   //((OracleHandlers)j).BuildOracle();
                }
               
              predictionResults.put((Oracle)j, new HashMap<Instance,OutputOracle>());
          }
            
         
             }
    
    
    private void UpdatePredictionSet(Instance inst) throws Exception{
    
    //REMEMBER THAT THIS CONVERSION IS MANDATORY DUE TO THE SERIALIZABLE OBJECT REQUIRED
    InputOracle i=DataConverter.FromInstancesToInputOracle(inst);
   
    
    
    for(Map.Entry<Oracle,HashMap<Instance,OutputOracle>> entry:predictionResults.entrySet()){
                DatasetOutputOracle dat=new DatasetOutputOracle();
                //System.out.println(entry.getKey());
                int errorflag=1;
                OutputOracle output=null;
                while(errorflag>0){//try several time to forecast,bug in the DAGS Oracle!!!
                
                    try{
                    
                        //NOTE :*refresh due to batching
                        output=entry.getKey().forecast(i);
                    
                        
                       // NOT NECESSARY WITH HANDLERS but with batching
                        for (Field f: DatasetOutputOracle.class.getDeclaredFields()){
                            
                            Method method=DatasetOutputOracle.class.getDeclaredMethod("set"+f.getName(),int.class, double.class);
            
                            Method method2=OutputOracle.class.getDeclaredMethod(f.getName(),int.class);
            
                            method.invoke(dat,0, method2.invoke(output, 0));
           
                            method.invoke(dat,1, method2.invoke(output, 1));
                        }
                      
                        break;
                    }
                   
                    catch(OracleException ex){
                        ex.printStackTrace();
                        logger.error("Prediction failed "+ex);
                        if(errorflag<2){
                            errorflag++;
                            continue;
                        }
                     
                       dat.initOnOracleError();
                 
                        logger.error("Error on prediction for "+entry.getKey().toString().split("@")[0]);
                        break;
                    }
                    
                    catch(Exception ex){
                         logger.error("Prediction failed"+ex);
                         throw new Exception(ex);
                    } 
                    
                    
                }
                 
          
          
          entry.getValue().put(inst, dat);
          
          logger.info(entry.getKey().toString()+"->"+dat.toString());
            }
              
    }
    
    private void UpdateValidationSet(InputOracle i,Instance inst) throws Exception{
        
        DatasetOutputOracle dat=new DatasetOutputOracle();
        try{
        for (Field f: DatasetOutputOracle.class.getDeclaredFields()){
        
            Method method=DatasetOutputOracle.class.getDeclaredMethod("set"+f.getName(),int.class, double.class);
            Method method2=CsvReader.class.getDeclaredMethod(f.getName(),int.class);
            method.invoke(dat,0, method2.invoke(i, 0));
            method.invoke(dat,1, method2.invoke(i, 1));
          
        }
        ValidationSet.put(inst,dat);
        logger.info("Instance Output-> "+ValidationSet.get(inst).toString());
        }
        catch(Exception e)
    
        {
            logger.error("Prediction failed"+e);
            throw new Exception(e);
        }
    }
    
    private void UpdatePredictionSet(ReadDataFromCsv i,Instance inst) throws  Exception {
    
        // DataInputOracle in=DataConverter.FromInstancesToInputOracle(i);
        //  logger.info(in.toString());
    
    
    for(Map.Entry<Oracle,HashMap<Instance,OutputOracle>> entry:predictionResults.entrySet()){
                DatasetOutputOracle dat=i.getOutputOracle(entry.toString().split("@")[0]);
                
                entry.getValue().put(inst, dat);
               
               logger.info(entry.getKey().toString()+"->"+dat.toString());
    }
    
            
            
    
    }
    
    private void UpdateValidationSet(ReadDataFromCsv i,Instance inst) throws  Exception {
    
        // DataInputOracle in=DataConverter.FromInstancesToInputOracle(i);
        //  logger.info(in.toString());
    
                DatasetOutputOracle dat=i.getOutputOracle("Output");
                ValidationSet.put(inst, dat);

               logger.info("Instance Output-> "+ValidationSet.get(inst).toString());
               
       }
    
    
    
    private int CreateDataset(String Directory_path) throws Exception{
    
        int numFiles=0;
        
        File dir = new File(Directory_path);
      for (File nextdir : dir.listFiles()) {
         if (nextdir.isDirectory()) {
            for (File csv : nextdir.listFiles()) {
               if (!csv(csv)) {
                  continue;
               }
               else{
                   
                 CsvReader  reader=new CsvReader(new CsvRgParams(csv.getPath()));
                   
                   Instance i=DataConverter.FromInputOracleToInstance(reader);
                   logger.info(i);
                   
                   
                   
                   ARFFDataSet.add(i);
                   InstancesMap.put(i.toStringNoWeight(), i);
                   UpdateValidationSet(reader,i);
                   //InputOracle inp=DataConverter.FromInstancesToInputOracle(i);
                   UpdatePredictionSet(i);
                   PrintDataOnCsv.setCsvPath(i,csv.getAbsolutePath(),this);
                   numFiles ++;
                         }
                             
                       
                     
                     }
               
               
               }  
            }
    
         return numFiles;
    
    }
    
    
    
    private int ImportDataset(String Directory_path) throws FileNotFoundException, IOException, Exception{
    
        int numFIles=0;
          File dir = new File(Directory_path);
            for (File csv : dir.listFiles()) {
               if (!csv(csv)) {
                  continue;
               }
               else{
        ReadDataFromCsv reader=new ReadDataFromCsv(csv);
        
        while(reader.ReadNextRow()){
            Instance i=DataConverter.FromInputOracleToInstance(reader);
            logger.info(i);
            UpdatePredictionSet(reader,i);
            UpdateValidationSet(reader, i);
             ARFFDataSet.add(i);
            InstancesMap.put(i.toStringNoWeight(), i);
            
            numFIles++;
        
    
    
    
        } 
        
    
    }
            
      }
      return numFIles;
    }

    @Override
    public Instances getARFFDataSet() {
       return ARFFDataSet;
    }

    @Override
    public HashMap<Instance,OutputOracle> getValidationSet() {
        return ValidationSet;
    }

    @Override
    public HashMap <Oracle,HashMap<Instance,OutputOracle> > getPredictiontionSet() {
        return predictionResults;
    }

    @Override
    public HashMap<String,Instance> getInstancesMap() {
        return InstancesMap;
    }

    @Override
    public Configuration getConfiguration() {
        return LK;
    }
        
        
}





