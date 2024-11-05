package org.spruce.compiler.ast.expressions;

import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.ast.classes.ASTFormalParameterList;

/**
 * <p>An <code>ASTLambdaParameterList</code> is either an inferred parameter
 * list or a formal parameter list.</p>
 *
 * <em>
 * LambdaParameterList:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InferredParameterList<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameterList<br>
 * </em>
 */
public sealed interface ASTLambdaParameterList extends ParentNode permits ASTFormalParameterList, ASTInferredParameterList {
}
