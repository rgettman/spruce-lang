package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTVariableDeclaratorList</code> is a comma-separated list of
 * variable declarators.</p>
 *
 * <em>
 * VariableDeclaratorList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;VariableDeclarator {, VariableDeclarator}
 * </em>
 */
public class ASTVariableDeclaratorList extends ASTListNode<ASTVariableDeclarator> {
    /**
     * Constructs an <code>ASTVariableDeclaratorList</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTVariableDeclarator</code>s.
     */
    public ASTVariableDeclaratorList(Location location, List<ASTVariableDeclarator> children) {
        super(location, children, Type.VARIABLE_DECLARATORS);
    }
}
