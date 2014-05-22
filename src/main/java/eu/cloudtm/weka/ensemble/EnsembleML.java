/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.ensemble;


import eu.cloudtm.Configuration.BoostingConfiguration;
import eu.cloudtm.Configuration.GenericLearnerConfiguration;
import eu.cloudtm.DataUtility.OutputsMap;
import eu.cloudtm.autonomicManager.oracles.exceptions.OracleException;
import eu.cloudtm.weka.Cubist.Cubist;
import eu.cloudtm.weka.Tas.Tas;
import eu.cloudtm.weka.batching.Batching;
import eu.cloudtm.weka.filters.AddMissingValues;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.log4j.Logger;
import weka.classifiers.AbstractClassifier;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;

/**
 *
 * @author ennio
 */
public class EnsembleML extends AbstractClassifier {
     static Logger logger = Logger.getLogger(EnsembleML.class.getName());
    
    
    private AbstractClassifier classifier=null;
    private final static int counter=0;
    private static final HashMap<String,Integer> register=new HashMap();
    private String Target=null;
    private static final ArrayList <Class> clList;
    private static AbstractClassifier AnaliticalModel=null;
    
    
    static{
        clList=new ArrayList <>();
        try {
            int i=0;
              
            
            for (OutputsMap m:OutputsMap.values()){
            
                register.put(m.toString(),counter);
                logger.info(m.toString()+"added to Ensembe class");
            }
            
            for(Class c:BoostingConfiguration.getInstance().getOracles()){
            
                //AbstractClassifier clax=(AbstractClassifier)c.newInstance();
                
                
                if((Batching.class.isAssignableFrom(c) || Tas.class.isAssignableFrom(c))&&AnaliticalModel==null)
                    AnaliticalModel=(AbstractClassifier)c.newInstance();
                else
                clList.add(c);
                
                logger.info(c+" added to Ensembe class");
           }
        }
           catch(ClassNotFoundException | InstantiationException | IllegalAccessException e){
            
                new ClassNotFoundException("impossible to load classifiers, they are not classifiers or not specified"); 
            }
  
    }
    
    
    public EnsembleML() throws ClassNotFoundException{
    
    super();
    
    }

    @Override
    public void buildClassifier(Instances i) throws Exception {
        try{
       
       // System.out.println(i);
        getCapabilities().testWithFail(i);
        Target=i.classAttribute().name();
        testonTarget();
       // System.out.println("Ke palle"+i.numAttributes());
        
        
        
        int temp=register.get(Target);
       
        
        if (temp==0&&AnaliticalModel!=null){
            
            classifier=AnaliticalModel;
            classifier.buildClassifier(i);
           // clList.remove(temp);
            temp++;
            logger.info("@"+Ensemble.class+"  "+classifier.getClass().getName()+" instance has been generated "+
                    "for"+Target+" prediction @");
        
        }
        
        else if (((temp % clList.size())<clList.size())){
        //set the classifier from the list
            
            classifier=(AbstractClassifier)clList.get(temp % clList.size()).newInstance();
           
            
            if(classifier instanceof MultilayerPerceptron){
                String Options[]=GenericLearnerConfiguration.getInstance().getOptions();//{"-C","-I","-H","4"};
                classifier.setOptions(Options);
            }
                //((MultilayerPerceptron)classifier).setNormalizeAttributes(false);
            
            if((classifier instanceof Cubist)){
               
                i=AddMissingValues.addValues(i);
              
            }
            //logger.info("ffffffffffffff"+i);
                //classifier.setOptions(Options);
            classifier.buildClassifier(i);
            temp++;
            logger.info("@"+Ensemble.class+"  "+classifier.getClass().getName()+" instance has been generated "+
                    "for"+Target+" prediction @");
            
        }
        
        else{ 
        //if Iterations>num classifiers instances set the last in the list as classifier
            /*int CLindex=getClassifiers().length-1;
            classifier=classifier=this.getClassifier(CLindex);
            classifier.buildClassifier(i);
            temp++;*/
            logger.error("@"+EnsembleML.class+"  "+" ensemble cannot be generated "+
                    "for"+Target+" prediction @");
        }
       
        register.put(Target, temp);
       logger.info("@ "+classifier.getClass().getName()+" classifier built @");
        
        }
        catch (OracleException e){
             e.printStackTrace();
            throw new Exception(e);
           
        }
        catch (Exception e){
            e.printStackTrace();
            throw new Exception(e);
        }
        
    }

    @Override
    public double classifyInstance(Instance instnc)throws Exception {
       
       /* if(classifier instanceof Batching ){
             double output,result;
                 result=classifier.classifyInstance(instnc);
                  System.out.println("classifier batching out - "+ result);
                 output=Mean-result;
                 
                 System.out.println("batching out - "+ output);
                 return output;
            }*/
        double result=classifier.classifyInstance(instnc);
        //System.out.println("classifier batching out - "+ result);
        return result;//classifier.classifyInstance(instnc);// instnc.value(instnc.numAttributes()-1);
    }

    @Override
    public double[] distributionForInstance(Instance instnc) throws Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Capabilities getCapabilities() {
        Capabilities cap=super.getCapabilities();
        
        cap.enable(Capabilities.Capability.NUMERIC_ATTRIBUTES);
        cap.enable(Capabilities.Capability.MISSING_VALUES);
        
        cap.enable(Capabilities.Capability.NUMERIC_CLASS);
        
        return cap;
        
    }
    
    @Override
    public void setOptions(String [] s){
    
        int i=0;
        for(String d:s){
        
            if(d.equals("target")){
                Target=s[i+1];
                break;
            }
            i++;
        }
        
    }
    
    private void testonTarget() throws Exception{
    
        if(Target==null)
            throw new Exception(Ensemble.class.getName()+"target not set");
    }
    
    private String getTarget(Instances i) throws Exception{
    
        String tar;
        tar=i.classAttribute().name();
        tar=OutputsMap.valueOf(tar).getTarget();
        //tar=tar.substring(0,tar.length()-3);
        return tar;
    }
    
    @Override
    public String toString(){
    
        String info="Ensemble Info Resume \n";
        for(Map.Entry<String,Integer> e:register.entrySet())
         info+="[ For-> "+e.getKey()+" prediction "+e.getKey()+" oracle inst. ]\n" ;
    
        return info;
    }
    
    
    
}
