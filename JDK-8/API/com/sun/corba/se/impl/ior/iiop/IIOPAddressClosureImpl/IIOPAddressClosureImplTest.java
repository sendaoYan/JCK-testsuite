/*
 * @test
 * @summary com.sun.corba.se.impl.ior.iiop.IIOPAddressClosureImpl conformance: type shape via reflection (headless-safe).
 * @run main IIOPAddressClosureImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IIOPAddressClosureImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.ior.iiop.IIOPAddressClosureImpl", false, IIOPAddressClosureImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.ior.iiop.IIOPAddressBase"), "superclass com.sun.corba.se.impl.ior.iiop.IIOPAddressBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getHost"), "method getHost");
        check(mn.contains("getPort"), "method getPort");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("write"), "method write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
