/**
 * File: ConcreteChannel
 * Last Updated: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: clearIOChannelBuffer
 * Fields added: inputmanager, outputmanager
 */
package cpusim;

import cpusim.util.Convert;

/**
 * This class represents an IOChannel that uses the State pattern to
 * delegate the actual behavior to another IOChannel. It just provides
 * an extra level of indirection to allow for separation of the GUI from
 * the model. This class is part of the model. The state channels
 * involve the GUI as needed.  It is up to the GUI to add these state
 * channels.
 */
//TODO: change name to BufferedChannel
public class BufferedChannel implements IOChannel {
    private String name;
    private StringChannel state;
    private InputManager inputmanager;
    private OutputManager outputmanager;
    private int numBits;
    
    /**
     * The constructor to create a ConcreteChannel.
     * 
     * @param n - The name of the channel.
     * @param s - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public BufferedChannel(String n, StringChannel s) {
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
    public BufferedChannel(StringChannel ioc) {
        this(null, ioc);
    }
    
    /**
     * Creates a concrete channel with null IOChannel.
     * 
     * @param n - The name of the channel.
     */
    public BufferedChannel(String n) {
        this(n, null);
    }
    
    /**
     * Sets the IOChannel.
     * 
     * @param c - The actual channel this concrete channel
     * uses to execute any instructions.
     */
    public void setState(StringChannel c) { 
        this.state = c; 
    }
    
    /**
     * read long values from the channel.
     * It goes through the inputbuffer.
     * 
     * @param numBits - the number of bits the long should be 
     * able to fit into.
     * @return the next long value in the input sequence 
     */
    public long readLong(int numBits) {
        this.numBits=numBits;
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            //send request:
            state.writeString("Enter input:");
            String input=state.readString();
            if(input==null){
                return 0;
            }
            else
                inputmanager.setBuffer(input);
        }
        //if inputmanager is not empty, 
        if(!this.inputmanager.isEmpty()){
            //if not empty, get next input:
            String output=this.inputmanager.nextInput("Long");
            if(!output.equals("")){
                //if input is valid:
                long outputResult=Convert.fromAnyBaseStringToLong(output);
                if(!Convert.fitsInBits(outputResult, numBits)){
                    state.writeString(
                                 "number of bits invalid, "
                                 +"enter again."
                                 +inputmanager.toString()+"\n");
                }
                else return outputResult;
            }
            else{
                state.writeString("Illegal integer detected, "
                            +"input discarded:"
                            +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.clearBuffer();
            }
        }
        //call yourself again
        return this.readLong(numBits);
    }
    
    /**
     * read ASCII characters from the channel.
     * @return the next character in the input sequencce 
     */
    public char readAscii() {
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            //send request:
            state.writeString("Enter Input:");
            String input=state.readString();
            if(input==null){
                return 0;
            }
            else
                inputmanager.setBuffer(input);
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
                state.writeString("Illegal Ascii detected, "
                                     +"input discarded:"
                                    +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.setBuffer("");
            }
        }
        return this.readAscii();
    }
    
    /**
     * read Unicode characters from the channel.
     * @return the next character in the input sequencce 
     */
    public char readUnicode() {
        //if inputmanager is empty, get the input from the states(channels).
        if(inputmanager.isEmpty()){
            //have the state getting the input
            //send request:
            state.writeString("Enter Input:");
            String input=state.readString();
            if(input==null){
                return 0;
            }
            else
                inputmanager.setBuffer(input);
        }
        if(!this.inputmanager.isEmpty()){
            //if not empty, get next input:
            String output=this.inputmanager.nextInput("Unicode");
            if(!output.equals("")){
                //if input is valid:
                return output.charAt(0);
            }
            else{
                state.writeString("Illegal Unicode detected, "
                                     +"input discarded:"
                                    +inputmanager.toString()+'\n');
                //reset inputmanager:
                this.inputmanager.setBuffer("");
            }
        }
        return this.readUnicode();
    }
    
    /**
     *  write a Long value to the output.
     * 
     * @param value - the value to writeString to the user.
     */
    public void writeLong(long value) {
        //just add the writeString since this will not be a '\n'
        if(!Convert.fitsInBits(value, numBits))
            throw new ExecutionException("Attempt to output integer:"+value+
                    ", which is greater than "+numBits+" bits. Integer"
                    + "overflow.");
        outputmanager.addOutput(String.valueOf(value)+ " ");
    }
    
    /**
     * write an ASCII value to the output.
     * 
     * @param longValue - the long value of the character to
 writeString to the user.
     */
    public void writeAscii(long longValue) {
        if (longValue > 255 || longValue < 0){
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        }
        if(longValue=='\n'){
            state.writeString("Output: "+outputmanager.toString()+'\n');
            outputmanager.clearBuffer();
        }
        //otherwise keep on appending
        else{
            this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
    }
    
    /**
     *  write a Unicode value to the output.
     * 
     * @param longValue - the long value of the Unicode character to writeString to the user.
     */
    public void writeUnicode(long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        if(longValue=='\n'){ 
            state.writeString("Output value: " + outputmanager.toString());
        }
        //otherwise keep on appending
        else{
            this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
    }
    
    /**
     * Doesn't currently do anything. Saved for legacy compatibility.
     */
    public void reset() {
    }
    
    /**
     * Gives the current state (IOChannel).
     * 
     * @return - the current state (IOChannel).
     */
    public StringChannel getChannel() {
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
     * cleariochannelbuffer method
     * clears the buffers in this channel
     */
    public void clearIOChannelBuffer(){
        //reset the buffer:
        if(!inputmanager.toString().isEmpty())
            state.writeString("Flushing Input: "+inputmanager.toString()+"\n");
        if(!outputmanager.toString().isEmpty())
            state.writeString("Remaining Output: "+outputmanager.toString()+'\n');
        this.inputmanager.clearBuffer();
        this.outputmanager.clearBuffer();
    }
}
