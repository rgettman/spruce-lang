package org.spruce.compiler.bootstrap.resolution;

import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
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
     * Resolve all symbols in a <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveTypeDeclaration(ASTTypeDeclaration typeDecl, ResolutionContext ctx) {
        switch (typeDecl) {
        case ASTClassDeclaration classDecl ->
            resolveClassDeclaration(classDecl, ctx);
        }
    }

    /**
     * Resolve all symbols in a <code>ClassDeclaration</code>, including any
     * <code>extends</code> clause, and all class parts.
     * @param classDecl An <code>ASTClassDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing a namespace if
     *            this type declaration is top-level, or the enclosing type if
     *            this type declaration is nested.
     */
    public void resolveClassDeclaration(ASTClassDeclaration classDecl, ResolutionContext ctx) {
        TypesResolver typesResolver = getTypesResolver();
        Optional<ASTDataTypeNoArray> optDtnaSuperclass = classDecl.getSuperclass();
        Optional<TypeSymbol> superclass = Optional.empty();
        if (optDtnaSuperclass.isPresent()) {
            ASTDataTypeNoArray dtnaSuperclass = optDtnaSuperclass.get();
            typesResolver.resolveDataTypeNoArray(dtnaSuperclass, ctx);
            superclass = Optional.ofNullable(dtnaSuperclass.getResolvedDataType());

            // Ensure no superclass loop, e.g. A -> B -> A.
            // Can't do that until all types in all OCUs have had all symbols
            // resolved.
            // Loop over all types in all OCUs after this initial resolution.
            // TODO: Will place in a second pass over all types in all OCUs.
            // This will be in the semantic analysis phase.
        }
        if (superclass.isEmpty()) {
            // If an explicit superclass is not specified, then assume that the
            // superclass is the root of the type hierarchy.
            superclass = Optional.ofNullable(typesResolver.resolveRootType(ctx));

            // If the class being resolved _is_ the root class, then that root
            // class has no superclass!
            if (superclass.isPresent() && superclass.get() == classDecl.getDeclSymbol()) {
                superclass = Optional.empty();
            }
        }

        superclass.ifPresent(st -> classDecl.getDeclSymbol().setSupertype(st));

        ResolutionContext classCtx = ctx.withEnclosingSymbol(classDecl.getDeclSymbol());
        for (ASTMember member : classDecl.getMembers()) {
            resolveMember(member, classCtx);
        }
    }

    /**
     * Resolve all symbols in a <code>Member</code>.
     * @param member An <code>ASTMember</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveMember(ASTMember member, ResolutionContext ctx) {
        switch(member) {
        case ASTTypeDeclaration typeDecl -> resolveTypeDeclaration(typeDecl, ctx);
        case ASTFieldDeclaration fieldDecl -> resolveFieldDeclaration(fieldDecl, ctx);
        case ASTMethodDeclaration methodDecl -> resolveMethodDeclaration(methodDecl, ctx);
        case ASTConstructorDeclaration constrDecl -> resolveConstructorDeclaration(constrDecl, ctx);
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
     * Resolve all symbols in a <code>MethodDeclaration</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveMethodDeclaration(ASTMethodDeclaration methodDecl, ResolutionContext ctx) {
        ParameterizedSymbol method = methodDecl.getDeclSymbol();
        ResolutionContext ctxMethod = ctx.withEnclosingSymbol(method);
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
        TypesResolver typesResolver = getTypesResolver();
        for (ASTFormalParameter formalParam : formalParams.getTypedChildren()) {
            ASTDataType dt = formalParam.getDataType();
            typesResolver.resolveDataType(dt, ctx);
            formalParam.getDeclSymbol().setDataType(dt.getResolvedDataType());
        }
    }

    /**
     * Resolves all symbols in a <code>ConstructorDeclaration</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param ctx A <code>ResolutionContext</code> representing the enclosing type.
     */
    public void resolveConstructorDeclaration(ASTConstructorDeclaration constrDecl, ResolutionContext ctx) {
        ResolutionContext ctxConstructor = ctx.withEnclosingSymbol(
                constrDecl.getDeclSymbol());
        resolveFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList(), ctxConstructor);
        getStatementsResolver().resolveBlock(constrDecl.getBlock(), ctxConstructor);
    }
}
