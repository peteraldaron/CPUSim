/**
 * File: CommandLineChannel
 * Last update: December 2013
 * Modified by: Peter Zhang, Stephen Jenkins, Brendan Tschaen
 * Methods added: readStirng, writeString 
 * Methods removed:reset, all read and write methods from the IOChannel
 */
package cpusim;

import java.util.Scanner;

/**
 * This class implements IOChannel using the terminal/command line.  It is
 * used when CPUSim is run in non-GUI mode.
 */
public class CommandLineChannel implements StringChannel {

    Scanner scanner = new Scanner(System.in);

    /**
     * Constructor for CommandLineChannel. There is only
     * one CommandLineChannel channel that is ever used.
     */
    public CommandLineChannel() {
    }

    /**
     * Gives a string representation of the object.
     * In this case, its name field.
     */
    public String toString() {
        return "Command Line Channel";
    }

    /**
     * write the given string to the commandline
     * @param String to be output
     */
    public void writeString(String s){
        System.out.println(s);
    }
    
    /**
     * get the input from the user
     * @return String rep of the input
     *
     */
    public String readString(){
        return scanner.nextLine();
    }

}
