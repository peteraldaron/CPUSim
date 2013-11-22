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
 */
package cpusim;

import java.lang.StringBuilder;

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
		this.buffer.append(input);
	}

	/**
	 * 
	 * @param input The input string from the user
	 * @return returns the next input in the sequence
	 */
	
	public String nextInput(String type){
		//parse the string according to type given
		//if string is empty:
		if(this.buffer.toString()==null||this.buffer.toString().equals("")){
			return "";
		}
		if(type.equals("Long")){
			// Eat up all the spaces before the first int char
			while( buffer.charAt(0) == ' ') {
				buffer.deleteCharAt(0);
			}
			//checks to see if first long is valid
			StringBuilder tempLong=new StringBuilder("");
			boolean isNegative=false;
			//negative check:
			if(!buffer.toString().isEmpty() && buffer.charAt(0)=='-' && buffer.charAt(1)<='9' && buffer.charAt(1)>='0'){
				isNegative=true;
				buffer.deleteCharAt(0);
			}
			while((!buffer.toString().isEmpty()) && (buffer.charAt(0)<='9' && buffer.charAt(0)>='0')){
				tempLong.append(buffer.charAt(0));
				buffer.deleteCharAt(0);
			}
			//make sure the input is not empty and there's a space after the string 
			//that represents long.
			if(buffer.length()==0 || buffer.charAt(0)==' '){
				if(buffer.length()!=0 && buffer.charAt(0)==' '){
					buffer.deleteCharAt(0);
				}
				if(isNegative){
					tempLong.insert(0, '-');
				}
				return tempLong.toString();
			}
			else return "";
			
		}
		
		else if(type.equals("ASCII")){
			//first make sure that the buffer ain't empty:
			if(this.buffer.toString().isEmpty())
				return "";
			else{
				//Checks to see if first char in buffer is a valid ascii char
				char asciiChar = buffer.charAt(0);
				if(0 >= asciiChar || asciiChar >= 255){
					return "";
				}
				buffer.deleteCharAt(0);
				return String.valueOf(asciiChar);
			}
		}
		
		else if(type.equals("Unicode")){
			//first make sure that the buffer ain't empty:
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
