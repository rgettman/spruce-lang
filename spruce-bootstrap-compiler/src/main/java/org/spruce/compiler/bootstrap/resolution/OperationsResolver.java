package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.expressions.*;
import org.spruce.compiler.bootstrap.ast.names.ASTExpressionName;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.ast.statements.ASTConstructorInvocation;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * An <code>OperationsResolver</code> is a <code>BasicResolver</code> that
 * resolves method invocations, constructor invocations, and expressions
 * involving operators in the expressions portion of an AST and Symbol Table.
 * All other expressions are resolved in the <code>ExpressionsResolver</code>
 * (except expression names, resolved in the <code>NamesResolver</code>).
 */
public class OperationsResolver extends BasicResolver {
    /**
     * Constructs an <code>OperationsResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public OperationsResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolves symbols in an <code>ArgumentList</code>.
     * @param argList An <code>ASTArgumentList</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return Whether all arguments were successfully resolved.
     */
    public boolean resolveArguments(ASTArgumentList argList, ResolutionContext ctx) {
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

    //
    // Constructor Resolution
    //

    /**
     * Resolves symbols in a <code>ConstructorInvocation</code>.
     * @param constrInvocation An <code>ASTConstructorInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveConstructorInvocation(ASTConstructorInvocation constrInvocation, ResolutionContext ctx) {
        // Resolve types of arguments first!
        ASTArgumentList argList = constrInvocation.getArgumentsList();
        if (!resolveArguments(argList, ctx)) {
            // Don't bother resolving the invocation to a constructor if the
            // arguments didn't even resolve.
            return;
        }

        // 1. Determine the type to search.
        Optional<TypeSymbol> optTypeToSearch = getTypeToSearch(constrInvocation, ctx);
        if (optTypeToSearch.isEmpty()) {
            // Error generated already.
            return;
        }

        // 2. Identify potentially applicable constructors, which MUST be
        //    directly on the type to search.
        TypeSymbol typeToSearch = optTypeToSearch.get();
        Set<ParameterizedSymbol> potentiallyApplicable = getPotentiallyApplicableConstructors(
                constrInvocation, typeToSearch);
        if (potentiallyApplicable.isEmpty()) {
            errorSymbolNotFound(constrInvocation.getLocation(), Symbol.NAME_CONSTRUCTOR);
            return;
        }

        // 3. Choose most specific constructor, if one such constructor exists.
        //    If no one constructor is maximally specific, ambiguous error.
        List<ParameterizedSymbol> maximallySpecific = getMaximallySpecificConstructors(potentiallyApplicable);
        if (maximallySpecific.isEmpty()) {
            throw internalError("Maximally specific filter eliminated all constructors!");
        }
        if (maximallySpecific.size() > 1) {
            error(constrInvocation.getLocation(), "Ambiguous constructor call - multiple maximally specific constructors match.");
            for (ParameterizedSymbol match : maximallySpecific) {
                note(match.getLocation(), "This constructor matches.");
            }
        }

        // 4. No Result type; all must be non-shared.
        ParameterizedSymbol resolved = maximallySpecific.get(0);
        constrInvocation.setResolvedEntity(resolved);
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
     * 2. Find all "potentially applicable methods" for the given
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
    public Set<ParameterizedSymbol> getPotentiallyApplicableConstructors(ASTConstructorInvocation constrInvocation,
                                                                         TypeSymbol typeToSearch) {
        // 2.1. Find methods by name.
        Set<ParameterizedSymbol> methods = findConstructors(typeToSearch);

        // 2.2. Find potentially applicable methods.
        return getInvocationConvertible(constrInvocation.getArgumentsList(), methods);
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
     *    to each of b's formal parameter types in a method invocation context.
     *    Constructors with identical signatures in the same type are already a
     *    compiler error in the symbol creation phase.
     *    If a constructor "a" is strictly more specific than constructor "b",
     *    then constructor "a" eliminates "b" from being a maximally specific
     *    constructor.
     * @param applicableConstructors A <code>Set</code> of <code>ParameterizedSymbol</code>s.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s.
     */
    public List<ParameterizedSymbol> getMaximallySpecificConstructors(Set<ParameterizedSymbol> applicableConstructors) {
        // 3.1. Get all constructors that are strictly more specific than others.

        // 3.2. Return all maximally specific constructors.  If there is exactly
        //      one left, then it is the maximally specific constructor.  If
        //      there are more than one left, then returning them all will
        //      result in an ambiguous error.
        return getStrictlyMoreSpecific(applicableConstructors);
    }



    public void resolveClassInstanceCreationExpression(ASTClassInstanceCreationExpression cice, ResolutionContext ctx) {

    }

    //
    // Method Resolution
    //

    /**
     * Resolves symbols in a <code>MethodInvocation</code>.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveMethodInvocation(ASTMethodInvocation methodInvocation, ResolutionContext ctx) {
        // Resolve types of arguments first!
        ASTArgumentList argList = methodInvocation.getArgumentList();
        if (!resolveArguments(argList, ctx)) {
            // Don't bother resolving the invocation to a constructor if the
            // arguments didn't even resolve.
            return;
        }
        ASTIdentifier name = methodInvocation.getIdentifier();

        // 1. Determine the type to search.
        Optional<TypeSymbol> optTypeToSearch = getTypeToSearch(methodInvocation, ctx);
        if (optTypeToSearch.isEmpty()) {
            // Error generated already.
            return;
        }

        // 2. Identify potentially applicable methods.
        TypeSymbol typeToSearch = optTypeToSearch.get();
        Set<ParameterizedSymbol> potentiallyApplicable = getPotentiallyApplicableMethods(
                methodInvocation, typeToSearch);
        if (potentiallyApplicable.isEmpty()) {
            errorSymbolNotFound(name.getLocation(), name.getValue());
            return;
        }

        // 3. Choose most specific method, if one such method exists.
        //    If no one method is maximally specific, ambiguous error.
        List<ParameterizedSymbol> maximallySpecific = getMaximallySpecificMethods(potentiallyApplicable);
        if (maximallySpecific.isEmpty()) {
            throw internalError("Maximally specific filter eliminated all methods!");
        }
        if (maximallySpecific.size() > 1) {
            error(name.getLocation(), "Ambiguous method call - multiple maximally specific methods match.");
            for (ParameterizedSymbol match : maximallySpecific) {
                note(match.getLocation(), "This method matches.");
            }
        }

        // 4. Type of the method invocation is the resolved type of the Result.
        //    (Or the enclosing class type for a Constructor.)
        ParameterizedSymbol resolved = maximallySpecific.get(0);
        // The Result data type was already resolved in early resolution; just
        // set the entity.
        methodInvocation.setResolvedEntity(resolved);

        // 5. Enforce whether the maximally specific method must be shared,
        //    must NOT be shared, or neither.  If the maximally specific method
        //    is shared, then it must be found directly in the type to search,
        //    not in the superclass or superinterface hierarchy.
        //    Shared methods are NOT inherited!
        //    This implies that the only way to call a shared method with a
        //    simple name is to declare the shared method in the same class.
        boolean resolvedToShared = resolved.isShared();
        if (resolvedToShared) {
            if (methodInvocation.isNonsharedContextOnly()) {
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
            if (methodInvocation.isSharedContextOnly()) {
                error(name.getLocation(), "Non-shared method '" + name.getValue() +
                        "' cannot be referenced from a shared context.");
            }
        }
    }

    /**
     * 1. Find the "type to search" for the given <code>MethodInvocation</code>,
     *    if it exists.
     * @param methodInvocation An <code>ASTMethodInvocation</code>.
     * @param ctx A <code>ResolutionContext</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
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

    private Set<ParameterizedSymbol> findMethodsByName(String methodName, TypeSymbol type) {
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
    public Set<ParameterizedSymbol> getPotentiallyApplicableMethods(ASTMethodInvocation methodInvocation,
                TypeSymbol typeToSearch) {
        String methodName = methodInvocation.getIdentifier().getValue();
        // 2.1. Find methods by name.
        Set<ParameterizedSymbol> methods = findMethodsByName(methodName, typeToSearch);

        // 2.2. Find potentially applicable methods.
        return getInvocationConvertible(methodInvocation.getArgumentList(), methods);
    }

    private Set<ParameterizedSymbol> getInvocationConvertible(ASTArgumentList argsList,
                                                              Set<ParameterizedSymbol> methods) {
        Set<ParameterizedSymbol> applicableMethods = new HashSet<>(methods.size());
        for (ParameterizedSymbol method : methods) {
            List<TypeSymbol> args = argsList.getTypedChildren().stream()
                    .map(ASTExpression::getResolvedDataType)
                    .toList();
            List<TypeSymbol> params = method.getParameters().stream()
                    .map(VariableSymbol::getDataType)
                    .toList();
            int numParams = method.numParameters();
            int numArgs = args.size();
            // Methods must have the same arity (number of parameters) as the
            // method invocation.
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
                applicableMethods.add(method);
            }
        }
        return applicableMethods;
    }

    // Invocation conversion is:
    // 1. Identity conversion (match type exactly)
    // 2. Reference widening conversion (argument type is a subtype of
    //    parameter type).
    // 3. Primitive widening conversion, e.g. Integer to Long.  (This does
    //    not exist in Spruce yet, and it cannot exist until a mechanism is
    //    decided upon to represent such an implicit conversion.)
    private boolean isInvocationConvertible(TypeSymbol arg, TypeSymbol param) {
        return arg == param || arg.isSubTypeOf(param);
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
    public List<ParameterizedSymbol> getMaximallySpecificMethods(Set<ParameterizedSymbol> applicableMethods) {
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

    // 3.1. Eliminate method "b" if method "a" is "strictly more specific" than
    //      method "b".  Method "a" is "strictly more specific" than method
    //      "b" if all formal parameter types of method "a" are convertible
    //      to the formal parameter types of method "b" by invocation
    //      conversion.  In case of override-equivalent signatures, if
    //      method "a" overrides method "b", then method "a" is more
    //      specific than method "b".
    private List<ParameterizedSymbol> getStrictlyMoreSpecific(Set<ParameterizedSymbol> applicableMethods) {
        TypesResolver typesResolver = getTypesResolver();
        List<ParameterizedSymbol> maxSpecificMethods = new ArrayList<>(applicableMethods);
        for (int i = 0; i < maxSpecificMethods.size(); i++) {
            for (int j = i + 1; j < maxSpecificMethods.size(); j++) {
                ParameterizedSymbol first = maxSpecificMethods.get(i);
                ParameterizedSymbol second = maxSpecificMethods.get(j);
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
                    maxSpecificMethods.remove(j);
                    // Try index j again; either the end of the list or another method awaits.
                    j--;
                }
                else if (removeFirst) {
                    // Second is strictly more specific than first.
                    maxSpecificMethods.remove(i);
                    // Try index i again; either the end of the list or another method awaits.
                    i--;
                    break;  // out of the "j" for loop.
                }
            }
        }
        return maxSpecificMethods;
    }

    // Returns true if all parameter types in the first list are invocation
    // convertible to their corresponding parameter type in the second list.
    private boolean isMoreSpecific(ParameterizedSymbol first, ParameterizedSymbol second) {
        // At this point the methods are both applicable, so this method
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

    // 3.2. Determine if all maximally specific methods are override-equivalent.
    private boolean areAllOverrideEquivalent(List<ParameterizedSymbol> maxSpecificMethods) {
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

    //
    // Operator Resolution
    //

    /**
     * Resolves symbols in a <code>UnaryExpression</code>.
     * @param unaryExpr An <code>ASTUnaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveUnaryExpression(ASTUnaryExpression unaryExpr, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        ASTValueExpression first = unaryExpr.getFirst();
        exprResolver.resolveValueExpression(first, ctx);
        Optional<TypeSymbol> optSymbol = Optional.ofNullable(first.getResolvedDataType());

        if (optSymbol.isPresent()) {
            TypeSymbol symbol = optSymbol.get();
            TypesResolver typesResolver = getTypesResolver();

            switch (unaryExpr.getOperation()) {
            case EXCLAMATION -> {
                if (typesResolver.isBoolean(symbol, ctx)) {
                    unaryExpr.setResolvedDataType(symbol);
                }
                else {
                    errorUnaryOperator(unaryExpr.getLocation(), EXCLAMATION, symbol);
                }
            }
            case MINUS -> {
                if (typesResolver.isNumeric(symbol, ctx)) {
                    unaryExpr.setResolvedDataType(symbol);
                }
                else {
                    errorUnaryOperator(unaryExpr.getLocation(), MINUS, symbol);
                }
            }
            default -> throw internalError("Can't resolve unexpected unary operator '" +
                           unaryExpr.getOperation() + "'!");
            }
        }
        // Else there was an error resolving the operand already.
    }

    private void errorUnaryOperator(Location loc, TokenType operator, TypeSymbol symbol) {
        error(loc, "Operator '" + operator.getRepresentation() + "' cannot have operand '" +
                symbol.getName() + "'");
    }

    /**
     * Resolves symbols in a <code>BinaryExpression</code>.
     * @param binaryExpr An <code>ASTBinaryExpression</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveBinaryExpression(ASTBinaryExpression binaryExpr, ResolutionContext ctx) {
        ExpressionsResolver exprResolver = getExpressionsResolver();
        ASTValueExpression first = binaryExpr.getFirst();
        ASTValueExpression second = binaryExpr.getSecond();
        exprResolver.resolveValueExpression(first, ctx);
        exprResolver.resolveValueExpression(second, ctx);
        Optional<TypeSymbol> optFirstSymbol = Optional.ofNullable(first.getResolvedDataType());
        Optional<TypeSymbol> optSecondSymbol = Optional.ofNullable(second.getResolvedDataType());

        if (optFirstSymbol.isPresent() && optSecondSymbol.isPresent()) {
            TypeSymbol firstSymbol = optFirstSymbol.get();
            TypeSymbol secondSymbol = optSecondSymbol.get();
            TypesResolver typesResolver = getTypesResolver();
            TypeSymbol bool = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.BOOLEAN_TYPE, ctx);
            TypeSymbol character = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.CHARACTER_TYPE, ctx);
            TypeSymbol integer = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.INTEGER_TYPE, ctx);
            TypeSymbol duble = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.DOUBLE_TYPE, ctx);
            TypeSymbol string = typesResolver.resolveBuiltInDataTypeByName(TypesResolver.STRING_TYPE, ctx);

            TokenType operator = binaryExpr.getOperation();
            switch (operator) {
            // Conditional logical operators.
            case DOUBLE_PIPE, DOUBLE_AMPERSAND -> {
                // boolean, boolean -> boolean.
                if (typesResolver.isBoolean(firstSymbol, ctx) && typesResolver.isBoolean(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(bool);
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // ISA is handled by ExpressionsResolver.resolveIsaExpression.
            // Relational operators.
            case DOUBLE_EQUAL, EXCLAMATION_EQUAL, LESS_THAN, LESS_THAN_OR_EQUAL,
                 GREATER_THAN, GREATER_THAN_OR_EQUAL -> {
                // numeric, numeric -> boolean
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(bool);
                }
                else {
                    // Yes, we can't even use == or != on non-numerics!
                    // Yes, operator overloading will cover this case, eventually.
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // Addition, numerics or strings with anything else (string conversion).
            case PLUS -> {
                // character, character -> character
                // integer, character/integer -> integer with promotion
                // double, numeric -> double with promotion
                // string, any/string -> string with string conversion
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    if (firstSymbol == character && secondSymbol == character) {
                        binaryExpr.setResolvedDataType(character);
                    }
                    else if (firstSymbol == duble || secondSymbol == duble) {
                        binaryExpr.setResolvedDataType(duble);
                        // Handle promotion later.
                    }
                    else {
                        binaryExpr.setResolvedDataType(integer);
                        // Handle promotion later.
                    }
                }
                else if (typesResolver.isString(firstSymbol, ctx) || typesResolver.isString(secondSymbol, ctx)) {
                    binaryExpr.setResolvedDataType(string);
                    // Handle string conversion later.
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            // Subtraction, numerics.
            case MINUS -> {
                // character, character -> character
                // integer, character/integer -> integer
                // double, numeric -> double
                if (typesResolver.isNumeric(firstSymbol, ctx) && typesResolver.isNumeric(secondSymbol, ctx)) {
                    if (firstSymbol == character && secondSymbol == character) {
                        binaryExpr.setResolvedDataType(character);
                    }
                    else if (firstSymbol == duble || secondSymbol == duble) {
                        binaryExpr.setResolvedDataType(duble);
                    }
                    else {
                        binaryExpr.setResolvedDataType(integer);
                    }
                }
                else {
                    errorBinaryOperator(binaryExpr.getLocation(), operator, firstSymbol, secondSymbol);
                }
            }
            default -> throw internalError("Can't resolve unexpected binary operator '" +
                    binaryExpr.getOperation() + "'!");
            }
        }
        // Else there was an error resolving at least one of the operands
        // already.
    }

    private void errorBinaryOperator(Location loc, TokenType operator,
                                     TypeSymbol first, TypeSymbol second) {
        error(loc, "Operator '" + operator.getRepresentation() + "' cannot have operands '" +
                first.getName() + "' and '" + second.getName() + "'");
    }
}
