/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.AckFrame conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main AckFrameTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AckFrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.AckFrame", false, AckFrameTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.AckFrame"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("jdk.internal.net.http.quic.frames.QuicFrame"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("ackDelay"), "method:ackDelay");
        check(methods.contains("ackRangeCount"), "method:ackRangeCount");
        check(methods.contains("ackRanges"), "method:ackRanges");
        check(methods.contains("acknowledged"), "method:acknowledged");
        check(methods.contains("ecnCECount"), "method:ecnCECount");
        check(methods.contains("ect0Count"), "method:ect0Count");
        check(methods.contains("ect1Count"), "method:ect1Count");
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getTypeField"), "method:getTypeField");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isAckEliciting"), "method:isAckEliciting");
        check(methods.contains("isAcknowledging"), "method:isAcknowledging");
        check(methods.contains("isRangeAcknowledged"), "method:isRangeAcknowledged");
        check(methods.contains("largestAcknowledged"), "method:largestAcknowledged");
        check(methods.contains("largestAcknowledgedInPacket"), "method:largestAcknowledgedInPacket");
        check(methods.contains("size"), "method:size");
        check(methods.contains("smallestAcknowledged"), "method:smallestAcknowledged");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("withAckDelay"), "method:withAckDelay");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
