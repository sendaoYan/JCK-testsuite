/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase conformance: type shape via reflection (headless-safe).
 * @run main POAPolicyMediatorBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class POAPolicyMediatorBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.POAPolicyMediatorBase", false, POAPolicyMediatorBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.oa.poa.POAPolicyMediator"), "interface com.sun.corba.se.impl.oa.poa.POAPolicyMediator");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("getInvocationServant"), "method getInvocationServant");
        check(mn.contains("getPolicies"), "method getPolicies");
        check(mn.contains("getScid"), "method getScid");
        check(mn.contains("getServerId"), "method getServerId");
        check(mn.contains("newSystemId"), "method newSystemId");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
