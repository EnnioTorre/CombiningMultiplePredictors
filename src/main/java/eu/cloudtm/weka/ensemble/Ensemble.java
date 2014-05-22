/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.ensemble;


import eu.cloudtm.DataUtility.OutputsMap;


import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import batching.offline.BatchingPaoloAnalyticalOracle;
import eu.cloudtm.weka.Cubist.Cubist;
import eu.cloudtm.weka.Tas.Tas;
import eu.cloudtm.weka.batching.Batching;
import java.util.HashMap;
import java.util.Map.Entry;

import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities.Capability;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

import org.apache.log4j.Logger;
/**
 *
 * @author ennio
 */
public class Ensemble extends AbstractClassifier {
    static Logger logger = Logger.getLogger(Ensemble.class.getName());
    
    
    private AbstractClassifier classifier;
    private final static int counter=1;
    private static HashMap<String,Integer> register=new HashMap();
    private String Target=null;
    
    static{
    
        int i=0;
        for (OutputsMap m:OutputsMap.values()){
        
            register.put(m.toString(),counter);
            logger.info(m.toString()+"added to Ensembe class");
        }
            
    
    }
    
    
    public Ensemble(){
    
    super();
    }

    @Override
    public void buildClassifier(Instances i) throws Exception {
        try{
        getCapabilities().testWithFail(i);
        Target=i.classAttribute().name();
        testonTarget();
        
        int temp=register.get(Target);
        
        if (!(temp>1)){
        
            classifier=new Batching();//new Tas();
            classifier.buildClassifier(i);
            temp++;
            logger.info("@"+Ensemble.class+"Tas instance has been generated "+
                    "for"+Target+" prediction @");
            
        }
        else{
        
            classifier=new Cubist();
            classifier.buildClassifier(i);
            temp++;
            logger.info("@"+Ensemble.class+"Cubist instance has been generated "+
                    "for"+Target+" prediction @");
        }
       
        register.put(Target, temp);
       logger.info("@ "+classifier.getClass().getName()+" classifier built @");
        
        }
        catch (OracleException e){
             e.printStackTrace();
            throw new Exception(e);
           
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        
    }

    @Override
    public double classifyInstance(Instance instnc)throws Exception {
                                  
        return classifier.classifyInstance(instnc);// instnc.value(instnc.numAttributes()-1);
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
            throw new Exception(Ensemble.class.getName()+"target not set");
    }
    
    private String getTarget(Instances i) throws Exception{
    
        String tar;
        tar=i.classAttribute().name();
        tar=OutputsMap.valueOf(tar).getTarget();
        //tar=tar.substring(0,tar.length()-3);
        return tar;
    }
    
    @Override
    public String toString(){
    
        String info="Ensemble Info Resume \n";
        for(Entry<String,Integer> e:register.entrySet())
         info+="[ For-> "+e.getKey()+" prediction "+e.getKey()+" oracle inst. ]\n" ;
    
        return info;
    }
    
}
