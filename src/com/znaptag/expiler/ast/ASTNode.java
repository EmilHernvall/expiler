package com.znaptag.expiler.ast;

public interface ASTNode
{
    public void visit(ASTVisitor visitor);
}
