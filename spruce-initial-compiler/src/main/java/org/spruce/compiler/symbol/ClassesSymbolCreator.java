package org.spruce.compiler.symbol;

import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.classes.ASTAdtDeclaration;
import org.spruce.compiler.ast.classes.ASTAnnotationDeclaration;
import org.spruce.compiler.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.ast.classes.ASTEnumDeclaration;
import org.spruce.compiler.ast.classes.ASTInterfaceDeclaration;
import org.spruce.compiler.ast.classes.ASTModifiableDeclaration;
import org.spruce.compiler.ast.classes.ASTRecordDeclaration;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
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
     * Creates and returns a <code>Symbol</code> for a <code>TypeDeclaration</code>.
     * @param typeDecl An <code>ASTTypeDeclaration</code>.
     * @param parent A <code>SymbolTable</code> to be the parent for the <code>Symbol</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public Symbol createSymbolsForTypeDeclaration(ASTTypeDeclaration typeDecl, SymbolTable parent) {
        Optional<ASTKeywordNode> accessMod = typeDecl.getAccessMod();
        long flags = getFlags(typeDecl);
        if (accessMod.isEmpty()) {
            flags |= DEFAULT_ACCESS_CLASS;
        }

        return switch (typeDecl) {
            case ASTClassDeclaration classDecl -> createSymbolForClassDeclaration(classDecl, flags, parent);
            case ASTInterfaceDeclaration interfaceDecl -> createSymbolForInterfaceDeclaration(interfaceDecl, flags, parent);
            case ASTEnumDeclaration enumDecl -> createSymbolForEnumDeclaration(enumDecl, flags, parent);
            case ASTAnnotationDeclaration annotationDecl -> createSymbolForAnnotationDeclaration(annotationDecl, flags, parent);
            case ASTRecordDeclaration recordDecl -> createSymbolForRecordDeclaration(recordDecl, flags, parent);
            case ASTAdtDeclaration adtDecl -> createSymbolForAdtDeclaration(adtDecl, flags, parent);
        };
    }

    /**
     * Creates and returns a <code>Symbol</code> for a <code>ClassDeclaration</code>.
     * @param classDecl An <code>ASTClassDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForClassDeclaration(ASTClassDeclaration classDecl, long flags, SymbolTable parent) {
        ParentSymbol symbol = new ParentSymbol(classDecl.getLocation(), classDecl.getName().getValue(),
                Symbol.Type.CLASS, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over class parts.
        ;

        return symbol;
    }

    /**
     * Creates and returns a <code>Symbol</code> for an <code>InterfaceDeclaration</code>.
     * @param interfaceDecl An <code>ASTInterfaceDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForInterfaceDeclaration(ASTInterfaceDeclaration interfaceDecl, long flags, SymbolTable parent) {
        // Interfaces are implicitly abstract.
        flags |= FLAG_MOD_ABSTRACT;
        ParentSymbol symbol = new ParentSymbol(interfaceDecl.getLocation(), interfaceDecl.getName().getValue(),
                Symbol.Type.INTERFACE, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over interface parts.
        ;

        return symbol;
    }

    /**
     * Creates and returns a <code>Symbol</code> for an <code>EnumDeclaration</code>.
     * @param enumDecl An <code>ASTEnumDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForEnumDeclaration(ASTEnumDeclaration enumDecl, long flags, SymbolTable parent) {
        ParentSymbol symbol = new ParentSymbol(enumDecl.getLocation(), enumDecl.getName().getValue(),
                Symbol.Type.ENUM, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over enum constants.
        // Loop over class parts.
        ;

        return symbol;
    }

    /**
     * Creates and returns a <code>Symbol</code> for an <code>AnnotationDeclaration</code>.
     * @param annotationDecl An <code>ASTAnnotationDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForAnnotationDeclaration(ASTAnnotationDeclaration annotationDecl, long flags, SymbolTable parent) {
        // Annotations are implicitly abstract.
        flags |= FLAG_MOD_ABSTRACT;
        ParentSymbol symbol = new ParentSymbol(annotationDecl.getLocation(), annotationDecl.getName().getValue(),
                Symbol.Type.ANNOTATION, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over annotation parts.
        ;

        return symbol;
    }

    /**
     * Creates and returns a <code>Symbol</code> for a <code>RecordDeclaration</code>.
     * @param recordDecl An <code>ASTRecordDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForRecordDeclaration(ASTRecordDeclaration recordDecl, long flags, SymbolTable parent) {
        // Records are implicitly final.
        flags |= FLAG_MOD_FINAL;
        ParentSymbol symbol = new ParentSymbol(recordDecl.getLocation(), recordDecl.getName().getValue(),
                Symbol.Type.RECORD, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over record components.
        // Loop over class parts.
        ;

        return symbol;
    }

    /**
     * Creates and returns a <code>Symbol</code> for an <code>AdtDeclaration</code>.
     * @param adtDecl An <code>ASTAdtDeclaration</code>.
     * @param flags Flags already extracted from the modifiers.
     * @param parent The parent <code>SymbolTable</code>.
     * @return A <code>ParentSymbol</code>.
     */
    public ParentSymbol createSymbolForAdtDeclaration(ASTAdtDeclaration adtDecl, long flags, SymbolTable parent) {
        // ADTs are implicitly sealed.
        flags |= FLAG_MOD_SEALED;
        ParentSymbol symbol = new ParentSymbol(adtDecl.getLocation(), adtDecl.getName().getValue(),
                Symbol.Type.ADT, parent, flags, SymbolTable.Scope.TYPE);

        // Loop over variants.
        // Loop over class parts.
        ;

        return symbol;
    }


//    public Symbol createSymbolsForMembers() {
//
//    }

    /**
     * Gets flags for an <code>ASTModifiableDeclaration</code>.
     * @param decl An <code>ASTModifiableDeclaration</code>.
     * @return Flags in the form of a <code>long</code>.
     */
    private long getFlags(ASTModifiableDeclaration decl) {
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
            case CONSTANT -> flags |= FLAG_MOD_SHARED;
            case DEFAULT -> flags |= FLAG_MOD_DEFAULT;
            case FINAL -> flags |= FLAG_MOD_FINAL;
            case OVERRIDE -> flags |= FLAG_MOD_OVERRIDE;
            case SEALED -> flags |= FLAG_MOD_SEALED;
            case SHARED -> flags |= FLAG_MOD_SHARED;
            case VOLATILE -> flags |= FLAG_MOD_VOLATILE;
            // Variable modifiers
            case MUT -> flags |= FLAG_VARIABLE_MUT;
            case VAR -> flags |= FLAG_VARIABLE_VAR;
            }
        }
        return flags;
    }
}
