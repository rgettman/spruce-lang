package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.Node;

/**
 * An <code>ASTHasTarget</code> has a "target", which is a for statement, if
 * statement, or while statement that a break or continue statement jumps to.
 * This is determined during the flow analysis phase and it is used during the
 * byte code generation phase.
 */
public interface ASTHasTarget extends Node {
    /**
     * Sets the target <code>BlockStatement</code>.
     * @param target An <code>ASTBlockStatement</code>.
     */
    void setTarget(ASTBlockStatement target);

    /**
     * Returns the target <code>BlockStatement</code>.
     * @return The target <code>BlockStatement</code>.
     */
    ASTBlockStatement getTarget();
}
