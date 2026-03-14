package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.symbol.Symbol;

/**
 * <p>An <code>ASTTypeDeclaration</code> is an <code>ASTMember</code>
 * that is a class.</p>
 *
 * <em>
 * TypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration
 * </em>
 */
public sealed interface ASTTypeDeclaration extends ParentNode, ASTClassPart
        permits ASTClassDeclaration {
    /**
     * Returns the <code>ASTIdentifier</code> representing the name of the type
     * declaration.
     * @return The <code>ASTIdentifier</code> representing the name of the type
     *         declaration.
     */
    ASTIdentifier getName();

    /**
     * Returns a <code>List</code> of <code>ASTMember</code>s.
     * @return A <code>List</code> of <code>ASTMember</code>s.
     */
    List<ASTMember> getMembers();

    /**
     * Sets the declaration <code>Symbol</code>.
     * @param symbol The declaration <code>Symbol</code>.
     */
    void setDeclSymbol(Symbol symbol);

    /**
     * Returns the declaration <code>Symbol</code>.
     * @return The declaration <code>Symbol</code>.
     */
    Symbol getDeclSymbol();
}

