/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.weka.filters;

import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

/**
 *
 * @author ennio
 */
public class RemoveFacilities {
    
   public static Instances remove(Instances i,String []attribute) throws Exception{
   
       int[] index=new int[attribute.length];
       Remove rmv=new Remove();
       
       for (int m=0;m<attribute.length;m++){
       
           index[m]=i.attribute(attribute[m]).index();
       }
       
       rmv.setAttributeIndicesArray(index);
       rmv.setInputFormat(i);
       return Filter.useFilter(i, rmv);
         
   }
    
}
