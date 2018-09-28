package org.spruce.compiler.scanner;

import java.util.Objects;

/**
 * A <code>Token</code> consists of a <code>Type</code>, the string value,
 * and its <code>Location</code>.
 */
public class Token
{
    private TokenType myType;
    private String myValue;
    private Location myLocation;

    /**
     * Constructs  a <code>Token</code> with the given <code>TokenType</code>,
     * the given value.
     * @param type The <code>TokenType</code>.
     * @param value The string value of the token.
     */
    public Token(TokenType type, String value)
    {
        this(null, type, value);
    }

    /**
     * Constructs at the given <code>Location</code> a <code>Token</code> with
     * the given <code>TokenType</code>, and the given value.
     * @param location The <code>Location</code> of the token.
     * @param type The <code>TokenType</code>.
     * @param value The string value of the token.
     */
    public Token(Location location, TokenType type, String value)
    {
        myLocation = location;
        myType = type;
        myValue = value;
    }

    /**
     * Returns the type of the token.
     * @return The type of the token.
     */
    public TokenType getType()
    {
        return myType;
    }

    /**
     * Returns the string value of the token.
     * @return The string value of the token.
     */
    public String getValue()
    {
        return myValue;
    }

    /**
     * Returns the <code>Location</code> of the token.
     * @return The <code>Location</code> of the token.
     */
    public Location getLocation()
    {
        return myLocation;
    }

    /**
     * Returns a string of the format <code>Token{type, value}</code>.
     * @return A string representation of this <code>Token</code>.
     */
    @Override
    public String toString()
    {
        return "Token{" + myType + ", \"" + myValue + "}";

    }

    /**
     * Returns whether all attributes (type, string value) match.
     * @param other The other object.
     * @return Whether all attributes (type, string value) match.
     */
    @Override
    public boolean equals(Object other)
    {
        if (other == null) return false;
        if (other instanceof Token)
        {
            Token t = (Token) other;
            return myType == t.myType && Objects.equals(myValue, t.myValue);
        }
        return false;
    }

    /**
     * Returns a hash code for a <code>Token</code>.
     * @return A hash code for a <code>Token</code>.
     */
    @Override
    public int hashCode()
    {
        return Objects.hash(myType, myValue);
    }
}
