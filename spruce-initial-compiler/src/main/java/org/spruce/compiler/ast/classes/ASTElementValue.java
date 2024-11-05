package org.spruce.compiler.ast.classes;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.expressions.ASTValueExpression;

/**
 * <p>An <code>ASTElementValue</code> is a value expression, an element
 * value array initializer, or an annotation.</p>
 *
 * <em>
 * ElementValue:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ElementValueArrayInitializer<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;Annotation
 * </em>
 */
public sealed interface ASTElementValue extends ParentNode permits ASTValueExpression, ASTElementValueList, ASTAnnotation {
}
