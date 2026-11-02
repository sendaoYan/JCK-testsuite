/*
 * @test
 * @summary com.sun.corba.se.spi.activation.ActivatorOperations conformance: type shape via reflection (headless-safe).
 * @run main ActivatorOperationsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ActivatorOperationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation.ActivatorOperations", false, ActivatorOperationsTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("activate"), "method activate");
        check(mn.contains("active"), "method active");
        check(mn.contains("getActiveServers"), "method getActiveServers");
        check(mn.contains("getORBNames"), "method getORBNames");
        check(mn.contains("install"), "method install");
        check(mn.contains("registerEndpoints"), "method registerEndpoints");
        check(mn.contains("shutdown"), "method shutdown");
        check(mn.contains("uninstall"), "method uninstall");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
