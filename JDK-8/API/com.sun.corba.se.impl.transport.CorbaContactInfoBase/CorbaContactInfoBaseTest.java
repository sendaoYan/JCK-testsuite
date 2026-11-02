/*
 * @test
 * @summary com.sun.corba.se.impl.transport.CorbaContactInfoBase conformance: type shape via reflection (headless-safe).
 * @run main CorbaContactInfoBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaContactInfoBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.CorbaContactInfoBase", false, CorbaContactInfoBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.transport.CorbaContactInfo"), "interface com.sun.corba.se.spi.transport.CorbaContactInfo");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 15, "public declared method count");
        check(mn.contains("createInputObject"), "method createInputObject");
        check(mn.contains("createMessageMediator"), "method createMessageMediator");
        check(mn.contains("createOutputObject"), "method createOutputObject");
        check(mn.contains("finishCreatingMessageMediator"), "method finishCreatingMessageMediator");
        check(mn.contains("getAddressingDisposition"), "method getAddressingDisposition");
        check(mn.contains("getBroker"), "method getBroker");
        check(mn.contains("getClientRequestDispatcher"), "method getClientRequestDispatcher");
        check(mn.contains("getConnectionCache"), "method getConnectionCache");
        check(mn.contains("getContactInfoList"), "method getContactInfoList");
        check(mn.contains("getEffectiveProfile"), "method getEffectiveProfile");
        check(mn.contains("getEffectiveTargetIOR"), "method getEffectiveTargetIOR");
        check(mn.contains("getTargetIOR"), "method getTargetIOR");
        check(mn.contains("setAddressingDisposition"), "method setAddressingDisposition");
        check(mn.contains("setConnectionCache"), "method setConnectionCache");
        check(mn.contains("toString"), "method toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
