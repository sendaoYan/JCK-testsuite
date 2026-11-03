/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult$HandshakeStatus conformance: white-box structure + black-box behavior.
 * @run main HandshakeStatusTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class HandshakeStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngineResult$HandshakeStatus");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Class.forName("javax.net.ssl.SSLEngineResult$HandshakeStatus").getSuperclass() == Class.forName("java.lang.Enum"), "superclass java.lang.Enum");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLEngineResult.HandshakeStatus[] vals = javax.net.ssl.SSLEngineResult.HandshakeStatus.values();
        check(vals.length == 6, "six constants");
        for (String n : new String[]{"NOT_HANDSHAKING", "FINISHED", "NEED_TASK", "NEED_WRAP", "NEED_UNWRAP", "NEED_UNWRAP_AGAIN"}) {
            check(javax.net.ssl.SSLEngineResult.HandshakeStatus.valueOf(n) != null, "valueOf " + n);
        }
        check(javax.net.ssl.SSLEngineResult.HandshakeStatus.valueOf("FINISHED") == javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED, "valueOf identity");
        check(expect(() -> { javax.net.ssl.SSLEngineResult.HandshakeStatus.valueOf("NOPE"); return "ok"; }).equals("IllegalArgumentException"), "bad valueOf");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
