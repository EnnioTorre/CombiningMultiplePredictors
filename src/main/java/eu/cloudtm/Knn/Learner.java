/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cloudtm.Knn;

import static eu.cloudtm.Knn.Knearestneighbourg.logger;
import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.ReplicationProtocol;
import static eu.cloudtm.autonomicManager.commons.ReplicationProtocol.PB;
import static eu.cloudtm.autonomicManager.commons.ReplicationProtocol.TO;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import java.io.BufferedReader;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.unsupervised.instance.RemoveWithValues;
import eu.cloudtm.autonomicManager.commons.IsolationLevel;
import eu.cloudtm.probing.ProbingDataSets;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */
public abstract class Learner {

    protected Instances m_Training = null;
    protected String m_TestSetFile = null;
    protected Instance m_TestSet = null;
    protected String m_TrainingFile = null;
    protected HashMap<Oracle,Double[]> RMSE;
    protected Instances Neighbourshood;
    protected double [] distances;
    protected double cutoff;
    protected double [][] SquaredErrors;
    
    
    
    public void setTraining(String name) throws Exception {
    m_Training     = new Instances(
                        new BufferedReader(new FileReader(name)));
    m_Training.setClassIndex(m_Training.numAttributes() - 1);
  }
    
       /**
 *
 * Valid value for Parameter are:
 * throughput
 * abortRate
 * responseTime
 * everyone separated by a space
 */
    
    protected HashMap<Oracle,Double[]> RMSE(String Parameter) throws Exception{
     HashMap<Oracle,Double[]> rmse=new  HashMap<Oracle,Double[]>();
     Double [] RMSe;
     double errorOutputRO;
     double errorOutputWO;
     double SEOutputRO=0D;
     double SEOutputWO=0D;
     //double AVGcontribution=0D;
     //double meanAVGcontribution=20000D;
     double SUMavgComponent=0D;
     Method method;
     OutputOracle outputValidationSet;
     OutputOracle outputOracle;
     StringTokenizer token;
     String outputname;
     
     SquaredErrors=new double[2][Neighbourshood.numInstances()];
       
     for(Map.Entry<Oracle,HashMap<Instance,OutputOracle>> entry:DataSets.predictionResults.entrySet()){
            for (int i=0;i<Neighbourshood.numInstances();i++){
     
                Instance inst= DataSets.InstancesMap.get(Neighbourshood.instance(i).toStringNoWeight());
                
                outputValidationSet=DataSets.ValidationSet.get(inst);
                outputOracle=entry.getValue().get(inst);
                
               logger.info("Instance :"+inst +"\n"+"validationOutput= "+outputValidationSet+"\n"+"Oracle Output= "+outputOracle+"\n"+"Distance= "+distances[i]);
                token=new StringTokenizer(Parameter);
                while(token.hasMoreTokens()){
                    
                    outputname=token.nextToken();
                    
                    method=OutputOracle.class.getMethod(outputname, int.class);
                    
                    errorOutputRO=(Double)method.invoke(outputValidationSet,0)-(Double)method.invoke(outputOracle,0);
                    errorOutputWO=(Double)method.invoke(outputValidationSet,1)-(Double)method.invoke(outputOracle,1);
                    
                 
                    //AVGcontribution=(Math.abs(errorOutputRO)+Math.abs(errorOutputWO))/(i+1);
                    
                    //if(AVGcontribution<(3*meanAVGcontribution)){//consider the point in accuracy computation only if its under a threshold
                    if(distances[i]<cutoff){//does not consider in the accuracy computation  points too distant
                    
                        //RMSE
                   /* SEOutputRO=SEOutputRO+Math.pow(errorOutputRO,2);
                    logger.info( "error on "+outputname+"RO prediction for "+entry.getKey().toString().split("@")[0]+" = " +errorOutputRO );
                
                    
                    SEOutputWO=SEOutputWO+Math.pow(errorOutputWO,2);
                    logger.info(   "error on "+outputname+"WO prediction for "+entry.getKey().toString().split("@")[0]+" = " +errorOutputWO );
                    */
                        
                     //MAPE 
                    SEOutputRO=errorOutputRO/(Double)method.invoke(outputValidationSet,0);
                    
                
                    
                    
                    SEOutputWO=errorOutputWO/(Double)method.invoke(outputValidationSet,1);
                   
                    
                    SquaredErrors[0][i]=SEOutputRO;
                    SquaredErrors[1][i]=SEOutputWO;
                    
                    
                    //update the threshold
                    
                    //SUMavgComponent=SUMavgComponent+(Math.abs(errorOutputRO)+Math.abs(errorOutputWO));
                    //meanAVGcontribution=SUMavgComponent/Math.pow((i+1),2);
                    
                    }
                    else{
                    logger.info(   "error on "+outputname+"WO prediction for "+entry.getKey().toString().split("@")[0]+" = " +errorOutputWO );
                    logger.info( "error on "+outputname+"RO prediction for "+entry.getKey().toString().split("@")[0]+" = " +errorOutputRO );
                    logger.info( "point not considered during accuracy computation due to the high AVG contribution ("+distances[i]+"> 1* " );
                    }
                    
                }
            
             }
            RMSe=new Double[2];
/*RMSE
            RMSe[0]=Math.sqrt(SEOutputRO)/Neighbourshood.numInstances();
            RMSe[1]=Math.sqrt(SEOutputWO)/Neighbourshood.numInstances();
            */
            //MAPE
            RMSe[0]=(SEOutputRO)/Neighbourshood.numInstances();
            RMSe[1]=(SEOutputWO)/Neighbourshood.numInstances();
            SEOutputRO=0D;
            SEOutputWO=0D;
            //meanAVGcontribution=10000D;
            //SUMavgComponent=0;
            logger.info("RESULT considering "+Parameter+" of  "+entry.getKey().toString().split("@")[0]+":"+"RMSERO ="+RMSe[0]+"  RMSEWO ="+RMSe[1]);
            rmse.put(entry.getKey(),RMSe);
     }
       return rmse;
   }
    
    
 
    
    public Double[]VarianceRMSE(Double[] RMSE){
        
    Double [] sd=new Double [2] ;
    
    
    for (int i=0;i<2;i++){
        
        sd[i]=0D;
    for (double error :SquaredErrors[i])
      {
         sd[i] = sd[i] + Math.pow(error-RMSE[i], 2);
      }
      sd[i]=Math.sqrt(sd[i]);
    }
    return sd;
    }
    
    
    
    protected Instances FilterInstances(String[] options) throws Exception{        
        
     
        RemoveWithValues remove = new RemoveWithValues();                         // new instance of filter
        
        remove.setOptions(options);                                            // set options
        
        remove.setInputFormat(this.m_Training);                          // inform filter about dataset **AFTER** setting options
           
        Instances newData = Filter.useFilter(this.m_Training, remove);   // apply filter
    
    return newData;
    }
    
    
    
    protected void SelectInstancesRP(InputOracle io) throws Exception{
        int index=DataSets.ARFFDataSet.attribute("ReplicationProtocol").index()+1;
        String[] options;
        switch((ReplicationProtocol)io.getForecastParam(ForecastParam.ReplicationProtocol)){
                            case TO:{
                                options = Utils.splitOptions("-C "+index+" -S "+1);
                                this.m_Training=FilterInstances(options);
                                 break;
                            }
                            case PB:{
                                options = Utils.splitOptions("-C "+index+" -S "+1.1+" -V ");
                                this.m_Training=FilterInstances(options);
                                options = Utils.splitOptions("-C "+index+" -S "+0.9);
                                this.m_Training=FilterInstances(options);
                                break;
                            }
                            default :{
                                options = Utils.splitOptions("-C "+index+" -S "+1+" -V ");
                                this.m_Training=FilterInstances(options);
                                break;
                            
                            }
                              
                        }
    
    }
    
    protected void SelectInstancesIL(InputOracle io) throws Exception{
        int index=DataSets.ARFFDataSet.attribute("ISOLATION_LEVEL").index()+1;
        String[] options;
        switch((IsolationLevel)io.getEvaluatedParam(EvaluatedParam.ISOLATION_LEVEL)){
                            case RR:{
                                options = Utils.splitOptions("-C "+index+" -S "+1);
                                this.m_Training=FilterInstances(options);
                                 break;
                            }
                            case RC:{
                                options = Utils.splitOptions("-C "+index+" -S "+1.1+" -V ");
                                this.m_Training=FilterInstances(options);
                                options = Utils.splitOptions("-C "+index+" -S "+0.9);
                                this.m_Training=FilterInstances(options);
                                break;
                            }
                            default :{
                                options = Utils.splitOptions("-C "+index+" -S "+1+" -V ");
                                this.m_Training=FilterInstances(options);
                                break;
                            
                            }
                              
                        }
    
    }
    
}
