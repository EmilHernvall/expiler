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
        StringReader reader = new StringReader("z^2 + 8*y + x");

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
        double res = expr.compute(vars);
        System.out.println("res: " + res);
    }
}
