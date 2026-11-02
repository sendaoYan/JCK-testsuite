/*
 * @test
 * @summary sun.security.krb5.internal.NetClient conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main NetClientTest
 */
import java.lang.reflect.*;
import java.util.*;
public class NetClientTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.NetClient", false, NetClientTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.NetClient"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.AutoCloseable"), "iface:java.lang.AutoCloseable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("receive"), "method:receive");
        check(methods.contains("send"), "method:send");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
