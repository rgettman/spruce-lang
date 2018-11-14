package org.spruce.compiler.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.expressions.ASTPrimary;
import org.spruce.compiler.ast.names.ASTExpressionName;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameters;
import org.spruce.compiler.exception.CompileException;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>ClassesParser</code> is a <code>StatementsParser</code> that also parses
 * classes.
 */
public class ClassesParser extends StatementsParser
{
    /**
     * Parses an <code>ASTAnnotationDeclaration</code>.
     * @return An <code>ASTAnnotationDeclaration</code>.
     */
    public ASTAnnotationDeclaration parseAnnotationDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, SHARED, STRICTFP)) != null)
        {
            children.add(parseInterfaceModifierList());
        }
        if (accept(ANNOTATION) == null)
        {
            throw new CompileException("Expected annotation.");
        }
        children.add(parseIdentifier());
        children.add(parseAnnotationBody());
        ASTAnnotationDeclaration node = new ASTAnnotationDeclaration(loc, children);
        node.setOperation(ANNOTATION);
        return node;
    }

    /**
     * Parses an <code>ASTAnnotationDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code> and <code>ASTGeneralModifierList</code>.
     * @param loc The <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @return An <code>ASTAnnotationDeclaration</code>.
     */
    public ASTAnnotationDeclaration parseAnnotationDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            children.add(gms.convertToSpecificList(
                    "Unexpected interface modifier.",
                    Arrays.asList(ABSTRACT, SHARED, STRICTFP),
                    ASTInterfaceModifierList::new
            ));
        }
        if (accept(ANNOTATION) == null)
        {
            throw new CompileException("Expected annotation.");
        }
        children.add(parseIdentifier());
        children.add(parseAnnotationBody());
        ASTAnnotationDeclaration node = new ASTAnnotationDeclaration(loc, children);
        node.setOperation(ANNOTATION);
        return node;
    }

    /**
     * Constructs a <code>ClassesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     */
    public ClassesParser(Scanner scanner)
    {
        super(scanner);
    }

    /**
     * Parses an <code>ASTAnnotationBody</code>.
     * @return An <code>ASTAnnotationBody</code>.
     */
    public ASTAnnotationBody parseAnnotationBody()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseAnnotationPartList());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        ASTAnnotationBody node = new ASTAnnotationBody(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTAnnotationPartList</code>.
     * @return An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationPartList parseAnnotationPartList()
    {
        return parseMultiple(
                t -> Arrays.asList(PUBLIC, PRIVATE, INTERNAL, PROTECTED,
                        ABSTRACT, FINAL, SHARED, STRICTFP, CLASS, INTERFACE,
                        CONSTANT, IDENTIFIER)
                        .contains(t.getType()),
                "Expected constant or element declaration.",
                this::parseAnnotationPart,
                ASTAnnotationPartList::new
        );
    }

    /**
     * Parses an <code>ASTAnnotationPart</code>.
     * @return An <code>ASTAnnotationPart</code>.
     */
    public ASTAnnotationPart parseAnnotationPart()
    {
        Location loc = curr().getLocation();
        ASTAccessModifier accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null)
        {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = null;
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, CONSTANT, DEFAULT, FINAL, OVERRIDE, SHARED, STRICTFP)) != null)
        {
            genModList = parseGeneralModifierList();
        }

        // class/enum/interface/annotation
        switch (curr().getType())
        {
        case CLASS:
            return new ASTAnnotationPart(loc, Arrays.asList(parseClassDeclaration(loc, accessMod, genModList)));
        case ENUM:
            return new ASTAnnotationPart(loc, Arrays.asList(parseEnumDeclaration(loc, accessMod, genModList)));
        case INTERFACE:
            return new ASTAnnotationPart(loc, Arrays.asList(parseInterfaceDeclaration(loc, accessMod, genModList)));
        case ANNOTATION:
            return new ASTAnnotationPart(loc, Arrays.asList(parseAnnotationDeclaration(loc, accessMod, genModList)));
        }

        ASTTypeParameters typeParams = null;
        if (isCurr(LESS_THAN))
        {
            typeParams = parseTypeParameters();
        }

        ASTDataType dt = parseDataType();
        if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))
        {
            if (typeParams != null)
            {
                throw new CompileException("Type parameters not allowed on annotation element declaration.");
            }
            if (genModList != null)
            {
                throw new CompileException("Method modifiers not allowed on annotation element declaration.");
            }
            if (accessMod != null)
            {
                throw new CompileException("Access modifiers not allowed on annotation element declaration.");
            }
            return new ASTAnnotationPart(loc, Arrays.asList(parseAnnotationTypeElementDeclaration(loc, dt)));
        }
        else
        {
            if (typeParams != null)
            {
                throw new CompileException("Type parameters not allowed on constant declaration.");
            }
            return new ASTAnnotationPart(loc, Arrays.asList(parseConstantDeclaration(loc, accessMod, genModList, dt)));
        }
    }

    /**
     * Parses an <code>ASTAnnotationTypeElementDeclaration</code>.
     * @return An <code>ASTAnnotationTypeElementDeclaration</code>.
     */
    public ASTAnnotationTypeElementDeclaration parseAnnotationTypeElementDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(parseDataType());
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('");
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'");
        }
        if (isCurr(DEFAULT))
        {
            children.add(parseDefaultValue());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTAnnotationTypeElementDeclaration node = new ASTAnnotationTypeElementDeclaration(loc, children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * Parses an <code>ASTAnnotationTypeElementDeclaration</code>, given an
     * already parsed <code>ASTDataType</code>.
     * @param loc The <code>Location</code>.
     * @param dt The <code>ASTDataType</code>.
     * @return An <code>ASTAnnotationTypeElementDeclaration</code>.
     */
    public ASTAnnotationTypeElementDeclaration parseAnnotationTypeElementDeclaration(Location loc, ASTDataType dt)
    {
        List<ASTNode> children = new ArrayList<>(3);
        children.add(dt);
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('");
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'");
        }
        if (isCurr(DEFAULT))
        {
            children.add(parseDefaultValue());
        }
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Missing semicolon.");
        }
        ASTAnnotationTypeElementDeclaration node = new ASTAnnotationTypeElementDeclaration(loc, children);
        node.setOperation(OPEN_PARENTHESIS);
        return node;
    }

    /**
     * Parses an <code>ASTDefaultValue</code>.
     * @return An <code>ASTDefaultValue</code>.
     */
    public ASTDefaultValue parseDefaultValue()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        if (accept(DEFAULT) == null)
        {
            throw new CompileException("Expected default.");
        }
        children.add(parseElementValue());
        ASTDefaultValue node = new ASTDefaultValue(loc, children);
        node.setOperation(DEFAULT);
        return node;
    }

    /**
     * Parses an <code>ASTAnnotation</code>.
     * @return An <code>ASTAnnotation</code>.
     */
    public ASTAnnotation parseAnnotation()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        if (accept(AT_SIGN) == null)
        {
            throw new CompileException("Expected '@'.");
        }
        ASTTypeName tn = parseTypeName();
        if (isCurr(OPEN_PARENTHESIS))
        {
            accept(OPEN_PARENTHESIS);
            if (isCurr(CLOSE_PARENTHESIS))
            {
                children.add(parseNormalAnnotation(loc, tn));
            }
            else if (isCurr(OPEN_BRACE))
            {
                children.add(parseSingleElementAnnotation(loc, tn));
            }
            else if (isCurr(IDENTIFIER) && isNext(ASSIGNMENT))
            {
                children.add(parseNormalAnnotation(loc, tn));
            }
            else
            {
                // Conditional Expression.
                children.add(parseSingleElementAnnotation(loc, tn));
            }
        }
        else
        {
            children.add(parseMarkerAnnotation(loc, tn));
        }
        ASTAnnotation node = new ASTAnnotation(loc, children);
        node.setOperation(AT_SIGN);
        return node;
    }

    /**
     * Parses an <code>ASTMarkerAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.
     * @param loc The <code>Location</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTMarkerAnnotation</code>.
     */
    public ASTMarkerAnnotation parseMarkerAnnotation(Location loc, ASTTypeName tn)
    {
        List<ASTNode> children = new ArrayList<>(1);
        children.add(tn);
        ASTMarkerAnnotation node = new ASTMarkerAnnotation(loc, children);
        node.setOperation(AT_SIGN);
        return node;
    }

    /**
     * Parses an <code>ASTSingleElementAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.  An open parenthesis ('(') has already been consumed.
     * @param loc The <code>Location</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTSingleElementAnnotation</code>.
     */
    public ASTSingleElementAnnotation parseSingleElementAnnotation(Location loc, ASTTypeName tn)
    {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn);
        children.add(parseElementValue());
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        ASTSingleElementAnnotation node = new ASTSingleElementAnnotation(loc, children);
        node.setOperation(AT_SIGN);
        return node;
    }

    /**
     * Parses an <code>ASTNormalAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.  An open parenthesis ('(') has already been consumed.
     * @param loc The <code>Location</code>.
     * @param tn An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTNormalAnnotation</code>.
     */
    public ASTNormalAnnotation parseNormalAnnotation(Location loc, ASTTypeName tn)
    {
        List<ASTNode> children = new ArrayList<>(2);
        children.add(tn);
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseElementValuePairList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        ASTNormalAnnotation node = new ASTNormalAnnotation(loc, children);
        node.setOperation(AT_SIGN);
        return node;
    }

    /**
     * Parses an <code>ASTElementValuePairList</code>.
     * @return An <code>ASTElementValuePairList</code>.
     */
    public ASTElementValuePairList parseElementValuePairList()
    {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier.",
                COMMA,
                this::parseElementValuePair,
                ASTElementValuePairList::new
        );
    }

    /**
     * Parses an <code>ASTElementValuePair</code>.
     * @return An <code>ASTElementValuePair</code>.
     */
    public ASTElementValuePair parseElementValuePair()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        children.add(parseIdentifier());
        if (accept(ASSIGNMENT) == null)
        {
            throw new CompileException("Expected assignment operator ':='.");
        }
        children.add(parseElementValue());
        ASTElementValuePair node = new ASTElementValuePair(loc, children);
        node.setOperation(ASSIGNMENT);
        return node;
    }

    /**
     * Parses an <code>ASTElementValueArrayInitializer</code>.
     * @return An <code>ASTElementValueArrayInitializer</code>.
     */
    public ASTElementValueArrayInitializer parseElementValueArrayInitializer()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseElementValueList());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        ASTElementValueArrayInitializer node = new ASTElementValueArrayInitializer(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTElementValueList</code>.
     * @return An <code>ASTElementValueList</code>.
     */
    public ASTElementValueList parseElementValueList()
    {
        return parseList(
                ExpressionsParser::isPrimary,
                "Expected value.",
                COMMA,
                this::parseElementValue,
                ASTElementValueList::new
        );
    }

    /**
     * Parses an <code>ASTElementValue</code>.
     * @return An <code>ASTElementValue</code>.
     */
    public ASTElementValue parseElementValue()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        // Could also be Annotation.
        if (isCurr(OPEN_BRACE))
        {
            children.add(parseElementValueArrayInitializer());
        }
        else if (isCurr(AT_SIGN))
        {
            children.add(parseAnnotation());
        }
        else
        {
            children.add(parseConditionalExpression());
        }
        return new ASTElementValue(loc, children);
    }

    /**
     * Parses an <code>ASTInterfaceDeclaration</code>.
     * @return An <code>ASTInterfaceDeclaration</code>.
     */
    public ASTInterfaceDeclaration parseInterfaceDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(6);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, SHARED, STRICTFP)) != null)
        {
            children.add(parseInterfaceModifierList());
        }
        if (accept(INTERFACE) == null)
        {
            throw new CompileException("Expected interface.");
        }
        children.add(parseIdentifier());
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        if (isCurr(EXTENDS))
        {
            children.add(parseExtendsInterfaces());
        }
        children.add(parseInterfaceBody());
        ASTInterfaceDeclaration node = new ASTInterfaceDeclaration(loc, children);
        node.setOperation(INTERFACE);
        return node;
    }

    /**
     * Parses an <code>ASTInterfaceDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code> and <code>ASTGeneralModifierList</code>.
     * @param loc The <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @return An <code>ASTInterfaceDeclaration</code>.
     */
    public ASTInterfaceDeclaration parseInterfaceDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms)
    {
        List<ASTNode> children = new ArrayList<>(6);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            children.add(gms.convertToSpecificList(
                    "Unexpected interface modifier.",
                    Arrays.asList(ABSTRACT, SHARED, STRICTFP),
                    ASTInterfaceModifierList::new
            ));
        }
        if (accept(INTERFACE) == null)
        {
            throw new CompileException("Expected interface.");
        }
        children.add(parseIdentifier());
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        if (isCurr(EXTENDS))
        {
            children.add(parseExtendsInterfaces());
        }
        children.add(parseInterfaceBody());
        ASTInterfaceDeclaration node = new ASTInterfaceDeclaration(loc, children);
        node.setOperation(INTERFACE);
        return node;
    }

    /**
     * Parses an <code>ASTInterfaceModifierList</code>.
     * @return An <code>ASTInterfaceModifierList</code>.
     */
    public ASTInterfaceModifierList parseInterfaceModifierList()
    {
        return parseGeneralModifierList()
                .convertToSpecificList("Expected abstract, shared, or strictfp.",
                        Arrays.asList(ABSTRACT, SHARED, STRICTFP),
                        ASTInterfaceModifierList::new);
    }

    /**
     * Parses an <code>ASTExtendsInterfaces</code>.
     * @return An <code>ASTExtendsInterfaces</code>.
     */
    public ASTExtendsInterfaces parseExtendsInterfaces()
    {
        Location loc = curr().getLocation();
        if (accept(EXTENDS) == null)
        {
            throw new CompileException("Expected extends.");
        }
        ASTExtendsInterfaces node = new ASTExtendsInterfaces(loc, Arrays.asList(parseDataTypeNoArrayList()));
        node.setOperation(EXTENDS);
        return node;
    }

    /**
     * Parses an <code>ASTInterfaceBody</code>.
     * @return An <code>ASTInterfaceBody</code>.
     */
    public ASTInterfaceBody parseInterfaceBody()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseInterfacePartList());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        ASTInterfaceBody node = new ASTInterfaceBody(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTInterfacePartList</code>.
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList parseInterfacePartList()
    {
        return parseMultiple(
                t -> Arrays.asList(PUBLIC, PRIVATE, INTERNAL, PROTECTED,
                        ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP, CLASS, INTERFACE,
                        DEFAULT, CONST, CONSTANT, VOID, IDENTIFIER, LESS_THAN)
                        .contains(t.getType()),
                "Expected constant or method declaration.",
                this::parseInterfacePart,
                ASTInterfacePartList::new
        );
    }

    /**
     * Parses an <code>ASTInterfacePart</code>.
     * @return An <code>ASTInterfacePart</code>.
     */
    public ASTInterfacePart parseInterfacePart()
    {
        Location loc = curr().getLocation();
        ASTAccessModifier accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null)
        {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = null;
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, CONSTANT, DEFAULT, FINAL, OVERRIDE, SHARED, STRICTFP)) != null)
        {
            genModList = parseGeneralModifierList();
        }

        // class/enum/interface/annotation
        switch (curr().getType())
        {
        case CLASS:
            return new ASTInterfacePart(loc, Arrays.asList(parseClassDeclaration(loc, accessMod, genModList)));
        case ENUM:
            return new ASTInterfacePart(loc, Arrays.asList(parseEnumDeclaration(loc, accessMod, genModList)));
        case INTERFACE:
            return new ASTInterfacePart(loc, Arrays.asList(parseInterfaceDeclaration(loc, accessMod, genModList)));
        case ANNOTATION:
            return new ASTInterfacePart(loc, Arrays.asList(parseAnnotationDeclaration(loc, accessMod, genModList)));
        }

        ASTTypeParameters typeParams = null;
        if (isCurr(LESS_THAN))
        {
            typeParams = parseTypeParameters();
        }

        if (isAcceptedOperator(Arrays.asList(CONST, VOID)) != null)
        {
            return new ASTInterfacePart(loc, Arrays.asList(parseInterfaceMethodDeclaration(loc, accessMod, genModList, typeParams)));
        }
        else
        {
            ASTDataType dt = parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))
            {
                return new ASTInterfacePart(loc, Arrays.asList(parseInterfaceMethodDeclaration(loc, accessMod, genModList, typeParams, dt)));
            }
            else
            {
                if (typeParams != null)
                {
                    throw new CompileException("Type parameters not allowed on constant declaration.");
                }
                return new ASTInterfacePart(loc, Arrays.asList(parseConstantDeclaration(loc, accessMod, genModList, dt)));
            }
        }
    }

    /**
     * Parses an <code>ASTInterfaceMethodDeclaration</code>.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED, STRICTFP)) != null)
        {
            children.add(parseInterfaceMethodModifierList());
        }
        children.add(parseMethodHeader());
        children.add(parseMethodBody());
        return new ASTInterfaceMethodDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTInterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>,
     * <code>ASTTypeParameters</code>.
     * @param loc The starting <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTTypeParameters tps, ASTDataType dt)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTInterfaceMethodModifierList mms = gms.convertToSpecificList(
                    "Unexpected interface method modifier.",
                    Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED, STRICTFP),
                    ASTInterfaceMethodModifierList::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        children.add(parseMethodHeader(tps, dt));
        children.add(parseMethodBody());
        return new ASTInterfaceMethodDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTInterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>,
     * <code>ASTTypeParameters</code>.
     * @param loc The starting <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTTypeParameters tps)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTInterfaceMethodModifierList mms = gms.convertToSpecificList(
                    "Unexpected interface method modifier.",
                    Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED, STRICTFP),
                    ASTInterfaceMethodModifierList::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        if (tps != null)
        {
            children.add(parseMethodHeader(tps));
        }
        else
        {
            children.add(parseMethodHeader());
        }
        children.add(parseMethodBody());
        return new ASTInterfaceMethodDeclaration(loc, children);
    }


    /**
     * Parses an <code>ASTInterfaceMethodModifierList</code>.
     * @return An <code>ASTInterfaceMethodModifierList</code>.
     */
    public ASTInterfaceMethodModifierList parseInterfaceMethodModifierList()
    {
        return parseGeneralModifierList()
                .convertToSpecificList("Expected abstract, default, override, shared, or strictfp.",
                        Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED, STRICTFP),
                        ASTInterfaceMethodModifierList::new);
    }

    /**
     * Parses an <code>ASTConstantDeclaration</code>.
     * @return An <code>ASTConstantDeclaration</code>.
     */
    public ASTConstantDeclaration parseConstantDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(CONSTANT))
        {
            children.add(parseConstantModifier());
        }
        children.add(parseDataType());
        children.add(parseVariableDeclaratorList());
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        return new ASTConstantDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTConstantDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>, and
     * <code>ASTDataType</code>.
     * @param loc The given <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param dt An already parsed <code>ASTAccessModifier</code>, present.
     * @return An <code>ASTConstantDeclaration</code>.
     */
    public ASTConstantDeclaration parseConstantDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTDataType dt)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTConstantModifier mms = gms.convertToSpecificList(
                    "Unexpected constant modifier.",
                    Arrays.asList(CONSTANT),
                    ASTConstantModifier::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        children.add(dt);
        children.add(parseVariableDeclaratorList());
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        return new ASTConstantDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTConstantModifier</code>.
     * @return An <code>ASTConstantModifier</code>.
     */
    public ASTConstantModifier parseConstantModifier()
    {
        return parseOneOf(
                Arrays.asList(CONSTANT),
                "Expected constant.",
                ASTConstantModifier::new
        );
    }

    /**
     * Parses an <code>ASTEnumDeclaration</code>.
     * @return An <code>ASTEnumDeclaration</code>.
     */
    public ASTEnumDeclaration parseEnumDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(5);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP)) != null)
        {
            children.add(parseClassModifierList());
        }
        if (accept(ENUM) == null)
        {
            throw new CompileException("Expected enum.");
        }
        children.add(parseIdentifier());
        if (isCurr(IMPLEMENTS))
        {
            children.add(parseSuperinterfaces());
        }
        children.add(parseEnumBody());
        ASTEnumDeclaration node = new ASTEnumDeclaration(loc, children);
        node.setOperation(ENUM);
        return node;
    }

    /**
     * Parses an <code>ASTEnumDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code> and <code>ASTGeneralModifierList</code>.
     * @param loc The <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @return An <code>ASTEnumDeclaration</code>.
     */
    public ASTEnumDeclaration parseEnumDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms)
    {
        List<ASTNode> children = new ArrayList<>(5);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            children.add(gms.convertToSpecificList(
                    "Unexpected enum modifier.",
                    Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP),
                    ASTClassModifierList::new
            ));
        }
        if (accept(ENUM) == null)
        {
            throw new CompileException("Expected enum.");
        }
        children.add(parseIdentifier());
        if (isCurr(IMPLEMENTS))
        {
            children.add(parseSuperinterfaces());
        }
        children.add(parseEnumBody());
        ASTEnumDeclaration node = new ASTEnumDeclaration(loc, children);
        node.setOperation(ENUM);
        return node;
    }

    /**
     * Parses an <code>ASTEnumConstantList</code>.
     * @return An <code>ASTEnumConstantList</code>.
     */
    public ASTEnumBody parseEnumBody()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        if (isAcceptedOperator(Arrays.asList(SEMICOLON, CLOSE_BRACE)) == null)
        {
            children.add(parseEnumConstantList());
        }
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseEnumBodyDeclarations());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        return new ASTEnumBody(loc, children);
    }

    /**
     * Parses an <code>ASTEnumBodyDeclarations</code>.
     * @return An <code>ASTEnumBodyDeclarations</code>.
     */
    public ASTEnumBodyDeclarations parseEnumBodyDeclarations()
    {
        Location loc = curr().getLocation();
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        ASTEnumBodyDeclarations node = new ASTEnumBodyDeclarations(loc, Arrays.asList(parseClassPartList()));
        node.setOperation(SEMICOLON);
        return node;
    }

    /**
     * Parses an <code>ASTEnumConstantList</code>.
     * @return An <code>ASTEnumConstantList</code>.
     */
    public ASTEnumConstantList parseEnumConstantList()
    {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected enum constant identifier.",
                COMMA,
                this::parseEnumConstant,
                ASTEnumConstantList::new
        );
    }

    /**
     * Parses an <code>ASTEnumConstant</code>.
     * @return An <code>ASTEnumConstant</code>.
     */
    public ASTEnumConstant parseEnumConstant()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(parseIdentifier());
        if (isCurr(OPEN_PARENTHESIS))
        {
            accept(OPEN_PARENTHESIS);
            if (!isCurr(CLOSE_PARENTHESIS))
            {
                children.add(parseArgumentList());
            }
            if (accept(CLOSE_PARENTHESIS) == null)
            {
                throw new CompileException("Expected ')'.");
            }
        }
        if (isCurr(OPEN_BRACE))
        {
            children.add(parseClassBody());
        }
        return new ASTEnumConstant(loc, children);
    }

    /**
     * Parses an <code>ASTClassDeclaration</code>.
     * @return An <code>ASTClassDeclaration</code>.
     */
    public ASTClassDeclaration parseClassDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(7);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP)) != null)
        {
            children.add(parseClassModifierList());
        }
        if (accept(CLASS) == null)
        {
            throw new CompileException("Expected class.");
        }
        children.add(parseIdentifier());
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        if (isCurr(EXTENDS))
        {
            children.add(parseSuperclass());
        }
        if (isCurr(IMPLEMENTS))
        {
            children.add(parseSuperinterfaces());
        }
        children.add(parseClassBody());
        ASTClassDeclaration node = new ASTClassDeclaration(loc, children);
        node.setOperation(CLASS);
        return node;
    }

    /**
     * Parses an <code>ASTClassDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code> and <code>ASTGeneralModifierList</code>.
     * @param loc The <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @return An <code>ASTClassDeclaration</code>.
     */
    public ASTClassDeclaration parseClassDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms)
    {
        List<ASTNode> children = new ArrayList<>(7);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            children.add(gms.convertToSpecificList(
                    "Unexpected class modifier.",
                    Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP),
                    ASTClassModifierList::new
            ));
        }
        if (accept(CLASS) == null)
        {
            throw new CompileException("Expected class.");
        }
        children.add(parseIdentifier());
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        if (isCurr(EXTENDS))
        {
            children.add(parseSuperclass());
        }
        if (isCurr(IMPLEMENTS))
        {
            children.add(parseSuperinterfaces());
        }
        children.add(parseClassBody());
        ASTClassDeclaration node = new ASTClassDeclaration(loc, children);
        node.setOperation(CLASS);
        return node;
    }

    /**
     * Parses an <code>ASTSuperinterfaces</code>.
     * @return An <code>ASTSuperinterfaces</code>.
     */
    public ASTSuperinterfaces parseSuperinterfaces()
    {
        Location loc = curr().getLocation();
        if (accept(IMPLEMENTS) == null)
        {
            throw new CompileException("Expected implements.");
        }
        ASTSuperinterfaces node = new ASTSuperinterfaces(loc, Arrays.asList(parseDataTypeNoArrayList()));
        node.setOperation(IMPLEMENTS);
        return node;
    }

    /**
     * Parses an <code>ASTDataTypeNoArrayList</code>.
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseDataTypeNoArrayList()
    {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected a data type (no array).",
                COMMA,
                this::parseDataTypeNoArray,
                ASTDataTypeNoArrayList::new
        );
    }

    /**
     * Parses an <code>ASTSuperclass</code>.
     * @return An <code>ASTSuperclass</code>.
     */
    public ASTSuperclass parseSuperclass()
    {
        Location loc = curr().getLocation();
        if (accept(EXTENDS) == null)
        {
            throw new CompileException("Expected extends.");
        }
        ASTSuperclass node = new ASTSuperclass(loc, Arrays.asList(parseDataTypeNoArray()));
        node.setOperation(EXTENDS);
        return node;
    }

    /**
     * Parses an <code>ASTClassModifierList</code>.
     * @return An <code>ASTClassModifierList</code>.
     */
    public ASTClassModifierList parseClassModifierList()
    {
        return parseGeneralModifierList()
                .convertToSpecificList("Expected abstract, final, shared, or strictfp.",
                        Arrays.asList(ABSTRACT, FINAL, SHARED, STRICTFP),
                        ASTClassModifierList::new);
    }

    /**
     * Parses an <code>ASTClassBody</code>.
     * @return An <code>ASTClassBody</code>.
     */
    public ASTClassBody parseClassBody()
    {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null)
        {
            throw new CompileException("Expected '{'.");
        }
        List<ASTNode> children = new ArrayList<>(1);
        if (!isCurr(CLOSE_BRACE))
        {
            children.add(parseClassPartList());
        }
        if (accept(CLOSE_BRACE) == null)
        {
            throw new CompileException("Expected '}'.");
        }
        ASTClassBody node = new ASTClassBody(loc, children);
        node.setOperation(OPEN_BRACE);
        return node;
    }

    /**
     * Parses an <code>ASTClassPartList</code>.
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList parseClassPartList()
    {
        return parseMultiple(
                t -> Arrays.asList(PUBLIC, PRIVATE, INTERNAL, PROTECTED, CLASS, INTERFACE,
                        ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP, TRANSIENT, VOLATILE,
                        CONSTRUCTOR, CONST, CONSTANT, VOID, IDENTIFIER, LESS_THAN)
                        .contains(t.getType()),
                "Expected constructor, field, or method declaration.",
                this::parseClassPart,
                ASTClassPartList::new
        );
    }

    /**
     * Parses an <code>ASTClassPart</code>.
     * @return An <code>ASTClassPart</code>.
     */
    public ASTClassPart parseClassPart()
    {
        Location loc = curr().getLocation();
        if (isCurr(SHARED) && isNext(CONSTRUCTOR))
        {
            return new ASTClassPart(loc, Arrays.asList(parseSharedConstructor()));
        }
        ASTAccessModifier accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null)
        {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = null;
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, CONSTANT, FINAL, OVERRIDE, SHARED, STRICTFP, TRANSIENT, VOLATILE)) != null)
        {
            genModList = parseGeneralModifierList();
        }

        // class/enum/interface/annotation
        switch(curr().getType())
        {
        case CLASS:
            return new ASTClassPart(loc, Arrays.asList(parseClassDeclaration(loc, accessMod, genModList)));
        case ENUM:
            return new ASTClassPart(loc, Arrays.asList(parseEnumDeclaration(loc, accessMod, genModList)));
        case INTERFACE:
            return new ASTClassPart(loc, Arrays.asList(parseInterfaceDeclaration(loc, accessMod, genModList)));
        case ANNOTATION:
            return new ASTClassPart(loc, Arrays.asList(parseAnnotationDeclaration(loc, accessMod, genModList)));
        }

        ASTTypeParameters typeParams = null;
        if (isCurr(LESS_THAN))
        {
            typeParams = parseTypeParameters();
        }

        if (isAcceptedOperator(Arrays.asList(CONST, VOID)) != null)
        {
            return new ASTClassPart(loc, Arrays.asList(parseMethodDeclaration(loc, accessMod, genModList, typeParams)));
        }
        else if (isAcceptedOperator(Arrays.asList(CONSTRUCTOR)) != null)
        {
            return new ASTClassPart(loc, Arrays.asList(parseConstructorDeclaration(loc, accessMod, genModList, typeParams)));
        }
        else
        {
            ASTDataType dt = parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS))
            {
                return new ASTClassPart(loc, Arrays.asList(parseMethodDeclaration(loc, accessMod, genModList, typeParams, dt)));
            }
            else
            {
                if (typeParams != null)
                {
                    throw new CompileException("Type parameters not allowed on field declaration.");
                }
                return new ASTClassPart(loc, Arrays.asList(parseFieldDeclaration(loc, accessMod, genModList, dt)));
            }
        }
    }

    /**
     * Parses an <code>ASTSharedConstructor</code>.
     *
     * @return An <code>ASTSharedConstructor</code>.
     */
    public ASTSharedConstructor parseSharedConstructor()
    {
        Location loc = curr().getLocation();
        if (accept(SHARED) == null)
        {
            throw new CompileException("Expected shared.");
        }
        if (accept(CONSTRUCTOR) == null)
        {
            throw new CompileException("Expected constructor.");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        return new ASTSharedConstructor(loc, Arrays.asList(parseBlock()));
    }

    /**
     * Parses an <code>ASTConstructorDeclaration</code>.
     *
     * @return An <code>ASTConstructorDeclaration</code>.
     */
    public ASTConstructorDeclaration parseConstructorDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(5);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isCurr(STRICTFP))
        {
            children.add(parseStrictfpModifier());
        }
        children.add(parseConstructorDeclarator());
        if (isCurr(COLON))
        {
            children.add(parseConstructorInvocation());
        }
        children.add(parseBlock());
        return new ASTConstructorDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTConstructorDeclaration</code>, given optionally already
     * parsed productions: <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>,
     * <code>ASTTypeParameters</code>.
     *
     * @param loc The starting <code>Location</code>.
     * @param am  An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTConstructorDeclaration parseConstructorDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTTypeParameters tps)
    {
        List<ASTNode> children = new ArrayList<>(5);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            children.add(gms.convertToSpecificList(
                    "Unexpected constructor modifier.",
                    Collections.singletonList(STRICTFP),
                    ASTStrictfpModifier::new
            ));
        }
        if (tps != null)
        {
            children.add(parseConstructorDeclarator(tps));
        }
        else
        {
            children.add(parseConstructorDeclarator());
        }
        if (isCurr(COLON))
        {
            children.add(parseConstructorInvocation());
        }
        children.add(parseBlock());
        return new ASTConstructorDeclaration(loc, children);
    }


    /**
     * Parses an <code>ASTConstructorInvocation</code>.
     * @return An <code>ASTConstructorInvocation</code>.
     */
    public ASTConstructorInvocation parseConstructorInvocation()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (accept(COLON) == null)
        {
            throw new CompileException("Expected ':' for explicit constructor invocation.");
        }
        ASTPrimary primary = null;
        if (isAcceptedOperator(Arrays.asList(CONSTRUCTOR, SUPER, LESS_THAN)) == null)
        {
            // Primary . [TypeArguments] super
            // ExpressionName . [TypeArguments] super
            try
            {
                primary = parsePrimary();

                // Must be an expression name or a primary.
                List<ASTNode> pChildren = primary.getChildren();
                if (pChildren.size() == 1 && pChildren.get(0) instanceof ASTExpressionName)
                {
                    children.add(pChildren.get(0));
                }
                else
                {
                    children.add(primary);
                }
                if (accept(DOT) == null)
                {
                    throw new CompileException("Expected '.' between expression and super.");
                }
                if (isCurr(LESS_THAN))
                {
                    children.add(parseTypeArguments());
                }
            }
            catch (CompileException containsAlreadyParsed)
            {
                // Occurs with ExpressionName . TypeArguments super
                // The parsePrimary method will attempt to produce a
                // MethodInvocation until it finds "super", when it throws this
                // Exception.  At that point the ExpressionName and
                // TypeArguments have already been parsed.  Capture them here.
                // See parseMethodInvocation(ASTExpressionName).
                children.addAll(containsAlreadyParsed.getAlreadyParsed());
            }
        }
        else if (isCurr(LESS_THAN))
        {
            children.add(parseTypeArguments());
        }

        ASTConstructorInvocation node = new ASTConstructorInvocation(loc, children);
        if (primary != null)
        {
            if (accept(SUPER) == null)
            {
                // ExpressionName and Primary can only have super.
                throw new CompileException("Expected super after expression dot for explicit superclass constructor invocation.");
            }
            node.setOperation(SUPER);
        }
        else
        {
            TokenType operation = isAcceptedOperator(Arrays.asList(SUPER, CONSTRUCTOR));
            if (operation == null)
            {
                throw new CompileException("Expected constructor or super for explicit constructor invocation.");
            }
            accept(operation);
            node.setOperation(operation);
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseArgumentList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        return node;
    }

    /**
     * Parses an <code>ASTStrictfpModifier</code>.
     * @return An <code>ASTStrictfpModifier</code>.
     */
    public ASTStrictfpModifier parseStrictfpModifier()
    {
        return parseOneOf(
                Arrays.asList(STRICTFP),
                "Expected strictfp.",
                ASTStrictfpModifier::new
        );
    }

    /**
     * Parses an <code>ASTConstructorDeclarator</code>.
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator parseConstructorDeclarator()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        if (accept(CONSTRUCTOR) == null)
        {
            throw new CompileException("Expected \"constructor\".");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseFormalParameterList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        ASTConstructorDeclarator node = new ASTConstructorDeclarator(loc, children);
        node.setOperation(CONSTRUCTOR);
        return node;
    }

    /**
     * Parses an <code>ASTConstructorDeclarator</code>, given an already parsed
     * <code>ASTTypeParameters</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator parseConstructorDeclarator(ASTTypeParameters tps)
    {
        Location loc = tps != null ? tps.getLocation() : null;
        List<ASTNode> children = new ArrayList<>(2);
        if (tps != null)
        {
            children.add(tps);
        }
        if (loc == null)
        {
            loc = curr().getLocation();
        }
        if (accept(CONSTRUCTOR) == null)
        {
            throw new CompileException("Expected \"constructor\".");
        }
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseFormalParameterList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        ASTConstructorDeclarator node = new ASTConstructorDeclarator(loc, children);
        node.setOperation(CONSTRUCTOR);
        return node;
    }

    /**
     * Parses an <code>ASTFieldDeclaration</code>.
     * @return An <code>ASTFieldDeclaration</code>.
     */
    public ASTFieldDeclaration parseFieldDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(CONST, CONSTANT, FINAL, SHARED, TRANSIENT, VOLATILE)) != null)
        {
            children.add(parseFieldModifierList());
        }
        children.add(parseDataType());
        children.add(parseVariableDeclaratorList());
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        return new ASTFieldDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTFieldDeclaration</code>, given an already parsed
     * <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>, and
     * <code>ASTDataType</code>.
     * @param loc The given <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param dt An already parsed <code>ASTAccessModifier</code>, present.
     * @return An <code>ASTFieldDeclaration</code>.
     */
    public ASTFieldDeclaration parseFieldDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTDataType dt)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTFieldModifierList mms = gms.convertToSpecificList(
                    "Unexpected field modifier.",
                    Arrays.asList(CONST, CONSTANT, FINAL, SHARED, TRANSIENT, VOLATILE),
                    ASTFieldModifierList::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        children.add(dt);
        children.add(parseVariableDeclaratorList());
        if (accept(SEMICOLON) == null)
        {
            throw new CompileException("Expected semicolon.");
        }
        return new ASTFieldDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTFieldModifierList</code>.
     * @return An <code>ASTFieldModifierList</code>.
     */
    public ASTFieldModifierList parseFieldModifierList()
    {
        return parseGeneralModifierList()
                .convertToSpecificList("Expected const, constant, final, shared, transient, or volatile.",
                        Arrays.asList(CONST, CONSTANT, FINAL, SHARED, TRANSIENT, VOLATILE),
                        ASTFieldModifierList::new);
    }

    /**
     * Parses an <code>ASTMethodDeclaration</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(4);
        if (isAcceptedOperator(Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE)) != null)
        {
            children.add(parseAccessModifier());
        }
        if (isAcceptedOperator(Arrays.asList(ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP)) != null)
        {
            children.add(parseMethodModifierList());
        }
        children.add(parseMethodHeader());
        children.add(parseMethodBody());
        return new ASTMethodDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTMethodDeclaration</code>, given optionally already
     * parsed productions: <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>,
     * <code>ASTTypeParameters</code>.
     * @param loc The starting <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTTypeParameters tps)
    {

        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTMethodModifierList mms = gms.convertToSpecificList(
                    "Unexpected method modifier.",
                    Arrays.asList(ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP),
                    ASTMethodModifierList::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        if (tps != null)
        {
            children.add(parseMethodHeader(tps));
        }
        else
        {
            children.add(parseMethodHeader());
        }
        children.add(parseMethodBody());
        return new ASTMethodDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTMethodDeclaration</code>, given optionally already
     * parsed productions: <code>ASTAccessModifier</code>, <code>ASTGeneralModifierList</code>,
     * <code>ASTTypeParameters</code>.
     * @param loc The starting <code>Location</code>.
     * @param am An already parsed <code>ASTAccessModifier</code>.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTAccessModifier am, ASTGeneralModifierList gms, ASTTypeParameters tps, ASTDataType dt)
    {
        List<ASTNode> children = new ArrayList<>(4);
        if (am != null)
        {
            children.add(am);
        }
        if (gms != null)
        {
            ASTMethodModifierList mms = gms.convertToSpecificList(
                    "Unexpected method modifier.",
                    Arrays.asList(ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP),
                    ASTMethodModifierList::new
            );
            if (mms != null)
            {
                children.add(mms);
            }
        }
        children.add(parseMethodHeader(tps, dt));
        children.add(parseMethodBody());
        return new ASTMethodDeclaration(loc, children);
    }

    /**
     * Parses an <code>ASTMethodBody</code>.
     * @return An <code>ASTMethodBody</code>.
     */
    public ASTMethodBody parseMethodBody()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(1);
        ASTMethodBody node = new ASTMethodBody(loc, children);
        if (isCurr(SEMICOLON))
        {
            accept(SEMICOLON);
            node.setOperation(SEMICOLON);
        }
        else if (isCurr(OPEN_BRACE))
        {
            children.add(parseBlock());
        }
        else
        {
            throw new CompileException("Expected block for method body.");
        }
        return node;
    }

    /**
     * Parses an <code>ASTAccessModifier</code>.
     * @return An <code>ASTAccessModifier</code>.
     */
    public ASTAccessModifier parseAccessModifier()
    {
        return parseOneOf(
                Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE),
                "Expected public, protected, internal, or private.",
                ASTAccessModifier::new
        );
    }

    /**
     * Parses an <code>ASTMethodModifierList</code>.
     * @return An <code>ASTMethodModifierList</code>.
     */
    public ASTMethodModifierList parseMethodModifierList()
    {
        return parseGeneralModifierList()
                .convertToSpecificList("Expected abstract, final, override, shared, or strictfp.",
                        Arrays.asList(ABSTRACT, FINAL, OVERRIDE, SHARED, STRICTFP),
                        ASTMethodModifierList::new);
    }

    /**
     * Parses an <code>ASTGeneralModifierList</code>.
     * @return An <code>ASTGeneralModifierList</code>.
     */
    public ASTGeneralModifierList parseGeneralModifierList()
    {
        return parseMultiple(
                t -> test(t, ABSTRACT, CONST, CONSTANT, DEFAULT, FINAL, OVERRIDE, SHARED, STRICTFP, TRANSIENT, VOLATILE),
                "Expected a general modifier.",
                this::parseGeneralModifier,
                ASTGeneralModifierList::new
        );
    }

    /**
     * Parses an <code>ASTGeneralModifier</code>.
     * @return An <code>ASTGeneralModifier</code>.
     */
    public ASTGeneralModifier parseGeneralModifier()
    {
        return parseOneOf(
                Arrays.asList(ABSTRACT, CONST, CONSTANT, DEFAULT, FINAL, OVERRIDE, SHARED, STRICTFP, TRANSIENT, VOLATILE),
                "Expected abstract, const, constant, default, final, override, shared, strictfp, transient, or volatile.",
                ASTGeneralModifier::new
        );
    }

    /**
     * Parses an <code>ASTMethodHeader</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (isCurr(LESS_THAN))
        {
            children.add(parseTypeParameters());
        }
        children.add(parseResult());
        children.add(parseMethodDeclarator());
        return new ASTMethodHeader(loc, children);
    }

    /**
     * Parses an <code>ASTMethodHeader</code>, given already parsed
     * <code>ASTTypeParameters</code>.
     * @param tps Already parsed <code>ASTTypeParameters/code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader(ASTTypeParameters tps)
    {
        List<ASTNode> children = new ArrayList<>(3);
        children.add(tps);
        children.add(parseResult());
        children.add(parseMethodDeclarator());
        return new ASTMethodHeader(tps.getLocation(), children);
    }

    /**
     * Parses an <code>ASTMethodHeader</code>, given already parsed
     * <code>ASTTypeParameters</code> (optional), and an already parsed
     * <code>ASTDataType</code>.
     * @param tps Already parsed <code>ASTTypeParameters</code>.  If not present, <code>null</code>.
     * @param dt Already parsed <code>ASTDataType</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader(ASTTypeParameters tps, ASTDataType dt)
    {
        Location loc = tps != null ? tps.getLocation() : dt.getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        if (tps != null)
        {
            children.add(tps);
        }
        children.add(parseResult(dt));
        children.add(parseMethodDeclarator());
        return new ASTMethodHeader(loc, children);
    }

    /**
     * Parses an <code>ASTResult</code>.
     * @return An <code>ASTResult</code>.
     */
    public ASTResult parseResult()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(2);
        if (isCurr(VOID))
        {
            accept(VOID);
            ASTResult node = new ASTResult(loc, children);
            node.setOperation(VOID);
            return node;
        }
        else if (isCurr(CONST))
        {
            children.add(parseConstModifier());
        }
        children.add(parseDataType());
        return new ASTResult(loc, children);
    }

    /**
     * Parses an <code>ASTResult</code>, given an already parsed <code>ASTDataType</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResult</code>.
     */
    public ASTResult parseResult(ASTDataType dt)
    {
        return new ASTResult(dt.getLocation(), Arrays.asList(dt));
    }

    /**
     * Parses an <code>ASTMethodDeclarator</code>.
     * @return An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodDeclarator parseMethodDeclarator()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        children.add(parseIdentifier());
        if (accept(OPEN_PARENTHESIS) == null)
        {
            throw new CompileException("Expected '('.");
        }
        if (!isCurr(CLOSE_PARENTHESIS))
        {
            children.add(parseFormalParameterList());
        }
        if (accept(CLOSE_PARENTHESIS) == null)
        {
            throw new CompileException("Expected ')'.");
        }
        if (isCurr(CONST))
        {
            children.add(parseConstModifier());
        }
        return new ASTMethodDeclarator(loc, children);
    }

    /**
     * Parses an <code>ASTConstModifier</code>.
     * @return An <code>ASTConstModifier</code>.
     */
    public ASTConstModifier parseConstModifier()
    {
        return parseOneOf(
                Arrays.asList(CONST),
                "Expected const.",
                ASTConstModifier::new
        );
    }

    /**
     * Parses an <code>ASTFormalParameterList</code>.
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList parseFormalParameterList()
    {
        ASTFormalParameterList node = parseList(
                t -> test(t, IDENTIFIER, CONST, CONSTANT, FINAL),
                "Expected data type",
                COMMA,
                this::parseFormalParameter,
                ASTFormalParameterList::new
        );

        // Enforce varargs parameter must be last.
        List<ASTNode> children = node.getChildren();
        boolean ellipsisSeen = false;
        for (ASTNode child : children)
        {
            if (ellipsisSeen)
            {
                throw new CompileException("Varargs parameter must be last in the list.");
            }
            ASTFormalParameter formalParam = (ASTFormalParameter) child;
            if (formalParam.getOperation() == ELLIPSIS)
            {
                ellipsisSeen = true;
            }
        }

        return node;
    }

    /**
     * Parses an <code>ASTFormalParameter</code>.
     * @return An <code>ASTFormalParameter</code>.
     */
    public ASTFormalParameter parseFormalParameter()
    {
        Location loc = curr().getLocation();
        List<ASTNode> children = new ArrayList<>(3);
        ASTFormalParameter node = new ASTFormalParameter(loc, children);
        if (isAcceptedOperator(Arrays.asList(CONST, FINAL, CONSTANT)) != null)
        {
            children.add(parseVariableModifierList());
        }
        children.add(parseDataType());
        if (isCurr(ELLIPSIS))
        {
            accept(ELLIPSIS);
            node.setOperation(ELLIPSIS);
        }
        children.add(parseIdentifier());
        return node;
    }

}
