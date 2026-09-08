package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.expressions.ASTMethodInvocation;
import org.spruce.compiler.bootstrap.ast.expressions.ASTPrimary;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * A <code>MethodsResolver</code> is an <code>InvocationResolver</code> that
 * resolves method invocations.
 */
public class MethodsResolver extends InvocationResolver<ASTMethodInvocation> {
    /**
     * Constructs an <code>MethodsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public MethodsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Interfaces are allowed on the type to search.
     * @return <code>true</code>
     */
    public boolean isInterfaceAllowedOnTypeToSearch() {
        return true;
    }

    /**
     * 1. Find the "type to search" for the given <code>MethodInvocation</code>,
     *    if it exists.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    @Override
    public Optional<TypeSymbol> getTypeToSearch(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        Optional<ASTKeywordNode> optSuper = methodInvocation.getSooper();
        Optional<ASTExpressionName> optExprName = methodInvocation.getExprName();
        Optional<ASTTypeName> optTypeName = methodInvocation.getTypeName();
        Optional<ASTPrimary> optPrimary = methodInvocation.getPrimary();

        if (optSuper.isPresent()) {
            if (optTypeName.isPresent()) {
                // 1a. Typename.super.methodName
                return getTypeToSearchTypenameSuper(methodInvocation, ctx);
            }
            else {
                // 1b. super.methodName
                return getTypeToSearchSuper(methodInvocation, ctx);
            }
        }
        else {
            if (optPrimary.isPresent()) {
                // 1c. Primary.methodName
                return getTypeToSearchPrimary(methodInvocation, ctx);
            }
            else if (optExprName.isPresent()) {
                // 1d. ExpressionName.methodName
                return getTypeToSearchExpressionName(methodInvocation, ctx);
            }
            else {
                // 1e. methodName
                Optional<TypeSymbol> optTypeToSearch = getTypeToSearchBareMethodName(methodInvocation, ctx);
                if (optTypeToSearch.isEmpty()) {
                    errorSymbolNotFound(methodInvocation.getLocation(), methodInvocation.getIdentifier().getValue());
                }
                return optTypeToSearch;
            }
        }
    }

    // 1a. Typename.super.methodName
    private Optional<TypeSymbol> getTypeToSearchTypenameSuper(ASTMethodInvocation methodInvocation,
                                                              ResolutionContext ctx) {
        NamesResolver namesResolver = getNamesResolver();
        TypesResolver typesResolver = getTypesResolver();

        if (methodInvocation.getTypeName().isEmpty()) {
            throw internalError("type name on method invocation");
        }
        ASTTypeName typeName = methodInvocation.getTypeName().get();
        namesResolver.resolveTypeName(typeName, ctx);
        Optional<TypeSymbol> optResolved = Optional.ofNullable(typeName.getResolvedDataType());
        if (optResolved.isPresent()) {
            TypeSymbol resolved = optResolved.get();
            if (typesResolver.isEnclosingType(resolved, ctx.enclosingSymbol())) {
                Optional<TypeSymbol> optSuperclass = resolved.getSuperclass();
                if (optSuperclass.isEmpty()) {
                    error(methodInvocation.getLocation(),"Type " + resolved.getName() + " has no superclass");
                }
                methodInvocation.setNonsharedContextOnly(true);
                return optSuperclass;
            }
            else {
                error(methodInvocation.getLocation(), "Type " + resolved.getName() + " is not an enclosing type");
            }
        }
        // else an error was already generated for not found.
        return Optional.empty();
    }

    // 1b. super.methodName
    private Optional<TypeSymbol> getTypeToSearchSuper(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol type = typesResolver.findEnclosingType(ctx.enclosingSymbol());
        Optional<TypeSymbol> optSuperclass = type.getSuperclass();
        if (optSuperclass.isEmpty()) {
            error(methodInvocation.getLocation(), "Type " + type.getName() + " has no superclass");
        }
        methodInvocation.setNonsharedContextOnly(true);
        return optSuperclass;
    }

    // 1c. Primary.methodName
    private Optional<TypeSymbol> getTypeToSearchPrimary(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        if (methodInvocation.getPrimary().isEmpty()) {
            throw internalError("primary on method invocation");
        }
        ASTPrimary primary = methodInvocation.getPrimary().get();
        getExpressionsResolver().resolvePrimary(primary, ctx);
        methodInvocation.setNonsharedContextOnly(true);
        return Optional.ofNullable(primary.getResolvedDataType());
    }

    // 1d. ExpressionName.methodName
    private Optional<TypeSymbol> getTypeToSearchExpressionName(ASTMethodInvocation methodInvocation,
                                                               ResolutionContext ctx) {
        NamesResolver namesResolver = getNamesResolver();

        if (methodInvocation.getExprName().isEmpty()) {
            throw internalError("type name on method invocation");
        }
        ASTExpressionName exprName = methodInvocation.getExprName().get();

        Optional<Symbol> optResolved = namesResolver.resolveExpressionOrTypeNameIds(exprName.getTypedChildren(), ctx);
        if (optResolved.isEmpty()) {
            // Errors resolving the symbol already generated.
            return Optional.empty();
        }
        else {
            Symbol resolved = optResolved.get();
            switch(resolved) {
            case TypeSymbol type -> {
                // TypeName.methodName
                methodInvocation.setSharedContextOnly(true);
                return Optional.of(type);
            }
            case VariableSymbol variable -> {
                // ExpressionName.methodName
                methodInvocation.setNonsharedContextOnly(true);
                return Optional.of(variable.getDataType());
            }
            default -> throw internalError("Unexpected symbol type resolving expression name in method invocation: " +
                    resolved.getClass().getName());
            }
        }
    }

    // 1e. methodName
    private Optional<TypeSymbol> getTypeToSearchBareMethodName(ASTMethodInvocation methodInvocation,
                                                               ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        ParentSymbol parent = typesResolver.findEnclosingType(ctx.enclosingSymbol());
        String methodName = methodInvocation.getIdentifier().getValue();

        // Look in the current type and its superclass and superinterfaces hierarchy,
        // before searching an enclosing type and its superclass and superinterface hierarchy.
        // The type to search is the current type, or an enclosing type, even if the
        // name is found in a superclass or a superinterface.

        // 1. Repeat (2) for each enclosing type further up the enclosing type
        //    hierarchy.  Stopping with a match means shadowing possible
        //    matches further up the enclosing type hierarchy.
        while (parent.isType()) {
            TypeSymbol type = (TypeSymbol) parent;

            // 2. Starting with the current type, check the superclass
            //    hierarchy and the superinterface hierarchy.  Finding a match
            //    means that the current type, NOT any superclass or
            //    superinterface, is the type to search.
            Set<ParameterizedSymbol> found = findMethodsByName(methodName, type);
            if (!found.isEmpty()) {
                // Can resolve to either a shared method or a non-shared method.
                // But it must be a shared method if the enclosing context is shared.
                if (ctx.isShared()) {
                    methodInvocation.setSharedContextOnly(true);
                }
                return Optional.of(type);
            }
            parent = ((ChildSymbolTable) type.getParent()).getParent();
        }

        // 3. Eventually, check shared use statements (they don't exist yet).

        // 4. No method in scope with that name.
        return Optional.empty();
    }

    /**
     * 2. Find all "potentially applicable methods" for the given
     *    <code>MethodInvocation</code>, if any exist.  The name must match,
     *    the number of parameters must match, and all arguments must be
     *    invocation convertible to the formal parameter type in this
     *    "invocation context".  The method may be in the type to search or
     *    up its superclass/superinterface hierarchy, but not in an enclosing
     *    class.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param typeToSearch A <code>TypeSymbol</code> representing the type to
     *                     search, found in Step 1.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    @Override
    public Set<ParameterizedSymbol> getPotentiallyApplicable(ASTMethodInvocation methodInvocation,
                                                             TypeSymbol typeToSearch) {
        String methodName = methodInvocation.getIdentifier().getValue();
        // 2.1. Find methods by name.
        Set<ParameterizedSymbol> methods = findMethodsByName(methodName, typeToSearch);

        // 2.2. Find potentially applicable methods.
        return getInvocationConvertible(methodInvocation.getArgumentList(), methods);
    }

    /**
     * Find all methods with the given name on the given <code>TypeSymbol</code>.
     * @param methodName The method name.
     * @param type A <code>TypeSymbol</code>.
     * @return A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     */
    protected Set<ParameterizedSymbol> findMethodsByName(String methodName, TypeSymbol type) {
        SymbolTable table = type.getTable();
        Set<ParameterizedSymbol> methods = new HashSet<>();
        if (table.containsMethodName(methodName)) {
            methods.addAll(table.getMethodsForName(methodName));
        }

        // Walk up superclass hierarchy then superinterface hierarchy looking
        // for the method name.
        if (type.getSuperclass().isPresent()) {
            TypeSymbol superclass = type.getSuperclass().get();
            Set<ParameterizedSymbol> foundInSuperclass = findMethodsByName(methodName, superclass);
            methods.addAll(foundInSuperclass);
        }

        methods.addAll(findMethodsInSuperinterfaces(methodName, type.getSuperinterfaces()));
        return methods;
    }

    private Set<ParameterizedSymbol> findMethodsInSuperinterfaces(String methodName, List<TypeSymbol> superinterfaces) {
        Set<ParameterizedSymbol> methods = new HashSet<>();
        for (TypeSymbol superinterface : superinterfaces) {
            ChildSymbolTable child = superinterface.getTable();
            if (child.containsMethodName(methodName)) {
                methods.addAll(child.getMethodsForName(methodName));
            }
            else {
                // Recur on this superinterface's superinterfaces.
                Set<ParameterizedSymbol> foundInSuperinterfaces = findMethodsInSuperinterfaces(
                        methodName, superinterface.getSuperinterfaces());
                methods.addAll(foundInSuperinterfaces);
            }
        }
        return methods;
    }

    /**
     * 3. Get all maximally specific methods given a set of applicable methods.
     *    A method "a" is "maximally specific" if there is no other applicable
     *    method that is "strictly more specific" than "a".
     *    A method "a" is strictly more specific than another method "b" if
     *    all of a's formal parameter types are convertible to each of b's
     *    formal parameter types in a method invocation context.
     *    Methods with identical signatures in the same type are already a
     *    compiler error in the symbol creation phase, leaving only the
     *    possibility that one method overrides the other.  In this case, the
     *    method of the overriding (or hiding) type is strictly more specific
     *    than the overridden (or hidden) method.
     *    If a method "a" is strictly more specific than method "b", then
     *    method "a" eliminates "b" from being a maximally specific method.
     * @param applicableMethods A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s.
     */
    @Override
    public List<ParameterizedSymbol> getMaximallySpecific(Set<ParameterizedSymbol> applicableMethods) {
        // 3.1. Get all methods that are strictly more specific than others.
        List<ParameterizedSymbol> maxSpecificMethods = getStrictlyMoreSpecific(applicableMethods);
        if (maxSpecificMethods.size() == 1) {
            return maxSpecificMethods;
        }

        // 3.2. Multiple methods remain.  If they are not all override
        //      equivalent, return them all for an ambiguous error.
        if (!areAllOverrideEquivalent(maxSpecificMethods)) {
            return maxSpecificMethods;
        }

        // 3.3. Multiple override-equivalent methods remain.  If more than 1 are
        //      concrete, return them all for an ambiguous error.  If exactly
        //      one is concrete, then it is the maximally specific method.
        List<ParameterizedSymbol> concreteMethods = maxSpecificMethods.stream()
                .filter(s -> !s.isAbstract())
                .toList();
        if (!concreteMethods.isEmpty()) {
            return concreteMethods;
        }

        // 3.4. Multiple abstract override-equivalent methods remain.  Choose
        //      "preferred" methods.  If there are any preferred
        //      methods, pick one (it's abstract and will be resolved at
        //      runtime anyway!)  Else return all for an ambiguous error.
        List<ParameterizedSymbol> preferredMethods = getPreferredMethods(maxSpecificMethods);
        if (!preferredMethods.isEmpty()) {
            return List.of(preferredMethods.get(0));
        }

        return maxSpecificMethods;
    }

    // 3.4. Get all "preferred" methods.  A "preferred" method is "return-type-
    //      substitutable" for all other methods.  A method is "return-type-
    //      substitutable" if both methods return "void" or its return type is
    //      a subtype of all other return types of all other methods.
    private List<ParameterizedSymbol> getPreferredMethods(List<ParameterizedSymbol> maxSpecificMethods) {
        List<ParameterizedSymbol> preferredMethods = new ArrayList<>(maxSpecificMethods.size());

        for (int i = 0; i < maxSpecificMethods.size(); i++) {
            ParameterizedSymbol method = maxSpecificMethods.get(i);
            boolean isReturnTypeSubstitutable = true;
            for (int j = 0; j < maxSpecificMethods.size(); j++) {
                if (i != j) {
                    ParameterizedSymbol other = maxSpecificMethods.get(j);
                    if (!method.getDataType().isSubTypeOf(other.getDataType())) {
                        isReturnTypeSubstitutable = false;
                        break;
                    }
                }
            }
            if (isReturnTypeSubstitutable) {
                preferredMethods.add(method);
            }
        }

        return preferredMethods;
    }

    /**
     * 5. Enforce whether the resolved method must be shared, must NOT be
     *    shared, or neither.  If the resolved method is shared, then it must
     *    be found directly in the type to search, not in the superclass or
     *    superinterface hierarchy.
     *    Shared methods are NOT inherited!
     *    This implies that the only way to call a shared method with a simple
     *    name is to declare the shared method in the same class.
     * @param invocation A concrete instance of <code>ASTInvocation</code>.
     * @param typeToSearch A <code>TypeSymbol</code> that is the type to search (step 1).
     * @param resolved An already resolved <code>ParameterizedSymbol</code>.
     */
    @Override
    public void enforceAdditionalRules(ASTMethodInvocation invocation, TypeSymbol typeToSearch, ParameterizedSymbol resolved) {
        ASTIdentifier name = invocation.getIdentifier();
        boolean resolvedToShared = resolved.isShared();
        if (resolvedToShared) {
            if (invocation.isNonsharedContextOnly()) {
                error(name.getLocation(), "Shared method '" + name.getValue() +
                        "' cannot be referenced from a qualified non-shared context.  " +
                        "Use the name of the type that contains the shared method.");
            }
            ChildSymbolTable table = typeToSearch.getTable();
            // Check if that symbol's name is in the type to search, and see if
            // that it matches the resolved symbol (not just hiding it).
            if (!table.containsSymbolName(resolved.getName()) || table.get(resolved.getName()) != resolved) {
                errorSymbolNotFound(name.getLocation(), name.getValue());
            }
        }
        else {
            if (invocation.isSharedContextOnly()) {
                error(name.getLocation(), "Non-shared method '" + name.getValue() +
                        "' cannot be referenced from a shared context.");
            }
        }
    }
}
