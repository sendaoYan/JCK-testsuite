/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.InitialPacket conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main InitialPacketTest
 */
import java.lang.reflect.*;
import java.util.*;
public class InitialPacketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.InitialPacket", false, InitialPacketTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.InitialPacket"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.quic.packets.LongHeaderPacket"), "iface:jdk.internal.net.http.quic.packets.LongHeaderPacket");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("frames"), "method:frames");
        check(methods.contains("hasLength"), "method:hasLength");
        check(methods.contains("numberSpace"), "method:numberSpace");
        check(methods.contains("packetNumber"), "method:packetNumber");
        check(methods.contains("packetType"), "method:packetType");
        check(methods.contains("prettyPrint"), "method:prettyPrint");
        check(methods.contains("token"), "method:token");
        check(methods.contains("tokenLength"), "method:tokenLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
