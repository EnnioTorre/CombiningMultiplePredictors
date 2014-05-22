/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleThreads;

import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Properties;
import morphr.MorphR;

/**
 *
 * @author ennio
 */
public class CubistThread extends OracleThread {
    
        private String target;
        private DatasetOutputOracle Output;
        private InputOracle i;
        private String CubistID;

        public CubistThread(String Target) {
            target=Target;
            Output=new DatasetOutputOracle();
            
        }
        
        public CubistThread() {
            target=null;
            Output=new DatasetOutputOracle();
            
        }
        
      
        
       
       public static void main( String[] args ) throws Exception{
        
            
             try {  
                // System.out.println("Input recieved");
                 ThreadInput input=getInput(args[0]);
                 //System.out.println("Input deserialized"+input.getTarget());
                 
                  String target=input.getTarget();
                  DatasetOutputOracle Output=new DatasetOutputOracle();
                  InputOracle i=input.getInput();
                 // String CubistID=input.getCubistID();
                  Properties CubistProp=input.getCubistProperties();
              // System.out.println("Start Prediction");
                MorphR morphR=new MorphR();
                
                if(CubistProp!=null)
                morphR.SetMoprhR(input.getCubistProperties());
                 
                 if (target!=null){
                   Method method;
                   OutputOracle prediction=morphR.forecast(i);
            
                method = DatasetOutputOracle.class.getDeclaredMethod("set"+target,int.class, double.class);
                 Method method2;
            
                method2 = OutputOracle.class.getDeclaredMethod(target,int.class);
                method.invoke(Output,0,(Double)method2.invoke(prediction, 0));
                method.invoke(Output,1,(Double)method2.invoke(prediction, 1));
                //method.invoke(Output,1,prediction.throughput(1));
                
                
                 }
                 
                 else
                     for (Field f: DatasetOutputOracle.class.getDeclaredFields()){
                     
                          Method method;
                          OutputOracle prediction=morphR.forecast(i);
            
                          method = DatasetOutputOracle.class.getDeclaredMethod("set"+f.getName(),int.class, double.class);
                          Method method2;
            
                          method2 = OutputOracle.class.getDeclaredMethod(f.getName(),int.class);
                          method.invoke(Output,0, method2.invoke(prediction, 0));
                          method.invoke(Output,1, method2.invoke(prediction, 1)); 
                     
                     }
                //System.out.println("$"+i.toString());  
                System.out.println("$"+Output); 
            }
             
             /*
             catch (IllegalAccessException ex) {
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            } catch (IllegalArgumentException ex) {
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            } catch (InvocationTargetException ex) {
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            }
            catch (NoSuchMethodException ex) {
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            } catch (SecurityException ex) {
                ex.printStackTrace();
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            }
             catch (OracleException ex) {
                 ex.printStackTrace();
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            }*/
            catch (Exception ex) {
                System.out.println(printErrors(ex));
                ex.printStackTrace();
                throw new RuntimeException(TasThread.class+"Error during TAS prediction,"+ex);
            
               
            }
                
                    }  
        
        
       
        
        public void setCubistID(String s){
        
            CubistID=s;
        }
         
        
        }
    
    
    

