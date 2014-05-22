/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author ennio
 */
public abstract class Configuration {
    static Logger logger = Logger.getLogger(Configuration.class.getName());
    protected Properties defaultProps;
    protected static Configuration myself=null; 
    
    public Configuration(String configurationFile){
        try{
        defaultProps = new Properties();
        FileInputStream in = new FileInputStream(configurationFile);
        defaultProps.load(in);
        in.close();
        }
        
        catch(FileNotFoundException e){
         logger.warn(e.getStackTrace()[0]);
         
      }
      
      catch(IOException ef){
        
          
          logger.warn(ef.getStackTrace()[0]);
          
      }
    }
    
    protected void checkvalue(Object o){

       if(o==null)
           throw new NullPointerException(Thread.currentThread().getStackTrace()[1]+" cannot find the required property");
    
}  
    
     public static Configuration getInstance(){
         return null;
     }
     
     public boolean isTasARFFenable(){
     if (defaultProps.getProperty("TasARFF").equals("true"))
         return true;
     return false;
    }
    
    public boolean isDAGSARFFenable(){
     if (defaultProps.getProperty("DAGSARFF").equals("true"))
         return true;
     return false;
    }
    public boolean isMorphRARFFenable(){
     if (defaultProps.getProperty("MorphRARFF").equals("true"))
         return true;
     return false;
    }
    
    public boolean isValidationSetARFFenable(){
     if (defaultProps.getProperty("ValidationSetARFF").equals("true"))
         return true;
     return false;
    }
    
    public boolean isCSVOutputenable(){
     if (defaultProps.getProperty("CSVOutput").equals("true"))
         return true;
     return false;
    }
    
    public Class[] getOracles() throws ClassNotFoundException{
     StringTokenizer st=new StringTokenizer(defaultProps.getProperty("Oracles"),",");
     checkvalue(st);
     Class Oracles[]=new Class[st.countTokens()];
     int i=0;
     
     while(st.hasMoreElements()){
         
         Oracles[i++]=Class.forName(st.nextToken());
          
         
     }
     return Oracles;
    }
    
    public String getOracleInputDescription(){
        String o=defaultProps.getProperty("ARFFwithInputOracleParameterpath");
        checkvalue(o);
        return o; 
    
    }
    
     public String getCsvOutputDir(){
       
        String filename=defaultProps.getProperty("CsvOutputDirectory");
       checkvalue(filename);
        if (!filename.equals(" ")){
       File f=new File(filename);
       if(!f.exists())
           f.mkdir();
       filename=filename+"/";
    }
       return filename;
    }
    
    public String getCsvInputDir(){
       
       String o= defaultProps.getProperty("CsvInputDirectory");
        checkvalue(o);
        return o; 
    }
    
}
