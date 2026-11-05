/*
 * @test
 * @summary javax.management.remote.JMXServiceURL conformance: protocol/host/port.
 * @run main JMXServiceURLTest
 */
import javax.management.*;


import javax.management.remote.*;
public class JMXServiceURLTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        JMXServiceURL u = new JMXServiceURL("rmi", "localhost", 9999);
        check("rmi".equals(u.getProtocol()), "protocol");
        check("localhost".equals(u.getHost()), "host");
        check(u.getPort() == 9999, "port");
        check(u.toString() != null, "toString");
        check(u.equals(new JMXServiceURL("rmi", "localhost", 9999)), "equals");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): JNDI-form parse, boundaries, case semantics, round-trip, serialization ====
    static void testDeepened() throws Exception {
        // JNDI-form URL: host/port belong to the outer URL (empty/0); the lookup address lives in the path.
        JMXServiceURL jndi = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/x");
        check("rmi".equals(jndi.getProtocol()), "d-jndi protocol");
        check("".equals(jndi.getHost()), "d-jndi outer host empty");
        check(jndi.getPort() == 0, "d-jndi outer port 0");
        check("/jndi/rmi://localhost:9999/x".equals(jndi.getURLPath()), "d-jndi path carries lookup address");

        // Host case is preserved by getHost ...
        JMXServiceURL up = new JMXServiceURL("service:jmx:rmi://LOCALhost:80/x");
        check("LOCALhost".equals(up.getHost()), "d-host case preserved");
        // ... but equals ignores host case (hashCode keeps the original case: it is
        // toString().hashCode(), so case-variant equal URLs may hash differently - recorded quirk).
        JMXServiceURL lo = new JMXServiceURL("service:jmx:rmi://localhost:80/x");
        check(up.equals(lo), "d-equals ignores host case");
        check(up.hashCode() == new JMXServiceURL("service:jmx:rmi://LOCALhost:80/x").hashCode(), "d-hashCode stable for identical input");
        check(up.equals(new JMXServiceURL("service:jmx:RMI://LOCALhost:80/x")), "d-equals ignores protocol case");

        // Port domain: 0..65535 legal, and out-of-range values above 65535 are also accepted
        // (only negative ports are rejected, by both JDK 8 and JDK 27).
        check(new JMXServiceURL("rmi", "h", 0, "/x").getPort() == 0, "d-port 0 legal");
        check(new JMXServiceURL("rmi", "h", 65535, "/x").getPort() == 65535, "d-port 65535 legal");
        check(new JMXServiceURL("rmi", "h", 65536, "/x").getPort() == 65536, "d-port 65536 accepted quirk");

        // IPv6 literal: brackets stripped from getHost, kept in toString.
        JMXServiceURL v6 = new JMXServiceURL("service:jmx:rmi://[::1]:9999/x");
        check("::1".equals(v6.getHost()), "d-ipv6 host without brackets");
        check(v6.getPort() == 9999, "d-ipv6 port");
        check(v6.toString().startsWith("service:jmx:rmi://[::1]:9999"), "d-ipv6 toString keeps brackets");

        // 3-arg constructor equals the 4-arg constructor with a null path.
        JMXServiceURL three = new JMXServiceURL("rmi", "localhost", 9999);
        JMXServiceURL four = new JMXServiceURL("rmi", "localhost", 9999, null);
        check("".equals(three.getURLPath()), "d-3arg path empty");
        check(three.equals(four), "d-3arg equals 4arg-null-path");

        // toString round-trip: parsing toString yields an equal URL (value-object contract).
        check(up.equals(new JMXServiceURL(up.toString())), "d-parse(toString) equals");
        check(jndi.equals(new JMXServiceURL(jndi.toString())), "d-parse(toString) equals jndi");

        // equals: null and foreign types are never equal.
        check(!up.equals(null), "d-not equals null");
        check(!up.equals("service:jmx:rmi://LOCALhost:80/x"), "d-not equals String");

        // Field-by-field inequality.
        check(!up.equals(new JMXServiceURL("service:jmx:rmi://localhost:81/x")), "d-different port not equals");
        check(!up.equals(new JMXServiceURL("service:jmx:rmi://localhost:80/y")), "d-different path not equals");
        check(!up.equals(new JMXServiceURL("service:jmx:jmxmp://localhost:80/x")), "d-different protocol not equals");

        // Null input: NullPointerException on both JDKs (message differs; see DESIGN.md).
        try {
            new JMXServiceURL((String) null);
            check(false, "d-null string should throw");
        } catch (NullPointerException e) {
            passed++;
        }

        // Serialization round-trip preserves value equality.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(up);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            check(up.equals(ois.readObject()), "d-serialization round-trip equals");
        } finally {
            if (ois != null) ois.close();
        }
    }

}
