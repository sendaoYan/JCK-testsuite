/*
 * @test
 * @summary javax.print.attribute.EnumSyntax conformance: abstract enum syntax class.
 * @run main EnumSyntaxTest
 */

import java.lang.reflect.*;


public class EnumSyntaxTest {
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
        Class<?> c = Class.forName("javax.print.attribute.EnumSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getValue"), "declares getValue");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        c.getDeclaredMethod("getStringTable");
        c.getDeclaredMethod("getEnumValueTable");
        c.getDeclaredMethod("getOffset");
        c.getDeclaredMethod("readResolve");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
