/*
 * @test
 * @summary com.sun.corba.se.spi.presentation.rmi.StubAdapter conformance: type shape via reflection (headless-safe).
 * @run main StubAdapterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StubAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.presentation.rmi.StubAdapter", false, StubAdapterTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 11, "public declared method count");
        check(mn.contains("activateServant"), "method activateServant");
        check(mn.contains("activateTie"), "method activateTie");
        check(mn.contains("connect"), "method connect");
        check(mn.contains("getDelegate"), "method getDelegate");
        check(mn.contains("getORB"), "method getORB");
        check(mn.contains("getTypeIds"), "method getTypeIds");
        check(mn.contains("isLocal"), "method isLocal");
        check(mn.contains("isStub"), "method isStub");
        check(mn.contains("isStubClass"), "method isStubClass");
        check(mn.contains("request"), "method request");
        check(mn.contains("setDelegate"), "method setDelegate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
