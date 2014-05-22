/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package csv;

import eu.cloudtm.Configuration.Configuration;
import eu.cloudtm.Dataset.Dataset;
import eu.cloudtm.autonomicManager.oracles.Oracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import org.apache.log4j.Logger;
import weka.core.Instance;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */
public class PrintDataOnCsv {
    
    private static String Names;
    private static String Values;
    private static boolean isInit=false;
    private static HashMap<Instance,String>csvHash=new HashMap<Instance,String>();
    private static Logger logger = Logger.getLogger(PrintDataOnCsv.class.getName());

     private static void PrintStringSet(Dataset data,PrintWriter out) throws Exception{
     
         
         
         for(Map.Entry<Instance,OutputOracle> outerentry:data.getValidationSet().entrySet()){
             
             Names="csvPath";
                 Values=csvHash.get(outerentry.getKey());
                 int i=0;
                for(double d:outerentry.getKey().toDoubleArray()){
                
                    Names=Names+","+data.getARFFDataSet().attribute(i++).name();
                    Values=Values+","+d;
                }
             
             for(Map.Entry<Oracle,HashMap<Instance,OutputOracle> >innerentry:data.getPredictiontionSet().entrySet()){
                
                 
                
                StringTokenizer st =new StringTokenizer (innerentry.getValue().get(outerentry.getKey()).toString());
                while(st.hasMoreTokens()){
                
                    StringTokenizer st1 =new StringTokenizer (st.nextToken(),"=");
                    Names=Names+","+innerentry.getKey().toString().split("@")[0]+st1.nextToken();
                    Values=Values+","+st1.nextToken();
                }
                
             }
             
             StringTokenizer st =new StringTokenizer (outerentry.getValue().toString());
                while(st.hasMoreTokens()){
                
                    StringTokenizer st1 =new StringTokenizer (st.nextToken(),"=");
                    Names=Names+",Output"+st1.nextToken();
                    Values=Values+","+st1.nextToken();
                }
             if(!isInit){
             
                 out.println(Names);
                 out.println(Values);
                 isInit=true;
             }
             
             else
                  out.println(Values);   
         }
     
     }
     
     public static void setCsvPath(Instance i,String path,Dataset data){
             Configuration LK=data.getConfiguration();
         
         if (LK.isCSVOutputenable()){
         
             csvHash.put(i, path);
         
         }
         
     }
     
     
     
     public static void PrintCsvFile(Dataset data) {
            Configuration LK=data.getConfiguration();
            PrintWriter out=null;
            FileWriter fstream=null;
         if (LK.isCSVOutputenable()&&data.getValidationSet()!=null&&data.getPredictiontionSet()!=null){
         
         try{
           
             String Filename=LK.getCsvOutputDir()+"/"+getAfileName();
             
            while( new File(Filename).exists())
                  Filename=LK.getCsvOutputDir()+"/"+getAfileName();
             
              fstream = new FileWriter(Filename,false);//TODO change to false
             
              
         
              out = new PrintWriter(fstream);
              PrintStringSet(data,out);
             
         
         }
         catch(Exception e){
         
             logger.error("impossible to print Datasets on Csv"+e);
             e.printStackTrace();
         }
         finally{
         
             if(out!=null)
             
                 out.close();
         }
         
         }
         
     }
     
     public static void PrintStringOnCsvFile(String filepath,String towrite){
     
         PrintWriter out=null;
         FileWriter fstream=null;
         
         String header="";
         String Values="";
         boolean headerPrint=true;
         StringTokenizer st= new StringTokenizer(towrite,",");
         while(st.hasMoreTokens()){
             
             header=header+st.nextToken()+",";
             Values=Values+st.nextToken()+",";
         
         }
         
         try{
           
             
              if( new File(filepath).exists())
                  headerPrint=false;
              fstream = new FileWriter(filepath,true);
              
              out = new PrintWriter(fstream);
              if(headerPrint)
              out.println(header);
              out.println(Values);
             
         
         }
         catch(Exception e){
         
             logger.error("impossible to print Datasets on Csv"+e);
             
             e.printStackTrace();
         }
         finally{
         
             if(out!=null)
             
                 out.close();
         }
         
     
     }
     
     private static String getAfileName(){
       char c []={'a','b','c','d','e','f','g','h','i','l','m','n','o','p','r','s','t','x','y','z',};
       String word="CsvTrainingSet-";
       Random generator = new Random(); 
       int index=0;
       for(int i=0;i<10;i++){
           
           index=generator.nextInt(20);
           word=word+c[index];
       
       }
       word=word+index+".csv";
       return word;  
     }
}
