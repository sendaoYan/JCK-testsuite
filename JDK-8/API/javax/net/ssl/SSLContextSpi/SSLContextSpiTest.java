/*
 * @test
 * @summary javax.net.ssl.SSLContextSpi conformance: abstract class hierarchy and key methods.
 * @run main SSLContextSpiTest
 */

public class SSLContextSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLContextSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineCreateSSLEngine"), "declares engineCreateSSLEngine");
        check(ms.contains("engineGetClientSessionContext"), "declares engineGetClientSessionContext");
        check(ms.contains("engineGetDefaultSSLParameters"), "declares engineGetDefaultSSLParameters");
        check(ms.contains("engineGetServerSessionContext"), "declares engineGetServerSessionContext");
        check(ms.contains("engineGetServerSocketFactory"), "declares engineGetServerSocketFactory");
        check(ms.contains("engineGetSocketFactory"), "declares engineGetSocketFactory");
        check(ms.contains("engineGetSupportedSSLParameters"), "declares engineGetSupportedSSLParameters");
        check(ms.contains("engineInit"), "declares engineInit");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
