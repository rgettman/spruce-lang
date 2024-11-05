package org.spruce.compiler.ast.classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTMethodBody</code> is a block or a semicolon.</p>
 *
 * <em>
 * MethodBody:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;;
 * </em>
 */
public class ASTMethodBody extends ASTParentNode {
    private final ASTBlock myBlock;

    /**
     * Constructs an <code>ASTMethodBody</code> at the given <code>Location</code>
     * with the given <code>ASTBlock</code>.
     * @param location The <code>Location</code>.
     * @param block An <code>ASTBlock</code>.
     */
    public ASTMethodBody(Location location, ASTBlock block) {
        super(location);
        myBlock = block;
    }

    /**
     * Constructs an <code>ASTMethodBody</code> at the given <code>Location</code>
     * with no Block.
     * @param location The <code>Location</code>.
     */
    public ASTMethodBody(Location location) {
        super(location);
        myBlock = null;
    }

    /**
     * Returns an <code>ASTBlock</code>, if it exists.
     * @return An <code>Optional&lt;ASTBlock&gt;</code>.
     */
    public Optional<ASTBlock> getBlock() {
        return Optional.ofNullable(myBlock);
    }

    @Override
    public List<Node> getChildren() {
        List<Node> children = new ArrayList<>(1);
        if (myBlock != null) {
            children.add(myBlock);
        }
        return children;
    }

    /**
     * Helper method to create a string representation of this node.  It takes
     * into account where in the tree this node is.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     * @return The String representation of this node.
     */
    @Override
    public String toString(String prefix, boolean isTail) {
        StringBuilder buf = new StringBuilder();
        buf.append(prefix).append(isTail ? "└── " : "├── ").append(getHeaderValue()).append("\n");
        if (myBlock != null) {
            buf.append(myBlock.toString(prefix + (isTail ? "    " : "|   "), true)).append("\n");
        }
        return buf.toString();
    }
}
