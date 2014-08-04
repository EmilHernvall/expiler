package com.znaptag.expiler.ast;

public class NumberNode implements ASTNode
{
    private double num;

    public NumberNode(double num)
    {
        this.num = num;
    }

    public double getNumber()
    {
        return num;
    }

    @Override
    public void visit(ASTVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return String.valueOf(num);
    }
}
