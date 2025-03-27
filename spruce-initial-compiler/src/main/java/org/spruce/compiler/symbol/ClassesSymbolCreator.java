package org.spruce.compiler.symbol;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.symbol.Symbol.*;
import static org.spruce.compiler.symbol.Symbol.FLAG_MOD_SEALED;

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
        Optional<ASTKeywordNode> accessMod = typeDecl.getAccessMod();
        long flags = getFlags(typeDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_CLASS;
        }

        Symbol.Type type;
        switch (typeDecl) {
            case ASTClassDeclaration ignored ->
                type = Symbol.Type.CLASS;
            case ASTInterfaceDeclaration ignored -> {
                type = Symbol.Type.INTERFACE;
                flags |= FLAG_MOD_ABSTRACT;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
            case ASTEnumDeclaration ignored -> {
                type = Symbol.Type.ENUM;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
            case ASTAnnotationDeclaration ignored -> {
                type = Symbol.Type.ANNOTATION;
                flags |= FLAG_MOD_ABSTRACT;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
            case ASTRecordDeclaration ignored -> {
                type = Symbol.Type.RECORD;
                flags |= FLAG_MOD_FINAL;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
            case ASTCompactRecordDeclaration ignored -> {
                type = Symbol.Type.RECORD;
                flags |= FLAG_MOD_FINAL;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
            case ASTAdtDeclaration ignored -> {
                type = Symbol.Type.ADT;
                flags |= FLAG_MOD_SEALED;
                if (isNested) {
                    flags |= FLAG_MOD_SHARED;
                }
            }
        }

        ParentSymbol symbol = new ParentSymbol(typeDecl.getLocation(), typeDecl.getName().getValue(),
                type, parent, flags /*, SymbolTable.Scope.TYPE*/);
        insertSymbol(parent, symbol);

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
     * @param member An <code>ASTMember</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForMember(SymbolTable parent, ASTMember member) {
        switch (member) {
        case ASTTypeDeclaration typeDecl -> createSymbolsForNestedTypeDeclaration(typeDecl, parent);
        case ASTSharedConstructor shConstr -> createSymbolsForSharedConstructor(shConstr, parent);
        case ASTConstructorDeclaration constrDecl -> createSymbolsForConstructorDeclaration(constrDecl, parent);
        case ASTCompactConstructorDeclaration constrDecl -> createSymbolsForConstructorDeclaration(constrDecl, parent);
        case ASTFieldDeclaration fieldDecl -> createSymbolsForFieldDeclaration(fieldDecl, parent);
        case ASTConstantDeclaration constDecl -> createSymbolsForFieldDeclaration(constDecl, parent);
        case ASTAnnotationTypeElementDeclaration ated -> createSymbolsForATED(ated, parent);
        case ASTMethodDeclaration methodDecl -> createSymbolsForMethodDeclaration(methodDecl, parent);
        case ASTInterfaceMethodDeclaration interfaceMethodDecl -> createSymbolsForInterfaceMethodDeclaration(interfaceMethodDecl, parent);
        case ASTEnumConstant enumConst -> createSymbolsForEnumConstant(enumConst, parent);
        case ASTRecordComponent recordComp -> createSymbolsForRecordComp(recordComp, parent);
        }
    }

    /**
     * Creates a <code>Symbol</code> for a <code>SharedConstructor</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param shConstr An <code>ASTSharedConstructor</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForSharedConstructor(ASTSharedConstructor shConstr, SymbolTable parent) {
        ParentSymbol symbol = new ParentSymbol(shConstr.getLocation(),
                NAME_SHARED_CONSTRUCTOR, Symbol.Type.SHARED_CONSTRUCTOR, parent, FLAG_MOD_SHARED);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        getStatementsSymbolCreator().createSymbolsForBlock(shConstr.getBlock(), table);
    }

    /**
     * Creates a <code>Symbol</code> for a <code>SharedConstructor</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param constrDecl An <code>ASTConstructorDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForConstructorDeclaration(ASTConstructorDeclaration constrDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = constrDecl.getAccessMod();
        long flags = getFlags(constrDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_CONSTRUCTOR;
        }
        ParameterizedSymbol symbol = new ParameterizedSymbol(constrDecl.getLocation(),
                getConstructorSymbolName(constrDecl), Symbol.Type.CONSTRUCTOR, parent, flags);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        createSymbolsForFormalParameterList(constrDecl.getConstructorDecl().getFormalParamList(), symbol);
        getStatementsSymbolCreator().createSymbolsForBlock(constrDecl.getBlock(), table);
    }

    /**
     * Creates a <code>Symbol</code> for a <code>CompactSharedConstructor</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param constrDecl An <code>ASTCompactConstructorDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForConstructorDeclaration(ASTCompactConstructorDeclaration constrDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = constrDecl.getAccessMod();
        long flags = getFlags(constrDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_FIELD;
        }
        ParentSymbol symbol = new ParentSymbol(constrDecl.getLocation(),
                NAME_CONSTRUCTOR, Symbol.Type.CONSTRUCTOR, parent, flags);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        getStatementsSymbolCreator().createSymbolsForBlock(constrDecl.getBlock(), table);
    }

    /**
     * Creates <code>Symbol</code>s for a <code>FieldDeclaration</code>.
     * Populates them in the given <code>SymbolTable</code>.
     * @param fieldDecl An <code>ASTFieldDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForFieldDeclaration(ASTFieldDeclaration fieldDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = fieldDecl.getAccessMod();
        long flags = getFlags(fieldDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_FIELD;
        }
        for (String name : fieldDecl.getNames().stream().map(ASTIdentifier::getValue).toList()) {
            Symbol symbol = new Symbol(fieldDecl.getLocation(), name, Symbol.Type.FIELD, parent, flags);
            insertSymbol(parent, symbol);
        }
    }

    /**
     * Creates <code>Symbol</code>s for a <code>ConstantDeclaration</code>.
     * Populates them in the given <code>SymbolTable</code>.
     * @param constDecl An <code>ASTConstantDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForFieldDeclaration(ASTConstantDeclaration constDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = constDecl.getAccessMod();
        long flags = getFlags(constDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_FIELD;
        }
        flags |= FLAG_MOD_FINAL | FLAG_MOD_SHARED;
        for (String name : constDecl.getNames().stream().map(ASTIdentifier::getValue).toList()) {
            Symbol symbol = new Symbol(constDecl.getLocation(), name, Symbol.Type.FIELD, parent, flags);
            insertSymbol(parent, symbol);
        }
    }

    /**
     * Creates a <code>Symbol</code> for an <code>AnnotationTypeElementDeclaration</code>.
     * @param ated An <code>ASTAnnotationTypeElementDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForATED(ASTAnnotationTypeElementDeclaration ated, SymbolTable parent) {
        long flags = FLAG_ACCESS_PUBLIC | FLAG_MOD_ABSTRACT;
        insertSymbol(parent, new Symbol(ated.getLocation(), ated.getName().getValue(),
                Type.ANNOTATION_TYPE_ELEMENT, parent, flags));
    }

    /**
     * Creates a <code>Symbol</code>s for a <code>MethodDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param methodDecl An <code>ASTMethodDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForMethodDeclaration(ASTMethodDeclaration methodDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = methodDecl.getAccessMod();
        long flags = getFlags(methodDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_METHOD;
        }
        if (methodDecl.getHeader().getMethodDecl().getMutModifier().isPresent()) {
            flags |= FLAG_METHOD_MUT;
        }
        ParameterizedSymbol symbol = new ParameterizedSymbol(methodDecl.getLocation(), getMethodSymbolName(methodDecl),
                Symbol.Type.METHOD, parent, flags);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        createSymbolsForFormalParameterList(methodDecl.getHeader().getMethodDecl().getFormalParamList(), symbol);

        if (methodDecl.getBody().getBlock().isPresent()) {
            getStatementsSymbolCreator().createSymbolsForBlock(methodDecl.getBody().getBlock().get(), table);
        }
    }

    /**
     * Creates a <code>Symbol</code> for an <code>InterfaceMethodDeclaration</code>.
     * Populates it in the given <code>SymbolTable</code>/
     * @param methodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForInterfaceMethodDeclaration(ASTInterfaceMethodDeclaration methodDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = methodDecl.getAccessMod();
        long flags = getFlags(methodDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_METHOD;
        }
        if (methodDecl.getHeader().getMethodDecl().getMutModifier().isPresent()) {
            flags |= FLAG_METHOD_MUT;
        }
        flags |= FLAG_MOD_ABSTRACT;
        ParameterizedSymbol symbol = new ParameterizedSymbol(methodDecl.getLocation(), getInterfaceMethodSymbolName(methodDecl),
                Symbol.Type.METHOD, parent, flags);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        createSymbolsForFormalParameterList( methodDecl.getHeader().getMethodDecl().getFormalParamList(), symbol);

        if (methodDecl.getBody().getBlock().isPresent()) {
            getStatementsSymbolCreator().createSymbolsForBlock(methodDecl.getBody().getBlock().get(), table);
        }
    }

    /**
     * Creates a <code>Symbol</code> for an <code>EnumConstant</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param enumConst An <code>ASTEnumConstant</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForEnumConstant(ASTEnumConstant enumConst, SymbolTable parent) {
        long flags = getFlags(enumConst);
        flags |= FLAG_ACCESS_PUBLIC | FLAG_MOD_SHARED;
        ParentSymbol symbol = new ParentSymbol(enumConst.getLocation(), enumConst.getName().getValue(),
                Symbol.Type.ENUM_CONSTANT, parent, flags);
        insertSymbol(parent, symbol);

        ChildSymbolTable table = new ChildSymbolTable(SymbolTable.Scope.MEMBER, parent);
        symbol.setTable(table);
        for (ASTClassPart member : enumConst.getClassParts().getTypedChildren()) {
            // A member may generate more than one symbol, e.g. a FieldDeclaration
            // that declares multiple variables.
            createSymbolsForMember(table, member);
        }
    }

    /**
     * Creates a <code>Symbol</code>s for a <code>RecordComponent</code>.
     * Populates it in the given <code>SymbolTable</code>.
     * @param recordComp An <code>ASTRecordComponent</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     */
    public void createSymbolsForRecordComp(ASTRecordComponent recordComp, SymbolTable parent) {
        long flags = getFlags(recordComp);
        flags |= FLAG_ACCESS_PUBLIC;
        insertSymbol(parent, new Symbol(recordComp.getLocation(), recordComp.getName().getValue(),
                Symbol.Type.RECORD_COMPONENT, parent, flags));
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
            long flags = 0;
            for (ASTKeywordNode varMod : formalParam.getVarModList().getTypedChildren()) {
                switch(varMod.getKeyword()) {
                case VAR -> flags |= FLAG_VARIABLE_VAR;
                case MUT -> flags |= FLAG_VARIABLE_MUT;
                }
            }
            Symbol symbol = new Symbol(formalParam.getLocation(), name, Symbol.Type.PARAMETER, parent, flags);
            ChildSymbolTable table = param.getTable();
            insertSymbol(table, symbol);
            param.addParameter(symbol);
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
     * Returns the symbol name for an interface method declaration.
     * @param methodDecl An <code>ASTInterfaceMethodDeclaration</code>.
     * @return The method name with the symbol name for the formal parameter
     *     list appended.
     */
    public String getInterfaceMethodSymbolName(ASTInterfaceMethodDeclaration methodDecl) {
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
            // Access modifier
            case PUBLIC -> flags |= FLAG_ACCESS_PUBLIC;
            case INTERNAL -> flags |= FLAG_ACCESS_INTERNAL;
            case PROTECTED -> flags |= FLAG_ACCESS_PROTECTED;
            case PRIVATE -> flags |= FLAG_ACCESS_PRIVATE;
            // General modifiers
            case ABSTRACT -> flags |= FLAG_MOD_ABSTRACT;
            case CONSTANT, SHARED -> flags |= FLAG_MOD_SHARED;
            case DEFAULT -> flags |= FLAG_MOD_DEFAULT;
            case FINAL -> flags |= FLAG_MOD_FINAL;
            case OVERRIDE -> flags |= FLAG_MOD_OVERRIDE;
            case SEALED -> flags |= FLAG_MOD_SEALED;
            case VOLATILE -> flags |= FLAG_MOD_VOLATILE;
            // Variable modifiers
            case MUT -> flags |= FLAG_VARIABLE_MUT;
            case VAR -> flags |= FLAG_VARIABLE_VAR;
            }
        }
        return flags;
    }
}
