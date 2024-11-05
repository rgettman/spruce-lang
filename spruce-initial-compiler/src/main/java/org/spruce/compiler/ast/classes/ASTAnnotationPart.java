package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTAnnotationPart</code> is an annotation type
 * member declaration, a constant declaration, a class declaration, an
 * interface declaration, an enum declaration, an annotation declaration,
 * a record declaration, or an adt declaration.</p>
 *
 * <em>
 * AnnotationPart:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationTypeElementDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
 * </em>
 */
public sealed interface ASTAnnotationPart extends ParentNode permits ASTAnnotationTypeElementDeclaration,
        ASTConstantDeclaration, ASTTypeDeclaration {
}

