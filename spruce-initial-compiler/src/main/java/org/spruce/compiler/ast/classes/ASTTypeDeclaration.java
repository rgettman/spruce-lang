package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

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
public sealed interface ASTTypeDeclaration extends ParentNode, ASTAnnotationPart, ASTClassPart, ASTInterfacePart permits
        ASTClassDeclaration, ASTEnumDeclaration, ASTInterfaceDeclaration, ASTAnnotationDeclaration,
        ASTRecordDeclaration, ASTAdtDeclaration {
}

