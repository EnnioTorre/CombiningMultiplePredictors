package eu.cloudtm.boosting;


import eu.cloudtm.weka.Cubist.Cubist;
import eu.cloudtm.CubistUtils.CubistData;
import eu.cloudtm.CubistUtils.CubistModels;
import eu.cloudtm.CubistUtils.NamesParser;
import eu.cloudtm.CubistUtils.CubistModelsData;
import eu.cloudtm.DataUtility.DataConverter;
import eu.cloudtm.Knn.DataSets;
import eu.cloudtm.Knn.Knearestneighbourg;
import eu.cloudtm.OracleHandlers.CubistHandler;
import eu.cloudtm.OracleHandlers.TasHandler;
import eu.cloudtm.autonomicManager.oracles.InputOracle;
import eu.cloudtm.autonomicManager.oracles.OutputOracle;
import eu.cloudtm.probing.Probing;
import eu.cloudtm.probing.ProbingDataSets;
import eu.cloudtm.weka.Tas.Tas;
import eu.cloudtm.weka.ensemble.Ensemble;
import java.io.InputStream;
import java.io.StringBufferInputStream;
import org.apache.log4j.PropertyConfigurator;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Instances;
import weka.classifiers.meta.AdditiveRegression;
import weka.core.Instance;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args ) throws Exception
    {
        System.out.println( "Hello World!" );
        PropertyConfigurator.configure("conf/log4j.properties");
        DataSource source = new DataSource("DataSet.arff");
        Instances data = source.getDataSet();
        data.setClassIndex(data.numAttributes() - 1);
        MultilayerPerceptron mv=new MultilayerPerceptron();
        mv.setNormalizeAttributes(false);
        mv.buildClassifier(data);
        System.out.println(mv.classifyInstance(data.firstInstance()));
        /*System.out.println(data.firstInstance());
         // setting class attribute if the data format does not provide this information
         // For example, the XRFF format saves the class attribute information as well
       
        Dataset d=new Dataset("csvfile");
        Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes() - 4);
        AdditiveRegression adR=new AdditiveRegression(new Cubist());
        //AdditiveRegression adR=new AdditiveRegression();
        String []Optons={"-W","eu.cloudtm.weka.Cubist.Cubist","-I","1"};
        adR.setOptions(Optons);
        adR.buildClassifier(Dataset.DataBoosting);
        
        
        System.out.println(adR.getOptions()[2]);
        
        System.out.println(adR.getNumIterations());
        System.out.println(adR.measureNumIterations());
       
        System.out.println(Dataset.DataBoosting.firstInstance());
        System.out.println(adR.classifyInstance(Dataset.DataBoosting.firstInstance()));
        System.out.println(Dataset.DataBoosting.firstInstance().value(Dataset.DataBoosting.firstInstance().numAttributes()-4));
        System.out.println(adR.classifyInstance(Dataset.DataBoosting.instance(2)));
        
        System.out.println(Dataset.DataBoosting.instance(2).value(Dataset.DataBoosting.firstInstance().numAttributes()-4));
       
        System.out.println(adR.classifyInstance(Dataset.DataBoosting.instance(20)));
        
        System.out.println(Dataset.DataBoosting.instance(20).value(Dataset.DataBoosting.firstInstance().numAttributes()-4));
        
        // System.out.println(adR.classifyInstance(data.instance(3)));
    /*
        NamesParser n= new NamesParser("lib/MorphR/abortFS.names");
        String attr;
        while((attr=n.getName())!=null)
            System.out.println(attr);
       CubistData cd= new CubistData("MorphR");
       cd.CreateCubistData(data);
       CubistModels cm= new CubistModels();
       cm.CreateCubistModels("MorphR");
       Dataset d=new Dataset("csvfile");/*
       Ensemble e0= new Ensemble();
       e0.buildClassifier(data);
       System.out.println(data.firstInstance().classValue());
       System.out.println(e0.classifyInstance(data.firstInstance()));
       Ensemble e= new Ensemble();
       e.buildClassifier(data);
       System.out.println(data.firstInstance().classValue());
       System.out.println(e.classifyInstance(data.firstInstance()));
       Ensemble e1= new Ensemble();
       e1.buildClassifier(data);
       System.out.println(data.firstInstance().classValue());
       System.out.println(e1.classifyInstance(data.firstInstance()));
      
       Dataset d=new Dataset("csvfile");
       Boosting b=new Boosting();
       Instance i=Dataset.DataBoosting.instance(5);
       InputOracle io=DataConverter.FromInstancesToInputOracle(i);
       OutputOracle o=b.forecast(io);
       System.out.println(o.throughput(0));
       System.out.println(o.throughput(1));
       System.out.println(o.throughput(0)+o.throughput(1)); 
       System.out.println(i.classValue());
       /*Dataset.DataBoosting.setClassIndex(Dataset.DataBoosting.numAttributes()-4);
       Ensemble e0= new Ensemble();
       AdditiveRegression adR=new AdditiveRegression(e0);
       adR.buildClassifier(Dataset.DataBoosting);
       System.out.println(adR.classifyInstance(i));
       e0.buildClassifier(Dataset.DataBoosting);
        System.out.println(i.classValue());
       System.out.println(e0.classifyInstance(i));
        
        DataSets dataknn=new DataSets("csvfile");
        Knearestneighbourg k=new Knearestneighbourg();
        System.out.println("KNN prediction "+(k.forecast(io).throughput(1)+k.forecast(io).throughput(0)));
        System.out.println("Target Value"+i.value(i.numAttributes()-4));
        System.out.println("Boosting prediction"+(b.forecast(io).throughput(0)+b.forecast(io).throughput(1)));
       
         i=Dataset.DataBoosting.instance(6);
        io=DataConverter.FromInstancesToInputOracle(i);
        System.out.println("KNN prediction "+(k.forecast(io).throughput(1)+k.forecast(io).throughput(0)));
        System.out.println("Target Value"+i.value(i.numAttributes()-4));
        System.out.println("Boosting prediction"+(b.forecast(io).throughput(0)+b.forecast(io).throughput(1)));
       */
      /* CubistHandler hl= new CubistHandler();
       //hl.BuildOracle();
       hl.forecast(io);
       TasHandler th=new TasHandler();
       th.forecast(io);*/
        
       /* ProbingDataSets dataknn=new ProbingDataSets("csvfile");
   
        Probing P= new Probing();
        
        Instance i=ProbingDataSets.ARFFDataSet.instance(2);
        
         InputOracle io=DataConverter.FromInstancesToInputOracle(i);
        System.out.println(P.forecast(io).throughput(0));
        
        Instance i1=ProbingDataSets.ARFFDataSet.instance(1);
        
         InputOracle io1=DataConverter.FromInstancesToInputOracle(i1);
        System.out.println(P.forecast(io1).throughput(0));
        */
    }
    
}
