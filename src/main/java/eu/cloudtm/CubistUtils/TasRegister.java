/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
import org.apache.log4j.Logger;

/**
 *
 * @author ennio
 */
public class TasRegister {
    static Logger logger = Logger.getLogger(TasRegister.class.getName());
    private static HashMap <String,Integer> id =new HashMap <String,Integer>();
    private static ArrayList<String> listprop=new ArrayList<String>();
    private static int counter;
    
    
    public static void registerOracle(String Oracle) throws IOException{
    
        
            
        id.put(Oracle,counter);
              
        String originaldir="";
        
        
        
        String dirname=originaldir;
        
        if(counter>0){
        dirname=dirname+counter;
        
        
        }
        listprop.add(counter,"");
        logger.info("New Cubist Oracle Registered ("+counter+")");
        counter++;
        
        
        
    }


}
