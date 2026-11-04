/*
 * @test
 * @summary javax.print.attribute.standard.Media conformance: abstract media class.
 * @run main MediaTest
 */

import java.lang.reflect.*;


public class MediaTest {
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
        Class<?> c = Class.forName("javax.print.attribute.standard.Media");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(c.getSuperclass().getName().equals("javax.print.attribute.EnumSyntax"), "superclass javax.print.attribute.EnumSyntax");
        check(hasIf(c, "javax.print.attribute.DocAttribute"), "implements javax.print.attribute.DocAttribute");
        check(hasIf(c, "javax.print.attribute.PrintRequestAttribute"), "implements javax.print.attribute.PrintRequestAttribute");
        check(hasIf(c, "javax.print.attribute.PrintJobAttribute"), "implements javax.print.attribute.PrintJobAttribute");
        check(hasMethod(c, "getCategory"), "declares getCategory");
        check(hasMethod(c, "getName"), "declares getName");
        check(hasMethod(c, "equals"), "declares equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
