package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTRecognizeDeclarationList</code> is multiple recognize declarations.</p>
 *
 * <em>
 * RecognizeDeclarationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecognizeDeclaration {RecognizeDeclaration}
 * </em>
 */
public class ASTRecognizeDeclarationList extends ASTParentNode
{
    /**
     * Constructs an <code>ASTRecognizeDeclarationList</code> at the given <code>Location</code>
     * and with the base and the index as its children.
     * @param children The child nodes.
     */
    public ASTRecognizeDeclarationList(Location location, List<ASTNode> children)
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

