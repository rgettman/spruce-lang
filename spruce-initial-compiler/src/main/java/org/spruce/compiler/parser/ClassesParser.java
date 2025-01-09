package org.spruce.compiler.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.spruce.compiler.ast.ASTKeywordNode;
import org.spruce.compiler.ast.ASTListNode;
import org.spruce.compiler.ast.classes.*;
import org.spruce.compiler.ast.expressions.ASTArgumentList;
import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTTypeName;
import org.spruce.compiler.ast.statements.ASTBlock;
import org.spruce.compiler.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.ast.statements.ASTVariableModifierList;
import org.spruce.compiler.ast.types.ASTDataType;
import org.spruce.compiler.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.ast.types.ASTTypeParameterList;
import org.spruce.compiler.scanner.Location;
import org.spruce.compiler.scanner.Scanner;
import org.spruce.compiler.scanner.Token;
import org.spruce.compiler.scanner.TokenType;

import static org.spruce.compiler.scanner.TokenType.*;

/**
 * A <code>ClassesParser</code> is a <code>BasicParser</code> that parses
 * classes.
 */
public class ClassesParser extends BasicParser {
    /**
     * Constructs a <code>ClassesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser The <code>Parser</code> that is creating this object.
     */
    public ClassesParser(Scanner scanner, Parser parser) {
        super(scanner, parser);
    }

    /**
     * General method to parse a nested type and produce a "part" type.  Reduces
     * code repetition because many different "part" nodes can contain any of
     * the same list of nested types.
     * <em>
     * TypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;EnumDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;RecordDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AdtDeclaration
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an Access Modifier, if it was found.
     * @param genModList An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTTypeDeclaration</code> of the appropriate type, e.g. <code>ASTClassDeclaration</code>.
     */
    private ASTTypeDeclaration parseNestedType(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                               ASTGeneralModifierList genModList) {
        return switch (curr().getType()) {
            case CLASS -> parseClassDeclaration(loc, annList, accessMod, genModList);
            case ENUM -> parseEnumDeclaration(loc, annList, accessMod, genModList);
            case INTERFACE -> parseInterfaceDeclaration(loc, annList, accessMod, genModList);
            case ANNOTATION -> parseAnnotationDeclaration(loc, annList, accessMod, genModList);
            case RECORD -> {
                if (!genModList.getChildren().isEmpty()) {
                    error(curr().getLocation(), "General modifier not allowed here.");
                }
                yield parseRecordDeclaration(loc, annList, accessMod);
            }
            case ADT -> {
                if (!genModList.getChildren().isEmpty()) {
                    error(curr().getLocation(), "General modifier not allowed here.");
                }
                yield parseAdtDeclaration(loc, annList, accessMod);
            }
            default -> throw internalError("type declaration");
        };
    }

    /**
     * Parses an <code>AnnotationDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, and GeneralModifierList.
     * <em>
     * AnnotationDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceModifierList] annotation Identifier AnnotationBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTAnnotationDeclaration</code>.
     */
    public ASTAnnotationDeclaration parseAnnotationDeclaration(Location loc, ASTAnnotationList annList,
                                                               ASTKeywordNode accessMod, ASTGeneralModifierList gms) {
        ASTInterfaceModifierList interfaceModList = convertToSpecificList(gms, 
                    "Unexpected annotation modifier.",
                    Arrays.asList(ABSTRACT, SHARED),
                    ASTInterfaceModifierList::new
        );
        if (accept(ANNOTATION) == null) {
            throw internalError(ANNOTATION);
        }
        ASTIdentifier name = getNamesParser().parseIdentifier();
        ASTAnnotationPartList body = parseAnnotationBody();
        if (accessMod != null) {
            return new ASTAnnotationDeclaration(loc, annList, accessMod, interfaceModList, name, body);
        }
        return new ASTAnnotationDeclaration(loc, annList, interfaceModList, name, body);
    }

    /**
     * Parses an <code>AnnotationBody</code>.
     * <em>
     * AnnotationBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ AnnotationPartList }
     * </em>
     * @return An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationPartList parseAnnotationBody() {
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTAnnotationPartList annotationPartList = parseAnnotationPartList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return annotationPartList;
    }

    /**
     * Parses an <code>AnnotationPartList</code>.
     * <em>
     * AnnotationPartList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationPart {AnnotationPart}
     * </em>
     * @return An <code>ASTAnnotationPartList</code>.
     */
    public ASTAnnotationPartList parseAnnotationPartList() {
        return parseMultiple(
                t -> Arrays.asList(AT_SIGN, PUBLIC, PRIVATE, INTERNAL, PROTECTED,
                        ABSTRACT, SHARED, CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,
                        CONSTANT, IDENTIFIER)
                        .contains(t.getType()),
                "Expected constant or element declaration.",
                this::parseAnnotationPart,
                Arrays.asList(CLOSE_BRACE, EOF),
                ASTAnnotationPartList::new,
                false
        );
    }

    /**
     * Parses an <code>AnnotationPart</code>.
     * <em>
     * AnnotationPart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;AnnotationTypeElementDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
     * </em>
     * @return An <code>ASTAnnotationPart</code> representing one of the above productions.
     */
    public ASTAnnotationPart parseAnnotationPart() {
        skipUnrecognizedTokens();
        Location loc = curr().getLocation();
        ASTAnnotationList annList = parseAnnotationList();
        ASTKeywordNode accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null) {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = parseGeneralModifierList();

        switch(curr().getType()) {
        case CLASS, ENUM, INTERFACE, ANNOTATION, RECORD, ADT:
            return parseNestedType(loc, annList, accessMod, genModList);
        }

        ASTTypeParameterList typeParams = null;
        if (isCurr(LESS_THAN)) {
            typeParams = getTypesParser().parseTypeParameters();

            if (isCurr(CONSTANT)) {
                ASTKeywordNode constantKeyword = parseModifier(Arrays.asList(CONSTANT),
                        "Internal error (constant).");
                error(typeParams.getLocation(), "Type parameters not allowed on constant declaration.");
                return parseConstantDeclaration(loc, annList, accessMod, constantKeyword);
            }
        }

        ASTDataType dt = getTypesParser().parseDataType();
        if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
            if (typeParams != null) {
                error(typeParams.getLocation(), "Type parameters not allowed on annotation element declaration.");
            }
            if (!genModList.getChildren().isEmpty()) {
                error(genModList.getLocation(), "Method modifiers not allowed on annotation element declaration.");
            }
            if (accessMod != null) {
                error(accessMod.getLocation(), "Access modifiers not allowed on annotation element declaration.");
            }
            return parseAnnotationTypeElementDeclaration(loc, annList, dt);
        }
        else {
            if (typeParams != null) {
                error(typeParams.getLocation(), "Type parameters not allowed on constant declaration.");
            }
            return parseConstantDeclaration(loc, annList, accessMod, genModList, dt);
        }
    }

    /**
     * Parses an <code>AnnotationTypeElementDeclaration</code>, given an
     * already parsed AnnotationList and DataType.
     * <em>
     * AnnotationTypeElementDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataType Identifier ( ) ;<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataType Identifier ( ) DefaultValue ;
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An <code>ASTAnnotationList</code>, possibly empty.
     * @param dt The <code>ASTDataType</code>.
     * @return An <code>ASTAnnotationTypeElementDeclaration</code>.
     */
    public ASTAnnotationTypeElementDeclaration parseAnnotationTypeElementDeclaration(Location loc,
                   ASTAnnotationList annList, ASTDataType dt) {
        ASTIdentifier name = getNamesParser().parseIdentifier();
        ASTElementValue defaultValue = null;
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        if (isCurr(DEFAULT)) {
            defaultValue = parseDefaultValue();
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (defaultValue == null) {
            return new ASTAnnotationTypeElementDeclaration(loc, annList, dt, name);
        }
        else {
            return new ASTAnnotationTypeElementDeclaration(loc, annList, dt, name, defaultValue);
        }
    }

    /**
     * Parses a <code>DefaultValue</code>.
     * <em>
     * DefaultValue:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;default ElementValue
     * </em>
     * @return An <code>ASTElementValue</code> representing the element value that is
     *     the default value.
     */
    public ASTElementValue parseDefaultValue() {
        if (accept(DEFAULT) == null) {
            throw internalError(DEFAULT);
        }
        return parseElementValue();
    }

    /**
     * Parses an <code>AnnotationList</code>.
     * <em>
     * AnnotationList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Annotation {, Annotation}
     * </em>
     * @return An <code>ASTAnnotationList</code>.
     */
    public ASTAnnotationList parseAnnotationList() {
        return parseMultiple(
                t -> test(t, AT_SIGN),
                this::parseAnnotation,
                ASTAnnotationList::new
        );
    }

    /**
     * Parses an <code>Annotation</code>.
     * <em>
     * Annotation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MarkerAnnotation<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SingleElementAnnotation<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;NormalAnnotation
     * </em>
     * @return An <code>ASTAnnotation</code> that could be an
     *     <code>ASTMarkerAnnotation</code>, an <code>ASTNormalAnnotation</code>,
     *     or an <code>ASTSingleElementAnnotation</code>.
     */
    public ASTAnnotation parseAnnotation() {
        Location loc = curr().getLocation();
        if (accept(AT_SIGN) == null) {
            throw internalError(AT_SIGN);
        }
        ASTTypeName typeName = getNamesParser().parseTypeName();
        if (isCurr(OPEN_PARENTHESIS)) {
            accept(OPEN_PARENTHESIS);
            if (isCurr(CLOSE_PARENTHESIS)) {
               return parseNormalAnnotation(loc, typeName);
            }
            else if (isCurr(OPEN_BRACE)) {
                return parseSingleElementAnnotation(loc, typeName);
            }
            else if (isCurr(IDENTIFIER) && isNext(EQUAL)) {
                return parseNormalAnnotation(loc, typeName);
            }
            else {
                // Value Expression.
                return parseSingleElementAnnotation(loc, typeName);
            }
        }
        else {
            return parseMarkerAnnotation(loc, typeName);
        }
    }

    /**
     * Parses a <code>MarkerAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.
     * <em>
     * MarkerAnnotation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName
     * </em>
     * @param loc The <code>Location</code>.
     * @param typeName An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTMarkerAnnotation</code>.
     */
    public ASTMarkerAnnotation parseMarkerAnnotation(Location loc, ASTTypeName typeName) {
        return new ASTMarkerAnnotation(loc, typeName);
    }

    /**
     * Parses a <code>SingleElementAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.  An open parenthesis ('(') has already been consumed.
     * <em>
     * SingleElementAnnotation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( ElementValue )
     * </em>
     * @param loc The <code>Location</code>.
     * @param typeName An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTSingleElementAnnotation</code>.
     */
    public ASTSingleElementAnnotation parseSingleElementAnnotation(Location loc, ASTTypeName typeName) {
        ASTElementValue elementValue = parseElementValue();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTSingleElementAnnotation(loc, typeName, elementValue);
    }

    /**
     * Parses a <code>NormalAnnotation</code>, given an already parsed
     * <code>ASTTypeName</code>.  An open parenthesis ('(') has already been consumed.
     * <em>
     * NormalAnnotation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;@ TypeName ( [ElementValuePairList] )
     * </em>
     * @param loc The <code>Location</code>.
     * @param typeName An already parsed <code>ASTTypeName</code>.
     * @return An <code>ASTNormalAnnotation</code>.
     */
    public ASTNormalAnnotation parseNormalAnnotation(Location loc, ASTTypeName typeName) {
        ASTElementValuePairList elementValuePairList = parseElementValuePairList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTNormalAnnotation(loc, typeName, elementValuePairList);
    }

    /**
     * Parses an <code>ElementValuePairList</code>.
     * <em>
     * ElementValuePairList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementValuePair {, ElementValuePair}
     * </em>
     * @return An <code>ASTElementValuePairList</code>.
     */
    public ASTElementValuePairList parseElementValuePairList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected identifier.",
                COMMA,
                this::parseElementValuePair,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, SEMICOLON,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTElementValuePairList::new,
                false
        );
    }

    /**
     * Parses an <code>ElementValuePair</code>.
     * <em>
     * ElementValuePair:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier = ElementValue
     * </em>
     * @return An <code>ASTElementValuePair</code>.
     */
    public ASTElementValuePair parseElementValuePair() {
        Location loc = curr().getLocation();
        ASTIdentifier elementName = getNamesParser().parseIdentifier();
        if (accept(EQUAL) == null) {
            error(curr().getLocation(), "Expected assignment operator '='.");
            if (!isExpression(curr())) {
                accept(curr().getType());
            }
        }
        return new ASTElementValuePair(loc, elementName, parseElementValue());
    }

    /**
     * Parses an <code>ElementValueArrayInitializer</code>.
     * <em>
     * ElementValueArrayInitializer:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{}<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ ElementValueList }
     * </em>
     * @return An <code>ASTElementValueList</code>.
     */
    public ASTElementValueList parseElementValueArrayInitializer() {
        if (accept(OPEN_BRACE) == null) {
            throw internalError(OPEN_BRACE);
        }
        ASTElementValueList elementValueArrayInit = parseElementValueList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return elementValueArrayInit;
    }

    /**
     * Parses an <code>ElementValueList</code>.
     * <em>
     * ElementValueList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementValue {, ElementValue}
     * </em>
     * @return An <code>ASTElementValueList</code>.
     */
    public ASTElementValueList parseElementValueList() {
        return parseList(
                t -> isValueExpression(t) || test(t, Arrays.asList(OPEN_BRACE, AT_SIGN)),
                "Expected value.",
                COMMA,
                this::parseElementValue,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, SEMICOLON,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTElementValueList::new,
                false
        );
    }

    /**
     * Parses an <code>ElementValue</code>.
     * <em>
     * ElementValue:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ValueExpression<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ElementValueArrayInitializer<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Annotation
     * </em>
     * @return An <code>ASTElementValue</code>, which could be an <code>ASTAnnotation</code>,
     *     a <code>ASTValueExpression</code>, or an <code>ASTElementValueArrayInitializer</code>.
     */
    public ASTElementValue parseElementValue() {
        if (isCurr(OPEN_BRACE)) {
            return parseElementValueArrayInitializer();
        }
        else if (isCurr(AT_SIGN)) {
            return parseAnnotation();
        }
        else {
            return getExpressionsParser().parseValueExpression();
        }
    }

    /**
     * Parses an <code>AdtDeclaration</code>, given an already parsed
     * Annotation List and Access Modifier.
     * <em>
     * AdtDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] adt Identifier [TypeParameters] [ExtendsInterfaces] AdtBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  Access Modifier.  If not present, <code>null</code>.
     * @return An <code>ASTAdtDeclaration</code>.
     */
    public ASTAdtDeclaration parseAdtDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod) {
        ASTAdtDeclaration.Builder builder = new ASTAdtDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessModifier(accessMod);
        }
        if (accept(ADT) == null) {
            throw internalError(ADT);
        }
        builder.setName(getNamesParser().parseIdentifier());
        if (isCurr(LESS_THAN)) {
            builder.setTypeParams(getTypesParser().parseTypeParameters());
        }
        if (isCurr(EXTENDS)) {
            builder.setExtendsInterfaces(parseExtendsInterfaces());
        }
        builder.setAdtBody(parseAdtBody());
        return builder.build();
    }

    /**
     * Parses an <code>AdtBody</code>.
     * <em>
     * AdtBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ VariantList [AdtBodyDeclarations] }
     * </em>
     * @return An <code>ASTAdtBody</code>.
     */
    public ASTAdtBody parseAdtBody() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            error(loc, "Expected '{'.");
        }
        ASTVariantList variantList = parseVariantList();
        ASTInterfacePartList bodyDecls = parseAdtBodyDeclarations();
        if (accept(CLOSE_BRACE) == null) {
            error(loc, "Expected '}'.");
        }
        return new ASTAdtBody(loc, variantList, bodyDecls);
    }

    /**
     * Parses a <code>VariantList</code>.
     * <em>
     * VariantList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Variant {, Variant}
     * </em>
     * @return An <code>ASTVariantList</code>.
     */
    public ASTVariantList parseVariantList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected a data type or a compact record declaration.",
                COMMA,
                this::parseVariant,
                Arrays.asList(CLOSE_BRACE, SEMICOLON,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTVariantList::new,
                false
        );
    }

    /**
     * Parses a <code>Variant</code>.
     * <em>
     * Variant:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariantType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;CompactRecordDeclaration<br>
     * </em>
     * @return An <code>ASTVariant</code>.
     */
    public ASTVariant parseVariant() {
        ASTAnnotationList annList = parseAnnotationList();
        if (!isCurr(IDENTIFIER)) {
            throw internalError("'identifier'");
        }
        switch(next().getType()) {
            case DOT, COMMA, SEMICOLON, CLOSE_BRACE -> {
                return parseVariantType(annList);
            }
            default -> {
                return parseCompactRecordDeclaration(annList);
            }
        }
    }

    /**
     * Parses a <code>VariantType</code>, given an already parsed
     * AnnotationList.
     * <em>
     * VariantType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] DataTypeNoArray
     * </em>
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @return An <code>ASTVariantType</code>.
     */
    public ASTVariantType parseVariantType(ASTAnnotationList annList) {
        Location loc = curr().getLocation();
        return new ASTVariantType(loc, annList, getTypesParser().parseDataTypeNoArray());
    }

    /**
     * Parses a <code>CompactRecordDeclaration</code>, given an already parsed
     * AnnotationList.
     * <em>
     * CompactRecordDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [TypeArguments] RecordHeader [Superinterfaces] ClassBody
     * </em>
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @return An <code>ASTCompactRecordDeclaration</code>.
     */
    public ASTCompactRecordDeclaration parseCompactRecordDeclaration(ASTAnnotationList annList) {
        Location loc = curr().getLocation();
        if (isCurr(RECORD)) {
            error(loc, "Keyword 'record' cannot be present for a compact record declaration.  Remove.");
            accept(RECORD);
        }
        ASTCompactRecordDeclaration.Builder builder = new ASTCompactRecordDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList)
                .setName(getNamesParser().parseIdentifier());
        if (isCurr(LESS_THAN)) {
            builder.setTypeParams(getTypesParser().parseTypeParameters());
        }
        builder.setFormalParamList(parseRecordHeader());
        if (isCurr(IMPLEMENTS)) {
            builder.setSuperinterfaces(parseSuperinterfaces());
        }
        return builder.setClassParts(parseClassBody()).build();
    }

    /**
     * Parses an <code>AdtBodyDeclarations</code>.
     * <em>
     * AdtBodyDeclarations:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[; [InterfacePartList]]
     * </em>
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList parseAdtBodyDeclarations() {
        Location loc = curr().getLocation();
        if (isCurr(SEMICOLON)) {
            accept(SEMICOLON);
            return parseInterfacePartList();
        }
        else if (!isCurr(CLOSE_BRACE)) {
            error(loc, "Expected ';'.");
        }
        return new ASTInterfacePartList(loc, Collections.emptyList());
    }

    /**
     * Parses an <code>InterfaceDeclaration</code>, given an already parsed
     * AnnotationList, Access Modifier, and general modifier list.
     * <em>
     * InterfaceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceModifierList] interface Identifier [TypeParameters] [ExtendsInterfaces] [Permits] InterfaceBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTInterfaceDeclaration</code>.
     */
    public ASTInterfaceDeclaration parseInterfaceDeclaration(Location loc, ASTAnnotationList annList,
                                                             ASTKeywordNode accessMod, ASTGeneralModifierList gms) {
        ASTInterfaceDeclaration.Builder builder = new ASTInterfaceDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        builder.setInterfaceModifierList(convertToSpecificList(gms, 
                    "Unexpected interface modifier.",
                    Arrays.asList(ABSTRACT, SHARED),
                    ASTInterfaceModifierList::new
            ));
        if (accept(INTERFACE) == null) {
            throw internalError(INTERFACE);
        }
        builder.setName(getNamesParser().parseIdentifier());
        if (isCurr(LESS_THAN)) {
            builder.setTypeParams(getTypesParser().parseTypeParameters());
        }
        if (isCurr(EXTENDS)) {
            builder.setExtendsInterfaces(parseExtendsInterfaces());
        }
        if (isCurr(PERMITS)) {
            builder.setPermits(parsePermits());
        }
        return builder.setInterfaceParts(parseInterfaceBody()).build();
    }

    /**
     * Parses an <code>ExtendsInterfaces</code>.
     * <em>
     * ExtendsInterfaces:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;extends DataTypeNoArrayList
     * </em>
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseExtendsInterfaces() {
        if (accept(EXTENDS) == null) {
            throw internalError(EXTENDS);
        }
        return getTypesParser().parseDataTypeNoArrayList();
    }

    /**
     * Parses an <code>InterfaceBody</code>.
     * <em>
     * InterfaceBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ InterfacePartList }
     * </em>
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList parseInterfaceBody() {
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTInterfacePartList node = parseInterfacePartList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return node;
    }

    /**
     * Parses an <code>InterfacePartList</code>.
     * <em>
     * InterfacePartList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfacePart {InterfacePart}
     * </em>
     * @return An <code>ASTInterfacePartList</code>.
     */
    public ASTInterfacePartList parseInterfacePartList() {
        return parseMultiple(
                t -> Arrays.asList(AT_SIGN, PUBLIC, PRIVATE, INTERNAL, PROTECTED,
                        ABSTRACT, OVERRIDE, SHARED, CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,
                        DEFAULT, MUT, CONSTANT, VOID, IDENTIFIER, LESS_THAN)
                        .contains(t.getType()),
                "Expected constant or method declaration.",
                this::parseInterfacePart,
                Arrays.asList(CLOSE_BRACE, EOF),
                ASTInterfacePartList::new,
                false
        );
    }

    /**
     * Parses an <code>InterfacePart</code>.
     * <em>
     * InterfacePart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstantDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceMethodDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
     * </em>
     * @return An <code>ASTInterfacePart</code> representing one of the above productions.
     */
    public ASTInterfacePart parseInterfacePart() {
        skipUnrecognizedTokens();
        Location loc = curr().getLocation();
        ASTAnnotationList annList = parseAnnotationList();
        ASTKeywordNode accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null) {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = parseGeneralModifierList();

        switch(curr().getType()) {
        case CLASS, ENUM, INTERFACE, ANNOTATION, RECORD, ADT:
            return parseNestedType(loc, annList, accessMod, genModList);
        }

        if (isCurr(LESS_THAN)) {
            // TypeParameters mut|void|identifier
            ASTTypeParameterList typeParams = getTypesParser().parseTypeParameters();

            if (isCurr(CONSTANT)) {
                ASTKeywordNode constantKeyword = parseModifier(Arrays.asList(CONSTANT),
                        "Internal error (constant).");
                error(typeParams.getLocation(), "Type parameters not allowed on constant declaration.");
                return parseConstantDeclaration(loc, annList, accessMod, constantKeyword);
            }
            return parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList, typeParams);
        }
        // No type parameters:
        if (isCurr(VOID)) {
            // Result(void) ...
            return parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList);
        }
        else {
            ASTVariableModifierList varModList = getStatementsParser().parseVariableModifierList();
            ASTDataType dt = getTypesParser().parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                // [mut] DataType identifier (
                return parseInterfaceMethodDeclaration(loc, annList, accessMod, genModList, varModList, dt);
            }
            else {
                if (!varModList.getChildren().isEmpty()) {
                    ASTKeywordNode bad = varModList.getTypedChildren().get(0);
                    error(bad.getLocation(), "Unexpected variable modifier.");
                }
                // [VariableModifierList] DataType ...
                return parseConstantDeclaration(loc, annList, accessMod, genModList, dt);
            }
        }
    }

    /**
     * Parses an <code>InterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, a GeneralModifierList,
     * and a TypeParameterList.
     * <em>
     * InterfaceMethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param tps An already parsed <code>ASTTypeParameterList</code>.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTAnnotationList annList,
                   ASTKeywordNode accessMod, ASTGeneralModifierList gms, ASTTypeParameterList tps) {
        ASTInterfaceMethodModifierList interfaceMethodModifiers = convertToSpecificList(gms, 
                    "Unexpected interface method modifier.",
                    Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED),
                    ASTInterfaceMethodModifierList::new
            );
        ASTMethodHeader header = parseMethodHeader(tps);
        ASTMethodBody body = parseMethodBody();
        if (accessMod != null) {
            return new ASTInterfaceMethodDeclaration(loc, annList, accessMod, interfaceMethodModifiers, header, body);
        }
        return new ASTInterfaceMethodDeclaration(loc, annList, interfaceMethodModifiers, header, body);
    }

    /**
     * Parses an <code>InterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, and a GeneralModifierList.
     * <em>
     * InterfaceMethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTAnnotationList annList,
                      ASTKeywordNode accessMod, ASTGeneralModifierList gms) {
        ASTInterfaceMethodModifierList interfaceMethodModifiers = convertToSpecificList(gms, 
                "Unexpected interface method modifier.",
                Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED),
                ASTInterfaceMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader();
        ASTMethodBody body = parseMethodBody();
        if (accessMod != null) {
            return new ASTInterfaceMethodDeclaration(loc, annList, accessMod, interfaceMethodModifiers, header, body);
        }
        return new ASTInterfaceMethodDeclaration(loc, annList, interfaceMethodModifiers, header, body);
    }

    /**
     * Parses an <code>InterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, and a GeneralModifierList.
     * <em>
     * InterfaceMethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param varModList An already parsed <code>ASTVariableModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTAnnotationList annList,
                  ASTKeywordNode accessMod, ASTGeneralModifierList gms, ASTVariableModifierList varModList, ASTDataType dt) {
        ASTInterfaceMethodModifierList interfaceMethodModifiers = convertToSpecificList(gms, 
                "Unexpected interface method modifier.",
                Arrays.asList(ABSTRACT, DEFAULT, OVERRIDE, SHARED),
                ASTInterfaceMethodModifierList::new
        );
        Optional<ASTKeywordNode> mutModifier = parseMutModifier(varModList);
        ASTMethodHeader header;
        if (mutModifier.isPresent()) {
            header = parseMethodHeader(mutModifier.get(), dt);
        }
        else {
            header = parseMethodHeader(dt);
        }
        ASTMethodBody body = parseMethodBody();
        if (accessMod != null) {
            return new ASTInterfaceMethodDeclaration(loc, annList, accessMod, interfaceMethodModifiers, header, body);
        }
        return new ASTInterfaceMethodDeclaration(loc, annList, interfaceMethodModifiers, header, body);
    }

    /**
     * Parses a <code>ConstantDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, GeneralModifierList, and DataType.
     * <em>
     * ConstantDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] AccessModifier] ConstantModifier DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     *            It should contain only <code>constant</code>.
     * @param dt An already parsed <code>ASTKeywordNode</code>, present.
     * @return An <code>ASTConstantDeclaration</code>.
     */
    public ASTConstantDeclaration parseConstantDeclaration(Location loc, ASTAnnotationList annList,
                ASTKeywordNode accessMod, ASTGeneralModifierList gms, ASTDataType dt) {
        ASTConstantModifierList constantModifiers = convertToSpecificList(gms, 
                    "Unexpected modifier for a constant.",
                    Collections.singletonList(CONSTANT),
                    ASTConstantModifierList::new
            );
        ASTKeywordNode constantMod;
        if (constantModifiers.getChildren().isEmpty()) {
            error(dt.getLocation(), "Expected 'constant'.");
            constantMod = new ASTKeywordNode(curr().getLocation(), UNKNOWN);
        }
        else {
            constantMod = constantModifiers.get(0);
        }
        ASTVariableDeclaratorList varDeclList = getStatementsParser().parseVariableDeclaratorList();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (accessMod != null) {
            return new ASTConstantDeclaration(loc, annList, accessMod, constantMod, dt, varDeclList);
        }
        return new ASTConstantDeclaration(loc, annList, constantMod, dt, varDeclList);
    }

    /**
     * Parses a <code>ConstantDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, GeneralModifierList, and DataType.
     * <em>
     * ConstantDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] AccessModifier] ConstantModifier DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @param constantMod An already parsed <code>ASTKeywordNode</code> representing
     *                    the keyword <code>constant</code>.
     * @return An <code>ASTConstantDeclaration</code>.
     */
    public ASTConstantDeclaration parseConstantDeclaration(Location loc, ASTAnnotationList annList,
                ASTKeywordNode accessMod, ASTKeywordNode constantMod) {
        ASTDataType dt = getTypesParser().parseDataType();
        ASTVariableDeclaratorList varDeclList = getStatementsParser().parseVariableDeclaratorList();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (accessMod != null) {
            return new ASTConstantDeclaration(loc, annList, accessMod, constantMod, dt, varDeclList);
        }
        return new ASTConstantDeclaration(loc, annList, constantMod, dt, varDeclList);
    }

    /**
     * Parses a <code>RecordDeclaration</code>, given an already parsed
     * AnnotationList and AccessModifier.
     * <em>
     * RecordDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [ClassModifierList] record Identifier [TypeParameters] RecordHeader [Superinterfaces] ClassBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @return An <code>ASTRecordDeclaration</code>.
     */
    public ASTRecordDeclaration parseRecordDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod) {
        ASTRecordDeclaration.Builder builder = new ASTRecordDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        if (accept(RECORD) == null) {
            throw internalError(RECORD);
        }
        builder.setName(getNamesParser().parseIdentifier());
        if (isCurr(LESS_THAN)) {
            builder.setTypeParams(getTypesParser().parseTypeParameters());
        }
        builder.setFormalParamList(parseRecordHeader());
        if (isCurr(IMPLEMENTS)) {
            builder.setSuperinterfaces(parseSuperinterfaces());
        }
        return builder.setClassParts(parseClassBody()).build();
    }

    /**
     * Parses a <code>RecordHeader</code>.
     * <em>
     * RecordHeader:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;( [FormalParameterList] )
     * </em>
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList parseRecordHeader() {
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTFormalParameterList formalParamList = parseFormalParameterList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return formalParamList;
    }

    /**
     * Parses a <code>CompactConstructorDeclaration</code> given a <code>Location</code>,
     * an already parsed AnnotationList, and possibly an already parsed AccessModifier.
     * <em>
     * CompactConstructorDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] constructor Block
     * </em>
     * @param loc A <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @return An <code>ASTCompactConstructorDeclaration</code>.
     */
    public ASTCompactConstructorDeclaration parseCompactConstructorDeclaration(Location loc, ASTAnnotationList annList,
                                                                               ASTKeywordNode accessMod) {
        if (accept(CONSTRUCTOR) == null) {
            throw internalError(CONSTRUCTOR);
        }
        ASTBlock block = getStatementsParser().parseBlock();
        if (accessMod != null) {
            return new ASTCompactConstructorDeclaration(loc, annList, accessMod, block);
        }
        else {
            return new ASTCompactConstructorDeclaration(loc, annList, block);
        }
    }

    /**
     * Parses an <code>EnumDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, and GeneralModifierList.
     * <em>
     * EnumDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [ClassModifierList] enum Identifier [Superinterfaces] EnumBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTEnumDeclaration</code>.
     */
    public ASTEnumDeclaration parseEnumDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                                   ASTGeneralModifierList gms) {
        ASTEnumDeclaration.Builder builder = new ASTEnumDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        builder.setClassModifierList(convertToSpecificList(gms, 
                    "Unexpected enum modifier.",
                    Arrays.asList(ABSTRACT, SHARED),
                    ASTClassModifierList::new
            ));
        if (accept(ENUM) == null) {
            throw internalError(ENUM);
        }
        builder.setName(getNamesParser().parseIdentifier());
        if (isCurr(IMPLEMENTS)) {
            builder.setSuperinterfaces(parseSuperinterfaces());
        }
        return builder.setEnumBody(parseEnumBody()).build();
    }

    /**
     * Parses an <code>EnumBody</code>.
     * <em>
     * EnumBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ [EnumConstantList] [EnumBodyDeclarations] }
     * </em>
     * @return An <code>ASTEnumBody</code>.
     */
    public ASTEnumBody parseEnumBody() {
        Location loc = curr().getLocation();
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTEnumBody node = new ASTEnumBody(loc, parseEnumConstantList(), parseEnumBodyDeclarations());
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return node;
    }

    /**
     * Parses an <code>EnumBodyDeclarations</code>.
     * <em>
     * EnumBodyDeclarations:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;; [ClassPartList]
     * </em>
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList parseEnumBodyDeclarations() {
        if (isAcceptedOperator(Arrays.asList(CLOSE_BRACE, EOF)) != null) {
            return new ASTClassPartList(curr().getLocation(), Collections.emptyList());
        }
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return parseClassPartList();
    }

    /**
     * Parses an <code>EnumConstantList</code>.
     * <em>
     * EnumConstantList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;EnumConstant {, EnumConstant}
     * </em>
     * @return An <code>ASTEnumConstantList</code>.
     */
    public ASTEnumConstantList parseEnumConstantList() {
        return parseList(
                t -> test(t, Arrays.asList(IDENTIFIER, AT_SIGN)),
                "Expected enum constant identifier.",
                COMMA,
                this::parseEnumConstant,
                Arrays.asList(CLOSE_BRACE, SEMICOLON,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTEnumConstantList::new,
                false
        );
    }

    /**
     * Parses an <code>EnumConstant</code>.
     * <em>
     * EnumConstant:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] Identifier [( ArgumentList )] [ClassBody]
     * </em>
     * @return An <code>ASTEnumConstant</code>.
     */
    public ASTEnumConstant parseEnumConstant() {
        ASTAnnotationList annList = parseAnnotationList();
        Location loc = curr().getLocation();
        ASTIdentifier name = getNamesParser().parseIdentifier();
        ASTArgumentList argsList;
        if (isCurr(OPEN_PARENTHESIS)) {
            accept(OPEN_PARENTHESIS);
            argsList = getExpressionsParser().parseArgumentList();
            if (accept(CLOSE_PARENTHESIS) == null) {
                error(curr().getLocation(), "Expected ')'.");
            }
        }
        else {
            argsList = new ASTArgumentList(curr().getLocation(), Collections.emptyList());
        }
        if (isCurr(OPEN_BRACE)) {
            return new ASTEnumConstant(loc, annList, name, argsList, parseClassBody());
        }
        return new ASTEnumConstant(loc, annList, name, argsList, new ASTClassPartList(
                curr().getLocation(),
                Collections.emptyList()
                ));
    }

    /**
     * Parses a <code>ClassDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, and GeneralModifierList.
     * <em>
     * ClassDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [ClassModifierList] class Identifier [TypeParameters] [Superclass] [Superinterfaces] [Permits] ClassBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> as an
     *                  AccessModifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTClassDeclaration</code>.
     */
    public ASTClassDeclaration parseClassDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                                     ASTGeneralModifierList gms) {
        ASTClassDeclaration.Builder builder = new ASTClassDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        if (accept(CLASS) == null) {
            throw internalError(CLASS);
        }
        builder.setClassModifierList(convertToSpecificList(gms, 
                    "Unexpected class modifier.",
                    Arrays.asList(ABSTRACT, SHARED),
                    ASTClassModifierList::new))
               .setName(getNamesParser().parseIdentifier());
        if (isCurr(LESS_THAN)) {
            builder.setTypeParams(getTypesParser().parseTypeParameters());
        }
        if (isCurr(EXTENDS)) {
            builder.setSuperclass(parseSuperclass());
        }
        if (isCurr(IMPLEMENTS)) {
            builder.setSuperinterfaces(parseSuperinterfaces());
        }
        if (isCurr(PERMITS)) {
            builder.setPermits(parsePermits());
        }
        return builder.setClassParts(parseClassBody())
                .build();
    }

    /**
     * Parses a <code>Permits</code>.
     * <em>
     * Permits:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;permits DataTypeNoArrayList
     * </em>
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parsePermits() {
        if (accept(PERMITS) == null) {
            throw internalError(PERMITS);
        }
        return getTypesParser().parseDataTypeNoArrayList();
    }

    /**
     * Parses a <code>Superinterfaces</code>.
     * <em>
     * Superinterfaces:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;implements DataTypeNoArrayList
     * </em>
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseSuperinterfaces() {
        if (accept(IMPLEMENTS) == null) {
            throw internalError(IMPLEMENTS);
        }
        return getTypesParser().parseDataTypeNoArrayList();
    }

    /**
     * Parses a <code>Superclass</code>.
     * <em>
     * Superclass:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;extends DataTypeNoArray
     * </em>
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray parseSuperclass() {
        if (accept(EXTENDS) == null) {
            throw internalError(EXTENDS);
        }
        return getTypesParser().parseDataTypeNoArray();
    }

    /**
     * Parses a <code>ClassBody</code>.
     * <em>
     * ClassBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ }<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;{ ClassPartList }
     * </em>
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList parseClassBody() {
        if (accept(OPEN_BRACE) == null) {
            error(curr().getLocation(), "Expected '{'.");
        }
        ASTClassPartList classPartList = parseClassPartList();
        if (accept(CLOSE_BRACE) == null) {
            error(curr().getLocation(), "Expected '}'.");
        }
        return classPartList;
    }

    /**
     * Parses a <code>ClassPartList</code>.
     * <em>
     * ClassPartList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassPart {ClassPart}
     * </em>
     * @return An <code>ASTClassPartList</code>.
     */
    public ASTClassPartList parseClassPartList() {
        return parseMultiple(
                t -> Arrays.asList(AT_SIGN, PUBLIC, PRIVATE, INTERNAL, PROTECTED, CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,
                        ABSTRACT, OVERRIDE, SHARED, VOLATILE,
                        CONSTRUCTOR, MUT, CONSTANT, VOID, IDENTIFIER, LESS_THAN)
                        .contains(t.getType()),
                "Expected constructor, field, or method declaration.",
                this::parseClassPart,
                Arrays.asList(CLOSE_BRACE, EOF),
                ASTClassPartList::new,
                false
        );
    }

    /**
     * Parses a <code>ClassPart</code>.
     * <em>
     * ClassPart:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SharedConstructor<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FieldDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MethodDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeDeclaration
     * </em>
     * @return An <code>ASTClassPart</code>.
     */
    public ASTClassPart parseClassPart() {
        skipUnrecognizedTokens();
        Location loc = curr().getLocation();

        if (isCurr(SHARED) && isNext(CONSTRUCTOR)) {
            return parseSharedConstructor();
        }
        ASTAnnotationList annList = parseAnnotationList();
        ASTKeywordNode accessMod = null;
        if (isAcceptedOperator(Arrays.asList(PUBLIC, INTERNAL, PROTECTED, PRIVATE)) != null) {
            accessMod = parseAccessModifier();
        }
        ASTGeneralModifierList genModList = parseGeneralModifierList();

        switch(curr().getType()) {
        case CLASS, ENUM, INTERFACE, ANNOTATION, RECORD, ADT:
            return parseNestedType(loc, annList, accessMod, genModList);
        }

        if (isCurr(LESS_THAN)) {
            ASTTypeParameterList typeParams = getTypesParser().parseTypeParameters();
            if (isCurr(CONSTRUCTOR)) {
                if (!genModList.getChildren().isEmpty()) {
                    ASTKeywordNode modifier = genModList.get(0);
                    error(genModList.getLocation(), "Unexpected modifier: '" +
                            modifier.getKeyword().getRepresentation() + "'.");
                }
                // TypeParameters constructor ...
                return parseConstructorDeclaration(loc, annList, accessMod, typeParams);
            }
            else if (isCurr(CONSTANT)) {
                ASTKeywordNode constantKeyword = parseModifier(Arrays.asList(CONSTANT),
                        "Internal error (constant).");
                error(typeParams.getLocation(), "Type parameters not allowed on constant declaration.");
                return parseFieldDeclaration(loc, annList, accessMod, constantKeyword);
            }
            else {
                // TypeParameters mut|void|identifier
                return parseMethodDeclaration(loc, annList, accessMod, genModList, typeParams);
            }
        }
        // No type parameters:
        if (isCurr(VOID)) {
            // Result(void) ...
            return parseMethodDeclaration(loc, annList, accessMod, genModList);
        }
        else if (isCurr(CONSTRUCTOR)) {
            if (!genModList.getChildren().isEmpty()) {
                ASTKeywordNode modifier = genModList.get(0);
                error(genModList.getLocation(), "Unexpected modifier: '" +
                        modifier.getKeyword().getRepresentation() + "'.");
            }
            if (isNext(OPEN_BRACE)) {
                // constructor {
                return parseCompactConstructorDeclaration(loc, annList, accessMod);
            }
            else {
                // constructor (
                return parseConstructorDeclaration(loc, annList, accessMod);
            }
        }
        else {
            ASTVariableModifierList varModList = getStatementsParser().parseVariableModifierList();
            ASTDataType dt = getTypesParser().parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                // [mut] DataType identifier (
                return parseMethodDeclaration(loc, annList, accessMod, genModList, varModList, dt);
            }
            else {
                // [VariableModifierList] DataType ...
                return parseFieldDeclaration(loc, annList, accessMod, genModList, varModList, dt);
            }
        }
    }

    /**
     * Parses a <code>SharedConstructor</code>.
     * <em>
     * SharedConstructor:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;shared constructor ( ) Block
     * </em>
     * @return An <code>ASTSharedConstructor</code>.
     */
    public ASTSharedConstructor parseSharedConstructor() {
        Location loc = curr().getLocation();
        if (accept(SHARED) == null) {
            throw internalError(SHARED);
        }
        if (accept(CONSTRUCTOR) == null) {
            throw internalError(CONSTRUCTOR);
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTSharedConstructor(loc, getStatementsParser().parseBlock());
    }

    /**
     * Parses a <code>ConstructorDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, GeneralModifierList,
     * TypeParameterList.
     * <em>
     * ConstructorDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] ConstructorDeclarator [ConstructorInvocation] Block
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  Access Modifier.  If not present, <code>null</code>.
     * @param tps An already parsed <code>ASTTypeParameterList</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTConstructorDeclaration parseConstructorDeclaration(Location loc, ASTAnnotationList annList,
                                                                 ASTKeywordNode accessMod, ASTTypeParameterList tps) {
        ASTConstructorDeclaration.Builder builder = new ASTConstructorDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        builder.setConstructorDecl(parseConstructorDeclarator(tps));
        if (isCurr(COLON)) {
            builder.setConstructorInvocation(parseConstructorInvocation());
        }
        return builder.setBlock(getStatementsParser().parseBlock())
                .build();
    }

    /**
     * Parses a <code>ConstructorDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, GeneralModifierList.
     * <em>
     * ConstructorDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] ConstructorDeclarator [ConstructorInvocation] Block
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  Access Modifier.  If not present, <code>null</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTConstructorDeclaration parseConstructorDeclaration(Location loc, ASTAnnotationList annList,
                                                                 ASTKeywordNode accessMod) {
        ASTConstructorDeclaration.Builder builder = new ASTConstructorDeclaration.Builder()
                .setLocation(loc)
                .setAnnList(annList);
        if (accessMod != null) {
            builder.setAccessMod(accessMod);
        }
        builder.setConstructorDecl(parseConstructorDeclarator());
        if (isCurr(COLON)) {
            builder.setConstructorInvocation(parseConstructorInvocation());
        }
        return builder.setBlock(getStatementsParser().parseBlock())
                .build();
    }

    /**
     * Parses a <code>ConstructorInvocation</code>.
     * <em>
     * ConstructorInvocation:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] constructor ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;: [TypeArguments] super ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;: ExpressionName . [TypeArguments] super ( ArgumentList )<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;: Primary . [TypeArguments] super ( ArgumentList )
     * </em>
     * @return An <code>ASTConstructorInvocation</code>.
     */
    public ASTConstructorInvocation parseConstructorInvocation() {
        Location loc = curr().getLocation();
        if (accept(COLON) == null) {
            throw internalError(COLON);
        }
        ASTConstructorInvocation.Builder builder = new ASTConstructorInvocation.Builder()
                .setLocation(loc);
        if (isCurr(LESS_THAN)) {
            builder.setTypeArgs(getTypesParser().parseTypeArguments());
        }
        if (isAcceptedOperator(Arrays.asList(CONSTRUCTOR, SUPER)) != null) {
            builder.setConstructorKeyword(parseModifier(Arrays.asList(CONSTRUCTOR, SUPER),
                            "'constructor' or 'super'."
                    )
            );
        }
        else {
            error(curr().getLocation(), "Expected 'constructor' or 'super'.");
            // Consume unless stopper.
            if (!test(curr(), Arrays.asList(OPEN_PARENTHESIS, CLOSE_PARENTHESIS, GIVE, OPEN_BRACE,
                    EOF
            ))) {
                builder.setConstructorKeyword(parseModifier(Arrays.asList(curr().getType()),
                        curr().getType().getRepresentation()
                        ));
            }
            else {
                builder.setConstructorKeyword(new ASTKeywordNode(curr().getLocation(), UNKNOWN));
            }
        }

        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        builder.setArgsList(getExpressionsParser().parseArgumentList());
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return builder.build();
    }

    /**
     * Parses a <code>ConstructorDeclarator</code>.
     * <em>
     * ConstructorDeclarator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[TypeParameters] constructor ( [FormalParameterList] )
     * </em>
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator parseConstructorDeclarator() {
        Location loc = curr().getLocation();
        ASTTypeParameterList typeParams = null;
        if (isCurr(LESS_THAN)) {
            typeParams = getTypesParser().parseTypeParameters();
        }
        if (accept(CONSTRUCTOR) == null) {
            error(curr().getLocation(), "Expected 'constructor'.");
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTFormalParameterList formalParamList = parseFormalParameterList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        if (typeParams == null) {
            return new ASTConstructorDeclarator(loc, formalParamList);
        }
        return new ASTConstructorDeclarator(loc, typeParams, formalParamList);
    }

    /**
     * Parses a <code>ConstructorDeclarator</code>, given an already parsed
     * <code>ASTTypeParameterList</code>.
     * <em>
     * ConstructorDeclarator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[TypeParameters] constructor ( [FormalParameterList] )
     * </em>
     * @param tps An already parsed <code>ASTTypeParameterList</code>.
     * @return An <code>ASTConstructorDeclarator</code>.
     */
    public ASTConstructorDeclarator parseConstructorDeclarator(ASTTypeParameterList tps) {
        Location loc = tps.getLocation();
        if (accept(CONSTRUCTOR) == null) {
            throw internalError(CONSTRUCTOR);
        }
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTFormalParameterList formalParamList = parseFormalParameterList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        return new ASTConstructorDeclarator(loc, tps, formalParamList);
    }

    /**
     * Parses a <code>FieldDeclaration</code>, given an already parsed
     * Annotation List, Access Modifier, a GeneralModifierList, and a DataType.
     * <em>
     * FieldDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [FieldModifierList] DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *           Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTFieldDeclaration</code>.
     */
    public ASTFieldDeclaration parseFieldDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                 ASTGeneralModifierList gms, ASTVariableModifierList varModList, ASTDataType dt) {
        ASTFieldModifierList fieldModifiers = convertToSpecificList(gms, 
                    "Unexpected field modifier.",
                    Arrays.asList(CONSTANT, SHARED, VOLATILE),
                    ASTFieldModifierList::new
            );
        ASTVariableDeclaratorList varDeclList = getStatementsParser().parseVariableDeclaratorList();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (accessMod == null) {
            return new ASTFieldDeclaration(loc, annList, fieldModifiers, varModList, dt, varDeclList);
        }
        return new ASTFieldDeclaration(loc, annList, accessMod, fieldModifiers, varModList, dt, varDeclList);
    }

    /**
     * Parses a <code>FieldDeclaration</code>, given an already parsed
     * Annotation List, Access Modifier, a GeneralModifierList, and a DataType.
     * <em>
     * FieldDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [FieldModifierList] DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *           Access Modifier.  If not present, <code>null</code>.
     * @param constantMod An already parsed <code>ASTKeywordNode</code> representing
     *                    the keyword <code>constant</code>.
     * @return An <code>ASTFieldDeclaration</code>.
     */
    public ASTFieldDeclaration parseFieldDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                ASTKeywordNode constantMod) {
        ASTFieldModifierList fieldModifiers = new ASTFieldModifierList(constantMod.getLocation(), Arrays.asList(constantMod));
        ASTVariableModifierList varModList = getStatementsParser().parseVariableModifierList();
        ASTDataType dt = getTypesParser().parseDataType();
        ASTVariableDeclaratorList varDeclList = getStatementsParser().parseVariableDeclaratorList();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        if (accessMod == null) {
            return new ASTFieldDeclaration(loc, annList, fieldModifiers, varModList, dt, varDeclList);
        }
        return new ASTFieldDeclaration(loc, annList, accessMod, fieldModifiers, varModList, dt, varDeclList);
    }


    /**
     * Parses a <code>MethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, a GeneralModifierList,
     * and a TypeParameterList.
     * <em>
     * MethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [MethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing
     *                  an Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param tps An already parsed <code>ASTTypeParameterList</code>.  If not present, <code>null</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTAnnotationList annList, ASTKeywordNode accessMod,
                                                       ASTGeneralModifierList gms, ASTTypeParameterList tps) {
        ASTMethodModifierList methodModifiers = convertToSpecificList(gms, 
                    "Unexpected method modifier.",
                    Arrays.asList(FINAL, ABSTRACT, OVERRIDE, SHARED),
                    ASTMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader(tps);
        ASTMethodBody body = parseMethodBody();
        if (accessMod == null) {
            return new ASTMethodDeclaration(loc, annList, methodModifiers, header, body);
        }
        return new ASTMethodDeclaration(loc, annList, accessMod, methodModifiers, header, body);
    }

    /**
     * Parses a <code>MethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, and a GeneralModifierList.
     * <em>
     * MethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [MethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTAnnotationList annList,
                                                       ASTKeywordNode accessMod, ASTGeneralModifierList gms) {
        ASTMethodModifierList methodModifiers = convertToSpecificList(gms, 
                "Unexpected method modifier.",
                Arrays.asList(FINAL, ABSTRACT, OVERRIDE, SHARED),
                ASTMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader();
        ASTMethodBody body = parseMethodBody();
        if (accessMod != null) {
            return new ASTMethodDeclaration(loc, annList, accessMod, methodModifiers, header, body);
        }
        return new ASTMethodDeclaration(loc, annList, methodModifiers, header, body);
    }

    /**
     * Parses a <code>MethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, a GeneralModifierList,
     * and a DataType.
     * <em>
     * MethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [MethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param annList An already parsed <code>ASTAnnotationList</code>, possibly empty.
     * @param accessMod An already parsed <code>ASTKeywordNode</code> representing an
     *                  Access Modifier.  If not present, <code>null</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param varModList An already parsed <code>ASTVariableModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTAnnotationList annList,
                                                       ASTKeywordNode accessMod, ASTGeneralModifierList gms,
                                                       ASTVariableModifierList varModList, ASTDataType dt) {
        ASTMethodModifierList methodModifiers = convertToSpecificList(gms, 
                "Unexpected method modifier.",
                Arrays.asList(FINAL, ABSTRACT, OVERRIDE, SHARED),
                ASTMethodModifierList::new
        );
        Optional<ASTKeywordNode> mutModifier = parseMutModifier(varModList);
        ASTMethodHeader header;
        if (mutModifier.isPresent()) {
            header = parseMethodHeader(mutModifier.get(), dt);
        }
        else {
            header = parseMethodHeader(dt);
        }
        ASTMethodBody body = parseMethodBody();
        if (accessMod != null) {
            return new ASTMethodDeclaration(loc, annList, accessMod, methodModifiers, header, body);
        }
        return new ASTMethodDeclaration(loc, annList, methodModifiers, header, body);
    }

    /**
     * Parses a <code>MethodBody</code>.
     * <em>
     * MethodBody:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Block<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;;
     * </em>
     * @return An <code>ASTBlock</code>.
     */
    public ASTMethodBody parseMethodBody() {
        Location loc = curr().getLocation();
        if (isCurr(SEMICOLON)) {
            accept(SEMICOLON);
            return new ASTMethodBody(loc);
        }
        return new ASTMethodBody(loc, getStatementsParser().parseBlock());
    }

    /**
     * Parses an <code>AccessModifier</code>.
     * <em>
     * AccessModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;public<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;protected<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;internal<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;private
     * </em>
     * @return An <code>ASTKeywordNode</code>.
     */
    public ASTKeywordNode parseAccessModifier() {
        return parseModifier(
                Arrays.asList(PUBLIC, PROTECTED, INTERNAL, PRIVATE),
                "public, protected, internal, or private."
        );
    }

    /**
     * Parses a <code>GeneralModifierList</code>.
     * <em>
     * GeneralModifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
     * </em>
     * @return An <code>ASTGeneralModifierList</code>.
     */
    public ASTGeneralModifierList parseGeneralModifierList() {
        return parseMultiple(
                t -> test(t, Arrays.asList(ABSTRACT, CONSTANT, DEFAULT, FINAL, OVERRIDE, SEALED, SHARED, VOLATILE)),
                this::parseGeneralModifier,
                ASTGeneralModifierList::new
        );
    }

    /**
     * Parses a <code>GeneralModifier</code>.
     * <em>
     * GeneralModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;abstract<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;constant<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;default<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;final<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;override<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;sealed<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;shared<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;volatile
     * </em>
     * @return An <code>ASTKeywordNode</code> of the appropriate keyword.
     */
    public ASTKeywordNode parseGeneralModifier() {
        return parseModifier(
                Arrays.asList(ABSTRACT, CONSTANT, DEFAULT, FINAL, OVERRIDE, SEALED, SHARED, VOLATILE),
                "general modifier."
        );
    }

    /**
     * Parses a <code>MethodHeader</code>.
     * <em>
     * MethodHeader:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;TypeParameters Result MethodDeclarator
     * </em>
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader() {
        Location loc = curr().getLocation();
        if (isCurr(LESS_THAN)) {
            return new ASTMethodHeader(loc, getTypesParser().parseTypeParameters(), parseResult(), parseMethodDeclarator());
        }
        return new ASTMethodHeader(loc, parseResult(), parseMethodDeclarator());
    }

    /**
     * Parses a <code>MethodHeader</code>, given an already parsed
     * <code>ASTTypeParameterList</code>.
     * <em>
     * MethodHeader:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeParameters Result MethodDeclarator</strong>
     * </em>
     * @param tps Already parsed <code>ASTTypeParameterList</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader(ASTTypeParameterList tps) {
        return new ASTMethodHeader(tps.getLocation(), tps, parseResult(), parseMethodDeclarator());
    }

    /**
     * Parses a <code>MethodHeader</code>, given already parsed
     * <code>ASTDataType</code>.
     * <em>
     * MethodHeader:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeParameters Result MethodDeclarator</strong>
     * </em>
     * @param dt Already parsed <code>ASTDataType</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader(ASTDataType dt) {
        Location loc = dt.getLocation();
        return new ASTMethodHeader(loc, parseResult(dt), parseMethodDeclarator());
    }

    /**
     * Parses a <code>MethodHeader</code>, given already parsed
     * <code>ASTKeywordNode</code> of keyword <code>mut</code> and an
     * <code>ASTDataType</code>.
     * <em>
     * MethodHeader:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Result MethodDeclarator<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>TypeParameters Result MethodDeclarator</strong>
     * </em>
     * @param mutModifier An already parsed <code>ASTKeywordNode</code> of keyword <code>mut</code>.
     * @param dt Already parsed <code>ASTDataType</code>.
     * @return An <code>ASTMethodHeader</code>.
     */
    public ASTMethodHeader parseMethodHeader(ASTKeywordNode mutModifier, ASTDataType dt) {
        Location loc = dt.getLocation();
        return new ASTMethodHeader(loc, parseResult(mutModifier, dt), parseMethodDeclarator());
    }

    /**
     * Parses a <code>Result</code>.
     * <em>
     * Result:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MutModifier DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
     * </em>
     * @return An <code>ASTResult</code>.
     */
    public ASTResult parseResult() {
        Location loc = curr().getLocation();
        if (isCurr(VOID)) {
            ASTKeywordNode voidKeyword = parseModifier(
                    Arrays.asList(VOID),
                    "'void'."
            );
            return new ASTResult(loc, voidKeyword);
        }
        else if (isCurr(MUT)) {
            return new ASTResult(loc, parseMutModifier(), getTypesParser().parseDataType());
        }
        return new ASTResult(loc, getTypesParser().parseDataType());
    }

    /**
     * Parses a <code>Result</code>, given an already parsed <code>ASTDataType</code>.
     * <em>
     * Result:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MutModifier DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>DataType</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
     * </em>
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResult</code>.
     */
    public ASTResult parseResult(ASTDataType dt)
    {
        return new ASTResult(dt.getLocation(), dt);
    }

    /**
     * Parses a <code>Result</code>, given an already parsed <code>ASTKeywordNode</code>
     * of keyword <code>mut</code> and an <code>ASTDataType</code>.
     * <em>
     * Result:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;MutModifier DataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;<strong>DataType</strong><br>
     * &nbsp;&nbsp;&nbsp;&nbsp;void<br>
     * </em>
     * @param mutModifier An <code>ASTKeywordNode</code> of keyword <code>mut</code>.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTResult</code>.
     */
    public ASTResult parseResult(ASTKeywordNode mutModifier, ASTDataType dt)
    {
        return new ASTResult(dt.getLocation(), mutModifier, dt);
    }

    /**
     * Parses a <code>MethodDeclarator</code>.
     * <em>
     * MethodDeclarator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( [FormalParameterList] ) [MutModifier]
     * </em>
     * @return An <code>ASTMethodDeclarator</code>.
     */
    public ASTMethodDeclarator parseMethodDeclarator() {
        Location loc = curr().getLocation();
        ASTIdentifier methodName = getNamesParser().parseIdentifier();
        if (accept(OPEN_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected '('.");
        }
        ASTFormalParameterList formalParamList = parseFormalParameterList();
        if (accept(CLOSE_PARENTHESIS) == null) {
            error(curr().getLocation(), "Expected ')'.");
        }
        if (isCurr(MUT)) {
            return new ASTMethodDeclarator(loc, methodName, formalParamList, parseMutModifier());
        }
        return new ASTMethodDeclarator(loc, methodName, formalParamList);
    }

    /**
     * Parses a <code>MutModifier</code>.
     * <em>
     * MutModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;mut
     * </em>
     * @return An <code>ASTKeywordNode</code of operation <code>MUT</code>.
     */
    public ASTKeywordNode parseMutModifier() {
        return parseModifier(
                Collections.singletonList(MUT),
                "'mut'"
        );
    }

    /**
     * Given an already parsed <code>ASTVariableModifierList</code>, ensure
     * that there is either no modifiers or just <code>mut</code>.
     * @param varModList An already parsed <code>ASTVariableModifierList</code>.
     * @return An <code>Optional&ltASTKeywordNode&gt;</code> of keyword <code>mut</code>.
     */
    public Optional<ASTKeywordNode> parseMutModifier(ASTVariableModifierList varModList) {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTKeywordNode> children = varModList.getTypedChildren();
        ASTKeywordNode mutKeyword = null;
        for (ASTKeywordNode mod : children) {
            TokenType modifier = mod.getKeyword();
            if (!seen.add(modifier)) {
                error(mod.getLocation(), "Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (modifier != MUT) {
                error(mod.getLocation(), "Expected 'mut'.");
            }
            mutKeyword = mod;
        }
        return Optional.ofNullable(mutKeyword);
    }

    /**
     * Parses a <code>FormalParameterList</code>.
     * <em>
     * FormalParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameter {, FormalParameter}
     * </em
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList parseFormalParameterList() {
        ASTFormalParameterList node = parseList(
                t -> test(t, Arrays.asList(TAKE, AT_SIGN, IDENTIFIER, MUT, VAR)),
                "Expected data type",
                COMMA,
                this::parseFormalParameter,
                Arrays.asList(CLOSE_PARENTHESIS, CLOSE_BRACE, OPEN_BRACE, SEMICOLON,
                        PUBLIC, PROTECTED, INTERNAL, PRIVATE,  // Access modifiers
                        ABSTRACT, FINAL, CONSTANT, DEFAULT, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                        VAR, MUT,  // Variable modifiers (return type, field type)
                        VOID, CONSTRUCTOR,  // Other class part stuff
                        CLASS, INTERFACE, ENUM, ANNOTATION, RECORD, ADT,  // Type declarations
                        EOF),
                ASTFormalParameterList::new,
                false
        );

        // Enforce varargs parameter must be last.
        List<ASTFormalParameter> children = node.getTypedChildren();
        boolean ellipsisSeen = false;
        for (ASTFormalParameter formalParam : children) {
            if (ellipsisSeen) {
                error(curr().getLocation(), "Varargs parameter must be last in the list.");
            }
            if (formalParam.getEllipsisMod().isPresent()) {
                ellipsisSeen = true;
            }
        }

        return node;
    }

    /**
     * Parses a <code>FormalParameter</code>.
     * <em>
     * FormalParameter:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;VariableModifierList DataType ... Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataType ... Identifier
     * </em>
     * @return An <code>ASTFormalParameter</code>.
     */
    public ASTFormalParameter parseFormalParameter() {
        Location loc = curr().getLocation();
        ASTFormalParameter.Builder builder = new ASTFormalParameter.Builder()
                .setLocation(loc)
                .setAnnList(parseAnnotationList());
        if (isCurr(TAKE)) {
            builder.setTakeMod(parseModifier(Arrays.asList(TAKE),
                    "'take'"
            ));
        }
        builder.setVarModList(getStatementsParser().parseVariableModifierList())
                .setDataType(getTypesParser().parseDataType());
        if (isCurr(THREE_DOTS)) {
            builder.setEllipsisMod(parseModifier(Arrays.asList(THREE_DOTS),
                    "'...'"
            ));
        }
        return builder.setName(getNamesParser().parseIdentifier())
                .build();
    }

    /**
     * Converts the given general modifier list to a more specific modifier list,
     * giving an error if a found modifier is not in a more specific
     * list, or if there are duplicate modifiers.
     * @param genModList An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param errorMessage The error message expected.
     * @param expectedModifiers A List of expected modifiers (token types).
     * @param nodeSupplier A <code>BiFunction/code> accepting a <code>Location</code>
     *                     and a <code>List</code> of <code>ASTKeywordNode</code>s
     *                     that constructs and returns the desired list node type.
     * @return A new <code>ASTListNode</code> of the given type.
     */
    public <T extends ASTListNode<ASTKeywordNode>> T convertToSpecificList(ASTGeneralModifierList genModList,
                      String errorMessage, List<TokenType> expectedModifiers,
                      BiFunction<Location, List<ASTKeywordNode>, T> nodeSupplier) {
        // Dupe check.
        HashSet<TokenType> seen = new HashSet<>();
        List<ASTKeywordNode> children = genModList.getTypedChildren();
        for (ASTKeywordNode mod : children) {
            TokenType modifier = mod.getKeyword();
            if (!seen.add(modifier)) {
                error(mod.getLocation(), "Duplicate modifier found: " + modifier.getRepresentation());
            }
            if (!expectedModifiers.contains(modifier)) {
                error(mod.getLocation(), errorMessage);
            }
        }
        return nodeSupplier.apply(genModList.getLocation(), children);
    }

    private void skipUnrecognizedTokens() {
        List<TokenType> firstTokens = Arrays.asList(
                PUBLIC, INTERNAL, PROTECTED, PRIVATE,  // Access modifiers
                CONSTRUCTOR,
                AT_SIGN, MUT, VOID,  // Annotations, Result
                ABSTRACT, CONSTANT, DEFAULT, FINAL, OVERRIDE, SEALED, SHARED, VOLATILE,  // General modifiers
                CLASS, ENUM, INTERFACE, ANNOTATION, RECORD, ADT,  // Type declarations
                LESS_THAN, IDENTIFIER,  // Type parameters, name
                CLOSE_BRACE, EOF
        );
        while (isAcceptedOperator(firstTokens) == null) {
            Token unexpected = curr();
            Location loc = unexpected.getLocation();
            error(loc, "Unexpected token: " + unexpected.getType().getRepresentation());
            accept(unexpected.getType());
        }
    }
}
