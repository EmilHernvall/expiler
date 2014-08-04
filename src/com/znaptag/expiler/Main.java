package com.znaptag.expiler;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.*;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;

public class Main
{
    public static class MyClassLoader extends ClassLoader
    {
        public Class defineClass(String name, byte[] b)
        {
            return defineClass(name, b, 0, b.length);
        }
    }

    public interface TestInterface
    {
        public void testMethod(int i, int j);
    }

    public static double testFunc(Map<String, Double> foo)
    {
        return 10.0;
    }

    public static void main(String[] args)
    throws Exception
    {
        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_5,
                 ACC_PUBLIC,
                 "com/znaptag/expiler/TestClass",
                 null,
                 "java/lang/Object",
                 new String[] { "com/znaptag/expiler/Main$TestInterface" });

        cw.visitField(ACC_PRIVATE,
                      "multiplier",
                      "I",
                      null,
                      new Integer(0)).visitEnd();

        MethodVisitor mv1 = cw.visitMethod(ACC_PUBLIC,
                                           "<init>",
                                           "(I)V",
                                           null,
                                           null);
            mv1.visitVarInsn(ALOAD, 0);
            mv1.visitMethodInsn(INVOKESPECIAL,
                                "java/lang/Object",
                                "<init>",
                                "()V",
                                false);
            mv1.visitVarInsn(ALOAD, 0);
            mv1.visitVarInsn(ILOAD, 1);
            mv1.visitFieldInsn(PUTFIELD,
                               "com/znaptag/expiler/TestClass",
                               "multiplier",
                               "I");
            mv1.visitInsn(RETURN);
            mv1.visitMaxs(3, 3);
            mv1.visitEnd();

        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC,
                                          "testMethod",
                                          "(II)V",
                                          null,
                                          null);
            mv.visitCode();
            mv.visitFieldInsn(GETSTATIC,
                              "java/lang/System",
                              "out",
                              "Ljava/io/PrintStream;");
            mv.visitVarInsn(ILOAD, 1);
            mv.visitVarInsn(ILOAD, 2);
            mv.visitInsn(IADD);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD,
                              "com/znaptag/expiler/TestClass",
                              "multiplier",
                              "I");
            mv.visitInsn(IMUL);
            mv.visitLdcInsn(10);
            mv.visitInsn(IADD);
            mv.visitMethodInsn(INVOKEVIRTUAL,
                               "java/io/PrintStream",
                               "println",
                               "(I)V",
                               false);
            mv.visitInsn(RETURN);
            mv.visitMaxs(10, 10);
            mv.visitEnd();

        cw.visitEnd();

        byte[] b = cw.toByteArray();

        MyClassLoader classLoader = new MyClassLoader();

        Class c = classLoader.defineClass("com.znaptag.expiler.TestClass", b);
        Constructor<?> constructor = c.getConstructors()[0];

        TestInterface obj = (TestInterface)constructor.newInstance(3);
        obj.testMethod(10, 20);

        Map<String, Double> foo = new HashMap<String, Double>();
        double d = foo.get("bar");
    }
}
