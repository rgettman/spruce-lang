package org.spruce.compiler.bootstrap.parser;

import org.spruce.compiler.bootstrap.ast.names.ASTIdentifier;
import org.spruce.compiler.bootstrap.ast.types.*;
import org.spruce.compiler.bootstrap.common.Location;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.scanner.Scanner;

import static org.spruce.compiler.bootstrap.scanner.TokenType.*;

/**
 * A <code>TypesParser</code> is a <code>BasicParser</code> that parses types.
 */
public class TypesParser extends BasicParser {
    /**
     * Constructs a <code>TypesParser</code> using a <code>Scanner</code>.
     *
     * @param scanner A <code>Scanner</code>.
     * @param parser A <code>Parser</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public TypesParser(Scanner scanner, Parser parser, MessageProducer msgProducer) {
        super(scanner, parser, msgProducer);
    }

    /**
     * Parses a <code>DataType</code>.
     * <em>
     * DataType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType !<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;BaseDataType ?
     * </em>
     * @return An <code>ASTDataType</code>.
     */
    public ASTDataType parseDataType() {
        Location loc = curr().getLocation();
        ASTBaseDataType base = parseBaseDataType();
        return new ASTDataType(loc, base);
    }

    /**
     * Parses a <code>BaseDataType</code>.
     * <em>
     * BaseDataType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;ArrayType
     * </em>
     * @return An <code>ASTBaseDataType</code> that could be an
     *     <code>ASTDataTypeNoArray</code> or an <code>ASTArrayType</code>.
     */
    public ASTBaseDataType parseBaseDataType() {
        return parseDataTypeNoArray();
    }

    /**
     * Parses a <code>DataTypeNoArrayList</code>.
     * <em>
     * DataTypeNoArrayList:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray {, DataTypeNoArray}
     * </em>
     * @return An <code>ASTDataTypeNoArrayList</code>.
     */
    public ASTDataTypeNoArrayList parseDataTypeNoArrayList() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected a data type (no array).",
                COMMA,
                getTypesParser()::parseDataTypeNoArray,
                ASTDataTypeNoArrayList::new
        );
    }

    /**
     * Parses a <code>DataTypeNoArray</code>.
     * <em>
     * DataTypeNoArray:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;SimpleType<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;DataTypeNoArray . SimpleType
     * </em>
     * @return An <code>ASTDataTypeNoArray</code>.
     */
    public ASTDataTypeNoArray parseDataTypeNoArray() {
        return parseList(
                t -> test(t, IDENTIFIER),
                "Expected an identifier",
                DOT,
                this::parseSimpleType,
                ASTDataTypeNoArray::new
        );
    }

    /**
     * Parses a <code>SimpleType</code>.
     * <em>
     * SimpleType:<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier<br>
     * &nbsp;&nbsp;&nbsp;&nbsp;Identifier TypeArguments
     * </em>
     * @return An <code>ASTSimpleType</code>.
     */
    public ASTSimpleType parseSimpleType() {
        Location loc = curr().getLocation();
        ASTIdentifier name = getNamesParser().parseIdentifier();
        return new ASTSimpleType(loc, name);
    }
}
