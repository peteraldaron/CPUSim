/**
 * Author: Jinghui Yu
 * Last editing date: 6/6/2013
 */

package cpusim.microinstruction;

import cpusim.Microinstruction;
import cpusim.Module;
import cpusim.module.ControlUnit;
import cpusim.util.Assert;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * The branch microinstruction is identical to the Test microinstruction except
 * that it is an unconditional jump.
 */
public class Branch extends Microinstruction
{
    private SimpleIntegerProperty amount;
    private ControlUnit controlUnit;

    /**
     * Constructor
     * creates a new Branch object with input values.
     *
     * @param name name of the microinstruction.
     * @param amount size of the relative jump.
     */
    public Branch(String name,
                  Integer amount,
                  ControlUnit controlUnit)
    {
        super(name);
        this.amount = new SimpleIntegerProperty(amount);
        this.controlUnit = controlUnit;
    }

    /**
     * returns the fixed value stored in the set microinstruction.
     *
     * @return the integer value of the field.
     */
    public Integer getAmount()
    {
        return amount.get();
    }

    /**
     * updates the fixed value stored in the set microinstruction.
     *
     * @param newAmount the new value for the field.
     */
    public void setAmount(Integer newAmount)
    {
        amount.set(newAmount);
    }
    
    /**
     * returns the class of the microinstruction
     * @return the class of the microinstruction
     */
    @Override
    public String getMicroClass(){
        return "branch";
    }

    /**
     * duplicate the set class and return a copy of the original Set class.
     *
     * @return a copy of the Set class
     */
    public Object clone()
    {
        return new Branch(getName(),getAmount(),this.controlUnit);
    }

    /**
     * increment the micro index by the amount specified by the instruction
     */
    public void execute()
    {
        controlUnit.incrementMicroIndex(amount.get());
    }

    /**
     * copies the data from the current micro to a specific micro
     * @param oldMicro the micro instruction that will be updated
     */
    public void copyDataTo(Microinstruction oldMicro)
    {
        Assert.That(oldMicro instanceof Branch,
                "Passed non-Branch to Branch.copyDataTo()");
        Branch newBranch = (Branch) oldMicro;
        newBranch.setName(getName());
        newBranch.setAmount(getAmount());
        newBranch.controlUnit = controlUnit;
    }

    /**
     * returns the XML description
     * @return the XML description
     */
    public String getXMLDescription()
    {
        return "<Branch name=\"" + getHTMLName() +
                "\" amount=\"" + getAmount() +
                "\" id=\"" + getID() + "\" />";
    }

    /**
     * returns the HTML description
     * @return the HTML description
     */
    public String getHTMLDescription()
    {
        return "<TR><TD>" + getHTMLName() + "</TD><TD>" +
                getAmount() + "</TD></TR>";
    }

    /**
     * returns true if this microinstruction uses m
     * (so if m is modified, this micro may need to be modified.
     * @param m the module that holds the microinstruction
     * @return boolean value true if this micro used the module
     */
    public boolean uses(Module m)
    {
        return false;
    }
}
