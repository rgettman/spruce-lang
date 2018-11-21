package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTExpressionName</code> is a node representing a simple name or
 * a qualified name.</p>
 *
 * <em>
 * ExpressionName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
 * </em>
 */
public class ASTExpressionName extends ASTParentNode
{
    /**
     * Constructs an <code>ASTExpressionName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     *
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTExpressionName(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     *
     * @return <code>false</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return false;
    }

    /**
     * Converts this to an <code>ASTTypeName</code>.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTPackageOrTypeName</code>.
     * @return An <code>ASTTypeName</code> with the same structure as this
     *     <code>ASTExpressionName</code>.
     * @see ASTAmbiguousName#convertToNamespaceOrTypeName
     */
    public ASTTypeName convertToTypeName()
    {
        List<ASTNode> children = getChildren();
        if (children.size() >= 1 && children.get(0) instanceof ASTAmbiguousName)
        {
            ASTAmbiguousName ambName = (ASTAmbiguousName) children.get(0);
            ASTNamespaceOrTypeName portName = ambName.convertToNamespaceOrTypeName();
            children.set(0, portName);
        }
        ASTTypeName typeName = new ASTTypeName(getLocation(), children);
        typeName.setOperation(getOperation());
        return typeName;
    }
}
