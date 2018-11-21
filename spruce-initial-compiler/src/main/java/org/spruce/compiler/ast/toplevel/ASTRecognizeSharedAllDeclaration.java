package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeSharedAllDeclaration</code> is "recognize" followed
 * by "shared" followed by a Type Name, dot, star, then a semicolon.</p>
 *
 * <em>
 * RecognizeSharedAllDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;recognize shared TypeName . * ;
 * </em>
 */
public class ASTRecognizeSharedAllDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeSharedAllDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeSharedAllDeclaration(Location location, List<ASTNode> children)
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

