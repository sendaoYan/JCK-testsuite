/*
 * @test
 * @summary com.sun.net.ssl.internal.www.protocol.https.DelegateHttpsURLConnection conformance: hierarchy and declared members via reflection.
 * @run main DelegateHttpsURLConnectionTest
 */

public class DelegateHttpsURLConnectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.ssl.internal.www.protocol.https.DelegateHttpsURLConnection", false, null);
        check(c.getName().equals("com.sun.net.ssl.internal.www.protocol.https.DelegateHttpsURLConnection"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection"), "superclass sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("dispose"), "declares dispose");
        check(ms.contains("getHostnameVerifier"), "declares getHostnameVerifier");
        check(ms.contains("getSSLSocketFactory"), "declares getSSLSocketFactory");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
