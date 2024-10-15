package org.spruce.compiler.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.ast.classes.ASTGeneralModifier;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTListNode</code> is an <code>ASTNode</code> that has a variable
 * number of children and a list type.
 */
public class ASTListNode extends ASTNode {
    /**
     * Types of list nodes.
     */
    public enum Type {
        AMBIGUOUS_NAME_IDS,
        ANNOTATION_PARTS,
        ARGUMENTS,
        BLOCK_STATEMENTS,
        CASE_CONSTANTS,
        CATCH_CLAUSES,
        CLASS_PARTS,
        DATA_TYPES,
        DATA_TYPES_NO_ARRAY,
        DIM_EXPRS,
        ELEMENT_VALUE_PAIRS,
        ELEMENT_VALUES,
        ENUM_CONSTANTS,
        EXPR_NAME_IDS,
        FORMAL_PARAMETERS,
        GENERAL_MODIFIERS,
        IDENTIFIERS,
        INFERRED_PARAMETERS,
        INTERFACE_PARTS,
        INTERSECTION_TYPES,
        NAMESPACE_IDS,
        NAMESPACE_OR_TYPENAME_IDS,
        PATTERNS,
        RESOURCES,
        SIMPLE_TYPES,
        STMT_EXPRS,
        SWITCH_EXPR_RULES,
        SWITCH_STMT_RULES,
        TYPE_DECLARATIONS,
        TYPENAME_IDS,
        TYPE_ARGUMENTS,
        TYPE_PARAMETERS,
        USE_DECLARATIONS,
        VARIABLE_DECLARATORS,
        VARIABLE_INITIALIZERS,
        VARIABLE_MODIFIERS,
        VARIANTS
    }

    private final List<ASTNode> myChildren;
    private final Type myType;

    /**
     * Constructs an <code>ASTParentNode</code> with a <code>Location</code>, a
     * list of child nodes, and an operation.
     * @param location The <code>Location</code>.
     * @param children The list of child nodes.
     * @param type The <code>ASTListNode.Type</code> that represents the type
     *     of list.
     */
    public ASTListNode(Location location, List<ASTNode> children, Type type) {
        super(location);
        myChildren = children;
        myType = type;
    }

    /**
     * Returns the list of child nodes.
     * @return A <code>List</code> of <code>ASTNodes</code>.
     */
    public List<ASTNode> getChildren() {
        return myChildren;
    }

    /**
     * Returns the type of list.
     * @return The <code>ASTListNode.Type</code> representing the list type.
     */
    public Type getType() {
        return myType;
    }

    /**
     * Prints this node and its children to the output stream.
     * @param prefix A string to indent the printing of this node.
     * @param isTail Whether this node is last in its siblings (or the only child).
     */
    @Override
    public void print(String prefix, boolean isTail) {
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
        Type type = getType();
        return getClass().getSimpleName() + (type != null ? ("(" + type + ")") : "")
                + " at " + getLocation();
    }

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
    public <T extends ASTListNode> T convertDescendant(List<Class<? extends ASTNode>> childClassList,
                                                       BiFunction<Location, List<ASTNode>, T> nodeSupplier,
                                                       String errorMsg) {
        ASTListNode current = this;
        List<ASTNode> children = getChildren();
        while (children.size() == 1 && current.getType() == null) {
            ASTNode child = children.get(0);
            if (childClassList.contains(child.getClass())) {
                return nodeSupplier.apply(current.getLocation(), Collections.singletonList(child));
            }
            else if (child instanceof ASTListNode) {
                // Look at next down.
                current = (ASTListNode) children.get(0);
                children = current.getChildren();
            }
            else {
                throw new CompileException(child.getLocation(), errorMsg);
            }
        }
        throw new CompileException(current.getLocation(), errorMsg);
    }

    /**
     * Converts this general modifier list to a more specific modifier list,
     * giving an error if we have a modifier that is not in a more specific
     * list, or if there are duplicate modifiers.
     * @param errorMessage The error message expected.
     * @param expectedModifiers A List of expected modifiers (token types).
     * @param nodeSupplier A BiFunction that constructs a specific node type
     *     given a Location and a list of child nodes.
     * @param <T> The specific node type to create.
     * @return The newly created specific modifier list.
     * @throws CompileException If there is a general modifier not in the more
     *     specific list, or if there are duplicate modifiers.
     */
    public <T extends ASTParentNode> T convertToSpecificList(String errorMessage, List<TokenType> expectedModifiers,
                                                             BiFunction<Location, List<ASTNode>, T> nodeSupplier) {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTNode> children = getChildren();
        for (ASTNode child : children) {
            ASTGeneralModifier mod = (ASTGeneralModifier) child;
            TokenType modifier = mod.getOperation();
            if (!seen.add(modifier)) {
                throw new CompileException(mod.getLocation(), "Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (!expectedModifiers.contains(modifier)) {
                throw new CompileException(mod.getLocation(), errorMessage);
            }
        }
        return nodeSupplier.apply(getLocation(), children);
    }
}
