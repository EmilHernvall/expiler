# expiler

This is an experiment with java bytecode generation. It contains a handwritten
lexer and parser for simple mathematical expressions (containing addition,
subtraction, multiplication, division, exponentiation, parenthesis, functions and
variables) and a compiler which converts the expression into a java class that
can be executed.

What's that supposed to be good for, you may ask? Well, compared to other
methods of evaluating an expression, allowing it to be run and optimized as
java bytecode means that the speed can reach CPU native levels. Consider the
expression "z\*z + 8\*y + x", which contains 2 multiplications and 2 additions.
After repeating the iteration 100 million times, the average execution time of
the expression is close to 15 nanoseconds, or about 4 nanoseconds per
operation (on a Core i7-3537U). That is pretty close to the maximum
theoretical throughput.

Obviously, this is a contrived example, but it's a nice and simple introduction
to the world of JVM byte code generation.

Here's sample usage:

    // We have to provide the lexer with an InputStream or Reader. Here we use
    // StringReader to lex a fixed string.
    StringReader reader = new StringReader("z^2 + 8*y + x");

    // Break the expression into tokens and parse it
    Parser parser = new Parser(new Lexer(reader));
    ASTNode tree = parser.parse();

    // Recursively reconstruct a string representation of the expression
    System.out.println(tree.toString());

    // Create a java class which can evaluate the expression and which
    // implements the CompiledExpression interface. TestExpression is the name
    // of the class.
    Compiler compiler = new Compiler();

    // (you can use compiler.registerConstant and compiler.registerFunction here)

    CompiledExpression expr = compiler.compile("TestExpression", tree);

    // Set up variables for execution
    Map<String, Double> vars = new HashMap<>();
    vars.put("y", 5.0);
    vars.put("z", 3.0);
    vars.put("x", 1.0);

    // Call the freshly compiled code
    double res = expr.compute(vars);
    System.out.println("res: " + res);

The project relies on [asm](http://asm.ow2.org/) to construct the bytecode.

Author: Emil Hernvall <emil.hernvall@gmail.com>
