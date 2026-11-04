/*
 * @test
 * @summary com.sun.corba.se.spi.transport.CorbaContactInfo conformance: type shape via reflection (headless-safe).
 * @run main CorbaContactInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaContactInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.transport.CorbaContactInfo", false, CorbaContactInfoTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.ContactInfo"), "interface com.sun.corba.se.pept.transport.ContactInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("getAddressingDisposition"), "method getAddressingDisposition");
        check(mn.contains("getEffectiveProfile"), "method getEffectiveProfile");
        check(mn.contains("getEffectiveTargetIOR"), "method getEffectiveTargetIOR");
        check(mn.contains("getMonitoringName"), "method getMonitoringName");
        check(mn.contains("getTargetIOR"), "method getTargetIOR");
        check(mn.contains("setAddressingDisposition"), "method setAddressingDisposition");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
