/*
 * @test
 * @summary com.sun.corba.se.spi.activation.RepositoryOperations conformance: type shape via reflection (headless-safe).
 * @run main RepositoryOperationsTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepositoryOperationsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation.RepositoryOperations", false, RepositoryOperationsTest.class.getClassLoader());
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
        check(mn.contains("getApplicationNames"), "method getApplicationNames");
        check(mn.contains("getServer"), "method getServer");
        check(mn.contains("getServerID"), "method getServerID");
        check(mn.contains("install"), "method install");
        check(mn.contains("isInstalled"), "method isInstalled");
        check(mn.contains("listRegisteredServers"), "method listRegisteredServers");
        check(mn.contains("registerServer"), "method registerServer");
        check(mn.contains("uninstall"), "method uninstall");
        check(mn.contains("unregisterServer"), "method unregisterServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
