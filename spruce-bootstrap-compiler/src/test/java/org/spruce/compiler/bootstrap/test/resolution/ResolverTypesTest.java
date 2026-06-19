package org.spruce.compiler.bootstrap.test.resolution;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.spruce.compiler.bootstrap.ast.classes.ASTClassDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTConstructorDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTInterfaceDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTInterfaceMethodDeclaration;
import org.spruce.compiler.bootstrap.ast.classes.ASTMethodDeclaration;
import org.spruce.compiler.bootstrap.symbol.ParameterizedSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.test.symbol.SymbolCreatorTestUtility;

import static org.spruce.compiler.bootstrap.symbol.Symbol.*;
import static org.spruce.compiler.bootstrap.test.resolution.ResolverTestUtility.*;
import static org.spruce.compiler.bootstrap.test.util.TestUtility.ensureIsa;

public class ResolverTypesTest {

    /**
     * Tests early resolution of formal parameters.
     */
    @Test
    public void testFormalParameterEarlyResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    constructor(Integer x, String y) {}
                    void testMethod(Boolean b, Double d) {}
                }
                interface Interface {
                    void interfaceMethod(String s, Test t);
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTConstructorDeclaration constrDecl = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTConstructorDeclaration.class);
        ParameterizedSymbol constr = constrDecl.getDeclSymbol();
        SymbolCreatorTestUtility.checkSymbol(constr, "<init>(spruce.lang.Integer, spruce.lang.String)",
                Symbol.Kind.CONSTRUCTOR, FLAG_NONE);

        ASTMethodDeclaration testMethodDecl = ensureIsa(testDecl.getClassParts().getTypedChildren().get(1),
                ASTMethodDeclaration.class);
        ParameterizedSymbol testMethod = testMethodDecl.getDeclSymbol();
        SymbolCreatorTestUtility.checkSymbol(testMethod, "testMethod(spruce.lang.Boolean, spruce.lang.Double)",
                Symbol.Kind.METHOD, FLAG_NONE);

        ASTInterfaceDeclaration interfaceDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(1),
                ASTInterfaceDeclaration.class);
        ASTInterfaceMethodDeclaration interfaceMethodDecl = ensureIsa(
                interfaceDecl.getInterfaceParts().getTypedChildren().get(0),
                ASTInterfaceMethodDeclaration.class);
        ParameterizedSymbol interfaceMethod = interfaceMethodDecl.getDeclSymbol();
        SymbolCreatorTestUtility.checkSymbol(interfaceMethod, "interfaceMethod(spruce.lang.String, Test)",
                Symbol.Kind.METHOD, FLAG_MOD_ABSTRACT);
    }

    /**
     * Tests fallback early resolution of formal parameters.
     */
    @Test
    public void testFormalParameterEarlyResolutionFallback() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(DoesNotExist dne) {}
                }
                """
        );
        ResolverTestUtility.Trio trio = compileSoFar(codes, 1);

        ASTClassDeclaration testDecl = ensureIsa(trio.ocus().get(1).getTypeDeclList().get(0), ASTClassDeclaration.class);
        ASTMethodDeclaration testMethodDecl = ensureIsa(testDecl.getClassParts().getTypedChildren().get(0),
                ASTMethodDeclaration.class);
        ParameterizedSymbol testMethod = testMethodDecl.getDeclSymbol();
        SymbolCreatorTestUtility.checkSymbol(testMethod, "testMethod(DoesNotExist)",
                Symbol.Kind.METHOD, FLAG_NONE);
    }

    /**
     * Test dupe formal parameters, dupe methods due to resolving simple type
     * and fully qualified type in formal parameters to the same symbol.
     */
    @Test
    public void testFormalParameterEarlyDupeResolution() {
        List<String> codes = List.of(
                CODE_SPRUCE_LANG,
                """
                class Test {
                    void testMethod(Integer i) {}
                    void testMethod(spruce.lang.Integer i) {}
                }
                """
        );
        compileSoFar(codes, 2);
    }
}
