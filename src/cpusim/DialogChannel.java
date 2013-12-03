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
        readUserInput();
        //not relevant anymore. io taken care by concretechannel
        return 0; 
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
        readUserInput();
        return ' '; 
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
        readUserInput();
        return ' ';
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
            done = true;
        }
    }
    
    /**
     * add the given long value to the outputmanager
     * to be output later
     *
     * @param value the long value to be output
     */
    public void writeLong(final long value) {
        //just add the output since this will not be a '\n'
        outputmanager.addOutput(String.valueOf(value)+ " ");
    }

    /**
     * add the given ascii value to the outputmanager
     * to be output later
     *
     * @param longValue the long value representation of an ascii to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(final long longValue) {
    }

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(final long longValue) {
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
    }

    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return name;
    }
    
    /**
     * output the string to a dialog
     * @param string to be output
     */
    public void output(final String s){
        try {
                FXUtilities.runAndWait(new Runnable() {
                    public void run() {
                        Dialogs.showInformationDialog(stage,
                            s,
                            "Notice", 
                            "Dialog Channel");
                    }
                });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read from the console.");
        }
    }
    
    /**
     * get the input in the dialog
     */
    public String getInput(){
        return input;
    }
}
