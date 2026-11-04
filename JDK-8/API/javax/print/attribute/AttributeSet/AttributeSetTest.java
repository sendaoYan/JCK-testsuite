/*
 * @test
 * @summary javax.print.attribute.AttributeSet conformance: interface.
 * @run main AttributeSetTest
 */

import java.lang.reflect.*;


public class AttributeSetTest {
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
        Class<?> c = Class.forName("javax.print.attribute.AttributeSet");
        check(c.isInterface(), "isInterface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "get"), "declares get");
        check(hasMethod(c, "add"), "declares add");
        check(hasMethod(c, "remove"), "declares remove");
        check(hasMethod(c, "containsKey"), "declares containsKey");
        check(hasMethod(c, "containsValue"), "declares containsValue");
        check(hasMethod(c, "addAll"), "declares addAll");
        check(hasMethod(c, "size"), "declares size");
        check(hasMethod(c, "toArray"), "declares toArray");
        check(hasMethod(c, "clear"), "declares clear");
        check(hasMethod(c, "isEmpty"), "declares isEmpty");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
