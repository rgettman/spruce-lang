package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeSharedMultDeclaration</code> is "recognize" followed
 * by "shared" followed by a Type Name, a dot, then an identifier list
 * within braces, and a semicolon.</p>
 *
 * <em>
 * RecognizeSharedMultDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;recognize shared TypeName . { IdentifierList } ;
 * </em>
 */
public class ASTRecognizeSharedMultDeclaration extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeSharedMultDeclaration</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeSharedMultDeclaration(Location location, List<ASTNode> children)
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

