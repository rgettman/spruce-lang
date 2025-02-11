package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTInterfacePart</code> is a constant declaration, an interface
 * method declaration, or a type declaration.</p>
 *
 * <em>
 * InterfacePart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceMethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
 * </em>
 */
public sealed interface ASTInterfacePart extends ParentNode, ASTMember permits ASTConstantDeclaration,
        ASTInterfaceMethodDeclaration, ASTTypeDeclaration {
}
