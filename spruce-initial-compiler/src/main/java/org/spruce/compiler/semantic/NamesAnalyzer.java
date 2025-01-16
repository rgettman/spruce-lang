package org.spruce.compiler.semantic;

import java.util.List;

import org.spruce.compiler.ast.names.ASTIdentifier;
import org.spruce.compiler.ast.names.ASTNamespaceName;
import org.spruce.compiler.common.MessageProducer;
import org.spruce.compiler.symbol.ParentSymbol;
import org.spruce.compiler.symbol.Symbol;
import org.spruce.compiler.symbol.SymbolTable;
import org.spruce.compiler.symbol.TopLevelSymbolTable;

import static org.spruce.compiler.symbol.SymbolTable.Scope.*;

/**
 * A <code>NamesAnalyzer</code> is a <code>BasicAnalyzer</code> that analyzes
 * symbols belonging to AST elements having to do with naming.
 */
public class NamesAnalyzer extends BasicAnalyzer {
    /**
     * Constructs a <code>BasicAnalyzer</code> using a <code>MessageProducer</code>.
     * @param analyzer A <code>SemanticAnalyzer</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     */
    public NamesAnalyzer(SemanticAnalyzer analyzer, MessageProducer msgProducer) {
        super(analyzer, msgProducer);
    }

    /**
     * Analyzes a <code>NamespaceName</code>, which is expected to contain at
     * least one identifier.
     * @param namespaceName An <code>ASTNamespaceName</code>.
     * @return A <code>Symbol</code> for the namespace name.
     */
    public Symbol analyzeNamespaceName(ASTNamespaceName namespaceName, TopLevelSymbolTable topLevel) {
        List<ASTIdentifier> identifiers = namespaceName.getTypedChildren();

        // First
        SymbolTable parent = topLevel;
        if (identifiers.isEmpty()) {
            throw internalError("identifier in namespace");
        }
        ASTIdentifier first = identifiers.get(0);
        ParentSymbol curr = new ParentSymbol(first.getLocation(), first.getValue(), parent, 0, NAMESPACE);
        Symbol symbol = curr;

        // Rest
        for (int i = 1; i < identifiers.size(); i++) {
            parent = curr.getTable();
            ASTIdentifier id = identifiers.get(i);
            curr = new ParentSymbol(id.getLocation(), id.getValue(), parent, 0, NAMESPACE);
            parent.insertSymbol(curr);
        }

        return symbol;
    }
}
