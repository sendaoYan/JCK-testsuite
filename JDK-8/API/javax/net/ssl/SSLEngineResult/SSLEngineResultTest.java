/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult conformance: white-box structure + black-box behavior.
 * @run main SSLEngineResultTest
 */
import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
public class SSLEngineResultTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        // JDK-8 exposes the 4-arg constructor only (no sequenceNumber added until JDK-9).
        boolean fourArg = false, noSequence = true;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            if (ps.length == 4 && ps[0] == javax.net.ssl.SSLEngineResult.Status.class
                    && ps[1] == javax.net.ssl.SSLEngineResult.HandshakeStatus.class
                    && ps[2] == int.class && ps[3] == int.class) fourArg = true;
            if (ps.length == 5) noSequence = false;
        }
        check(fourArg, "4-arg constructor (Status,HandshakeStatus,int,int)");
        check(noSequence, "no 5-arg (sequenceNumber) constructor on JDK-8");
        for (Method m : c.getMethods()) if (m.getName().equals("sequenceNumber")) noSequence = false;
        check(noSequence, "no sequenceNumber() method on JDK-8");
        String[] names = {"getStatus", "getHandshakeStatus", "bytesConsumed", "bytesProduced", "toString"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLEngineResult r = new javax.net.ssl.SSLEngineResult(
            javax.net.ssl.SSLEngineResult.Status.OK,
            javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP, 10, 20);
        check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.OK, "status");
        check(r.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NEED_UNWRAP, "handshake status");
        check(r.bytesConsumed() == 10 && r.bytesProduced() == 20, "bytes");
        javax.net.ssl.SSLEngineResult r2 = new javax.net.ssl.SSLEngineResult(
            javax.net.ssl.SSLEngineResult.Status.CLOSED,
            javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED, 1, 2);
        check(r2.getStatus() == javax.net.ssl.SSLEngineResult.Status.CLOSED, "closed status");
        check(r2.toString() != null && r2.toString().length() > 0, "toString non-empty");
        check(expect(() -> { new javax.net.ssl.SSLEngineResult(null, javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, 0); return "ok"; }).equals("IllegalArgumentException"), "null status IAE");
        check(expect(() -> { new javax.net.ssl.SSLEngineResult(javax.net.ssl.SSLEngineResult.Status.OK, null, 0, 0); return "ok"; }).equals("IllegalArgumentException"), "null handshake status IAE");
        check(expect(() -> { new javax.net.ssl.SSLEngineResult(javax.net.ssl.SSLEngineResult.Status.OK, javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, -1, 0); return "ok"; }).equals("IllegalArgumentException"), "negative consumed IAE");
        check(expect(() -> { new javax.net.ssl.SSLEngineResult(javax.net.ssl.SSLEngineResult.Status.OK, javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 0, -1); return "ok"; }).equals("IllegalArgumentException"), "negative produced IAE");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
