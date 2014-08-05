package com.znaptag.expiler.ast;

public abstract class AbstractVisitor implements ASTVisitor
{
    @Override
    public void visit(NumberNode node)
    {
    }

    @Override
    public void visit(VariableNode node)
    {
    }

    @Override
    public void visit(FunctionNode node)
    {
        ASTNode expr = node.getExpression();
        expr.visit(this);
    }

    @Override
    public void visit(AddNode node)
    {
        ASTNode left = node.getLeft();
        left.visit(this);

        ASTNode right = node.getRight();
        right.visit(this);
    }

    @Override
    public void visit(SubNode node)
    {
        ASTNode left = node.getLeft();
        left.visit(this);

        ASTNode right = node.getRight();
        right.visit(this);
    }

    @Override
    public void visit(MulNode node)
    {
        ASTNode left = node.getLeft();
        left.visit(this);

        ASTNode right = node.getRight();
        right.visit(this);
    }

    @Override
    public void visit(DivNode node)
    {
        ASTNode left = node.getLeft();
        left.visit(this);

        ASTNode right = node.getRight();
        right.visit(this);
    }

    @Override
    public void visit(ExpNode node)
    {
        ASTNode left = node.getLeft();
        left.visit(this);

        ASTNode right = node.getRight();
        right.visit(this);
    }
}
