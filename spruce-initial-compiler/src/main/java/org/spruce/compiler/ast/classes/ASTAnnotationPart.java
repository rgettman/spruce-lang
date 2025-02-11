package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTAnnotationPart</code> is an annotation type
 * member declaration, a constant declaration, or a type declaration.</p>
 *
 * <em>
 * AnnotationPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationTypeElementDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
 * </em>
 */
public sealed interface ASTAnnotationPart extends ParentNode, ASTMember permits ASTAnnotationTypeElementDeclaration,
        ASTConstantDeclaration, ASTTypeDeclaration {
}

