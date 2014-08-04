package com.znaptag.expiler.ast;

public class VariableNode implements ASTNode
{
    private String name;

    public VariableNode(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public void visit(ASTVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return name;
    }
}
