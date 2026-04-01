package org.spruce.compiler.bootstrap.ast.types;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.common.Location;

/**
 * <p>An <code>ASTDataTypeNoArray</code> is a simple or fully qualified
 * type.</p>
 *
 * <em>
 * DataTypeNoArray:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
 * </em>
 */
public final class ASTDataTypeNoArray extends ASTListNode<ASTSimpleType> implements ASTBaseDataType {
    /**
     * Constructs an <code>ASTDataTypeNoArray</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTSimpleType</code>s.
     */
    public ASTDataTypeNoArray(Location location, List<ASTSimpleType> children) {
        super(location, children, Type.SIMPLE_TYPES);
    }

    /**
     * Returns a normalized name with each simple type separated by a dot,
     * without any spaces.
     * @return The normalized name.
     */
    @Override
    public String getTypeName() {
        return getTypedChildren().stream()
                .map(simpleType -> simpleType.getName().getValue())
                .collect(Collectors.joining("."));
    }

    /**
     * Returns whether this DataTypeNoArray can be converted to an ExpressionName.
     * Must not have any SimpleType children with type arguments.
     * @return Whether this DataTypeNoArray can be converted to an ExpressionName.
     */
    @Override
    public boolean canConvertToExpressionName() {
        return true;
    }

    /**
     * Converts to an Expression Name.
     * @return An <code>ASTExpressionName</code>.
     */
    @Override
    public ASTExpressionName convertToExpressionName() {
        List<ASTIdentifier> exprNameChildren = convertChildren();
        return new ASTExpressionName(getLocation(), exprNameChildren);
    }

    /**
     * Converts the children from (DTNA, SimpleType) to (AmbiguousName, Identifier)
     * or (SimpleType) to (Identifier).
     * @return A <code>List</code> of <code>ASTIdentifier</code> nodes suitable for an
     *     Ambiguous Name or an Expression Name.
     */
    private List<ASTIdentifier> convertChildren() {
        List<ASTSimpleType> children = getTypedChildren();
        List<ASTIdentifier> convertedChildren = new ArrayList<>(children.size());
        for (ASTSimpleType st : children) {
            convertedChildren.add(st.getName());
        }
        return convertedChildren;
    }
}
