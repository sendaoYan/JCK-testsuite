/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicConnectionImpl.ProtectionRecord conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main ProtectionRecordTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ProtectionRecordTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicConnectionImpl$ProtectionRecord", false, ProtectionRecordTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicConnectionImpl$ProtectionRecord"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("datagram"), "method:datagram");
        check(methods.contains("encrypt"), "method:encrypt");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("firstPacketOffset"), "method:firstPacketOffset");
        check(methods.contains("flags"), "method:flags");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("last"), "method:last");
        check(methods.contains("more"), "method:more");
        check(methods.contains("packet"), "method:packet");
        check(methods.contains("packetOffset"), "method:packetOffset");
        check(methods.contains("retransmittedPacketNumber"), "method:retransmittedPacketNumber");
        check(methods.contains("retransmitting"), "method:retransmitting");
        check(methods.contains("single"), "method:single");
        check(methods.contains("toString"), "method:toString");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("COALESCED"), "field:COALESCED");
        check(fields.contains("LAST_PACKET"), "field:LAST_PACKET");
        check(fields.contains("SINGLE_PACKET"), "field:SINGLE_PACKET");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
