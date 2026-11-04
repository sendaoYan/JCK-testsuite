/*
 * @test
 * @summary com.sun.corba.se.impl.javax.rmi.PortableRemoteObject conformance: type shape via reflection (headless-safe).
 * @run main PortableRemoteObjectTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PortableRemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.javax.rmi.PortableRemoteObject", false, PortableRemoteObjectTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.rmi.CORBA.PortableRemoteObjectDelegate"), "interface javax.rmi.CORBA.PortableRemoteObjectDelegate");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("connect"), "method connect");
        check(mn.contains("exportObject"), "method exportObject");
        check(mn.contains("narrow"), "method narrow");
        check(mn.contains("toStub"), "method toStub");
        check(mn.contains("unexportObject"), "method unexportObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
