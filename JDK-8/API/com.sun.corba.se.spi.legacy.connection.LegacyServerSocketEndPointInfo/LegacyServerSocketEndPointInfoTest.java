/*
 * @test
 * @summary com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo conformance: type shape via reflection (headless-safe).
 * @run main LegacyServerSocketEndPointInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class LegacyServerSocketEndPointInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.legacy.connection.LegacyServerSocketEndPointInfo", false, LegacyServerSocketEndPointInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("getHostName"), "method getHostName");
        check(mn.contains("getLocatorPort"), "method getLocatorPort");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("getType"), "method getType");
        check(mn.contains("setLocatorPort"), "method setLocatorPort");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
