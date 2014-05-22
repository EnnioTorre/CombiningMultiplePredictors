/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package testsimulator;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */



import CsvOracles.params.CsvRgParams;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Knearestneighbourg;
import static eu.cloudtm.Knn.DataSets.predictionResults;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.Configuration.KnnConfiguration;
import csv.CsvReader;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import weka.core.Instance;
import eu.cloudtm.DataUtility.ParameterClassConversion;
import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.ReplicationProtocol;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.learner.GenericLearnerDataset;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;


public final class testsimulatorforecast {
static String path="csvfile/new/0.csv";


 static Logger logger = Logger.getLogger(testsimulatorforecast.class.getName());   

   public static void main(String[] args) {
    PropertyConfigurator.configure("conf/log4j.properties");
      
      logger.info("primo append");
      
      int choose=Integer.parseInt(args[0]);
      //int choose=2;
      
      if(choose==1){
      logger.info("^^DATASET CREATION SETTED UP^^");
    try {
        DataSets i=new DataSets(args[1]);
        //InputOracle csvI =new CsvReader(new CsvRgParams(path));
    } catch (Exception ex) {
        ex.printStackTrace();
    }
      }    
        
   
    if(choose==2){ 
        logger.info("^^DATASET CREATION FROM DIR AND TEST SETTED UP^^");
        Test_on_Testset test=new Test_on_Testset();
    try {
        DataSets i=new DataSets(args[1]);
        test.test(args[2]);
        
        
        //Knearestneighbourg kn= new Knearestneighbourg("EuclideanDistance","-D",10,"throughput");
        //OutputOracle result=kn.forecast(csvI);
        //Instance n=kn.getNeighboughood().instance(3);
        //System.out.println(kn.getNeighboughood());
        //System.out.println(DataSets.InstancesMap.get(n.toStringNoWeight()));
        //System.out.println(result.throughput(0)+result.toString());
        //System.out.println(result.throughput(0)+result.toString());
    } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
    } catch (InstantiationException ex) {
        ex.printStackTrace();
    } catch (IllegalAccessException ex) {
        ex.printStackTrace();
    } catch (OracleException ex) {
        ex.printStackTrace();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }   
      
   if(choose==3){ 
        logger.info("^^TEST SETTED UP^^");
        Test_on_Testset test=new Test_on_Testset();
    try {
        DataSets i=new DataSets();
        test.test(args[1]);
        
        
    } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
    } catch (InstantiationException ex) {
        ex.printStackTrace();
    } catch (IllegalAccessException ex) {
        ex.printStackTrace();
    } catch (OracleException ex) {
        ex.printStackTrace();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }
        

    if(choose==4){
      logger.info("^^DATSET CREATION SETTED UP^^");
    try {
        DataSets i=new DataSets();
        //InputOracle csvI =new CsvReader(new CsvRgParams(path));
    } catch (Exception ex) {
        ex.printStackTrace();
    }
      } 
    
    if(choose==5){ 
        logger.info("^^TEST SETTED UP (Boosting INCLUDED)^^");
        Test_on_Testset test=new Test_on_Testset();
        test.addBosting(true);
    try {
        DataSets i=new DataSets();
        GenericLearnerDataset r=new GenericLearnerDataset(args[2]);
        test.test(args[1]);
        
        
    } catch (ClassNotFoundException ex) {
        ex.printStackTrace();
    } catch (InstantiationException ex) {
        ex.printStackTrace();
    } catch (IllegalAccessException ex) {
        ex.printStackTrace();
    } catch (OracleException ex) {
        ex.printStackTrace();
    } catch (Exception ex) {
        ex.printStackTrace();
    }
    }
        
  }
}

