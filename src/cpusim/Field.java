/**
 * File: Field
 * Last Update: August 2013
 * @author Kevin Marsteller & Dale Skrien
 */
package cpusim; 

import cpusim.util.*;
import cpusim.xml.*;
import java.util.ArrayList;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * Class used to hold field options for each field of 
 * a machine instruction.
 */
public class Field implements NamedObject {

	public static enum Relativity {absolute, pcRelativePreIncr,
                                    pcRelativePostIncr}
    public static enum Type {required, optional, ignored}

    // The name of the field
    private SimpleStringProperty name;    
    // One of the three values of enum Type
    private SimpleObjectProperty<Type> type;  
    // The number of bits of the field
    private SimpleIntegerProperty numBits;          	
    // Absolute or pc relative
    private SimpleObjectProperty<Relativity> relativity;
    // The acceptable values for this field
    private ObservableList<FieldValue> values;  		
    // The value to use if optional or ignored
    private SimpleIntegerProperty defaultValue; 		

    // If true, allows only signed 2's complement values
    // If false, allows only unsigned binary values
    private SimpleBooleanProperty signed; 

    private String ID; //used for saving to XML


    /** 
     * Constructor for Field with name "?".
     */
    public Field() {
        this("?");
    }
    
    /**
     * Constructor for new Field with specified Name.
     * Other values are set to defaults.
     * 
     * @param name - The name of the field.
     */
    public Field(String name) {
        this(name, Type.required, 0, Relativity.absolute,
             FXCollections.observableArrayList(new ArrayList<FieldValue>()), 0, true);
    }
    
    /**
     * Constructor for new Field with specified values.
     * 
     * @param name - Name of Field.
     * @param type - Type of Field.
     * @param length - Numbits of field.
     * @param relativity - Enum relativity type.
     * @param values - List of FieldValues.
     * @param defaultValue - The default int value.
     * @param signed - Whether or not the Field is a signed int.
     */
    public Field(String name, Type type, int length, Relativity relativity,
               ObservableList<FieldValue> values, int defaultValue, boolean signed) {
        this.name = new SimpleStringProperty();
        this.type = new SimpleObjectProperty<>();
        this.numBits = new SimpleIntegerProperty();
        this.relativity = new SimpleObjectProperty<>();
        this.defaultValue = new SimpleIntegerProperty();
        this.signed = new SimpleBooleanProperty();
        
        this.name.set(name);
        this.type.set(type);
        this.numBits.set(length);
        this.relativity.set(relativity);
        this.values = values;
        this.defaultValue.set(defaultValue);
        this.signed.set(signed);

        String s = super.toString();
        int index = s.indexOf('@');
        if (index == -1)
            ID = s;
        else
            ID = s.substring(7, index) + s.substring(index + 1);
    }

    ////////////////// Setters and getters //////////////////
    
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public Type getType() {
        return type.get();
    }

    public void setType(Type type) {
        this.type.set(type);
    }

    public int getNumBits() {
        return numBits.get();
    }

    public void setNumBits(int numBits) {
        this.numBits.set(numBits);
    }

    public Relativity getRelativity() {
        return relativity.get();
    }

    public void setRelativity(Relativity relativity) {
        this.relativity.set(relativity);
    }

    public ObservableList<FieldValue> getValues() {
        return values;
    }

    public void setValues(ObservableList<FieldValue> values) {
        this.values = values;
    }

    public int getDefaultValue() {
        return defaultValue.get();
    }

    public void setDefaultValue(int defaultValue) {
        this.defaultValue.set(defaultValue);
    }

    public boolean isSigned() {
        return signed.get();
    }

    public void setSigned(boolean signed) {
        this.signed.set(signed);
    }

    public SimpleBooleanProperty signedProperty() {
        return signed;
    }

    public String getID() { 
    	return ID; 
    }
    
    /**
     * Puts all the data in this field into
     * the specified field.
     * 
     * @param newField - The specified field that
     * the data should be copied into.
     */
    public void copyDataTo(Field newField) {
        newField.setName(getName());
        newField.setType(getType());
        newField.setRelativity(getRelativity());
        newField.setNumBits(getNumBits());
        newField.setDefaultValue(getDefaultValue());
        newField.setSigned(isSigned());
        newField.setValues(getValues());
    }

    /**
     * returns the FieldValue with the given name.
     * returns null if there is no such FieldValue
     * @param name the String with the name of the value
     * @return the FieldValue with that name, or null if none exists
     */
    public FieldValue getValue(String name) {
        for (FieldValue value : getValues()) {
            if (value.getName().equals(name)) {
            	return value;
            }
        }
        return null;
    }

    /**
     * Gives a string representation of this object.
     */
    public String toString() {
        return name.get();
    }

    /**
     * Returns a clone of this field.
     */
    public Object clone() {
        Field c = new Field();
        c.setName(name.get());
        c.setType(type.get());
        c.setNumBits(numBits.get());
        c.setRelativity(relativity.get());
        c.setValues(values);
        c.setDefaultValue(defaultValue.get());
        c.setSigned(signed.get());
        return c;
    }

    /**
     * Gives the XMLDescription of this Field.
     * 
     * @param indent - amount of indent. String of spaces.
     * @return - The XML description.
     */
    public String getXMLDescription(String indent) {
        String nl = System.getProperty("line.separator");
        String result = indent + "<Field name=\"" + HtmlEncoder.sEncode(getName()) +
                "\" type=\"" + getType() + "\" numBits=\"" + getNumBits() +
                "\" relativity=\"" + getRelativity() + "\" signed=\"" + isSigned()
                + "\" defaultValue=\"" + getDefaultValue() +
                "\" id=\"" + getID() + "\">" + nl;
        for (FieldValue value : values)
            result += indent + "\t" + value.getXMLDescription() + nl;
        result += indent + "</Field>";
        return result;
    }
    
    /**
     * Gives an HTML description of this Field.
     * 
     * @return Gives an HTML description of this Field.
     */
    public String getHTMLDescription() {
        String result = "<TR><TD>" + HtmlEncoder.sEncode(name.get()) +
                "</TD><TD>" + getType() +
                "</TD><TD>" + getNumBits() +
                "</TD><TD>" + getRelativity() +
                "</TD><TD>" + isSigned() +
                // "</TD><TD>" + offset +  //offset is not yet used
                "</TD><TD>" + getDefaultValue() + "</TD><TD>";
        if (values.size() == 0) result += HtmlEncoder.sEncode("<any>");
        for (FieldValue fieldValue : values) {
            result += HtmlEncoder.sEncode(fieldValue.getName() + "=") +
                    fieldValue.getValue() + "<BR>";
        }
        return result + "</TD></TR>";
    }
}