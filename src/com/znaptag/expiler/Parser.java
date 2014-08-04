package com.znaptag.expiler;

import java.io.IOException;

import com.znaptag.expiler.ast.*;

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

    private ASTNode parseAtoms()
    throws IOException, ParseException
    {
        Token t = lexer.next();
        if (t.getType() == Token.Type.LPAREN) {
            //System.out.println("PAREN");
            ASTNode expr = parseAddSub();
            lexer.next();
            return expr;
        }
        else if (t.getType() == Token.Type.NUMBER ||
                 t.getType() == Token.Type.DECIMALNUMBER) {

            //System.out.println("NUMBER");
            return new NumberNode(Double.parseDouble(t.getRepr()));
        }
        else if (t.getType() == Token.Type.IDENT) {
            //System.out.println("VAR");
            return new VariableNode(t.getRepr());
        }

        throw new ParseException("Unexpected token");
    }

    private ASTNode parseExp()
    throws IOException, ParseException
    {
        ASTNode left = parseAtoms();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.EXP) {

            lexer.next();

            //System.out.println("EXP");
            ASTNode right = parseExp();
            return new ExpNode(left, right);
        }

        return left;
    }

    private ASTNode parseMulDiv()
    throws IOException, ParseException
    {
        ASTNode left = parseExp();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.MUL) {

            lexer.next();

            //System.out.println("MUL");
            ASTNode right = parseMulDiv();
            return new MulNode(left, right);
        }
        else if (t != null &&
                t.getType() == Token.Type.DIV) {
            lexer.next();

            //System.out.println("DIV");
            ASTNode right = parseMulDiv();
            return new DivNode(left, right);
        }

        return left;
    }

    private ASTNode parseAddSub()
    throws IOException, ParseException
    {
        ASTNode left = parseMulDiv();

        Token t = lexer.peek(0);
        if (t != null &&
            t.getType() == Token.Type.ADD) {

            lexer.next();

            //System.out.println("ADD");
            ASTNode right = parseAddSub();
            return new AddNode(left, right);
        }
        else if (t != null &&
                 t.getType() == Token.Type.SUB) {

            lexer.next();

            //System.out.println("SUB");
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

    public static void main(String[] args)
    throws IOException, ParseException
    {
        Parser parser = new Parser(new Lexer(System.in));
        ASTNode tree = parser.parse();
        System.out.println(tree.toString());
    }
}
