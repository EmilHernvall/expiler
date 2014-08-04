package com.znaptag.expiler;

import java.util.Map;
import java.util.HashMap;
import java.io.StringReader;

import com.znaptag.expiler.ast.*;

public class Main
{
    public static void main(String[] args)
    throws Exception
    {
        //StringReader reader = new StringReader("z^2 + 8*y + x");
        StringReader reader = new StringReader("z*z + 8*y + x");
        //StringReader reader = new StringReader("(z*z + 8*y) / x");

        // Parse expression
        Parser parser = new Parser(new Lexer(reader));
        ASTNode tree = parser.parse();

        System.out.println(tree.toString());

        // Compile to bytecode
        Compiler compiler = new Compiler();
        CompiledExpression expr = compiler.compile("TestExpression", tree);

        // Set up variables for execution
        Map<String, Double> vars = new HashMap<>();
        vars.put("y", 5.0);
        vars.put("z", 3.0);
        vars.put("x", 1.0);

        // Execute freshly compiled code
        long s = System.nanoTime();
        double res = 0.0;
        int count = 100000000;
        for (int i = 0; i < count; i++) {
            res = expr.compute(vars);
        }
        long s2 = (System.nanoTime() - s)/count;
        System.out.println("res: " + res);
        System.out.println("time: " + s2 + "ns");
    }
}
