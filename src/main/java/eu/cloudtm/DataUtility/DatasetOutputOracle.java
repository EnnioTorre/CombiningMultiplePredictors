/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package eu.cloudtm.DataUtility;

import eu.cloudtm.autonomicManager.oracles.OutputOracle;

/**
 *
 * @author Ennio email:ennio_torre@hotmail.it
 */
public class DatasetOutputOracle implements DataOutputOracleInterface {

    
    private double []throughput=new double [2];
    private double []abortRate=new double [2];
    private double []responseTime=new double [2];
    
    
    
    @Override
    public double throughput(int i) {
        
        return this.throughput[i];
         
    }

    @Override
    public double abortRate(int i) {
       return this.abortRate[i];
    }

    @Override
    public double responseTime(int i) {
        return this.responseTime[i];
    }

    
    
    @Override
    public double getConfidenceThroughput(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getConfidenceAbortRate(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getConfidenceResponseTime(int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void setthroughput(int i,double t) {
        
                 this.throughput[i]=t;        
    }

    public void setabortRate(int i,double ar) {
        this.abortRate[i]=ar; 
    }

    public void setresponseTime(int i,double rt) {
        this.responseTime[i]=rt;
    }
    
    @Override
    public String toString(){
    return "throughputRO="+throughput[0]+
           " throughputWO=" +throughput[1]+
           " abortRateRO=" +abortRate[0]+
           " abortRateWO=" +abortRate[1]+
           " responseTimeRO="+responseTime[0]+
           " responseTimeWO="+responseTime[1];
    
    }
    
    public void initOnOracleError(){
        
    throughput[0]=abortRate[0]=responseTime[0]=0;
    throughput[1]=abortRate[1]=responseTime[1]=0;
    
    }
    
    }


