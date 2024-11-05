package org.spruce.compiler.ast.statements;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.ASTParentNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.ParentNode;
import org.spruce.compiler.scanner.Location;

/**
 * <p>An <code>ASTInit</code> is a local variable declaration
 * or a statement expression list.</p>
 *
 * <em>
 * Init:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;LocalVariableDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StatementExpressionList
 * </em>
 */
public sealed interface ASTInit extends ParentNode permits ASTLocalVariableDeclaration, ASTStatementExpressionList {
}
