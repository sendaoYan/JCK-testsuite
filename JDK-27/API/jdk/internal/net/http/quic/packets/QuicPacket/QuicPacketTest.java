/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.QuicPacket conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main QuicPacketTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.QuicPacket", false, QuicPacketTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.QuicPacket"), "name");
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
        check(methods.contains("destinationId"), "method:destinationId");
        check(methods.contains("frames"), "method:frames");
        check(methods.contains("hasLength"), "method:hasLength");
        check(methods.contains("headersType"), "method:headersType");
        check(methods.contains("isAckEliciting"), "method:isAckEliciting");
        check(methods.contains("length"), "method:length");
        check(methods.contains("numberSpace"), "method:numberSpace");
        check(methods.contains("packetNumber"), "method:packetNumber");
        check(methods.contains("packetType"), "method:packetType");
        check(methods.contains("payloadSize"), "method:payloadSize");
        check(methods.contains("prettyPrint"), "method:prettyPrint");
        check(methods.contains("size"), "method:size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
