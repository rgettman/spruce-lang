package org.spruce.compiler.bootstrap.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.spruce.compiler.bootstrap.ast.classes.ASTGeneralModifierList;
import org.spruce.compiler.bootstrap.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifierList;
import org.spruce.compiler.bootstrap.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.bootstrap.ast.names.ASTTypeName;
import org.spruce.compiler.bootstrap.ast.toplevel.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>TopLevelParser</code> is a <code>BasicParser</code> that parses
 * top-level productions.
 */
public class TopLevelParser extends BasicParser {
    /**
     * Constructs a <code>TopLevelParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TopLevelParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * Parses an <code>OrdinaryCompilationUnit</code>.
     * <em>
     * OrdinaryCompilationUnit:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NamespaceDeclaration UseDeclarationList TypeDeclarationList<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseDeclarationList TypeDeclarationList
     * </em>
     * @return An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public ASTOrdinaryCompilationUnit parseOrdinaryCompilationUnit() {
        Location loc = curr().getLocation();
        ASTNamespaceDeclaration namespaceDecl = null;

        // Namespace
        if (isCurr(NAMESPACE)) {
            namespaceDecl = parseNamespaceDeclaration();
        }

        // Use
        ASTUseDeclarationList useDeclList = parseUseDeclarationList();

        // Type
        ASTTypeDeclarationList typeDeclList = parseTypeDeclarationList();
        if (namespaceDecl != null) {
            return new ASTOrdinaryCompilationUnit(loc, namespaceDecl, useDeclList, typeDeclList);
        }
        return new ASTOrdinaryCompilationUnit(loc, useDeclList, typeDeclList);
    }

    /**
     * Parses a <code>NamespaceDeclaration</code>.
     * <em>
     * NamespaceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;namespace NamespaceName
     * </em>
     * @return An <code>ASTNamespaceDeclaration</code>.
     */
    public ASTNamespaceDeclaration parseNamespaceDeclaration() {
        Location loc = curr().getLocation();
        if (accept(NAMESPACE) == null) {
            throw internalError(NAMESPACE);
        }
        ASTNamespaceDeclaration node = new ASTNamespaceDeclaration(loc, getNamesParser().parseNamespaceName());
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return node;
    }

    /**
     * Parses a <code>UseDeclarationList</code>.
     * <em>
     * UseDeclarationList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseDeclaration {UseDeclaration}
     * </em>
     * @return An <code>ASTUseDeclarationList</code>.
     */
    public ASTUseDeclarationList parseUseDeclarationList() {
        return parseMultiple(
                t -> test(t, USE),
                "Expected use declaration.",
                this::parseUseDeclaration,
                ASTUseDeclarationList::new,
                false
        );
    }

    /**
     * Parses a <code>UseDeclaration</code>.
     * <em>
     * UseDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseTypeDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseMultDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseAllDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedTypeDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedMultDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;UseSharedAllDeclaration
     * </em>
     * @return An <code>ASTUseDeclaration</code>.
     */
    public ASTUseDeclaration parseUseDeclaration() {
        Location loc = curr().getLocation();
        if (accept(USE) == null) {
            throw internalError(USE);
        }

        ASTTypeName tn = getNamesParser().parseTypeName();

        if (isCurr(DOT) && isNext(OPEN_BRACE)) {
            return parseUseMultDeclaration(loc, tn);
        }
        else if (isCurr(DOT) && isNext(PLUS)) {
            return parseUseAllDeclaration(loc, tn);
        }
        else {
            return parseUseTypeDeclaration(loc, tn);
        }
    }

    /**
     * Parses a <code>UseMultDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseMultDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . { IdentifierList } ;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseMultDeclaration</code>.
     */
    public ASTUseMultDeclaration parseUseMultDeclaration(Location loc, ASTTypeName tn) {
        ASTNamespaceOrTypeName namespaceOrTypeName = tn.convertToNamespaceOrTypeName();
        if (accept(DOT) == null || accept(OPEN_BRACE) == null) {
            throw internalError(curr().getType());
        }
        ASTIdentifierList identifiers = getNamesParser().parseIdentifierList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return new ASTUseMultDeclaration(loc, namespaceOrTypeName, identifiers);
    }

    /**
     * Parses a <code>UseAllDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseAllDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use NamespaceOrTypeName . * ;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseAllDeclaration</code>.
     */
    public ASTUseAllDeclaration parseUseAllDeclaration(Location loc, ASTTypeName tn) {
        ASTUseAllDeclaration node = new ASTUseAllDeclaration(loc, tn.convertToNamespaceOrTypeName());
        if (accept(DOT) == null || accept(PLUS) == null) {
            throw internalError(curr().getType());
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return node;
    }

    /**
     * Parses a <code>UseTypeDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseTypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use TypeName ;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseTypeDeclaration</code>.
     */
    public ASTUseTypeDeclaration parseUseTypeDeclaration(Location loc, ASTTypeName tn) {
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return new ASTUseTypeDeclaration(loc, tn);
    }

    /**
     * Parses a <code>TypeDeclarationList</code>.
     * <em>
     * TypeDeclarationList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration {TypeDeclaration}
     * </em>
     * @return An <code>ASTTypeDeclarationList</code>.
     */
    public ASTTypeDeclarationList parseTypeDeclarationList() {
        Location loc = curr().getLocation();
        List<ASTTypeDeclaration> children = new ArrayList<>();
        Predicate<Token> isOnInitialToken = t ->
                Arrays.asList(ABSTRACT, CONSTANT, OVERRIDE,  // General Modifiers
                              CLASS, INTERFACE  // Types
                        )
                        .contains(t.getType());
        while (!isCurr(EOF)) {
            if (isOnInitialToken.test(curr())) {
                children.add(parseTypeDeclaration());
            }
            else {
                error(curr().getLocation(), "Unexpected token '" +
                        curr().getType().getRepresentation() + "'.");
                accept(curr().getType());
            }
        }
        return new ASTTypeDeclarationList(loc, children);
    }

    /**
     * Parses a <code>TypeDeclaration</code>.
     * <em>
     * TypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration
     * </em>
     * @return An <code>ASTTypeDeclaration</code>.
     */
    public ASTTypeDeclaration parseTypeDeclaration() {
        Location loc = curr().getLocation();
        ClassesParser cp = getClassesParser();

        ASTGeneralModifierList genModList = cp.parseGeneralModifierList();
        return switch (curr().getType()) {
            case CLASS -> cp.parseClassDeclaration(loc, genModList);
            case INTERFACE -> cp.parseInterfaceDeclaration(loc, genModList);
            default -> throw internalError("class expected");
        };
    }
}
