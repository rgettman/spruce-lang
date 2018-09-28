package org.spruce.compiler.scanner;

/**
 * A <code>Location</code> describes where a <code>Token</code> can be found in
 * source code.  It consists of a filename, a line number (1-based), a
 * character position (1-based), and the line on which the token starts.
 */
public class Location
{
    private String myFilename;
    private int myLineNbr;
    private int myCharPos;
    private String myLine;

    /**
     * Constructs a <code>Location</code> based on the given attributes.
     * @param filename The filename.
     * @param zeroBasedLineNbr The 0-based line number (0 is top).
     * @param zeroBasedCharPos The 0-based character position (0 is far left).
     * @param line The line on which the token starts.
     */
    public Location(String filename, int zeroBasedLineNbr, int zeroBasedCharPos, String line)
    {
        myFilename = filename;
        myLineNbr = zeroBasedLineNbr + 1;
        myCharPos = zeroBasedCharPos + 1;
        myLine = line;
    }

    /**
     * Returns the filename.
     * @return The filename.
     */
    public String getFilename()
    {
        return myFilename;
    }

    /**
     * Returns the one-based line number (1 is top).
     * @return The one-based line number (1 is top).
     */
    public int getLineNbr()
    {
        return myLineNbr;
    }

    /**
     * Returns the one-based character position (1 is far left).
     * @return The one-based character position (1 is far left).
     */
    public int getCharPos()
    {
        return myCharPos;
    }

    /**
     * Returns the line on which the token starts.
     * @return The line on which the token starts.
     */
    public String getLine()
    {
        return myLine;
    }

    /**
     * Returns a string of the format "Location{filename:line, pos fromLeft, line "source_code_line"}".
     * @return A string representation of this <code>Location</code>.
     */
    @Override
    public String toString()
    {
        return "Location{" + myFilename + ":" + myLineNbr + ", pos " + myCharPos + ", line \"" + myLine + "\"}";
    }

    /**
     * Returns a string of the format "filename:lineNbr".
     * @return A string of the format "filename:lineNbr".
     */
    public String getFileAndLineNbr()
    {
        return myFilename + ":" + myLineNbr;
    }

    /**
     * Returns a string of <code>pos - 1</code> spaces followed by a caret
     * <code>^</code>.  This is used to point to the token in the line,
     * assuming it's printed in the line above.
     * <code>String test;</code>
     * <code>       ^</code>
     * @return A a string of <code>pos - 1</code> spaces followed by a caret
     *     <code>^</code>.
     */
    public String getPosIndicator()
    {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < myCharPos - 1; i++)
        {
            buf.append(' ');
        }
        buf.append('^');
        return buf.toString();
    }
}
