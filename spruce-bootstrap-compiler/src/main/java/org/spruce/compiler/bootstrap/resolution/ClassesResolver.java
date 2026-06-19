package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTTypeDeclarationList;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;

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
        // Members
        ResolutionContext typeCtx = ctx.withEnclosingSymbol(typeDecl.getDeclSymbol());
        for (ASTMember member : typeDecl.getMembers()) {
            resolveMember(member, typeCtx);
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

        for (ASTVariableDeclarator varDecl : constDecl.getVarDeclList().getTypedChildren()) {
            varDecl.getDeclSymbol().setDataType(dt.getResolvedDataType());
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

        for (ASTVariableDeclarator varDecl : fieldDecl.getVarDeclList().getTypedChildren()) {
            varDecl.getDeclSymbol().setDataType(dt.getResolvedDataType());
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

        methodDecl.getBody().getBlock().ifPresent(
                block -> getStatementsResolver().resolveBlock(block, ctxMethod));
        // TODO: Second analysis pass: Detect method overrides.
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

        methodDecl.getBody().getBlock().ifPresent(
                block -> getStatementsResolver().resolveBlock(block, ctxMethod));
        // TODO: Second analysis pass: Detect method overrides.
    }

    /**
     * Resolves all symbols in a <code>MethodHeader</code>.
     * @param header An <code>ASTMethodHeader</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing method.
     */
    public void resolveMethodHeader(ASTMethodHeader header, ResolutionContext ctx) {
        resolveMethodResult(header.getResult(), ctx);
        resolveFormalParameterList(header.getMethodDecl().getFormalParamList(), ctx);
    }

    /**
     * Resolves all symbols in a <code>Result</code>.
     * @param result An <code>ASTResult</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing method.
     */
    public void resolveMethodResult(ASTResult result, ResolutionContext ctx) {
        if (result.getVoidKeyword().isPresent()) {
            result.setResolvedDataType(TypeSymbol.VOID);
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
     * @param ctx A <code>ResolutionContext</code> representing the enclosing
     *            method or constructor.
     */
    public void resolveFormalParameterList(ASTFormalParameterList formalParams, ResolutionContext ctx) {
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
        resolveFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList(), ctxConstructor);
        getStatementsResolver().resolveBlock(constrDecl.getBlock(), ctxConstructor);
    }
}
