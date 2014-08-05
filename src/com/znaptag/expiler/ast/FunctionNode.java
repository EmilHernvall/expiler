package com.znaptag.expiler.ast;

public class FunctionNode implements ASTNode
{
    private String name;
    private ASTNode expr;

    public FunctionNode(String name, ASTNode expr)
    {
        this.name = name;
        this.expr = expr;
    }

    public String getName()
    {
        return name;
    }

    public ASTNode getExpression()
    {
        return expr;
    }

    @Override
    public void visit(ASTVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return name + "(" + expr.toString() + ")";
    }
}
