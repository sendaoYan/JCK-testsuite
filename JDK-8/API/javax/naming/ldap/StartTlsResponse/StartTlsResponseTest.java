/*
 * @test
 * @summary javax.naming.ldap.StartTlsResponse conformance: abstract class hierarchy and members.
 * @run main StartTlsResponseTest
 */



import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.StartTlsResponse;
public class StartTlsResponseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.StartTlsResponse");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.ldap.ExtendedResponse");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("close"), "has method close");
        check(names.contains("getEncodedValue"), "has method getEncodedValue");
        check(names.contains("getID"), "has method getID");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dStRespSurface();
        dStRespSubclass();
        System.out.println("deep: StartTlsResponse contracts OK");
    }

    static void dStRespSurface() throws Exception {
        check("1.3.6.1.4.1.1466.20037".equals(StartTlsResponse.OID), "OID constant");
        check(java.lang.reflect.Modifier.isAbstract(StartTlsResponse.class.getModifiers()),
                "abstract class");
        java.util.Set<String> abstracts = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : StartTlsResponse.class.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers()))
                abstracts.add(m.getName());
        check(abstracts.contains("setEnabledCipherSuites"), "abstract setEnabledCipherSuites");
        check(abstracts.contains("setHostnameVerifier"), "abstract setHostnameVerifier");
        check(abstracts.contains("negotiate"), "abstract negotiate");
        check(abstracts.contains("close"), "abstract close");
        check(StartTlsResponse.class.isInterface() == false, "is a class");
    }

    /** Minimal concrete subclass proving the full abstract surface is implementable. */
    static class DeepTlsResp extends StartTlsResponse {
        int negotiated, closed;
        String[] suites;
        javax.net.ssl.HostnameVerifier verifier;
        public String getID() { return StartTlsResponse.OID; }
        public byte[] getEncodedValue() { return null; }
        public void setEnabledCipherSuites(String[] suites) { this.suites = suites; }
        public void setHostnameVerifier(javax.net.ssl.HostnameVerifier verifier) { this.verifier = verifier; }
        public javax.net.ssl.SSLSession negotiate() { negotiated++; return null; }
        public javax.net.ssl.SSLSession negotiate(javax.net.ssl.SSLSocketFactory factory) {
            negotiated++;
            return null;
        }
        public void close() { closed++; }
    }

    static void dStRespSubclass() {
        DeepTlsResp r = new DeepTlsResp();
        check(StartTlsResponse.OID.equals(r.getID()), "getID override");
        check(r.getEncodedValue() == null, "getEncodedValue override");
        r.setEnabledCipherSuites(new String[]{"TLS_A"});
        check(r.suites[0].equals("TLS_A"), "cipher suites recorded");
        r.setHostnameVerifier(null);
        check(r.verifier == null, "null verifier allowed");
        r.negotiate();
        r.negotiate((javax.net.ssl.SSLSocketFactory) null);
        check(r.negotiated == 2, "both negotiate overloads callable");
        check(r.negotiate() == null, "session object supplied by impl");
        r.close();
        check(r.closed == 1, "close callable");
    }

}
