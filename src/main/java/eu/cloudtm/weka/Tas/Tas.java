/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.Tas;

/**
 *
 * @author ennio
 */

import eu.cloudtm.CubistUtils.CubistModelsData;
import eu.cloudtm.CubistUtils.CubistModels;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.OracleHandlers.OracleHandlers;



import eu.cloudtm.autonomicManager.oracles.InputOracle;

import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import java.io.BufferedReader;
import java.io.InputStreamReader;




import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities.Capability;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import org.apache.log4j.Logger;
import testsimulator.Test_on_Testset;
/**
 *
 * @author ennio
 */
public class Tas extends AbstractClassifier {
    static Logger logger = Logger.getLogger(eu.cloudtm.weka.Tas.Tas.class.getName());
    
    
    private String Target,methodTarg;
    
    public Tas(){
    
    super();
    }

    @Override
    public void buildClassifier(Instances i) throws Exception {
        try{
        getCapabilities().testWithFail(i);
        
        Target=i.classAttribute().name();
        testonTarget();
            
        methodTarg=OutputsMap.valueOf(Target).getTarget();
            
        
         if(Test_on_Testset.Tmodels){ //TO DO AVOID TO USE THIS 
        CubistModelsData t=new CubistModelsData("conf/tas/gmu/tpc/cubist/demo_all");
        t.CreateTasData();
        CubistModels cm= new CubistModels();
        cm.CreateCubistModels("conf/tas/gmu/tpc/cubist/demo_all");
         }
         logger.info("Tas CLASSIFIER built");
        
        }
        catch (OracleException e){
             e.printStackTrace();
            throw new Exception(e);
           
        }
        catch (Exception e){
            throw new Exception(e);
        }
        
    }

    @Override
    public double classifyInstance(Instance instnc)throws Exception {
        
        
        double output=0D;
        try{       
            InputOracle inst=DataConverter.FromInstancesToInputOracle(instnc);
        
            //String target=BoostingConfiguration.getInstance().getTarget();
                   
            
            
            OutputOracle dat=new DatasetOutputOracle();
            
            String Options=OracleHandlers.Options;
            String classpath=OracleHandlers.classpath;
            String Argument=OracleHandlers.getInputString(inst,methodTarg,this.toString(),null);
            String command="java "+Options+" -classpath "+classpath+" eu.cloudtm.OracleThreads.TasThread "+Argument;
            Process p;
            Runtime run=Runtime.getRuntime();
             
             p=run.exec(command);
             BufferedReader buff=new BufferedReader(new InputStreamReader(p.getInputStream()));
             
             String line,precline;
             precline=null;
            while ((line=buff.readLine())!=null){
                System.out.println("Output------>"+line);
                precline=line;
                }
            
            if((precline)!=null&&(precline.contains("$"))){
                dat=OracleHandlers.getOutputOracle(precline);          
            }
            else
                throw new Exception("Error during Tas prediction:Tas Thread OUtput Wrong \n"+line);
            
           //System.out.println(dat); 
             
             
             
             p.waitFor();
            buff.close();
            
            
            
            if(Target.equals("throughput"))
                output=dat.throughput(0)+dat.throughput(1);
            
            else if(Target.equals("abortRate"))
                output=dat.abortRate(0)+dat.abortRate(1);
        
            else if(Target.equals("responseTimeRO"))
                output=dat.responseTime(0);
            else 
                output=dat.responseTime(1);
            
             }
        
        
        catch(Exception e ){
             
            logger.error(e);
            e.printStackTrace();
            throw e;
        }
                            
        return output;// instnc.value(instnc.numAttributes()-1);
    }

    @Override
    public double[] distributionForInstance(Instance instnc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Capabilities getCapabilities() {
        Capabilities cap=super.getCapabilities();
        
        cap.enable(Capability.NUMERIC_ATTRIBUTES);
        cap.enable(Capability.MISSING_VALUES);
        
        cap.enable(Capability.NUMERIC_CLASS);
        
        return cap;
        
    }
    
    @Override
    public void setOptions(String [] s){
    
        int i=0;
        for(String d:s){
        
            if(d.equals("target")){
                Target=s[i+1];
                break;
            }
            i++;
        }
        
    }
    
    private void testonTarget() throws Exception{
    
        if(Target==null)
            throw new Exception(Tas.class.getName()+"target not set");
        
    }
    
    
     public void setTarget(String Target){
    
        methodTarg=OutputsMap.valueOf(Target).getTarget();
    
    }
    
   
    
}

