/*
 * @test
 * @summary com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager conformance: hierarchy and declared members via reflection.
 * @run main X509ExtendedTrustManagerTest
 */

public class X509ExtendedTrustManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager", false, null);
        check(c.getName().equals("com.sun.net.ssl.internal.ssl.X509ExtendedTrustManager"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
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
