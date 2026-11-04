/*
 * @test
 * @summary jdk.internal.net.http.quic.streams.QuicBidiStreamImpl conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.streams:+open
 * @run main QuicBidiStreamImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicBidiStreamImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.streams.QuicBidiStreamImpl", false, QuicBidiStreamImplTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.streams.QuicBidiStreamImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.streams.AbstractQuicStream"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.streams.QuicBidiStream"), "iface:jdk.internal.net.http.quic.streams.QuicBidiStream");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("connectReader"), "method:connectReader");
        check(methods.contains("connectWriter"), "method:connectWriter");
        check(methods.contains("dataReceived"), "method:dataReceived");
        check(methods.contains("dataSent"), "method:dataSent");
        check(methods.contains("disconnectReader"), "method:disconnectReader");
        check(methods.contains("disconnectWriter"), "method:disconnectWriter");
        check(methods.contains("isDone"), "method:isDone");
        check(methods.contains("isStopSendingRequested"), "method:isStopSendingRequested");
        check(methods.contains("maxStreamData"), "method:maxStreamData");
        check(methods.contains("rcvErrorCode"), "method:rcvErrorCode");
        check(methods.contains("receiverPart"), "method:receiverPart");
        check(methods.contains("receivingState"), "method:receivingState");
        check(methods.contains("requestStopSending"), "method:requestStopSending");
        check(methods.contains("reset"), "method:reset");
        check(methods.contains("senderPart"), "method:senderPart");
        check(methods.contains("sendingState"), "method:sendingState");
        check(methods.contains("sndErrorCode"), "method:sndErrorCode");
        check(methods.contains("stopSendingReceived"), "method:stopSendingReceived");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
