/*
 * @test
 * @summary jdk.internal.net.http.quic.frames.AckFrame.AckFrameBuilder conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic.frames:+open
 * @run main AckFrameBuilderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AckFrameBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.frames.AckFrame$AckFrameBuilder", false, AckFrameBuilderTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.frames.AckFrame$AckFrameBuilder"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("ackDelay"), "method:ackDelay");
        check(methods.contains("addAck"), "method:addAck");
        check(methods.contains("build"), "method:build");
        check(methods.contains("dropAckRangesAfter"), "method:dropAckRangesAfter");
        check(methods.contains("dropAcksBefore"), "method:dropAcksBefore");
        check(methods.contains("ecnCECount"), "method:ecnCECount");
        check(methods.contains("ect0Count"), "method:ect0Count");
        check(methods.contains("ect1Count"), "method:ect1Count");
        check(methods.contains("getLargestAckAcked"), "method:getLargestAckAcked");
        check(methods.contains("isAcknowledging"), "method:isAcknowledging");
        check(methods.contains("isEmpty"), "method:isEmpty");
        check(methods.contains("length"), "method:length");
        check(methods.contains("ofNullable"), "method:ofNullable");
        check(methods.contains("smallestAcknowledged"), "method:smallestAcknowledged");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
