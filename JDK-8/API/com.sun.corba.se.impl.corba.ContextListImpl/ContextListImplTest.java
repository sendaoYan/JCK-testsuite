/*
 * @test
 * @summary com.sun.corba.se.impl.corba.ContextListImpl conformance: type shape via reflection (headless-safe).
 * @run main ContextListImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ContextListImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.ContextListImpl", false, ContextListImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.ContextList"), "superclass org.omg.CORBA.ContextList");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("add"), "method add");
        check(mn.contains("count"), "method count");
        check(mn.contains("item"), "method item");
        check(mn.contains("remove"), "method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
