/*
 * @test
 * @summary javax.print.attribute.Size2DSyntax conformance: abstract syntax class.
 * @run main Size2DSyntaxTest
 */

import java.lang.reflect.*;


public class Size2DSyntaxTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasIf(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] a) throws Exception {
        check(javax.print.attribute.Size2DSyntax.INCH == 25400, "INCH constant");
        check(javax.print.attribute.Size2DSyntax.MM == 1000, "MM constant");
        Class<?> c = Class.forName("javax.print.attribute.Size2DSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getSize"), "declares getSize");
        check(hasMethod(c, "getX"), "declares getX");
        check(hasMethod(c, "getY"), "declares getY");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
