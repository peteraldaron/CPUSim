/**
 * File: DialogChannel
 * Last update: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods modified: all but setStage, reset and toString
 * Methods added: clearIOChannelBuffer, readUserInput
 * Fields added: inputmanager, outputmanager
 * 
 * For details of each method, see header of methods
 * 
 * This class implements IOChannel using Dialog Boxes.
 */
package cpusim;

import javafx.scene.control.Dialogs;
//import cpusim.ConsoleChannel.Type;
import cpusim.util.Convert;
import cpusim.util.FXUtilities;
import javafx.stage.Stage;

/**
 * This class implements IOChannel using Dialog Boxes.
 */
public class DialogChannel implements IOChannel {
    private Stage stage;
    private String name; 
    private Dialogs.DialogResponse result;  
    private String input;
    private InputManager inputmanager;
    private OutputManager outputmanager;
    
    /**
     * Creates a new dialog channel. There is only
	 * one DialogChannel that is used, however, look in
	 * CPUSimConstants file to find the DialogChannel.
     * 
     * @param name - The name given to the console channel.
     */
    public DialogChannel(String name) {
        this.name = name;
        this.stage = null;
        this.result = Dialogs.DialogResponse.NO;
        inputmanager=new InputManager();
        outputmanager=new OutputManager();
    }
    
    /**
     * Sets the Stage.
     * 
     * @param stage the Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(final int numBits) {
    	if(!this.inputmanager.isEmpty()){
    		String output=this.inputmanager.nextInput("Long");
    		if(!output.equals("")){
    			long outputResult=Convert.fromAnyBaseStringToLong(output);
				if(!Convert.fitsInBits(outputResult, numBits)){
					try {
	            		FXUtilities.runAndWait(new Runnable() {
	            			public void run() {
	            				Dialogs.showErrorDialog(stage,
	            						"Number of bits invalid, "
	    	    				             	+ "enter again:"+
	    	    				             		"\n"+inputmanager.toString()+"\n", 
	                        			"Error", 
	                        			"Dialog Channel");
	            			}
	            		});
	        		} catch (Exception e) {
	        			throw new ExecutionException("An Exception was thrown" +
	        					" when we attempted to read from the console.");
	        		}
    			}
				else return outputResult;
    		}
    		else{
    			try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
            						"Queue not empty and/or type mismatch, "
    	    				             	+ "flusing queue:"+
    	    				             		"\n"+inputmanager.toString()+"\n", 
                        			"Error", 
                        			"Dialog Channel");
            			}
            		});
        		} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to read from the console.");
        		}
    		}
    		
    	}
    	readUserInput();
		return readLong(numBits);
    }

	private void readUserInput() {
		boolean done = false;
    	while (!done) {
    		try {
	    			FXUtilities.runAndWait(new Runnable() {
	    				public void run() {
		    					input = Dialogs.showInputDialog(stage,
		    							"Enter argument(s):", 
		    							"Input Dialog", 
		    							"Dialog Channel");
	    				}
	    			});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read a value from the console.");
    		}
    		
    		if (input == null || input.equals("")) {
    			// The user chose "Cancel" from the input dialog.
    			throw new ExecutionException("Input cancelled.");
            }
            inputmanager.setBuffer(input);
            done = true;
        }
	}

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
    	if(!this.inputmanager.isEmpty()){
    		//if not empty, get next input:
    		String output=this.inputmanager.nextInput("ASCII");
    		if(!output.equals("")){
    			//if input is valid:
				return output.charAt(0);
    		}
    		else{
    			//if type mismatch, output dialog 
    			//that displays remaining items in buffer
    			//and flushes buffer
    			try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
            						"Queue not empty and type mismatch, "
    	    				             	+ "flusing queue:"+
    	    				             		"\n"+inputmanager.toString()+"\n", 
                        			"Error", 
                        			"Dialog Channel");
            			}
            		});
        		} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to read from the console.");
        		}
    		}
    		
    	}
    	readUserInput();
		return readAscii();
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
    	if(!this.inputmanager.isEmpty()){
    		//if not empty, get next input:
    		String output=this.inputmanager.nextInput("Unicode");
    		if(!output.equals("")){
    			//if input is valid:
				return output.charAt(0);
    		}
    		else{
    			//if type mismatch, output dialog 
    			//that displays remaining items in buffer
    			//and flushes buffer
    			try {
            		FXUtilities.runAndWait(new Runnable() {
            			public void run() {
            				Dialogs.showErrorDialog(stage,
            						"Queue not empty and type mismatch, "
    	    				             	+ "flusing queue:"+
    	    				             		"\n"+inputmanager.toString()+"\n", 
                        			"Error", 
                        			"Dialog Channel");
            			}
            		});
        		} catch (Exception e) {
        			throw new ExecutionException("An Exception was thrown" +
        					" when we attempted to read from the console.");
        		}
    		}
    		
    	}
    	readUserInput();
		return readUnicode();
    }

    /**
     * add the given long value to the outputmanager
     * to be output later
     *
     * @param value the long value to be output
     */
    public void writeLong(final long value) {
    	//just add the output since this will not be a '\n'
    	outputmanager.addOutput(String.valueOf(value));
    }

    /**
     * add the given ascii value to the outputmanager
     * to be output later
     *
     * @param longValue the long value representation of an ascii to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(final long longValue) {
        if (longValue > 255 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        if(longValue=='\n'){
	        try {
	        	FXUtilities.runAndWait(new Runnable() {
	                public void run() {
	                	result = Dialogs.showConfirmDialog(stage,
	                			"Output value: " + outputmanager.toString(),
	                			"ASCII output", 
	    						"Dialog Channel", 
	    						Dialogs.DialogOptions.OK_CANCEL);
	                }
	            });
	        } catch (InterruptedException e) {
	            // This usually happens if the user picks "Stop".  But the output dialog
	            // is modal and so we can't pick "Stop", and instead the user must
	            // click the "Abort" button in the dialog, so we need do nothing here.
	        } catch (java.util.concurrent.ExecutionException e) {
	            //Don't know how to handle this exception, so let's just
	            //throw an ExecutionException
	            throw new ExecutionException("An InvocationTargetException was thrown" +
	                    " when we attempted to write the ASCII value " +
	                    ((char) longValue) + " to the user in a dialog.");
	        }
	        if (result == Dialogs.DialogResponse.CANCEL) {
	            // The user chose "Abort" from the output dialog
	            throw new ExecutionException("Abort requested.");
	        }
        }
        //otherwise keep on appending
        else{
        	this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
        
    }

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(final long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        if(longValue=='\n'){
	        try {
	        	FXUtilities.runAndWait(new Runnable() {
	                public void run() {
	                	result = Dialogs.showConfirmDialog(stage,
	                			"Output value: " + outputmanager.toString(),
	                			"Unicode output", 
	    						"Dialog Channel", 
	    						Dialogs.DialogOptions.OK_CANCEL);
	                }
	            });
	        } catch (InterruptedException e) {
	            // This usually happens if the user picks "Stop".  But the output dialog
	            // is modal and so we can't pick "Stop", and instead the user must
	            // click the "Abort" button in the dialog, so we need do nothing here.
	        } catch (java.util.concurrent.ExecutionException e) {
	            // Don't know how to handle this exception, so let's just
	            // throw an ExecutionException
	            throw new ExecutionException("An InvocationTargetException was thrown" +
	                    " when we \nattempted to write the Unicode value " +
	                    ((char) longValue) + " to the user in a dialog.");
	        }
	        if (result == Dialogs.DialogResponse.CANCEL) {
	            // The user chose "Abort" from the output dialog
	            throw new ExecutionException("Abort requested.");
	        }
        }
        //otherwise keep on appending
        else{
        	this.outputmanager.addOutput(String.valueOf((char)longValue));
        }
    }

    /** 
     * Reset the input and output. 
     * 
     */
    public void reset() {
    }
    /**
     * clear the iochannel buffer
     * called in the machine's listener
     */
    public void clearIOChannelBuffer(){
    	//reset the buffer:
    	if(!inputmanager.toString().isEmpty()){
    		try {
        		FXUtilities.runAndWait(new Runnable() {
        			public void run() {
        				Dialogs.showErrorDialog(stage,
        						"Flushing Input: "+inputmanager.toString()+"\n", 
                    			"Flushing", 
                    			"Dialog Channel");
        			}
        		});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read from the console.");
    		}
		}
		if(!outputmanager.toString().isEmpty())
    		try {
        		FXUtilities.runAndWait(new Runnable() {
        			public void run() {
        				Dialogs.showErrorDialog(stage,
        						"Remaining Output: "+outputmanager.toString()+'\n',
                    			"Flushing", 
                    			"Dialog Channel");
        			}
        		});
    		} catch (Exception e) {
    			throw new ExecutionException("An Exception was thrown" +
    					" when we attempted to read from the console.");
    		}
    	this.inputmanager.clearBuffer();
    	this.outputmanager.clearBuffer();
    }

    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return name;
    }

}
