/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.QuicPacketEncoder.OutgoingQuicPacket conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main OutgoingQuicPacketTest
 */
import java.lang.reflect.*;
import java.util.*;
public class OutgoingQuicPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.QuicPacketEncoder$OutgoingQuicPacket", false, OutgoingQuicPacketTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.QuicPacketEncoder$OutgoingQuicPacket"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.packets.QuicPacket"), "iface:jdk.internal.net.http.quic.packets.QuicPacket");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("destinationId"), "method:destinationId");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
