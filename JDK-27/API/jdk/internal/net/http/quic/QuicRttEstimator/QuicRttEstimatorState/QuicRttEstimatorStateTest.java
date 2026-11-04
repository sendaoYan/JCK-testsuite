/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicRttEstimator.QuicRttEstimatorState conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicRttEstimatorStateTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicRttEstimatorStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicRttEstimator$QuicRttEstimatorState", false, QuicRttEstimatorStateTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicRttEstimator$QuicRttEstimatorState"), "name");
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
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("latestRttMicros"), "method:latestRttMicros");
        check(methods.contains("minRttMicros"), "method:minRttMicros");
        check(methods.contains("rttSampleCount"), "method:rttSampleCount");
        check(methods.contains("rttVarMicros"), "method:rttVarMicros");
        check(methods.contains("smoothedRttMicros"), "method:smoothedRttMicros");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
