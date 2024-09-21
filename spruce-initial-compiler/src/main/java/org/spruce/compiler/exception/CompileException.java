package org.spruce.compiler.exception;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.parser.ClassesParser;
import org.spruce.compiler.parser.ExpressionsParser;
import org.spruce.compiler.scanner.Location;

/**
 * A <code>CompileException</code> is thrown when an unrecoverable error occurs
 * while scanning or parsing the Spruce source code.  Compilation stops with this
 * error message.
 */
public class CompileException extends RuntimeException {
    private List<ASTNode> myAlreadyParsed;
    private final Location myLocation;

    /**
     * Create a <code>CompileException</code> with the given message.
     * @param message The message.
     */
    public CompileException(Location location, String message) {
        super(message);
        myLocation = location;
    }

    /**
     * Create a <code>CompileException</code> with the given message and
     * already parsed ASTTypeArguments.
     * @param message The message.
     * @param alreadyParsed Already parsed <code>ASTNodes</code>.
     */
    public CompileException(Location location, String message, List<ASTNode> alreadyParsed) {
        super(message);
        myLocation = location;
        myAlreadyParsed = alreadyParsed;
    }

    /**
     * Only exists due to a parsing difficulty regarding a qualified
     * constructor invocation: ExpressionName|Primary . TypeArguments super ( [ArgumentList] ).
     * Normally parsing a primary would throw a CompileException on "super", an
     * invalid method name.  In this case the ClassesParser must back up and produce
     * and ExpressionName/Primary so the production for a constructor invocation can
     * proceed.  However, at this point, TypeArguments have already been
     * parsed.  Store them here so parseConstructorInvocation can use them.
     * @return A List of already parsed ASTNodes.
     * @see ExpressionsParser#parseMethodInvocation(org.spruce.compiler.ast.names.ASTExpressionName).
     * @see ExpressionsParser#parseMethodInvocation(org.spruce.compiler.ast.expressions.ASTPrimary)
     * @see ClassesParser#parseConstructorInvocation
     */
    public List<ASTNode> getAlreadyParsed() {
        return myAlreadyParsed;
    }

    /**
     * Returns the <code>Location</code> in the source code that caused this
     * <code>CompileException</code>.
     * @return A <code>Location</code>.
     */
    public Location getLocation() {
        return myLocation;
    }

    /**
     * Inserts the <code>Location</code> into the error message string.
     * @return The String representation of this exception object.
     */
    @Override
    public String getMessage() {
        return System.lineSeparator() + String.join(System.lineSeparator(), getLocation().where(), super.getMessage());
    }
}
