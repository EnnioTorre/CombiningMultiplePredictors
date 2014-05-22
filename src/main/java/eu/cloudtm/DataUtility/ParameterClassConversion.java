/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.DataUtility;


import eu.cloudtm.autonomicManager.commons.EvaluatedParam;
import eu.cloudtm.autonomicManager.commons.ForecastParam;
import eu.cloudtm.autonomicManager.commons.Param;
import eu.cloudtm.autonomicManager.commons.ReplicationProtocol;
import org.apache.log4j.Logger;
import eu.cloudtm.autonomicManager.commons.IsolationLevel;


/**
 *
 * @author ennio
 */
public class ParameterClassConversion {
    static Logger logger = Logger.getLogger(ParameterClassConversion.class.getName());
             
    public static Object ConvertTo(Object param,Double value) throws Exception{
        
        //PARAM
      //Double Value=GetaDouble(value);
      
      
      if(param instanceof Param){
          return GetRequiredParamType((Param)param,value);
      
    } 
      if( ForecastParam.class.isInstance(param)){
          
          //System.out.println("conversion FP"+param+"class"+param.getClass()+value);
          return GetRequiredForecastParamType((ForecastParam)param,value);
          //System.out.println("conversion FP"+param+"ok");
          
    }
      if(EvaluatedParam.class.isInstance(param)){
         // System.out.println("conversion EP"+param+value);
          return GetRequiredEvaluatedParamType((EvaluatedParam)param,value);
         // System.out.println("conversion FP"+param+"ok");
          
      
    }
      else
          logger.warn(param+" not valid as Parameter Type");
          throw new IllegalArgumentException(param.getClass()+"not valid as Parameter Type");
      
    }
    
    
   
    public static Number ConvertTo(Object param,Object value) throws Exception{
        
        //PARAM
      //Double Value=GetaDouble(value);
      
      
      if(param instanceof Param){
          return GetRequiredParamType((Param)param,value);
      
    } 
      if( ForecastParam.class.isInstance(param)){
          
          //System.out.println("conversion FP"+param+"class"+param.getClass()+value);
          return GetRequiredForecastParamType((ForecastParam)param,value);
          //System.out.println("conversion FP"+param+"ok");
          
    }
      if(EvaluatedParam.class.isInstance(param)){
         // System.out.println("conversion EP"+param+value);
         return GetRequiredEvaluatedParamType((EvaluatedParam)param,value);
         // System.out.println("conversion FP"+param+"ok");
          
      
    }
      else
          logger.warn(param+" not valid as Parameter Type");
          throw new IllegalArgumentException(param.getClass()+"not valid as Parameter Type");
      
    }
    

private static Number GetRequiredParamType(Param param,Object Value) throws Exception{

       switch ((Param)param) {
         default:
            return ((Number)Value).doubleValue();
      }

   }



private static Object GetRequiredForecastParamType(ForecastParam forecastParam,Double Value) throws Exception {
      switch (forecastParam) {
         case ReplicationProtocol:
         {
             if (Value==0)
             return ReplicationProtocol.TWOPC;
             if (Value==1)
             return ReplicationProtocol.PB;
             if (Value==2)
             return ReplicationProtocol.TO;
             else{
                 logger.warn(forecastParam+" not valid as Parameter Type");
                 throw new IllegalArgumentException(forecastParam.getClass()+"not valid as Parameter Type");
             }
         }
         default:
             return Value;
      }
   }

private static Number GetRequiredForecastParamType(ForecastParam forecastParam,Object Value) throws Exception {
     
         switch(forecastParam){
                            case ReplicationProtocol:
                                switch((ReplicationProtocol)Value){
                                        case TO:
                                        return 2;
                                         
                                         case PB:
                                        return 1;
                                         
                                         case TWOPC :
                                         return 0;
                                             
                                         default:
                                         {
                                            logger.warn(forecastParam+" not valid as Parameter Type");
                                            throw new IllegalArgumentException(forecastParam.getClass()+"not valid as Parameter Type");
                                         }
                              
                        }
                            default:
                                return (Number)Value;
                                  
                              
                        }
      }


private static Object GetRequiredEvaluatedParamType(EvaluatedParam evaluatedParam,Double Value) throws Exception {
      switch (evaluatedParam) {
         case ISOLATION_LEVEL:
             {
             if (Value==0)
             return IsolationLevel.GMU;
             if (Value==1)
             return IsolationLevel.RC;
             if (Value==2)
             return IsolationLevel.RR;
             else{
                 logger.warn(evaluatedParam+" not valid as Parameter Type");
                 throw new IllegalArgumentException(evaluatedParam.getClass()+"not valid as Parameter Type");
             }
            }
         default:
             return Value;
      }
   }

private static Number GetRequiredEvaluatedParamType(EvaluatedParam evaluatedParam,Object Value) throws Exception {
      switch (evaluatedParam) {
          case ISOLATION_LEVEL:
              switch((IsolationLevel)Value){
                  case RR:
                      return 2;
                  case RC:
                      return 1;
                  case GMU:
                      return 0;
                  default:
                  {
                      logger.warn(evaluatedParam+" not valid as Parameter Type");
                      throw new IllegalArgumentException(evaluatedParam.getClass()+"not valid as Parameter Type");
                  }
                 }
          default:
              return (Number)Value;
                                  
                              
                        }
     
   }



}
    

