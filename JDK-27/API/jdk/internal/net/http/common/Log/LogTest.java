/*
 * @test
 * @summary jdk.internal.net.http.common.Log conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main LogTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LogTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.Log", false, LogTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.Log"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
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
        check(methods.contains("altsvc"), "method:altsvc");
        check(methods.contains("channel"), "method:channel");
        check(methods.contains("dumpHeaders"), "method:dumpHeaders");
        check(methods.contains("errors"), "method:errors");
        check(methods.contains("frames"), "method:frames");
        check(methods.contains("headers"), "method:headers");
        check(methods.contains("http3"), "method:http3");
        check(methods.contains("logAltSvc"), "method:logAltSvc");
        check(methods.contains("logChannel"), "method:logChannel");
        check(methods.contains("logError"), "method:logError");
        check(methods.contains("logFrames"), "method:logFrames");
        check(methods.contains("logHeaders"), "method:logHeaders");
        check(methods.contains("logHttp3"), "method:logHttp3");
        check(methods.contains("logParams"), "method:logParams");
        check(methods.contains("logQuic"), "method:logQuic");
        check(methods.contains("logQuicPacketIn"), "method:logQuicPacketIn");
        check(methods.contains("logQuicPacketOut"), "method:logQuicPacketOut");
        check(methods.contains("logRequest"), "method:logRequest");
        check(methods.contains("logResponse"), "method:logResponse");
        check(methods.contains("logSSL"), "method:logSSL");
        check(methods.contains("logTrace"), "method:logTrace");
        check(methods.contains("loggingFrame"), "method:loggingFrame");
        check(methods.contains("quic"), "method:quic");
        check(methods.contains("quicCC"), "method:quicCC");
        check(methods.contains("quicControl"), "method:quicControl");
        check(methods.contains("quicCrypto"), "method:quicCrypto");
        check(methods.contains("quicDBB"), "method:quicDBB");
        check(methods.contains("quicData"), "method:quicData");
        check(methods.contains("quicHandshake"), "method:quicHandshake");
        check(methods.contains("quicPacketInLoggable"), "method:quicPacketInLoggable");
        check(methods.contains("quicPacketOutLoggable"), "method:quicPacketOutLoggable");
        check(methods.contains("quicProcessed"), "method:quicProcessed");
        check(methods.contains("quicRetransmit"), "method:quicRetransmit");
        check(methods.contains("quicTimer"), "method:quicTimer");
        check(methods.contains("requests"), "method:requests");
        check(methods.contains("ssl"), "method:ssl");
        check(methods.contains("trace"), "method:trace");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ALL"), "field:ALL");
        check(fields.contains("CHANNEL"), "field:CHANNEL");
        check(fields.contains("CONTENT"), "field:CONTENT");
        check(fields.contains("CONTROL"), "field:CONTROL");
        check(fields.contains("DATA"), "field:DATA");
        check(fields.contains("ERRORS"), "field:ERRORS");
        check(fields.contains("FRAMES"), "field:FRAMES");
        check(fields.contains("HEADERS"), "field:HEADERS");
        check(fields.contains("HTTP3"), "field:HTTP3");
        check(fields.contains("OFF"), "field:OFF");
        check(fields.contains("QUIC"), "field:QUIC");
        check(fields.contains("QUIC_ACK"), "field:QUIC_ACK");
        check(fields.contains("QUIC_ALL"), "field:QUIC_ALL");
        check(fields.contains("QUIC_CC"), "field:QUIC_CC");
        check(fields.contains("QUIC_CONTROL"), "field:QUIC_CONTROL");
        check(fields.contains("QUIC_CRYPTO"), "field:QUIC_CRYPTO");
        check(fields.contains("QUIC_DATA"), "field:QUIC_DATA");
        check(fields.contains("QUIC_DIRECT_BUFFER_POOL"), "field:QUIC_DIRECT_BUFFER_POOL");
        check(fields.contains("QUIC_HANDSHAKE"), "field:QUIC_HANDSHAKE");
        check(fields.contains("QUIC_PING"), "field:QUIC_PING");
        check(fields.contains("QUIC_PROCESSED"), "field:QUIC_PROCESSED");
        check(fields.contains("QUIC_RETRANSMIT"), "field:QUIC_RETRANSMIT");
        check(fields.contains("QUIC_TIMER"), "field:QUIC_TIMER");
        check(fields.contains("REQUESTS"), "field:REQUESTS");
        check(fields.contains("SSL"), "field:SSL");
        check(fields.contains("TRACE"), "field:TRACE");
        check(fields.contains("WINDOW_UPDATES"), "field:WINDOW_UPDATES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
