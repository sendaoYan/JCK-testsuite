/*
 * @test
 * @summary com.sun.corba.se.impl.transport.DefaultSocketFactoryImpl conformance: type shape via reflection (headless-safe).
 * @run main DefaultSocketFactoryImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultSocketFactoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.transport.DefaultSocketFactoryImpl", false, DefaultSocketFactoryImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.transport.ORBSocketFactory"), "interface com.sun.corba.se.spi.transport.ORBSocketFactory");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("createServerSocket"), "method createServerSocket");
        check(mn.contains("createSocket"), "method createSocket");
        check(mn.contains("setAcceptedSocketOptions"), "method setAcceptedSocketOptions");
        check(mn.contains("setORB"), "method setORB");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
