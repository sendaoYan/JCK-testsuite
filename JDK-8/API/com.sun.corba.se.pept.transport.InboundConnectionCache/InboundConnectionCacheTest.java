/*
 * @test
 * @summary com.sun.corba.se.pept.transport.InboundConnectionCache conformance: type shape via reflection (headless-safe).
 * @run main InboundConnectionCacheTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class InboundConnectionCacheTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.pept.transport.InboundConnectionCache", false, InboundConnectionCacheTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.ConnectionCache"), "interface com.sun.corba.se.pept.transport.ConnectionCache");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("get"), "method get");
        check(mn.contains("getAcceptor"), "method getAcceptor");
        check(mn.contains("put"), "method put");
        check(mn.contains("remove"), "method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
