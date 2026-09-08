package org.spruce.compiler.bootstrap.resolution;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

import static org.spruce.compiler.bootstrap.scanner.TokenType.SELF;
import static org.spruce.compiler.bootstrap.scanner.TokenType.SUPER;

/**
 * A <code>ConstructorsResolver</code> is an <code>InvocationResolver</code> that
 * resolves constructor invocations.
 */
public class ConstructorsResolver extends InvocationResolver<ASTConstructorInvocation> {
    /**
     * Constructs an <code>ConstructorsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public ConstructorsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Interfaces are not allowed on the type to search.
     * @return <code>false</code>
     */
    public boolean isInterfaceAllowedOnTypeToSearch() {
        return false;
    }

    /**
     * 1. Find the "type to search" for the given <code>ConstructorInvocation</code>,
     *    if it exists.
     * @param constrInvocation An <code>ASTConstructorInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> getTypeToSearch(ASTConstructorInvocation constrInvocation, ResolutionContext ctx) {
        TokenType tokenType = constrInvocation.getConstructorKeyword().getKeyword();
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol type = typesResolver.findEnclosingType(ctx.enclosingSymbol());
        if (tokenType == SELF) {
            // 1a. Self
            return Optional.of(type);
        }
        else if (tokenType == SUPER) {
            // 1b. Super
            Optional<TypeSymbol> optSuperclass = type.getSuperclass();
            if (optSuperclass.isEmpty()) {
                error(constrInvocation.getLocation(), "Type " + type.getName() + " has no superclass");
            }
            return optSuperclass;
        }
        else {
            throw internalError("expected self or super on constructor invocation!");
        }
    }

    /**
     * 2. Find all "potentially applicable constructors" for the given
     *    <code>ConstructorInvocation</code>, if any exist.  The name must
     *    match, the number of parameters must match, and all arguments must be
     *    invocation convertible to the formal parameter type in this
     *    "invocation context".  The constructor must be directly in the type
     *    to search; not up its superclass/superinterface hierarchy and not in
     *    an enclosing class.
     * @param constrInvocation An <code>ASTConstructorInvocation</code>.
     * @param typeToSearch A <code>TypeSymbol</code> representing the type to
     *                     search, found in Step 1.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    @Override
    public Set<ParameterizedSymbol> getPotentiallyApplicable(ASTConstructorInvocation constrInvocation,
                                                             TypeSymbol typeToSearch) {
        // 2.1. Find constructors by name.
        Set<ParameterizedSymbol> constructors = findConstructors(typeToSearch);

        // 2.2. Find potentially applicable constructors.
        return getInvocationConvertible(constrInvocation.getArgumentList(), constructors);
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
