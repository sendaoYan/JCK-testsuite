/*
 * @test
 * @summary javax.net.ssl.SSLSession conformance: interface hierarchy and key methods.
 * @run main SSLSessionTest
 */

public class SSLSessionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSession");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getApplicationBufferSize"), "declares getApplicationBufferSize");
        check(ms.contains("getCipherSuite"), "declares getCipherSuite");
        check(ms.contains("getCreationTime"), "declares getCreationTime");
        check(ms.contains("getId"), "declares getId");
        check(ms.contains("getLastAccessedTime"), "declares getLastAccessedTime");
        check(ms.contains("getLocalCertificates"), "declares getLocalCertificates");
        check(ms.contains("getLocalPrincipal"), "declares getLocalPrincipal");
        check(ms.contains("getPacketBufferSize"), "declares getPacketBufferSize");
        check(ms.contains("getPeerCertificateChain"), "declares getPeerCertificateChain");
        check(ms.contains("getPeerCertificates"), "declares getPeerCertificates");
        check(ms.contains("getPeerHost"), "declares getPeerHost");
        check(ms.contains("getPeerPort"), "declares getPeerPort");
        check(ms.contains("getPeerPrincipal"), "declares getPeerPrincipal");
        check(ms.contains("getProtocol"), "declares getProtocol");
        check(ms.contains("getSessionContext"), "declares getSessionContext");
        check(ms.contains("getValue"), "declares getValue");
        check(ms.contains("getValueNames"), "declares getValueNames");
        check(ms.contains("invalidate"), "declares invalidate");
        check(ms.contains("isValid"), "declares isValid");
        check(ms.contains("putValue"), "declares putValue");
        check(ms.contains("removeValue"), "declares removeValue");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
