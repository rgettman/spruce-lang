package org.spruce.compiler.ast.classes;

import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.names.ASTIdentifier;

/**
 * <p>An <code>ASTTypeDeclaration</code> is a class, enum, interface, or
 * annotation declaration.</p>
 *
 * <em>
 * TypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
 * </em>
 */
public sealed interface ASTTypeDeclaration extends ParentNode, ASTAnnotationPart, ASTClassPart, ASTInterfacePart,
                ASTModifiableDeclaration
        permits ASTClassDeclaration, ASTEnumDeclaration, ASTInterfaceDeclaration, ASTAnnotationDeclaration,
                ASTRecordDeclaration, ASTAdtDeclaration {
    /**
     * Returns an <code>ASTKeywordNode</code> representing the access modifier,
     * if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    Optional<ASTKeywordNode> getAccessMod();

    /**
     * Returns an <code>ASTIdentifier</code> representing the type name.
     * @return An <code>ASTIdentifier</code> representing the type name.
     */
    ASTIdentifier getName();

}

