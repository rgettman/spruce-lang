package org.spruce.compiler.ast;

import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTParentNode</code> is an <code>ASTNode</code> that has children.
 */
public abstract class ASTParentNode extends ASTNode {
    private final List<ASTNode> myChildren;
    private TokenType myOperation;

    /**
     * Constructs an <code>ASTParentNode</code> with a <code>Location</code>
     * and a list of child nodes.
     * @param location The <code>Location</code>.
     * @param children The list of child nodes.
     */
    public ASTParentNode(Location location, List<ASTNode> children) {
        this(location, children, null);
    }

    /**
     * Constructs an <code>ASTParentNode</code> with a <code>Location</code>, a
     * list of child nodes, and an operation.
     * @param location The <code>Location</code>.
     * @param children The list of child nodes.
     * @param operation The <code>TokenType</code> that represents an operation
     *     involving the children.
     */
    public ASTParentNode(Location location, List<ASTNode> children, TokenType operation) {
        super(location);
        myChildren = children;
        myOperation = operation;
    }

    /**
     * Returns the list of child nodes.
     * @return A <code>List</code> of <code>ASTNodes</code>.
     */
    public List<ASTNode> getChildren() {
        return myChildren;
    }

    /**
     * Returns the name representing the operation between the children, or
     * <code>null</code> if there is no such operation.
     * @return The <code>TokenType</code> representing the operation name.
     */
    public TokenType getOperation() {
        return myOperation;
    }

    /**
     * Sets the name representing the operation between the children, or
     * <code>null</code> if there is no such operation.
     * @param operation The <code>TokenType</code> representing the operation name.
     */
    public void setOperation(TokenType operation) {
        myOperation = operation;
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    protected void print(String prefix, boolean isTail) {
        System.out.println(prefix + (isTail ? "└── " : "├── ") + toString());
        for (int i = 0; i < myChildren.size() - 1; i++) {
            myChildren.get(i).print(prefix + (isTail ? "    " : "|   "), false);
        }
        if (!myChildren.isEmpty()) {
            myChildren.get(myChildren.size() - 1).print(prefix + (isTail ? "    " : "│   "), true);
        }
    }

    /**
     * Returns a string of the format "ClassSimpleName at Location" or
     * "ClassSimpleName(operation) at Location".
     * @return A string representation of this node.
     */
    @Override
    public String toString() {
        TokenType operation = getOperation();
        return getClass().getSimpleName() + (operation != null ? ("(" + operation.getRepresentation() + ")") : "")
                + " at " + getLocation();
    }

    /**
     * Sometimes a parent node may have only one child, and each child in turn
     * has only one child.  Collapse until we reach a child that is either not
     * a parent node, has more than one child, or refuses to collapse further.
     */
    public void collapse() {
        List<ASTNode> children = getChildren();
        if (isCollapsible()) {
            // Collapses children recursively.
            for (int i = 0; i < children.size(); i++) {
                ASTNode child = children.get(i);
                ASTNode descendant = findNonCollapsibleDescendant(child);
                if (descendant != child) {
                    children.set(i, descendant);
                }
                if (descendant instanceof ASTParentNode) {
                    ((ASTParentNode) descendant).collapse();
                }
            }
        }
    }

    /**
     * Collapses then prints this node.
     */
    public void collapseThenPrint() {
        collapse();
        print();
    }

    /**
     * Finds the non-collapsible descendant node.
     * @param child Starting from the given <code>ASTNode</code>.
     * @return Returns the non-collapsible descendant node.  Could be
     *     <code>child</code> if it's not collapsible.
     */
    private ASTNode findNonCollapsibleDescendant(ASTNode child) {
        if (child instanceof ASTParentNode childAsParent) {
            List<ASTNode> descendants = childAsParent.getChildren();
            if (childAsParent.isCollapsible() && descendants.size() == 1 && childAsParent.getOperation() == null) {
                return childAsParent.findNonCollapsibleDescendant(descendants.get(0));
            }
            else {
                return childAsParent;
            }
        }
        return child;
    }

    /**
     * Returns whether this node is collapsible.
     * @return Whether this node is collapsible.
     */
    public abstract boolean isCollapsible();

    /**
     * Finds a descendant that is an instance of one of the given classes, and
     * make it a child of a new node to be created, specified by the given
     * node supplier.
     * @param childClassList A <code>List</code> of acceptable descendant classes.
     * @param nodeSupplier A <code>BiFunction</code> that takes a <code>Location</code>
     *     and a <code>List</code> of child nodes and returns the desired node.
     * @param errorMsg The error message of a <code>CompileException</code> if
     *     no suitable descendant is found.
     * @return The desired node with the desired descendant as its child.
     * @throws CompileException If there is no suitable descendant.
     */
    public <T extends ASTParentNode> T convertDescendant(List<Class<? extends ASTNode>> childClassList,
                                                         BiFunction<Location, List<ASTNode>, T> nodeSupplier,
                                                         String errorMsg) {
        ASTParentNode current = this;
        List<ASTNode> children = getChildren();
        while (children.size() == 1 && current.getOperation() == null) {
            ASTNode child = children.get(0);
            if (childClassList.contains(child.getClass())) {
                return nodeSupplier.apply(current.getLocation(), Collections.singletonList(child));
            }
            else if (child instanceof ASTParentNode) {
                // Look at next down.
                current = (ASTParentNode) children.get(0);
                children = current.getChildren();
            }
            else {
                throw new CompileException(errorMsg);
            }
        }
        throw new CompileException(errorMsg);
    }
}
