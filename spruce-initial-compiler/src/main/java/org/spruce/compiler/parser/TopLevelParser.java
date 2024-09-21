package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.classes.ASTAccessModifier;
import org.spruce.compiler.ast.classes.ASTGeneralModifierList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.toplevel.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>TopLevelParser</code> is a <code>BasicParser</code> that parses
 * top-level productions.
 */
public class TopLevelParser extends BasicParser {
    /**
     * Constructs a <code>TopLevelParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public TopLevelParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTOrdinaryCompilationUnit</code>.
     * @return An <code>ASTOrdinaryCompilationUnit</code>.
     */
    public ASTOrdinaryCompilationUnit parseOrdinaryCompilationUnit() {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(NAMESPACE)) {
            children.add(parseNamespaceDeclaration());
        }
        if (isCurr(USE)) {
            children.add(parseUseDeclarationList());
        }
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE, ABSTRACT, SHARED,
                CLASS, ENUM, INTERFACE, ANNOTATION)) != null) {
            children.add(parseTypeDeclarationList());
        }
        return new ASTOrdinaryCompilationUnit(loc, children);
    }

    /**
     * Parses an <code>ASTNamespaceDeclaration</code>.
     * @return An <code>ASTNamespaceDeclaration</code>.
     */
    public ASTNamespaceDeclaration parseNamespaceDeclaration() {
        Location loc = curr().getLocation();
        if (accept(NAMESPACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected namespace.");
        }
        ASTNamespaceDeclaration node = new ASTNamespaceDeclaration(loc, Collections.singletonList(getNamesParser().parseNamespaceName()));
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        node.setOperation(NAMESPACE);
        return node;
    }

    /**
     * Parses an <code>ASTUseDeclarationList</code>.
     * @return An <code>ASTUseDeclarationList</code>.
     */
    public ASTUseDeclarationList parseUseDeclarationList() {
        return parseMultiple(
                t -> test(t, USE),
                "Expected use declaration.",
                this::parseUseDeclaration,
                ASTUseDeclarationList::new
        );
    }

    /**
     * Parses an <code>ASTUseDeclaration</code>.
     * @return An <code>ASTUseDeclaration</code>.
     */
    public ASTUseDeclaration parseUseDeclaration() {
        Location loc = curr().getLocation();
        if (accept(USE) == null) {
            throw new CompileException(curr().getLocation(), "Expected use.");
        }
        boolean isShared = false;
        if (isCurr(SHARED)) {
            accept(SHARED);
            isShared = true;
        }
        ASTTypeName tn = getNamesParser().parseTypeName();
        if (isShared) {
            if (isCurr(DOT) && isNext(OPEN_BRACE)) {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseSharedMultDeclaration(loc, tn)));
            }
            else if (isCurr(DOT) && isNext(STAR)) {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseSharedAllDeclaration(loc, tn)));
            }
            else {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseSharedTypeDeclaration(loc, tn)));
            }
        }
        else {
            if (isCurr(DOT) && isNext(OPEN_BRACE)) {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseMultDeclaration(loc, tn)));
            }
            else if (isCurr(DOT) && isNext(STAR)) {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseAllDeclaration(loc, tn)));
            }
            else {
                return new ASTUseDeclaration(loc, Collections.singletonList(parseUseTypeDeclaration(loc, tn)));
            }
        }
    }

    /**
     * Parses an <code>ASTUseSharedMultDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseSharedMultDeclaration</code>.
     */
    public ASTUseSharedMultDeclaration parseUseSharedMultDeclaration(Location loc, ASTTypeName tn) {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn);
        if (accept(DOT) == null || accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot then '{'.");
        }
        children.add(getNamesParser().parseIdentifierList());
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTUseSharedMultDeclaration node = new ASTUseSharedMultDeclaration(loc, children);
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTUseMultDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseMultDeclaration</code>.
     */
    public ASTUseMultDeclaration parseUseMultDeclaration(Location loc, ASTTypeName tn) {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn.convertToNamespaceOrTypeName());
        if (accept(DOT) == null || accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot then '{'.");
        }
        children.add(getNamesParser().parseIdentifierList());
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        ASTUseMultDeclaration node = new ASTUseMultDeclaration(loc, children);
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTUseSharedAllDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseSharedAllDeclaration</code>.
     */
    public ASTUseSharedAllDeclaration parseUseSharedAllDeclaration(Location loc, ASTTypeName tn) {
        ASTUseSharedAllDeclaration node = new ASTUseSharedAllDeclaration(loc, Collections.singletonList(tn));
        if (accept(DOT) == null || accept(STAR) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot, star.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTUseAllDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseAllDeclaration</code>.
     */
    public ASTUseAllDeclaration parseUseAllDeclaration(Location loc, ASTTypeName tn) {
        ASTUseAllDeclaration node = new ASTUseAllDeclaration(loc, Collections.singletonList(tn.convertToNamespaceOrTypeName()));
        if (accept(DOT) == null || accept(STAR) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot, star.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTUseSharedTypeDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseSharedTypeDeclaration</code>.
     */
    public ASTUseSharedTypeDeclaration parseUseSharedTypeDeclaration(Location loc, ASTTypeName tn) {
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        // Extract identifier, last child of type name.
        List<ASTNode> children = tn.getChildren();
        ASTIdentifier identifier = (ASTIdentifier) children.get(1);
        ASTNamespaceOrTypeName notn = (ASTNamespaceOrTypeName) children.get(0);
        ASTTypeName actual = new ASTTypeName(notn.getLocation(), notn.getChildren());
        actual.setOperation(notn.getOperation());
        ASTUseSharedTypeDeclaration node = new ASTUseSharedTypeDeclaration(loc, Arrays.asList(actual, identifier));
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTUseTypeDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseTypeDeclaration</code>.
     */
    public ASTUseTypeDeclaration parseUseTypeDeclaration(Location loc, ASTTypeName tn) {
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        ASTUseTypeDeclaration node = new ASTUseTypeDeclaration(loc, Collections.singletonList(tn));
        node.setOperation(USE);
        return node;
    }

    /**
     * Parses an <code>ASTTypeDeclarationList</code>.
     * @return An <code>ASTTypeDeclarationList</code>.
     */
    public ASTTypeDeclarationList parseTypeDeclarationList() {
        return parseMultiple(
                t -> Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE, ABSTRACT, SHARED,
                        CLASS, ENUM, INTERFACE, ANNOTATION, RECORD).contains(t.getType()),
                "Expected class, enum, interface, annotation, or record declaration.",
                this::parseTypeDeclaration,
                ASTTypeDeclarationList::new
        );
    }

    /**
     * Parses an <code>ASTTypeDeclaration</code>.
     * @return An <code>ASTTypeDeclaration</code>.
     */
    public ASTTypeDeclaration parseTypeDeclaration() {
        Location loc = curr().getLocation();
        ClassesParser cp = getClassesParser();
        ASTAccessModifier accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null) {
            accessMod = cp.parseAccessModifier();
        }
        ASTGeneralModifierList genModList = null;
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, SHARED)) != null) {
            genModList = cp.parseGeneralModifierList();
        }
        return switch (curr().getType()) {
            case CLASS ->
                    new ASTTypeDeclaration(loc, Collections.singletonList(cp.parseClassDeclaration(loc, accessMod, genModList)));
            case ENUM ->
                    new ASTTypeDeclaration(loc, Collections.singletonList(cp.parseEnumDeclaration(loc, accessMod, genModList)));
            case INTERFACE ->
                    new ASTTypeDeclaration(loc, Collections.singletonList(cp.parseInterfaceDeclaration(loc, accessMod, genModList)));
            case ANNOTATION ->
                    new ASTTypeDeclaration(loc, Collections.singletonList(cp.parseAnnotationDeclaration(loc, accessMod, genModList)));
            case RECORD -> {
                if (genModList != null) {
                    throw new CompileException(curr().getLocation(), "General modifier not allowed here.");
                }
                yield new ASTTypeDeclaration(loc, Collections.singletonList(cp.parseRecordDeclaration(loc, accessMod)));
            }
            default -> throw new CompileException(curr().getLocation(), "Expected class, enum, interface, annotation, or record.");
        };
    }
}
