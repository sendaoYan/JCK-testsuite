/*
 * @test
 * @summary jdk.internal.net.http.hpack.HPACK.Logger conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.hpack:+open
 * @run main LoggerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.hpack.HPACK$Logger", false, LoggerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.hpack.HPACK$Logger"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.System$Logger"), "iface:java.lang.System$Logger");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getName"), "method:getName");
        check(methods.contains("isLoggable"), "method:isLoggable");
        check(methods.contains("log"), "method:log");
        check(methods.contains("subLogger"), "method:subLogger");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
