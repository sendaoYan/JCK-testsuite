/*
 * @test
 * @summary javax.naming.ldap.StartTlsResponse conformance: white-box structure + black-box behavior.
 * @run main StartTlsResponseTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.net.ssl.*;
import javax.naming.ldap.*;

public class StartTlsResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.StartTlsResponse");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(ExtendedResponse.class.isAssignableFrom(c), "implements ExtendedResponse");
        check("1.3.6.1.4.1.1466.20037".equals(StartTlsResponse.OID), "OID value");
        check(hasMethod(c, "setEnabledCipherSuites"), "setEnabledCipherSuites");
        check(hasMethod(c, "setHostnameVerifier"), "setHostnameVerifier");
        check(hasMethod(c, "negotiate"), "negotiate");
        check(hasMethod(c, "close"), "close");
    }

    static void testBlackBox() throws Exception {
        StartTlsResponse resp = new StartTlsResponse() {
            public void setEnabledCipherSuites(String[] suites) { }
            public void setHostnameVerifier(HostnameVerifier verifier) { }
            public SSLSession negotiate() { return null; }
            public SSLSession negotiate(SSLSocketFactory factory) { return null; }
            public void close() { }
        };
        check(StartTlsResponse.OID.equals(resp.getID()), "getID");
        check(resp.getEncodedValue() == null, "getEncodedValue null");
        resp.setEnabledCipherSuites(new String[]{"TLS_AES_128_GCM_SHA256"});
        resp.setHostnameVerifier((hostname, session) -> true);
        check(resp.negotiate() == null, "negotiate()");
        check(resp.negotiate((SSLSocketFactory) SSLSocketFactory.getDefault()) == null, "negotiate(factory)");
        resp.close();
        check(true, "close callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
