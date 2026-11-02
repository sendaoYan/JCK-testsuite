/*
 * @test
 * @summary com.sun.corba.se.impl.naming.namingutil.IIOPEndpointInfo conformance: type shape via reflection (headless-safe).
 * @run main IIOPEndpointInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IIOPEndpointInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.namingutil.IIOPEndpointInfo", false, IIOPEndpointInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("dump"), "method dump");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getMajor"), "method getMajor");
        check(mn.contains("getMinor"), "method getMinor");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("setHost"), "method setHost");
        check(mn.contains("setPort"), "method setPort");
        check(mn.contains("setVersion"), "method setVersion");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
