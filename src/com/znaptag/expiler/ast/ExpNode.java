package com.znaptag.expiler.ast;

public class ExpNode extends BinaryNode
{
    public ExpNode(ASTNode base, ASTNode exponent)
    {
        super(base, exponent);
    }

    @Override
    public void visit(ASTVisitor visitor)
    {
        visitor.visit(this);
    }

    @Override
    public String toString()
    {
        return "(" + getLeft().toString() + " ^ " + getRight().toString() + ")";
    }
}
