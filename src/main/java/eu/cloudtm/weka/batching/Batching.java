/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.batching;

import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import batching.offline.BatchingPaoloAnalyticalOracle;
import eu.cloudtm.Configuration.BatchingConfigurator;
import weka.classifiers.AbstractClassifier;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author ennio
 */
public class Batching extends AbstractClassifier {
    
    private BatchingPaoloAnalyticalOracle bc;
    private String Target;
    
    @Override
    public void buildClassifier(Instances i) throws Exception {
        getCapabilities().testWithFail(i);
        
        Target=i.classAttribute().name();
        
        BatchingConfigurator.getInstance().SetAccuracy();
        BatchingConfigurator.getInstance().setCutOff();
        bc =new BatchingPaoloAnalyticalOracle();
        
                
   }
    
    @Override
     public double classifyInstance(Instance instnc)throws Exception {
        InputOracle inst=DataConverter.FromInstancesToInputOracle(instnc);
        
        
        if(Target.equals("responseTimeWO"))
                return bc.forecast(inst).responseTime(1);
            else 
                return 0D;
     }
    
    
    public Capabilities getCapabilities() {
        Capabilities cap=super.getCapabilities();
        
        cap.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
        cap.enable(Capabilities.Capability.MISSING_VALUES);
        
        cap.enable(Capabilities.Capability.NUMERIC_CLASS);
        
        return cap;
        
    }
    
}
