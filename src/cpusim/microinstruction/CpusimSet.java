/**
 * auther: Jinghui Yu
 * last editing date: 6/5/2013
 */

package cpusim.microinstruction;

import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.Register;
import cpusim.util.Assert;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;

/**
 * The Set microinstruction allows the computer to set the contents
 * of any contiguous set of bits in any register to any fixed value.
 */
public class CpusimSet extends Microinstruction{

    private SimpleObjectProperty<Register> register;
    private SimpleIntegerProperty start;
    private SimpleIntegerProperty numBits;
    private SimpleLongProperty value;

    /**
     * Constructor
     * creates a new Set object with input valules.
     *
     * @param name name of the microinstruction.
     * @param register the register whose bits are to be set.
     * @param start the leftmost bit of the register that is to be set.
     * @param numBits the number of consecutive bits in the register to be set.
     * @param value the base-10 value to which the bits are to be set.
     */
    public CpusimSet(String name,
                     Register register,
                     Integer start,
                     Integer numBits,
                     Long value){
        super(name);
        this.register = new SimpleObjectProperty<>(register);
        this.start = new SimpleIntegerProperty(start);
        this.numBits = new SimpleIntegerProperty(numBits);
        this.value = new SimpleLongProperty(value);
    }


    /**
     * @return the Register in the set microinstruction.
     */
    public Register getRegister(){
        return register.get();
    }

    /**
     * updates the register used by the microinstruction.
     * @param newRegister the new selected Register for the set microinstruction.
     */
    public void setRegister(Register newRegister){
        register.set(newRegister);
    }

    /**
     * returns the index of the start bit of the microinstruction.
     * @return the integer value of the index.
     */
    public Integer getStart(){
        return start.get();
    }

    /**
     * updates the index of the start bit of the microinstruction.
     * @param newStart the new index of the start bit for the set microinstruction.
     */
    public void setStart(Integer newStart){
        start.set(newStart);
    }

    /**
     * returns the number of bits of the value.
     * @return the integer value of the number of bits.
     */
    public Integer getNumBits(){
        return numBits.get();
    }

    /**
     * updates the number of bits of the value.
     * @param newNumbits the new value of the number of bits.
     */
    public void setNumBits(Integer newNumbits){
        numBits.set(newNumbits);
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     * @return the integer value of the field.
     */
    public Long getValue(){
        return value.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     * @param newValue the new value for the field.
     */
    public void setValue(Long newValue){
        value.set(newValue);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "set";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     * @return a copy of the Set class
     */
    public Object clone(){
        return new CpusimSet(getName(),getRegister(),getStart(),getNumBits(),getValue());
    }

    /**
     * execute the micro instruction from machine
     */
    public void execute()
    {
        int width = register.get().getWidth();
        long registerValue = register.get().getValue() << (64 - width);
        long leftPart = (start.get() == 0 ? 0 :
                (registerValue >>> (64 - start.get())) << (64 - start.get()));
        long middlePart = (value.get() << (64 - numBits.get())) >>> start.get();
        long rightPart = (start.get() + numBits.get() == 64 ? 0 :
                (registerValue << (start.get() + numBits.get()))
                        >>> (start.get() + numBits.get()));
        long result = (leftPart | middlePart | rightPart) >> (64 - width);
        register.get().setValue(result);
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro){
        Assert.That(oldMicro instanceof CpusimSet,
                "Passed non-Set to Set.copyDataTo()");
        CpusimSet newSet = (CpusimSet) oldMicro;
        newSet.setName(getName());
        newSet.setRegister(getRegister());
        newSet.setStart(getStart());
        newSet.setNumBits(getNumBits());
        newSet.setValue(getValue());
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription(){
        return "<Set name=\"" + getHTMLName() +
                "\" register=\"" + getRegister().getID() +
                "\" start=\"" + getStart() +
                "\" numBits=\"" + getNumBits() +
                "\" value=\"" + getValue() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription(){
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" + getRegister().getHTMLName() +
                "</TD><TD>" + getStart() + "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getValue() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m){
        return (m == register.get());
    }

}
