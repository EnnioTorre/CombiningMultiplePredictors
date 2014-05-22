/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.CubistUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import org.apache.log4j.Logger;
import weka.core.Instances;


/**
 *
 * @author ennio
 */
public class CubistData {
   static Logger logger = Logger.getLogger(CubistData.class.getName());
   private File dir;
   private ArrayList <double []>list;
    
    public CubistData(String dir){
       list=new ArrayList();
       this.dir=new File(dir);
        if (this.dir.exists()&&this.dir.isDirectory()){
            for(File f:this.dir.listFiles()){
            
                if(!f.getName().endsWith(".names"))
                    f.delete();
            }
        }
        else logger.error("dir "+dir+" empty or it does not exist");
        
    }
    
    public boolean CreateCubistData(Instances instnc) throws IOException {
      
        NamesParser names;
        String attr,filename;
        int index;
        try{
            
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.info(" /Cubist data Creation (From Instances) Starts/"); 
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            
        for(File f:this.dir.listFiles()){
             filename=dir.getPath()+"/"+f.getName().split("\\.")[0]+".data";
             //System.out.println(filename);
             names=new NamesParser(f.getPath());
            if(!list.isEmpty()){
               list.clear();
            }
            
            while((attr=names.getName())!=null){
                
                index=instnc.attribute(attr).index();
                
                list.add(instnc.attributeToDoubleArray(index));
                
            }
           logger.info(filename +" :Cubist training set size (in terms of instances) "+list.get(1).length);
           PrintDataCubist(filename); 
        }
        
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        logger.info(" /Cubist data Creation (From Instances) Finished/"); 
        logger.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
        
        }
        catch(NullPointerException e){
         logger.error("error due to attribute selection:" +e);
         e.printStackTrace();
         throw e;
        }
        catch(IOException e){
         logger.error("impossible to create Cubist trainig set"+e);
         e.printStackTrace();
         throw e;
        }
        
        
        
        return true;
    }
    
    private void PrintDataCubist(String name) throws IOException{
        
            PrintWriter fw =new PrintWriter(name);
            String data="";
            try{
            for(int i=0 ;i< list.get(1).length;i++){
                for(int j=0 ;j< list.size()-1; j++)
                    data=data+list.get(j)[i]+",";
                    data=data+list.get(list.size()-1)[i];
                    //System.out.println(data);
                    fw.println(data);
                data="";
                    }
                    fw.close();
                    }
            catch(Exception e){
            
               logger.error(e + "during Cubist data printing");
                fw.close();
            }
       
    }
}
