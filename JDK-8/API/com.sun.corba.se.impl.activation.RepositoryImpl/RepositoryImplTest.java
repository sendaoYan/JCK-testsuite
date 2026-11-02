/*
 * @test
 * @summary com.sun.corba.se.impl.activation.RepositoryImpl conformance: type shape via reflection (headless-safe).
 * @run main RepositoryImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class RepositoryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.activation.RepositoryImpl", false, RepositoryImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.activation._RepositoryImplBase"), "superclass com.sun.corba.se.spi.activation._RepositoryImplBase");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "interface java.io.Serializable");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("getApplicationNames"), "method getApplicationNames");
        check(mn.contains("getServer"), "method getServer");
        check(mn.contains("getServerID"), "method getServerID");
        check(mn.contains("install"), "method install");
        check(mn.contains("isInstalled"), "method isInstalled");
        check(mn.contains("listRegisteredServers"), "method listRegisteredServers");
        check(mn.contains("main"), "method main");
        check(mn.contains("registerServer"), "method registerServer");
        check(mn.contains("uninstall"), "method uninstall");
        check(mn.contains("unregisterServer"), "method unregisterServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
