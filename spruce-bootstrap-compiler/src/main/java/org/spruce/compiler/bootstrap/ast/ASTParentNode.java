package org.spruce.compiler.bootstrap.ast;

import java.util.List;

import org.spruce.compiler.bootstrap.common.Location;

/**
 * An <code>ASTParentNode</code> is an <code>ASTNode</code> that has children.
 */
public abstract class ASTParentNode extends ASTNode implements ParentNode {
    /**
     * Constructs an <code>ASTParentNode</code> with a <code>Location</code>
     * and a list of child nodes.
     * @param location The <code>Location</code>.
     */
    public ASTParentNode(Location location) {
        super(location);
    }

    /**
     * Returns the list of child nodes.
     * @return A <code>List</code> of <code>Node</code>s.
     */
    public abstract List<Node> getChildren();

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
        List<Node> children = getChildren();
        buf.append(super.toString(prefix, isTail));
        for (int i = 0; i < children.size(); i++) {
            buf.append(children.get(i).toString(prefix + (isTail ? "    " : "|   "), (i == children.size() - 1)));
        }
        return buf.toString();
    }

    /**
     * The header value is "Parent".
     * @return A header value for this node.
     */
    public String getHeaderValue() {
        return "Parent";
    }
}
