package org.spruce.compiler.ast.statements;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.common.Location;

/**
 * <p>An <code>ASTBlockStatements</code> is a list of block statement instances.</p>
 *
 * <em>
 * BlockStatements:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BlockStatement {BlockStatement}
 * </em>
 */
public class ASTBlockStatements extends ASTListNode<ASTBlockStatement> {
    /**
     * Constructs an <code>ASTBlockStatements</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTBlockStatement</code>s.
     */
    public ASTBlockStatements(Location location, List<ASTBlockStatement> children) {
        super(location, children, Type.BLOCK_STMTS);
    }
}
