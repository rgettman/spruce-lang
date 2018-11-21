package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeAllDeclaration</code> is "recognize" followed
 * by a Package Or Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * RecognizeAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;recognize PackageOrTypeName . * ;
 * </em>
 */
public class ASTRecognizeAllDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeAllDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeAllDeclaration(Location location, List<ASTNode> children)
    {
        super(location, children);
    }

    /**
     * This node is collapsible.
     * @return <code>true</code>.
     */
    @Override
    public boolean isCollapsible()
    {
        return true;
    }
}

