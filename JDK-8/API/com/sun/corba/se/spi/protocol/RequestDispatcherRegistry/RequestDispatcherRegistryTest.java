/*
 * @test
 * @summary com.sun.corba.se.spi.protocol.RequestDispatcherRegistry conformance: type shape via reflection (headless-safe).
 * @run main RequestDispatcherRegistryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RequestDispatcherRegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.protocol.RequestDispatcherRegistry", false, RequestDispatcherRegistryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 9, "public declared method count");
        check(mn.contains("getClientRequestDispatcher"), "method getClientRequestDispatcher");
        check(mn.contains("getLocalClientRequestDispatcherFactory"), "method getLocalClientRequestDispatcherFactory");
        check(mn.contains("getObjectAdapterFactories"), "method getObjectAdapterFactories");
        check(mn.contains("getObjectAdapterFactory"), "method getObjectAdapterFactory");
        check(mn.contains("getServerRequestDispatcher"), "method getServerRequestDispatcher");
        check(mn.contains("registerClientRequestDispatcher"), "method registerClientRequestDispatcher");
        check(mn.contains("registerLocalClientRequestDispatcherFactory"), "method registerLocalClientRequestDispatcherFactory");
        check(mn.contains("registerObjectAdapterFactory"), "method registerObjectAdapterFactory");
        check(mn.contains("registerServerRequestDispatcher"), "method registerServerRequestDispatcher");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
