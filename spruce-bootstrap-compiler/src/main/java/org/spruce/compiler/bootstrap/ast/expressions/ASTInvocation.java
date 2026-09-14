package org.spruce.compiler.bootstrap.ast.expressions;

import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.symbol.EntityResolution;

/**
 * <p>An <code>ASTInvocation</code> is an <code>ASTMethodInvocation</code>,
 * a <code>ASTClassInstanceCreationExpression</code>, or an
 * <code>ASTConstructorInvocation</code>.</p>
 *
 * <em>
 * Invocation:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassInstanceCreationExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorInvocation<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodInvocation
 * </em>
 */
public interface ASTInvocation extends EntityResolution {
    /**
     * Returns the <code>Location</code>.
     * @return The <code>Location</code>.
     */
    Location getLocation();

    /**
     * Returns an <code>ASTArgumentList</code>.
     * @return An <code>ASTArgumentList</code>.
     */
    ASTArgumentList getArgumentList();
}
