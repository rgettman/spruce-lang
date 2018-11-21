package org.spruce.compiler.ast.names;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAmbiguousName</code> is a node representing part of a
 * qualified name that could be an expression name, a package name, or a type
 * name.</p>
 *
 * <em>
 * AmbiguousName:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AmbiguousName . Identifier<br>
 * </em>
 */
public class ASTAmbiguousName extends ASTParentNode
{
    /**
     * Constructs an <code>ASTAmbiguousName</code> at the given <code>Location</code>
     * and with at least one node as its children.
     * @param location The <code>Location</code>.
     * @param children The child nodes.
     */
    public ASTAmbiguousName(Location location, List<ASTNode> children)
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
     * Converts this to an <code>ASTNamespaceOrTypeName</code>.  Converts any child
     * <code>ASTAmbiguousName</code> to an <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>ASTNamespaceOrTypeName</code> with the same structure as this
     *     <code>ASTAmbiguousName</code>.
     * @see ASTExpressionName#convertToTypeName
     */
    public ASTNamespaceOrTypeName convertToNamespaceOrTypeName()
    {
        List<ASTNode> children = getChildren();
        if (children.size() >= 1 && children.get(0) instanceof ASTAmbiguousName)
        {
            ASTAmbiguousName ambName = (ASTAmbiguousName) children.get(0);
            ASTNamespaceOrTypeName portName = ambName.convertToNamespaceOrTypeName();
            children.set(0, portName);
        }
        ASTNamespaceOrTypeName portName = new ASTNamespaceOrTypeName(getLocation(), children);
        portName.setOperation(getOperation());
        return portName;
    }
}
