/*
 * @test
 * @summary com.sun.corba.se.impl.orbutil.CacheTable conformance: type shape via reflection (headless-safe).
 * @run main CacheTableTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CacheTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orbutil.CacheTable", false, CacheTableTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("containsKey"), "method containsKey");
        check(mn.contains("containsOrderedVal"), "method containsOrderedVal");
        check(mn.contains("containsVal"), "method containsVal");
        check(mn.contains("done"), "method done");
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("getVal"), "method getVal");
        check(mn.contains("put"), "method put");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
