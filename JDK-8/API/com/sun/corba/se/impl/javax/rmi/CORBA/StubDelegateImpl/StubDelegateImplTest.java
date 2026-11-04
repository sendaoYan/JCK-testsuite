/*
 * @test
 * @summary com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl conformance: type shape via reflection (headless-safe).
 * @run main StubDelegateImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StubDelegateImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl", false, StubDelegateImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.rmi.CORBA.StubDelegate"), "interface javax.rmi.CORBA.StubDelegate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("connect"), "method connect");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getIOR"), "method getIOR");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("readObject"), "method readObject");
        check(mn.contains("toString"), "method toString");
        check(mn.contains("writeObject"), "method writeObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
