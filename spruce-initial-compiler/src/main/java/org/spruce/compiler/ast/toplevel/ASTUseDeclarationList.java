package org.spruce.compiler.ast.toplevel;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTUseDeclarationList</code> is multiple use declarations.</p>
 *
 * <em>
 * UseDeclarationList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseDeclaration {UseDeclaration}
 * </em>
 */
public class ASTUseDeclarationList extends ASTListNode<ASTUseDeclaration> {
    /**
     * Constructs an <code>ASTUseDeclarationList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTUseDeclaration</code>s.
     */
    public ASTUseDeclarationList(Location location, List<ASTUseDeclaration> children) {
        super(location, children, Type.USE_DECLARATIONS);
    }
}
