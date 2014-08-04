package com.znaptag.expiler;

import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import java.io.FileOutputStream;

import java.lang.reflect.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import com.znaptag.expiler.ast.*;

public class Compiler
{
    private static class VariableVisitor implements ASTVisitor
    {
        private Set<String> variables;
        private int maxStackDepth = 2;
        private int currentStackDepth = 0;

        public VariableVisitor()
        {
            variables = new HashSet<String>();
        }

        public int getMaxStackDepth()
        {
            return maxStackDepth;
        }

        public Set<String> getVariables()
        {
            return variables;
        }

        @Override
        public void visit(NumberNode node)
        {
            currentStackDepth++;
            maxStackDepth = Math.max(currentStackDepth, maxStackDepth);
        }

        @Override
        public void visit(VariableNode node)
        {
            variables.add(node.getName());
            currentStackDepth++;
            maxStackDepth = Math.max(currentStackDepth, maxStackDepth);
        }

        @Override
        public void visit(AddNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            currentStackDepth--;
        }

        @Override
        public void visit(SubNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            currentStackDepth--;
        }

        @Override
        public void visit(MulNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            currentStackDepth--;
        }

        @Override
        public void visit(DivNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            currentStackDepth--;
        }

        @Override
        public void visit(ExpNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            currentStackDepth--;
        }
    }

    private static class CodeGenerationVisitor implements ASTVisitor
    {
        private MethodVisitor mv;
        private Map<String, Integer> registers;

        public CodeGenerationVisitor(MethodVisitor mv,
                                     Map<String, Integer> registers)
        {
            this.mv = mv;
            this.registers = registers;
        }

        @Override
        public void visit(NumberNode node)
        {
            //System.out.println("NUMBER: " + node.getNumber());
            mv.visitLdcInsn(node.getNumber());
        }

        @Override
        public void visit(VariableNode node)
        {
            //System.out.println("VARIABLE: " + node.getName());

            int reg = registers.get(node.getName());
            mv.visitVarInsn(DLOAD, reg);
        }

        @Override
        public void visit(AddNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            //System.out.println("ADD");

            mv.visitInsn(DADD);
        }

        @Override
        public void visit(SubNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            //System.out.println("SUB");

            mv.visitInsn(DSUB);
        }

        @Override
        public void visit(MulNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            //System.out.println("MUL");

            mv.visitInsn(DMUL);
        }

        @Override
        public void visit(DivNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            //System.out.println("DIV");

            mv.visitInsn(DDIV);
        }

        @Override
        public void visit(ExpNode node)
        {
            ASTNode left = node.getLeft();
            left.visit(this);

            ASTNode right = node.getRight();
            right.visit(this);

            //System.out.println("EXP");

            mv.visitMethodInsn(INVOKESTATIC,
                               "java/lang/Math",
                               "pow",
                               "(DD)D",
                               false);
        }
    }

    public static class MyClassLoader extends ClassLoader
    {
        public Class defineClass(String name, byte[] b)
        {
            return defineClass(name, b, 0, b.length);
        }
    }

    public Compiler()
    {
    }

    public CompiledExpression compile(String name, ASTNode tree)
    {
        // Find all variables used by the expression
        VariableVisitor variableVisitor = new VariableVisitor();
        tree.visit(variableVisitor);

        Set<String> variables = variableVisitor.getVariables();
        System.out.println("Found " + variables.size() + " variables");
        System.out.println("Max stack depth is " + variableVisitor.getMaxStackDepth());

        // Setup class header
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_7,
                 ACC_PUBLIC,
                 "com/znaptag/expiler/" + name,
                 null,
                 "java/lang/Object",
                 new String[] { "com/znaptag/expiler/CompiledExpression" });

        // Create default constructor
        MethodVisitor mv1 = cw.visitMethod(ACC_PUBLIC,
                                           "<init>",
                                           "()V",
                                           null,
                                           null);
            mv1.visitVarInsn(ALOAD, 0);
            mv1.visitMethodInsn(INVOKESPECIAL,
                                "java/lang/Object",
                                "<init>",
                                "()V",
                                false);
            mv1.visitInsn(RETURN);
            mv1.visitMaxs(2, 2);
            mv1.visitEnd();

        // Create computation method
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                                          "compute",
                                          "(Ljava/util/Map;)D",
                                          null,
                                          null);
            mv.visitCode();

            // Load all variables from the map into registers
            int regCounter = 2;
            Map<String, Integer> registers = new HashMap<>();
            for (String var : variables) {
                mv.visitVarInsn(ALOAD, 1);
                mv.visitLdcInsn(var);
                mv.visitMethodInsn(INVOKEINTERFACE,
                                   "java/util/Map",
                                   "get",
                                   "(Ljava/lang/Object;)Ljava/lang/Object;",
                                   true);
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL,
                                   "java/lang/Double",
                                   "doubleValue",
                                   "()D",
                                   false);
                mv.visitVarInsn(DSTORE, regCounter);

                registers.put(var, regCounter);

                regCounter++;
            }

            CodeGenerationVisitor codegen = new CodeGenerationVisitor(mv, registers);
            tree.visit(codegen);

            mv.visitInsn(DRETURN);
            mv.visitMaxs(2*variableVisitor.getMaxStackDepth(), regCounter+1);
            mv.visitEnd();

        // Finish class and retrieve byte code
        cw.visitEnd();

        byte[] b = cw.toByteArray();

        try {
            FileOutputStream out = new FileOutputStream(name + ".class");
            out.write(b, 0, b.length);
            out.close();
        }
        catch (IOException e) {
        }

        // Register class
        MyClassLoader classLoader = new MyClassLoader();
        Class c = classLoader.defineClass("com.znaptag.expiler." + name, b);

        try {
            return (CompiledExpression)c.newInstance();
        }
        catch (InstantiationException e) {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args)
    throws Exception
    {
        Parser parser = new Parser(new Lexer(System.in));
        ASTNode tree = parser.parse();

        Compiler compiler = new Compiler();
        CompiledExpression expr = compiler.compile("TestExpression", tree);

        Map<String, Double> vars = new HashMap<>();
        vars.put("z", 3.0);

        double res = expr.compute(vars);
        System.out.println("res: " + res);
    }
}
