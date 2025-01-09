package org.spruce.compiler.ast.expressions;

import java.util.List;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTCaseConstants</code> is a comma-separated list of
 * conditional expressions.</p>
 *
 * <em>
 * CaseConstants:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression {, ValueExpression}
 * </em>
 */
public class ASTCaseConstants extends ASTListNode<ASTValueExpression> {
    /**
     * Constructs an <code>ASTCaseConstants</code> with a <code>Location</code>, a
     * list of child nodes, and a list type.
     *
     * @param location The <code>Location</code>.
     * @param children A <code>List</code> of <code>ASTValueExpression</code>s.
     */
    public ASTCaseConstants(Location location, List<ASTValueExpression> children) {
        super(location, children, Type.CASE_CONSTANTS);
    }
}
