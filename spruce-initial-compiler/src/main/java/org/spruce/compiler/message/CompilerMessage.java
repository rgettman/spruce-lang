package org.spruce.compiler.message;

import org.spruce.compiler.scanner.Location;

/**
 * A <code>CompilerMessage</code> is something incorrect or important that
 * needs to be communicated back to the user.
 */
public class CompilerMessage {
    /**
     * The severity of the message.
     */
    public enum Level {
        /**
         * A compiler error has occurred.  The compiler phase in which the
         * error occurred may continue, but no further phases will run.  The
         * message will be communicated back to the user.
         */
        ERROR,
        /**
         * A compiler warning has occurred.  Compiling will continue but the
         * message will still be communicated back to the user.
         */
        WARNING,
        /**
         * A note to the user that doesn't represent something to be fixed.  It
         * could be a suggestion to eliminate another message representing an
         * Error or Warning.
         */
        NOTE
    }
    private final Location myLocation;
    private final Level myLevel;
    private final String myMessage;

    /**
     * Constructs a <code>CompilerMessage</code> at the given <code>Location</code>
     * with the given <code>Level</code> and the given text as the message.
     * @param location The <code>Location</code>.
     * @param level The <code>Level</code>.
     * @param message The test message to be presented to the user.
     */
    public CompilerMessage(Location location, Level level, String message) {
        myLocation = location;
        myLevel = level;
        myMessage = message;
    }

    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    public Location getLocation() {
        return myLocation;
    }

    /**
     * Returns the <code>Level</code>.
     * @return The <code>Level</code>.
     */
    public Level getLevel() {
        return myLevel;
    }

    /**
     * Returns the compiler message.
     * @return The compiler message.
     */
    public String getMessage() {
        return myMessage;
    }

    /**
     * Returns formatted text presentable to the user.
     * @return Formatted text presentable to the user.
     */
    public String toString() {
        Location loc = getLocation();
        return String.join(System.lineSeparator(), getLevel() + " at: " + loc.getFileLinePos(),
                loc.where(), getMessage());
    }
}
