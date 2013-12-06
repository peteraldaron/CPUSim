/**
 * File: ConsoleChannel
 * LastUpdate: December 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: readUserInput, writeString, readString 
 * Methods modified: all but reset and toString
 * Methods removed: reset, all read and write methods.
 * Fields removed: readingType,numBits, needToDeleteEnter, valueFromUser
 * Fileds added:userInput
 * 
 * This class implements StringChannel using the Console in CPUSim
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
                                if (ioConsole.getCaretPosition() == startCaret){
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
                        String enteredText = (ioConsole.getText(startCaret, 
                                ioConsole.getText().length()));
                        
                        ioConsole.appendText(LINE_SEPARATOR);
                        
                        //easter egg:
                        //since these are not official "help", they are treated 
                        //as entered text.
                        if(enteredText.toLowerCase().equals("ayuda")){
                                ioConsole.appendText( "Puedes entregar enteros en" 
                                        +"formato de dec, bin, o hex " 
                                        +"con prefijo como" 
                                        +"\"0b\" ,\"-0b\"." 
                                        +"\"0x\" , \"-0x\"." + LINE_SEPARATOR
                                        +"y tambien puedes entregar letras."
                                        +LINE_SEPARATOR);
                        }
                        //in honor of Linus T. who developed the lovely tool of git
                        if(enteredText.toLowerCase().equals("apu")){
                            ioConsole.appendText ("kokonaislukuja: Kirjoita"
                                         + "desimaali, binääri-tai heksadesimaali"
                                         + "kokonaislukuja."
                                         + "Binäärisille Käytä etuliite"
                                         + "\" 0B \" tai \"-0B \"."
                                         + "Heksadesimaaliluvussa, käytä" +
                                         "\" 0x \"tai \"-0x \"." + LINE_SEPARATOR);
                            ioConsole.appendText ("Sillä merkkiä: Kirjoita"
                                         + "-merkki ilman ympäröivän"
                                         + "lainausmerkkejä ja paina Enter / Return."
                                         + LINE_SEPARATOR
                                         + "Jos haluat pysäyttää suorituksen painamalla Pysäytys"
                                         + "-valikosta kohteen Suorita valikosta."
                                         + LINE_SEPARATOR);
                            
                        }
                        // Output directions if the user asks for "help"
                        if(enteredText.toLowerCase().equals("help")) {
                                ioConsole.appendText( "For Integers:Type in" 
                                        +"decimal, binary, or hexadecimal " 
                                        +"integers. " 
                                        +"For binary, use a prefix of" 
                                        +"\"0b\" or \"-0b\"." 
                                        +"For hexadecimal, use " +
                                        "\"0x\" or \"-0x\"." + LINE_SEPARATOR);
                                ioConsole.appendText("For characters: Type in" 
                                        +"a character with no surrounding " 
                                        +"quotes and then press Enter/Return." 
                                        + LINE_SEPARATOR 
                                        +"To halt execution, use the Stop" 
                                        +"menu item from the Execute menu."
                                        +LINE_SEPARATOR);
                        }
                        else if (enteredText.length()>0) 
                        {
                            userInput=enteredText;
                        }
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
        //call readuserinput to prompt user:
        if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT)
            && !(mediator.getMachine().getRunMode() == Machine.RunModes.STOP)){
            this.readUserInput();
            while (userInput==null || userInput.length()==0){
                if(!(mediator.getMachine().getRunMode() == Machine.RunModes.ABORT)
                    && !(mediator.getMachine().getRunMode() == Machine.RunModes.STOP)){
                    this.writeString("Enter Input:");
                    this.readUserInput();
                }
                else break;
            }
            String output=this.userInput;
            this.userInput=null;
            return output;
        }
        //if machine is aborted or stopped, return null...
        //which is then handled by bufferchannel as the final step.
        else return null;
    }
    
}
