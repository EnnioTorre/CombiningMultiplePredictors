/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;


import java.nio.file.Files;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import utils.PropertyReader;
import static java.nio.file.StandardCopyOption.*;
import javax.xml.crypto.KeySelectorException;
import org.apache.log4j.Logger;
/**
 *
 * @author ennio
 */
public class MorphRregister {
    static Logger logger = Logger.getLogger(MorphRregister.class.getName());
    private static int counter=0;
    private static ArrayList<Properties> listprop=new ArrayList<Properties>();
    private static HashMap <String,Integer> id =new HashMap <String,Integer>();
    
    public MorphRregister(){
    
       
    }
    
    public static Properties getCubistProperty(String Oracle) throws KeySelectorException{
    
       if(id.containsKey(Oracle)){
        int ID=id.get(Oracle);
        return listprop.get(ID);
       }
       throw new KeySelectorException(Oracle+" has not been registered");
       
    }
    
    private static Properties getnewproperties(String dirpath){
    
        Properties prop=new Properties();
        
        
        
        prop.setProperty("modelFilenameAbort2PC",dirpath+"/abortFS"); 
        prop.setProperty("modelFilenameThroughput2PC",dirpath+"/throughputFS"); 
        prop.setProperty("modelFilenameReadOnly2PC",dirpath+"/roResponseFS"); 
        prop.setProperty("modelFilenameWrite2PC",dirpath+"/wrResponseFS"); 
        prop.setProperty("modelFilenameAbortPB",dirpath+"/abortFS"); 
        prop.setProperty("modelFilenameThroughputPB",dirpath+"/throughputFS"); 
        prop.setProperty("modelFilenameReadOnlyPB",dirpath+"/roResponseFS"); 
        prop.setProperty("modelFilenameWritePB",dirpath+"/wrResponseFS"); 
        prop.setProperty("modelFilenameAbortTO",dirpath+"/abortFS"); 
        prop.setProperty("modelFilenameThroughputTO",dirpath+"/throughputFS"); 
        prop.setProperty("modelFilenameReadOnlyTO",dirpath+"/roResponseFS");
        prop.setProperty("modelFilenameWriteTO",dirpath+"/wrResponseFS"); 
        prop.setProperty("oracleDir", dirpath);
        
        return prop;
        
    }
    
    public static void registerOracle(String Oracle) throws IOException{
    
         if(id.containsKey(Oracle)){
        logger.warn(Oracle+"has been already registered !! it will not be registered again !!");
        return;
       }
            
        id.put(Oracle,counter);
              
        String originaldir=getOriginalMorphRdir();
        
        
        
        String dirname=originaldir;
        
        if(counter>0){
        dirname=dirname+counter;
        copy(originaldir, dirname);
        
        }
        listprop.add(counter,getnewproperties(dirname));
        logger.info("New Cubist Oracle( "+Oracle+" ) Registered ("+counter+")");
        counter++;
        
        
        
    }
    
    
    public static String getMorphRInstanceDir(String Oracle) throws Exception{
    
    
     
    
        if(id.containsKey(Oracle)){
        
            return listprop.get(id.get(Oracle)).getProperty("oracleDir");
        }
        throw new Exception(Oracle+" has not been registered");
        
    }
    
    private static void copy(String input,String output) throws IOException{
    
    
        File in=new File(input);
        File out=new File(output);
        
        if(!in.exists()){
        
            throw new IOException("Impossible to copy from"+input+" because it does not exist");
        }
        
        if(!out.exists())
            out.mkdir();
        
        for(File f:in.listFiles()){
            
            Files.copy(f.toPath(),new File(out+"/"+f.getName()).toPath(), REPLACE_EXISTING);
        }
        
        
    }
    
    public static String getOriginalMorphRdir() throws IOException{
    
        String dir=PropertyReader.getString("modelFilenameThroughput2PC","/conf/MorphR/MorphR.properties");
        File f=new File (dir);
        //System.out.println(f.toPath().getParent().toString());
        f=f.toPath().getParent().toFile();
        
        if (!f.exists())
            throw new IOException("MorphR models dir does not exist");
        logger.info("The Original MorphR properties dir is:"+ f.toPath().getParent());
        dir=f.getPath();
        //System.out.println(dir);
        return dir;
    }
    
}
