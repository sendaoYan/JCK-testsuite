/*
 * @test
 * @summary jdk.internal.net.http.websocket.BuilderImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.websocket:+open
 * @run main BuilderImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class BuilderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.websocket.BuilderImpl", false, BuilderImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.websocket.BuilderImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.net.http.WebSocket$Builder"), "iface:java.net.http.WebSocket$Builder");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("buildAsync"), "method:buildAsync");
        check(methods.contains("connectTimeout"), "method:connectTimeout");
        check(methods.contains("header"), "method:header");
        check(methods.contains("subprotocols"), "method:subprotocols");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
