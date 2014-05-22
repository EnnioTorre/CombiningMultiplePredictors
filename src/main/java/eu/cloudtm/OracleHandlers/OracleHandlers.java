/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleHandlers;

import eu.cloudtm.CubistUtils.MorphRregister;
import eu.cloudtm.DataUtility.DatasetOutputOracle;
import eu.cloudtm.OracleThreads.ThreadInput;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author ennio
 */
public abstract class OracleHandlers implements Oracle{
    
    public static String Options;
    public static String classpath;
    
  static {
   
        Options="-Djava.library.path=lib/";
        //classpath="target/combinedLearner-1.1-SNAPSHOT.jar:.:runnable/dependencies/*";
        //classpath=".:dependencies/*";
        classpath=System.getProperty("java.class.path");
   }
    
    public void BuildOracle()throws Exception{};
    
    
    
        public static DatasetOutputOracle getOutputOracle(String Output) throws NoSuchMethodException, IllegalAccessException, IllegalArgumentException, InvocationTargetException{
        
        DatasetOutputOracle OO=new DatasetOutputOracle();
        String RO;
        String WO;
        
        try{
        for (Field f: DatasetOutputOracle.class.getDeclaredFields()){
        
            Method method=DatasetOutputOracle.class.getDeclaredMethod("set"+f.getName(),int.class, double.class);
            RO=Output.split(f.getName()+"RO=")[1];
            RO=RO.split(" ")[0];
            WO=Output.split(f.getName()+"WO=")[1];
            WO=WO.split(" ")[0];
            
            method.invoke(OO,0,Double.parseDouble(RO));
            method.invoke(OO,1,Double.parseDouble(WO));
        }
        }
        catch(IllegalArgumentException e){
        
            
            throw new NullPointerException();
            
        }
    
        
        
        
    
        return OO;
    }
        
        
      public static String getInputString(InputOracle i,String target,String Cubist,Properties p) throws Exception{
       
       ThreadInput Ti=new ThreadInput();
       Ti.setCubistID(Cubist);
       Ti.setTarget(target);
       Ti.setCubistProperties(p);
       Ti.setInput(i);
       
       ByteArrayOutputStream out= new ByteArrayOutputStream();
       ObjectOutput output=new ObjectOutputStream(out);
       output.writeObject(Ti);
       String S = DatatypeConverter.printBase64Binary(out.toByteArray());
       output.close();
       return S;
   }
    
    
}
