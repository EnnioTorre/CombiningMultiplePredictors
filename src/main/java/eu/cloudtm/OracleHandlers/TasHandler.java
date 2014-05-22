/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleHandlers;


import eu.cloudtm.CubistUtils.CubistModels;
import eu.cloudtm.CubistUtils.CubistModelsData;
import eu.cloudtm.CubistUtils.MorphRregister;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.OracleThreads.TasThread;
import eu.cloudtm.OracleThreads.ThreadInput;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import java.io.*;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;
import org.apache.log4j.Logger;
import testsimulator.Test_on_Testset;

/**
 *
 * @author ennio
 */
public class TasHandler extends OracleHandlers  {
static Logger logger = Logger.getLogger(TasHandler.class.getName());

   
public TasHandler() throws Exception {
        //BuildOracle();
    }




    @Override
    public void BuildOracle() throws Exception{
    try{
        
        CubistModelsData t=new CubistModelsData("conf/tas/gmu/tpc/cubist/demo_all");
        t.CreateTasData();
        CubistModels cm= new CubistModels();
        cm.CreateCubistModels("conf/tas/gmu/tpc/cubist/demo_all");
        
        logger.info("Tas classifier built");
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
        
        DatasetOutputOracle dat =new DatasetOutputOracle();
        try {
            
            String Argument=getInputString(io,null,this.toString(),null);
            String command="java "+Options+" -classpath "+classpath+" eu.cloudtm.OracleThreads.TasThread "+Argument;
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
                dat=getOutputOracle(precline);          
            }
            else
                throw new Exception("Error during Tas prediction:Tas Thread OUtput Wrong"+line);
            
           //System.out.println(dat); 
             
             
             
             p.waitFor();
            buff.close();
            
        } catch (Exception ex) {
            throw new OracleException(ex);
        }
        return dat;
    }

    
}
