/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Configuration;

import java.io.File;


/**
 *
 * @author ennio
 */
public class ProbingConfiguration extends Configuration {
    
    private static ProbingConfiguration myself=null;
    public  ProbingConfiguration(){
       super("conf/Probing/Probing.properties");
    }
    
           
   
    public Class getMainOracle() throws ClassNotFoundException{
    
        Class c;
        String o= defaultProps.getProperty("MainOracle");
        checkvalue(o);
        c=Class.forName(o);
        return c; 
    
    }
    
    public Class getSecondaryOracle() throws ClassNotFoundException{
         Class c;
        String o= defaultProps.getProperty("SecondaryOracle");
        checkvalue(o);
        c=Class.forName(o);
        return c;
    
    }
    
    public String getTarget(){
    
        String o= defaultProps.getProperty("Target");
         checkvalue(o);
        return o; 
    
    }
    public String getTheshold(){
    
        String o= defaultProps.getProperty("Threshold");
         checkvalue(o);
        return o; 
    
    }
    
    public static ProbingConfiguration getInstance(){
        return (myself==null)?myself=new eu.cloudtm.Configuration.ProbingConfiguration():myself;
           
     
      
    }
    
   
}
