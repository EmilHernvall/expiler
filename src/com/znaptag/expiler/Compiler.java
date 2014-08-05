package com.znaptag.expiler;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.lang.reflect.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

import com.znaptag.expiler.ast.*;
import com.znaptag.expiler.function.*;

public class Compiler
{
    // Visitor to do an initial pass over the AST and collect information about
    // which variables are used, and the maximum stack depth
    private static class FirstPassVisitor extends AbstractVisitor
    {
        private Set<String> variables;
        private Set<String> functions;
        private int maxStackDepth = 2;
        private int currentStackDepth = 0;

        public FirstPassVisitor()
        {
            variables = new HashSet<String>();
            functions = new HashSet<String>();
        }

        public int getMaxStackDepth()
        {
            return maxStackDepth;
        }

        public Set<String> getVariables()
        {
            return variables;
        }

        public Set<String> getFunctions()
        {
            return functions;
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
        public void visit(FunctionNode node)
        {
            super.visit(node);
            functions.add(node.getName());
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
        private Map<String, Class<? extends Function>> functions;
        private Map<String, Double> constants;

        public CodeGenerationVisitor(MethodVisitor mv,
                                     Map<String, Integer> registers,
                                     Map<String, Class<? extends Function>> functions,
                                     Map<String, Double> constants)
        {
            this.mv = mv;
            this.registers = registers;
            this.functions = functions;
            this.constants = constants;
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
            if (constants.containsKey(node.getName())) {
                // constants are translated into bytecode constants
                double constant = constants.get(node.getName());
                mv.visitLdcInsn(constant);
            } else {
                // Variables have already been loaded into local registers
                int reg = registers.get(node.getName());
                mv.visitVarInsn(DLOAD, reg);
            }
        }

        @Override
        public void visit(FunctionNode node)
        {
            super.visit(node);
            Class<? extends Function> func = functions.get(node.getName());
            String name = func.getCanonicalName().replaceAll("\\.", "/");

            mv.visitMethodInsn(INVOKESTATIC,
                               name,
                               "call",
                               "(D)D",
                               false);
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

    public static class CompilationException extends Exception
    {
        public CompilationException(String message)
        {
            super(message);
        }
    }

    private Map<String, Class<? extends Function>> functions;
    private Map<String, Double> constants;

    public Compiler()
    {
        functions = new HashMap<>();
        constants = new HashMap<>();
    }

    public void registerConstant(String name, double value)
    {
        constants.put(name, value);
    }

    public void registerFunction(String name, Class<? extends Function> func)
    {
        Method[] methods = func.getMethods();
        Method callMethod = null;
        for (Method method : methods) {
            if ("call".equals(method.getName())) {
                callMethod = method;
                break;
            }
        }

        if (callMethod == null) {
            throw new IllegalArgumentException("No call method found for function " +
                                               name);
        }

        if ((callMethod.getModifiers() & Modifier.STATIC) == 0) {
            throw new IllegalArgumentException("Call method must be static for " +
                                               "function " + name);
        }
        if ((callMethod.getModifiers() & Modifier.PUBLIC) == 0) {
            throw new IllegalArgumentException("Call method must be public for " +
                                               "function " + name);
        }

        functions.put(name, func);
    }

    public byte[] compileToBytecode(String name, ASTNode tree)
    throws CompilationException
    {
        // Find all variables used by the expression, as well as the max stack
        // depth
        FirstPassVisitor firstPass = new FirstPassVisitor();
        tree.visit(firstPass);

        Set<String> foundFunctions = firstPass.getFunctions();
        for (String functionName : foundFunctions) {
            if (!functions.containsKey(functionName)) {
                throw new CompilationException("Function " + functionName +
                                               " is not known.");
            }
        }

        Set<String> variables = firstPass.getVariables();

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
                if (constants.containsKey(var)) {
                    continue;
                }

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
            CodeGenerationVisitor codegen =
                new CodeGenerationVisitor(mv, registers, functions, constants);
            tree.visit(codegen);

            // Return the double
            mv.visitInsn(DRETURN);
            // Set stack parameters
            mv.visitMaxs(2*firstPass.getMaxStackDepth(), regCounter+1);
            mv.visitEnd();

        // Finish class and retrieve byte code
        cw.visitEnd();

        byte[] b = cw.toByteArray();

        return b;
    }

    // Compile and save generated class to disk
    public void compileAndSave(String name, ASTNode tree)
    throws IOException, CompilationException
    {
        byte[] bytecode = compileToBytecode(name, tree);

        FileOutputStream out = new FileOutputStream(name + ".class");
        out.write(bytecode, 0, bytecode.length);
        out.close();
    }

    // Compile and load new class directly into memory
    public CompiledExpression compile(String name, ASTNode tree)
    throws CompilationException
    {
        byte[] bytecode = compileToBytecode(name, tree);

        // Load and register class
        MyClassLoader classLoader = new MyClassLoader();
        Class c = classLoader.defineClass("com.znaptag.expiler." + name, bytecode);

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
        //StringReader reader = new StringReader("z^2 + 8*y + x");
        //StringReader reader = new StringReader("z*z + 8*y / x");
        StringReader reader = new StringReader("(z*z + 8*y) / x");

        // Parse expression
        Parser parser = new Parser(new Lexer(reader));
        ASTNode tree = parser.parse();

        System.out.println(tree.toString());

        // Setup the compiler and register all constants and functions
        Compiler compiler = new Compiler();

        compiler.registerConstant("PI", Math.PI);
        compiler.registerConstant("E", Math.E);

        compiler.registerFunction("sqrt", SqrtFunction.class);
        compiler.registerFunction("sin", SinFunction.class);
        compiler.registerFunction("cos", CosFunction.class);
        compiler.registerFunction("tan", TanFunction.class);
        compiler.registerFunction("asin", ArcSinFunction.class);
        compiler.registerFunction("acos", ArcCosFunction.class);
        compiler.registerFunction("atan", ArcTanFunction.class);
        compiler.registerFunction("exp", ExpFunction.class);
        compiler.registerFunction("log", LogFunction.class);
        compiler.registerFunction("ln", LogFunction.class);

        // Compile to bytecode
        CompiledExpression expr = compiler.compile("TestExpression", tree);

        // Set up variables for execution
        Map<String, Double> vars = new HashMap<>();
        vars.put("y", 5.0);
        vars.put("z", 3.0);
        vars.put("x", 2.0);

        // Execute freshly compiled code
        long s = System.nanoTime();
        double res = 0.0, res2 = 0.0;
        int count = 100000000;
        for (int i = 0; i < count; i++) {
            res = expr.compute(vars);
            res2 += res;
        }
        long s2 = (System.nanoTime() - s)/count;
        System.out.println("res: " + res);
        System.out.println("res2: " + res2);
        System.out.println("time: " + s2 + "ns");
    }
}
