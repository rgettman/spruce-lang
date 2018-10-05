package org.spruce.compiler.ast;

import java.util.Arrays;
import java.util.List;

import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTDataType</code> is a data type (no array) or an array type.</p>
 *
 * <em>
 * DataType:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
 * </em>
 */
public class ASTDataType extends ASTParentNode
{
    /**
     * Constructs an <code>ASTDataType</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTDataType(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }

    /**
     * Converts this into an <code>ASTExpressionName</code>.
     * @return An <code>ASTExpressionName</code>.
     */
    public ASTExpressionName convertToExpressionName()
    {
        List<ASTNode> children = getChildren();
        ASTNode child = children.get(0);
        if (child instanceof ASTArrayType)
        {
            throw new CompileException("Expected variable.");
        }
        ASTDataTypeNoArray dtna = (ASTDataTypeNoArray) child;
        List<ASTNode> exprNameChildren = dtna.convertChildren();
        ASTExpressionName exprName = new ASTExpressionName(getLocation(), exprNameChildren);
        exprName.setOperation(getOperation());
        return exprName;
    }
}
