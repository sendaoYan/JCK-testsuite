/*
 * @test
 * @summary com.sun.corba.se.spi.servicecontext.ServiceContexts conformance: type shape via reflection (headless-safe).
 * @run main ServiceContextsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ServiceContextsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.servicecontext.ServiceContexts", false, ServiceContextsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("addAlignmentPadding"), "method addAlignmentPadding");
        check(mn.contains("delete"), "method delete");
        check(mn.contains("get"), "method get");
        check(mn.contains("put"), "method put");
        check(mn.contains("write"), "method write");
        check(mn.contains("writeNullServiceContext"), "method writeNullServiceContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
