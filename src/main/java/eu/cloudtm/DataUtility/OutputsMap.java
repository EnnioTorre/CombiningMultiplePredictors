/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.DataUtility;

/**
 *
 * @author ennio
 */
public enum OutputsMap {
    
    throughput ("throughput",4),
    abortRate ("abortRate",3),
    responseTimeRO ("responseTime",2),
    responseTimeWO ("responseTime",1);
    
    private final int ArffIndex;
    private final String Target; 
    
    private OutputsMap(String target,int ArffIndex){
    
        this.ArffIndex=ArffIndex;
        this.Target=target;
    }
    
    public int getArffDispacement(){ return ArffIndex;}
    public String getTarget(){ return Target;}
    
    public String []getOtherClassTargets(){
    
        int i=4-1;
        String []Output=new String [i];
        for (Enum n:values()){
        
            if(!n.equals(this))
            
                Output[--i]=n.toString();
        }
        
        return Output;
    }
    
}
