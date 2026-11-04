/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicStream conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicStream", false, QuicStreamTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicStream"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("hasError"), "method:hasError");
        check(methods.contains("isBidirectional"), "method:isBidirectional");
        check(methods.contains("isClientInitiated"), "method:isClientInitiated");
        check(methods.contains("isLocalInitiated"), "method:isLocalInitiated");
        check(methods.contains("isRemoteInitiated"), "method:isRemoteInitiated");
        check(methods.contains("isServerInitiated"), "method:isServerInitiated");
        check(methods.contains("mode"), "method:mode");
        check(methods.contains("state"), "method:state");
        check(methods.contains("streamId"), "method:streamId");
        check(methods.contains("type"), "method:type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
