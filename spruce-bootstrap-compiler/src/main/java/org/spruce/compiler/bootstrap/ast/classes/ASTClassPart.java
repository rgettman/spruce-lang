package org.spruce.compiler.bootstrap.ast.classes;

import org.spruce.compiler.bootstrap.ast.ParentNode;

/**
 * <p>An <code>ASTClassPart</code> is a shared constructor, a constructor, a
 * field declaration, a method declaration, or a type declaration.</p>
 *
 * <em>
 * ClassPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SharedConstructor<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CompactConstructorDeclaration
 * </em>
 */
public sealed interface ASTClassPart extends ParentNode, ASTMember permits ASTConstructorDeclaration,
        ASTFieldDeclaration, ASTMethodDeclaration, ASTTypeDeclaration {
}
