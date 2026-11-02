/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.PacketSpace conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main PacketSpaceTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PacketSpaceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.PacketSpace", false, PacketSpaceTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.PacketSpace"), "name");
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
        check(methods.contains("allocateNextPN"), "method:allocateNextPN");
        check(methods.contains("close"), "method:close");
        check(methods.contains("confirmHandshake"), "method:confirmHandshake");
        check(methods.contains("fastRetransmit"), "method:fastRetransmit");
        check(methods.contains("getLargestPeerAckedPN"), "method:getLargestPeerAckedPN");
        check(methods.contains("getLargestProcessedPN"), "method:getLargestProcessedPN");
        check(methods.contains("getLargestSentAckedPN"), "method:getLargestSentAckedPN");
        check(methods.contains("getMinPNThreshold"), "method:getMinPNThreshold");
        check(methods.contains("getNextAckFrame"), "method:getNextAckFrame");
        check(methods.contains("getTransmitLock"), "method:getTransmitLock");
        check(methods.contains("isAcknowledged"), "method:isAcknowledged");
        check(methods.contains("isClosed"), "method:isClosed");
        check(methods.contains("packetNumberSpace"), "method:packetNumberSpace");
        check(methods.contains("packetReceived"), "method:packetReceived");
        check(methods.contains("packetSent"), "method:packetSent");
        check(methods.contains("processAckFrame"), "method:processAckFrame");
        check(methods.contains("requestSendPing"), "method:requestSendPing");
        check(methods.contains("retry"), "method:retry");
        check(methods.contains("runTransmitter"), "method:runTransmitter");
        check(methods.contains("updatePeerTransportParameters"), "method:updatePeerTransportParameters");
        check(methods.contains("versionChanged"), "method:versionChanged");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
