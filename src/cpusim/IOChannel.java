/**
 * File: IOChannel
 * LastUpdate: November 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: clearIOChannelBuffer
 */

package cpusim;

import cpusim.util.CPUSimConstants;

/**
 * Interface that any channels we want to use for input or output
 * must implement.
 */
public interface IOChannel extends CPUSimConstants {
    
    /**
     * returns the next integer from input as a long that fits in the given
     * number of bits.  If it doesn't fit, a NumberFormatException is thrown.
     *
     * @param numBits the number of bits into which the long must fit
     * @return the long value that was input
     * @throws ExecutionException if it cannot read a long.
     */
    public long readLong(int numBits);

    /**
     * returns the next ASCII char from input.
     *
     * @return the ASCII character read
     * @throws ExecutionException if it cannot read an ASCII char.
     */
    public char readAscii();

    /**
     * returns the next Unicode char from input.
     *
     * @return the Unicode character read
     * @throws ExecutionException if it cannot read an Unicode char.
     */
    public char readUnicode();

    /**
     * writes the given long value to the output
     *
     * @param value the long value to be output
     */
    public void writeLong(long value);

    /**
     * writes the given long value to the output as an ASCII value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an ASCII char
     */
    public void writeAscii(long longValue);

    /**
     * writes the given long value to the output as a Unicode value
     *
     * @param longValue the long value to be output
     * @throws ExecutionException if the long is not an Unicode char
     */
    public void writeUnicode(long longValue);


    /** reset the input and output */
    public void reset();
    
    /**
     * clear the iochannel input/output buffer if the implemented class
     * used a buffer
     */
    public void clearIOChannelBuffer();
   
}
