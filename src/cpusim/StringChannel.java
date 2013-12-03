/**
 * File: StringChannel
 * @author Peter Zhang, Brendan Tschaen, Stephen Jenkins
 * @created December 2013
 * This is the StringChannel interface that deals with input/output of strings
 */
package cpusim;
import cpusim.util.CPUSimConstants;

/**
 * Interface that any channels we want to use for input or output
 * must implement.
 */
public interface StringChannel extends CPUSimConstants {

    public String getInput();
    
    /**
     * set output to channel
     * @param string to be output 
     */
    public void output(String out);
}
