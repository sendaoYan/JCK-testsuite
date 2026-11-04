/*
 * @test
 * @summary java.net.ProxySelector conformance: abstract class hierarchy and members.
 * @run main ProxySelectorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ProxySelectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** Records every select/connectFailed invocation for argument-transparency checks. */
    static class RecordingSelector extends ProxySelector {
        final List<URI> selectUris = new ArrayList<URI>();
        final List<Object[]> failures = new ArrayList<Object[]>();
        final List<Proxy> answer = Collections.singletonList(Proxy.NO_PROXY);
        int selectCalls, failedCalls;
        public List<Proxy> select(URI uri) { selectCalls++; selectUris.add(uri); return answer; }
        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            failedCalls++; failures.add(new Object[] {uri, sa, ioe});
        }
    }

    // ---- white-box structure (javap -public java.net.ProxySelector, JDK-8) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ProxySelector");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getConstructor() != null, "public no-arg ctor");
        Method select = c.getMethod("select", URI.class);
        check(Modifier.isAbstract(select.getModifiers()), "select abstract");
        check(List.class.equals(select.getReturnType()), "select returns List");
        Method cf = c.getMethod("connectFailed", URI.class, SocketAddress.class, IOException.class);
        check(Modifier.isAbstract(cf.getModifiers()), "connectFailed abstract");
        check(cf.getReturnType() == void.class, "connectFailed void");
        check(Modifier.isStatic(c.getMethod("getDefault").getModifiers()), "getDefault static");
        check(Modifier.isStatic(c.getMethod("setDefault", ProxySelector.class).getModifiers()), "setDefault static");
        check(c.getMethod("setDefault", ProxySelector.class).getReturnType() == void.class, "setDefault void");
        // JDK-8 has no ProxySelector.of(InetSocketAddress) factory (added in JDK 9)
        boolean ofAbsent = false;
        try {
            c.getMethod("of", InetSocketAddress.class);
        } catch (NoSuchMethodException e) {
            ofAbsent = true;
        }
        check(ofAbsent, "of(InetSocketAddress) absent on JDK-8");
    }

    // ---- abstract-class instantiation and default selector ----
    static void testDefaultSelector() throws Exception {
        ProxySelector def = ProxySelector.getDefault();
        check(def != null, "default selector non-null (sun.net.spi.DefaultProxySelector)");
        check(def != null && !(def instanceof RecordingSelector), "default is not user type");
        // No proxy system properties in a plain jtreg run: selection must be a
        // non-empty list whose first element behaves as DIRECT. Guarded so a
        // harness that sets proxy properties does not false-fail.
        boolean proxyConfigured = System.getProperty("http.proxyHost") != null
            || System.getProperty("https.proxyHost") != null
            || System.getProperty("socksProxyHost") != null;
        List<Proxy> l = def.select(URI.create("http://127.0.0.1/some/path"));
        check(l != null, "select returns non-null list");
        check(!l.isEmpty(), "select returns non-empty list");
        check(l.get(0) != null, "first element non-null");
        if (!proxyConfigured) {
            check(l.size() == 1, "no-proxy env: exactly one DIRECT entry");
            check(l.get(0) == Proxy.NO_PROXY, "element is Proxy.NO_PROXY identity");
            check(l.get(0).type() == Proxy.Type.DIRECT, "element type DIRECT");
            check(l.get(0).address() == null, "element address null");
            List<Proxy> l2 = def.select(URI.create("http://localhost/y"));
            check(l2.size() == 1 && l2.get(0).type() == Proxy.Type.DIRECT, "localhost also DIRECT");
        }
        // select(null) is rejected by the default implementation (probed: IAE)
        try {
            def.select(null);
            check(false, "select(null) should throw");
        } catch (IllegalArgumentException e) {
            check(String.valueOf(e.getMessage()).contains("null"), "select(null) message mentions null");
        } catch (NullPointerException e) {
            passed++; // tolerate NPE; both are legal per spec wording
        }
    }

    // ---- subclass contract: select/connectFailed argument transparency ----
    static void testRecordingSubclass() throws Exception {
        RecordingSelector rs = new RecordingSelector();
        URI u1 = URI.create("http://127.0.0.1/a");
        URI u2 = URI.create("ftp://127.0.0.1/b");
        List<Proxy> r1 = rs.select(u1);
        List<Proxy> r2 = rs.select(u2);
        check(rs.selectCalls == 2, "select invoked twice");
        check(rs.selectUris.get(0) == u1, "first select uri identity");
        check(rs.selectUris.get(1) == u2, "second select uri identity");
        check(r1 == rs.answer, "select returns subclass list unchanged");
        check(r2 == rs.answer, "select returns same list each call");
        check(r1.size() == 1 && r1.get(0) == Proxy.NO_PROXY, "answer content");
        InetSocketAddress sa = new InetSocketAddress("127.0.0.1", 3128);
        IOException boom = new IOException("boom");
        rs.connectFailed(u2, sa, boom);
        check(rs.failedCalls == 1, "connectFailed invoked once");
        check(rs.failures.get(0)[0] == u2, "connectFailed uri identity");
        check(rs.failures.get(0)[1] == sa, "connectFailed address identity");
        check(rs.failures.get(0)[2] == boom, "connectFailed IOException identity");
        // returning a mutable list with HTTP proxies is fine too
        final List<Proxy> httpList = new ArrayList<Proxy>(
            Arrays.asList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080))));
        ProxySelector httpSel = new ProxySelector() {
            public List<Proxy> select(URI uri) { return httpList; }
            public void connectFailed(URI uri, SocketAddress sa2, IOException ioe) { }
        };
        List<Proxy> got = httpSel.select(URI.create("http://127.0.0.1/x"));
        check(got == httpList, "http selector returns exact list");
        check(got.get(0).type() == Proxy.Type.HTTP, "http element type");
        check(((InetSocketAddress) got.get(0).address()).getPort() == 8080, "http element port");
        httpList.add(Proxy.NO_PROXY);
        check(httpSel.select(URI.create("http://127.0.0.1/x")).size() == 2, "list is live");
        httpSel.connectFailed(URI.create("http://127.0.0.1/x"), sa, boom); // must simply return
        passed++;
        // empty list is a legal (if unusual) answer
        ProxySelector emptySel = new ProxySelector() {
            public List<Proxy> select(URI uri) { return Collections.<Proxy>emptyList(); }
            public void connectFailed(URI uri, SocketAddress sa2, IOException ioe) { }
        };
        check(emptySel.select(URI.create("http://127.0.0.1/x")).isEmpty(), "empty list answer");
        emptySel.connectFailed(URI.create("http://127.0.0.1/x"), sa, boom);
        passed++;
        // multi-entry fallback list (proxy then DIRECT) keeps caller order
        Proxy httpP = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("127.0.0.1", 8080));
        Proxy socksP = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080));
        final List<Proxy> fallback = new ArrayList<Proxy>(Arrays.asList(httpP, socksP, Proxy.NO_PROXY));
        ProxySelector fbSel = new ProxySelector() {
            public List<Proxy> select(URI uri) { return fallback; }
            public void connectFailed(URI uri, SocketAddress sa2, IOException ioe) { }
        };
        List<Proxy> fb = fbSel.select(URI.create("http://127.0.0.1/x"));
        check(fb == fallback, "fallback list identity");
        check(fb.size() == 3, "fallback size 3");
        check(fb.get(0) == httpP && fb.get(1) == socksP && fb.get(2) == Proxy.NO_PROXY, "fallback order preserved");
        check(fb.get(0).type() == Proxy.Type.HTTP && fb.get(1).type() == Proxy.Type.SOCKS, "fallback types");
    }

    // ---- setDefault/getDefault round-trip (restore previous value) ----
    static void testSetDefaultRoundTrip() throws Exception {
        ProxySelector prev = ProxySelector.getDefault();
        RecordingSelector rs = new RecordingSelector();
        try {
            ProxySelector.setDefault(rs);
            check(ProxySelector.getDefault() == rs, "setDefault then getDefault identity");
            check(ProxySelector.getDefault() == rs, "getDefault stable");
            ProxySelector rs2 = new RecordingSelector();
            ProxySelector.setDefault(rs2);
            check(ProxySelector.getDefault() == rs2, "second setDefault replaces");
            ProxySelector.setDefault(null);
            check(ProxySelector.getDefault() == null, "setDefault(null) allowed, resets to null");
            check(ProxySelector.getDefault() == null, "null default stable");
            // A null default means no selector is consulted by the protocol
            // handlers; the JVM does not reinstall the built-in one automatically
            // (probed). No network access is performed here.
        } finally {
            ProxySelector.setDefault(prev);
            check(ProxySelector.getDefault() == prev, "previous selector restored");
        }
        check(ProxySelector.getDefault() != null, "restored default non-null");
    }

    // ---- default selector: URI shape matrix + connectFailed bookkeeping ----
    static void testDefaultSelectorUriMatrix() throws Exception {
        ProxySelector def = ProxySelector.getDefault();
        check(ProxySelector.getDefault() == def, "getDefault stable identity");
        URI[] uris = {
            URI.create("http://127.0.0.1"),
            URI.create("http://127.0.0.1:9999/deep/path?q=1"),
            URI.create("https://127.0.0.1/s"),
            URI.create("ftp://127.0.0.1/f"),
        };
        for (URI u : uris) {
            List<Proxy> l = def.select(u);
            check(l != null && !l.isEmpty() && l.get(0) != null, "select non-empty for " + u);
            check(l.get(0).type() == Proxy.Type.DIRECT, "DIRECT for " + u);
        }
        // connectFailed on the default selector is bookkeeping only (no IO, probed)
        def.connectFailed(URI.create("http://127.0.0.1/x"),
            new InetSocketAddress("127.0.0.1", 80), new IOException("probe"));
        passed++;
        check(ProxySelector.getDefault() == def, "default unchanged after connectFailed");
        try {
            def.connectFailed(null, null, null);
            check(false, "connectFailed(nulls) should throw");
        } catch (IllegalArgumentException e) {
            check(String.valueOf(e.getMessage()).contains("null"), "connectFailed nulls message");
        }
        // select still works after a reported failure
        check(def.select(URI.create("http://127.0.0.1/x")).get(0).type() == Proxy.Type.DIRECT,
            "select works after connectFailed");
    }

    // ---- exception transparency: subclass exceptions propagate unchanged ----
    static void testExceptionTransparency() throws Exception {
        final IllegalStateException boom = new IllegalStateException("sel-boom");
        ProxySelector throwing = new ProxySelector() {
            public List<Proxy> select(URI uri) { throw boom; }
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) { }
        };
        try {
            throwing.select(URI.create("http://127.0.0.1/x"));
            check(false, "subclass exception must propagate");
        } catch (IllegalStateException e) {
            check(e == boom, "exact exception object propagates");
        }
        final IOException failBoom = new IOException("cf-boom");
        ProxySelector failing = new ProxySelector() {
            public List<Proxy> select(URI uri) { return Collections.singletonList(Proxy.NO_PROXY); }
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) { throw new UnsupportedOperationException(failBoom); }
        };
        try {
            failing.connectFailed(URI.create("http://127.0.0.1/x"), null, null);
            check(false, "connectFailed exception must propagate");
        } catch (UnsupportedOperationException e) {
            check(e.getCause() == failBoom, "connectFailed cause propagates");
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.ProxySelector");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("connectFailed"), "has method connectFailed");
        check(names.contains("getDefault"), "has method getDefault");
        check(names.contains("select"), "has method select");
        testWhiteBox();
        testDefaultSelector();
        testRecordingSubclass();
        testSetDefaultRoundTrip();
        testDefaultSelectorUriMatrix();
        testExceptionTransparency();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
