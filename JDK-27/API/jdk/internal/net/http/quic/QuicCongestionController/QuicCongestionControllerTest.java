/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicCongestionController conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicCongestionControllerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicCongestionControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicCongestionController", false, QuicCongestionControllerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicCongestionController"), "name");
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
        check(methods.contains("appLimited"), "method:appLimited");
        check(methods.contains("canSendPacket"), "method:canSendPacket");
        check(methods.contains("congestionWindow"), "method:congestionWindow");
        check(methods.contains("initialWindow"), "method:initialWindow");
        check(methods.contains("isCwndLimited"), "method:isCwndLimited");
        check(methods.contains("isPacerLimited"), "method:isPacerLimited");
        check(methods.contains("isSlowStart"), "method:isSlowStart");
        check(methods.contains("maxDatagramSize"), "method:maxDatagramSize");
        check(methods.contains("pacerDeadline"), "method:pacerDeadline");
        check(methods.contains("packetAcked"), "method:packetAcked");
        check(methods.contains("packetDiscarded"), "method:packetDiscarded");
        check(methods.contains("packetLost"), "method:packetLost");
        check(methods.contains("packetSent"), "method:packetSent");
        check(methods.contains("updateMaxDatagramSize"), "method:updateMaxDatagramSize");
        check(methods.contains("updatePacer"), "method:updatePacer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
