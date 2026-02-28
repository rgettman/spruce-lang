package org.spruce.compiler.bootstrap.ast.classes;

import java.util.List;

import org.spruce.compiler.bootstrap.ast.ParentNode;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;

/**
 * <p>An <code>ASTTypeDeclaration</code> is an <code>ASTMember</code>
 * that is a class, enum, interface, annotation, record, or ADT declaration.</p>
 *
 * <em>
 * TypeDeclaration:<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
 * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
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
}

