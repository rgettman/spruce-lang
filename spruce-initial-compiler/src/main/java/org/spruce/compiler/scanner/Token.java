package org.spruce.compiler.scanner;

import java.util.Objects;
import java.util.Optional;

import org.spruce.compiler.message.CompilerMessage;

/**
 * A <code>Token</code> consists of a <code>Type</code>, the string value,
 * and its <code>Location</code>.
 */
public class Token {
    private final TokenType myType;
    private final String myValue;
    private final Location myLocation;
    private final CompilerMessage myMessage;

    /**
     * Constructs a <code>Token</code> with the given <code>TokenType</code>,
     * the given value.
     * @param type The <code>TokenType</code>.
     * @param value The string value of the token.
     */
    public Token(TokenType type, String value) {
        this(null, type, value, null);
    }

    /**
     * Constructs at the given <code>Location</code> a <code>Token</code> with
     * the given <code>TokenType</code>, and the given value.
     * @param location The <code>Location</code> of the token.
     * @param type The <code>TokenType</code>.
     * @param value The string value of the token.
     */
    public Token(Location location, TokenType type, String value) {
        this(location, type, value, null);
    }

    /**
     * Constructs at the given <code>Location</code> a <code>Token</code> with
     * the given <code>TokenType</code>, the given value, and the given
     * <code>CompilerMessage</code>.
     * @param location The <code>Location</code> of the token.
     * @param type The <code>TokenType</code>.
     * @param value The string value of the token.
     * @param message The <code>CompilerMessage</code>.
     */
    public Token(Location location, TokenType type, String value, CompilerMessage message) {
        myLocation = location;
        myType = type;
        myValue = value;
        myMessage = message;
    }

    /**
     * Returns the type of the token.
     * @return The type of the token.
     */
    public TokenType getType() {
        return myType;
    }

    /**
     * Returns the string value of the token.
     * @return The string value of the token.
     */
    public String getValue() {
        return myValue;
    }

    /**
     * Returns the <code>Location</code> of the token.
     * @return The <code>Location</code> of the token.
     */
    public Location getLocation() {
        return myLocation;
    }

    /**
     * Returns the <code>CompilerMessage</code>, if it exists.
     * @return An <code>Optional&lt;CompilerMessage&gt;</code>.
     */
    public Optional<CompilerMessage> getCompilerMessage() {
        return Optional.ofNullable(myMessage);
    }

    /**
     * Returns a string of the format <code>Token{type, value[, message]}</code>.
     * @return A string representation of this <code>Token</code>.
     */
    @Override
    public String toString() {
        return "Token{" + myType + ", \"" + myValue + "\"" +
                (myMessage != null ? ", " + myMessage : "") +
                "}";
    }

    /**
     * Returns whether all attributes (type, string value) match.
     * @param other The other object.
     * @return Whether all attributes (type, string value) match.
     */
    @Override
    public boolean equals(Object other) {
        if (other == null) return false;
        if (other instanceof Token t) {
            return myType == t.myType && Objects.equals(myValue, t.myValue) &&
                    Objects.equals(myMessage, t.myMessage);
        }
        return false;
    }

    /**
     * Returns a hash code for a <code>Token</code>.
     * @return A hash code for a <code>Token</code>.
     */
    @Override
    public int hashCode() {
        return Objects.hash(myType, myValue, myMessage);
    }
}
