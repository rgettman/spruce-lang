package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
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
public class TopLevelParser extends BasicParser
{
    /**
     * Constructs a <code>TopLevelParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public TopLevelParser(Scanner scanner, Parser parser)
    {
        super(scanner, parser);
    }

    /**
     * Parses an <code>ASTNamespaceDeclaration</code>.
     * @return An <code>ASTNamespaceDeclaration</code>.
     */
    public ASTOrdinaryCompilationUnit parseOrdinaryCompilationUnit()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(NAMESPACE))
        {
            children.add(parseNamespaceDeclaration());
        }
        if (isCurr(RECOGNIZE))
        {
            children.add(parseRecognizeDeclarationList());
        }
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE, ABSTRACT, FINAL, SHARED, STRICTFP,
                CLASS, ENUM, INTERFACE, ANNOTATION)) != null)
        {
            children.add(parseTypeDeclarationList());
        }
        return new ASTOrdinaryCompilationUnit(loc, children);
    }

    /**
     * Parses an <code>ASTNamespaceDeclaration</code>.
     * @return An <code>ASTNamespaceDeclaration</code>.
     */
    public ASTNamespaceDeclaration parseNamespaceDeclaration()
    {
        Location loc = curr().getLocation();
        if (accept(NAMESPACE) == null)
        {
            throw new CompileException("Expected namespace.");
        }
        ASTNamespaceDeclaration node = new ASTNamespaceDeclaration(loc, Arrays.asList(getNamesParser().parseNamespaceName()));
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        node.setOperation(NAMESPACE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeDeclarationList</code>.
     * @return An <code>ASTRecognizeDeclarationList</code>.
     */
    public ASTRecognizeDeclarationList parseRecognizeDeclarationList()
    {
        return parseMultiple(
                t -> test(t, RECOGNIZE),
                "Expected recognize declaration.",
                this::parseRecognizeDeclaration,
                ASTRecognizeDeclarationList::new
        );
    }

    /**
     * Parses an <code>ASTRecognizeMultDeclaration</code>.
     * @return An <code>ASTRecognizeMultDeclaration</code>.
     */
    public ASTRecognizeDeclaration parseRecognizeDeclaration()
    {
        Location loc = curr().getLocation();
        if (accept(RECOGNIZE) == null)
        {
            throw new CompileException("Expected recognize.");
        }
        boolean isShared = false;
        if (isCurr(SHARED))
        {
            accept(SHARED);
            isShared = true;
        }
        ASTTypeName tn = getNamesParser().parseTypeName();
        if (isShared)
        {
            if (isCurr(DOT) && isNext(OPEN_BRACE))
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeSharedMultDeclaration(loc, tn)));
            }
            else if (isCurr(DOT) && isNext(STAR))
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeSharedAllDeclaration(loc, tn)));
            }
            else
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeSharedTypeDeclaration(loc, tn)));
            }
        }
        else
        {
            if (isCurr(DOT) && isNext(OPEN_BRACE))
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeMultDeclaration(loc, tn)));
            }
            else if (isCurr(DOT) && isNext(STAR))
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeAllDeclaration(loc, tn)));
            }
            else
            {
                return new ASTRecognizeDeclaration(loc, Arrays.asList(parseRecognizeTypeDeclaration(loc, tn)));
            }
        }
    }

    /**
     * Parses an <code>ASTRecognizeSharedMultDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeSharedMultDeclaration</code>.
     */
    public ASTRecognizeSharedMultDeclaration parseRecognizeSharedMultDeclaration(Location loc, ASTTypeName tn)
    {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn);
        if (accept(DOT) == null || accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected dot then '{'.");
        }
        children.add(getNamesParser().parseIdentifierList());
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTRecognizeSharedMultDeclaration node = new ASTRecognizeSharedMultDeclaration(loc, children);
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeMultDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeMultDeclaration</code>.
     */
    public ASTRecognizeMultDeclaration parseRecognizeMultDeclaration(Location loc, ASTTypeName tn)
    {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn.convertToNamespaceOrTypeName());
        if (accept(DOT) == null || accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected dot then '{'.");
        }
        children.add(getNamesParser().parseIdentifierList());
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTRecognizeMultDeclaration node = new ASTRecognizeMultDeclaration(loc, children);
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeSharedAllDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeSharedAllDeclaration</code>.
     */
    public ASTRecognizeSharedAllDeclaration parseRecognizeSharedAllDeclaration(Location loc, ASTTypeName tn)
    {
        ASTRecognizeSharedAllDeclaration node = new ASTRecognizeSharedAllDeclaration(loc, Arrays.asList(tn));
        if (accept(DOT) == null || accept(STAR) == null)
        {
            throw new CompileException("Expected dot, star.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeAllDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeAllDeclaration</code>.
     */
    public ASTRecognizeAllDeclaration parseRecognizeAllDeclaration(Location loc, ASTTypeName tn)
    {
        ASTRecognizeAllDeclaration node = new ASTRecognizeAllDeclaration(loc, Arrays.asList(tn.convertToNamespaceOrTypeName()));
        if (accept(DOT) == null || accept(STAR) == null)
        {
            throw new CompileException("Expected dot, star.");
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeSharedTypeDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeSharedTypeDeclaration</code>.
     */
    public ASTRecognizeSharedTypeDeclaration parseRecognizeSharedTypeDeclaration(Location loc, ASTTypeName tn)
    {
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        // Extract identifier, last child of type name.
        List<ASTNode> children = tn.getChildren();
        ASTIdentifier identifier = (ASTIdentifier) children.get(1);
        ASTNamespaceOrTypeName potn = (ASTNamespaceOrTypeName) children.get(0);
        ASTTypeName actual = new ASTTypeName(potn.getLocation(), potn.getChildren());
        actual.setOperation(potn.getOperation());
        ASTRecognizeSharedTypeDeclaration node = new ASTRecognizeSharedTypeDeclaration(loc, Arrays.asList(actual, identifier));
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTRecognizeTypeDeclaration</code>, given an already
     * parsed <code>ASTTypeName</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTRecognizeTypeDeclaration</code>.
     */
    public ASTRecognizeTypeDeclaration parseRecognizeTypeDeclaration(Location loc, ASTTypeName tn)
    {
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        ASTRecognizeTypeDeclaration node = new ASTRecognizeTypeDeclaration(loc, Arrays.asList(tn));
        node.setOperation(RECOGNIZE);
        return node;
    }

    /**
     * Parses an <code>ASTTypeDeclarationList</code>.
     * @return An <code>ASTTypeDeclarationList</code>.
     */
    public ASTTypeDeclarationList parseTypeDeclarationList()
    {
        return parseMultiple(
                t -> Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE, ABSTRACT, FINAL, SHARED, STRICTFP,
                        CLASS, ENUM, INTERFACE, ANNOTATION).contains(t.getType()),
                "Expected class, enum, interface, or annotation declaration.",
                this::parseTypeDeclaration,
                ASTTypeDeclarationList::new
        );
    }

    /**
     * Parses an <code>ASTTypeDeclaration</code>.
     * @return An <code>ASTTypeDeclaration</code>.
     */
    public ASTTypeDeclaration parseTypeDeclaration()
    {
        Location loc = curr().getLocation();
        ClassesParser cp = getClassesParser();
        ASTAccessModifier accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null)
        {
            accessMod = cp.parseAccessModifier();
        }
        ASTGeneralModifierList genModList = null;
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP)) != null)
        {
            genModList = cp.parseGeneralModifierList();
        }
        switch (curr().getType())
        {
        case CLASS:
            return new ASTTypeDeclaration(loc, Arrays.asList(cp.parseClassDeclaration(loc, accessMod, genModList)));
        case ENUM:
            return new ASTTypeDeclaration(loc, Arrays.asList(cp.parseEnumDeclaration(loc, accessMod, genModList)));
        case INTERFACE:
            return new ASTTypeDeclaration(loc, Arrays.asList(cp.parseInterfaceDeclaration(loc, accessMod, genModList)));
        case ANNOTATION:
            return new ASTTypeDeclaration(loc, Arrays.asList(cp.parseAnnotationDeclaration(loc, accessMod, genModList)));
        default:
            throw new CompileException("Expected class, enum, interface, or annotation.");
        }
    }
}
