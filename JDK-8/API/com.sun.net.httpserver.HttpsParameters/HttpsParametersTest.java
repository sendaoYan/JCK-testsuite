/*
 * @test
 * @summary com.sun.net.httpserver.HttpsParameters conformance: hierarchy and declared members via reflection.
 * @run main HttpsParametersTest
 */

public class HttpsParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.net.httpserver.HttpsParameters", false, null);
        check(c.getName().equals("com.sun.net.httpserver.HttpsParameters"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCipherSuites"), "declares getCipherSuites");
        check(ms.contains("getClientAddress"), "declares getClientAddress");
        check(ms.contains("getHttpsConfigurator"), "declares getHttpsConfigurator");
        check(ms.contains("getNeedClientAuth"), "declares getNeedClientAuth");
        check(ms.contains("getProtocols"), "declares getProtocols");
        check(ms.contains("getWantClientAuth"), "declares getWantClientAuth");
        check(ms.contains("setCipherSuites"), "declares setCipherSuites");
        check(ms.contains("setNeedClientAuth"), "declares setNeedClientAuth");
        check(ms.contains("setProtocols"), "declares setProtocols");
        check(ms.contains("setSSLParameters"), "declares setSSLParameters");
        check(ms.contains("setWantClientAuth"), "declares setWantClientAuth");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
