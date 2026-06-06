package org.spruce.compiler.bootstrap.resolution;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spruce.compiler.bootstrap.ast.names.*;
import org.spruce.compiler.bootstrap.common.MessageProducer;
import org.spruce.compiler.bootstrap.symbol.ChildSymbolTable;
import org.spruce.compiler.bootstrap.symbol.ParentSymbol;
import org.spruce.compiler.bootstrap.symbol.Symbol;
import org.spruce.compiler.bootstrap.symbol.SymbolTable;
import org.spruce.compiler.bootstrap.symbol.GlobalLookup;
import org.spruce.compiler.bootstrap.symbol.TypeSymbol;
import org.spruce.compiler.bootstrap.symbol.VariableSymbol;

import static org.spruce.compiler.bootstrap.symbol.Symbol.Kind.FIELD;

/**
 * A <code>NamesResolver</code> is a <code>BasicResolver</code> that resolves
 * symbols that refer to declared symbols: Variable names of local variables,
 * parameters, and fields.  It has many entry points for resolving name-like
 * symbols of various kinds.
 */
public class NamesResolver extends BasicResolver {
    /**
     * Constructs a <code>NamesResolver</code>.
     * @param resolver An <code>Resolver</code>.
     * @param msgProducer A <code>MessageProducer</code>.
     * @param global The <code>GlobalLookup</code>.
     */
    public NamesResolver(Resolver resolver, MessageProducer msgProducer, GlobalLookup global) {
        super(resolver, msgProducer, global);
    }

    /**
     * Resolve all symbols in a <code>TypeName</code>.
     * @param tn An <code>ASTTypeName</code>.
     * @return An <code>Optional&lt;TypeSymbol&gt;</code>.
     */
    public Optional<TypeSymbol> resolveUseTypeName(ASTTypeName tn) {
        List<ASTIdentifier> typeIds = tn.getTypedChildren();
        Optional<ParentSymbol> optParent = resolveNamespaceOrTypeNameIds(typeIds, getGlobalLookup());
        if (optParent.isPresent()) {
            ParentSymbol parent = optParent.get();
            if (parent.isType()) {
                return Optional.of((TypeSymbol) parent);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolve all symbols in a <code>TypeName</code>.
     * @param tn An <code>ASTTypeName</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveTypeName(ASTTypeName tn, ResolutionContext ctx) {
        // The parser ensures at least one simple type.
        List<ASTIdentifier> ids = tn.getTypedChildren();
        getTypesResolver().resolveIdentifiers(ids, ctx).ifPresent(tn::setResolvedDataType);
    }

    /**
     * Resolve all symbols in a <code>NamespaceOrTypeName</code>.
     * @param notn An <code>ASTNamespaceOrTypeName</code>.
     * @return An <code>Optional&lt;ParentSymbol&gt;</code>.
     */
    public Optional<ParentSymbol> resolveNamespaceOrTypeName(ASTNamespaceOrTypeName notn) {
        List<ASTIdentifier> namespaceOrTypeIds = notn.getTypedChildren();
        SymbolTable currTable = getGlobalLookup();
        return resolveNamespaceOrTypeNameIds(namespaceOrTypeIds, currTable);
    }

    private Optional<ParentSymbol> resolveNamespaceOrTypeNameIds(List<ASTIdentifier> ids, SymbolTable currTable) {
        // Parser guarantees at least one identifier, which must be a namespace.
        String first = ids.get(0).getValue();
        ParentSymbol resolved;
        if (currTable.containsNamespace(first)) {
            resolved = (ParentSymbol) currTable.get(first);
            currTable = resolved.getTable();
        }
        else {
            errorSymbolNotFound(ids.get(0).getLocation(), first);
            return Optional.empty();
        }

        // The remaining identifiers can be namespaces or types, but once a
        // type is found, then all subsequent identifiers must be types until
        // fully resolved.
        boolean typeFound = false;
        for (int i = 1; i < ids.size(); i++) {
            ASTIdentifier id = ids.get(i);
            String name = id.getValue();

            boolean nameFound;
            if (typeFound) {
                nameFound = currTable.containsType(name);
            }
            else {
                nameFound = currTable.containsNamespaceOrType(name);
            }
            if (nameFound) {
                resolved = (ParentSymbol) currTable.get(name);
                currTable = resolved.getTable();
                typeFound = resolved.getKind() != Symbol.Kind.NAMESPACE;
            }
            else {
                errorSymbolNotFound(id.getLocation(), name);
                return Optional.empty();
            }
        }

        return Optional.of(resolved);
    }

    /**
     * Resolves the <code>ExpressionName</code> to a variable and a datatype.
     * It can be resolved to a variable, a parameter, or a field.
     * @param exprName An <code>ASTExpressionName</code>.
     * @param ctx A <code>ResolutionContext</code>.
     */
    public void resolveExpressionName(ASTExpressionName exprName, ResolutionContext ctx) {
        // The parser ensures at least one identifier.
        List<ASTIdentifier> ids = exprName.getTypedChildren();

        // 1. Resolve the first identifier, which could be a "variable" (local
        //    variable, a parameter, or a field), a type, or a namespace.
        ASTIdentifier first = ids.get(0);
        String name = first.getValue();
        List<Symbol> matches = resolveFirstIdentifier(first, ctx);
        if (matches.isEmpty()) {
            errorSymbolNotFound(first.getLocation(), name);
            return;
        }
        else if (matches.size() > 1) {
            error(first.getLocation(), "Symbol " + name + " is ambiguous with " + matches.size() +
                    " matches.");
            for (Symbol match : matches) {
                note(match.getLocation(), name + " matches here.");
            }
            return;
        }
        Symbol resolved = matches.get(0);
        if (ctx.isShared() && resolved.getKind() == FIELD && !resolved.isShared()) {
            error(first.getLocation(), "Symbol " + name +
                    " cannot be resolved from a shared context.");
            return;
        }

        // 2. Within the scope of the previous identifier, resolve the next
        //    identifier.
        for (int i = 1; i < ids.size(); i++) {
            ASTIdentifier next = ids.get(i);
            Optional<Symbol> optResolved = resolveSubsequentIdentifier(next, resolved);
            if (optResolved.isEmpty()) {
                errorSymbolNotFound(next.getLocation(), next.getValue());
                return;
            }
            resolved = optResolved.get();
        }

        // 3. Must be a variable at the end.
        if (!resolved.isVariable()) {
            error(resolved.getLocation(), "Variable expected.");
            return;
        }

        exprName.setResolvedEntity((VariableSymbol) resolved);
    }

    private List<Symbol> resolveFirstIdentifier(ASTIdentifier first, ResolutionContext ctx) {
        // 1. Check scopes up to, and including, the immediately enclosing type.
        Optional<VariableSymbol> resolvedVar = resolveInType(first, ctx);
        if (resolvedVar.isPresent()) {
            return List.of(resolvedVar.get());
        }

        // 2. Check for a namespace or a type name.
        Optional<ParentSymbol> resolvedParent =
                getTypesResolver().resolveNamespaceOrTypeByName(first.getValue(), ctx);
        if (resolvedParent.isPresent()) {
            return List.of(resolvedParent.get());
        }

        ParentSymbol parent = getTypesResolver().findEnclosingType(ctx.enclosingSymbol());
        // 3. Repeat (4) for each enclosing type further up the enclosing type
        //    hierarchy.  Stopping with a match means shadowing possible
        //    matches further up the enclosing type hierarchy.
        while (parent.isType()) {
            TypeSymbol type = (TypeSymbol) parent;

            // 4. Starting with the current type, check the superclass
            //    hierarchy and the superinterface hierarchy.  Stopping with a
            //    match means hiding possible matches further up the hierarchy.
            //    Multiple non-hidden matches = ambiguous error.
            List<VariableSymbol> matches = resolveFieldOfAncestor(first, type);
            if (!matches.isEmpty()) {
                return List.copyOf(matches);
            }
            parent = ((ChildSymbolTable) type.getParent()).getParent();
        }

        // 5. Eventually, check shared use statements (they don't exist yet).

        // 6. Not resolved.
        return List.of();
    }

    private Optional<Symbol> resolveSubsequentIdentifier(ASTIdentifier next, Symbol parent) {
        String name = next.getValue();

        // Namespace -> namespace or type.
        if (parent.getKind() == Symbol.Kind.NAMESPACE) {
            SymbolTable table = ((ParentSymbol) parent).getTable();
            if (table.containsNamespaceOrType(name)) {
                return Optional.of(table.get(name));
            }
        }
        // Type -> type or variable (must be shared).
        else if (parent.isType()) {
            SymbolTable table = ((ParentSymbol) parent).getTable();
            if (table.containsType(name)) {
                return Optional.of(table.get(name));
            }
            else if (table.containsVariable(name)) {
                VariableSymbol resolved = (VariableSymbol) table.get(name);
                if (!resolved.isShared()) {
                    error(next.getLocation(), "Non-shared field " + name +
                            " cannot be resolved from a shared context.");
                }
                return Optional.of(resolved);
            }
        }
        // Variable -> another variable (must NOT be shared.)
        else if (parent.isVariable()) {
            TypeSymbol type = ((VariableSymbol) parent).getDataType();
            SymbolTable table = type.getTable();
            VariableSymbol resolved = (VariableSymbol) table.get(name);
            if (resolved.isShared()) {
                error(next.getLocation(), "Shared field " + name +
                        " cannot be resolved from a non-shared context.  Use the type name," +
                        " not a variable of the type.");
            }
            return Optional.of(resolved);
        }

        return Optional.empty();
    }

    private Optional<VariableSymbol> resolveInType(ASTIdentifier id, ResolutionContext ctx) {
        String name = id.getValue();
        ParentSymbol parent = ctx.enclosingSymbol();
        boolean enclosingTypeFound = false;
        while (!enclosingTypeFound) {
            enclosingTypeFound = parent.isType();
            ChildSymbolTable table = parent.getTable();
            if (table.containsVariable(name)) {
                return Optional.of((VariableSymbol) table.get(name));
            }
            else {
                parent = ((ChildSymbolTable) parent.getParent()).getParent();
            }
        }
        return Optional.empty();
    }

    private List<VariableSymbol> resolveFieldOfAncestor(ASTIdentifier id, TypeSymbol type) {
        String name = id.getValue();
        List<VariableSymbol> resolved = new ArrayList<>(2);

        SymbolTable table = type.getTable();
        if (table.containsField(name)) {
            return List.of((VariableSymbol) table.get(name));
        }

        // Walk up superclass hierarchy then superinterface hierarchy looking
        // for the symbol name.
        if (type.getSuperclass().isPresent()) {
            TypeSymbol superclass = type.getSuperclass().get();
            resolved.addAll(resolveFieldOfAncestor(id, superclass));
        }

        resolved.addAll(resolveFieldInSuperinterfaces(name, type.getSuperinterfaces()));

        return resolved;
    }

    private List<VariableSymbol> resolveFieldInSuperinterfaces(String name, List<TypeSymbol> superinterfaces) {
        List<VariableSymbol> resolved = new ArrayList<>(2);
        for (TypeSymbol superinterface : superinterfaces) {
            ChildSymbolTable child = superinterface.getTable();
            if (child.containsField(name)) {
                // This will hide anything further up the chain.
                resolved.add((VariableSymbol) child.get(name));
            }
            else {
                // Recur on this superinterface's superinterfaces.
                resolved.addAll(resolveFieldInSuperinterfaces(name, superinterface.getSuperinterfaces()));
            }
        }
        return resolved;
    }
}
