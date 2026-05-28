package org.spruce.compiler.bootstrap.parser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;

import org.spruce.compiler.bootstrap.ast.ASTKeywordNode;
import org.spruce.compiler.bootstrap.ast.ASTListNode;
import org.spruce.compiler.bootstrap.ast.classes.*;
import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.statements.ASTVariableDeclaratorList;
import org.spruce.compiler.bootstrap.ast.types.ASTDataType;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArray;
import org.spruce.compiler.bootstrap.ast.types.ASTDataTypeNoArrayList;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.Scanner;
import org.spruce.compiler.bootstrap.scanner.Token;
import org.spruce.compiler.bootstrap.scanner.TokenType;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>ClassesParser</code> is a <code>BasicParser</code> that parses
 * classes.
 */
public class ClassesParser extends BasicParser {
    /**
     * Constructs a <code>ClassesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public ClassesParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * General method to parse a nested type and produce a "part" type.  Reduces
     * code repetition because many different "part" nodes can contain any of
     * the same list of nested types.
     * <em>
     * TypeDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ClassDeclaration<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;InterfaceDeclaration
     * </em>
     * @param loc The <code>Location</code>.
     * @param genModList An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTTypeDeclaration</code> of the appropriate type, e.g. <code>ASTClassDeclaration</code>.
     */
    private ASTTypeDeclaration parseNestedType(Location loc, ASTGeneralModifierList genModList) {
        return switch (curr().getType()) {
            case CLASS -> parseClassDeclaration(loc, genModList);
            case INTERFACE -> parseInterfaceDeclaration(loc, genModList);
            default -> throw internalError("type declaration");
        };
    }

    /**
     * Parses an <code>InterfaceDeclaration</code>, given an already parsed
     * general modifier list.
     * <em>
     * InterfaceDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;interface Identifier [TypeParameters] [ExtendsInterfaces] [Permits] InterfaceBody
     * </em>
     * @param loc The <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTInterfaceDeclaration</code>.
     */
    public ASTInterfaceDeclaration parseInterfaceDeclaration(Location loc, ASTGeneralModifierList gms) {
        ASTInterfaceDeclaration.Builder builder = new ASTInterfaceDeclaration.Builder()
                .setLocation(loc);
        if (accept(INTERFACE) == null) {
            throw internalError(INTERFACE);
        }
        // No expected modifiers for interfaces (yet).
        convertToSpecificList(gms,
                        "Unexpected interface modifier.",
                        Arrays.asList(),
                        ASTClassModifierList::new);
        builder.setName(getNamesParser().parseIdentifier());
        if (isCurr(EXTENDS)) {
            builder.setExtendsInterfaces(parseExtendsInterfaces());
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
                t -> Arrays.asList(ABSTRACT, OVERRIDE, CLASS, INTERFACE,
                                 CONSTANT, VOID, IDENTIFIER)
                        .contains(t.getType()),
                "Expected constant or method declaration.",
                this::parseInterfacePart,
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
        ASTGeneralModifierList genModList = parseGeneralModifierList();

        switch(curr().getType()) {
        case CLASS, INTERFACE -> { return parseNestedType(loc, genModList); }
        }

        if (isCurr(VOID)) {
            // Result(void) ...
            return parseInterfaceMethodDeclaration(loc, genModList);
        }
        else {
            ASTDataType dt = getTypesParser().parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                // [mut] DataType identifier (
                return parseInterfaceMethodDeclaration(loc, genModList, dt);
            }
            else {
                // DataType ...
                return parseConstantDeclaration(loc, genModList, dt);
            }
        }
    }

    /**
     * Parses an <code>InterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, a GeneralModifierList,
     * and a TypeParameterList.
     * <em>
     * InterfaceMethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[InterfaceMethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTGeneralModifierList gms) {
        ASTInterfaceMethodModifierList interfaceMethodModifiers = convertToSpecificList(gms,
                "Unexpected interface method modifier.",
                Arrays.asList(OVERRIDE),
                ASTInterfaceMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader();
        ASTMethodBody body = parseMethodBody();
        return new ASTInterfaceMethodDeclaration(loc, interfaceMethodModifiers, header, body);
    }

    /**
     * Parses an <code>InterfaceMethodDeclaration</code>, given optionally already
     * parsed productions: AnnotationList, AccessModifier, and a GeneralModifierList.
     * <em>
     * InterfaceMethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[AnnotationList] [AccessModifier] [InterfaceMethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>.
     * @return An <code>ASTInterfaceMethodDeclaration</code>.
     */
    public ASTInterfaceMethodDeclaration parseInterfaceMethodDeclaration(Location loc, ASTGeneralModifierList gms,
                                                                         ASTDataType dt) {
        ASTInterfaceMethodModifierList interfaceMethodModifiers = convertToSpecificList(gms,
                "Unexpected interface method modifier.",
                Arrays.asList(OVERRIDE),
                ASTInterfaceMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader(dt);
        ASTMethodBody body = parseMethodBody();
        return new ASTInterfaceMethodDeclaration(loc, interfaceMethodModifiers, header, body);
    }

    /**
     * Parses a <code>ConstantDeclaration</code>, given an already parsed
     * AnnotationList, AccessModifier, GeneralModifierList, and DataType.
     * <em>
     * ConstantDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstantModifier DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     *            It should contain only <code>constant</code>.
     * @param dt An already parsed <code>ASTKeywordNode</code>, present.
     * @return An <code>ASTConstantDeclaration</code>.
     */
    public ASTConstantDeclaration parseConstantDeclaration(Location loc, ASTGeneralModifierList gms, ASTDataType dt) {
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
        return new ASTConstantDeclaration(loc, constantMod, dt, varDeclList);
    }

    /**
     * Parses a <code>ClassDeclaration</code>, given an already parsed
     * GeneralModifierList.
     * <em>
     * ClassDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;class Identifier [Superclass] [Superinterfaces] ClassBody
     * </em>
     * @param loc The <code>Location</code>.
     * @return An <code>ASTClassDeclaration</code>.
     */
    public ASTClassDeclaration parseClassDeclaration(Location loc, ASTGeneralModifierList gms) {
        ASTClassDeclaration.Builder builder = new ASTClassDeclaration.Builder()
                .setLocation(loc);
        if (accept(CLASS) == null) {
            throw internalError(CLASS);
        }
        builder.setClassModifierList(convertToSpecificList(gms,
                        "Unexpected class modifier.",
                        Arrays.asList(ABSTRACT),
                        ASTClassModifierList::new))
                .setName(getNamesParser().parseIdentifier());
        if (isCurr(EXTENDS)) {
            builder.setSuperclass(parseSuperclass());
        }
        if (isCurr(IMPLEMENTS)) {
            builder.setSuperinterfaces(parseSuperinterfaces());
        }
        return builder.setClassParts(parseClassBody())
                .build();
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
                t -> Arrays.asList(ABSTRACT, CLASS, INTERFACE, OVERRIDE,
                                CONSTRUCTOR, CONSTANT, VOID, IDENTIFIER)
                        .contains(t.getType()),
                "Expected constructor, field, or method declaration.",
                this::parseClassPart,
                ASTClassPartList::new,
                false
        );
    }

    /**
     * Parses a <code>ClassPart</code>.
     * <em>
     * ClassPart:<br>
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

        ASTGeneralModifierList genModList = parseGeneralModifierList();

        switch (curr().getType()) {
        case CLASS, INTERFACE -> { return parseNestedType(loc, genModList); }
        }

        // No type parameters:
        if (isCurr(VOID)) {
            // Result(void) ...
            return parseMethodDeclaration(loc, genModList);
        }
        else if (isCurr(CONSTRUCTOR)) {
            if (!genModList.getChildren().isEmpty()) {
                ASTKeywordNode modifier = genModList.get(0);
                error(genModList.getLocation(), "Unexpected modifier: '" +
                        modifier.getKeyword().getRepresentation() + "'.");
            }
            return parseConstructorDeclaration(loc);
        }
        else {
            ASTDataType dt = getTypesParser().parseDataType();
            if (isCurr(IDENTIFIER) && isNext(OPEN_PARENTHESIS)) {
                // DataType identifier (
                return parseMethodDeclaration(loc, genModList, dt);
            }
            else {
                // DataType ...
                return parseFieldDeclaration(loc, genModList, dt);
            }
        }
    }

    /**
     * Parses a <code>ConstructorDeclaration</code>.
     * <em>
     * ConstructorDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ConstructorDeclarator Block
     * </em>
     * @param loc The starting <code>Location</code>.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTConstructorDeclaration parseConstructorDeclaration(Location loc) {
        ASTConstructorDeclaration.Builder builder = new ASTConstructorDeclaration.Builder()
                .setLocation(loc);
        builder.setConstructorDecl(parseConstructorDeclarator());
        return builder.setBlock(getStatementsParser().parseBlock())
                .build();
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
        return new ASTConstructorDeclarator(loc, formalParamList);
    }

    /**
     * Parses a <code>FieldDeclaration</code>, given an already parsed
     * Annotation List, Access Modifier, a GeneralModifierList, and a DataType.
     * <em>
     * FieldDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[FieldModifierList] DataType VariableDeclaratorList
     * </em>
     * @param loc The given <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTFieldDeclaration</code>.
     */
    public ASTFieldDeclaration parseFieldDeclaration(Location loc, ASTGeneralModifierList gms, ASTDataType dt) {
        ASTFieldModifierList fieldModifiers = convertToSpecificList(gms,
                "Unexpected field modifier.",
                Arrays.asList(CONSTANT),
                ASTFieldModifierList::new
        );
        ASTVariableDeclaratorList varDeclList = getStatementsParser().parseVariableDeclaratorList();
        if (accept(SEMICOLON) == null) {
            error(curr().getLocation(), "Expected ';'.");
        }
        return new ASTFieldDeclaration(loc, fieldModifiers, dt, varDeclList);
    }

    /**
     * Parses a <code>MethodDeclaration</code>, given optionally already
     * parsed productions: a GeneralModifierList.
     * <em>
     * MethodDeclaration:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;[MethodModifierList] MethodHeader MethodBody
     * </em>
     * @param loc The starting <code>Location</code>.
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTGeneralModifierList gms) {
        ASTMethodModifierList methodModifiers = convertToSpecificList(gms,
                "Unexpected method modifier.",
                Arrays.asList(ABSTRACT, OVERRIDE),
                ASTMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader();
        ASTMethodBody body = parseMethodBody();
        return new ASTMethodDeclaration(loc, methodModifiers, header, body);
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
     * @param gms An already parsed <code>ASTGeneralModifierList</code>, possibly empty.
     * @param dt An already parsed <code>ASTDataType</code>, present.
     * @return An <code>ASTMethodDeclaration</code>.
     */
    public ASTMethodDeclaration parseMethodDeclaration(Location loc, ASTGeneralModifierList gms,
                                                       ASTDataType dt) {
        ASTMethodModifierList methodModifiers = convertToSpecificList(gms,
                "Unexpected method modifier.",
                Arrays.asList(ABSTRACT, OVERRIDE),
                ASTMethodModifierList::new
        );
        ASTMethodHeader header = parseMethodHeader(dt);
        ASTMethodBody body = parseMethodBody();
        return new ASTMethodDeclaration(loc, methodModifiers, header, body);
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
     * Parses a <code>GeneralModifierList</code>.
     * <em>
     * GeneralModifierList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;GeneralModifier {GeneralModifier}
     * </em>
     * @return An <code>ASTGeneralModifierList</code>.
     */
    public ASTGeneralModifierList parseGeneralModifierList() {
        return parseMultiple(
                t -> test(t, Arrays.asList(ABSTRACT, CONSTANT, OVERRIDE)),
                this::parseGeneralModifier,
                ASTGeneralModifierList::new
        );
    }

    /**
     * Parses a <code>GeneralModifier</code>.
     * <em>
     * GeneralModifier:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;constant<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;override
     * </em>
     * @return An <code>ASTKeywordNode</code> of the appropriate keyword.
     */
    public ASTKeywordNode parseGeneralModifier() {
        return parseModifier(
                Arrays.asList(ABSTRACT, CONSTANT, OVERRIDE),
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
        return new ASTMethodHeader(loc, parseResult(), parseMethodDeclarator());
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
     * Parses a <code>Result</code>.
     * <em>
     * Result:<br>
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
        return new ASTResult(loc, getTypesParser().parseDataType());
    }

    /**
     * Parses a <code>Result</code>, given an already parsed <code>ASTDataType</code>.
     * <em>
     * Result:<br>
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
     * Parses a <code>MethodDeclarator</code>.
     * <em>
     * MethodDeclarator:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier ( [FormalParameterList] )
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
        return new ASTMethodDeclarator(loc, methodName, formalParamList);
    }

    /**
     * Parses a <code>FormalParameterList</code>.
     * <em>
     * FormalParameterList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;FormalParameter {, FormalParameter}
     * </em>
     * @return An <code>ASTFormalParameterList</code>.
     */
    public ASTFormalParameterList parseFormalParameterList() {
        return parseList(
                t -> test(t, Arrays.asList(IDENTIFIER)),
                "Expected data type",
                COMMA,
                this::parseFormalParameter,
                ASTFormalParameterList::new,
                false
        );
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
                .setLocation(loc);
        builder.setDataType(getTypesParser().parseDataType());
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
                CONSTRUCTOR,
                VOID,  // Result
                ABSTRACT, CONSTANT, OVERRIDE,  // General modifiers
                CLASS, INTERFACE, // Type declarations
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
