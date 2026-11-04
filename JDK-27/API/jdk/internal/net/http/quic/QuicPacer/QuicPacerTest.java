/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicPacer conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicPacerTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicPacerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicPacer", false, QuicPacerTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicPacer"), "name");
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
        check(methods.contains("appLimited"), "method:appLimited");
        check(methods.contains("canSend"), "method:canSend");
        check(methods.contains("packetSent"), "method:packetSent");
        check(methods.contains("twoPacketDeadline"), "method:twoPacketDeadline");
        check(methods.contains("updateQuota"), "method:updateQuota");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
