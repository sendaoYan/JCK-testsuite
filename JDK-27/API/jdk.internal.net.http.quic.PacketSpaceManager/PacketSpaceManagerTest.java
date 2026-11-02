/*
 * @test
 * @summary jdk.internal.net.http.quic.PacketSpaceManager conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main PacketSpaceManagerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class PacketSpaceManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.PacketSpaceManager", false, PacketSpaceManagerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.PacketSpaceManager"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.packets.PacketSpace"), "iface:jdk.internal.net.http.quic.packets.PacketSpace");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("allocateNextPN"), "method:allocateNextPN");
        check(methods.contains("close"), "method:close");
        check(methods.contains("computeNextDeadline"), "method:computeNextDeadline");
        check(methods.contains("confirmHandshake"), "method:confirmHandshake");
        check(methods.contains("fastRetransmit"), "method:fastRetransmit");
        check(methods.contains("getLargestPeerAckedPN"), "method:getLargestPeerAckedPN");
        check(methods.contains("getLargestProcessedPN"), "method:getLargestProcessedPN");
        check(methods.contains("getLargestSentAckedPN"), "method:getLargestSentAckedPN");
        check(methods.contains("getMaxAckDelay"), "method:getMaxAckDelay");
        check(methods.contains("getMinPNThreshold"), "method:getMinPNThreshold");
        check(methods.contains("getNextAckFrame"), "method:getNextAckFrame");
        check(methods.contains("getNextPN"), "method:getNextPN");
        check(methods.contains("getPeerMaxAckDelayMillis"), "method:getPeerMaxAckDelayMillis");
        check(methods.contains("getTransmitLock"), "method:getTransmitLock");
        check(methods.contains("isAcknowledged"), "method:isAcknowledged");
        check(methods.contains("isClosed"), "method:isClosed");
        check(methods.contains("nextScheduledDeadline"), "method:nextScheduledDeadline");
        check(methods.contains("packetNumberSpace"), "method:packetNumberSpace");
        check(methods.contains("packetReceived"), "method:packetReceived");
        check(methods.contains("packetSent"), "method:packetSent");
        check(methods.contains("pendingAcknowledgements"), "method:pendingAcknowledgements");
        check(methods.contains("pendingRetransmission"), "method:pendingRetransmission");
        check(methods.contains("processAckFrame"), "method:processAckFrame");
        check(methods.contains("requestSendPing"), "method:requestSendPing");
        check(methods.contains("retry"), "method:retry");
        check(methods.contains("runTransmitter"), "method:runTransmitter");
        check(methods.contains("triggeredForRetransmission"), "method:triggeredForRetransmission");
        check(methods.contains("updatePeerTransportParameters"), "method:updatePeerTransportParameters");
        check(methods.contains("versionChanged"), "method:versionChanged");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ACK_DELAY_EXPONENT"), "field:ACK_DELAY_EXPONENT");
        check(fields.contains("ADVERTISED_MAX_ACK_DELAY"), "field:ADVERTISED_MAX_ACK_DELAY");
        check(fields.contains("MAX_ACKRANGE_COUNT_BEFORE_PING"), "field:MAX_ACKRANGE_COUNT_BEFORE_PING");
        check(fields.contains("MAX_ACK_DELAY"), "field:MAX_ACK_DELAY");
        check(fields.contains("TIMER_DELAY"), "field:TIMER_DELAY");
        check(fields.contains("debug"), "field:debug");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
