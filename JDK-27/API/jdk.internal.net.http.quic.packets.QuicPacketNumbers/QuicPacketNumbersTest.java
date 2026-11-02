/*
 * @test
 * @summary jdk.internal.net.http.quic.packets.QuicPacketNumbers conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.packets:+open
 * @run main QuicPacketNumbersTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicPacketNumbersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.packets.QuicPacketNumbers", false, QuicPacketNumbersTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.packets.QuicPacketNumbers"), "name");
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
        check(methods.contains("computePacketNumberLength"), "method:computePacketNumberLength");
        check(methods.contains("decodePacketNumber"), "method:decodePacketNumber");
        check(methods.contains("encodePacketNumber"), "method:encodePacketNumber");
        check(methods.contains("truncatePacketNumber"), "method:truncatePacketNumber");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
