package org.spruce.compiler.ast;

import java.util.Arrays;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTUnaryNode</code> is an <code>ASTParentNode</code> that has
 * exactly 1 child.
 */
public class ASTUnaryNode extends ASTParentNode {
    private final ASTNode myFirst;

    /**
     * Constructs an <code>ASTUnaryNode</code> at the given Location, with two
     * given child nodes.
     * @param location The <code>Location</code> marking the start of this node.
     * @param first The first child node.
     */
    public ASTUnaryNode(Location location, ASTNode first) {
        this(location, null, first);
    }

    /**
     * Constructs an <code>ASTUnaryNode</code> at the given Location, with the
     * given operation, with two given child nodes.
     * @param location The <code>Location</code> marking the start of this node.
     * @param operation The <code>TokenType</code> that represents an operation
     *      involving the children.
     * @param first The first child node.
     */
    public ASTUnaryNode(Location location, TokenType operation, ASTNode first) {
        super(location, Arrays.asList(first), operation);
        myFirst = first;
    }

    /**
     * Returns the first child.
     * @return The first child.
     */
    public ASTNode getFirst() {
        return myFirst;
    }

    /**
     * TODO: For removal when removing collapsing.
     */
    @Override
    public boolean isCollapsible() {
        return false;
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    public void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
        myFirst.print(prefix + (isTail ? "    " : "|   "), true);
    }
}
