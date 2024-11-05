package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTClassPart</code> is a shared constructor, a constructor, a
 * field declaration, a method declaration, a class declaration, an interface
 * declaration, an enum declaration, an annotation declaration, a record
 * declaration, or an adt declaration.</p>
 *
 * <em>
 * ClassPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;SharedConstructor<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FieldDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;MethodDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
 * </em>
 */
public sealed interface ASTClassPart extends ParentNode permits ASTSharedConstructor, ASTConstructorDeclaration,
        ASTFieldDeclaration, ASTMethodDeclaration, ASTTypeDeclaration, ASTCompactConstructorDeclaration {
}
