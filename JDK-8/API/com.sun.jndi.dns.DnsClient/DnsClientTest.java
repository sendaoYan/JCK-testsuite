/*
 * @test
 * @summary com.sun.jndi.dns.DnsClient conformance: hierarchy and declared members via reflection.
 * @run main DnsClientTest
 */

public class DnsClientTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.dns.DnsClient", false, null);
        check(c.getName().equals("com.sun.jndi.dns.DnsClient"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkResponseCode"), "declares checkResponseCode");
        check(ms.contains("close"), "declares close");
        check(ms.contains("continueTcpQuery"), "declares continueTcpQuery");
        check(ms.contains("doTcpQuery"), "declares doTcpQuery");
        check(ms.contains("doUdpQuery"), "declares doUdpQuery");
        check(ms.contains("dprint"), "declares dprint");
        check(ms.contains("finalize"), "declares finalize");
        check(ms.contains("getDatagramSocket"), "declares getDatagramSocket");
        check(ms.contains("isMatchResponse"), "declares isMatchResponse");
        check(ms.contains("lookupResponse"), "declares lookupResponse");
        check(ms.contains("makeQueryName"), "declares makeQueryName");
        check(ms.contains("makeQueryPacket"), "declares makeQueryPacket");
        check(ms.contains("query"), "declares query");
        check(ms.contains("queryZone"), "declares queryZone");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
