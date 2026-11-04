/*
 * @test
 * @summary javax.net.ssl.SNIMatcher conformance: white-box structure + black-box behavior.
 * @run main SNIMatcherTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class SNIMatcherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SNIMatcher");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Arrays.stream(Class.forName("javax.net.ssl.SNIMatcher").getMethods()).anyMatch(m -> m.getName().equals("matches")), "method matches");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SNIMatcher m = javax.net.ssl.SNIHostName.createSNIMatcher("example.com");
        check(m.getType() == 0, "matcher type host_name");
        check(m.matches(new javax.net.ssl.SNIHostName("example.com")), "matches same");
        check(!m.matches(new javax.net.ssl.SNIHostName("other.com")), "not matches other");
        javax.net.ssl.SNIMatcher neg = new javax.net.ssl.SNIMatcher(1) {
            public boolean matches(javax.net.ssl.SNIServerName n) { return true; }
        };
        check(neg.getType() == 1, "custom type");
        check(neg.matches(new javax.net.ssl.SNIHostName("x.com")), "custom matches");
        check(expect(() -> { new javax.net.ssl.SNIMatcher(-1) { public boolean matches(javax.net.ssl.SNIServerName n) { return true; } }; return "ok"; }).equals("IllegalArgumentException"), "negative type rejected");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
