/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Dataset;

import eu.cloudtm.Configuration.Configuration;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import java.util.HashMap;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author ennio
 */


public interface Dataset {
       
        
    public Instances getARFFDataSet();
    public HashMap<Instance,OutputOracle> getValidationSet();
    public HashMap <Oracle,HashMap<Instance,OutputOracle> > getPredictiontionSet();
    public HashMap<String,Instance>getInstancesMap();
    
    public Configuration getConfiguration();
    
    
}
