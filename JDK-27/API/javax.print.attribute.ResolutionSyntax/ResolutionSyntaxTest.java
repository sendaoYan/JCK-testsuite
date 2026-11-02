/*
 * @test
 * @summary javax.print.attribute.ResolutionSyntax conformance: abstract syntax class.
 * @run main ResolutionSyntaxTest
 */

import java.lang.reflect.*;


public class ResolutionSyntaxTest {
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
        check(javax.print.attribute.ResolutionSyntax.DPI == 100, "DPI constant");
        check(javax.print.attribute.ResolutionSyntax.DPCM == 254, "DPCM constant");
        Class<?> c = Class.forName("javax.print.attribute.ResolutionSyntax");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(hasIf(c, "java.io.Serializable"), "implements java.io.Serializable");
        check(hasIf(c, "java.lang.Cloneable"), "implements java.lang.Cloneable");
        check(hasMethod(c, "getResolution"), "declares getResolution");
        check(hasMethod(c, "getCrossFeedResolution"), "declares getCrossFeedResolution");
        check(hasMethod(c, "getFeedResolution"), "declares getFeedResolution");
        check(hasMethod(c, "toString"), "declares toString");
        check(hasMethod(c, "lessThanOrEquals"), "declares lessThanOrEquals");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
