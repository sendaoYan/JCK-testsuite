/*
 * @test
 * @summary org.ietf.jgss.GSSContext conformance: interface hierarchy and key methods.
 * @run main GSSContextTest
 */

public class GSSContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSContext");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("acceptSecContext"), "declares acceptSecContext");
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("export"), "declares export");
        check(ms.contains("getAnonymityState"), "declares getAnonymityState");
        check(ms.contains("getConfState"), "declares getConfState");
        check(ms.contains("getCredDelegState"), "declares getCredDelegState");
        check(ms.contains("getDelegCred"), "declares getDelegCred");
        check(ms.contains("getIntegState"), "declares getIntegState");
        check(ms.contains("getLifetime"), "declares getLifetime");
        check(ms.contains("getMIC"), "declares getMIC");
        check(ms.contains("getMech"), "declares getMech");
        check(ms.contains("getMutualAuthState"), "declares getMutualAuthState");
        check(ms.contains("getReplayDetState"), "declares getReplayDetState");
        check(ms.contains("getSequenceDetState"), "declares getSequenceDetState");
        check(ms.contains("getSrcName"), "declares getSrcName");
        check(ms.contains("getTargName"), "declares getTargName");
        check(ms.contains("getWrapSizeLimit"), "declares getWrapSizeLimit");
        check(ms.contains("initSecContext"), "declares initSecContext");
        check(ms.contains("isEstablished"), "declares isEstablished");
        check(ms.contains("isInitiator"), "declares isInitiator");
        check(ms.contains("isProtReady"), "declares isProtReady");
        check(ms.contains("isTransferable"), "declares isTransferable");
        check(ms.contains("requestAnonymity"), "declares requestAnonymity");
        check(ms.contains("requestConf"), "declares requestConf");
        check(ms.contains("requestCredDeleg"), "declares requestCredDeleg");
        check(ms.contains("requestInteg"), "declares requestInteg");
        check(ms.contains("requestLifetime"), "declares requestLifetime");
        check(ms.contains("requestMutualAuth"), "declares requestMutualAuth");
        check(ms.contains("requestReplayDet"), "declares requestReplayDet");
        check(ms.contains("requestSequenceDet"), "declares requestSequenceDet");
        check(ms.contains("setChannelBinding"), "declares setChannelBinding");
        check(ms.contains("unwrap"), "declares unwrap");
        check(ms.contains("verifyMIC"), "declares verifyMIC");
        check(ms.contains("wrap"), "declares wrap");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
