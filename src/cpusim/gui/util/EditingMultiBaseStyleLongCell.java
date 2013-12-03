/**
 * author: Jinghui Yu
 * last edit data: 6/3/2013
 */

package cpusim.gui.util;

import cpusim.assembler.EQU;
import cpusim.module.RAMLocation;
import cpusim.module.Register;
import cpusim.util.Convert;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.control.Dialogs;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;

/**
 * An editable cell class that allows the user to modify the integer in the cell.
 */

public class EditingMultiBaseStyleLongCell<T> extends TableCell<T, Long> {
    private TextField textField;
    private Base base;
    public String color;
    private String errorMessage;
    private Boolean valid;
    private int cellSize;
    public SimpleStringProperty tooltipStringProperty;

    public EditingMultiBaseStyleLongCell(Base base, String color) {
        this.base = base;
        tooltipStringProperty = new SimpleStringProperty("");
        this.color = color;
    }
       
    public Base getBase(){
        return base;
    }

    /**
     * What happens when the user starts to edit the table cell.  Namely that 
     * a text field is created 
     */
    @Override
    public void startEdit() {
        if (!isEmpty()) {
            super.startEdit();
            createTextField();
            setText(null);
            setGraphic(textField);
            textField.selectAll();
        }
    }

    /**
     * What happens when the user cancels while editing a table cell.  Namely the
     * graphic is set to null and the text is set to the proper value
     */
    @Override
    public void cancelEdit() {
        super.cancelEdit();

        setText(formatString(prepareString(String.valueOf(getItem()))));
        setGraphic(null);
    }

    /**
     * updates the Long in the table cell
     * @param item used for the parent method
     * @param empty used for the parent method
     */
    @Override
    public void updateItem(Long item, boolean empty) {
        super.updateItem(item, empty);
        
        setStyle(color);

        
        if (empty) {
            setText(null);
            setGraphic(null);
            setTooltip(null);
        }
        else {
            if (isEditing()) {
                if (textField != null) {
                    textField.setText(formatString(prepareString(getString())));
                }
                setText(null);
                setGraphic(textField);
            }
            else {
                setTooltipsAndCellSize();
                setText(formatString(convertLong(Long.parseLong(getString()))));
                setGraphic(null);
            }
        }
    }

    /**
     * creates a text field with listeners so that that edits will be committed 
     * at the proper time
     */
    private void createTextField() {
        textField = new TextField(formatString(convertLong(Long.parseLong(getString()))));
        textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);

        // if user takes focus from the textField, check the validity of its
        // contents and, if valid, save the changes, else, put up an error dialog.
        textField.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> arg0,
                                Boolean arg1, Boolean arg2) {
                if (!arg2) {
                    checkValidity(textField.getText());
                    if (valid) {
                        commitEdit(convertString(prepareString(textField.getText())));
                    }
                    else {
                        if (textField.getScene() != null) {
                            Dialogs.showErrorDialog(
                                    (Stage) textField.getScene().getWindow(),
                                    errorMessage, "Error", "CPU Sim");
                            cancelEdit();
                        }
                    }
                }
            }
        });

        // On ENTER , check for valid value and, if so,
        // save the changes, else, color the cell red.
        // On CANCEL, cancel the editing.
        textField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (t.getCode() == KeyCode.ENTER) {
                    checkValidity(textField.getText());
                    if (valid) {
                        commitEdit(convertString(prepareString(textField.getText())));
                    }
                    else {
                        textField.setStyle("-fx-background-color:red;");
                        textField.setTooltip(new Tooltip(errorMessage));
                        // TODO make this work some other way
                        //The following code crashes the program
                        //Dialogs.showErrorDialog(
                        //        (Stage)textField.getScene().getWindow(),
                        //        "This column requires integer values",
                        //        "Error Dialog", "title");
                        //cancelEdit();
                    }
                }
                else if (t.getCode() == KeyCode.ESCAPE) {
                    cancelEdit();
                }
            }
        });
    }

    /**
     * Checks the validity of the string using the current base and cell size.
     * If not valid, an error message is saved to the errorMessage field.
     * @param s
     */
    private void checkValidity(String s) {
        char[] string = textField.getText().toCharArray();

        if (base.getBase().equals("Bin")) {

            for (char c : string) {
                if (c != '0' && c != '1' && c != ' ') {
                    valid = false;
                    errorMessage = "You need to enter a binary number";
                    return;
                }
            }

            char[] stringWithNoWhiteSpace = Convert.removeAllWhiteSpace(s).toCharArray();

            if (stringWithNoWhiteSpace.length > cellSize) {
                valid = false;
                errorMessage = "This is too many bits, you only have " + cellSize + " available";
                return;
            }

            valid = true;
        }
        else if (base.getBase().equals("Hex")) {
            char[] validChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A',
                    'B', 'C', 'D', 'E', 'F', 'a', 'b', 'c', 'd', 'e', 'f', ' '};

            for (char c : string) {
                boolean valid = false;
                for (char v : validChars) {
                    valid = valid || c == v;
                }
                if (!valid) {
                    this.valid = false;
                    errorMessage = "You need to enter a hexadecimal number";
                    return;
                }
            }

            char[] stringWithoutWhiteSpace = Convert.removeAllWhiteSpace(s).toCharArray();

            if (stringWithoutWhiteSpace.length > cellSize / 4) {
                this.valid = false;
                errorMessage = "You have entered too many hexadecimal digits, you only "
                        + "have " + cellSize / 4 + " available";
                return;
            }
            this.valid = true;
        }
        else if ( base.getBase().equals("ASCII") ){
        	if(s.length() > 1 || ((int) s.charAt(0)) > 255){
        		this.valid = false;
        		errorMessage = "Please enter a valid ASCII char";
        		return;
        	}
        	this.valid = true;
        }
        else { //Decimal or unsigned decimal
            char[] validChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};

            int i = 0;
            for (char c : string) {
                boolean valid = false;
                for (char v : validChars) {
                    valid = valid || c == v;
                }
                if (!valid) {
                    if (i != 0 || c != '-') {
                        this.valid = false;
                        this.errorMessage = "You need to enter an integer";
                        return;
                    }
                }
                i++;
            }

            Integer value = null;
            try  {
                value = Integer.parseInt(Convert.removeAllWhiteSpace(s));
                if( base.getBase().equals("Dec") ){
	                // value must be between -2^(n-1) and 2^(n-1)-1
	                if (value > Math.pow(2, cellSize - 1) - 1 || value < -(Math.pow(2, cellSize - 1))) {
	                    this.valid = false;
	                    
	                    long lowRange = (long) (-(Math.pow(2, cellSize - 1)));
	                    long upperRange = (long) Math.pow(2, cellSize - 1) - 1;
	                    this.errorMessage = "There are not enough bits to represent "
	                    						+ "this value as a signed decimal. "
	                    						+ "Please enter a number between " 
	                    						+ lowRange + " and " + upperRange;
	                    return;
	                }
                }
                else{ //unsigned decimal
                	// value must be between 0 and 2^(n)-1
	                if (value > Math.pow(2, cellSize) - 1 || value < 0 ) {
	                    this.valid = false;
	                    
	                    long lowRange = 0;
	                    long upperRange = (long) (Math.pow(2, cellSize) - 1);
	                    this.errorMessage = "There are not enough bits to represent "
	                    							+ "this value as an unsigned decimal."
	                    							+ "Please enter a number between "
	                    							+ lowRange + " and " + upperRange;
	                    return;
	                }
                }
            }
            catch (NumberFormatException ex){
                this.valid = false;
                this.errorMessage = "There are not enough bits to represent this value.";
                return;
            }

            this.valid = true;
        }
    }

    /**
     * Removes spaces and adds leading zeros to the string to make it the right
     * length.
     * @param s the String to be displayed
     * @return the String with spaces removed and leading zeros added as needed.
     */
    private String prepareString(String s) {
        String string = Convert.removeAllWhiteSpace(s);
        if (base.getBase().equals("Bin")) {
            int zeroesNeeded = cellSize - string.length();
            String zeroString = "";
            for (int i = 0; i < zeroesNeeded; i++) {
                zeroString += "0";
            }
            string = zeroString + string;
        }
        else if (base.getBase().equals("Hex")) {
            int zeroesNeeded = cellSize / 4 - string.length();
            String zeroString = "";
            for (int i = 0; i < zeroesNeeded; i++) {
                zeroString += "0";
            }
            string = zeroString + string;
        }
        else {
            string = s;
        }

        return string;
    }

    private Long convertString(String s) {
        if (base.getBase().equals("Bin")) {
            return Convert.fromBinaryStringToLong(s, cellSize);
        }
        else if (base.getBase().equals("Hex")) {
            return Convert.fromHexadecimalStringToLong(s, cellSize);
        }
        else if (base.getBase().equals("Uns_Dec")){
        	// 1111 in decimal string -> -1 in decimal long value
        	// need to add 2^n to correct to unsigned decimal, n = # of bits
        	//e.g. unsValue = (decimal of s) + 2^cellSize
        	long decValue = Convert.fromDecimalStringToLong(s, cellSize);
        	if ( decValue < 0 )
        		decValue += Math.pow(2, cellSize);
        	return decValue;
        }
        else if (base.getBase().equals("ASCII")){
        	int numBitsFromByte = cellSize%8;
        	return Convert.fromAsciiStringToLong(s, cellSize + numBitsFromByte);
        }
        else { // base is type Decimal
            return Long.parseLong(s);
        }
    }

    private String convertLong(Long l) {
        if (base.getBase().equals("Bin")) {
            return Convert.fromLongToTwosComplementString(l, cellSize);
        }
        else if (base.getBase().equals("Hex")) {
            return Convert.fromLongToHexadecimalString(l, cellSize);
        }
        else if (base.getBase().equals("Uns_Dec") ){
        	return Convert.fromLongToUnsignedDecString(l, cellSize);
        }
        else if(base.getBase().equals("ASCII")){
        	// first we need to add zeros to this value
        	//next we need the unsigned decimal equivalent of the value
        	// with this new unsigned decimal, we can convert to the
        	//			correct ASCII values
        	int numZerosAdded = 0;
        	if ( cellSize%8 != 0){
        		numZerosAdded = 8 - cellSize%8;
        	}
        	String x = Convert.fromLongToUnsignedDecString(l, cellSize);
        	long y = Long.parseLong(x);
        	//return "☐";
        	return Convert.fromLongToAsciiString(y, cellSize+ numZerosAdded);
        }
        else { //base is type Decimal
            return String.valueOf(l);
        }
    }

    /**
     * Add spaces after every 4 characters in String s if s is in binary or hex.
     * @param s the String into which spaces are to be put
     * @return the new string with the spaces added.
     */
    private String formatString(String s) {
        if (base.getBase().equals("Bin") || base.getBase().equals("Hex")) {
            return Convert.insertSpacesInString(s, 4);
        }
        else {
            return s;
        }
    }

    private String otherBasesToolTip() {
    	String ret = "";

    	if (base.getBase().equals(Base.HEX)) {
    		// 1
    		ret += "D: ";
    		base.setBase(Base.DECIMAL);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// 2
        	ret += " B: ";
        	base.setBase(Base.BINARY);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//3
        	ret += "U: ";
        	base.setBase(Base.UNSIGNED_DEC);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//4
        	ret += "A: ";
        	base.setBase(Base.ASCII);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// Set back to original
    		base.setBase(Base.HEX);
    	}
    	else if (base.getBase().equals(Base.BINARY)) {
    		// 1
    		ret += "D: ";
    		base.setBase(Base.DECIMAL);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// 2
        	ret += " H: ";
        	base.setBase(Base.HEX);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//3
        	ret += "U: ";
        	base.setBase(Base.UNSIGNED_DEC);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//4
        	ret += "A: ";
        	base.setBase(Base.ASCII);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// Set back to original
    		base.setBase(Base.BINARY);
    	}
    	else if (base.getBase().equals(Base.UNSIGNED_DEC)) {
    		// 1
    		ret += "D: ";
    		base.setBase(Base.DECIMAL);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// 2
        	ret += " H: ";
        	base.setBase(Base.HEX);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//3
        	ret += "U: ";
        	base.setBase(Base.BINARY);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//4
        	ret += "A: ";
        	base.setBase(Base.ASCII);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// Set back to original
    		base.setBase(Base.UNSIGNED_DEC);
    	}
    	else if (base.getBase().equals(Base.ASCII)) {
    		// 1
    		ret += "D: ";
    		base.setBase(Base.DECIMAL);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// 2
        	ret += " H: ";
        	base.setBase(Base.HEX);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//3
        	ret += "U: ";
        	base.setBase(Base.UNSIGNED_DEC);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//4
        	ret += "A: ";
        	base.setBase(Base.BINARY);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// Set back to original
    		base.setBase(Base.ASCII);
    	}
    	else { //decimal
    		// 1
    		ret += "B: ";
    		base.setBase(Base.BINARY);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// 2
        	ret += " H: ";
        	base.setBase(Base.HEX);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//3
        	ret += "U: ";
        	base.setBase(Base.UNSIGNED_DEC);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	//4
        	ret += "A: ";
        	base.setBase(Base.ASCII);
        	ret += formatString(convertLong(Long.parseLong(getString())));
        	// Set back to original
    		base.setBase(Base.DECIMAL);
    	}
    	return ret;
    }

    /**
     * Sets the proper tool tips and cell size for the table cell
     */
    private void setTooltipsAndCellSize() {
    	if( getTableRow() == null ) return;
    	
        Object o = getTableRow().getItem();
        if (o instanceof Register) {
            cellSize = ((Register) o).getWidth();
            tooltipStringProperty.set(otherBasesToolTip());
        }
        else if ((o instanceof RAMLocation) &&
                          getTableColumn().getText().equals("Data")) {
            RAMLocation ramLoc = (RAMLocation) o;
            cellSize = ramLoc.getRam().getCellSize();
            if (ramLoc.getSourceLine() != null) {
            	setTooltip(new Tooltip(ramLoc.getSourceLine().getLine()
                        + ": " + ((RAMLocation) o).getComment()));
            }
            else {
                setTooltip(null);
            }
        }
        else if ((o instanceof RAMLocation) &&
                          getTableColumn().getText().equals("Addr")) {
            cellSize = 31 - Integer.numberOfLeadingZeros(
                                             getTableView().getItems().size());
            // we don't care about tooltips for the address column
            // tooltipStringProperty.set(otherBasesToolTip());
        }
        else if (o instanceof EQU) {
        	cellSize = 32;
        	tooltipStringProperty.set(otherBasesToolTip());
        }
        else {
            cellSize = Integer.MAX_VALUE;
        }
    }

    /**
     * returns a string value of the item in the table cell
     * @return a string value of the item in the table cell
     */
    private String getString() {
        return getItem() == null ? "" : getItem().toString();
    }

    /*public void setOutlineRows(Set<Integer> rowSet){
        if (rowSet.contains(getIndex())){
            setStyle("-fx-border-color: forestgreen; -fx-border-width: 1px;");
        }
        else{
            setStyle("-fx-border-width: 0px;");
        }
    }*/
}
