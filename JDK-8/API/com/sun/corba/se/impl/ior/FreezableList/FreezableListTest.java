/*
 * @test
 * @summary com.sun.corba.se.impl.ior.FreezableList conformance: type shape via reflection (headless-safe).
 * @run main FreezableListTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class FreezableListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.FreezableList", false, FreezableListTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.AbstractList"), "superclass java.util.AbstractList");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("add"), "method add");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("get"), "method get");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isImmutable"), "method isImmutable");
        check(mn.contains("makeElementsImmutable"), "method makeElementsImmutable");
        check(mn.contains("makeImmutable"), "method makeImmutable");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("set"), "method set");
        check(mn.contains("size"), "method size");
        check(mn.contains("subList"), "method subList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
