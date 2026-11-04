/*
 * @test
 * @summary com.sun.net.httpserver.HttpsParameters conformance: white-box structure + black-box behavior.
 * @run main HttpsParametersTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import javax.net.ssl.*;

public class HttpsParametersTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.net.httpserver.HttpsParameters");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"getHttpsConfigurator","getClientAddress","setSSLParameters","getCipherSuites","setCipherSuites","getProtocols","setProtocols","getWantClientAuth","setWantClientAuth","getNeedClientAuth","setNeedClientAuth"};
        for (String n : names) check(m.contains(n), n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        final com.sun.net.httpserver.HttpsConfigurator config =
                new com.sun.net.httpserver.HttpsConfigurator(SSLContext.getDefault());
        final InetSocketAddress addr = new InetSocketAddress("127.0.0.1", 9999);
        final SSLParameters[] recorded = new SSLParameters[1];

        com.sun.net.httpserver.HttpsParameters p = new com.sun.net.httpserver.HttpsParameters() {
            public com.sun.net.httpserver.HttpsConfigurator getHttpsConfigurator() { return config; }
            public InetSocketAddress getClientAddress() { return addr; }
            public void setSSLParameters(SSLParameters sp) { recorded[0] = sp; }
        };

        // abstract accessors
        check(p.getHttpsConfigurator() == config, "getHttpsConfigurator");
        check(p.getClientAddress().equals(addr), "getClientAddress");

        // cipher suites: default null; set clones; get clones; null clears
        check(p.getCipherSuites() == null, "cipherSuites default null");
        p.setCipherSuites(new String[]{"A", "B"});
        String[] cs = p.getCipherSuites();
        check(cs.length == 2 && cs[0].equals("A") && cs[1].equals("B"), "set/get cipherSuites");
        cs[0] = "MUTATED";
        check(p.getCipherSuites()[0].equals("A"), "getCipherSuites returns defensive copy");
        String[] input = new String[]{"X"};
        p.setCipherSuites(input);
        input[0] = "MUTATED";
        check(p.getCipherSuites()[0].equals("X"), "setCipherSuites clones input");
        p.setCipherSuites(null);
        check(p.getCipherSuites() == null, "setCipherSuites null clears");

        // protocols: same semantics
        check(p.getProtocols() == null, "protocols default null");
        p.setProtocols(new String[]{"TLSv1.2", "TLSv1.3"});
        check(p.getProtocols().length == 2 && p.getProtocols()[0].equals("TLSv1.2"), "set/get protocols");
        p.setProtocols(null);
        check(p.getProtocols() == null, "setProtocols null clears");

        // client auth flags are mutually exclusive
        check(!p.getWantClientAuth() && !p.getNeedClientAuth(), "client auth defaults false");
        p.setWantClientAuth(true);
        check(p.getWantClientAuth() && !p.getNeedClientAuth(), "setWantClientAuth clears needClientAuth");
        p.setNeedClientAuth(true);
        check(p.getNeedClientAuth() && !p.getWantClientAuth(), "setNeedClientAuth clears wantClientAuth");

        // setSSLParameters dispatch
        SSLParameters sp = new SSLParameters();
        p.setSSLParameters(sp);
        check(recorded[0] == sp, "setSSLParameters recorded");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
