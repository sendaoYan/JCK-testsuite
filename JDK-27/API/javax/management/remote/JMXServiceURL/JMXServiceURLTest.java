/*
 * @test
 * @summary javax.management.remote.JMXServiceURL conformance: white-box structure + black-box behavior.
 * @run main JMXServiceURLTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;

import javax.management.remote.JMXServiceURL;

public class JMXServiceURLTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXServiceURL");
        check(!c.isInterface(), "concrete class");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");

        Method[] mm = c.getMethods();
        check(has(mm, "getProtocol"), "getProtocol");
        check(has(mm, "getHost"), "getHost");
        check(has(mm, "getPort"), "getPort");
        check(has(mm, "getURLPath"), "getURLPath");
        check(has(mm, "toString"), "toString");
        check(has(mm, "equals"), "equals");
        check(has(mm, "hashCode"), "hashCode");

        Constructor<?>[] cs = c.getConstructors();
        boolean hasStringCtor = false, has3Ctor = false, has4Ctor = false;
        for (Constructor<?> k : cs) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 1 && p[0] == String.class) hasStringCtor = true;
            if (p.length == 3 && p[0] == String.class && p[1] == String.class && p[2] == int.class) has3Ctor = true;
            if (p.length == 4 && p[0] == String.class && p[1] == String.class && p[2] == int.class && p[3] == String.class) has4Ctor = true;
        }
        check(hasStringCtor, "String ctor");
        check(has3Ctor, "(String,String,int) ctor");
        check(has4Ctor, "(String,String,int,String) ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Full parse of a JRMP URL with empty host and a JNDI path.
        JMXServiceURL rmi = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        check(rmi.getProtocol().equals("rmi"), "rmi protocol");
        check(rmi.getHost().equals(""), "rmi empty host");
        check(rmi.getPort() == 0, "rmi port 0");
        check(rmi.getURLPath().equals("/jndi/rmi://localhost:9999/server"), "rmi url path");
        check(rmi.toString().equals("service:jmx:rmi:///jndi/rmi://localhost:9999/server"), "rmi toString round-trip");

        // Full parse of a jmxmp URL with host and port.
        JMXServiceURL jmxmp = new JMXServiceURL("service:jmx:jmxmp://host:1234");
        check(jmxmp.getProtocol().equals("jmxmp"), "jmxmp protocol");
        check(jmxmp.getHost().equals("host"), "jmxmp host");
        check(jmxmp.getPort() == 1234, "jmxmp port");
        check(jmxmp.getURLPath().equals(""), "jmxmp empty path");
        check(jmxmp.toString().equals("service:jmx:jmxmp://host:1234"), "jmxmp toString");

        // Protocol is lower-cased on parse.
        JMXServiceURL upper = new JMXServiceURL("service:jmx:RMI://LOCALHOST:9999/x");
        check(upper.getProtocol().equals("rmi"), "protocol lower-cased");

        // toString omits zero port.
        JMXServiceURL noPort = new JMXServiceURL("service:jmx:rmi://localhost/x");
        check(noPort.getPort() == 0, "noPort port 0");
        check(noPort.toString().equals("service:jmx:rmi://localhost/x"), "noPort toString omits 0");

        // equals: protocol and host are case-insensitive, port/path sensitive.
        JMXServiceURL a = new JMXServiceURL("service:jmx:rmi://LOCALHOST:9999/x");
        JMXServiceURL b = new JMXServiceURL("service:jmx:RMI://localhost:9999/x");
        check(a.equals(b), "equals ignore case protocol+host");
        // hashCode is toString().hashCode() and therefore uses the original host case.
        JMXServiceURL a2 = new JMXServiceURL("service:jmx:rmi://LOCALHOST:9999/x");
        check(a.hashCode() == a2.hashCode(), "hashCode stable for identical input");
        check(!a.equals(new JMXServiceURL("service:jmx:rmi://localhost:9998/x")), "different port not equal");
        check(!a.equals(new JMXServiceURL("service:jmx:rmi://localhost:9999/y")), "different path not equal");
        check(!a.equals("service:jmx:rmi://LOCALHOST:9999/x"), "not equal to String");

        // 4-arg constructor: explicit protocol/host/port/path; null urlPath defaults to empty.
        JMXServiceURL four = new JMXServiceURL("rmi", "localhost", 9999, "/server");
        check(four.getProtocol().equals("rmi"), "4-arg protocol");
        check(four.getHost().equals("localhost"), "4-arg host");
        check(four.getPort() == 9999, "4-arg port");
        check(four.getURLPath().equals("/server"), "4-arg path");
        JMXServiceURL fourDefault = new JMXServiceURL("jmxmp", "localhost", 0, null);
        check(fourDefault.getURLPath().equals(""), "null urlPath defaults empty");
        check(fourDefault.getPort() == 0, "4-arg port 0");

        // Exact MalformedURLException messages for illegal URLs.
        try {
            new JMXServiceURL("not-a-url");
            check(false, "expected MalformedURLException for bad prefix");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Service URL must start with service:jmx:"), "prefix message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("service:jmx:rmi:foo");
            check(false, "expected missing :// exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Missing \"://\" after protocol name"), "missing :// message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("service:jmx:rmi://:9999");
            check(false, "expected port-without-host exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Cannot give port number without host name"), "port w/o host message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("rmi", "localhost", -1, "/x");
            check(false, "expected negative port exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Bad port: -1"), "negative port message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("rmi", "localhost", 9999, "noSlash");
            check(false, "expected bad path exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Bad URL path: noSlash"), "bad path message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("9bad", "localhost", 0, "/x");
            check(false, "expected invalid protocol exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Missing or invalid protocol name: \"9bad\""), "bad protocol message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("rmi", "bad_host", 0, "/x");
            check(false, "expected bad host exception");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Bad host: \"bad_host\""), "bad host message: " + e.getMessage());
        }
        try {
            new JMXServiceURL("service:jmx:rmi://localhost:9999abc");
            check(false, "expected bad url path exception (trailing junk)");
        } catch (MalformedURLException e) {
            check(e.getMessage().equals("Bad URL path: abc"), "trailing junk path message: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
