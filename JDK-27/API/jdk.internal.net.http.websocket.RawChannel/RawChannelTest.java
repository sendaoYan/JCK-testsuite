/*
 * @test
 * @summary jdk.internal.net.http.websocket.RawChannel conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main RawChannelTest
 */
import java.lang.reflect.*;
import java.util.*;
public class RawChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.RawChannel", false, RawChannelTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.RawChannel"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.io.Closeable"), "iface:java.io.Closeable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("close"), "method:close");
        check(methods.contains("initialByteBuffer"), "method:initialByteBuffer");
        check(methods.contains("read"), "method:read");
        check(methods.contains("registerEvent"), "method:registerEvent");
        check(methods.contains("shutdownInput"), "method:shutdownInput");
        check(methods.contains("shutdownOutput"), "method:shutdownOutput");
        check(methods.contains("write"), "method:write");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
