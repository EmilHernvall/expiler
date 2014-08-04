package com.znaptag.expiler;

import java.util.Iterator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.io.InputStream;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Lexer
{
    private InputStream stream;
    private StringBuilder current;
    private Deque<Token> tokens;

    public Lexer(InputStream stream)
    {
        this.stream = stream;
        this.current = new StringBuilder();
        this.tokens = new ArrayDeque<>();
    }

    private void consume()
    throws IOException
    {
        Token.Type activeTokenType = null;
        String activeTokenString = null;
        boolean endOfStream = false;
        while (true) {
            //System.out.println("looping... current.length() = " + current.length());
            if (current.length() != 0) {
                //System.out.println(current.toString());

                Token.Type matchingTokenType = null;
                for (Token.Type type : Token.Type.values()) {
                    Pattern p = type.getPattern();
                    Matcher m = p.matcher(current.toString());
                    if (m.matches()) {
                        //System.out.println(type);
                        matchingTokenType = type;
                        activeTokenString = m.group(0);
                    }
                }

                //System.out.println(matchingTokenType);

                if ((matchingTokenType == null &&
                     activeTokenType != null) ||
                    endOfStream) {

                    break;
                }

                activeTokenType = matchingTokenType;
            }

            int res = stream.read();
            //System.out.println("res=" + res);
            if (res == -1) {
                endOfStream = true;
                if (current.length() == 0) {
                    break;
                } else {
                    continue;
                }
            }
            current.appendCodePoint(res);
        }

        if (activeTokenType == null) {
            return;
        }

        //System.out.println("got " + activeTokenType + ": " + activeTokenString);
        current.delete(0, activeTokenString.length());

        if (activeTokenType == Token.Type.WHITESPACE) {
            consume();
        } else {
            tokens.addLast(new Token(activeTokenType, activeTokenString));
        }
    }

    public Token next()
    throws IOException
    {
        if (tokens.size() == 0) {
            consume();
            if (tokens.size() == 0) {
                return null;
            }
        }

        return tokens.removeFirst();
    }

    public Token peek(int n)
    throws IOException
    {
        if (tokens.size() <= n) {
            for (int i = 0; i <= n; i++) {
                consume();
            }
        }

        Iterator<Token> it = tokens.iterator();
        int i = 0;
        while (it.hasNext()) {
            Token t = it.next();
            if (i < n) {
                i++;
                continue;
            }

            return t;
        }

        return null;
    }

    public static void main(String[] args)
    throws Exception
    {
        /*Pattern p = Pattern.compile("[0-9]+\\.[0-9]+");
        Matcher m = p.matcher("3.1415");
        if (m.matches()) {
            System.out.println(m.group(0));
        } else {
            System.out.println("no match");
        }*/

        Lexer lexer = new Lexer(System.in);
        while (true) {
            Token t = lexer.next();
            if (t == null) {
                break;
            }

            System.out.println("got " + t.getType() + ": " + t.getRepr());

            //Token t2 = lexer.peek(2);
            //if (t2 != null) {
            //    System.out.println("lookahead(2) " + t2.getType() + ": " + t2.getRepr());
            //}
        }
    }
}
