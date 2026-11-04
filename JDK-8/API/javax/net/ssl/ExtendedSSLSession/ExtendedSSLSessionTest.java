/*
 * @test
 * @summary javax.net.ssl.ExtendedSSLSession conformance: white-box structure (JDK-8 has no exportKeyingMaterial).
 * @run main ExtendedSSLSessionTest
 */
import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
public class ExtendedSSLSessionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.ExtendedSSLSession");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(java.util.Arrays.asList(c.getInterfaces()).contains(javax.net.ssl.SSLSession.class),
                "implements javax.net.ssl.SSLSession");
        String[] names = {"getLocalSupportedSignatureAlgorithms", "getPeerSupportedSignatureAlgorithms",
                "getRequestedServerNames"};
        for (String n : names) {
            boolean found = false;
            for (Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declares method " + n);
        }
        // JDK-8 does not expose the TLS 1.3 keying-material export API (added later)
        boolean noExport = true;
        for (Method m : c.getMethods())
            if (m.getName().startsWith("exportKeyingMaterial")) noExport = false;
        check(noExport, "no exportKeyingMaterial* on JDK-8");
        // abstract methods are abstract
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getLocalSupportedSignatureAlgorithms"))
                check(Modifier.isAbstract(m.getModifiers()), "getLocalSupportedSignatureAlgorithms abstract");
        // getRequestedServerNames is a concrete default (returns an empty immutable list)
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getRequestedServerNames"))
                check(!Modifier.isAbstract(m.getModifiers()), "getRequestedServerNames concrete");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
