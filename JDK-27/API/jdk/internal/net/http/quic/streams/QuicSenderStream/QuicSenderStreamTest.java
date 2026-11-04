/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicSenderStream conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicSenderStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicSenderStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicSenderStream", false, QuicSenderStreamTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicSenderStream"), "name");
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
        check(methods.contains("connectWriter"), "method:connectWriter");
        check(methods.contains("dataSent"), "method:dataSent");
        check(methods.contains("disconnectWriter"), "method:disconnectWriter");
        check(methods.contains("hasError"), "method:hasError");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("sendingState"), "method:sendingState");
        check(methods.contains("sndErrorCode"), "method:sndErrorCode");
        check(methods.contains("stopSendingReceived"), "method:stopSendingReceived");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
