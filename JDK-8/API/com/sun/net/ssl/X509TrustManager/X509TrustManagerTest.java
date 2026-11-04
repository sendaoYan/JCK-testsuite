/*
 * @test
 * @summary com.sun.net.ssl.X509TrustManager conformance: hierarchy and declared members via reflection.
 * @run main X509TrustManagerTest
 */

public class X509TrustManagerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.X509TrustManager", false, null);
        check(c.getName().equals("com.sun.net.ssl.X509TrustManager"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.net.ssl.TrustManager"), "implements com.sun.net.ssl.TrustManager");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getAcceptedIssuers"), "declares getAcceptedIssuers");
        check(ms.contains("isClientTrusted"), "declares isClientTrusted");
        check(ms.contains("isServerTrusted"), "declares isServerTrusted");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
