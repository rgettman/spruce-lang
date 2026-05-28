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
     * @param globalLookup A <code>GlobalLookup</code>.
     */
    public ClassesSymbolCreator(SymbolCreator symbolCreator, MessageProducer msgProducer, GlobalLookup globalLookup) {
        super(symbolCreator, msgProducer, globalLookup);
    }

    /**
     * Creates a <code>Symbol</code> for a top-level <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> of kind <code>NAMESPACE</code>
     *               that will be the parent of the new type declaration.
     */
    public void createSymbolsForTopLevelTypeDeclaration(ASTTypeDeclaration typeDecl, ParentSymbol parent) {
        createSymbolsForTypeDeclaration(typeDecl, parent, false);
    }

    /**
     * Creates a <code>Symbol</code> for a nested <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> of kind representing a Type
     *               that will be the parent of the new type declaration.
     */
    public void createSymbolsForNestedTypeDeclaration(ASTTypeDeclaration typeDecl, ParentSymbol parent) {
        createSymbolsForTypeDeclaration(typeDecl, parent, true);
    }

    /**
     * Creates a <code>Symbol</code> for a <code>TypeDeclaration</code>,
     * and inserts it into the parent symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> that will be the parent of the
     *               declaration symbol.
     * @param isNested Whether the <code>TypeDeclaration</code> is nested with
     *                 another <code>TypeDeclaration</code>.
     */
    public void createSymbolsForTypeDeclaration(ASTTypeDeclaration typeDecl, ParentSymbol parent,
                                                boolean isNested) {
        long flags = getFlags(typeDecl);

        Kind kind;
        switch (typeDecl) {
        case ASTClassDeclaration ignored ->
            kind = Kind.CLASS;
        case ASTInterfaceDeclaration ignored -> {
            kind = Kind.INTERFACE;
            flags |= FLAG_MOD_ABSTRACT;
            if (isNested) {
                flags |= FLAG_MOD_SHARED;
            }
        }
        }

        ChildSymbolTable parentTable = parent.getTable();
        TypeSymbol symbol = new TypeSymbol(typeDecl.getLocation(), typeDecl.getName().getValue(),
                kind, parentTable, flags);
        insertSymbol(parentTable, symbol);
        typeDecl.setDeclSymbol(symbol);

        createSymbolTableForTypeDeclaration(typeDecl, symbol);
    }

    /**
     * Creates and returns a child symbol table for a <code>TypeDeclaration</code>.
     * Creates child symbols for the type's members and populates them in the symbol table.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to own the new <code>SymbolTable</code>.
     */
    public void createSymbolTableForTypeDeclaration(ASTTypeDeclaration typeDecl, ParentSymbol parent) {
        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.TYPE, parent);
        parent.setTable(table);
        for (ASTMember member : typeDecl.getMembers()) {
            createSymbolsForMember(parent, member);
        }
    }

    /**
     * Creates symbols for a <code>Member</code>, and populates them in the
     * given <code>SymbolTable</code>.
     * @param parent A <code>ParentSymbol</code>
     * @param member An <code>ASTMember</code>.
     */
    public void createSymbolsForMember(ParentSymbol parent, ASTMember member) {
        switch (member) {
        case ASTTypeDeclaration typeDecl -> createSymbolsForNestedTypeDeclaration(typeDecl, parent);
        case ASTConstantDeclaration constDecl -> createSymbolsForConstantDeclaration(constDecl, parent);
        case ASTConstructorDeclaration constrDecl -> createSymbolsForConstructorDeclaration(constrDecl, parent);
        case ASTFieldDeclaration fieldDecl -> createSymbolsForFieldDeclaration(fieldDecl, parent);
        case ASTInterfaceMethodDeclaration iMethodDecl ->
                createSymbolsForInterfaceMethodDeclaration(iMethodDecl, parent);
        case ASTMethodDeclaration methodDecl -> createSymbolsForMethodDeclaration(methodDecl, parent);
        }
    }

    /**
     * Creates a <code>Symbol</code> for a <code>ConstantDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param constDecl An <code>ASTConstantDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForConstantDeclaration(ASTConstantDeclaration constDecl, ParentSymbol parent) {
        long flags = getFlags(constDecl);
        ChildSymbolTable parentTable = parent.getTable();
        flags |= FLAG_MOD_SHARED;
        for (ASTVariableDeclarator varDecl : constDecl.getVarDeclList().getTypedChildren()) {
            String name = varDecl.getVarName().getValue();
            VariableSymbol symbol = new VariableSymbol(constDecl.getLocation(), name, Kind.FIELD, parentTable, flags);
            insertSymbol(parentTable, symbol);
            varDecl.setDeclSymbol(symbol);
        }
    }

    /**
     * Creates a <code>Symbol</code> for a <code>Constructor</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForConstructorDeclaration(ASTConstructorDeclaration constrDecl, ParentSymbol parent) {
        long flags = getFlags(constrDecl);
        ChildSymbolTable parentTable = parent.getTable();
        ParameterizedSymbol symbol = new ParameterizedSymbol(constrDecl.getLocation(),
                getConstructorSymbolName(constrDecl), Kind.CONSTRUCTOR, parentTable, flags);
        insertSymbol(parentTable, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        constrDecl.setDeclSymbol(symbol);

        createSymbolsForFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList(), symbol);
        getStatementsSymbolCreator().createSymbolsForBlock(constrDecl.getBlock(), symbol);
    }

    /**
     * Creates <code>Symbol</code>s for a <code>FieldDeclaration</code>.
     * Populates them in the given <code>SymbolTable</code>.
     * @param fieldDecl An <code>ASTFieldDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForFieldDeclaration(ASTFieldDeclaration fieldDecl, ParentSymbol parent) {
        long flags = getFlags(fieldDecl);
        ChildSymbolTable parentTable = parent.getTable();
        for (ASTVariableDeclarator varDecl : fieldDecl.getVarDeclList().getTypedChildren()) {
            String name = varDecl.getVarName().getValue();
            VariableSymbol symbol = new VariableSymbol(fieldDecl.getLocation(), name, Kind.FIELD, parentTable, flags);
            insertSymbol(parentTable, symbol);
            varDecl.setDeclSymbol(symbol);
        }
    }

    /**
     * Creates a <code>Symbol</code>s for an <code>InterfaceMethodDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param methodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForInterfaceMethodDeclaration(ASTInterfaceMethodDeclaration methodDecl,
                                                           ParentSymbol parent) {
        long flags = getFlags(methodDecl);
        ChildSymbolTable parentTable = parent.getTable();
        flags |= FLAG_MOD_ABSTRACT;
        ParameterizedSymbol symbol = new ParameterizedSymbol(methodDecl.getLocation(), getInterfaceMethodSymbolName(methodDecl),
                Kind.METHOD, parentTable, flags);
        insertSymbol(parentTable, symbol);
        methodDecl.setDeclSymbol(symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, symbol);
        symbol.setTable(table);
        createSymbolsForFormalParameterList(methodDecl.getHeader().getMethodDecl().getFormalParamList(), symbol);

        if (methodDecl.getBody().getBlock().isPresent()) {
            getStatementsSymbolCreator().createSymbolsForBlock(methodDecl.getBody().getBlock().get(), symbol);
        }
    }

    /**
     * Creates a <code>Symbol</code>s for a <code>MethodDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param parent A <code>ParentSymbol</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForMethodDeclaration(ASTMethodDeclaration methodDecl, ParentSymbol parent) {
        long flags = getFlags(methodDecl);
        ChildSymbolTable parentTable = parent.getTable();
        ParameterizedSymbol symbol = new ParameterizedSymbol(methodDecl.getLocation(), getMethodSymbolName(methodDecl),
                Kind.METHOD, parentTable, flags);
        insertSymbol(parentTable, symbol);
        methodDecl.setDeclSymbol(symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, symbol);
        symbol.setTable(table);
        createSymbolsForFormalParameterList(methodDecl.getHeader().getMethodDecl().getFormalParamList(), symbol);

        if (methodDecl.getBody().getBlock().isPresent()) {
            getStatementsSymbolCreator().createSymbolsForBlock(methodDecl.getBody().getBlock().get(), symbol);
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
            VariableSymbol symbol = new VariableSymbol(formalParam.getLocation(), name, Kind.PARAMETER, parent, FLAG_NONE);
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
        return getMethodHeaderSymbolName(methodDecl.getHeader());
    }

    /**
     * Returns the symbol name for an interface method declaration.
     * @param methodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     * @return The method name with the symbol name for the formal parameter
     *     list appended.
     */
    public String getInterfaceMethodSymbolName(ASTInterfaceMethodDeclaration methodDecl) {
        return getMethodHeaderSymbolName(methodDecl.getHeader());
    }

    private String getMethodHeaderSymbolName(ASTMethodHeader header) {
        return header.getMethodDecl().getName().getValue() +
                getFormalParameterListSymbolName(header.getMethodDecl().getFormalParamList());
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
     * Gets flags for a <code>Member</code>.
     * @param decl An <code>ASTMember</code>.
     * @return Flags in the form of a <code>long</code>.
     */
    private long getFlags(ASTMember decl) {
        List<TokenType> modifiers = decl.getModifiers();
        long flags = 0;
        for (TokenType modifier : modifiers) {
            switch (modifier) {
            // General modifiers
            case ABSTRACT -> flags |= FLAG_MOD_ABSTRACT;
            case CONSTANT -> flags |= FLAG_MOD_SHARED;
            case OVERRIDE -> flags |= FLAG_MOD_OVERRIDE;
            }
        }
        return flags;
    }
}
