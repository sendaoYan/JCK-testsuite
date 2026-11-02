/*
 * @test
 * @summary com.sun.corba.se.spi.activation._RepositoryStub conformance: type shape via reflection (headless-safe).
 * @run main _RepositoryStubTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class _RepositoryStubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.activation._RepositoryStub", false, _RepositoryStubTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.portable.ObjectImpl"), "superclass org.omg.CORBA.portable.ObjectImpl");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.activation.Repository"), "interface com.sun.corba.se.spi.activation.Repository");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("_ids"), "method _ids");
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
