/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ennio
 */
public class NamesParser {
    private FileReader fr ;
    private BufferedReader br;
    
    public NamesParser(String filename) throws IOException{
    try{
        fr = new FileReader(filename);
        br = new BufferedReader(fr); 
    }
    catch(IOException e){
        fr.close();
        throw e;
    }
    
    }
      
    public String getName() throws IOException{
        String line;
        try{
            
        while((line=br.readLine())!= null){
        
        if(line.split(":").length!=2 || line.contains(","))
            continue;
        
        return line.split(":")[0];
        
        }
        return null;
       
        }
        
        catch(Exception e){
         System.out.println("exit");
         fr.close();
         return null;
         
        }
    }
    
    
    }

