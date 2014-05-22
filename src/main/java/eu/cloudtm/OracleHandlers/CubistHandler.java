/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleHandlers;


import eu.cloudtm.CubistUtils.CubistModels;
import eu.cloudtm.CubistUtils.CubistModelsData;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.CubistUtils.MorphRregister;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import java.io.*;
import java.util.Properties;
import javax.xml.crypto.KeySelectorException;
import org.apache.log4j.Logger;



/**
 *
 * @author ennio
 */
public class CubistHandler extends OracleHandlers {
 static Logger logger = Logger.getLogger(CubistHandler.class.getName());
 private boolean isBuilt; 
// private MorphR Cubist;
 
 
 public CubistHandler() throws Exception{
 
    // BuildOracle();
 }
   
        
    @Override
    public void BuildOracle() throws Exception{
        
    
    try{
        
        //Cubist=new MorphR();
        
        
        MorphRregister.registerOracle(this.toString());
        
                 
        CubistModelsData t=new CubistModelsData(MorphRregister.getMorphRInstanceDir(this.toString()));
        t.CreateTasData();
        CubistModels cm= new CubistModels();
        cm.CreateCubistModels(MorphRregister.getMorphRInstanceDir(this.toString()));
        isBuilt=true;
        logger.info("oracle built with models from "+MorphRregister.getMorphRInstanceDir(this.toString()));
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
    public OutputOracle forecast(InputOracle io) throws OracleException {
       
        DatasetOutputOracle dat=new DatasetOutputOracle();
        dat.initOnOracleError();
        Properties CubistProp=null;
        
        try {
            
             CubistProp=MorphRregister.getCubistProperty(this.toString());
        
        }catch(KeySelectorException ex){
        
            if(isBuilt)
                throw new OracleException(ex);
        
            logger.warn("[ ! ]- Cubist Oracle has not been generated,it will use already provided models");
        }
        
        try{    
            
            String Argument=getInputString(io,null,this.toString(),CubistProp);
            String command="java "+Options+" -classpath "+classpath+" eu.cloudtm.OracleThreads.CubistThread "+Argument;
            Process p;
            Runtime run=Runtime.getRuntime();
            // logger.info(command);
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
                throw new Exception("Error during Cubist prediction:Cubist Thread OUtput Wrong \n"+precline);
            
           // System.out.println(dat); 
             
             
             
             p.waitFor();
            buff.close();
            
        } catch (Exception ex) {
            throw new OracleException(ex);
        }
        return dat;
    }
    
   

}
