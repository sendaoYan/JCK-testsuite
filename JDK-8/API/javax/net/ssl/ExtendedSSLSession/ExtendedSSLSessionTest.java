/*
 * @test
 * @summary javax.net.ssl.ExtendedSSLSession conformance: abstract class hierarchy and key methods.
 * @run main ExtendedSSLSessionTest
 */

public class ExtendedSSLSessionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.ExtendedSSLSession");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.net.ssl.SSLSession"), "implements javax.net.ssl.SSLSession");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getLocalSupportedSignatureAlgorithms"), "declares getLocalSupportedSignatureAlgorithms");
        check(ms.contains("getPeerSupportedSignatureAlgorithms"), "declares getPeerSupportedSignatureAlgorithms");
        check(ms.contains("getRequestedServerNames"), "declares getRequestedServerNames");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
