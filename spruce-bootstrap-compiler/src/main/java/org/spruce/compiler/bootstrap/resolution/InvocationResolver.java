package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * An <code>InvocationResolver</code> is a <code>BasicResolver</code> that
 * resolves invocations such as method invocations, constructor invocations,
 * and class instance creation expressions in the expressions portion of an AST
 * and Symbol Table.
 * All other expressions are resolved in the <code>ExpressionsResolver</code>
 * (except expression names, resolved in the <code>NamesResolver</code>).
 */
public abstract class InvocationResolver<T extends ASTInvocation> extends BasicResolver {
    /**
     * Constructs an <code>InvocationResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public InvocationResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolves symbols in an <code>Invocation</code>.
     * @param invocation A concrete instance of <code>ASTInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveInvocation(T invocation, ResolutionContext ctx) {
        // Resolve types of arguments first!
        ASTArgumentList argList = invocation.getArgumentList();
        if (!resolveArguments(argList, ctx)) {
            // Don't bother resolving the invocation if the arguments didn't
            // even resolve.
            return;
        }

        // 1. Determine the type to search.
        Optional<TypeSymbol> optTypeToSearch = getTypeToSearch(invocation, ctx);
        if (optTypeToSearch.isEmpty() || (!isInterfaceAllowedOnTypeToSearch() && optTypeToSearch.get().isInterface())) {
            // Error generated already.
            return;
        }

        // 2. Identify potentially applicable invocations, which MUST be
        //    directly on the type to search.
        TypeSymbol typeToSearch = optTypeToSearch.get();
        Set<ParameterizedSymbol> potentiallyApplicable = getPotentiallyApplicable(invocation, typeToSearch);
        if (potentiallyApplicable.isEmpty()) {
            errorSymbolNotFound(invocation.getLocation(), Symbol.NAME_CONSTRUCTOR);
            return;
        }

        // 3. Choose most specific invocation, if one such invocation exists.
        //    If no one invocation is maximally specific, ambiguous error.
        List<ParameterizedSymbol> maximallySpecific = getMaximallySpecific(potentiallyApplicable);
        if (maximallySpecific.isEmpty()) {
            throw internalError("Maximally specific filter eliminated all invocations!");
        }
        if (maximallySpecific.size() > 1) {
            error(invocation.getLocation(), "Ambiguous invocation - multiple maximally specific invocations match.");
            for (ParameterizedSymbol match : maximallySpecific) {
                note(match.getLocation(), "This invocation matches.");
            }
        }

        // 4. Type of the invocation.
        ParameterizedSymbol resolved = maximallySpecific.get(0);
        invocation.setResolvedEntity(resolved);

        // 5. Enforce any additional rules.
        enforceAdditionalRules(invocation, typeToSearch, resolved);
    }

    /**
     * Resolves symbols in an <code>ArgumentList</code>.
     * @param argList An <code>ASTArgumentList</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return Whether all arguments were successfully resolved.
     */
    public final boolean resolveArguments(ASTArgumentList argList, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        List<ASTExpression> args = argList.getTypedChildren();
        boolean resolved = true;
        for (ASTExpression arg : args) {
            exprResolver.resolveExpression(arg, ctx);
            Optional<TypeSymbol> optResolved = Optional.ofNullable(arg.getResolvedDataType());
            if (optResolved.isEmpty()) {
                resolved = false;
            }
        }
        return resolved;
    }

    /**
     * Returns whether an interface is allowed on the type to search.
     * @return Whether an interface is allowed on the type to search.
     */
    public abstract boolean isInterfaceAllowedOnTypeToSearch();

    /**
     * 1. Find the "type to search" for the given <code>Invocation</code>,
     *    if it exists.
     * @param invocation A concrete instance of <code>ASTInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public abstract Optional<TypeSymbol> getTypeToSearch(T invocation, ResolutionContext ctx);

    /**
     * 2. Find all "potentially applicable invocations" for the given
     *    <code>Invocation</code>, if any exist.  The name must
     *    match, the number of parameters must match, and all arguments must be
     *    invocation convertible to the formal parameter type in this
     *    "invocation context".  The invocation must be directly in the type
     *    to search; not up its superclass/superinterface hierarchy and not in
     *    an enclosing class.
     * @param invocation A concrete instance of <code>ASTInvocation</code>.
     * @param typeToSearch A <code>TypeSymbol</code> representing the type to
     *                     search, found in Step 1.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public abstract Set<ParameterizedSymbol> getPotentiallyApplicable(T invocation, TypeSymbol typeToSearch);

    /**
     * 3. Get all maximally specific invocations given a set of applicable
     *    invocations.  An invocation "a" is "maximally specific" if there is
     *    no other applicable invocation that is "strictly more specific" than
     *    "a".  An invocation "a" is strictly more specific than another
     *    invocation "b" if all of a's formal parameter types are convertible
     *    to each of b's formal parameter types in an invocation context.
     * @param applicable A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s.
     */
    public abstract List<ParameterizedSymbol> getMaximallySpecific(Set<ParameterizedSymbol> applicable);

    /**
     * 5. Enforce any additional rules that may be applicable to a particular
     *    invocation type.  The default implementation does nothing.
     *    Implementing subclasses may override this method to provide additional
     *    validation.
     * @param invocation A concrete instance of <code>ASTInvocation</code>.
     * @param typeToSearch A <code>TypeSymbol</code> that is the type to search (step 1).
     * @param resolved An already resolved <code>ParameterizedSymbol</code>.
     */
    public void enforceAdditionalRules(T invocation, TypeSymbol typeToSearch, ParameterizedSymbol resolved) {
    }

    /**
     * Find which invocations match the given <code>ArgumentList</code>, in
     * that each argument is invocation-convertible to the corresponding formal
     * parameter of the invocation.
     * @param argsList An <code>ASTArgumentList</code>.
     * @param invocations A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     * @return Another <code>Set</code> of <code>ParameterizedSymbol</code>s.
     */
    protected Set<ParameterizedSymbol> getInvocationConvertible(ASTArgumentList argsList,
                                                                Set<ParameterizedSymbol> invocations) {
        Set<ParameterizedSymbol> applicableMethods = new HashSet<>(invocations.size());
        for (ParameterizedSymbol invocation : invocations) {
            List<TypeSymbol> args = argsList.getTypedChildren().stream()
                    .map(ASTExpression::getResolvedDataType)
                    .toList();
            List<TypeSymbol> params = invocation.getParameters().stream()
                    .map(VariableSymbol::getDataType)
                    .toList();
            int numParams = invocation.numParameters();
            int numArgs = args.size();
            // Invocations must have the same arity (number of parameters) as the
            // target invocation.
            if (numParams != numArgs) {
                continue;
            }
            // All argument types must be convertible by invocation conversion to
            // the corresponding formal parameter type.
            boolean applicable = true;
            for (int i = 0; i < numParams; i++) {
                TypeSymbol param = params.get(i);
                TypeSymbol arg = args.get(i);
                if (param == null || arg == null || !isInvocationConvertible(arg, param)) {
                    applicable = false;
                    break;
                }
            }

            if (applicable) {
                applicableMethods.add(invocation);
            }
        }
        return applicableMethods;
    }

    /**
     * <p>Determines whether an argument is invocation-convertible to a formal
     * parameter.</p>
     * <p>
     * Invocation conversion is:
     * <ol>
     * <li>Identity conversion (match type exactly)</li>
     * <li>Reference widening conversion (argument type is a subtype of
     * parameter type).</li>
     * <li>Primitive widening conversion, e.g. Integer to Long.  (This does
     * not exist in Spruce yet, and it cannot exist until a mechanism is
     * decided upon to represent such an implicit conversion.)</li>
     * </ol>
     * </p>
     */
    protected boolean isInvocationConvertible(TypeSymbol arg, TypeSymbol param) {
        return arg == param || arg.isSubTypeOf(param);
    }

    /**
     * 3.1. Eliminate invocation "b" if invocation "a" is "strictly more
     *      specific" than invocation "b".  Invocation "a" is "strictly more
     *      specific" than invocation "b" if all formal parameter types of
     *      invocation "a" are convertible to the formal parameter types of
     *      invocation "b" by invocation conversion.  In case of
     *      override-equivalent signatures, if invocation "a" overrides
     *      invocation "b", then invocation "a" is more specific than
     *      invocation "b".
     * @param applicable A <code>Set</code> of <code>ParameterizedSymbol</code>s
     *                   representing maximally specific invocations.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s that
     *         are strictly more specific.
     */
    protected final List<ParameterizedSymbol> getStrictlyMoreSpecific(Set<ParameterizedSymbol> applicable) {
        TypesResolver typesResolver = getTypesResolver();
        List<ParameterizedSymbol> maxSpecific = new ArrayList<>(applicable);
        for (int i = 0; i < maxSpecific.size(); i++) {
            for (int j = i + 1; j < maxSpecific.size(); j++) {
                ParameterizedSymbol first = maxSpecific.get(i);
                ParameterizedSymbol second = maxSpecific.get(j);
                boolean isFirstMoreSpecific = isMoreSpecific(first, second);
                boolean isSecondMoreSpecific = isMoreSpecific(second, first);
                boolean removeFirst = false;
                boolean removeSecond = false;
                if (isFirstMoreSpecific && isSecondMoreSpecific) {
                    // Same signature.  Determine which overrides the other.
                    TypeSymbol firstType = typesResolver.findEnclosingType(first);
                    TypeSymbol secondType = typesResolver.findEnclosingType(second);
                    if (firstType.isSubTypeOf(secondType)) {
                        removeSecond = true;
                    }
                    else if (secondType.isSubTypeOf(firstType)) {
                        removeFirst = true;
                    }
                }
                else if (isFirstMoreSpecific) {
                    removeSecond = true;
                }
                else if (isSecondMoreSpecific) {
                    removeFirst = true;
                }

                if (removeSecond) {
                    // First is strictly more specific than second.
                    maxSpecific.remove(j);
                    // Try index j again; either the end of the list or another invocation awaits.
                    j--;
                }
                else if (removeFirst) {
                    // Second is strictly more specific than first.
                    maxSpecific.remove(i);
                    // Try index i again; either the end of the list or another invocation awaits.
                    i--;
                    break;  // out of the "j" for loop.
                }
            }
        }
        return maxSpecific;
    }

    /**
     * Returns true if all parameter types in the first list are invocation
     * convertible to their corresponding parameter type in the second list.
     * @param first The first <code>ParameterizedSymbol</code>.
     * @param second The second <code>ParameterizedSymbol</code>.
     * @return Whether the first symbol is more specific than the second.
     */
    protected final boolean isMoreSpecific(ParameterizedSymbol first, ParameterizedSymbol second) {
        // At this point the invocations are both applicable, so this logic
        // assumes that they have the same name and the same arity (number of
        // parameters).
        List<TypeSymbol> firstParams = first.getParameters().stream()
                .map(VariableSymbol::getDataType)
                .toList();
        List<TypeSymbol> secondParams = second.getParameters().stream()
                .map(VariableSymbol::getDataType)
                .toList();
        for (int p = 0; p < firstParams.size(); p++) {
            TypeSymbol firstParam = firstParams.get(p);
            TypeSymbol secondParam = secondParams.get(p);
            if (!isInvocationConvertible(firstParam, secondParam)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 3.2. Determine if all maximally specific invocations are override-equivalent.
     * @param maxSpecificMethods A <code>List</code> of <code>ParameterizedSymbol</code>s.
     * @return Whether all given invocations are override-equivalent.
     */
    protected final boolean areAllOverrideEquivalent(List<ParameterizedSymbol> maxSpecificMethods) {
        ParameterizedSymbol first = maxSpecificMethods.get(0);
        for (int i = 1; i < maxSpecificMethods.size(); i++) {
            ParameterizedSymbol other = maxSpecificMethods.get(i);
            if (!isOverrideEquivalent(first, other)) {
                return false;
            }
        }

        return true;
    }

    private boolean isOverrideEquivalent(ParameterizedSymbol first, ParameterizedSymbol second) {
        if (!first.getName().equals(second.getName())) {
            return false;
        }
        List<VariableSymbol> firstParams = first.getParameters();
        List<VariableSymbol> secondParams = second.getParameters();
        if (firstParams.size() != secondParams.size()) {
            return false;
        }
        for (int i = 0; i < firstParams.size(); i++) {
            if (firstParams.get(i).getDataType() != secondParams.get(i).getDataType()) {
                return false;
            }
        }
        return true;
    }
}
