package org.spruce.compiler.ast;

import java.util.Arrays;

import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTBinaryNode</code> is an <code>ASTParentNode</code> that has
 * exactly 2 children.
 */
public class ASTBinaryNode extends ASTParentNode {
    private final ASTNode myFirst;
    private final ASTNode mySecond;

    /**
     * Constructs an <code>ASTBinaryNode</code> at the given Location, with two
     * given child nodes.
     * @param location The <code>Location</code> marking the start of this node.
     * @param first The first child node.
     * @param second The second child node.
     */
    public ASTBinaryNode(Location location, ASTNode first, ASTNode second) {
        this(location, null, first, second);
    }

    /**
     * Constructs an <code>ASTBinaryNode</code> at the given Location, with the
     * given operation, with two given child nodes.
     * @param location The <code>Location</code> marking the start of this node.
     * @param operation The <code>TokenType</code> that represents an operation
     *      involving the children.
     * @param first The first child node.
     * @param second The second child node.
     */
    public ASTBinaryNode(Location location, TokenType operation, ASTNode first, ASTNode second) {
        super(location, Arrays.asList(first, second), operation);
        myFirst = first;
        mySecond = second;
    }

    /**
     * Returns the first child.
     * @return The first child.
     */
    public ASTNode getFirst() {
        return myFirst;
    }

    /**
     * Returns the second child.
     * @return The second child.
     */
    public ASTNode getSecond() {
        return mySecond;
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
        myFirst.print(prefix + (isTail ? "    " : "|   "), false);
        mySecond.print(prefix + (isTail ? "    " : "│   "), true);
    }
}
