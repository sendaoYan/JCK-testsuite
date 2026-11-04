/*
 * @test
 * @summary javax.net.ssl.SSLSocket conformance: abstract class hierarchy and key methods.
 * @run main SSLSocketTest
 */

public class SSLSocketTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSocket");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.net.Socket"), "extends java.net.Socket");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("addHandshakeCompletedListener"), "declares addHandshakeCompletedListener");
        check(ms.contains("getApplicationProtocol"), "declares getApplicationProtocol");
        check(ms.contains("getEnableSessionCreation"), "declares getEnableSessionCreation");
        check(ms.contains("getEnabledCipherSuites"), "declares getEnabledCipherSuites");
        check(ms.contains("getEnabledProtocols"), "declares getEnabledProtocols");
        check(ms.contains("getHandshakeApplicationProtocol"), "declares getHandshakeApplicationProtocol");
        check(ms.contains("getHandshakeSession"), "declares getHandshakeSession");
        check(ms.contains("getNeedClientAuth"), "declares getNeedClientAuth");
        check(ms.contains("getSSLParameters"), "declares getSSLParameters");
        check(ms.contains("getSession"), "declares getSession");
        check(ms.contains("getSupportedCipherSuites"), "declares getSupportedCipherSuites");
        check(ms.contains("getSupportedProtocols"), "declares getSupportedProtocols");
        check(ms.contains("getUseClientMode"), "declares getUseClientMode");
        check(ms.contains("getWantClientAuth"), "declares getWantClientAuth");
        check(ms.contains("removeHandshakeCompletedListener"), "declares removeHandshakeCompletedListener");
        check(ms.contains("setEnableSessionCreation"), "declares setEnableSessionCreation");
        check(ms.contains("setEnabledCipherSuites"), "declares setEnabledCipherSuites");
        check(ms.contains("setEnabledProtocols"), "declares setEnabledProtocols");
        check(ms.contains("setHandshakeApplicationProtocolSelector"), "declares setHandshakeApplicationProtocolSelector");
        check(ms.contains("setNeedClientAuth"), "declares setNeedClientAuth");
        check(ms.contains("setSSLParameters"), "declares setSSLParameters");
        check(ms.contains("setUseClientMode"), "declares setUseClientMode");
        check(ms.contains("setWantClientAuth"), "declares setWantClientAuth");
        check(ms.contains("startHandshake"), "declares startHandshake");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
