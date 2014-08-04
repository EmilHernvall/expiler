package com.znaptag.expiler.ast;

public abstract class BinaryNode implements ASTNode
{
    private ASTNode left;
    private ASTNode right;

    public BinaryNode(ASTNode left, ASTNode right)
    {
        this.left = left;
        this.right = right;
    }

    public ASTNode getLeft() { return left; }
    public ASTNode getRight() { return right; }
}
