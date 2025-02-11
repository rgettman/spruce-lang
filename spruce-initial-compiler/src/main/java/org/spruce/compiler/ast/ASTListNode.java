package org.spruce.compiler.ast;

import java.util.List;

import org.spruce.compiler.common.Location;

/**
 * An <code>ASTListNode</code> is an <code>ASTParentNode</code> that has a variable
 * number of children and a list type.
 * @param <T> The type of child node.
 */
public class ASTListNode<T extends Node> extends ASTParentNode {
    /**
     * Types of list nodes.
     */
    public enum Type {
        ANNOTATION_PARTS, ANNOTATIONS, ARGUMENTS,
        BLOCK_STMTS,
        CASE_CONSTANTS, CATCH_CLAUSES, CLASS_MODIFIERS, CLASS_PARTS, CONSTANT_MODIFIERS,
        DATA_TYPES, DATA_TYPES_NO_ARRAY, DIMS, DIM_EXPRS,
        ELEMENT_VALUE_PAIRS, ELEMENT_VALUES, ENUM_CONSTANTS, EXPR_NAME_IDS,
        FIELD_MODIFIERS, FOR_EXPR_HEADERS, FORMAL_PARAMETERS,
        GENERAL_MODIFIERS,
        IDENTIFIERS, INFERRED_PARAMETERS, INTERFACE_METHOD_MODIFIERS, INTERFACE_MODIFIERS, INTERFACE_PARTS, INTERSECTION_TYPES,
        METHOD_MODIFIERS,
        NAMESPACE_IDS, NAMESPACE_OR_TYPENAME_IDS,
        PATTERNS,
        RECORD_COMPONENTS, RESOURCES,
        SIMPLE_TYPES, STMT_EXPRS, SWITCH_EXPR_RULES, SWITCH_STMT_RULES,
        TYPE_DECLARATIONS, TYPENAME_IDS, TYPE_ARGUMENTS, TYPE_PARAMETERS,
        USE_DECLARATIONS,
        VARIABLE_DECLARATORS, VARIABLE_MODIFIERS, VARIANTS
    }

    private final List<T> myChildren;
    private final Type myType;

    /**
     * Constructs an <code>ASTListNode</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     * @param location The <code>Location</code>.
     * @param children The list of child nodes.
     * @param type The <code>ASTListNode.Type</code> that represents the type
     *     of list.
     */
    public ASTListNode(Location location, List<T> children, Type type) {
        super(location);
        myChildren = children;
        myType = type;
    }

    /**
     * Returns the list of child nodes.
     * @return A <code>List</code> of <code>Node</code>s.
     */
    @Override
    public List<Node> getChildren() {
        return myChildren.stream()
                .map(n -> (Node) n)
                .toList();
    }

    /**
     * Returns the list of child nodes.
     * @return A <code>List</code> of nodes of type <code>T</code>.
     */
    public List<T> getTypedChildren() {
        return myChildren;
    }

    /**
     * Returns the element in the list specified by the given 0-based index.
     * @param index A zero-based index.
     * @return The specified element from the list.
     */
    public T get(int index) {
        return myChildren.get(index);
    }

    /**
     * Returns the type of list.
     * @return The <code>ASTListNode.Type</code> representing the list type.
     */
    public Type getType() {
        return myType;
    }

    /**
     * Returns this list's type as a string.
     * @return A header value for this node.
     */
    @Override
    public String getHeaderValue() {
        return myType.toString();
    }
}
