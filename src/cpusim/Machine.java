
/** * File: Machine * Last update: Dec 2013 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen * Method modified: the Machine Constructor, initializeMicroMap, execute * (modified the executed method under commandline so it notifies the bufferedchannel * to clear io buffers when execution terminates.) * Variables Modified:MICRO_CLASSES: added the comment micro */package cpusim;import cpusim.assembler.EQU;import cpusim.assembler.PunctChar;import cpusim.microinstruction.*;import cpusim.module.*;import cpusim.util.Assert;import cpusim.util.CPUSimConstants;import javafx.beans.property.SimpleObjectProperty;import javafx.collections.FXCollections;import javafx.collections.ObservableList;import javafx.concurrent.Task;import java.io.File;import java.io.Serializable;import java.util.*;/** * This file contains the class for Machines created using CPU Sim * <p/> * Class Modifications: * Generics added to setRegisterArrays, getMicrosThatUse, setEnd, setRAMs, * haltBitsThatAreSet and getInstructionsThatUse methods. * <p/> * In the execute method we added "fire property change" to indicate the start * of a microinstruction. This property change is used to indicate when to put * a new microInstructionStack of HashMaps as an element in the * machineInstructionStack. */public class Machine extends Module implements Serializable, CPUSimConstants {    private static final long serialVersionUID = 1L;    // constants for different running modes    public static enum RunModes{        RUN,        STEP_BY_MICRO,        STEP_BY_INSTR,        RUN_AND_FIRE_CYCLES,        STOP,        ABORT,        COMMAND_LINE    }    /** the names of all the microinstruction classes */    public static final String[] MICRO_CLASSES =            {"arithmetic", "branch", "decode", "end",                    "increment", "io", "logical", "memoryAccess",                    "set", "setCondBit", "shift", "test",                    "transferRtoR", "transferRtoA", "transferAtoR","comment"};    /** the hardware modules making up the machine */    private ObservableList<Register> registers;    private ObservableList<RegisterArray> registerArrays;    private ObservableList<ConditionBit> conditionBits;    private ObservableList<RAM> rams;    /** the current state of the machine, defined by enum state     * and associated Object value     */    private SimpleObjectProperty<StateWrapper> wrappedState;    // Machine instructions    private List<MachineInstruction> instructions;    // EQUs    private ObservableList<EQU> EQUs;    // key = micro name, value = list of microinstructions    private HashMap<String, ObservableList<Microinstruction>> microMap;    // key = module name, value = list of modules    private HashMap<String, ObservableList<? extends Module>> moduleMap;    // Control unit for keeping track of index of micro within machine instruction    private ControlUnit controlUnit;    // The machine's fetch sequence    private MachineInstruction fetchSequence;    // Can be: RUN, STEP_BY_MICRO, STEP_BY_INSTR, STOP, ABORT, COMMAND_LINE    // It is volatile to ensure that all threads can read it at any time    private volatile RunModes runMode = RunModes.ABORT;    // Fields of the machine instructions    private List<Field> fields;    // Array of PunctChars.    private PunctChar[] punctChars;    // Address to start when loading RAM with a program    private int startingAddressForLoading;    // RAM where the code store resides    private SimpleObjectProperty<RAM> codeStore;        /**     * Creates a new machine.     *     * @param name - The name of the machine.     */    public Machine(String name) {        super(name);        registers = FXCollections.observableArrayList();        registerArrays = FXCollections.observableArrayList();        conditionBits = FXCollections.observableArrayList();        rams = FXCollections.observableArrayList();        wrappedState = new SimpleObjectProperty<>(this, "machine state", new StateWrapper(State.NEVER_RUN, ""));        moduleMap = new HashMap<>();        microMap = new HashMap<>();        instructions = new ArrayList<>();        EQUs = FXCollections.observableArrayList(new ArrayList<EQU>());        fields = new ArrayList<>();        punctChars = getDefaultPunctChars();        fetchSequence =                new MachineInstruction("Fetch sequence", 0, "", this);        controlUnit = new ControlUnit("ControlUnit", this);        startingAddressForLoading = 0;        codeStore = new SimpleObjectProperty<>(null);        initializeModuleMap();        initializeMicroMap();        //listener for IOChannel Buffers:        stateProperty().addListener(new javafx.beans.value.ChangeListener<StateWrapper>(){            @Override            public void changed(javafx.beans.value.ObservableValue<? extends StateWrapper> arg0,                    StateWrapper arg1, StateWrapper arg2) {                if(arg2.getState()==Machine.State.EXECUTION_HALTED                        || arg2.getState()==Machine.State.EXECUTION_ABORTED                         || arg2.getState()==Machine.State.EXCEPTION_THROWN                         || arg2.getState()==Machine.State.HALTED_STEP_BY_MICRO){                    ObservableList ios = getMicros("io");                    for (Object io : ios) {                        ((IO) io).getConnection().clearIOChannelBuffer();                    }                }                            }        });    }    /**     * The enum for the State of the Machine.     */    public static enum State {        NEVER_RUN, //initial state when machine is first loaded        START_OF_EXECUTE_THREAD, //called once at the start of execution        EXCEPTION_THROWN,        EXECUTION_ABORTED,        EXECUTION_HALTED,        START_OF_MACHINE_CYCLE,        START_OF_MICROINSTRUCTION,        BREAK,        HALTED_STEP_BY_MICRO    }    /**     * StateWrapper class wraps a State enum and an Object value so that listeners will register     * a change when the state is set, even if the enum is the same so that changed method is     * always called by listeners     */    public class StateWrapper{        /**         * state is the enum State of the Machine         */        State state;        /** value is any additional information needed by listeners who are         *  listening to state changes. */        Object value;        /**         * constructor initializes field state and value to parameter newState and newValue         * @param newState new enum State of Machine         * @param newValue any additional information needed by listeners who are listening to state changes.         */        public StateWrapper(State newState, Object newValue){            this.state = newState;            this.value = newValue;        }        /**         * returns the Machine of this State         * @return the Machine of this State         */        public Machine getMachine() {            return Machine.this;        }        /**         * getter for the state field         * @return the State of the Machine         */        public State getState() {            return this.state;        }        /**         * getter for the value field         * @return the value of the state         */        public Object getValue() {            return this.value;        }        /**         * setter for the state field         * @param newState the new State of the Machine         */        public void setState(State newState) {            this.state = newState;        }        /**         * setter for the value field         * @param newValue the new value of the state         */        public void setValue(Object newValue) {            this.value = newValue;        }    }    /**     * Returns the wrapped state of the machine.     *     * @return - the wrapped state of the machine.     */    public StateWrapper getStateWrapper() {        return wrappedState.get();    }    /**     * Sets the state of the machine, resulting in a new StateWrapper object     * that wraps the newState parameter and associates the newValue parameter.     *     * @param newState - The new state's name.     * @param newValue - The new state's value.     */    public void setState(State newState, Object newValue) {        StateWrapper stateWrapper = new StateWrapper(newState, newValue);        this.wrappedState.set(stateWrapper);    }    /**     * Gives the state Simple Object Property.     *     * @return - the state Simple Object Property.     */    public SimpleObjectProperty<StateWrapper> stateProperty() {        return this.wrappedState;    }    /**     * Gives an Array of the default PunctChars and their     * Uses.     *     * @return - an Array of the default PunctChars and their     *         Uses.     */    public PunctChar[] getDefaultPunctChars() {        //for the punctuation characters:  !@$%^&*()_={}[]|\:;,.?/~#-+`        //    creates the default uses.        //The other four punctuation characters: <>"'        //    already have special meanings and cannot be redefined.        //    <>"' are all quoting characters.        // The punctuation characters + and - can initiate numbers but        //    otherwise they can be symbol chars or        //    individual tokens or illegal.        return new PunctChar[]{                new PunctChar('!', PunctChar.Use.symbol),                new PunctChar('#', PunctChar.Use.symbol),                new PunctChar('$', PunctChar.Use.symbol),                new PunctChar('%', PunctChar.Use.symbol),                new PunctChar('&', PunctChar.Use.symbol),                new PunctChar('^', PunctChar.Use.symbol),                new PunctChar('_', PunctChar.Use.symbol),                new PunctChar('`', PunctChar.Use.symbol),                new PunctChar('*', PunctChar.Use.symbol),                new PunctChar('?', PunctChar.Use.symbol),                new PunctChar('@', PunctChar.Use.symbol),                new PunctChar('~', PunctChar.Use.symbol),                new PunctChar('+', PunctChar.Use.symbol),                new PunctChar('-', PunctChar.Use.symbol),                new PunctChar('(', PunctChar.Use.token),                new PunctChar(')', PunctChar.Use.token),                new PunctChar(',', PunctChar.Use.token),                new PunctChar('/', PunctChar.Use.token),                new PunctChar('=', PunctChar.Use.token),                new PunctChar('[', PunctChar.Use.token),                new PunctChar('\\', PunctChar.Use.token),                new PunctChar(']', PunctChar.Use.token),                new PunctChar('{', PunctChar.Use.token),                new PunctChar('|', PunctChar.Use.token),                new PunctChar('}', PunctChar.Use.token),                new PunctChar('.', PunctChar.Use.pseudo),                new PunctChar(':', PunctChar.Use.label),                new PunctChar(';', PunctChar.Use.comment),        };    }    ////////////////// Setters and setters //////////////////    public PunctChar[] getPunctChars() {        return punctChars;    }    public void setPunctChars(PunctChar[] punctChars) {        this.punctChars = punctChars;    }    public char getLabelChar() {        for (PunctChar c : punctChars) {            if (c.getUse() == PunctChar.Use.label) {                return c.getChar();            }        }        //should never get this far        Assert.That(false, "No label char found in the machine's punctChars");        return ':';  //to satisfy the compiler    }        public char getCommentChar() {        for (PunctChar c : punctChars) {            if (c.getUse() == PunctChar.Use.comment) {                return c.getChar();            }        }        //should never get this far        Assert.That(false, "No comment char found in the machine's punctChars");        return ';';  //to satisfy the compiler    }    public List<Field> getFields() {        return fields;    }    public void setFields(List<Field> f) {        fields = f;    }    /**     * A getter method for all module objects     *     * @param moduleType a String that describes the type of module     * @return a Vector object     */    public ObservableList<? extends Module> getModule(String moduleType) {        return moduleMap.get(moduleType);    }    /**     * A getter method for all microinstructions     *     * @param micro a String that describes the microinstruction type     * @return a Vector object     */    public ObservableList<Microinstruction> getMicros(String micro) {        return microMap.get(micro);    }    public List<MachineInstruction> getInstructions() {        return instructions;    }    public ObservableList<EQU> getEQUs() {        return EQUs;    }    public End getEnd() {        return (End) (microMap.get("end")).get(0);    }    public int getStartingAddressForLoading() {        return startingAddressForLoading;    }        public SimpleObjectProperty<RAM> getCodeStoreProperty() {        return codeStore;    }    public RAM getCodeStore() {        return codeStore.get();    }    public void setCodeStore(RAM r) {        codeStore.set(r);    }    public void setStartingAddressForLoading(int add) {        startingAddressForLoading = add;    }    private void initializeModuleMap() {        moduleMap.put("registers", registers);        moduleMap.put("registerArrays", registerArrays);        moduleMap.put("conditionBits", conditionBits);        moduleMap.put("rams", rams);    }    private void initializeMicroMap() {        for (String aMicroClass : MICRO_CLASSES)            microMap.put(aMicroClass, FXCollections.observableArrayList(new                    ArrayList<Microinstruction>()));        microMap.get("end").add(new End(this));        microMap.get("comment").add(new Comment());    }    //--------------------------------    // copyFrom:  Copies the data from the newMachine into this machine    public void copyFrom(Machine newMachine) {        if (newMachine.equals(this)) {            return; //no need to copy--occurs in        }        //case of command line loading        this.registers.clear();        this.registers.addAll(newMachine.registers);        this.registerArrays.clear();        this.registerArrays.addAll(newMachine.registerArrays);        this.conditionBits.clear();        this.conditionBits.addAll(newMachine.conditionBits);        this.rams.clear();        this.rams.addAll(newMachine.rams);        this.fields.clear();        this.fields.addAll(newMachine.fields);        for (String aMicroClass : MICRO_CLASSES) {            ObservableList<Microinstruction> newMicros =                    newMachine.microMap.get(aMicroClass);            microMap.put(aMicroClass, newMicros);        }        this.instructions = newMachine.instructions;        for (MachineInstruction instr : instructions)            instr.setMachine(this);        this.EQUs = newMachine.EQUs;        this.controlUnit = newMachine.controlUnit;        this.fetchSequence = newMachine.fetchSequence;        this.codeStore = newMachine.codeStore;        this.startingAddressForLoading = newMachine.startingAddressForLoading;        this.setName(newMachine.getName());        this.punctChars = newMachine.punctChars;        //fix the Decode & End micros to refer to this        //machine instead of the newMachine.        this.getEnd().setMachine(this);        for (int i = 0; i < getMicros("decode").size(); i++) {            ((Decode) getMicros("decode").get(i)).setMachine(this);        }    }    //-----------------------------------    // returns a new Vector containing all the registers of the machine    // with the registers coming from register arrays at the end of the vector.    public ObservableList<Register> getAllRegisters() {        ObservableList<Register> allRegisters = FXCollections.observableArrayList();        //clones the registers into a different observable list        //this may cause an error when run (I'm not sure if it should be        //size() or size()-1        for (Register register1 : registers) {            allRegisters.add(register1);        }        for (RegisterArray registerArray : registerArrays) {            ObservableList<Register> registers = registerArray.registers();            for (Register register : registers) {                allRegisters.add(register);            }        }        return allRegisters;    }    //-----------------------------------    // returns a new observable list containing all the RAMs of the machine.    @SuppressWarnings("unchecked")    public ObservableList<RAM> getAllRAMs() {        return (ObservableList<RAM>) getModule("rams");    }    //-------------------------------    // updates machine instructions    public void setInstructions(List<MachineInstruction> newInstructions) {        instructions = newInstructions;    }    //-------------------------------    // updates global EQUs    public void setEQUs(ObservableList<EQU> newEQUs) {        EQUs = newEQUs;    }    //-------------------------------    // updates the registers    public void setRegisters(Vector<Register> newRegisters) {        for (Register oldRegister : registers) {            if (!newRegisters.contains(oldRegister)) {                HashMap microsThatUseIt = getMicrosThatUse(oldRegister);                Set e = microsThatUseIt.keySet();                for (Object anE : e) {                    //get next micro                    Microinstruction micro = (Microinstruction) anE;                    //remove it from all machine instructions                    removeAllOccurencesOf(micro);                    //also remove the microinstruction from its list.                    ((ObservableList) microsThatUseIt.get(micro)).remove(micro);                }            }        }        //reuse the old Vector since the desktop uses it as the key        //in the moduleWindows hashtable.        registers.clear();        registers.addAll(newRegisters);    }    //-------------------------------    // updates the register arrays    public void setRegisterArrays(Vector<RegisterArray> newRegisterArrays) {        for (RegisterArray oldArray : registerArrays) {            if (!newRegisterArrays.contains(oldArray)) {                HashMap microsThatUseIt = getMicrosThatUse(oldArray);                Set e = microsThatUseIt.keySet();                for (Object anE : e) {                    //get next micro; should be transferRtoA or transferAtoR                    Microinstruction micro =                            (Microinstruction) anE;                    //remove it from all machine instructions                    removeAllOccurencesOf(micro);                    //also remove the microinstruction from its list.                    ((Vector) microsThatUseIt.get(micro)).removeElement(micro);                }            }        }        //reuse the old Vector in case in the future some other objects use it.        registerArrays.clear();        registerArrays.addAll(newRegisterArrays);    }    //--------------------------------    // returns a HashMap whose keys consist of all microinstructions that    // use m and such that the value associated with each key is the vector    // of microinstructions that contains the key.    public HashMap getMicrosThatUse(Module m) {        HashMap<Microinstruction, ObservableList> result =                new HashMap<Microinstruction, ObservableList>();        for (String aMicroClass : MICRO_CLASSES) {            ObservableList<Microinstruction> v = microMap.get(aMicroClass);            for (int i = v.size() - 1; i >= 0; i--) {                Microinstruction micro = v.get(i);                if (micro.uses(m)) {                    result.put(micro, v);                }            }        }        return result;    }    //-------------------------------    // updates the End microinstruction    public void setEnd(End end) {        ObservableList<Microinstruction> ends = microMap.get("end");        ends.clear();        ends.add(end);    }    //-------------------------------    // updates the condition bits    public void setConditionBits(Vector<ConditionBit> newConditionBits) {        //first delete arithmetics, setCondBits, and increments that use        //any deleted ConditionBits, including removing        //these micros from all machine instructions that use them.        for (int i = getMicros("arithmetic").size() - 1; i >= 0; i--) {            Arithmetic micro = (Arithmetic) getMicros("arithmetic").get(i);            if (((!newConditionBits.contains(micro.getOverflowBit())) &&                    (micro.getOverflowBit() != NO_CONDITIONBIT)) ||                    ((!newConditionBits.contains(micro.getCarryBit())) &&                            (micro.getCarryBit() != NO_CONDITIONBIT))) {                removeAllOccurencesOf(micro);                getMicros("arithmetic").remove(micro);            }        }        for (int i = getMicros("setCondBit").size() - 1; i >= 0; i--) {            SetCondBit micro = (SetCondBit) getMicros("setCondBit").get(i);            if (!newConditionBits.contains(micro.getBit())) {                removeAllOccurencesOf(micro);                getMicros("setCondBit").remove(micro);            }        }        for (int i = getMicros("increment").size() - 1; i >= 0; i--) {            Increment micro = (Increment) getMicros("increment").get(i);            if ((!newConditionBits.contains(micro.getOverflowBit())) &&                    (micro.getOverflowBit() != NO_CONDITIONBIT)) {                removeAllOccurencesOf(micro);                getMicros("increment").remove(micro);            }        }        conditionBits.clear();        conditionBits.addAll(newConditionBits);    }    //-------------------------------    // updates the RAM arrays    public void setRAMs(Vector<RAM> newRams) {        //first delete all MemoryAccess micros that reference any deleted rams        //    and remove the micros from all machine instructions that use them.        for (int i = getMicros("memoryAccess").size() - 1; i >= 0; i--) {            MemoryAccess access = (MemoryAccess) getMicros(                    "memoryAccess").get(i);            if (!newRams.contains(access.getMemory())) {                removeAllOccurencesOf(access);                getMicros("memoryAccess").remove(access);            }        }        rams.clear();        rams.addAll(newRams);    }    //-------------------------------    // updates the given vector of micros of a given class, but first deletes    // all references to a deleted micro    public void setMicros(String microClass, ObservableList<Microinstruction> newMicros) {        //first delete all references in machine instructions        // to any old micros not in the new list        ObservableList<Microinstruction> oldMicros = microMap.get(microClass);        for (Microinstruction oldMicro : oldMicros) {            if (!newMicros.contains(oldMicro)) {                removeAllOccurencesOf(oldMicro);            }        }        microMap.put(microClass, newMicros);    }    // other utility methods    //--------------------------------    // returns true if the given microinstruction is in the hash table    // of micros for this machine    public boolean contains(Microinstruction micro) {        for (String microClass : MICRO_CLASSES) {            ObservableList v = microMap.get(microClass);            if (v.contains(micro)) {                return true;            }        }        return false;    }    //--------------------------------    // resetAllChannels    public void resetAllChannels() {        ObservableList ios = getMicros("io");        for (Object io : ios) {            ((IO) io).getConnection().reset();        }    }    public void resetAllChannelsButConsole() {        ObservableList ios = getMicros("io");        for (Object io : ios) {            IOChannel channel = ((IO) io).getConnection();            if (channel != CONSOLE_CHANNEL) {                channel.reset();            }        }    }    //--------------------------------    // JRL (11.3.00) clearAllRegisters    public void clearAllRegisters() {        ObservableList<Register> registers =                (ObservableList<Register>) getModule("registers");        for (Register register : registers) {            register.clear();        }    }    //--------------------------------    // JRL (11.3.00) clearAllRegisterArrays    public void clearAllRegisterArrays() {        for (int i = 0; i < getModule("registerArrays").size(); i++) {            ((RegisterArray) getModule("registerArrays").get(i)).clear();        }    }    //--------------------------------    // JRL (11.3.00) clearAllRAMs    public void clearAllRAMs() {        for (int i = 0; i < getModule("rams").size(); i++) {            ((RAM) getModule("rams").get(i)).clear();        }    }    //--------------------------------    // returns a Vector of all machine instructions that use m    public Vector getInstructionsThatUse(Microinstruction m) {        Vector<MachineInstruction> result = new Vector<MachineInstruction>();        for (MachineInstruction instr : instructions) {            if (instr.usesMicro(m)) {                result.addElement(instr);            }        }        //don't forget the fetchSequence too.        if (fetchSequence.usesMicro(m)) {            result.addElement(fetchSequence);        }        return result;    }    //--------------------------------    // returns a List of all machine instructions that use f    public List<MachineInstruction> getInstructionsThatUse(Field f) {        List<MachineInstruction> result = new ArrayList<MachineInstruction>();        for (MachineInstruction instr : instructions) {            if (instr.usesField(f)) {                result.add(instr);            }        }        return result;    }    //--------------------------------    // deletes from all machine instructions every use of m    public void removeAllOccurencesOf(Microinstruction m) {        for (MachineInstruction instr : instructions) {            instr.removeMicro(m);        }        fetchSequence.removeMicro(m);    }    //--------------------------------    // get the control unit    public ControlUnit getControlUnit() {        return controlUnit;    }    //--------------------------------    // get & set the fetch sequence    public MachineInstruction getFetchSequence() {        return fetchSequence;    }    public void setFetchSequence(MachineInstruction f) {        fetchSequence = f;    }    //--------------------------------    // called by user when they want to halt execution    public void setRunMode(RunModes newRunMode) {        runMode = newRunMode;    }    //--------------------------------    // called by user when they want to know what the status of the    // current execution.    public RunModes getRunMode() {        return runMode;    } // Get the current microinstruction
    public Microinstruction getCurrentMicro() {
    	MachineInstruction currentInstruction =
		                		controlUnit.getCurrentInstruction();
		List<Microinstruction> microInstructions =
		                     	currentInstruction.getMicros();
		int currentIndex = controlUnit.getMicroIndex();
		
		if (currentIndex < 0 || currentIndex >= microInstructions.size()) {
			System.out.println("Error: The step is out of range\n" +
			  "at step " + currentIndex + " of " +
			  controlUnit.getCurrentInstruction() + ".\n");
			}
		
		return microInstructions.get(currentIndex);
    }
        /**     * executes the machine using the given mode of execution.     * The mode can be any of the following values in CPUSimConstants:     * RUN, STEP_BY_MICRO, STEP_BY_INSTR, STOP, ABORT, COMMAND_LINE     * The machine uses the current contents of memory and its registers     * and the control store as it executes.     * @param mode the run mode for execution.     */    public void execute(final RunModes mode) {        setRunMode(mode);        if (mode == RunModes.COMMAND_LINE)        {            // do not use the GUI at all--purely command line execution            // There is no stepping or backing up.  It executes in the            // main (and only) thread until it finishes or the user            // quits it from the command line (like with Ctrl-C).            while (runMode != RunModes.STOP &&                    runMode != RunModes.ABORT &&                    haltBitsThatAreSet().size() == 0) {                MachineInstruction currentInstruction =                                          controlUnit.getCurrentInstruction();                List<Microinstruction> microInstructions =                                               currentInstruction.getMicros();                int currentIndex = controlUnit.getMicroIndex();                if (currentIndex < 0 || currentIndex >= microInstructions.size()) {                    System.out.println("Error: The step is out of range\n" +                            "at step " + currentIndex + " of " +                            controlUnit.getCurrentInstruction() + ".\n");                    break;                }                Microinstruction currentMicro = microInstructions.get(currentIndex);                controlUnit.incrementMicroIndex(1);                try {                    currentMicro.execute();                } catch (ExecutionException e) {                    System.out.println("Exception thrown: " + e.getMessage());                    setState(Machine.State.EXECUTION_ABORTED,null);                    return;                }            }            setState(Machine.State.EXECUTION_HALTED,null);            System.out.println("Execution halted.");        }        else {  // use the GUI            setState(State.START_OF_EXECUTE_THREAD, runMode == RunModes.RUN);            Task<Void> executionTask = new Task<Void>() {                @Override                protected Void call() throws Exception {                    while (runMode != RunModes.STOP &&                            runMode != RunModes.ABORT &&                            !isCancelled() &&                            haltBitsThatAreSet().size() == 0) {                        MachineInstruction currentInstruction =                                            controlUnit.getCurrentInstruction();                        List<Microinstruction> microInstructions =                                                 currentInstruction.getMicros();                        int currentIndex = controlUnit.getMicroIndex();                        if (currentIndex < 0 ||                                currentIndex >= microInstructions.size()) {                            //change the state indicating an exception and quit                            setState(Machine.State.EXCEPTION_THROWN, "The step is out of range\n" +                                    "at step " + currentIndex + " of " +                                    currentInstruction + ".\n");                            break;                        }                        else if (runMode != RunModes.RUN &&                                currentIndex == 0 &&                                currentInstruction == getFetchSequence()) {                            //fire property change for start of a machine cycle                            setState(Machine.State.START_OF_MACHINE_CYCLE, false); //false is unused                        }                        Microinstruction currentMicro = microInstructions.get(currentIndex);                        // Fire property change indicating the start of a                        // microinstruction                        if (currentIndex < microInstructions.size()) {                                 setState(Machine.State.START_OF_MICROINSTRUCTION,                                          controlUnit.getCurrentState());                        }                        controlUnit.incrementMicroIndex(1);                        try {                            currentMicro.execute();                        } catch (ExecutionException e) {                            //fire property change indicating an exception and quit                            setState(Machine.State.EXCEPTION_THROWN, e.getMessage());                            controlUnit.setMicroIndex(currentIndex);                            return null;                        } catch (BreakException e) {                            //fire property change indicating an exception and quit                            setState(Machine.State.BREAK, e);                            runMode = RunModes.STOP;                        }                        if (runMode == RunModes.STEP_BY_MICRO) {                            runMode = RunModes.STOP;                        }                        if (runMode == RunModes.STEP_BY_INSTR && currentMicro == getEnd()) {                            runMode = RunModes.STOP;                        }                    }                    //Now fire a property change that execution halted or aborted                    setState(                        runMode == RunModes.ABORT ? Machine.State.EXECUTION_ABORTED :                        mode == RunModes.STEP_BY_MICRO ? Machine.State.HALTED_STEP_BY_MICRO :                         /* else */ Machine.State.EXECUTION_HALTED, haltBitsThatAreSet().size() > 0);                    return null;                }            };            new Thread(executionTask,"Machine execution thread").start();        }    }    //--------------------------------    // haltBitsThatAreSet: returns a Vector of all condition bits    // specified by all Halt micros have value 1    public Vector haltBitsThatAreSet() {        Vector<ConditionBit> result = new Vector<ConditionBit>();        for (int i = 0; i < getModule("conditionBits").size(); i++) {            ConditionBit condBit = conditionBits.get(i);            if (condBit.getHalt() && condBit.isSet()) {                result.addElement(condBit);            }        }        return result;    }    public ObservableList<Register> getRegisters() {        return registers;    }    public List<Comment> getCommentMicros() {        //go through all the instrs and get their Comment micros in a list.        List<Comment> result = new ArrayList<Comment>();        for (MachineInstruction instr : instructions) {            List<Microinstruction> micros = instr.getMicros();            for (Microinstruction micro : micros)                if (micro instanceof Comment) {                    result.add((Comment) micro);                }        }        //now do the same for the fetch sequence        for (Microinstruction micro : fetchSequence.getMicros())            if (micro instanceof Comment) {                result.add((Comment) micro);            }        return result;    }    //----------------------------------------    // Module abstract methods that are implemented here    public Object clone() {        return new Machine(getName());        // DJS Do more here?    }    public void copyDataTo(Module newModule) {        Assert.That(newModule instanceof Machine,                "Passed non-Machine to Machine.copyDataTo()");        // DJS Do more here?    }    public String getXMLDescription() {        return getHTMLName();        // DJS Do more here?    }    public String getHTMLDescription() {        return getHTMLName();        // DJS Do more here?    }}
