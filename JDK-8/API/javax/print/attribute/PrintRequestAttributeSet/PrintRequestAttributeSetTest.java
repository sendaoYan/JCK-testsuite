/*
 * @test
 * @summary javax.print.attribute.PrintRequestAttributeSet conformance: attribute set interface.
 * @run main PrintRequestAttributeSetTest
 */

import java.lang.reflect.*;


public class PrintRequestAttributeSetTest {
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
        Class<?> c = Class.forName("javax.print.attribute.PrintRequestAttributeSet");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasIf(c, "javax.print.attribute.AttributeSet"), "extends javax.print.attribute.AttributeSet");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "addAll"), "declares addAll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
