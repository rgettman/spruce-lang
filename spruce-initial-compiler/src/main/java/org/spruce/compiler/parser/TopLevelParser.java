package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.classes.ASTAnnotationList;
import org.spruce.compiler.ast.classes.ASTGeneralModifierList;
import org.spruce.compiler.ast.classes.ASTTypeDeclaration;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTIdentifierList;
import org.spruce.compiler.ast.names.ASTNamespaceOrTypeName;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.toplevel.*;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;

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
        ClassesParser classesParser = getClassesParser();
        ASTNamespaceDeclaration namespaceDecl = null;
        ASTAnnotationList annList = classesParser.parseAnnotationList();

        // Namespace
        if (isCurr(NAMESPACE)) {
            namespaceDecl = parseNamespaceDeclaration(annList);
            annList = classesParser.parseAnnotationList();
        }

        // Use
        ASTUseDeclarationList useDeclList = parseUseDeclarationList();
        if (!annList.getChildren().isEmpty() && !useDeclList.getChildren().isEmpty()) {
            Location errorLoc = annList.get(0).getLocation();
            throw new CompileException(errorLoc, "Annotations are not allowed on use declarations.");
        }
        else if (annList.getChildren().isEmpty() && !useDeclList.getChildren().isEmpty()) {
            annList = classesParser.parseAnnotationList();
        }
        // Else the use declaration list was empty, and we can use the above annotation list for types.

        // Type
        ASTTypeDeclarationList typeDeclList = parseTypeDeclarationList(annList);
        if (namespaceDecl != null) {
            return new ASTOrdinaryCompilationUnit(loc, namespaceDecl, useDeclList, typeDeclList);
        }
        return new ASTOrdinaryCompilationUnit(loc, useDeclList, typeDeclList);
    }

    /**
     * Parses a <code>NamespaceDeclaration</code>.
     * <em>
     * NamespaceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] namespace NamespaceName
     * </em>
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @return An <code>ASTNamespaceDeclaration</code>.
     */

    public ASTNamespaceDeclaration parseNamespaceDeclaration(ASTAnnotationList annList) {
        Location loc = curr().getLocation();
        if (accept(NAMESPACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected namespace.");
        }
        ASTNamespaceDeclaration node = new ASTNamespaceDeclaration(loc, annList, getNamesParser().parseNamespaceName());
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
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
                return parseUseSharedMultDeclaration(loc, tn);
            }
            else if (isCurr(DOT) && isNext(STAR)) {
                return parseUseSharedAllDeclaration(loc, tn);
            }
            else {
                return parseUseSharedTypeDeclaration(loc, tn);
            }
        }
        else {
            if (isCurr(DOT) && isNext(OPEN_BRACE)) {
                return parseUseMultDeclaration(loc, tn);
            }
            else if (isCurr(DOT) && isNext(STAR)) {
                return parseUseAllDeclaration(loc, tn);
            }
            else {
                return parseUseTypeDeclaration(loc, tn);
            }
        }
    }

    /**
     * Parses a <code>UseSharedMultDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseSharedMultDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . { IdentifierList } ;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseSharedMultDeclaration</code>.
     */
    public ASTUseSharedMultDeclaration parseUseSharedMultDeclaration(Location loc, ASTTypeName tn) {
        if (accept(DOT) == null || accept(OPEN_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot then '{'.");
        }
        ASTIdentifierList identifierList = getNamesParser().parseIdentifierList();
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        return new ASTUseSharedMultDeclaration(loc, tn, identifierList);
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
            throw new CompileException(curr().getLocation(), "Expected dot then '{'.");
        }
        ASTIdentifierList identifiers = getNamesParser().parseIdentifierList();
        if (accept(CLOSE_BRACE) == null) {
            throw new CompileException(curr().getLocation(), "Expected '}'");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Missing semicolon.");
        }
        return new ASTUseMultDeclaration(loc, namespaceOrTypeName, identifiers);
    }

    /**
     * Parses a <code>UseSharedAllDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseSharedAllDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . * ;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTUseSharedAllDeclaration</code>.
     */
    public ASTUseSharedAllDeclaration parseUseSharedAllDeclaration(Location loc, ASTTypeName tn) {
        ASTUseSharedAllDeclaration node = new ASTUseSharedAllDeclaration(loc, tn);
        if (accept(DOT) == null || accept(STAR) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot, star.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        return node;
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
        if (accept(DOT) == null || accept(STAR) == null) {
            throw new CompileException(curr().getLocation(), "Expected dot, star.");
        }
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        return node;
    }

    /**
     * Parses a <code>UseSharedTypeDeclaration</code>, given an already
     * parsed type name.
     * <em>
     * UseSharedTypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;use shared TypeName . Identifier;
     * </em>
     * @param tn An already parsed <code>ASTTypeName</code> as a type name.
     * @return An <code>ASTUseSharedTypeDeclaration</code>.
     */
    public ASTUseSharedTypeDeclaration parseUseSharedTypeDeclaration(Location loc, ASTTypeName tn) {
        if (accept(SEMICOLON) == null) {
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        // Extract identifier, last child of type name.
        List<ASTIdentifier> children = tn.getTypedChildren();
        ASTIdentifier identifier = children.get(children.size() - 1);
        children.remove(children.size() - 1);
        ASTTypeName actual = new ASTTypeName(tn.getLocation(), children);
        return new ASTUseSharedTypeDeclaration(loc, actual, identifier);
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
            throw new CompileException(curr().getLocation(), "Expected semicolon.");
        }
        return new ASTUseTypeDeclaration(loc, tn);
    }

    /**
     * Parses a <code>TypeDeclarationList</code>, given an already parsed
     * AnnotationList.
     * <em>
     * TypeDeclarationList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration {TypeDeclaration}
     * </em>
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @return An <code>ASTTypeDeclarationList</code>.
     */
    public ASTTypeDeclarationList parseTypeDeclarationList(ASTAnnotationList annList) {
        Location loc = curr().getLocation();
        ASTAnnotationList currAnnList = annList;
        Predicate<Token> isOnInitialToken = t ->
                Arrays.asList(AT_SIGN, PUBLIC, INTERNAL, PROTECTED, PRIVATE, ABSTRACT, SHARED,
                              CLASS, ENUM, INTERFACE, ANNOTATION, RECORD)
                        .contains(t.getType());
        if (isOnInitialToken.test(curr())) {
            List<ASTTypeDeclaration> children = new ArrayList<>();
            children.add(parseTypeDeclaration(currAnnList));
            currAnnList = getClassesParser().parseAnnotationList();
            while (isOnInitialToken.test(curr())) {
                children.add(parseTypeDeclaration(currAnnList));
                currAnnList = getClassesParser().parseAnnotationList();
            }
            return new ASTTypeDeclarationList(loc, children);
        }
        else {
            return new ASTTypeDeclarationList(curr().getLocation(), new ArrayList<>());
        }
    }

    /**
     * Parses a <code>TypeDeclaration</code>, given an already parsed
     * AnnotationList.
     * <em>
     * TypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
     * </em>
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @return An <code>ASTTypeDeclaration</code>.
     */
    public ASTTypeDeclaration parseTypeDeclaration(ASTAnnotationList annList) {
        Location loc = curr().getLocation();
        ClassesParser cp = getClassesParser();
        ASTKeywordNode accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null) {
            accessMod = cp.parseAccessModifier();
        }
        ASTGeneralModifierList genModList = cp.parseGeneralModifierList();
        return switch (curr().getType()) {
            case CLASS -> cp.parseClassDeclaration(loc, annList, accessMod, genModList);
            case ENUM -> cp.parseEnumDeclaration(loc, annList, accessMod, genModList);
            case INTERFACE -> cp.parseInterfaceDeclaration(loc, annList, accessMod, genModList);
            case ANNOTATION -> cp.parseAnnotationDeclaration(loc, annList, accessMod, genModList);
            case RECORD -> {
                if (!genModList.getChildren().isEmpty()) {
                    throw new CompileException(curr().getLocation(), "General modifier not allowed here.");
                }
                yield cp.parseRecordDeclaration(loc, annList, accessMod);
            }
            case ADT -> {
                if (!genModList.getChildren().isEmpty()) {
                    throw new CompileException(curr().getLocation(), "General modifier not allowed here.");
                }
                yield cp.parseAdtDeclaration(loc, annList, accessMod);
            }
            default -> throw new CompileException(curr().getLocation(), "Expected class, enum, interface, annotation, record, or adt.");
        };
    }
}
