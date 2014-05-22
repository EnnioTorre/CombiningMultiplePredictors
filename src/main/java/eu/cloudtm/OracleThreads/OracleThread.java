/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleThreads;

import eu.cloudtm.DataUtility.DatasetOutputOracle;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.xml.bind.DatatypeConverter;

/**
 *
 * @author ennio
 */
 abstract class OracleThread {
    
     public static void main(String[] args)throws Exception{};
     
  
    
     public static ThreadInput getInput(String s) throws IOException, ClassNotFoundException{
        
           // byte[] stream=s.getBytes();
            byte []c=DatatypeConverter.parseBase64Binary(s);
            ByteArrayInputStream io=new ByteArrayInputStream(c); 
            ObjectInput i=new ObjectInputStream(io);
            ThreadInput o= (ThreadInput)i.readObject();
            io.close();
            
            return o;
        }
     
     public static String printErrors(Exception e){
     
         String ErrorStack="";
         for(StackTraceElement r:e.getStackTrace())
             ErrorStack+= r.toString()+"\n";
         return ErrorStack;
     }
}
