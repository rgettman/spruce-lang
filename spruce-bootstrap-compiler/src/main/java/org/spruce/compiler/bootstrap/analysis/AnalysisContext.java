package org.spruce.compiler.bootstrap.analysis;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.statements.ASTBlockStatement;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * An <code>AnalysisContext</code> contains contextual data to aid in the
 * analysis of blocks and statements.
 * @param fieldsWithInitializers A <code>List</code> of <code>VariableSymbol</code>s
 *                               representing fields that have initializers.
 * @param initializedLocalVars A <code>List</code> of <code>VariableSymbol</code>s
 *                             representing local variables that are initialized.
 * @param target An <code>ASTBlockStatement</code> representing a break/continue target.
 *               It's null if there is no such target.
 * @param isInConstructor Whether the scope is inside a constructor (or method if not).
 * @param isPastPrologue Whether the scope is past a constructor's prologue,
 *                       which is the set of statements before any constructor
 *                       invocation.
 * @param isReturningVoid Whether the scope is inside a constructor (effectively
 *                        returning <code>void</code>) or inside a <code>void</code>
 *                        method.
 */
public record AnalysisContext(
        List<VariableSymbol> fieldsWithInitializers,
        List<VariableSymbol> initializedLocalVars,
        ASTBlockStatement target,
        boolean isInConstructor,
        boolean isPastPrologue,
        boolean isReturningVoid
) {
    /**
     * Creates and returns an <code>AnalysisContext</code> for a constructor.
     * @param fieldsWithInitializers A <code>List</code> of <code>VariableSymbol</code>s
     *                               representing fields that have initializers.
     * @return An <code>AnalysicContext</code>.
     */
    public static AnalysisContext ofConstructor(List<VariableSymbol> fieldsWithInitializers) {
        return new AnalysisContext(fieldsWithInitializers, new ArrayList<>(), null, true,
                false,true);
    }

    /**
     * Creates and returns an <code>AnalysisContext</code> for a method.
     * @param isReturningVoid Whether the enclosing method returns <code>void</code>.
     * @return An <code>AnalysicContext</code>.
     */
    public static AnalysisContext ofMethod(boolean isReturningVoid) {
        return new AnalysisContext(new ArrayList<>(), new ArrayList<>(), null, false,
                false, isReturningVoid);
    }

    /**
     * Returns a new <code>AnalysisContext</code> with a copy of the <code>List</code>
     * of uninitialized local variables for a new subscope.
     * @return An <code>AnalysisContext</code>.
     */
    public AnalysisContext withSubscope() {
        return new AnalysisContext(this.fieldsWithInitializers, new ArrayList<>(this.initializedLocalVars),
                this.target, this.isInConstructor, this.isPastPrologue, this.isReturningVoid);
    }

    /**
     * Returns a new <code>AnalysisContext</code> with a copy of the <code>List</code>
     * of uninitialized local variables for a new subscope, and with the given
     * <code>BlockStatement</code> as the break/continue target.
     * @param target An <code>ASTBlockStatement</code> representing the
     *               break/continue target.
     * @return An <code>AnalysisContext</code>.
     */
    public AnalysisContext withTargetedSubscope(ASTBlockStatement target) {
        return new AnalysisContext(this.fieldsWithInitializers, new ArrayList<>(this.initializedLocalVars),
                target, this.isInConstructor, this.isPastPrologue, this.isReturningVoid);
    }

    /**
     * Returns a new <code>AnalysisContext</code> that is past the prologue,
     * which is the set of statements before an alternate constructor invocation.
     * @return An <code>AnalysisContext</code>.
     */
    public AnalysisContext pastPrologue() {
        return new AnalysisContext(this.fieldsWithInitializers, new ArrayList<>(this.initializedLocalVars),
                target, this.isInConstructor, true, this.isReturningVoid);
    }

    /**
     * Returns the target <code>ASTBlockStatement</code>, if it exists.
     * @return An <code>Optional&lt;ASTBlockStatement&gt;</code>.
     */
    public Optional<ASTBlockStatement> getTarget() {
        return Optional.ofNullable(this.target);
    }
}
