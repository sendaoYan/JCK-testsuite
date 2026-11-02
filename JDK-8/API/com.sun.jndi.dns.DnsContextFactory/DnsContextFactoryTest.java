/*
 * @test
 * @summary com.sun.jndi.dns.DnsContextFactory conformance: hierarchy and declared members via reflection.
 * @run main DnsContextFactoryTest
 */

public class DnsContextFactoryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.DnsContextFactory", false, null);
        check(c.getName().equals("com.sun.jndi.dns.DnsContextFactory"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.naming.spi.InitialContextFactory"), "implements javax.naming.spi.InitialContextFactory");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("constructProviderUrl"), "declares constructProviderUrl");
        check(ms.contains("filterNameServers"), "declares filterNameServers");
        check(ms.contains("getContext"), "declares getContext");
        check(ms.contains("getInitCtxUrl"), "declares getInitCtxUrl");
        check(ms.contains("getInitialContext"), "declares getInitialContext");
        check(ms.contains("platformServersAvailable"), "declares platformServersAvailable");
        check(ms.contains("platformServersUsed"), "declares platformServersUsed");
        check(ms.contains("serversForUrls"), "declares serversForUrls");
        check(ms.contains("urlToContext"), "declares urlToContext");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
