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
public class ConsoleChannel implements IOChannel {
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
    //INPUT buf
    private InputManager inputmanager;
    //Output buf
    private OutputManager outputmanager;
    
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
        this.inputmanager=new InputManager();
        this.outputmanager=new OutputManager();
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
                        else 
                            //set userinput:
                            //inputmanager.setBuffer(enteredText);
                            userInput=enteredText;
                        // reset
                        ioConsole.setEditable(false);
                        done = true;
                    }
                });
    }
    
    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits. If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(int numBits) {
        readingType = Type.Long;
        //TODO:fix
        if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT)){
            readUserInput();
        }
        return 0;
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
        readingType = Type.ASCII;
        if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT))
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
        readingType = Type.Unicode;
        readUserInput();
        if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT))
            return readUnicode();
        else return ' ';
    }

    //reads user input
    //prompts the user to input by setting ioconsole as editable
    private void readUserInput() {
        
        try {
            Platform.runLater(new Runnable() {
                public void run() {
                    ioConsole.appendText(getPrompt());
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
     * writes the given long value to the outputbuffer
     *
     * @param longValue the long value to be output
     */
    public void writeLong(final long longValue) {
        //just add the output since this will not be a '\n'
        /*
        outputmanager.addOutput(String.valueOf(longValue)+ " ");
        */
    }

    /**
     * writes the given long value to the outputbuffer as an ASCII value
     *
     * @param longValue the long value to be output
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
    
    private String getPrompt() {
        return "Enter input(s):";
    }
    /**
     * output the string to the console
     * @param String to be output
     */
    public void output(final String s){
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
    public String getInput(){
        //return the empty string
        return this.userInput;
    }
    
}
