package org.spruce.compiler.bootstrap.analysis;

import java.util.ArrayList;
import java.util.List;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.ast.toplevel.ASTTypeDeclarationList;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

/**
 * A <code>ClassesAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * the data flow within type declarations.
 */
public class ClassesAnalyzer extends BasicAnalyzer {

    /**
     * Constructs a <code>ClassesAnalyzer</code>.
     * @param analyzer An <code>Analyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ClassesAnalyzer(Analyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyze all type declarations in a <code>TypeDeclarationList</code>.
     * @param typeDeclList An <code>ASTTypeDeclarationList</code>.
     */
    public void analyzeTypeDeclarationList(ASTTypeDeclarationList typeDeclList) {
        for (ASTTypeDeclaration typeDecl : typeDeclList.getTypedChildren()) {
            analyzeTypeDeclaration(typeDecl);
        }
    }

    /**
     * Analyze the <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     */
    public void analyzeTypeDeclaration(ASTTypeDeclaration typeDecl) {
        // Get a list of all field variable symbols whose declarations have an
        // initializer.  Include the constants.
        List<VariableSymbol> fieldsWithInitializers = new ArrayList<>();
        for (ASTMember member : typeDecl.getMembers()) {
            if (member instanceof ASTFieldDeclaration fieldDecl) {
                for (ASTVariableDeclarator field : fieldDecl.getVarDeclList().getTypedChildren()) {
                    field.getVarInitializer().ifPresent(_ -> fieldsWithInitializers.add(field.getDeclSymbol()));
                }
            }
            else if (member instanceof ASTConstantDeclaration constantDecl) {
                for (ASTVariableDeclarator constant : constantDecl.getVarDeclList().getTypedChildren()) {
                    constant.getVarInitializer().ifPresent(_ -> fieldsWithInitializers.add(constant.getDeclSymbol()));
                }
            }
        }

        for (ASTMember member : typeDecl.getMembers()) {
            analyzeMember(member, fieldsWithInitializers);
        }
    }

    /**
     * Analyze the <code>Member</code>.
     * @param member An <code>ASTMember</code>.
     * @param fieldsWithInitializers A <code>List</code> of <code>VariableSymbol</code>
     *                               representing fields declared with an initializer.
     */
    public void analyzeMember(ASTMember member, List<VariableSymbol> fieldsWithInitializers) {
        switch (member) {
        case ASTTypeDeclaration typeDecl -> analyzeTypeDeclaration(typeDecl);
        case ASTConstructorDeclaration constrDecl -> analyzeConstructorDeclaration(constrDecl, fieldsWithInitializers);
        case ASTInterfaceMethodDeclaration interfaceMethodDecl -> analyzeInterfaceMethodDeclaration(interfaceMethodDecl);
        case ASTMethodDeclaration methodDecl -> analyzeMethodDeclaration(methodDecl);

        // The members that need to be analyzed go above.
        default -> {}
        }
    }

    /**
     * Analyze the <code>ConstructorDeclaration</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param fieldsWithInitializers A <code>List</code> of <code>VariableSymbol</code>
     *                               representing fields declared with an initializer.
     */
    public void analyzeConstructorDeclaration(ASTConstructorDeclaration constrDecl,
                                              List<VariableSymbol> fieldsWithInitializers) {
        AnalysisContext ctx = AnalysisContext.ofConstructor(fieldsWithInitializers);
        analyzeFormalParameters(constrDecl.getConstructorDecl().getFormalParamList(), ctx);
        getStatementsAnalyzer().analyzeBlock(constrDecl.getBlock(), ctx);
    }

    /**
     * Analyze the <code>InterfaceMethodDeclaration</code>.
     * @param interfaceMethodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public void analyzeInterfaceMethodDeclaration(ASTInterfaceMethodDeclaration interfaceMethodDecl) {
        boolean isVoidPresent = interfaceMethodDecl.getHeader().getResult().getVoidKeyword().isPresent();
        AnalysisContext ctx = AnalysisContext.ofMethod(isVoidPresent);
        analyzeFormalParameters(interfaceMethodDecl.getHeader().getMethodDecl().getFormalParamList(), ctx);
        analyzeMethodBody(interfaceMethodDecl.getBody(), ctx);
    }

    /**
     * Analyze the <code>MethodDeclaration</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     */
    public void analyzeMethodDeclaration(ASTMethodDeclaration methodDecl) {
        boolean isVoidPresent = methodDecl.getHeader().getResult().getVoidKeyword().isPresent();
        AnalysisContext ctx = AnalysisContext.ofMethod(isVoidPresent);
        analyzeFormalParameters(methodDecl.getHeader().getMethodDecl().getFormalParamList(), ctx);
        analyzeMethodBody(methodDecl.getBody(), ctx);
    }

    /**
     * Analyzes the <code>FormalParameterList</code>.  All formal parameters
     * are considered already initialized local variables.
     * @param formalParamList An <code>ASTFormalParameterList</code>.
     */
    public void analyzeFormalParameters(ASTFormalParameterList formalParamList, AnalysisContext ctx) {
        List<VariableSymbol> initializedLocalVars = ctx.initializedLocalVars();
        for (ASTFormalParameter formalParam : formalParamList.getTypedChildren()) {
            initializedLocalVars.add(formalParam.getDeclSymbol());
        }
    }

    /**
     * Analyze the <code>MethodBody</code>.
     * @param methodBody An <code>ASTMethodBody</code>.
     * @param ctx The enclosing <code>AnalysisContext</code>.
     */
    public void analyzeMethodBody(ASTMethodBody methodBody, AnalysisContext ctx) {
        methodBody.getBlock().ifPresent(block ->
                getStatementsAnalyzer().analyzeBlock(block, ctx));
    }
}
