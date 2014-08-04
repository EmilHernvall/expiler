package com.znaptag.expiler.ast;

public class SubNode extends BinaryNode
{
    public SubNode(ASTNode left, ASTNode right)
    {
        super(left, right);
    }

    @Override
    public void visit(ASTVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return "(" + getLeft().toString() + " - " + getRight().toString() + ")";
    }
}
