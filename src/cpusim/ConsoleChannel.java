/**
 * File: ConsoleChannel
 * LastUpdate: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: clearIOChannelBuffer, readUserInput
 * Methods modified: all but reset and toString
 * Fields removed: numBits, needToDeleteEnter, valueFromUser
 * Fields added: inputmanager, outputmanager
 * 
 * This class implements IOChannel using the Console in CPUSim
 */
package cpusim;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import cpusim.util.FXUtilities;

/**
 * This class implements IOChannel using a console that appears as a
 * panel along the bottom edge of the main CPU Sim desktop.
 */
public class ConsoleChannel implements StringChannel {
    private String name; 
    private TextArea ioConsole;
    
    private static enum Type {Long, ASCII, Unicode}
    private Type readingType;
    
    private boolean inputStarted;
    private boolean done;
    private int startCaret;
    private Mediator mediator;
    
    private String LINE_SEPARATOR = System.getProperty("line.separator");
    
    private boolean inputCancelled;
    
    private String userInput;
    /**
     * Constructor for new Console Channel. There is only
     * one Console channel that is used, however, look in
     * CPUSimConstants file to find the ConsoleChannel.
     * 
     * @param name - The name given to the console channel.
     */
    public ConsoleChannel(String name) {
        this.name = name;
        this.ioConsole = null;
        readingType = Type.Long;
        inputCancelled = false;
    }
    
    /**
     * Sets the mediator and sets up ioConsole.
     * 
     * @param med - The Mediator.
     */
    public void setMediator(Mediator med) {
        this.mediator = med;
        this.ioConsole = med.getDesktopController().getIOConsole();
        ioConsole.setEditable(false);
        ioConsole.setOnKeyPressed(
                new EventHandler<KeyEvent>() {
                    @Override
                    public void handle(KeyEvent event) {
                        if (ioConsole.isEditable()) {
                            String content = ioConsole.getText();

                            if (!inputStarted) {
                                startCaret = content.length();
                            }

                            if (ioConsole.getCaretPosition() < startCaret) {
                                ioConsole.positionCaret(content.length());
                            }

                            if (event.getCode().equals(KeyCode.BACK_SPACE)) {
                                if (ioConsole.getCaretPosition() == startCaret) {
                                    ioConsole.insertText(startCaret, " ");
                                }
                            }
                            else if (event.getCode().equals(KeyCode.ENTER)) {
                                handleEnter();
                                return;
                            }

                            inputStarted = true;
                        }
                    }
                    
                    //handleEnter method
                    //deals with storing the input to buffer once enter is hit
                    private void handleEnter() {
                        inputStarted = false;
                        String enteredText = (ioConsole.getText(startCaret, ioConsole.getText().length()));
                        
                        ioConsole.appendText(LINE_SEPARATOR);
                        
                        // Output directions if the user asks for "help"
                        if(enteredText.toLowerCase().equals("help")) {
                            switch(readingType) {
                            case Long:
                                ioConsole.appendText( "Type in a decimal, binary, or hexadecimal " +
                                        "integer. " +
                                        "For binary, use a prefix of \"0b\" or \"-0b\"." +
                                        "For hexadecimal, use " +
                                        "\"0x\" or \"-0x\"." + LINE_SEPARATOR +
                                        "To halt execution, use the Stop menu item from the Execute menu."
                                        +LINE_SEPARATOR); 
                                break;
                            case ASCII:
                                ioConsole.appendText("Type in a character with no surrounding " +
                                        "quotes and then press Enter/Return." + LINE_SEPARATOR +
                                        "To halt execution, use the Stop menu item from the Execute menu."
                                        +LINE_SEPARATOR);
                                break;
                            case Unicode:
                                ioConsole.appendText("Type in a character with no surrounding " +
                                        "quotes and then press Enter." + LINE_SEPARATOR +
                                        "To halt execution, use the Stop menu item from the Execute menu."
                                        +LINE_SEPARATOR);
                                break;
                            default:
                                break;
                            }
                        }
                        else if (enteredText.length()!=0) 
                            //set userinput:
                            //inputmanager.setBuffer(enteredText);
                            userInput=enteredText;
                        // reset
                        ioConsole.setEditable(false);
                        done = true;
                    }
                });
    }
    
    //reads user input
    //prompts the user to input by setting ioconsole as editable
    private void readUserInput() {
        
        try {
            Platform.runLater(new Runnable() {
                public void run() {
                    ioConsole.setEditable(true);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to read a value from the console.");
        }
        
        while (!done && !inputCancelled) {
            try {
                inputCancelled = mediator.getMachine().getRunMode() 
                        ==Machine.RunModes.ABORT;
                Thread.sleep(10);
            }
            catch (InterruptedException e) {
                System.out.println("Error while sleeping thread");
            }
        }
        if (inputCancelled) {
            Platform.runLater(new Runnable() {
                public void run() {
                    ioConsole.appendText(LINE_SEPARATOR);
                    ioConsole.setEditable(false);
                }
            });
        }
        done = false;
        inputCancelled = false;
        inputStarted = false;
    }
    
    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return name;
    }
    /**
     * writeString the string to the console
     * @param String to be writeString
     */
    public void writeString(final String s){
        try {
            FXUtilities.runAndWait(new Runnable() {
                public void run() {
                    ioConsole.appendText(s);
                }
            });
        } catch (Exception e) {
            throw new ExecutionException("An Exception was thrown" +
                    " when we attempted to write a value to the console.");
        }
    }
    
    /**
     * get input from the console.
     * added here for inheritance compatibility
     */
    public String readString(){
        //call get user input to prompt user:
        if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT)
            || !(mediator.getMachine().getRunMode() == Machine.RunModes.STOP))
            this.readUserInput();
        else{
            return null;
        }
        //System.out.println("cc-rs,input="+userInput.charAt(0));
        return this.userInput;
    }
    
}
