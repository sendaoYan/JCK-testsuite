/*
 * @test
 * @summary com.sun.corba.se.spi.ior.iiop.GIOPVersion conformance: type shape via reflection (headless-safe).
 * @run main GIOPVersionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class GIOPVersionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.iiop.GIOPVersion", false, GIOPVersionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 13, "public declared method count");
        check(mn.contains("chooseRequestVersion"), "method chooseRequestVersion");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getInstance"), "method getInstance");
        check(mn.contains("getMajor"), "method getMajor");
        check(mn.contains("getMinor"), "method getMinor");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("intValue"), "method intValue");
        check(mn.contains("lessThan"), "method lessThan");
        check(mn.contains("parseVersion"), "method parseVersion");
        check(mn.contains("read"), "method read");
        check(mn.contains("supportsIORIIOPProfileComponents"), "method supportsIORIIOPProfileComponents");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
