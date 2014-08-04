package com.znaptag.expiler;

import java.io.IOException;

import com.znaptag.expiler.ast.*;

// This is a recursive descent parser, which makes do with a single token
// lookahead. Consumes tokens from the Lexer and returns a fully formed AST.
public class Parser
{
    public static class ParseException extends Exception
    {
        public ParseException(String message)
        {
            super(message);
        }
    }

    private Lexer lexer;

    public Parser(Lexer lexer)
    {
        this.lexer = lexer;
    }

    // Parse parenthesized expressions as well as numbers and variables
    private ASTNode parseAtoms()
    throws IOException, ParseException
    {
        Token t = lexer.next();
        if (t.getType() == Token.Type.LPAREN) {
            ASTNode expr = parseAddSub();
            lexer.next();
            return expr;
        }
        else if (t.getType() == Token.Type.NUMBER ||
                 t.getType() == Token.Type.DECIMALNUMBER) {

            return new NumberNode(Double.parseDouble(t.getRepr()));
        }
        else if (t.getType() == Token.Type.IDENT) {
            return new VariableNode(t.getRepr());
        }

        throw new ParseException("Unexpected token");
    }

    // Parse exponentiations
    private ASTNode parseExp()
    throws IOException, ParseException
    {
        ASTNode left = parseAtoms();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.EXP) {

            lexer.next();

            ASTNode right = parseExp();
            return new ExpNode(left, right);
        }

        return left;
    }

    // Parse multiplication and divison
    private ASTNode parseMulDiv()
    throws IOException, ParseException
    {
        ASTNode left = parseExp();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.MUL) {

            lexer.next();

            ASTNode right = parseMulDiv();
            return new MulNode(left, right);
        }
        else if (t != null &&
                t.getType() == Token.Type.DIV) {
            lexer.next();

            ASTNode right = parseMulDiv();
            return new DivNode(left, right);
        }

        return left;
    }

    // Parse addition and subtraction
    private ASTNode parseAddSub()
    throws IOException, ParseException
    {
        ASTNode left = parseMulDiv();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.ADD) {

            lexer.next();

            ASTNode right = parseAddSub();
            return new AddNode(left, right);
        }
        else if (t != null &&
                 t.getType() == Token.Type.SUB) {

            lexer.next();

            ASTNode right = parseAddSub();
            return new SubNode(left, right);
        }

        return left;
    }

    public ASTNode parse()
    throws IOException, ParseException
    {
        return parseAddSub();
    }

    // Basic test method: Parse and output a string representation of the AST
    public static void main(String[] args)
    throws IOException, ParseException
    {
        Parser parser = new Parser(new Lexer(System.in));
        ASTNode tree = parser.parse();
        System.out.println(tree.toString());
    }
}
