package com.znaptag.expiler;

import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;

import com.znaptag.expiler.ast.*;
import com.znaptag.expiler.function.*;

public class Main
{
    public static void main(String[] args)
    throws Exception
    {
        Parser parser = new Parser(new Lexer(System.in));
        ASTNode tree = parser.parse();

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

        CompiledExpression expr = compiler.compile("TestExpression", tree);

        Map<String, Double> vars = new HashMap<>();
        double res = expr.compute(vars);
        System.out.println("res: " + res);
    }
}
