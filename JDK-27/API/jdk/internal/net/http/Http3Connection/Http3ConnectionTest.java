/*
 * @test
 * @summary jdk.internal.net.http.Http3Connection conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http:+open
 * @run main Http3ConnectionTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Http3ConnectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.Http3Connection", false, Http3ConnectionTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.Http3Connection"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
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
        check(methods.contains("dispatchingFailed"), "method:dispatchingFailed");
        check(methods.contains("getMinPushId"), "method:getMinPushId");
        check(methods.contains("onRemoteBidirectionalStream"), "method:onRemoteBidirectionalStream");
        check(methods.contains("onRemoteUnidirectionalStream"), "method:onRemoteUnidirectionalStream");
        check(methods.contains("quicConnectionTag"), "method:quicConnectionTag");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
