package org.spruce.compiler.exception;

/**
 * A <code>CompileException</code> is thrown when an unrecoverable error occurs
 * while scanning or parsing the Spruce source code.  Compilation stops with this
 * error message.
 */
public class CompileException extends RuntimeException
{
    /**
     * Create a <code>CompileException</code>.
     */
    public CompileException()
    {
        super();
    }

    /**
     * Create a <code>CompileException</code> with the given message.
     * @param message The message.
     */
    public CompileException(String message)
    {
        super(message);
    }

    /**
     * Create a <code>CompileException</code>.
     * @param cause The cause.
     */
    public CompileException(Throwable cause)
    {
        super(cause);
    }

    /**
     * Create a <code>CompileException</code> with the given message.
     * @param message The message.
     * @param cause The cause.
     */
    public CompileException(String message, Throwable cause)
    {
        super(message, cause);
    }
}
