/*
 * @test
 * @summary jdk.internal.net.http.quic.QuicRttEstimator conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.quic:+open
 * @run main QuicRttEstimatorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class QuicRttEstimatorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.quic.QuicRttEstimator", false, QuicRttEstimatorTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.quic.QuicRttEstimator"), "name");
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
        check(methods.contains("consumeRttSample"), "method:consumeRttSample");
        check(methods.contains("getBasePtoDuration"), "method:getBasePtoDuration");
        check(methods.contains("getLossThreshold"), "method:getLossThreshold");
        check(methods.contains("getPtoBackoff"), "method:getPtoBackoff");
        check(methods.contains("increasePtoBackoff"), "method:increasePtoBackoff");
        check(methods.contains("isMinBackoffTimeoutExceeded"), "method:isMinBackoffTimeoutExceeded");
        check(methods.contains("resetPtoBackoff"), "method:resetPtoBackoff");
        check(methods.contains("state"), "method:state");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAX_PTO_BACKOFF"), "field:MAX_PTO_BACKOFF");
        check(fields.contains("MAX_PTO_BACKOFF_TIMEOUT"), "field:MAX_PTO_BACKOFF_TIMEOUT");
        check(fields.contains("MIN_PTO_BACKOFF_TIMEOUT"), "field:MIN_PTO_BACKOFF_TIMEOUT");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
