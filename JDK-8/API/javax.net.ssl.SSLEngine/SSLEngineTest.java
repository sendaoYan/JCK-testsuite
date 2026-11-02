/*
 * @test
 * @summary javax.net.ssl.SSLEngine conformance: abstract class hierarchy and key methods.
 * @run main SSLEngineTest
 */

public class SSLEngineTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLEngine");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("beginHandshake"), "declares beginHandshake");
        check(ms.contains("closeInbound"), "declares closeInbound");
        check(ms.contains("closeOutbound"), "declares closeOutbound");
        check(ms.contains("getApplicationProtocol"), "declares getApplicationProtocol");
        check(ms.contains("getDelegatedTask"), "declares getDelegatedTask");
        check(ms.contains("getEnableSessionCreation"), "declares getEnableSessionCreation");
        check(ms.contains("getEnabledCipherSuites"), "declares getEnabledCipherSuites");
        check(ms.contains("getEnabledProtocols"), "declares getEnabledProtocols");
        check(ms.contains("getHandshakeApplicationProtocol"), "declares getHandshakeApplicationProtocol");
        check(ms.contains("getHandshakeSession"), "declares getHandshakeSession");
        check(ms.contains("getHandshakeStatus"), "declares getHandshakeStatus");
        check(ms.contains("getNeedClientAuth"), "declares getNeedClientAuth");
        check(ms.contains("getPeerHost"), "declares getPeerHost");
        check(ms.contains("getPeerPort"), "declares getPeerPort");
        check(ms.contains("getSSLParameters"), "declares getSSLParameters");
        check(ms.contains("getSession"), "declares getSession");
        check(ms.contains("getSupportedCipherSuites"), "declares getSupportedCipherSuites");
        check(ms.contains("getSupportedProtocols"), "declares getSupportedProtocols");
        check(ms.contains("getUseClientMode"), "declares getUseClientMode");
        check(ms.contains("getWantClientAuth"), "declares getWantClientAuth");
        check(ms.contains("isInboundDone"), "declares isInboundDone");
        check(ms.contains("isOutboundDone"), "declares isOutboundDone");
        check(ms.contains("setEnableSessionCreation"), "declares setEnableSessionCreation");
        check(ms.contains("setEnabledCipherSuites"), "declares setEnabledCipherSuites");
        check(ms.contains("setEnabledProtocols"), "declares setEnabledProtocols");
        check(ms.contains("setHandshakeApplicationProtocolSelector"), "declares setHandshakeApplicationProtocolSelector");
        check(ms.contains("setNeedClientAuth"), "declares setNeedClientAuth");
        check(ms.contains("setSSLParameters"), "declares setSSLParameters");
        check(ms.contains("setUseClientMode"), "declares setUseClientMode");
        check(ms.contains("setWantClientAuth"), "declares setWantClientAuth");
        check(ms.contains("unwrap"), "declares unwrap");
        check(ms.contains("wrap"), "declares wrap");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
