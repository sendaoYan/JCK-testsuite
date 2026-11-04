/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicSenderStreamImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicSenderStreamImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicSenderStreamImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicSenderStreamImpl", false, QuicSenderStreamImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicSenderStreamImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.streams.AbstractQuicStream"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.streams.QuicSenderStream"), "iface:jdk.internal.net.http.quic.streams.QuicSenderStream");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("available"), "method:available");
        check(methods.contains("connectWriter"), "method:connectWriter");
        check(methods.contains("dataAcknowledged"), "method:dataAcknowledged");
        check(methods.contains("dataSent"), "method:dataSent");
        check(methods.contains("disconnectWriter"), "method:disconnectWriter");
        check(methods.contains("isBlocked"), "method:isBlocked");
        check(methods.contains("isDone"), "method:isDone");
        check(methods.contains("poll"), "method:poll");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("resetAcknowledged"), "method:resetAcknowledged");
        check(methods.contains("resetSent"), "method:resetSent");
        check(methods.contains("sendingState"), "method:sendingState");
        check(methods.contains("setMaxStreamData"), "method:setMaxStreamData");
        check(methods.contains("sndErrorCode"), "method:sndErrorCode");
        check(methods.contains("state"), "method:state");
        check(methods.contains("stopSendingReceived"), "method:stopSendingReceived");
        check(methods.contains("streamSize"), "method:streamSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
