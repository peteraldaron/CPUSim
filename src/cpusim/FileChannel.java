/**
 * File: FileChannel
 * Author: Dale Skrien
 * Last update: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Method added: clearIOChannelBuffer
 * 
 * Changed PushbackReader to have a buffer size of the current file size 
 */
package cpusim;

import cpusim.util.*;

import java.io.*;
import java.util.Stack;

/**
 * This file contains the class that manages an IO channel to/from a file.
 * It maintains PushBackReaders and FileWriters for each file
 * and maintains the data to and from the user.
 */
public class FileChannel implements IOChannel  {
    // Where to get or send the data
    private File file;            
    // Reader for the file for input
    private PushbackReader reader;     
    // Writer for the file for output
    private FileWriter writer;      
    // Stack to hold the strings read by the reader so they can be unread
    private Stack<String> unreadStack;
    
    /**
     * Creates a new File Channel. Note that this file channel
     * constructor should probably not be used. The file channel
     * that is used is in the CPUSimConstants file, it is the only
     * one that is used.
     * 
     * @param file - The file for the channel to read from or write to.
     */
    public FileChannel(File file) {
        this.file = file;
        this.reader = null;
        this.writer = null;
        
        unreadStack = new Stack<String>()	;
    }

    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(int numBits) {
        try {
            if (reader == null) {
                reader = new PushbackReader(new FileReader(file), 
                						    ((int) file.length()) );
            }
            // Read past any white space and
            // read the first non-white space-- if not a digit or + or -,
            // throw error
            int c = reader.read();
            while (c != -1 && Character.isWhitespace((char) c)) {
                c = reader.read();
            }
            String s = "";
            if (c == '+' || c == '-') {
                s += (char) c;
                c = reader.read();
            }
            if (c == -1 || !Character.isDigit((char) c)) {
                throw new ExecutionException("Attempted to read an integer" +
                        " from file " + file.getName() + " but found " +
                        (c == -1 ? "the end of file" : "" + (char) c) + ".");
            }
            // Loop while reading digits, appending them to the string
            while (c != -1 && Character.isDigit((char) c)) {
                s += (char) c;
                c = reader.read();
            }
            // Push back the last character read
            reader.unread(c);
            // Make sure input is valid
            long value = Convert.fromAnyBaseStringToLong(s);
            Convert.checkFitsInBits(value, numBits);
            // Push the string onto the unreadStack so it can be unread later
            unreadStack.push(s);
            return value;
        } catch (NumberFormatException e) {
            throw new ExecutionException(e.getMessage());
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii() {
        try {
            if (reader == null) {
                reader = new PushbackReader(new FileReader(file), 
						   				    ((int) file.length()) ); 
            }
            int c = reader.read();
            if (c > 255 || c < 0) {
                throw new ExecutionException("Attempted to read an ASCII" +
                        "character from file " + file.getName() + "\n but the next " +
                        "character was " +
                        (c == -1 ? "the end of file" : "" + (char) c) + ".");
            }
            // Push the character onto the unreadStack so it can be unread
            unreadStack.push(""+((char) c));
            return (char) c;
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode() {
        try {
            if (reader == null) {
                reader = new PushbackReader(new FileReader(file), 
						   				    ((int) file.length()) );   
            }
            int c = reader.read();
            if (c < 0) {
                throw new ExecutionException("Attempted to read a Unicode " +
                        "character from file " + file.getName() + "\n but the " +
                        " end of the file was reached.");
            }
            // Push the character onto the unreadStack so it can be unread
            unreadStack.push(""+((char) c));
            return (char) c;
        } catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to read from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to read " +
                    "from file " + file.getName() + ".");
        }
    }

    /**
     * writes the given long value to the output, preceded by a space
     *
     * @param value the long value to be output
     */
    public void writeLong(long value) {
        try {
            if (writer == null) {
                writer = new FileWriter(file);
            }
            // Start it with a space char
            String longString = " " + value;  
            writer.write(longString, 0, longString.length());
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * writes the given long value to the output as an ASCII value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(long longValue) {
        if (longValue > 255 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as an ASCII value.");
        try {
            if (writer == null) {
                writer = new FileWriter(file);
            }
            writer.write((int) longValue);
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(long longValue) {
        if (longValue > 65535 || longValue < 0)
            throw new ExecutionException("Attempt to output the value " +
                    longValue + " as a Unicode value.");
        try {
            if (writer == null) {
                writer = new FileWriter(file);
            }
            writer.write((int) longValue);
            writer.flush();
        } catch (IOException ioe) {
            String message = "CPUSim was unable to write " +
                    "to file: " + file.getName();
            if (ioe.getMessage() != null) {
                message += " because: " + ioe.getMessage();
            }
            throw new ExecutionException(message);
        }
    }
    
    /**
     * Pushes back the last thing read from the input file back into
     * the pushbackReader to be read again.
     */
    public void unread() {
    	try {
            if (reader == null) {
                reader = new PushbackReader(new FileReader(file), 
						   				    ((int) file.length()) ); 
            }
            if( !unreadStack.isEmpty() ) {
            	String s = " " + unreadStack.pop();
            	reader.unread(s.toCharArray());
            	System.out.println(s);
            }
        	
    	} catch (FileNotFoundException fne) {
            throw new ExecutionException("Attempted to unread from file " +
                    file.getName() + " but it could not be found.");
        } catch (IOException ioe) {
            throw new ExecutionException("CPUSim was unable to unread " +
                    "from file " + file.getName() + ".");
        }
    }

    
    public void unwriteLong() {
    	
    }
    
    public void unwriteAscii() {
    	
    }
    
    public void unwriteUnicode() {
    	
    }
    
    /**
     * Reset the file channel.
     */
    public void reset() {
        // Close the file if it is open.
        try {
            if (reader != null) {
                reader.close();
                reader = null;
            }
            if (writer != null) {
                writer.close();  // This flushes it first
                writer = null;
            }
        } catch (IOException ioe) {
            System.out.println("IOException occurred when attempting to " +
                "reset the file: " + file + ".");
        }
    }

    /**
     * Gives the file of the fileChannel.
     * @return - the file of the fileChannel.
     */
    public File getFile() {
        return file;
    }

    /**
     * Returns a string representation of the object.
     */
    public String toString() {
        return file.toString();
    }
    
    /**
     * getID is the same as toString(). It is included
     * only for backwards compatibility.
     * @return a String representation of the object.
     */
    public String getID() {
        return file.toString();
    }
    /**
     * File channel does not have io buffers.
     * don't do anything.
     */
    public void clearIOChannelBuffer(){
        
    }
    
    /**
     * Does not use this method. 
     * added here for inheritance compatibility
     */
    public void output(String s){}
    
    /**
     * Does not use this method. 
     * added here for inheritance compatibility
     */
    public String getInput(){
        //return the empty string
        return "";
    }
}
