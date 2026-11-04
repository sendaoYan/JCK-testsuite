/*
 * @test
 * @summary javax.net.ssl.HostnameVerifier conformance: white-box structure + black-box behavior.
 * @run main HostnameVerifierTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class HostnameVerifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.HostnameVerifier");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.HostnameVerifier v = (h, s) -> h.equals("good");
        check(v.verify("good", null), "verify true");
        check(!v.verify("bad", null), "verify false");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
