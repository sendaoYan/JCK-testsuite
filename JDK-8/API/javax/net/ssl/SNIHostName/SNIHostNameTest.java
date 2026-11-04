/*
 * @test
 * @summary javax.net.ssl.SNIHostName conformance: white-box structure + black-box behavior.
 * @run main SNIHostNameTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SNIHostNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIHostName");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Class.forName("javax.net.ssl.SNIHostName").getSuperclass() == Class.forName("javax.net.ssl.SNIServerName"), "superclass javax.net.ssl.SNIServerName");
        check(Arrays.stream(Class.forName("javax.net.ssl.SNIHostName").getMethods()).anyMatch(m -> m.getName().equals("getAsciiName")), "method getAsciiName");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SNIHostName h = new javax.net.ssl.SNIHostName("example.com");
        check(h.getAsciiName().equals("example.com"), "ascii name");
        check(h.getType() == 0, "type host_name");
        check(new javax.net.ssl.SNIHostName("EXAMPLE.COM").getAsciiName().equals("EXAMPLE.COM"), "case preserved");
        javax.net.ssl.SNIHostName hb = new javax.net.ssl.SNIHostName("ab.com".getBytes());
        check(hb.getAsciiName().equals("ab.com"), "byte[] ctor");
        check(new javax.net.ssl.SNIHostName("ab.com").equals(new javax.net.ssl.SNIHostName("AB.com")), "equals case-insensitive");
        check(new javax.net.ssl.SNIHostName("AB.com").hashCode() == new javax.net.ssl.SNIHostName("ab.com").hashCode(), "hashCode case-insensitive");
        check(!new javax.net.ssl.SNIHostName("ab.com").equals(new javax.net.ssl.SNIHostName("xy.com")), "equals diff host");
        check(!new javax.net.ssl.SNIHostName("ab.com").equals("ab.com"), "equals non-SNI object");
        check(h.toString().contains("ab.com") || h.toString().contains("example.com"), "toString contains name");
        check(expect(() -> { new javax.net.ssl.SNIHostName(""); return "ok"; }).equals("IllegalArgumentException"), "empty host rejected");
        check(expect(() -> { new javax.net.ssl.SNIHostName("ab.com."); return "ok"; }).equals("IllegalArgumentException"), "trailing dot rejected");
        check(expect(() -> { new javax.net.ssl.SNIHostName("a b"); return "ok"; }).equals("IllegalArgumentException"), "space rejected");
        check(expect(() -> { new javax.net.ssl.SNIHostName((String) null); return "ok"; }).equals("NullPointerException"), "null NPE");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
