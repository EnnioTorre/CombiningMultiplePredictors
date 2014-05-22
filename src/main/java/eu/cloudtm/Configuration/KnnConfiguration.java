/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cloudtm.Configuration;

import java.io.File;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */
public class KnnConfiguration extends Configuration{
    static Logger logger = Logger.getLogger(KnnConfiguration.class.getName());
    private static KnnConfiguration myself=null;
    
    public  KnnConfiguration(){
        super("conf/K-NN/KNN.properties");
    }
     
   
    public String getCombiner(){
    
        String o= defaultProps.getProperty("combiner");
         checkvalue(o);
         return o; 
    
    }
    
    public static KnnConfiguration getInstance(){
        return (myself==null)?myself=new KnnConfiguration():myself;
           
    }
    
   
    
    public String getknearestneighbourg(){
    
       String o= defaultProps.getProperty("K-number");
        checkvalue(o);
        return o; 
    }
    
    public String getDistanceFunction(){
    String o= defaultProps.getProperty("DistanceFunction");
     checkvalue(o);
        return o; 
    
    }
    
    public String getCutoff(){
    
       String o= defaultProps.getProperty("CutoffValue");
        checkvalue(o);
        return o; 
    }
    
    public String getTarget(){
    
        String o=defaultProps.getProperty("Target");
         checkvalue(o);
        return o; 
    
    }
    
    public String getIsNormalized(){
    if (defaultProps.getProperty("Normalization").equals("true"))
         return "";
     return "-D";
    
    }
    
    
    
    
    
}
