/**
 * File: ConcreteChannel
 * Last Updated: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: clearIOChannelBuffer
 * Fields added: inputmanager, outputmanager
 */
package cpusim;

import cpusim.util.Convert;
import cpusim.util.FXUtilities;

/**
 * This class represents an IOChannel that uses the State pattern to
 * delegate the actual behavior to another IOChannel. It just provides
 * an extra level of indirection to allow for separation of the GUI from
 * the model. This class is part of the model. The state channels
 * involve the GUI as needed.  It is up to the GUI to add these state
 * channels.
 */
//TODO: change name to BufferedChannel
public class ConcreteChannel implements IOChannel {
    private String name;
    private IOChannel state;
    private InputManager inputmanager;
    private OutputManager outputmanager;
    
    /**
     * The constructor to create a ConcreteChannel.
     * 
     * @param n - The name of the channel.
     * @param s - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public ConcreteChannel(String n, IOChannel s) {
        this.state = s;
        this.name = n;
        this.inputmanager=new InputManager();
        this.outputmanager=new OutputManager();
    }
    
    /**
     * Creates a concrete channel with null name.
     * 
     * @param ioc - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public ConcreteChannel(IOChannel ioc) {
        this(null, ioc);
    }
    
    /**
     * Creates a concrete channel with null IOChannel.
     * 
     * @param n - The name of the channel.
     */
    public ConcreteChannel(String n) {
        this(n, null);
    }
    
    /**
     * Sets the IOChannel.
     * 
     * @param c - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public void setState(IOChannel c) { 
        this.state = c; 
    }
    
    /**
     * Uses the state to read a long from the channel.
     * 
     * @param numBits - the number of bits the long should be 
     * able to fit into.
     */
    public long readLong(int numBits) {
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            state.readLong(numBits);
            inputmanager.setBuffer(state.getInput());
        }
        //if inputmanager is not empty, 
        if(!this.inputmanager.isEmpty()){
            //if not empty, get next input:
            String output=this.inputmanager.nextInput("Long");
            if(!output.equals("")){
                //if input is valid:
                long outputResult=Convert.fromAnyBaseStringToLong(output);
                if(!Convert.fitsInBits(outputResult, numBits)){
                    state.output("\n"
                                 +"number of bits invalid, "
                                 +"enter again."
                                 +inputmanager.toString()+"\n");
            }
                else return outputResult;
            }
            else{
                state.output('\n'+"Illegal integer detected, "
                            +"input discarded:"
                            +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.setBuffer("");
            }
        }
        //call yourself again
        return this.readLong(numBits);
    }
    
    /**
     * Uses the state to read an ASCII character from the channel.
     */
    public char readAscii() {
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            state.readAscii();
            inputmanager.setBuffer(state.getInput());
        }
        //if inputmanager is not empty, 
        if(!this.inputmanager.isEmpty()){
                //if not empty, get next input:
            String output=this.inputmanager.nextInput("ASCII");
            if(!output.equals("")){
                //if input is valid:
                return output.charAt(0);
            }
            else{
                state.output('\n'+"Illegal Ascii detected, "
                                     +"input discarded:"
                                    +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.setBuffer("");
            }
        }
        return this.readAscii();
    }
    
    /**
     * Uses the state to read a Unicode character from the channel.
     */
    public char readUnicode() {
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            state.readUnicode();
            inputmanager.setBuffer(state.getInput());
        }
        if(!this.inputmanager.isEmpty()){
            //if not empty, get next input:
            String output=this.inputmanager.nextInput("Unicode");
            if(!output.equals("")){
                //if input is valid:
                return output.charAt(0);
            }
            else{
                state.output('\n'+"Illegal Unicode detected, "
                                     +"input discarded:"
                                    +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.setBuffer("");
            }
        }
        return this.readUnicode();
    }
    
    /**
     * Uses the state to output a Long value to the user.
     * 
     * @param value - the value to output to the user.
     */
    public void writeLong(long value) {
        //just add the output since this will not be a '\n'
        outputmanager.addOutput(String.valueOf(value)+ " ");
        //we can skip the state completely:
        //state.writeLong(value);
    }
    
    /**
     * Uses the state to output an ASCII value to the user.
     * 
     * @param longValue - the long value of the character to
     * output to the user.
     */
    public void writeAscii(long longValue) {
        if (longValue > 255 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        if(longValue=='\n'){
            state.output("Output: "+outputmanager.toString()+'\n');
            outputmanager.clearBuffer();
        }
        //otherwise keep on appending
        else{
            this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
        //skipping state:
        //state.writeAscii(longValue);
    }
    
    /**
     * Uses the state to output a Unicode value to the user.
     * 
     * @param longValue - the long value of the Unicode character
     * to output to the user.
     */
    public void writeUnicode(long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        if(longValue=='\n'){ 
            state.output("Output value: " + outputmanager.toString());
        }
        //otherwise keep on appending
        else{
            this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
        state.writeUnicode(longValue);
    }
    
    /**
     * Resets the state.
     */
    public void reset() {
        state.reset();
    }
    
    /**
     * Gives the current state (IOChannel).
     * 
     * @return - the current state (IOChannel).
     */
    public IOChannel getChannel() {
        return state;
    }
    
    /**
     * Gives a string representation of the Concrete channel.
     */
    public String toString() { 
        if (state == null) {
            return name; 
        }
        else {
            return state.toString();
        }
    }
    
    /**
     * call state's cleariochannelbuffer method
     */
    public void clearIOChannelBuffer(){
        //reset the buffer:
        if(!inputmanager.toString().isEmpty())
            state.output("Flushing Input: "+inputmanager.toString()+"\n");
        if(!outputmanager.toString().isEmpty())
            state.output("Remaining Output: "+outputmanager.toString()+'\n');
        this.inputmanager.clearBuffer();
        this.outputmanager.clearBuffer();
        this.state.clearIOChannelBuffer();
    }
    /**
     * This class does not use this method. 
     * added here for inheritance compatibility
     */
    public void output(String s){}
    
    /**
     * Does not use this method. 
     * added here for inheritance compatibility
     */
    public String getInput(){
        //return the empty string
        return "";
    }
}
