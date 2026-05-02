package org.spruce.compiler.bootstrap.ast.statements;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.SymbolDeclaration;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;

/**
 * An <code>ASTForStatement</code> is a particular kind of statement that is a
 * BasicForStatement or an EnhancedForStatement.
 * <em>
 * ForStatement:
 * &nbsp;&nbsp;&nbsp;&nbsp;BasicForStatement
 * &nbsp;&nbsp;&nbsp;&nbsp;EnhancedForStatement
 * </em>
 */
public sealed interface ASTForStatement extends ParentNode, ASTStatement, SymbolDeclaration<ParentSymbol>
        permits ASTBasicForStatement, ASTEnhancedForStatement {
}
