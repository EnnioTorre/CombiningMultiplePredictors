
package eu.cloudtm.Knn;

import eu.cloudtm.Configuration.KnnConfiguration;
import eu.cloudtm.DataUtility.DataConverter;

import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;

import java.lang.reflect.Method;
import java.util.Map;

import org.apache.log4j.Logger;
import weka.core.DistanceFunction;

import weka.core.Instances;
import weka.core.NormalizableDistance;
import weka.core.neighboursearch.LinearNNSearch;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */



public class Knearestneighbourg extends Learner implements Oracle {
    static Logger logger = Logger.getLogger(Knearestneighbourg.class.getName()); 
    protected LinearNNSearch KNN;
    protected int NumNeighbours;
    protected String ConsideredOutOracle;
   

   
   
    public Knearestneighbourg () throws Exception{
      
        
       //this.m_TrainingFile=trainingset;
       //this.m_TestSetFile=testset;
       String[] parameters=getconfiguration();
       
       if(parameters.length<5||parameters.length>5)
           throw new Exception("Bad configuration of KnearestNeighbourg ,parameters number is wrong");
      
       String[] options=new String[1];
       options[0] =parameters[1];
       Method method;
       NumNeighbours=Integer.parseInt(parameters[2]);      
       ConsideredOutOracle=parameters[3];
       cutoff=Double.parseDouble(parameters[4]);
       //setTraining(this.m_TrainingFile);
       //setTestSet(this.m_TestSetFile);
       KNN=new LinearNNSearch();
       
       
       Class c = Class.forName("weka.core."+parameters[0]);
	    Object distanceFunc = c.newInstance();
             method=distanceFunc.getClass().getMethod("setOptions", String[].class);
        method.invoke(distanceFunc, (Object) options);
        
        KNN.setDistanceFunction((DistanceFunction)distanceFunc);
      
        
       
        if(DataSets.ARFFDataSet!=null){
        
            this.m_Training=DataSets.ARFFDataSet;
        
        
        }
        else{
            logger.error("--"+"Datasets Not instanziated");
            throw new InstantiationException("Datasets Not instanziated");
        }
        
        
        //Instance I =getInstanceToTest(0);
        //KNN.kNearestNeighbours(I,1);
        
        
       
        
        
       
    }
    
  
  /*public void setTestSet(String name) throws Exception {
    m_TestSet = new Instances(
                        new BufferedReader(new FileReader(name)));
   
  }
  
  public Instance getInstanceToTest(int index){
     return m_TestSet.instance(index);
  }*/
   public Instances getNeighboughood() {
        return Neighbourshood;
    }
   

   

    @Override
    public OutputOracle forecast(InputOracle io) throws OracleException {
        
        double AVGrmse=Double.MAX_VALUE;
        Oracle best=null;
        
        try{
           
           
           SelectInstancesRP(io);
           KNN.setInstances(m_Training);
           m_TestSet=DataConverter.FromInputOracleToInstance(io);
           logger.info("PREDICT TARGET VALUE FOR  : "+m_TestSet.toStringNoWeight());
           System.out.println(m_TestSet);
           Neighbourshood=KNN.kNearestNeighbours(m_TestSet,NumNeighbours);//>k neighbours are returned if there are more than one neighbours at the kth boundary.
           distances=KNN.getDistances();
           
           
           RMSE=RMSE(this.ConsideredOutOracle);
           
       
 
        double actual;
        for(Map.Entry<Oracle,Double[]>entry:RMSE.entrySet()){
            
          
           // actual=(entry.getValue()[0]*VarianceRMSE(entry.getValue())[0]+entry.getValue()[1]*VarianceRMSE(entry.getValue())[1])/2;
            actual=(entry.getValue()[0]+entry.getValue()[1])/2;
            if(actual<=AVGrmse){
                 AVGrmse=actual;
                 best=entry.getKey();
                 
               }
        }
        
       logger.info("ORACLE SELECTED FOR PREDICTION : "+best.toString().split("@")[0]);
        
        return best.forecast(io);
        
         }
        catch( Exception ex){
            logger.error("forecast error  "+ex);
            throw new OracleException(ex);
        }
    }
    
   
   private String[] getconfiguration(){
   
       String[] param=new String[5];
       param[0]=KnnConfiguration.getInstance().getDistanceFunction();
       param[1]=KnnConfiguration.getInstance().getIsNormalized();
       param[2]=KnnConfiguration.getInstance().getknearestneighbourg();
       param[3]=KnnConfiguration.getInstance().getTarget();
       param[4]=KnnConfiguration.getInstance().getCutoff();
    
       return param;
               
               
   } 
    

 
}
