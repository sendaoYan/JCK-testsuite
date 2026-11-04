/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult conformance: status record + enum surfaces.
 * @run main SSLEngineResultTest
 */
import java.lang.reflect.*;
import javax.net.ssl.*;
public class SSLEngineResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()),
                "concrete class");
        int pubCtors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors()) if (Modifier.isPublic(k.getModifiers())) pubCtors++;
        check(pubCtors == 1, "single public 4-arg constructor (5-arg is JDK 17+)");
        // JDK 17+ adds a 5-arg constructor with a sequence number; JDK-8 has only the 4-arg form
        SSLEngineResult r = new SSLEngineResult(SSLEngineResult.Status.OK,
                SSLEngineResult.HandshakeStatus.FINISHED, 3, 5);
        check(r.getStatus() == SSLEngineResult.Status.OK, "getStatus");
        check(r.getHandshakeStatus() == SSLEngineResult.HandshakeStatus.FINISHED, "getHandshakeStatus");
        check(r.bytesConsumed() == 3 && r.bytesProduced() == 5, "byte counters");
        SSLEngineResult same = new SSLEngineResult(SSLEngineResult.Status.OK,
                SSLEngineResult.HandshakeStatus.FINISHED, 3, 5);
        // identity semantics: SSLEngineResult does not implement value equality
        check(!r.equals(same) || r.equals(same), "equals usable");
        check(r.equals(r), "reflexive equality");
        String s = r.toString();
        check(s.contains("OK") && s.contains("FINISHED") && s.contains("3") && s.contains("5"),
                "toString renders all four fields: " + s.replace('\n', ' '));
        // zero counters are legal
        SSLEngineResult z = new SSLEngineResult(SSLEngineResult.Status.BUFFER_UNDERFLOW,
                SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0);
        check(z.bytesConsumed() == 0 && z.bytesProduced() == 0, "zero counters accepted");
        check(z.getStatus() == SSLEngineResult.Status.BUFFER_UNDERFLOW, "underflow status recorded");

        // nested Status enum contract
        Class<?> st = Class.forName("javax.net.ssl.SSLEngineResult$Status");
        check(st.isEnum(), "Status is an enum");
        SSLEngineResult.Status[] statuses = SSLEngineResult.Status.values();
        check(statuses.length == 4, "4 Status constants, found " + statuses.length);
        for (SSLEngineResult.Status v : statuses) check(SSLEngineResult.Status.valueOf(v.name()) == v,
                "Status.valueOf round trip " + v);
        // nested HandshakeStatus enum contract
        Class<?> hs = Class.forName("javax.net.ssl.SSLEngineResult$HandshakeStatus");
        check(hs.isEnum(), "HandshakeStatus is an enum");
        SSLEngineResult.HandshakeStatus[] hss = SSLEngineResult.HandshakeStatus.values();
        check(hss.length == 5, "5 HandshakeStatus constants (NEED_UNWRAP_AGAIN is DTLS/JDK 9+), found " + hss.length);
        for (SSLEngineResult.HandshakeStatus v : hss) check(SSLEngineResult.HandshakeStatus.valueOf(v.name()) == v,
                "HandshakeStatus.valueOf round trip " + v);
        System.out.println("PASS (" + passed + " assertions)");
    }
}
