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
public class DialogChannel implements StringChannel {
    private Stage stage;
    private String name; 
    private Dialogs.DialogResponse result;  
    private String input;
    
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
    }
    
    /**
     * Sets the Stage.
     * 
     * @param stage the Stage
     */
    public void setStage(Stage stage) {
        this.stage = stage;
    }

    //reads user input by prompting in a dialog box
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
        this.readUserInput();
        return input;
    }
}
