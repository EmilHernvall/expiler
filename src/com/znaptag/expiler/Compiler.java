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
    // Visitor to do an initial pass over the AST and collect information about
    // which variables are used, and the maximum stack depth
    private static class VariableVisitor extends AbstractVisitor
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
            super.visit(node);
            currentStackDepth--;
        }

        @Override
        public void visit(SubNode node)
        {
            super.visit(node);
            currentStackDepth--;
        }

        @Override
        public void visit(MulNode node)
        {
            super.visit(node);
            currentStackDepth--;
        }

        @Override
        public void visit(DivNode node)
        {
            super.visit(node);
            currentStackDepth--;
        }

        @Override
        public void visit(ExpNode node)
        {
            super.visit(node);
            currentStackDepth--;
        }
    }

    // Visitor which walks the AST and generates the actual java bytecode
    private static class CodeGenerationVisitor extends AbstractVisitor
    {
        // asm methodvisitor for code generation
        private MethodVisitor mv;
        // maps variable names to register indices
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
            // Numbers are loaded onto the stack as constants
            mv.visitLdcInsn(node.getNumber());
        }

        @Override
        public void visit(VariableNode node)
        {
            // Variables have already been loaded into local registers
            int reg = registers.get(node.getName());
            mv.visitVarInsn(DLOAD, reg);
        }

        @Override
        public void visit(AddNode node)
        {
            super.visit(node);
            mv.visitInsn(DADD);
        }

        @Override
        public void visit(SubNode node)
        {
            super.visit(node);
            mv.visitInsn(DSUB);
        }

        @Override
        public void visit(MulNode node)
        {
            super.visit(node);
            mv.visitInsn(DMUL);
        }

        @Override
        public void visit(DivNode node)
        {
            super.visit(node);
            mv.visitInsn(DDIV);
        }

        @Override
        public void visit(ExpNode node)
        {
            super.visit(node);
            // There's no opcode for exponentiation, so we call the static
            // method Math.pow(base, exp)
            mv.visitMethodInsn(INVOKESTATIC,
                               "java/lang/Math",
                               "pow",
                               "(DD)D",
                               false);
        }
    }

    // Custom class loaded that we use to define and load the class
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
        // Find all variables used by the expression, as well as the max stack
        // depth
        VariableVisitor variableVisitor = new VariableVisitor();
        tree.visit(variableVisitor);

        Set<String> variables = variableVisitor.getVariables();

        // Setup class header
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_7, // java version
                 ACC_PUBLIC, // flags, ACC_PUBLIC means public access
                 "com/znaptag/expiler/" + name, // qualified name of new class
                 null,
                 "java/lang/Object", // parent class
                 new String[] { "com/znaptag/expiler/CompiledExpression" } // implemented interfaces
                 );

        // Create default constructor
        MethodVisitor mv1 = cw.visitMethod(ACC_PUBLIC,
                                           "<init>", // java internal name of constructors
                                           "()V", // return type is void, no parameters needed
                                           null,
                                           null);

            // load "this" reference from register 0
            mv1.visitVarInsn(ALOAD, 0);
            // invoke java.lang.Object constructor
            mv1.visitMethodInsn(INVOKESPECIAL,
                                "java/lang/Object",
                                "<init>",
                                "()V",
                                false);
            // return void
            mv1.visitInsn(RETURN);
            mv1.visitMaxs(2, 2);
            mv1.visitEnd();

        // Create computation method
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                                          "compute", // implement the compute method
                                          "(Ljava/util/Map;)D", // accepts a java.util.Map as parameter, returns a double
                                          null,
                                          null);
            mv.visitCode();

            // Load all variables from the passed map into registers

            // Used to keep track of the number of local variables used
            int regCounter = 2;

            // Lookup table to translate var names to register indices
            Map<String, Integer> registers = new HashMap<>();
            for (String var : variables) {

                // Load the map passed to the function onto the stack
                mv.visitVarInsn(ALOAD, 1);
                // Put the name of the variable onto the stack, as a constant
                mv.visitLdcInsn(var);
                // Invoke the Map.get method to push the variable on stack
                mv.visitMethodInsn(INVOKEINTERFACE,
                                   "java/util/Map",
                                   "get",
                                   "(Ljava/lang/Object;)Ljava/lang/Object;",
                                   true);
                // Assert that we're dealing with a double
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                // Perform unboxing of double
                mv.visitMethodInsn(INVOKEVIRTUAL,
                                   "java/lang/Double",
                                   "doubleValue",
                                   "()D",
                                   false);
                // Store it into a register
                mv.visitVarInsn(DSTORE, regCounter);

                registers.put(var, regCounter);

                // doubles occupy two registers
                regCounter += 2;
            }

            // Walk the AST to generate the code for the actual calculation
            CodeGenerationVisitor codegen = new CodeGenerationVisitor(mv, registers);
            tree.visit(codegen);

            // Return the double
            mv.visitInsn(DRETURN);
            // Set stack parameters
            mv.visitMaxs(2*variableVisitor.getMaxStackDepth(), regCounter+1);
            mv.visitEnd();

        // Finish class and retrieve byte code
        cw.visitEnd();

        byte[] b = cw.toByteArray();

        // Debug code to save the generated class to disk for inspection
        /*try {
            FileOutputStream out = new FileOutputStream(name + ".class");
            out.write(b, 0, b.length);
            out.close();
        }
        catch (IOException e) {
        }*/

        // Load and register class
        MyClassLoader classLoader = new MyClassLoader();
        Class c = classLoader.defineClass("com.znaptag.expiler." + name, b);

        // Create an instance and return it
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

    // Simple test case
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
