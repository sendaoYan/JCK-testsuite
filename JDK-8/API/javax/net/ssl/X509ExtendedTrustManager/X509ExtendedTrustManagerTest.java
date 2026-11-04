/*
 * @test
 * @summary javax.net.ssl.X509ExtendedTrustManager conformance: abstract class hierarchy and key methods.
 * @run main X509ExtendedTrustManagerTest
 */

public class X509ExtendedTrustManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.X509ExtendedTrustManager");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.net.ssl.X509TrustManager"), "implements javax.net.ssl.X509TrustManager");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkClientTrusted"), "declares checkClientTrusted");
        check(ms.contains("checkServerTrusted"), "declares checkServerTrusted");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
