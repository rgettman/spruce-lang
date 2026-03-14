package org.spruce.compiler.bootstrap.symbol;

import java.util.List;
import java.util.stream.Collectors;

import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclarator;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.TokenType;

import static org.spruce.compiler.bootstrap.symbol.Symbol.*;

/**
 * A <code>ClassesSymbolCreator</code> is a <code>BasicSymbolCreator</code>
 * that creates symbols belonging to class level AST elements.
 */
public class ClassesSymbolCreator extends BasicSymbolCreator {
    /**
     * Constructs a <code>ClassesSymbolCreator</code>.
     * @param symbolCreator A <code>SymbolCreator</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ClassesSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer) {
        super(symbolCreator, msgProducer);
    }

    /**
     * Creates a <code>Symbol</code> for a top-level <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForTopLevelTypeDeclaration(ASTTypeDeclaration typeDecl, SymbolTable parent) {
        createSymbolsForTypeDeclaration(typeDecl, parent, false);
    }

    /**
     * Creates a <code>Symbol</code> for a nested <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForNestedTypeDeclaration(ASTTypeDeclaration typeDecl, SymbolTable parent) {
        createSymbolsForTypeDeclaration(typeDecl, parent, true);
    }

    /**
     * Creates a <code>Symbol</code> for a <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param isNested Whether the <code>TypeDeclaration</code> is nested with
     *                 another <code>TypeDeclaration</code>.
     */
    public void createSymbolsForTypeDeclaration(ASTTypeDeclaration typeDecl, SymbolTable parent, boolean isNested) {
        long flags = getFlags(typeDecl);

        Symbol.Type type;
        switch (typeDecl) {
            case ASTClassDeclaration ignored ->
                type = Symbol.Type.CLASS;
        }

        ParentSymbol symbol = new ParentSymbol(typeDecl.getLocation(), typeDecl.getName().getValue(),
                type, parent, flags);
        insertSymbol(parent, symbol);
        typeDecl.setDeclSymbol(symbol);

        ChildSymbolTable table = createSymbolTableForTypeDeclaration(typeDecl, parent);
        symbol.setTable(table);
    }

    /**
     * Creates and returns a child symbol table for a <code>TypeDeclaration</code>.
     * Creates child symbols and populates them in the symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>SymbolTable</code>.
     * @return A <code>ChildSymbolTable</code>.
     */
    public ChildSymbolTable createSymbolTableForTypeDeclaration(ASTTypeDeclaration typeDecl, SymbolTable parent) {
        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.TYPE, parent);
        for (ASTMember member : typeDecl.getMembers()) {
            createSymbolsForMember(table, member);
        }
        return table;
    }

    /**
     * Creates symbols for a <code>Member</code>, and populates them in the
     * given <code>SymbolTable</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @param member An <code>ASTMember</code>.
     */
    public void createSymbolsForMember(SymbolTable parent, ASTMember member) {
        switch (member) {
        case ASTTypeDeclaration typeDecl -> createSymbolsForNestedTypeDeclaration(typeDecl, parent);
        case ASTConstructorDeclaration constrDecl -> createSymbolsForConstructorDeclaration(constrDecl, parent);
        case ASTFieldDeclaration fieldDecl -> createSymbolsForFieldDeclaration(fieldDecl, parent);
        case ASTMethodDeclaration methodDecl -> createSymbolsForMethodDeclaration(methodDecl, parent);
        }
    }

    /**
     * Creates a <code>Symbol</code> for a <code>SharedConstructor</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForConstructorDeclaration(ASTConstructorDeclaration constrDecl, SymbolTable parent) {
        long flags = getFlags(constrDecl);
        ParameterizedSymbol symbol = new ParameterizedSymbol(constrDecl.getLocation(),
                getConstructorSymbolName(constrDecl), Symbol.Type.CONSTRUCTOR, parent, flags);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        constrDecl.getConstructorDecl().setDeclSymbol(symbol);

        createSymbolsForFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList(), symbol);
        getStatementsSymbolCreator().createSymbolsForBlock(constrDecl.getBlock(), table);
    }

    /**
     * Creates <code>Symbol</code>s for a <code>FieldDeclaration</code>.
     * Populates them in the given <code>SymbolTable</code>.
     * @param fieldDecl An <code>ASTFieldDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForFieldDeclaration(ASTFieldDeclaration fieldDecl, SymbolTable parent) {
        long flags = getFlags(fieldDecl);
        for (ASTVariableDeclarator varDecl : fieldDecl.getVarDeclList().getTypedChildren()) {
            String name = varDecl.getVarName().getValue();
            Symbol symbol = new Symbol(fieldDecl.getLocation(), name, Symbol.Type.FIELD, parent, flags);
            insertSymbol(parent, symbol);
            varDecl.setDeclSymbol(symbol);
        }
    }

    /**
     * Creates a <code>Symbol</code>s for a <code>MethodDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForMethodDeclaration(ASTMethodDeclaration methodDecl, SymbolTable parent) {
        long flags = getFlags(methodDecl);
        ParameterizedSymbol symbol = new ParameterizedSymbol(methodDecl.getLocation(), getMethodSymbolName(methodDecl),
                Symbol.Type.METHOD, parent, flags);
        insertSymbol(parent, symbol);
        methodDecl.getHeader().getMethodDecl().setDeclSymbol(symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        createSymbolsForFormalParameterList(methodDecl.getHeader().getMethodDecl().getFormalParamList(), symbol);

        if (methodDecl.getBody().getBlock().isPresent()) {
            getStatementsSymbolCreator().createSymbolsForBlock(methodDecl.getBody().getBlock().get(), table);
        }
    }

    /**
     * Creates symbols for a <code>FormalParameterList</code> and inserts them
     * into the given <code>ParameterizedSymbol</code>.
     * @param formalParams An <code>ASTFormalParameterList</code>.
     * @param param A <code>ParameterizedSymbol</code> to be the parent for
     *              the <code>Symbol</code>s.
     */
    public void createSymbolsForFormalParameterList(ASTFormalParameterList formalParams, ParameterizedSymbol param) {
        SymbolTable parent = param.getParent();
        for (ASTFormalParameter formalParam : formalParams.getTypedChildren()) {
            String name = formalParam.getName().getValue();
            Symbol symbol = new Symbol(formalParam.getLocation(), name, Symbol.Type.PARAMETER, parent, FLAG_NONE);
            ChildSymbolTable table = param.getTable();
            insertSymbol(table, symbol);
            param.addParameter(symbol);
            formalParam.setDeclSymbol(symbol);
        }
    }

    /**
     * Returns the symbol name for a constructor declaration.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @return The constructor name with the symbol name for the formal parameter
     *     list appended.
     */
    public String getConstructorSymbolName(ASTConstructorDeclaration constrDecl) {
        return NAME_CONSTRUCTOR +
                getFormalParameterListSymbolName(constrDecl.getConstructorDecl().getFormalParamList());
    }

    /**
     * Returns the symbol name for a method declaration.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @return The method name with the symbol name for the formal parameter
     *     list appended.
     */
    public String getMethodSymbolName(ASTMethodDeclaration methodDecl) {
        return methodDecl.getHeader().getMethodDecl().getName().getValue() +
                getFormalParameterListSymbolName(methodDecl.getHeader().getMethodDecl().getFormalParamList());
    }

    /**
     * Returns the symbol name for a formal parameter list.
     * @param formalParams An <code>ASTFormalParameterList</code>.
     * @return A comma-separated, parentheses-enclosed string of formal parameter
     *     data type symbol names.
     */
    public String getFormalParameterListSymbolName(ASTFormalParameterList formalParams) {
        TypesSymbolCreator creator = getTypesSymbolCreator();
        return formalParams.getTypedChildren().stream()
                .map(ASTFormalParameter::getDataType)
                .map(creator::getNameForDataType)
                .collect(Collectors.joining(",", "(", ")"));
    }

    /**
     * Gets flags for an <code>ASTMember</code>.
     * @param decl An <code>ASTMember</code>.
     * @return Flags in the form of a <code>long</code>.
     */
    private long getFlags(ASTMember decl) {
        List<TokenType> modifiers = decl.getModifiers();
        long flags = 0;
        for (TokenType modifier : modifiers) {
            switch (modifier) {
            // General modifiers
            case CONSTANT -> flags |= FLAG_MOD_SHARED;
            case OVERRIDE -> flags |= FLAG_MOD_OVERRIDE;
            }
        }
        return flags;
    }
}
