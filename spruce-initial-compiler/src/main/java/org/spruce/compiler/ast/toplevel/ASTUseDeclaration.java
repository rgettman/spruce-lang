package org.spruce.compiler.ast.toplevel;

import org.spruce.compiler.ast.ParentNode;

/**
 * <p>An <code>ASTUseDeclaration</code> is a use type declaration,
 * a use mult declaration, a use all declaration, or the shared
 * version of any of those three.</p>
 *
 * <em>
 * UseDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseAllDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedTypeDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedMultDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedAllDeclaration
 * </em>
 */
public sealed interface ASTUseDeclaration extends ParentNode permits ASTUseTypeDeclaration, ASTUseMultDeclaration,
        ASTUseAllDeclaration, ASTUseSharedTypeDeclaration, ASTUseSharedMultDeclaration, ASTUseSharedAllDeclaration {
}

