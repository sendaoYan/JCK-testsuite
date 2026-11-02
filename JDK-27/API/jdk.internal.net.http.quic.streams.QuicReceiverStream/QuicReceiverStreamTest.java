/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicReceiverStream conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicReceiverStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicReceiverStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicReceiverStream", false, QuicReceiverStreamTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicReceiverStream"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.streams.QuicStream"), "iface:jdk.internal.net.http.quic.streams.QuicStream");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("connectReader"), "method:connectReader");
        check(methods.contains("dataReceived"), "method:dataReceived");
        check(methods.contains("disconnectReader"), "method:disconnectReader");
        check(methods.contains("hasError"), "method:hasError");
        check(methods.contains("isStopSendingRequested"), "method:isStopSendingRequested");
        check(methods.contains("maxStreamData"), "method:maxStreamData");
        check(methods.contains("rcvErrorCode"), "method:rcvErrorCode");
        check(methods.contains("receivingState"), "method:receivingState");
        check(methods.contains("requestStopSending"), "method:requestStopSending");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
