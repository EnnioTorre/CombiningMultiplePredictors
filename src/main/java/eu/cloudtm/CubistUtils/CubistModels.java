/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.log4j.Logger;

/**
 *
 * @author ennio
 */
public class CubistModels {
    
    static Logger logger = Logger.getLogger(CubistModels.class.getName());
    private Runtime run;
    private File dir,workdir;
    private String defcmd,command,filename;
    private Process p;
    private BufferedReader bf;
    
    public void CreateCubistModels(String dir) throws IOException, InterruptedException{
    
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      logger.info(" Cubist Models Creation Start/"); 
      logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      
        run=Runtime.getRuntime();
        this.dir=new File(dir);
        workdir=new File("CubistR2/cubist");
        
        if(workdir==null)
            throw new IOException("Cubist directory wrong");
        defcmd="./"+workdir.getPath()+" -f ";
        
        if (this.dir.isDirectory()){
            for(File f:this.dir.listFiles()){
            
                if(f.getName().endsWith(".names")){
                    filename=f.getName().split("\\.")[0];
                    command=defcmd+this.dir.getAbsolutePath()+"/"+filename;
                    logger.info(command);
                   p=run.exec(command);
                   p.waitFor();
                   if(!CheckFileExistence(filename))
                       throw new IOException(CubistModels.class+" :error during Cubist Models Creation in dir " + dir);
                }
            }
                    
            }
         logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
         logger.info(" /Models Created/");
         logger.info("Output file produced in "+filename);
         logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
      
    
    }
    
    private boolean CheckFileExistence(String file){
        
        for (File f:dir.listFiles()){
        
            if(f.getName().equals(file+".model"))
                return true;
        }
    
        return false;
    
    }
}
