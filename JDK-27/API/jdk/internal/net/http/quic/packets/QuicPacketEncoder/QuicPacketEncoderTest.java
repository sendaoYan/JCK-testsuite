/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.QuicPacketEncoder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main QuicPacketEncoderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicPacketEncoderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.QuicPacketEncoder", false, QuicPacketEncoderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.QuicPacketEncoder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("computeMaxHandshakePayloadSize"), "method:computeMaxHandshakePayloadSize");
        check(methods.contains("computeMaxInitialPayloadSize"), "method:computeMaxInitialPayloadSize");
        check(methods.contains("computeMaxOneRTTPayloadSize"), "method:computeMaxOneRTTPayloadSize");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("newHandshakePacket"), "method:newHandshakePacket");
        check(methods.contains("newInitialPacket"), "method:newInitialPacket");
        check(methods.contains("newOneRttPacket"), "method:newOneRttPacket");
        check(methods.contains("newOutgoingPacket"), "method:newOutgoingPacket");
        check(methods.contains("newRetryPacket"), "method:newRetryPacket");
        check(methods.contains("newVersionNegotiationPacket"), "method:newVersionNegotiationPacket");
        check(methods.contains("newZeroRttPacket"), "method:newZeroRttPacket");
        check(methods.contains("of"), "method:of");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
