/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.HandshakeStatus conformance: enum surface.
 * @run main HandshakeStatusTest
 */
import java.lang.reflect.*;
import javax.net.ssl.*;
public class HandshakeStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult$HandshakeStatus");
        check(c.isEnum(), "is enum");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isFinal(c.getModifiers()), "public final");
        check(c.getSuperclass() == java.lang.Enum.class, "extends Enum");
        SSLEngineResult.HandshakeStatus[] vs = SSLEngineResult.HandshakeStatus.values();
        check(vs.length == 5, "5 constants, found " + vs.length);
        java.util.Set<String> names = new java.util.TreeSet<>();
        for (SSLEngineResult.HandshakeStatus v : vs) names.add(v.name());
        check(names.containsAll(java.util.Arrays.asList("NOT_HANDSHAKING", "FINISHED", "NEED_TASK", "NEED_WRAP", "NEED_UNWRAP")),
                "core 5 states present: " + names);
        check(!names.contains("NEED_UNWRAP_AGAIN"), "NEED_UNWRAP_AGAIN absent (DTLS 1.3, JDK 9+)");
        for (SSLEngineResult.HandshakeStatus v : vs) {
            check(SSLEngineResult.HandshakeStatus.valueOf(v.name()) == v, "valueOf round trip " + v);
            check(v.compareTo(v) == 0 && v.ordinal() >= 0, "ordinal/compareTo sane " + v);
            check(v.name().equals(v.toString()), "name == toString for " + v);
        }
        Class<?> enclosing = c.getEnclosingClass();
        check(enclosing != null && enclosing.getName().equals("javax.net.ssl.SSLEngineResult"),
                "nested in SSLEngineResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
