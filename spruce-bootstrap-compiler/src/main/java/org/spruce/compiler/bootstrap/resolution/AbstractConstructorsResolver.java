package org.spruce.compiler.bootstrap.resolution;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.spruce.compiler.bootstrap.ast.expressions.ASTInvocation;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

/**
 * An <code>AbstractConstructorsResolver</code> is an <code>InvocationResolver</code>
 * that contains code common to resolving constructor invocations and class
 * instance creation expressions.
 */
public abstract class AbstractConstructorsResolver<T extends ASTInvocation> extends InvocationResolver<T> {
    /**
     * Constructs a <code>AbstractConstructorsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public AbstractConstructorsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * 2. Find all "potentially applicable constructors" for the given
     *    <code>Invocation</code>, if any exist.  The name must
     *    match, the number of parameters must match, and all arguments must be
     *    invocation convertible to the formal parameter type in this
     *    "invocation context".  The constructor must be directly in the type
     *    to search; not up its superclass/superinterface hierarchy and not in
     *    an enclosing class.
     * @param invocation A concrete instance of <code>ASTInvocation</code>,
     *                   resolvable to a constructor.
     * @param typeToSearch A <code>TypeSymbol</code> representing the type to
     *                     search, found in Step 1.
     * @return An <code>Set</code> of <code>ParameterizedSymbol</code>s.
     */
    @Override
    public Set<ParameterizedSymbol> getPotentiallyApplicable(T invocation,
                                                             TypeSymbol typeToSearch) {
        // 2.1. Find constructors by name.
        Set<ParameterizedSymbol> constructors = findConstructors(typeToSearch);

        // 2.2. Find potentially applicable constructors.
        return getInvocationConvertible(invocation.getArgumentList(), constructors);
    }

    private Set<ParameterizedSymbol> findConstructors(TypeSymbol type) {
        SymbolTable table = type.getTable();
        Set<ParameterizedSymbol> constructors = new HashSet<>();
        if (table.containsMethodName(Symbol.NAME_CONSTRUCTOR)) {
            constructors.addAll(table.getMethodsForName(Symbol.NAME_CONSTRUCTOR));
        }
        return constructors;
    }

    /**
     * 3. Get all maximally specific constructors given a set of applicable
     *    constructors.  A constructor "a" is "maximally specific" if there is
     *    no other applicable constructor that is "strictly more specific" than
     *    "a".  A constructor "a" is strictly more specific than another
     *    constructor "b" if all of a's formal parameter types are convertible
     *    to each of b's formal parameter types in an invocation context.
     *    Constructors with identical signatures in the same type are already a
     *    compiler error in the symbol creation phase.
     *    If a constructor "a" is strictly more specific than constructor "b",
     *    then constructor "a" eliminates "b" from being a maximally specific
     *    constructor.
     * @param applicableConstructors A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s.
     */
    @Override
    public List<ParameterizedSymbol> getMaximallySpecific(Set<ParameterizedSymbol> applicableConstructors) {
        // 3.1. Get all constructors that are strictly more specific than others.

        // 3.2. Return all maximally specific constructors.  If there is exactly
        //      one left, then it is the maximally specific constructor.  If
        //      there are more than one left, then returning them all will
        //      result in an ambiguous error.
        return getStrictlyMoreSpecific(applicableConstructors);
    }
}
