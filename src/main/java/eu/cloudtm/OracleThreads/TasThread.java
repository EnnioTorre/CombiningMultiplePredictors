/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleThreads;

import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import tasOracle.common.TasOracle;

/**
 *
 * @author ennio
 */
public class TasThread extends OracleThread {
    
       
        private String target;
        private DatasetOutputOracle Output;
        private InputOracle i;

        public TasThread(String Target) {
            target=Target;
            Output=new DatasetOutputOracle();
            
        }
        
        public TasThread() {
            target=null;
            Output=new DatasetOutputOracle();
            
        }
        
        
        
        
    
       public static void main (String[] args) throws Exception{
        
            
             try {  
               
                 ThreadInput input=getInput(args[0]);
                 
                  String target=input.getTarget();
                  DatasetOutputOracle Output=new DatasetOutputOracle();
                  InputOracle i=input.getInput();
                  String CubistID=input.getCubistID();
                 
                TasOracle Tas=new TasOracle();
                 
                 if (target!=null){
                   Method method;
                   OutputOracle prediction=Tas.forecast(i);
            
                method = DatasetOutputOracle.class.getDeclaredMethod("set"+target,int.class, double.class);
                 Method method2;
            
                method2 = OutputOracle.class.getDeclaredMethod(target,int.class);
                method.invoke(Output,0, method2.invoke(prediction, 0));
                method.invoke(Output,1, method2.invoke(prediction, 1)); 
                 
                 }
                 
                 else
                     for (Field f: DatasetOutputOracle.class.getDeclaredFields()){
                     
                          Method method;
                          OutputOracle prediction=Tas.forecast(i);
            
                          method = DatasetOutputOracle.class.getDeclaredMethod("set"+f.getName(),int.class, double.class);
                          Method method2;
            
                          method2 = OutputOracle.class.getDeclaredMethod(f.getName(),int.class);
                          method.invoke(Output,0, method2.invoke(prediction, 0));
                          method.invoke(Output,1, method2.invoke(prediction, 1)); 
                     
                     }
                
                 System.out.println("$ "+Output);
                 
            } 
             catch (Exception ex) {
                System.out.println(printErrors(ex));
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            }
                
                    }  
        
        
       
        
        }
    

