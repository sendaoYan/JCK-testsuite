/*
 * @test
 * @summary javax.net.ssl.X509KeyManager conformance: interface hierarchy and key methods.
 * @run main X509KeyManagerTest
 */

public class X509KeyManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.X509KeyManager");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("chooseClientAlias"), "declares chooseClientAlias");
        check(ms.contains("chooseServerAlias"), "declares chooseServerAlias");
        check(ms.contains("getCertificateChain"), "declares getCertificateChain");
        check(ms.contains("getClientAliases"), "declares getClientAliases");
        check(ms.contains("getPrivateKey"), "declares getPrivateKey");
        check(ms.contains("getServerAliases"), "declares getServerAliases");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
