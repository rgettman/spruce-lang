package org.spruce.compiler.ast.classes;

import java.util.List;

import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTModifiableDeclaration</code> can have <em>modifier</em> keywords
 * associated with the declaration.
 */
public interface ASTModifiableDeclaration {
    /**
     * Returns a <code>List</code> of all <code>TokenType</code>s found as
     * modifiers on a declaration, such as a type declaration or a part of a
     * declaration.
     * @return A <code>List</code> of <code>TokenType</code>s, possibly empty.
     */
    List<TokenType> getModifiers();
}
