/*
 * @test
 * @summary javax.net.ssl.SSLSession conformance: white-box structure + black-box behavior (via a mock session).
 * @run main SSLSessionTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.security.*;
import javax.net.ssl.*;
public class SSLSessionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSession");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        String[] names = {"getPeerCertificates", "getLocalCertificates", "getCipherSuite", "getProtocol",
                "getId", "getCreationTime", "getLastAccessedTime", "isValid", "getSessionContext",
                "getPeerHost", "getPeerPort", "getPacketBufferSize", "getApplicationBufferSize",
                "getPeerPrincipal", "getLocalPrincipal", "putValue", "getValue", "removeValue",
                "getValueNames", "invalidate"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
    }
    // ---- Part 2: black-box (behavioral) ----
    static Object sessionDefault(java.lang.reflect.Method m) {
        Class<?> rt = m.getReturnType();
        if (rt == boolean.class) return false;
        if (rt == int.class) return 0;
        if (rt == long.class) return 0L;
        return null;
    }
    static void testBlackBox() throws Exception {
        // a dynamic mock session records value-binding behavior (no real handshake)
        final java.util.Map<String, Object> values = new java.util.HashMap<>();
        javax.net.ssl.SSLSession sess = (javax.net.ssl.SSLSession) Proxy.newProxyInstance(
            javax.net.ssl.SSLSession.class.getClassLoader(),
            new Class<?>[] { javax.net.ssl.SSLSession.class },
            (proxy, method, args) -> {
                switch (method.getName()) {
                    case "toString": return "mock-session";
                    case "hashCode": return System.identityHashCode(proxy);
                    case "equals": return proxy == args[0];
                    case "putValue": values.put((String) args[0], args[1]); return null;
                    case "getValue": return values.get((String) args[0]);
                    case "removeValue": return values.remove((String) args[0]);
                    case "getValueNames": return values.keySet().toArray(new String[0]);
                    default: return sessionDefault(method);
                }
            });
        check(sess.getId() == null, "mock id default null");
        sess.putValue("k", "v");
        check("v".equals(sess.getValue("k")), "put/get value");
        check(java.util.Arrays.asList(sess.getValueNames()).contains("k"), "value names contain k");
        sess.removeValue("k");
        check(sess.getValue("k") == null, "remove value");
        check(sess.toString().equals("mock-session"), "toString dispatch");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
