/*
 * @test
 * @summary com.sun.corba.se.spi.ior.IOR conformance: type shape via reflection (headless-safe).
 * @run main IORTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class IORTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.ior.IOR", false, IORTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.ior.MakeImmutable"), "interface com.sun.corba.se.spi.ior.MakeImmutable");
        check(in.contains("com.sun.corba.se.spi.ior.Writeable"), "interface com.sun.corba.se.spi.ior.Writeable");
        check(in.contains("java.util.List"), "interface java.util.List");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("getIOPIOR"), "method getIOPIOR");
        check(mn.contains("getIORTemplates"), "method getIORTemplates");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("getProfile"), "method getProfile");
        check(mn.contains("getTypeId"), "method getTypeId");
        check(mn.contains("isEquivalent"), "method isEquivalent");
        check(mn.contains("isNil"), "method isNil");
        check(mn.contains("iteratorById"), "method iteratorById");
        check(mn.contains("stringify"), "method stringify");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
