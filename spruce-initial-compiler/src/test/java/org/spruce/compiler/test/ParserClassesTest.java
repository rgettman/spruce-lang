package org.spruce.compiler.test;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.ast.ASTDataType;
import org.spruce.compiler.ast.ASTFormalParameter;
import org.spruce.compiler.ast.ASTIdentifier;
import org.spruce.compiler.ast.ASTNode;
import org.spruce.compiler.ast.ASTVariableModifierList;
import org.spruce.compiler.parser.Parser;
import org.spruce.compiler.scanner.Scanner;

import static org.junit.jupiter.api.Assertions.*;
import static org.spruce.compiler.scanner.TokenType.*;
import static org.spruce.compiler.test.ParserTestUtility.*;

/**
 * All tests for the parser related to classes, methods, etc..
 */
public class ParserClassesTest
{
    /**
     * Tests formal parameter, no variable modifier list.
     */
    @Test
    public void testFormalParameterNoVML()
    {
        Parser parser = new Parser(new Scanner("String[] args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkBinary(node, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }

    /**
     * Tests formal parameter, variable modifier list.
     */
    @Test
    public void testFormalParameterOfVML()
    {
        Parser parser = new Parser(new Scanner("final String[] args"));
        ASTFormalParameter node = parser.parseFormalParameter();
        checkTrinary(node, null, ASTVariableModifierList.class, ASTDataType.class, ASTIdentifier.class);
        node.collapseThenPrint();
    }
}
