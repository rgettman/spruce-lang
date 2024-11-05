package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTInterfacePart</code> is a constant declaration, an interface
 * method declaration, a class declaration, an interface declaration, an enum
 * declaration, an annotation declaration, a record declaration, or an adt
 * declaration.</p>
 *
 * <em>
 * InterfacePart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceMethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
 * </em>
 */
public sealed interface ASTInterfacePart extends ParentNode permits ASTConstantDeclaration,
        ASTInterfaceMethodDeclaration, ASTTypeDeclaration {
}
