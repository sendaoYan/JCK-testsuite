/*
 * @test
 * @summary javax.net.ssl.SSLSocketFactory conformance: abstract class hierarchy and key methods.
 * @run main SSLSocketFactoryTest
 */

public class SSLSocketFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.SSLSocketFactory");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("javax.net.SocketFactory"), "extends javax.net.SocketFactory");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("createSocket"), "declares createSocket");
        check(ms.contains("getDefault"), "declares getDefault");
        check(ms.contains("getDefaultCipherSuites"), "declares getDefaultCipherSuites");
        check(ms.contains("getSupportedCipherSuites"), "declares getSupportedCipherSuites");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
