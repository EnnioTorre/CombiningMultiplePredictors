/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testsimulator;

import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Knearestneighbourg;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.boosting.Boosting;
import eu.cloudtm.learner.GenericLearner;
import eu.cloudtm.learner.GenericLearnerDataset;
import eu.cloudtm.probing.Probing;
import eu.cloudtm.probing.ProbingDataSets;
import org.apache.log4j.PropertyConfigurator;
import weka.core.Instance;

/**
 *
 * @author ennio
 */
public class Demo {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        System.out.println( "Hello World!" );
        PropertyConfigurator.configure("conf/log4j.properties");
        
        //Boosting
        
        //LearnerDataset d=new GenericLearnerDataset("csvfile"); //Boosting dataset
        //Boosting b=new Boosting();
        
        //Knn
        
        DataSets dataknn=new DataSets("csvfile");//Knn dataset
        Knearestneighbourg k=new Knearestneighbourg();
        
        //Probing
        
         //ProbingDataSets dataP=new ProbingDataSets("csvfile"); //Probing dataset
         //Probing P= new Probing();
        
         //simple results
        
        //GenericLearner gn=new GenericLearner();
        
         Instance i=GenericLearnerDataset.ClassifierDataset.instance(1);
         InputOracle io=DataConverter.FromInstancesToInputOracle(i);
        /* System.out.println("Non so prediction"+(gn.forecast(io).responseTime(0)+gn.forecast(io).responseTime(1)));
         System.out.println("Knn prediction"+(k.forecast(io).responseTime(0)+k.forecast(io).responseTime(1)));
         System.out.println("Target : "+i.value(i.numAttributes()-1));
         
         System.out.println("Boosting prediction"+(b.forecast(io).responseTime(1)));
         System.out.println("Probing prediction"+(P.forecast(io).responseTime(0)+P.forecast(io).responseTime(1)));
        */
         
    }
}
