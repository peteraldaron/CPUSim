/**
 * The OutputBuffer class.
 * This class represents an output buffer that takes individual output from the 
 * user on the same line until the newline character ('\n') is seen. 
 * The class stores a stringbuilder that contains all outputs in
 * forms of long integer, ascii code or unicode.
 * Used in the subclasses of the IOChannel method.
 * 
 * @created November 10, 2013
 * @author Peter Zhang, Brendan Tschaen, Stephen Jenkins
 */


package cpusim;
import java.lang.StringBuilder;
public class OutputManager {
    private StringBuilder stb;
    public OutputManager(){
        this.stb=new StringBuilder("");
    }
    /**
     * takes each output and add it to the string
     * until a new line character is entered.
     * @param output 
     */
    public void addOutput(String output){
        stb.append(output);
    }
    /**
     * @return the string that represents the input
     */
    public String toString(){
        return stb.toString();
    }
    /**
     * the clear function that clears the input buffer
     */
    public void clearBuffer(){
        this.stb=new StringBuilder("");
    }

}
