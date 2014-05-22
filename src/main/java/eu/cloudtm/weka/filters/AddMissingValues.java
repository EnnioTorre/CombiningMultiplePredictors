/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.filters;

import eu.cloudtm.DataUtility.OutputsMap;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;

/**
 *
 * @author ennio
 */
public class AddMissingValues {
    
    public static Instances addValues(Instances b) throws Exception{
       
       Instances NewData=new Instances(populate(b),-1);
       
       NewData.addAll(b);
       ReplaceMissingValues rmv=new ReplaceMissingValues();
       rmv.setInputFormat(NewData);
       NewData=Filter.useFilter(NewData, rmv);
       
       return NewData;
       
    }
    
    private static Instances populate(Instances a){
    
        Instances data=new Instances(a);
        
        for(String attr:OutputsMap.valueOf(a.classAttribute().name()).getOtherClassTargets()){
    
            
            data.insertAttributeAt(new Attribute(attr),a.numAttributes());
        }
        
        return data;
    }
}
