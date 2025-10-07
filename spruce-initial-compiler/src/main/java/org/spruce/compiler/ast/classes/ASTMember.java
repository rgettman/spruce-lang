package org.spruce.compiler.ast.classes;

import java.util.List;
import java.util.Optional;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.Node;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.TokenType;

/**
 * An <code>ASTMember</code> is a <code>Node</code> that is permitted to be
 * declared inside the body of a type declaration, and can have general
 * modifiers, an access modifier, and one or more names.
 */
public sealed interface ASTMember extends Node permits ASTClassPart, ASTInterfacePart, ASTAnnotationPart,
        ASTEnumConstant, ASTCompactRecordDeclaration, ASTRecordComponent {
    /**
     * Returns a <code>List</code> of all <code>TokenType</code>s found as
     * modifiers on a declaration, such as a type declaration or a part of a
     * declaration.
     * @return A <code>List</code> of <code>TokenType</code>s, possibly empty.
     */
    List<TokenType> getModifiers();

    /**
     * Returns an <code>ASTKeywordNode</code> representing the access modifier,
     * if it exists.
     * @return An <code>Optional&lt;ASTKeywordNode&gt;</code>.
     */
    Optional<ASTKeywordNode> getAccessMod();

    /**
     * Returns a <code>List</code> of <code>ASTIdentifier</code>s representing
     * the name(s) of this declaration.
     * @return A possibly empty <code>List</code> of <code>ASTIdentifier</code>s.
     */
    List<ASTIdentifier> getNames();

    /**
     * Returns an <code>ASTTypeParameterList</code>, if it exists.
     * @return An <code>Optional&lt;ASTTypeParameterList&gt;</code>.
     */
    Optional<ASTTypeParameterList> getTypeParams();
}
