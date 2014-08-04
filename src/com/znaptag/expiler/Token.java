package com.znaptag.expiler;

import java.util.regex.Pattern;

public class Token
{
    public enum Type
    {
        NUMBER (Pattern.compile("[0-9]+")),
        DECIMALNUMBER (Pattern.compile("[0-9]+\\.[0-9]*")),
        IDENT (Pattern.compile("[A-Za-z]+[A-Za-z0-9_]*")),
        ADD (Pattern.compile("\\+")),
        SUB (Pattern.compile("-")),
        MUL (Pattern.compile("\\*")),
        DIV (Pattern.compile("/")),
        EXP (Pattern.compile("\\^")),
        LPAREN (Pattern.compile("\\(")),
        RPAREN (Pattern.compile("\\)")),
        WHITESPACE (Pattern.compile("[ \t\n]+"));

        private Pattern pattern;

        private Type(Pattern pattern)
        {
            this.pattern = pattern;
        }

        public Pattern getPattern()
        {
            return pattern;
        }
    }

    private Type type;
    private String repr;

    public Token(Type type, String repr)
    {
        this.type = type;
        this.repr = repr;
    }

    public Type getType()
    {
        return type;
    }

    public String getRepr()
    {
        return repr;
    }
}
