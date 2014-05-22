/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;

import common.GlobalValidator;
import config.FactoriesConfig;
import config.GlobalValidationConfig;
import config.ValidationConfiguration;
import factories.PrinterFactory;
import java.io.*;
import java.util.ArrayList;

import org.apache.log4j.Logger;
import printers.ValidationPrinter;
import xml.DXmlParser;
/**
 *
 * @author ennio
 */
public class CubistModelsData {
     static Logger logger = Logger.getLogger(CubistModelsData.class.getName());
     private File direc;
     
     public CubistModelsData(String dir) throws Exception{
     
        direc=new File(dir);
        if (direc.isDirectory()&&direc.exists()){
            for(File f:direc.listFiles()){
            
                if(!f.getName().endsWith(".names"))
                    f.delete();
            }
        }
        else throw new Exception("dir "+dir+" empty or it does not exist");
        
    }
     
     
      public boolean CreateTasData() throws Exception{
     
          ArrayList <Thread> Thlist=new ArrayList();
          String confname,filename;
          try{
          for(File f:direc.listFiles()){
             confname=f.getName().split("\\.")[0];
             filename=confname+".data";
             //System.out.println(filename);
             //System.out.println(confname);
             PrintData printthread=new PrintData(confname);
             Thlist.add(printthread);
             printthread.start();
             printthread.join();
             
          }
             logger.info("Model Creation < all thread have finished >");
             return true;
          }
          catch(Exception e){
              logger.error(e);
              throw e;
          
          }
          
      }
    
      private class PrintData extends Thread{
         private String confile;
        
         private PrintData(String confile) throws Exception{
         
             this.confile=confile;
             
             } 
     
        @Override
       public void run() {
      try{
      
      
      ValidationConfiguration conf = new DXmlParser<ValidationConfiguration>().parse("conf/Validation/"+confile+".xml");
        
      GlobalValidationConfig globalValidationConfig = conf.getGlobalValidationConfig();
      FactoriesConfig factoriesConfig = conf.getFactoriesConfig();
 
      String testPath = globalValidationConfig.getTestPath();
      
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      logger.info(" /Validation Start/");
      logger.info("Files From: " + testPath);
      logger.info("Validation file read form: "+confile);
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      
      String outPath = globalValidationConfig.getOutput();
      GlobalValidator gv = new GlobalValidator(globalValidationConfig, factoriesConfig);
      gv.validateAll();
      ValidationPrinter vp = PrinterFactory.buildPrinter(factoriesConfig, gv.getValidatedScenarios(), outPath);
      vp.printValidation();
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      logger.info(" /Validation Ended/");
      logger.info("Output file produced in " + outPath);
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      
      
      }
      catch(Exception e){
      
          logger.error(e);
          e.printStackTrace();
          throw new RuntimeException("error during CUBIST Data Creation"+e);
          
      }
    }
      }
}
