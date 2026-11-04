/*
 * @test
 * @summary javax.net.ssl.SSLSessionContext conformance: white-box structure + black-box behavior (no handshake).
 * @run main SSLSessionContextTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSessionContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSessionContext");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        String[] names = {"getSession", "getIds", "getSessionTimeout", "setSessionTimeout",
                "getSessionCacheSize", "setSessionCacheSize"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.SSLContext ctx = javax.net.ssl.SSLContext.getDefault();
        javax.net.ssl.SSLSessionContext client = ctx.getClientSessionContext();
        javax.net.ssl.SSLSessionContext server = ctx.getServerSessionContext();
        check(client != null && server != null, "session contexts non-null");

        // timeout roundtrip
        int to = client.getSessionTimeout();
        check(to > 0, "default session timeout positive");
        client.setSessionTimeout(to + 1);
        check(client.getSessionTimeout() == to + 1, "session timeout roundtrip");
        client.setSessionTimeout(to);

        // cache size roundtrip
        int sz = client.getSessionCacheSize();
        check(sz > 0, "default cache size positive");
        client.setSessionCacheSize(sz + 1);
        check(client.getSessionCacheSize() == sz + 1, "cache size roundtrip");
        client.setSessionCacheSize(sz);

        // ids enumeration is non-null (empty before any handshake)
        check(client.getIds() != null, "getIds non-null");
        check(server.getIds() != null, "server getIds non-null");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
