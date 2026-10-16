package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.expressions.ASTExpression;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTTypeDeclarationList;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.spruce.compiler.bootstrap.symbol.TypeSymbol.VOID;

/**
 * A <code>ClassesResolver</code> is a <code>BasicResolver</code> that resolves
 * data types in the classes portion of an AST and Symbol Table:
 * extends, fields, method result types, and formal parameters.
 */
public class ClassesResolver extends BasicResolver {
    /**
     * Constructs a <code>ClassesResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public ClassesResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * 1. Resolve all superclass and superinterface symbols in a
     *    <code>TypeDeclarationList</code>.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveTypeDeclarationListExtends(ASTTypeDeclarationList typeDeclList, ResolutionContext ctx) {
        // Resolve all type declaration superclass/superinterfaces recursively.
        for (ASTTypeDeclaration typeDecl : typeDeclList.getTypedChildren()) {
            resolveTypeDeclarationExtends(typeDecl, ctx);
        }
    }

    /**
     * 2. Detect any dependency cycles in superclass and superinterface
     *    relationships in a <code>TypeDeclarationList</code>.  This must be
     *    done after resolving all superclass and superinterface symbols, but
     *    before resolving symbols in members.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     * @return Whether a dependency cycle was detected for any of the type
     *     declarations in the list.
     */
    public boolean detectDependencyCycles(ASTTypeDeclarationList typeDeclList) {
        boolean cycleDetected = false;
        for (ASTTypeDeclaration typeDecl : typeDeclList.getTypedChildren()) {
            cycleDetected |= detectDependencyCyclesTypeDeclaration(typeDecl);
        }
        return cycleDetected;
    }

    /**
     * 3. Resolve all remaining symbols in a <code>TypeDeclarationList</code>.
     *    Many of these resolutions depend on cycles being detected and
     *    eliminated first.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveTypeDeclarationListMembers(ASTTypeDeclarationList typeDeclList, ResolutionContext ctx) {
        // Resolve all non-type-declaration members, including those members
        // within nested type declarations.
        for (ASTTypeDeclaration typeDecl : typeDeclList.getTypedChildren()) {
            resolveTypeDeclarationMembers(typeDecl, ctx);
        }
    }

    /**
     * 1. Resolve only the superclass and superinterface symbols in a
     *    <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveTypeDeclarationExtends(ASTTypeDeclaration typeDecl, ResolutionContext ctx) {
        switch (typeDecl) {
        case ASTClassDeclaration classDecl ->
                resolveClassDeclarationExtends(classDecl, ctx);
        case ASTInterfaceDeclaration interfaceDecl ->
                resolveInterfaceDeclarationExtends(interfaceDecl, ctx);
        }

        // Nested types.
        ResolutionContext classCtx = ctx.withEnclosingSymbol(typeDecl.getDeclSymbol());
        for (ASTMember member : typeDecl.getMembers()) {
            if (member instanceof ASTTypeDeclaration nested) {
                resolveTypeDeclarationExtends(nested, classCtx);
            }
        }
    }

    /**
     * Resolve only the superclass and superinterface symbols in a
     * <code>ClassDeclaration</code>.
     * @param classDecl An <code>ASTClassDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveClassDeclarationExtends(ASTClassDeclaration classDecl, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        TypeSymbol declSymbol = classDecl.getDeclSymbol();

        // Superclass
        Optional<ASTDataTypeNoArray> optDtnaSuperclass = classDecl.getSuperclass();
        Optional<TypeSymbol> optSuperclass = Optional.empty();
        if (optDtnaSuperclass.isPresent()) {
            ASTDataTypeNoArray dtnaSuperclass = optDtnaSuperclass.get();
            typesResolver.resolveDataTypeNoArray(dtnaSuperclass, ctx);
            optSuperclass = Optional.ofNullable(dtnaSuperclass.getResolvedDataType());

            if (optSuperclass.isPresent()) {
                TypeSymbol superclass = optSuperclass.get();
                Symbol.Kind kind = superclass.getKind();
                if (kind != Symbol.Kind.CLASS) {
                    error(dtnaSuperclass.getLocation(), "Class cannot extend " +
                            kind.toString().toLowerCase());
                }
            }
        }
        if (optSuperclass.isEmpty()) {
            // If an explicit superclass is not specified, then assume that the
            // superclass is the root of the type hierarchy.
            optSuperclass = Optional.ofNullable(typesResolver.resolveRootType(ctx));

            // If the class being resolved _is_ the root class, then that root
            // class has no superclass!
            if (optSuperclass.isPresent() && optSuperclass.get() == declSymbol) {
                optSuperclass = Optional.empty();
            }
        }
        optSuperclass.ifPresent(declSymbol::setSuperclass);

        // Superinterfaces
        Optional<ASTDataTypeNoArrayList> optSuperinterfaces = classDecl.getSuperinterfaces();
        if (optSuperinterfaces.isPresent()) {
            resolveSuperinterfaces(declSymbol, optSuperinterfaces.get(), ctx);
        }
    }

    /**
     * Resolve only the superinterface symbols in an <code>InterfaceDeclaration</code>.
     * @param interfaceDecl An <code>ASTInterfaceDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveInterfaceDeclarationExtends(ASTInterfaceDeclaration interfaceDecl, ResolutionContext ctx) {
        TypeSymbol declSymbol = interfaceDecl.getDeclSymbol();

        // Superinterfaces
        Optional<ASTDataTypeNoArrayList> optSuperinterfaces = interfaceDecl.getExtendsInterfaces();
        if (optSuperinterfaces.isPresent()) {
            resolveSuperinterfaces(declSymbol, optSuperinterfaces.get(), ctx);
        }
    }

    private void resolveSuperinterfaces(TypeSymbol declSymbol, ASTDataTypeNoArrayList superinterfaces,
                                        ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        // Superinterfaces
        List<TypeSymbol> resolvedInterfaces = declSymbol.getSuperinterfaces();

        for (ASTDataTypeNoArray dtnaSuperinterface : superinterfaces.getTypedChildren()) {
            typesResolver.resolveDataTypeNoArray(dtnaSuperinterface, ctx);

            Optional<TypeSymbol> optSuperinterface = Optional.ofNullable(dtnaSuperinterface.getResolvedDataType());
            if (optSuperinterface.isPresent()) {
                TypeSymbol superinterface = optSuperinterface.get();

                Symbol.Kind kind = superinterface.getKind();
                if (kind != Symbol.Kind.INTERFACE) {
                    switch(declSymbol.getKind()) {
                    case CLASS -> error(dtnaSuperinterface.getLocation(), "Class cannot implement " +
                            kind.toString().toLowerCase());
                    case INTERFACE -> error(dtnaSuperinterface.getLocation(), "Interface cannot extend " +
                            kind.toString().toLowerCase());
                    }
                }

                // No duplicates.
                if (resolvedInterfaces.contains(superinterface)) {
                    error(superinterface.getLocation(), "Duplicate superinterface");
                }
                else {
                    declSymbol.addSuperinterface(superinterface);
                }
            }
        }
    }

    /**
     * 2. Detect any dependency cycles in superclass, superinterfaces, and
     *    enclosing type relationships in a <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @return Whether a dependency cycle was detected involving the given symbol.
     */
    public boolean detectDependencyCyclesTypeDeclaration(ASTTypeDeclaration typeDecl) {
        TypeSymbol declSymbol = typeDecl.getDeclSymbol();
        return detectDependencyCycle(declSymbol, declSymbol, new ArrayList<>());
    }

    private boolean detectDependencyCycle(TypeSymbol original, TypeSymbol curr, List<TypeSymbol> seen) {
        boolean cycleDetected = false;
        // Superclass
        if (curr.getSuperclass().isPresent()) {
            TypeSymbol superclass = curr.getSuperclass().get();
            cycleDetected = checkForDependency(original, superclass, seen);
        }
        // Superinterfaces
        for (TypeSymbol superinterface : curr.getSuperinterfaces()) {
            cycleDetected |= checkForDependency(original, superinterface, seen);
        }
        // Enclosing type.
        Optional<TypeSymbol> optEnclosingType = curr.getEnclosingType();
        if (optEnclosingType.isPresent()) {
            TypeSymbol enclosingType = optEnclosingType.get();
            cycleDetected |= checkForDependency(original, enclosingType, seen);
        }

        return cycleDetected;
    }

    private boolean checkForDependency(TypeSymbol original, TypeSymbol curr, List<TypeSymbol> seen) {
        boolean cycleDetected = false;
        if (original == curr) {
            error(original.getLocation(), "Dependency cycle detected on " + original.getName());
            cycleDetected = true;
        }
        else if (!seen.contains(curr)) {
            seen.add(curr);
            cycleDetected = detectDependencyCycle(original, curr, seen);
        }
        return cycleDetected;
    }

    /**
     * 3. Resolve all non-type-declaration symbols in a <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTClassDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveTypeDeclarationMembers(ASTTypeDeclaration typeDecl, ResolutionContext ctx) {
        // No duplicate simple names in enclosing type hierarchy.  Else, inner
        // would shadow outer.  Qualified this would be ambiguous.
        String name = typeDecl.getName().getValue();
        TypeSymbol curr = typeDecl.getDeclSymbol();
        Optional<TypeSymbol> optEnclosing = curr.getEnclosingType();
        while (optEnclosing.isPresent()) {
            TypeSymbol enclosing = optEnclosing.get();
            if (name.equals(enclosing.getName())) {
                error(typeDecl.getLocation(), "Type declaration cannot have same simple name as enclosing type.");
            }
            optEnclosing = enclosing.getEnclosingType();
        }
        // Members
        ResolutionContext typeCtx = ctx.withEnclosingSymbol(typeDecl.getDeclSymbol());
        for (ASTMember member : typeDecl.getMembers()) {
            resolveMember(member, typeCtx);
        }
        // Override, inheritance, and return-type-subsitutability checks.
        // This will supersede my previously written checkOverride methods.
        checkOverrideInheritance(typeDecl);
    }

    /**
     * Checks that all inherited methods that are override-equivalent with each
     * other have a method that is return-type-subsitutable for all other such
     * methods.  Enforces override and return-type-subsitutability if that
     * method is on this type declaration.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     */
    public void checkOverrideInheritance(ASTTypeDeclaration typeDecl) {
        TypeSymbol type = typeDecl.getDeclSymbol();
        // 1. Get ALL inherited methods and declared methods for this type,
        //    from throughout the superclass and superinterface hierarchy.
        List<ParameterizedSymbol> methods = getInheritedAndDeclaredMethods(type);

        // 2. Group them into groups of methods that are all override-equivalent
        //    with each other.
        List<List<ParameterizedSymbol>> overrideEquivalentGroups = groupOverrideEquivalentMethods(methods);

        // 3. For each group of override-equivalent methods, enforce override,
        //    covariant return types, abstract, conflicting return types on
        //    inherited methods.
        for (List<ParameterizedSymbol> group : overrideEquivalentGroups) {
            checkOverrideEquivalentGroup(group, type);
        }
    }

    /**
     * 1. Returns a list of all inherited methods and all declared methods on the
     *    given <code>TypeDeclaration</code>, including the superclass and
     *    superinterface hierarchy.
     * @param type A <code>TypeSymbol</code>.
     * @return A <code>List</code> of <code>ParameterizedSymbol</code>s.
     */
    public List<ParameterizedSymbol> getInheritedAndDeclaredMethods(TypeSymbol type) {
        List<ParameterizedSymbol> methods = new ArrayList<>(type.getTable().getAllMethods());

        // Walk up superclass hierarchy then superinterface hierarchy looking
        // for all methods.
        if (type.getSuperclass().isPresent()) {
            TypeSymbol superclass = type.getSuperclass().get();
            methods.addAll(getInheritedAndDeclaredMethods(superclass));
        }

        methods.addAll(getInheritedAndDeclaredMethodsInSuperinterfaces(type.getSuperinterfaces()));
        return methods;
    }

    private List<ParameterizedSymbol> getInheritedAndDeclaredMethodsInSuperinterfaces(
            List<TypeSymbol> superinterfaces) {
        List<ParameterizedSymbol> methods = new ArrayList<>();
        for (TypeSymbol superinterface : superinterfaces) {
            ChildSymbolTable child = superinterface.getTable();
            methods.addAll(child.getAllMethods());
            methods.addAll(getInheritedAndDeclaredMethodsInSuperinterfaces(superinterface.getSuperinterfaces()));
        }
        return methods;
    }

    /**
     * 2. Group the given methods into groups of methods that are all
     *    override-equivalent with each other.
     * @param methods A <code>List</code> of <code>ParameterizedSymbol</code>s.
     * @return A <code>List</code> of <code>List</code>s of <code>ParameterizedSymbol</code>s.
     *         Each inner list contains only methods that are override-equivalent
     *         with each other.  Methods that aren't override-equivalent with
     *         any other method will be the only methods in their own list.
     */
    public List<List<ParameterizedSymbol>> groupOverrideEquivalentMethods(List<ParameterizedSymbol> methods) {
        List<List<ParameterizedSymbol>> groups = new ArrayList<>();

        // Yes, this is O(n^2).  Make it better if performance is an issue!
        for (ParameterizedSymbol method : methods) {
            boolean added = false;
            for (List<ParameterizedSymbol> group : groups) {
                ParameterizedSymbol candidate = group.get(0);
                if (isOverrideEquivalent(method, candidate)) {
                    group.add(method);
                    added = true;
                    break;
                }
            }
            if (!added) {
                // Most of the time, one method might be override-equivalent
                // with one other method.
                List<ParameterizedSymbol> group = new ArrayList<>(2);
                group.add(method);
                groups.add(group);
            }
        }

        return groups;
    }

    /**
     * 3. For each group of override-equivalent methods:
     *    1. At least one method must be return-type-substitutable for all
     *       other methods in that group.  If such a method is in the current
     *       type declaration, then it must be one that is
     *       return-type-substitutable for all other methods in that group.
     *    2. Enforce override.  If present, there must be at least one
     *       other method in the group.  If not present, there must NOT be
     *       any other methods in the group.
     */
    private void checkOverrideEquivalentGroup(List<ParameterizedSymbol> group, TypeSymbol type) {
        List<ParameterizedSymbol> rts = new ArrayList<>(group.size());
        ParameterizedSymbol declaredInThisType = null;
        boolean addedThisTypeToRts = false;
        for (ParameterizedSymbol candidate : group) {
            boolean fromThisType = false;
            if (((ChildSymbolTable) candidate.getParent()).getParent() == type) {
                declaredInThisType = candidate;
                fromThisType = true;
            }

            // Is it return-type-substitutable for every other override-equivalent method?
            boolean isRts = true;
            for (ParameterizedSymbol other : group) {
                TypeSymbol otherType = other.getDataType();
                if (otherType != null && !candidate.getDataType().isSubTypeOf(otherType)) {
                    isRts = false;
                    break;
                }
            }
            if (isRts) {
                rts.add(candidate);
                if (fromThisType) {
                    addedThisTypeToRts = true;
                }
            }
        }

        // 1. At least one method must be return-type-substitutable for all
        //    other methods in that group. If such a method is in the current
        //    type declaration, then it must be one that is
        //    return-type-substitutable for all other methods in that group.
        if (declaredInThisType != null && !addedThisTypeToRts) {
            // Overriding method is not RTS with all others.
            // This enforces return type covariance.
            error(declaredInThisType.getLocation(), "Return type incompatible with overridden method.");
            for (ParameterizedSymbol other : group) {
                if (other != null && other != declaredInThisType) {
                    note(other.getLocation(), "Other overridden method with return type.");
                }
            }
        }
        if (declaredInThisType == null && rts.isEmpty()) {
            // Inherited methods are not RTS with each other.
            error(type.getLocation(), "Inherited methods with signature '" + group.get(0).getName() +
                    "' are not return type substitutable.");
            for (ParameterizedSymbol other : group) {
                note(other.getLocation(), "Inherited method with return type.");
            }
        }
        if (declaredInThisType != null) {
            // 2. Enforce override.  If present, there must be at least one other
            //    method in the group.  If not present, there must NOT be any other
            //    methods in the group.
            checkOverride(declaredInThisType, group);
        }
    }

    private void checkOverride(ParameterizedSymbol method, List<ParameterizedSymbol> group) {
        if (method.isShared()) {
            if (method.isOverride()) {
                error(method.getLocation(), "Method cannot be both 'shared' and 'override'.");
            }
            for (ParameterizedSymbol overridden : group) {
                if (overridden == method) {
                    continue;
                }
                if (!overridden.isShared()) {
                    error(method.getLocation(), "Shared method cannot override non-shared method.");
                    note(overridden.getLocation(), "Non-shared method being overridden.");
                }
            }
            // Shared methods are NOT inherited, so there is no method hiding here.
            // A shared method that would hide a supertype's shared method can
            // only be called with a simple name or that class's name.
            // sharedMethod(); or Subclass.sharedMethod();
            // To call a shared method that would have been hidden, the
            // superclass name is thus required.   Superclass.sharedMethod();
        }
        else {
            if (method.isOverride()) {
                if (group.size() > 1) {
                    for (ParameterizedSymbol overridden : group) {
                        if (overridden == method) {
                            continue;
                        }
                        if (overridden.isShared()) {
                            error(method.getLocation(), "Non-shared method cannot override shared method.");
                            note(overridden.getLocation(), "Shared method being overridden.");
                        }

                        // Return type covariance is checked with checking
                        // override-equivalent groups.

                        // Keep track of all overridden methods for later.
                        method.addOverridden(overridden);
                    }
                }
                else {
                    error(method.getLocation(), "Method does not override method from supertype.  Ensure it overrides a method or remove 'override'.");
                }
            }
            else if (group.size() > 1) {
                error(method.getLocation(), "Method overrides method from supertype.  Add 'override' or ensure it does not override method.");
                for (ParameterizedSymbol overridden : group) {
                    if (overridden == method) {
                        continue;
                    }
                    method.addOverridden(overridden);
                    note(overridden.getLocation(), "Overridden method.");
                }
            }
        }
    }

    /**
     * Resolve all symbols in a <code>Member</code>.
     * @param member An <code>ASTMember</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveMember(ASTMember member, ResolutionContext ctx) {
        switch(member) {
        case ASTTypeDeclaration typeDecl -> resolveTypeDeclarationMembers(typeDecl, ctx);
        case ASTConstantDeclaration constDecl -> resolveConstantDeclaration(constDecl, ctx);
        case ASTConstructorDeclaration constrDecl -> resolveConstructorDeclaration(constrDecl, ctx);
        case ASTFieldDeclaration fieldDecl -> resolveFieldDeclaration(fieldDecl, ctx);
        case ASTInterfaceMethodDeclaration iMethodDecl -> resolveInterfaceMethodDeclaration(iMethodDecl, ctx);
        case ASTMethodDeclaration methodDecl -> resolveMethodDeclaration(methodDecl, ctx);
        }
    }

    /**
     * Resolve all symbols in a <code>ConstantDeclaration</code>.  All
     * <code>VariableDeclarator</code>s will be resolved to whatever the
     * <code>DataType</code> resolves to.
     * @param constDecl An <code>ASTConstantDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveConstantDeclaration(ASTConstantDeclaration constDecl, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        ASTDataType dt = constDecl.getDataType();
        typesResolver.resolveDataType(dt, ctx);
        TypeSymbol targetType = dt.getResolvedDataType();

        for (ASTVariableDeclarator varDecl : constDecl.getVarDeclList().getTypedChildren()) {
            varDecl.getDeclSymbol().setDataType(targetType);

            // All constants must have an initializer.
            if (varDecl.getVarInitializer().isEmpty()) {
                error(varDecl.getLocation(), "Constant declaration must be initialized.");
                return;
            }

            ASTExpression initializer = varDecl.getVarInitializer().get();
            getExpressionsResolver().resolveExpression(initializer, ctx);

            TypeSymbol exprType = initializer.getResolvedDataType();
            if (targetType != null && exprType != null && !getStatementsResolver().isAssignmentConvertible(exprType, targetType)) {
                errorTypeMismatch(initializer.getLocation(), targetType.getFullyQualifiedName(), exprType.getFullyQualifiedName());
            }
            varDecl.getDeclSymbol().setConstantValue(initializer.isConstantExpression());
        }
    }

    /**
     * Resolve all symbols in a <code>FieldDeclaration</code>.  All
     * <code>VariableDeclarator</code>s will be resolved to whatever the
     * <code>DataType</code> resolves to.
     * @param fieldDecl An <code>ASTFieldDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveFieldDeclaration(ASTFieldDeclaration fieldDecl, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        ASTDataType dt = fieldDecl.getDataType();
        typesResolver.resolveDataType(dt, ctx);
        TypeSymbol targetType = dt.getResolvedDataType();

        for (ASTVariableDeclarator varDecl : fieldDecl.getVarDeclList().getTypedChildren()) {
            varDecl.getDeclSymbol().setDataType(targetType);

            if (varDecl.getVarInitializer().isPresent()) {
                ASTExpression initializer = varDecl.getVarInitializer().get();
                getExpressionsResolver().resolveExpression(initializer, ctx);

                TypeSymbol exprType = initializer.getResolvedDataType();
                if (targetType != null && exprType != null && !getStatementsResolver().isAssignmentConvertible(exprType, targetType)) {
                    errorTypeMismatch(initializer.getLocation(), targetType.getFullyQualifiedName(), exprType.getFullyQualifiedName());
                }
                // Really should require final and immutable, but that's what constant is (so far).
                VariableSymbol varSymbol = varDecl.getDeclSymbol();
                varSymbol.setConstantValue(varSymbol.isShared() && varSymbol.isFinal() && initializer.isConstantExpression());
            }
        }
    }

    /**
     * Resolve all symbols in an <code>InterfaceMethodDeclaration</code>.
     * @param methodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveInterfaceMethodDeclaration(ASTInterfaceMethodDeclaration methodDecl, ResolutionContext ctx) {
        ParameterizedSymbol method = methodDecl.getDeclSymbol();
        ResolutionContext ctxMethod = ctx.withEnclosingSymbol(method);
        resolveMethodHeader(methodDecl.getHeader(), ctxMethod);
        String methodName = methodDecl.getHeader().getMethodDecl().getName().getValue();

        TypeSymbol targetType = methodDecl.getHeader().getResult().getResolvedDataType();
        methodDecl.getBody().getBlock().ifPresent(
                block -> getStatementsResolver().resolveBlock(block, targetType, ctxMethod));
        checkInterferenceWithRootClass(method, methodName, ctx);
    }

    /**
     * If the interface method is override-equivalent to a (public) method in
     * the root class:
     * 1. It cannot be shared.
     * 2. It must be abstract.
     * 3. It cannot change the return type, even covariantly.
     */
    private void checkInterferenceWithRootClass(ParameterizedSymbol interfaceMethod, String methodName, ResolutionContext ctx) {
        TypeSymbol root = getTypesResolver().resolveRootType(ctx);
        Optional<ParameterizedSymbol> optRootMethod = findMethodOnType(interfaceMethod, methodName, root);
        if (optRootMethod.isPresent()) {
            ParameterizedSymbol rootMethod = optRootMethod.get();
            if (!rootMethod.isShared()) {
                // Right now, "shared" is the only non-abstract interface method modifier.
                if (interfaceMethod.isShared()) {
                    error(interfaceMethod.getLocation(), "Shared interface method cannot override method in '" + root.getFullyQualifiedName() + "'");
                }

                // No return type covariance here.  Return type must match exactly.
                // Or else this interface would force an implementation to have
                // two methods, this one, and the root class one, that are mutually
                // incompatible.
                if (interfaceMethod.getDataType() != rootMethod.getDataType()) {
                    error(interfaceMethod.getLocation(), "Incompatible return type with method in " + root.getFullyQualifiedName());
                }
            }
        }
    }

    /**
     * Resolve all symbols in a <code>MethodDeclaration</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveMethodDeclaration(ASTMethodDeclaration methodDecl, ResolutionContext ctx) {
        ParameterizedSymbol method = methodDecl.getDeclSymbol();
        boolean isShared = methodDecl.getDeclSymbol().isShared();
        ResolutionContext ctxMethod = ctx.withEnclosingSymbol(method, isShared);
        resolveMethodHeader(methodDecl.getHeader(), ctxMethod);

        TypeSymbol targetType = methodDecl.getHeader().getResult().getResolvedDataType();
        methodDecl.getBody().getBlock().ifPresent(
                block -> getStatementsResolver().resolveBlock(block, targetType, ctxMethod));
    }

    private Optional<ParameterizedSymbol> findMethodOnType(ParameterizedSymbol method, String methodName, TypeSymbol type) {
        for (ParameterizedSymbol candidate : type.getTable().getMethodsForName(methodName)) {
            List<TypeSymbol> methodParams = method.getParameters().stream()
                    .map(VariableSymbol::getDataType)
                    .toList();
            List<TypeSymbol> candidateParams = candidate.getParameters().stream()
                    .map(VariableSymbol::getDataType)
                    .toList();
            boolean paramsMatch = true;
            if (methodParams.size() != candidateParams.size()) {
                continue;
            }
            for (int i = 0; i < methodParams.size(); i++) {
                if (methodParams.get(i) != candidateParams.get(i)) {
                    paramsMatch = false;
                    break;
                }
            }
            if (paramsMatch) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves all symbols in a <code>MethodHeader</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing method.
     */
    public void resolveMethodHeader(ASTMethodHeader header, ResolutionContext ctx) {
        resolveMethodResult(header.getResult(), ctx);
        resolveFormalParameterList(header.getMethodDecl().getFormalParamList());
    }

    /**
     * Resolves all symbols in a <code>Result</code>.
     * @param result An <code>ASTResult</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing method.
     */
    public void resolveMethodResult(ASTResult result, ResolutionContext ctx) {
        if (result.getVoidKeyword().isPresent()) {
            result.setResolvedDataType(VOID);
        }
        else if (result.getDataType().isPresent()){
            ASTDataType dt = result.getDataType().get();
            getTypesResolver().resolveDataType(dt, ctx);
            result.setResolvedDataType(dt.getResolvedDataType());
        }
        else {
            throw internalError("Neither void nor data type present on method result!");
        }
    }

    /**
     * Resolves all symbols in a <code>FormalParameterList</code>.
     * @param formalParams An <code>ASTFormalParameterList</code>.
     */
    public void resolveFormalParameterList(ASTFormalParameterList formalParams) {
        for (ASTFormalParameter formalParam : formalParams.getTypedChildren()) {
            ASTDataType dt = formalParam.getDataType();
            // The data type was already resolved early; don't resolve again.
            // If there was a symbol not found error, resolving again would
            // duplicate the error!
            formalParam.getDeclSymbol().setDataType(dt.getResolvedDataType());
        }
    }

    /**
     * Resolves all symbols in a <code>ConstructorDeclaration</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveConstructorDeclaration(ASTConstructorDeclaration constrDecl, ResolutionContext ctx) {
        // Constructors are always in a non-shared context.
        ResolutionContext ctxConstructor = ctx.withEnclosingSymbol(
                constrDecl.getDeclSymbol(), false);
        resolveFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList());

        // Constructors don't have a return type, but it's effectively void.
        getStatementsResolver().resolveBlock(constrDecl.getBlock(), VOID, ctxConstructor);
    }
}
