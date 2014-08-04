package com.znaptag.expiler;

import java.util.Iterator;
import java.util.Deque;
import java.util.ArrayDeque;
import java.util.List;
import java.util.ArrayList;
import java.io.Reader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

// Regex based lexer which accepts an InputStream or an Reader and outputs
// a stream of Tokens
public class Lexer
{
    private Reader reader;

    // Accumulated token data
    private StringBuilder current;

    // Pending tokens
    private Deque<Token> tokens;

    public Lexer(InputStream stream)
    {
        this.reader = new InputStreamReader(stream);
        this.current = new StringBuilder();
        this.tokens = new ArrayDeque<>();
    }

    public Lexer(Reader reader)
    {
        this.reader = reader;
        this.current = new StringBuilder();
        this.tokens = new ArrayDeque<>();
    }

    // Find tokens within the stream
    private void consume()
    throws IOException
    {
        Token.Type activeTokenType = null;
        String activeTokenString = null;
        boolean endOfStream = false;

        // Loop until we have a fully accumulated token, or we reach the end of
        // the stream
        while (true) {

            // If there's nothing in the buffer, there's no need to try to
            // match anything
            if (current.length() != 0) {

                // Loop over all known tokens, and apply their matching regex
                Token.Type matchingTokenType = null;
                for (Token.Type type : Token.Type.values()) {
                    Pattern p = type.getPattern();
                    Matcher m = p.matcher(current.toString());
                    if (m.matches()) {
                        matchingTokenType = type;
                        activeTokenString = m.group(0);
                    }
                }

                // If there's no currently matching token type, but there was
                // one during the previous iteration, exit the loop.
                if ((matchingTokenType == null &&
                     activeTokenType != null) ||
                    endOfStream) {

                    break;
                }

                activeTokenType = matchingTokenType;
            }

            // Retrieve a byte from the reader and append to buffer. Spin
            // around.
            int res = reader.read();
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

        // If there's a whitespace token, call ourselves recursively until we
        // get something else. Typically, this shouldn't require more than
        // a single call, ever.
        if (activeTokenType == Token.Type.WHITESPACE) {
            consume();
        }
        // If not whitespace, add the new token to the deque
        else {
            tokens.addLast(new Token(activeTokenType, activeTokenString));
        }
    }

    // Retrieve the next token and remove it. Returns null if nothing is
    // available.
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

    // "Look ahead" n tokens without removing anything. This is required by the
    // recursive descent parser. Returns null if nothing is available.
    public Token peek(int n)
    throws IOException
    {
        // Make sure the deque contains enough tokens
        if (tokens.size() <= n) {
            for (int i = 0; i <= n; i++) {
                consume();
            }
        }

        // Step forward through the deque until we found the token we want
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

    // Basic test method: Read from stdin and print the identified tokens
    public static void main(String[] args)
    throws Exception
    {
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
