/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.cloudtm.OracleThreads;

import eu.cloudtm.autonomicManager.oracles.InputOracle;
import java.io.Serializable;
import java.util.Properties;

/**
 *
 * @author ennio
 */
public class ThreadInput implements Serializable {
    
    private Properties CubistProperties;
    private String CubistID;
    private String Target;
    private InputOracle Input;

    public InputOracle getInput() {
        return Input;
    }

    public void setInput(InputOracle Input) {
        this.Input = Input;
    }

    public String getCubistID() {
        return CubistID;
    }

    public void setCubistID(String CubistID) {
        this.CubistID = CubistID;
    }

    public Properties getCubistProperties() {
        return CubistProperties;
    }

    public void setCubistProperties(Properties CubistProperties) {
        this.CubistProperties = CubistProperties;
    }

    public String getTarget() {
        return Target;
    }

    public void setTarget(String Target) {
        this.Target = Target;
    }
    
    
}
