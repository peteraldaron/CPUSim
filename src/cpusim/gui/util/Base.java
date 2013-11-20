/*
 * Base.java
 */
package cpusim.gui.util;

/**
 * This class stores a base ("Bin", "Dec", "Hex", "Uns_Dec" or "ASCII")
 * for displaying values of registers or RAM cells.
 * @author Ben Borchard
 * 
 * Nov. 15, 2013 edited by: Stephen Jenkins, Brendan Tschaen, Peter Zhang
 */
public class Base {
	public static final String BINARY = "Bin";
	public static final String DECIMAL = "Dec";
	public static final String HEX = "Hex";
	public static final String UNSIGNED_DEC = "Uns_Dec";
	public static final String ASCII = "ASCII";
	
    private String base;
    
    public Base(String base) {
        this.base = base;
    }
    
    public String getBase() {
        return base;
    }
    
    public void setBase(String base) {
        this.base = base;
    }

    public String toString() {
    	return base;
    }
}
