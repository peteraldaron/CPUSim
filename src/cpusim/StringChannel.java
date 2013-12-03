/**
 * File: StringChannel
 * @author Peter Zhang, Brendan Tschaen, Stephen Jenkins
 * @created December 2013
 * This is the StringChannel interface that deals with input/output of strings
 */
package cpusim;
import cpusim.util.CPUSimConstants;

/**
 * Interface that any channels we want to use for input or writeString
 must implement.
 */
public interface StringChannel extends CPUSimConstants {

    /**
     * get user input string
     * @return 
     */
    public String readString();
    
    /**
     * set writeString to channel
     * @param string to be writeString 
     */
    public void writeString(String out);
}
