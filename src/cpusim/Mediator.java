/**
 * File: Mediator
 * User: djskrien
 * Date: 5/29/13
 *
 * Created: 6/4/13
 */

/**
 * File: Mediator
 * Author: Pratap Luitel, Scott Franchi, Stephen Webel
 * Date: 10/27/13
 * 
 * Fields added:
 *      private SimpleBooleanProperty machineDirty
 *      private File machineFile
 *      private SimpleStringProperty machineDirtyString
 *      private String currentMachineDirectory
 * 
 * Methods added:
 *      public File getMachineFile()
 *      public void setMachineFile(File file)
 *      public boolean isMachineDirty()
 *      public void setMachineDirty(boolean b)
 *      public SimpleStringProperty getMachineDirtyProperty()
 *      public void setCurrentMachineDirectory(String s)
 *      public String getCurrentMachineDirectory()
 *      private void addMachineStateListeners()
 *      public void saveMachine()
 *      public void saveAsMachine()
 *      public void newMachine()
 *      public void openMachine(File fileToOpen)
 * 
 * Methods modified:
 *      public boolean Assemble(String programFileName)
 *      public void AssembleLoad(String programFileName)
 *      public void AssembleLoadRun(String programFileName)
 *      public void ClearAssembleLoadRun(String programFileName)
 *      public void Run()
 *      public void Stop()
 *      public void ResetEverything()
 *      private boolean load(boolean clearing)
 * 
 * Methods removed:
 *      private boolean assemble()
 *      private void run()
 *      private void stop()
 */
package cpusim;
import cpusim.Machine.StateWrapper;
import cpusim.assembler.AssembledInstructionCall;
import cpusim.assembler.Assembler;
import cpusim.assembler.AssemblyException;
import cpusim.gui.desktop.DesktopController;
import cpusim.gui.desktop.editorpane.EditorPaneController;
import cpusim.microinstruction.IO;
import cpusim.mif.MIFScanner;
import cpusim.module.RAM;
import cpusim.module.RAMLocation;
import cpusim.module.Register;
import cpusim.module.RegisterArray;
import cpusim.util.*;
import cpusim.xml.MachineReader;
import cpusim.xml.MachineWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Dialogs;
import javafx.stage.Stage;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.stage.FileChooser;

import org.xml.sax.SAXParseException;

/**
 * This class is the repository of global data, such as the current machine.
 */
public class Mediator {
    static final String NEWLINE = System.getProperty("line.separator");
    static final String SPACES = "              ";

    private SimpleObjectProperty<Machine> machine;
    private BackupManager backupManager;
    private Stage stage;
    private Assembler assembler;
    private DesktopController desktopController;
    private SimpleBooleanProperty machineDirty;
    private File machineFile;
    private SimpleStringProperty machineDirtyString;
    private String currentMachineDirectory;
    
    public Mediator(Stage s) {
        this.stage = s;
        this.backupManager = new BackupManager();
        this.machine = new SimpleObjectProperty<>();
        this.machineDirty = new SimpleBooleanProperty(false);
	this.machineFile = null;
        String d = machineDirty.get() ? "*" : "";
        this.machineDirtyString = new SimpleStringProperty(d);
		machineDirty.addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> arg0,
					Boolean oldVal, Boolean newVal) {
				String d = newVal ? "*" : "";
				machineDirtyString.set(d);
			}
		});
        setMachine(new Machine("New"));
    }
    
    /////////////////// Standard setters and getters ///////////////////

    
    /**
     * goes through all RAMs, registers, and arrays,
     * and adds the given listener as a
     * PropertyChangeListener to each of them.
     */
    public void addPropertyChangeListenerToAllModules(ChangeListener listener) {
        ObservableList registerList = machine.get().getModule("registers");
        for (Object o : registerList) {
            Register register = (Register) o;
            register.valueProperty().removeListener(listener);
            //the preceding statement does nothing if the listener
            //is not currently a listener of the register
            register.valueProperty().addListener(listener);
        }
        ObservableList arrays = machine.get().getModule("registerArrays");
        for (Object array : arrays) {
            ObservableList<Register> registers =
                    ((RegisterArray) array).registers();
            for (Register register : registers) {
                register.valueProperty().removeListener(listener);
                register.valueProperty().addListener(listener);
            }
        }
        ObservableList rams = machine.get().getModule("rams");
        for (Object o : rams) {
            RAM ram = (RAM) o;
            ram.dataProperty().removeListener(listener);
            ram.dataProperty().addListener(listener);
        }
    }

    public void setDesktopController(DesktopController d) {
        this.desktopController = d;
    }

    public DesktopController getDesktopController() {
        return this.desktopController;
    }

    public ObservableList getModule(String moduleType) {
        Machine machineObj = machine.get();
        return machineObj.getModule(moduleType);
    }

    /**
     * Sets the assembler's machine field
     * and sets up a new assembler.
     *
     * @param m - the new Machine for this mediator.
     */
    public void setMachine(Machine m) {
        machine.set(m);

       // Bind title to machine name
        if (stage.titleProperty().isBound()) {
        	stage.titleProperty().unbind();
        }
        if (desktopController != null) {
        	stage.titleProperty().bind(machineDirtyString.
            		concat(machine.get().getNameProperty()));
        }
        else {
        	stage.titleProperty().bind(machine.get().getNameProperty());
        }
        
        // In case name ends with .cpu
        String newName = machine.get().getName(); 
        if (newName.toLowerCase().endsWith(".cpu")) {
        	machine.get().getNameProperty().set(newName.substring(0,newName.length()-4));
        }

        machine.get().stateProperty().removeListener(backupManager);
        machine.get().stateProperty().addListener(backupManager);
        addPropertyChangeListenerToAllModules(backupManager);
        this.assembler = new Assembler(machine.get());
    }

    /**
     * get the backup Manager object.
     *
     * @return backupManager object.
     */
    public BackupManager getBackupManager() {
        return backupManager;
    }

    /**
     * gets the Mediator's current Machine object.
     *
     * @return the Mediator's current Machine object.
     */
    public Machine getMachine() {
        return machine.get();
    }

    /**
     * gets the Mediator's current Machine Property.
     *
     * @return the Mediator's current Machine Property.
     */
    public SimpleObjectProperty<Machine> getMachineProperty() {
        return machine;
    }

    /**
     * gets the Mediator's current assembler object.
     *
     * @return the Mediator's current assembler object.
     */
    public Assembler getAssembler() {
        return assembler;
    }
    
    public Stage getStage() {
        return stage;
    }
    
    //--------------------------------------------------------------------------
    //ADDED METHODS
    /**
     * gets the Mediator's current machineFile.
     *
     * @return the Mediator's current machineFile.
     */
    public File getMachineFile(){
        return this.machineFile;
    }
    
    /**
     * sets the Mediator's machineFile field.
     *
     */
    public void setMachineFile(File file){
        this.machineFile = file;
    }
    
    /**
     * gets the Dirtiness of the machine.
     *
     * @return the Mediator's current machineDirty value.
     */
    public boolean isMachineDirty(){
        return this.machineDirty.get();
    }
    
    /**
     * sets the Mediator's machineDirty field.
     *
     */
    public void setMachineDirty(boolean b){
        this.machineDirty.set(b);
    }
    
    /**
     * Gives the simple string property that is "*"
     * if machine is dirty, "" if not.
     *
     * @return the simple string property that is "*"
     * if machine is dirty, "" if not.
     */
    public SimpleStringProperty getMachineDirtyProperty() {
	return this.machineDirtyString;
    }
    
    /**
     * Set the currentMachineDirectory feild to a given string.
     *
     * @param s the string to set the field to.
     */
    public void setCurrentMachineDirectory(String s){
        currentMachineDirectory = s;
    }
    
    /**
     * Get the currentMachineDirectory.
     * 
     */
    public String getCurrentMachineDirectory(){
        return currentMachineDirectory;
    }
    
    /**
     * Removes old listeners and adds new ones. 
     * Used when opening or creating a new machine from the DesktopController
     * 
     */
    private void addMachineStateListeners() {
        machine.get().stateProperty().removeListener(this.desktopController.getHighlightManager());
        machine.get().stateProperty().addListener(this.desktopController.getHighlightManager());
        machine.get().stateProperty().removeListener(this.desktopController.getUpdateDisplayManager());
        machine.get().stateProperty().addListener(this.desktopController.getUpdateDisplayManager());
        machine.get().stateProperty().removeListener(this.desktopController.getConsoleManager());
        machine.get().stateProperty().addListener(this.desktopController.getConsoleManager());
    }
    
    /**
	 * saves the current machine to its file
	 * if it has no file it calls the saveAsMachine method
	 */
    public void saveMachine() {
        if (machineFile == null) {
                this.saveAsMachine();
                return;
        }

        MachineWriter writer = new MachineWriter();
        try {
                writer.writeMachine(machine.get(), machineFile.getName(),
                                getRegisterRAMPairs(),
                                new PrintWriter(new FileWriter(machineFile), true));
                setMachineDirty(false);
        } catch (IOException IOe) {
                Dialogs.showErrorDialog(stage, "Could not save file for unknown reason",
                                "Error", "CPU Sim");
        }
    }
    
    /**
    * saves the current machine to a user specified file
    */
    public void saveAsMachine() {
        FileChooser fileChooser = new FileChooser();
        this.desktopController.initFileChooser(fileChooser, "Save Machine", false);

        File fileToSave = fileChooser.showSaveDialog(stage);
        if (fileToSave == null) {
                return;
        }

        MachineWriter writer = new MachineWriter();
        try {
                writer.writeMachine(machine.get(), fileToSave.getName(),
                                getRegisterRAMPairs(),
                                new PrintWriter(new FileWriter(fileToSave), true));
                setCurrentMachineDirectory(fileToSave.getParent());
                setMachineFile(fileToSave);
                setMachineDirty(false);

                // to get name without .cpu
                String newName = fileToSave.getName();
                if (newName.toLowerCase().endsWith(".cpu")) {
                        machine.get().setName(newName.substring(0, newName.length() - 4));
                }
                else {
                        machine.get().setName(newName);
                }
        } catch (IOException e) {
                Dialogs.showErrorDialog(stage, "Could not save file for unknown reason",
                                "Error", "CPU Sim");
        }


    }
    
    public void newMachine() {
            Machine machine = new Machine("New");
            setMachineFile(null);
            setMachine(machine);
            ObservableList<RegisterRAMPair> newPairs = FXCollections.observableArrayList();
            this.desktopController.getHighlightManager().setRegisterRAMPairs(newPairs);

            addMachineStateListeners();

            this.desktopController.clearTables();
            this.desktopController.setUpTables();
    }

    /**
     * Attempts to load a machine from the given file.
     * If the file is null or not properly formatted, an error message
     * appears and a new empty machine is loaded instead.
     *
     * @param fileToOpen the File containing the machine to load.
     */
    public void openMachine(File fileToOpen) {
            String errorMessage;
            MachineReader reader = new MachineReader();
            Machine machine;

            try {
                    reader.parseDataFromFile(fileToOpen);
            } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                    if (errorMessage == null) {
                            errorMessage = "The error type is unknown.";
                    }
                    String messagePrefix = "Error when reading the machine file \"" +
                                    fileToOpen.getName() + "\"";
                    if (ex instanceof SAXParseException) {
                            messagePrefix += " at line " +
                                            ((SAXParseException) ex).getLineNumber();
                    }
                    errorMessage = messagePrefix + "." + NEWLINE + errorMessage;
                    Dialogs.showErrorDialog(stage, errorMessage, "Error reading machine file", "CPU Sim");

                    if (this.desktopController.getReopenMachineFiles().contains(fileToOpen.getAbsolutePath())) {
                            this.desktopController.getReopenMachineFiles().remove(fileToOpen.getAbsolutePath());
                    }
                    this.desktopController.updateReopenMachineMenu();
                    return;
            }

            this.desktopController.updateReopenMachineFiles(fileToOpen);
            setCurrentMachineDirectory(fileToOpen.getParent());
            setMachineFile(fileToOpen);
            setMachineDirty(false);

            machine = reader.getMachine();
            setMachine(machine);
            this.desktopController.getHighlightManager().setRegisterRAMPairs(FXCollections.observableList(reader.getRegisterRAMPairs()));

            addMachineStateListeners();

            this.desktopController.clearTables();
            this.desktopController.setUpTables();
    }
    
    //--------------------------------------------------------------------------
    
    
    /////////////////// Execute Menu ///////////////////

    //--------------------------------------------------------------------------
    /**
     * Assembles the current program. Called
     * from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    //CHANGE: returns boolean value
    public boolean Assemble(String programFileName) {
        try {
            assembler.assemble(programFileName,
                    (machine.get()).getStartingAddressForLoading());
            return true;
        } catch (AssemblyException ae) {
            //TODO:  Use the catch block from Desktop.assembleCurrentProgram
            //       in version 3.8.3.  In the meanwhile, just display
            //      the error message from the Exception.
            desktopController.highlightToken(ae.token);
            Dialogs.showErrorDialog(desktopController.getStage(),
                    ae.getMessage()+System.lineSeparator()+"Error is at line "+(ae.token.lineNumber+1)+
                    " and column "+ae.token.columnNumber+", in file "+
                    ae.token.filename);
            return false;
        }
    }
    
    /**
     * Assembles and Loads the current program. Called
     * from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void AssembleLoad(String programFileName) {
        // Try assembling
        //CHANGE: Assemble called rather than assemble
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // Try Loading
        load(false);
    }

    /**
     * Assembles, Loads, and Runs the current program.
     * Called from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void AssembleLoadRun(String programFileName) {
        // Try assembling
        //CHANGE: Assemble called rather than assemble
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // Try Loading
        success = load(false);
        if (!success) {
            return;
        }

        // Try running
        //CHANGE: Run called rather than run
        Run();
    }

    /**
     * Clears, Assembles, Loads, and Runs the current program.
     * Called from the Execute menu.
     *
     * @param programFileName the name of the current file.
     */
    public void ClearAssembleLoadRun(String programFileName) {

        // Try assembling
        //CHANGE: Assemble called rather than assemble
        boolean success = Assemble(programFileName);
        if (!success) {
            return;
        }

        // Try Loading
        success = load(true);
        if (!success) {
            return;
        }

        // Try clearing
        success = clear();
        if (!success) {
            return;
        }

        // Try running
        //CHANGE: Run called rather than run
        Run();
    }

    /**
     * Runs the current program.
     * Called from the Execute menu.
     */
    //CHANGE: run() code replaced body
    public void Run() {
        // Try running
        machine.get().getControlUnit().reset();
        machine.get().resetAllChannels();
        machine.get().execute(Machine.RunModes.RUN);
    }

    /**
     * Stops the currently running program.
     * Called from the Execute menu.
     */
    //CHANGE: stop() code replaced body
    public void Stop() {
        machine.get().setRunMode(Machine.RunModes.ABORT);
    }

    /**
     * Clears all registers and Arrays.
     * Called from the Execute menu.
     */
    public void ResetEverything() {
        clearHelper();
        clear();
    }

    
    /////////////////////// Private helper Methods ///////////////////////

    /**
     * The clearing of all RAMs and registers is always
     * done in two parts. The clearHelper is first
     * called, and then clear is called.
     */
    private void clearHelper() {
        // TODO
        // first cancel all editing
        // Update for JavaFX? Old version found within ActionListenerFor.java

        machine.get().clearAllRAMs();
    }

    /**
     * To avoid repetitive coding, the clear method
     * is implemented here. It clears the Back-Ups
     * in the Debug menu, clears the registers in the
     * machine, and updates the displays to show that
     * everything has been cleared.
     */
    private boolean clear() {
        machine.get().clearAllRegisters();
        machine.get().clearAllRegisterArrays();
        return true;
    }

    /**
     * To avoid repetitive coding, the load method
     * is implemented here. It loads the binary decoded
     * instructions into RAM.
     *
     * @param clearing        if the clearing should be done
     * @return boolean true if everything went smoothly,
     *         false if there was an error
     */
    //CHANGE: first param removed
    private boolean load(boolean clearing) {
    	
    	if (machine.get().getCodeStore() == null) {
            Dialogs.showErrorDialog(stage,
                    "The machine has no RAM's and so there is " +
                            "nowhere to load the assembled code.",
                    "Error Finding RAM", "CPU Sim");

            return false;
        }
        List<AssembledInstructionCall> instrs =
                assembler.getAssembledInstructions();

        try {
            if (clearing) {
                clearHelper();
            }
            machine.get().getCodeStore().loadAssembledInstructions(instrs,
                    machine.get().getStartingAddressForLoading());
            
    		// TAKE DEBUG-MODE INTO ACCOUNT HERE
            EditorPaneController epc = desktopController.getTabEditorContollers().get(
            		desktopController.getTextTabPane().getSelectionModel().getSelectedItem());
            epc.clearAllBreakPointsInRam(machine.get().getCodeStore());
            RAM r = machine.get().getCodeStore();
            epc.setBreakPointsInRam(r);
            return true;
        } catch (LoadException ex) {
            Dialogs.showErrorDialog(desktopController.getStage(),
                    "LoadException", "Error", "title", ex);
            return false;
        }
    }
    //--------------------------------------------------------------------------
    
    /////////////////////// Other Getters, Setters and Parsers ///////////////////////

    /**
     * takes the ram information and puts it into mif format
     *
     * @param ram  The RAM whose data is to be converted to MIF
     * @return the textual representation of the ram according to mif formatting
     */
    public String ramToMIF(RAM ram) {
        String ramInMIF = "";

        ramInMIF += "DEPTH = " + ram.getLength() + ";" + NEWLINE;
        ramInMIF += "WIDTH = " + ram.getCellSize() + ";" + NEWLINE;
        ramInMIF += "ADDRESS_RADIX = HEX;" + NEWLINE;
        ramInMIF += "DATA_RADIX = BIN;" + NEWLINE;
        ramInMIF += "CONTENT" + NEWLINE;
        ramInMIF += "BEGIN" + NEWLINE + NEWLINE;
        int i = 0;
        int dataChunkBeginAddress = -1;
        for (RAMLocation ramLoc : ram.data()) {
            if (i != ram.data().size() - 1) {
                if (ramLoc.getValue() == ram.data().get(i + 1).getValue()) {
                    if (dataChunkBeginAddress == -1) {
                        dataChunkBeginAddress = i;
                    }
                    i++;
                    continue;
                }
                else {
                    if (dataChunkBeginAddress != -1) {
                        ramInMIF += "[" + Convert.fromLongToHexadecimalString(
                                dataChunkBeginAddress, ram.getCellSize()) + ".." +
                                Convert.fromLongToHexadecimalString(i, ram.getNumAddrBits()) + "]:  " +
                                Convert.fromLongToTwosComplementString(ramLoc.getValue(),
                                        ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                        dataChunkBeginAddress = -1;
                    }
                    else {
                        ramInMIF += Convert.fromLongToHexadecimalString(i, ram.getNumAddrBits())
                                + "        :  " + Convert.fromLongToTwosComplementString(ramLoc.getValue(),
                                ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                    }
                }
            }
            else {
                if (dataChunkBeginAddress != -1) {
                    ramInMIF += "[" + Convert.fromLongToHexadecimalString(
                            dataChunkBeginAddress, ram.getCellSize()) + ".." +
                            Convert.fromLongToHexadecimalString(i, ram.getNumAddrBits()) + "]:  " +
                            Convert.fromLongToTwosComplementString(ramLoc.getValue(),
                                    ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                    dataChunkBeginAddress = -1;
                }
                else {
                    ramInMIF += Convert.fromLongToHexadecimalString(i, ram.getNumAddrBits())
                            + "        :  " + Convert.fromLongToTwosComplementString(ramLoc.getValue(),
                            ram.getCellSize()) + ";" + SPACES + "-- " + ramLoc.getComment() + NEWLINE;
                }
            }
            i++;
        }

        ramInMIF += "END;" + NEWLINE;

        return ramInMIF;
    }

    public String ramToIntelHex(RAM ram) {
        String ramInIntelHex = "";

        int bytesNeeded = (ram.getCellSize() + 7) / 8;


        for (RAMLocation ramLoc : ram.data()) {
            ramInIntelHex += ":";
            ramInIntelHex += Convert.fromLongToHexadecimalString(bytesNeeded, 8);
            ramInIntelHex += Convert.fromLongToHexadecimalString(ramLoc.getAddress(), 16);
            ramInIntelHex += "00";
            ramInIntelHex += Convert.fromLongToHexadecimalString(ramLoc.getValue(), bytesNeeded * 8);
            ramInIntelHex += getCheckSumString(bytesNeeded, (int) ramLoc.getAddress(), ramLoc.getValue());
            ramInIntelHex += NEWLINE;
        }

        ramInIntelHex += ":00000001FF";

        return ramInIntelHex;
    }

    /**
     * parses text from an MIF file, putting information in the proper places
     *
     * @param fileText text from the MIF file to parse
     * @param ram      the RAM to load the info from the file into
     * @param pathName the pathname of the file being parsed
     */
    public void parseMIFFile(String fileText, RAM ram, String pathName) {
        String[] lines = fileText.split(NEWLINE);

        MIFScanner scanner = new MIFScanner(lines);

        ObservableList<RAMLocation> ramToBe = FXCollections.observableArrayList();

        String addressRadix = "";
        String dataRadix = "";
        int depth = -1;
        int width = -1;
        int startAddr;
        int endAddr;
        int dataBeginIndex;
        ArrayList<Long> data = new ArrayList<>();
        String comment;

        //search for the preliminary information
        while (true) {
            ArrayList<String> tokens = scanner.getNextTokens(false);
            if (tokens == null) {
                throw (new MIFReaderException("There is no content indicator"));
            }


            if (tokens.size() == 4) {
                if (!tokens.get(1).equals("=")) {
                    throw (new MIFReaderException("Equals sign missing or in incorrect "
                            + "position on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(3).equals(";")) {
                    throw (new MIFReaderException("Semicolon missing or in incorrect "
                            + "position on line " + scanner.getLineNumber()));
                }
                if (tokens.get(0).toLowerCase().equals("depth")) {
                    try {
                        depth = Integer.parseInt(tokens.get(2));
                    } catch (NumberFormatException nfe) {
                        throw (new MIFReaderException("The value for depth assigned on line "
                                + scanner.getLineNumber() + " should be a valid integer"));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("width")) {
                    try {
                        width = Integer.parseInt(tokens.get(2));
                    } catch (NumberFormatException nfe) {
                        throw (new MIFReaderException("The value for width assigned on line "
                                + scanner.getLineNumber() + " should be a valid integer"));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("address_radix")) {
                    addressRadix = tokens.get(2).toLowerCase();
                    if (addressRadix.equals("hexadecimal")) {
                        addressRadix = "hex";
                    }
                    if (addressRadix.equals("decimal")) {
                        addressRadix = "dec";
                    }
                    if (addressRadix.equals("binary")) {
                        addressRadix = "bin";
                    }
                    if (!addressRadix.equals("hex") && !addressRadix.equals("dec") &&
                            !addressRadix.equals("bin")) {
                        throw (new MIFReaderException("unknown radix '" + addressRadix +
                                "' found on line " + scanner.getLineNumber()));
                    }
                }
                if (tokens.get(0).toLowerCase().equals("data_radix")) {
                    dataRadix = tokens.get(2).toLowerCase();
                    if (dataRadix.equals("hexadecimal")) {
                        dataRadix = "hex";
                    }
                    if (dataRadix.equals("decimal")) {
                        dataRadix = "dec";
                    }
                    if (dataRadix.equals("binary")) {
                        dataRadix = "bin";
                    }
                    if (!dataRadix.equals("hex") && !dataRadix.equals("dec") &&
                            !dataRadix.equals("bin")) {
                        throw (new MIFReaderException("unknown radix " + dataRadix +
                                " found on line " + scanner.getLineNumber()));
                    }
                }

            }
            else if (tokens.size() == 1) {
                if (tokens.get(0).toLowerCase().equals("content")) {
                    break;
                }
                else {
                    throw (new MIFReaderException("Unknown indicator " + tokens.get(0) + " on"
                            + "line " + scanner.getLineNumber()));
                }
            }
            else if (tokens.size() != 0) {
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }

        }

        if (depth > ram.getLength()) {
            Dialogs.showWarningDialog(stage, "There is more data being loaded into the ram"
                    + "than the ram has room for.  This means that not all of the information"
                    + "in the file will be loaded into ram");
        }

        if (width != ram.getCellSize()) {
            throw (new MIFReaderException("The specified width does not match the "
                    + "cell size of the ram and therefor the data from the file"
                    + " cannot be loaded"));
        }

        //look for the begin indicator
        while (true) {
            ArrayList<String> tokens = scanner.getNextTokens(false);
            if (tokens == null) {
                throw (new MIFReaderException("There is no begin indicator"));
            }

            if (tokens.size() == 1 && tokens.get(0).toLowerCase().equals("begin")) {
                break;
            }
        }

        //parse the data
        while (true) {
            endAddr = -1;
            data.clear();
            comment = "";
            ArrayList<String> tokens = scanner.getNextTokens(true);
            if (tokens == null) {
                throw (new MIFReaderException("There is no end indicator"));
            }

            if (tokens.size() == 0) {
                continue;
            }

            if (tokens.size() < 4) {
                if (tokens.size() == 2) {
                    if (tokens.get(0).toLowerCase().equals("end") && tokens.get(1).equals(";")) {
                        break;
                    }
                    else {
                        throw (new MIFReaderException("Unknown indicator " + tokens.get(0) + " on"
                                + "line " + scanner.getLineNumber()));
                    }
                }
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }
            if (tokens.get(1).equals(":")) {
                try {
                    if (addressRadix.equals("hex")) {
                        startAddr = Integer.parseInt(tokens.get(0), 16);
                    }
                    else if (addressRadix.equals("dec")) {
                        startAddr = Integer.parseInt(tokens.get(0));
                    }
                    else {
                        startAddr = Integer.parseInt(tokens.get(0), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(0) +
                            "on line " + scanner.getLineNumber()));
                }
                dataBeginIndex = 2;
            }
            else if (tokens.get(0).equals("[")) {
                if (!tokens.get(2).equals("..")) {
                    throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(4).equals("]")) {
                    throw (new MIFReaderException("Closed bracket is missing or misplaced "
                            + "on line " + scanner.getLineNumber()));
                }
                if (!tokens.get(5).equals(":")) {
                    throw (new MIFReaderException("Colon is missing or misplaced "
                            + "on line " + scanner.getLineNumber()));
                }
                try {
                    if (addressRadix.equals("hex")) {
                        startAddr = Integer.parseInt(tokens.get(1), 16);
                    }
                    else if (addressRadix.equals("dec")) {
                        startAddr = Integer.parseInt(tokens.get(1));
                    }
                    else {
                        startAddr = Integer.parseInt(tokens.get(1), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(1) +
                            "on line " + scanner.getLineNumber()));
                }
                try {
                    if (addressRadix.equals("hex")) {
                        endAddr = Integer.parseInt(tokens.get(3), 16);
                    }
                    else if (addressRadix.equals("dec")) {
                        endAddr = Integer.parseInt(tokens.get(3));
                    }
                    else {
                        endAddr = Integer.parseInt(tokens.get(3), 2);
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid address value" + tokens.get(3) +
                            "on line " + scanner.getLineNumber()));
                }
                dataBeginIndex = 6;
            }
            else {
                throw (new MIFReaderException("Parse error on line " + scanner.getLineNumber()));
            }
            while (true) {
                if (tokens.get(dataBeginIndex).equals(";")) {
                    if (dataBeginIndex != tokens.size() - 1) {
                        comment = tokens.get(dataBeginIndex + 1);
                    }
                    break;
                }
                try {
                    if (dataRadix.equals("hex")) {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex), 16));
                    }
                    else if (addressRadix.equals("dec")) {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex)));
                    }
                    else {
                        data.add(Long.parseLong(tokens.get(dataBeginIndex), 2));
                    }
                } catch (NumberFormatException e) {
                    throw (new MIFReaderException("Invalid data value" + tokens.get(dataBeginIndex) +
                            "on line " + scanner.getLineNumber()));
                }
                if (dataBeginIndex == tokens.size() - 1) {
                    break;
                }
                dataBeginIndex++;
            }

            if (ram.data().size() == ramToBe.size()) {
                break;
            }


            int k = 0;
            if (endAddr == -1) {
                for (long value : data) {
                    if (ram.data().size() == ramToBe.size()) {
                        break;
                    }
                    ramToBe.add(new RAMLocation(startAddr + k, value, ram, false, comment,
                            new SourceLine(scanner.getLineNumber(), pathName)));
                    k++;
                }
            }
            else {
                while (startAddr + k != endAddr + 1) {
                    if (ram.data().size() == ramToBe.size()) {
                        break;
                    }
                    ramToBe.add(new RAMLocation(startAddr + k, data.get(k % data.size()), ram,
                            false, comment, new SourceLine(scanner.getLineNumber(), pathName)));
                    k++;
                }
            }
        }


        int i = 0;
        for (RAMLocation ramLoc : ram.data()) {
            ramLoc.setValue(ramToBe.get(i).getValue());
            ramLoc.setComment(ramToBe.get(i).getComment());
            ramLoc.setSourceLine(ramToBe.get(i).getSourceLine());
            i++;
        }


    }

    public void setRegisterRamPairs(ObservableList<RegisterRAMPair> registerRAMPairs) {
        desktopController.getHighlightManager().setRegisterRAMPairs(registerRAMPairs);
    }

    public ObservableList<RegisterRAMPair> getRegisterRAMPairs() {
        return desktopController.getHighlightManager().getRegisterRAMPairs();
    }

    private String getCheckSumString(int byteCount, int address, long value) {
        int sum = byteCount + address % 256 + address / 256;
        byte[] bytes = Convert.fromLongToByteArray(value, byteCount);
        for (byte b : bytes)
            sum += b;
        sum &= 255;  // to get just the lowest 8 bits
        int checkSum = (256 - sum) % 256; // only want the lowest 8 bits
        return Convert.fromLongToHexadecimalString(checkSum, 8);
    }

    public void parseIntelHexFile(String fileText, RAM ram, String pathName) {
        String[] lines = fileText.split(NEWLINE);

        ObservableList<RAMLocation> ramToBe = FXCollections.observableArrayList();

        int lineNumber = 1;
        for (String line : lines) {
            if (line.length() % 2 != 1) {
                Dialogs.showErrorDialog(stage, "There is an even number of characters on"
                        + "line " + lineNumber + ", which is not allowed in intel hex format",
                        "Invalid Value", "CPU Sim");
            }
            if (line.charAt(0) != ':') {
                Dialogs.showErrorDialog(stage, "line " + lineNumber + " does not start with"
                        + "a colon as it should", "Invalid Value", "CPU Sim");
            }
            int bytesOfData = (int) Convert.fromHexadecimalStringToLong(line.substring(1, 3), 8);
            if (line.length() != 11 + 2 * bytesOfData) {
                Dialogs.showErrorDialog(stage, "Line " + lineNumber +
                        " specifies " + bytesOfData + " bytes of data but contains " +
                        (line.length() - 11) + " bytes of data.", "Invalid Value", "CPU Sim");
            }
            if (line.substring(7, 9).equals("01")) {
                int i = 0;
                for (RAMLocation ramLoc : ram.data()) {
                    ramLoc.setValue(ramToBe.get(i).getValue());
                    ramLoc.setComment(ramToBe.get(i).getComment());
                    ramLoc.setSourceLine(ramToBe.get(i).getSourceLine());
                    i++;
                }
                return;
            }

            int address = Integer.parseInt(line.substring(3, 7), 16);

            int recordType = Integer.parseInt(line.substring(7, 9), 16);
            if (!(recordType == 0 || recordType == 1)) {
                Dialogs.showErrorDialog(stage, "Line " + lineNumber +
                        " has a record type other than data record (00)" +
                        " or end of file record (01), which CPUSim means CPUSim cannot"
                        + "parse this file.");
            }
            // check the checksum for the line
            // first get all the chars after the colon into a byte array
            BigInteger bline = new BigInteger(line.substring(1), 16);
            byte[] byteArray = bline.toByteArray();
            // The toByteArray() method might add an extra byte of 0
            // at the beginning of the array, but it won't affect the
            // check sum.
            int sum = 0;
            for (byte b : byteArray)
                sum += b;
            if ((sum & 255) != 0) {
                Dialogs.showErrorDialog(stage, "Line " + lineNumber +
                        " has an incorrect checksum value.");
            }
            long data = Convert.fromHexadecimalStringToLong(line.substring(9, 9 + bytesOfData * 2),
                    bytesOfData * 8);

            ramToBe.add(new RAMLocation(address, data, ram, false, "", new SourceLine(lineNumber,
                    pathName)));

            lineNumber++;
        }
        // if we get here, there must not have been an end-of-line
        // record type, so throw an exception
        Dialogs.showErrorDialog(stage, "The file is missing an end-of-line" +
                " record.");
    }
}
