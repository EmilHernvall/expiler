package com.znaptag.expiler.ast;

public interface ASTVisitor
{
    public void visit(NumberNode node);
    public void visit(VariableNode node);
    public void visit(FunctionNode node);
    public void visit(AddNode node);
    public void visit(SubNode node);
    public void visit(MulNode node);
    public void visit(DivNode node);
    public void visit(ExpNode node);
}
