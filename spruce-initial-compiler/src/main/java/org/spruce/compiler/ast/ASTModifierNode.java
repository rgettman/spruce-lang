package org.spruce.compiler.ast;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTModifierNode</code> is a leaf <code>ASTNode</code> with an operation
 * that is a modifier.
 */
public class ASTModifierNode extends ASTNode {
    private final TokenType myOperation;

    /**
     * Constructs an <code>ASTModifierNode</code> with the given <code>Location</code>
     * and the string value from the <code>Token</code>.
     * @param location The <code>Location</code>.
     * @param operation The operation representing the modifier.
     */
    public ASTModifierNode(Location location, TokenType operation) {
        super(location);
        myOperation = operation;
    }

    /**
     * Returns the operation.
     * @return The operation, a <code>TokenType</code>.
     */
    public TokenType getOperation() {
        return myOperation;
    }

    /**
     * Prints this node to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    protected void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
    }

    /**
     * Returns a string of the format "ClassSimpleName(operation) at Location".
     * @return A string representation of this node.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + getOperation() + ") at " + getLocation();
    }
}