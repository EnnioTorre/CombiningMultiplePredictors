/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.Configuration;

import java.util.StringTokenizer;
import org.apache.log4j.Logger;

import weka.classifiers.AbstractClassifier;

/**
 *
 * @author ennio
 */
public class GenericLearnerConfiguration extends Configuration {

    static Logger logger = Logger.getLogger(GenericLearnerConfiguration.class.getName());
    private static GenericLearnerConfiguration myself;
    
    public GenericLearnerConfiguration() {
        super("conf/GenericLearner/GenericLearner.properties");
    }
    
    public String [] getOptions(){

       
     StringTokenizer st=new StringTokenizer(defaultProps.getProperty("Options"),",");
     checkvalue(st);
     String Options[]=new String[st.countTokens()];
     int i=0;
     
     while(st.hasMoreElements()){
         
         Options[i++]=st.nextToken();
          
         
     }
     return Options;
    }
    
     public static GenericLearnerConfiguration getInstance(){
        return (myself==null)?myself=new GenericLearnerConfiguration():myself;
     }
    
    @Override
      public Class[] getOracles() throws ClassNotFoundException{
      
          StringTokenizer st=new StringTokenizer(defaultProps.getProperty("Oracles"),",");
         
          if(st.countTokens()>1)
             throw new ClassNotFoundException("Generic learner does not need more than one oracle");
             
             
          checkvalue(st);
         Class Oracles[]=new Class[st.countTokens()];
         Oracles[0]=Class.forName(st.nextToken());
         return Oracles;
      
      }
    
    
     protected void checkCLass(Class o) throws ClassNotFoundException{

         AbstractClassifier c;
        
            if(o==null||!(o.isAssignableFrom(AbstractClassifier.class)))
                throw new ClassNotFoundException(Thread.currentThread().getStackTrace()[1]+" cannot find the required property");
          
     
   }
}
   
