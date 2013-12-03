/**
 * The InputBuffer class.
 * This class represents an input buffer that takes multiple input from the 
 * user on the same line until the newline character ('\n') is seen. i.e.,
 * until "enter" key is hit. 
 * The class stores a StringBuilder that represent the buffer
 * of inputs whether long, ascii code or unicode. Each input can be retrieved
 * sequentially by using the nextInput method;
 * Used in the subclasses of the IOChannel method.
 * 
 * @created November 13, 2013
 * @author Peter Zhang, Brendan Tschaen, Stephen Jenkins
 * @modified November 22 2013
 */
package cpusim;

import cpusim.util.Convert;

class InputManager {
    private StringBuilder buffer;
    /**
     * Constructor
     * init the buffer
     */
    public InputManager(){
        this.buffer=new StringBuilder("");
    }
    
    /**
     * set the buffer to the input string
     * @param input the string input
     */
    public void setBuffer(String input){
        //clear the buffer
        this.buffer=new StringBuilder("");
        //deal with null case.
        if(input==null)
            this.buffer.append("");
        this.buffer.append(input);
    }

	/**
	 * 
	 * @param input The input string from the user
	 * @return returns the next input in the sequence
	 */
	
	public String nextInput(String type){
		//parse the input buffer string according to type given
		//if string is empty or null:
		if(this.buffer.toString()==null || this.buffer.toString().isEmpty()){
			return "";
		}
		if(type.equals("Long")){
			return parseLong();	// Parse the next integer off the buffer.
		}
		
		else if(type.equals("ASCII")){
			//first make sure that the buffer isn't empty:
			if(this.buffer.toString().isEmpty())
				return "";
			else{
				//Checks to see if first char in buffer is a valid ascii char
				char asciiChar = buffer.charAt(0);
				if(0 >= asciiChar || asciiChar >= 255){
					return "";
				}
				//Delete the char from the buffer and return the value of it
				buffer.deleteCharAt(0);
				return String.valueOf(asciiChar);
			}
		}
		
		else if(type.equals("Unicode")){
			//first make sure that the buffer isn't empty:
			if(this.buffer.toString().isEmpty())
				return "";
			//Checks to see if first char in buffer is a valid unicode char
			char unicodeChar = buffer.charAt(0);
			if(0 >= unicodeChar || unicodeChar >= 65535){
				return "";
			}
			buffer.deleteCharAt(0);
			return String.valueOf(unicodeChar);
		}
		//not supposed to get here
		return "";
	}
	
	// Parse the next long off the buffer string.
	private String parseLong() {
		// Eat up all the spaces before the first int char
		while( buffer.charAt(0) == ' ' && buffer.length()>0) {
			buffer.deleteCharAt(0);
            if(buffer.length()==0){
                return "";
            }
		}
		// Create a tempBuffer that will be searched through for a valid long
		StringBuilder tempBuffer = new StringBuilder(buffer);
		
		// While the string cannot be resolved into a long, shrink the string
		// and try again
		boolean flag = true;
		while( flag ) {
			try{
				if( tempBuffer.length() != 0 ){
					Convert.fromAnyBaseStringToLong( tempBuffer.toString() );
                }
				
				flag = false;	// Convert didn't throw an exception, so break;
			}
			catch(NumberFormatException e) {
				// String is not valid integer, so retry with smaller integer
				tempBuffer.deleteCharAt(tempBuffer.length() - 1);
			}
		}
		
		if( tempBuffer.length() == 0 ){
			return "";
		}
        else{
			// Delete the chars that make up the long from the original buffer
			buffer.delete(0, tempBuffer.length());
			return tempBuffer.toString();
        }
	}
	
	/**
	 * The toString method
	 * @return a string representing all the tokens
	 */
	public String toString(){
		return buffer.toString();
	}
	
	/**
	 * returns whether the buffer is empty or not
	 * @return a boolean value 
	 */
	public boolean isEmpty(){
		//does function correctly
		return buffer.toString().isEmpty();
	}
	/**
	 * 
	 * @return the size of the buffer, in terms of number of chars/numbers
	 */
	public int getBufferSize(){
		//does function correctly
		return this.buffer.length();
	}
	/**
	 * clears the buffer
	 */
	public void clearBuffer(){
		this.buffer = new StringBuilder("");
	}

    
}
