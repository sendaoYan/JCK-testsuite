/*
 * @test
 * @summary com.sun.corba.se.impl.util.IdentityHashtable conformance: type shape via reflection (headless-safe).
 * @run main IdentityHashtableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IdentityHashtableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.util.IdentityHashtable", false, IdentityHashtableTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.util.Dictionary"), "superclass java.util.Dictionary");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("clear"), "method clear");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("containsKey"), "method containsKey");
        check(mn.contains("elements"), "method elements");
        check(mn.contains("get"), "method get");
        check(mn.contains("isEmpty"), "method isEmpty");
        check(mn.contains("keys"), "method keys");
        check(mn.contains("put"), "method put");
        check(mn.contains("remove"), "method remove");
        check(mn.contains("size"), "method size");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
