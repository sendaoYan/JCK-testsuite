/*
 * @test
 * @summary com.sun.corba.se.impl.orb.NormalDataCollector conformance: type shape via reflection (headless-safe).
 * @run main NormalDataCollectorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NormalDataCollectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.orb.NormalDataCollector", false, NormalDataCollectorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.orb.DataCollectorBase"), "superclass com.sun.corba.se.impl.orb.DataCollectorBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 1, "public declared method count");
        check(mn.contains("isApplet"), "method isApplet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
