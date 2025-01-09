package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

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
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
 * </em>
 */
public sealed interface ASTClassPart extends ParentNode permits ASTSharedConstructor, ASTConstructorDeclaration,
        ASTFieldDeclaration, ASTMethodDeclaration, ASTTypeDeclaration, ASTCompactConstructorDeclaration {
}
