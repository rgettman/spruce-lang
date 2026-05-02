package org.spruce.compiler.bootstrap.ast.literals;

import org.spruce.compiler.bootstrap.ast.ValueNode;

/**
 * <p>An <code>ASTLiteral</code> is a node representing a literal value.</p>
 *
 * <em>
 * Literal:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;BooleanLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;IntegerLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;FloatingPointLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;CharacterLiteral<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;StringLiteral
 * </em>
 */
public sealed interface ASTLiteral extends ValueNode permits ASTBooleanLiteral, ASTCharacterLiteral,
        ASTFloatingPointLiteral, ASTIntegerLiteral, ASTStringLiteral {
}