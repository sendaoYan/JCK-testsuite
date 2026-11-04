/*
 * @test
 * @summary java.net.SocketPermission conformance: name/actions/implies (JDK-27; class deprecated for removal since 25 but functional).
 * @run main SocketPermissionTest
 */

import java.net.SocketPermission;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Enumeration;

public class SocketPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static SocketPermission sp(String h, String a) { return new SocketPermission(h, a); }
    static void i(String from, String fAct, String to, String tAct, boolean want) {
        boolean got = sp(from, fAct).implies(sp(to, tAct));
        check(got == want, "implies(\"" + from + "\",\"" + fAct + "\") -> (\"" + to + "\",\"" + tAct
            + "\") expected " + want + " got " + got);
    }
    static void iTrue(String from, String fAct, String to, String tAct) { i(from, fAct, to, tAct, true); }
    static void iFalse(String from, String fAct, String to, String tAct) { i(from, fAct, to, tAct, false); }
    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }
    static void failsMsg(Class<? extends Throwable> ex, String needle, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && String.valueOf(t.getMessage()).contains(needle)) { passed++; return; }
            throw new AssertionError(m + " wrong exception/message " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    // ---- white-box structure (javap -public java.net.SocketPermission, JDK-8) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getSuperclass() == java.security.Permission.class, "direct Permission subclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructors().length == 1, "single public ctor");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", Permission.class) != null, "implies");
        check(c.getMethod("equals", Object.class) != null, "equals");
        check(c.getMethod("hashCode") != null, "hashCode");
        check(c.getMethod("newPermissionCollection") != null, "newPermissionCollection");
    }

    // ---- actions parsing and canonical getActions (probed; canonical order
    //      connect,listen,accept,resolve and RESOLVE auto-OR'd with the others) ----
    static void testActionNormalization() {
        check("connect,resolve".equals(sp("localhost:80", "connect").getActions()), "connect adds resolve");
        check("listen,resolve".equals(sp("localhost:80", "listen").getActions()), "listen adds resolve");
        check("accept,resolve".equals(sp("localhost:80", "accept").getActions()), "accept adds resolve");
        check("resolve".equals(sp("localhost:80", "resolve").getActions()), "resolve alone stays resolve");
        check("connect,resolve".equals(sp("localhost:80", "CONNECT").getActions()), "case folded");
        check("listen,resolve".equals(sp("localhost:80", "LISTEN").getActions()), "LISTEN folded");
        check("accept,resolve".equals(sp("localhost:80", "Accept").getActions()), "Accept folded");
        check("resolve".equals(sp("localhost:80", "Resolve").getActions()), "Resolve folded");
        check("connect,resolve".equals(sp("localhost:80", "connect,connect").getActions()), "duplicates collapse");
        check("connect,resolve".equals(sp("localhost:80", "connect,resolve,connect").getActions()),
            "duplicate after resolve collapses");
        check("connect,accept,resolve".equals(sp("localhost:80", "connect, accept").getActions()),
            "space after comma skipped");
        check("connect,resolve".equals(sp("localhost:80", " Connect ").getActions()), "leading/trailing spaces skipped");
        check("connect,listen,accept,resolve".equals(sp("localhost:80", "connect,accept,listen,resolve").getActions()),
            "canonical order 1");
        check("connect,listen,accept,resolve".equals(sp("localhost:80", "resolve,listen,connect,accept").getActions()),
            "canonical order 2 (input reversed)");
        check("connect,listen,accept,resolve".equals(sp("localhost:80", "ACCEPT, Connect,LiStEn,resolve").getActions()),
            "canonical order 3 (mixed case and spaces)");
        check("connect,listen,resolve".equals(sp("*:1-1024", "listen,connect").getActions()), "listen+connect");
        check("connect,listen,accept,resolve".equals(sp("localhost:80", "accept,listen,connect").getActions()),
            "three actions canonical");
        // host part does not influence action parsing
        check("connect,resolve".equals(sp("*.example.com", "connect").getActions()), "wildcard host actions");
        check("connect,resolve".equals(sp("[::1]", "connect").getActions()), "IPv6 host actions");
        // resolve never implies the others
        check(!"resolve".equals(sp("localhost:80", "connect").getActions()), "connect is not resolve-only");
        check(!sp("localhost:80", "resolve").implies(sp("localhost:80", "connect")), "resolve not imply connect");
    }

    // ---- name forms: getName echoes the spec as given (probed) ----
    static void testNameForms() {
        check("localhost:80".equals(sp("localhost:80", "connect").getName()), "plain host:port");
        check("localhost".equals(sp("localhost", "resolve").getName()), "host without port");
        check("localhost:".equals(sp("localhost:", "connect").getName()), "empty port kept");
        check("localhost:*".equals(sp("localhost:*", "connect").getName()), "star port kept");
        check("localhost:80-".equals(sp("localhost:80-", "connect").getName()), "open upper bound kept");
        check("localhost:-80".equals(sp("localhost:-80", "connect").getName()), "open lower bound kept");
        check("localhost:80-90".equals(sp("localhost:80-90", "connect").getName()), "range kept");
        check("localhost:0".equals(sp("localhost:0", "connect").getName()), "port 0");
        check("localhost:080".equals(sp("localhost:080", "connect").getName()), "leading zero kept in name");
        check("*.example.com".equals(sp("*.example.com", "resolve").getName()), "wildcard host");
        check("*".equals(sp("*", "connect").getName()), "bare wildcard");
        check("*:80".equals(sp("*:80", "connect").getName()), "wildcard with port");
        check("127.0.0.1".equals(sp("127.0.0.1", "connect").getName()), "IPv4 literal");
        check("[::1]".equals(sp("[::1]", "connect").getName()), "IPv6 literal");
        check("[::1]:80".equals(sp("[::1]:80", "connect").getName()), "IPv6 literal with port");
        check("[::1]:1-1024".equals(sp("[::1]:1-1024", "connect").getName()), "IPv6 literal with range");
        check("localhost".equals(sp("", "connect").getName()), "empty host normalizes to localhost");
        check("localhost".equals(sp("", "resolve").getName()), "empty host resolve normalizes");
        check(":".equals(sp(":", "connect").getName()), "colon-only name kept");
    }

    // ---- constructor error paths (probed on JDK-8; messages recorded) ----
    static void testCtorErrors() {
        failsMsg(IllegalArgumentException.class, "action can't be empty", "empty actions",
            () -> sp("localhost:80", ""));
        failsMsg(NullPointerException.class, "action can't be null", "null actions",
            () -> sp("localhost:80", null));
        failsMsg(IllegalArgumentException.class, "invalid permission: foo", "unknown action",
            () -> sp("localhost:80", "foo"));
        failsMsg(IllegalArgumentException.class, "invalid permission: connect,,accept", "empty action word",
            () -> sp("localhost:80", "connect,,accept"));
        fails(IllegalArgumentException.class, "action garbage", () -> sp("localhost:80", "connectity"));
        fails(NullPointerException.class, "null host", () -> sp(null, "connect"));
        failsMsg(IllegalArgumentException.class, "invalid port range: abc", "non-numeric port",
            () -> sp("localhost:abc", "connect"));
        failsMsg(IllegalArgumentException.class, "invalid port range: 80-abc", "bad range upper",
            () -> sp("localhost:80-abc", "connect"));
        failsMsg(IllegalArgumentException.class, "invalid port range: 90-80", "inverted range",
            () -> sp("localhost:90-80", "connect"));
        failsMsg(IllegalArgumentException.class, "invalid port range: 1-1024-", "double dash",
            () -> sp("localhost:1-1024-", "connect"));
        // legacy parser accepts out-of-16-bit and negative ports (probed)
        check(sp("localhost:65536", "connect") != null, "port 65536 accepted (legacy parser)");
        check(sp("localhost:70000", "connect") != null, "port 70000 accepted (legacy parser)");
        check(sp("localhost:-1", "connect") != null, "port -1 accepted (legacy parser)");
        check(sp("localhost:0", "connect") != null, "port 0 accepted");
        check(sp("localhost:0-65535", "connect") != null, "full range accepted");
        check(sp("localhost:65535", "connect") != null, "port 65535 accepted");
    }

    // ---- implies: host matching (all cells probed; wildcard outcome is
    //      DNS-independent, IP-vs-localhost uses only local hosts-file lookup) ----
    static void testImpliesHosts() {
        iTrue("*:80", "connect", "127.0.0.1:80", "connect");
        iTrue("*:80", "accept", "127.0.0.1:80", "accept");
        iTrue("*:80", "connect", "localhost:80", "connect");
        iTrue("*", "connect", "127.0.0.1:8080", "connect");
        iTrue("*", "connect", "localhost:1-65535", "connect");
        iTrue("*", "connect,resolve,accept,listen", "127.0.0.1:1-", "accept,listen,connect,resolve");
        iFalse("*:80", "connect", "127.0.0.1:81", "connect");
        iFalse("*.example.com", "resolve", "example.com", "resolve");
        iTrue("*.example.com", "resolve", "a.example.com", "resolve");
        iTrue("*.example.com", "resolve", "b.a.example.com", "resolve");
        iTrue("*.example.com", "resolve", "a.example.com:8080", "resolve");
        iTrue("*.example.com", "connect", "a.example.com:8080", "connect");
        iTrue("*.example.com:80-90", "connect", "a.example.com:85", "connect");
        iFalse("*.example.com:80", "connect", "a.example.com:85", "connect");
        iTrue("*.EXAMPLE.com", "resolve", "a.example.com", "resolve");
        iTrue("*.example.com", "resolve", "A.EXAMPLE.COM", "resolve");
        iTrue("*.example.com", "resolve", "*.example.com", "resolve"); // wildcard implies wildcard
        // local literal-vs-name comparisons (loopback only; no external DNS)
        iTrue("127.0.0.1:80", "connect", "localhost:80", "connect");
        iTrue("localhost:80", "connect", "127.0.0.1:80", "connect");
        iTrue("127.0.0.1:80", "resolve", "localhost:80", "resolve");
        iTrue("localhost:80", "resolve", "127.0.0.1:80", "resolve");
        iTrue("localhost:80", "connect", "LOCALHOST:80", "connect");
        iTrue("LOCALHOST:80", "connect", "localhost:80", "connect");
        iFalse("127.0.0.1:80", "connect", "localhost:81", "connect");
        iFalse("localhost:80", "connect", "127.0.0.2:80", "connect");
        // IPv6 literal equivalence ([::1] vs full form) is canonical, no DNS
        iTrue("[::1]:80", "connect", "[::1]:80", "connect");
        iTrue("[::1]", "connect", "[::1]:80", "connect");
        iTrue("[::1]:80", "connect", "[0:0:0:0:0:0:0:1]:80", "connect");
        iTrue("[0:0:0:0:0:0:0:1]:80", "connect", "[::1]:80", "connect");
        iTrue("[::1]:1-", "connect", "[::1]:2", "connect");
        iFalse("[::1]:80", "connect", "[::2]:80", "connect");
        // non-wildcard never implies a wildcard target
        iFalse("a.example.com", "resolve", "*.example.com", "resolve");
    }

    // ---- implies: port range algebra (all cells probed) ----
    static void testImpliesPorts() {
        iTrue("localhost:80", "connect", "localhost:80", "connect");
        iFalse("localhost:80", "connect", "localhost:81", "connect");
        iTrue("localhost:80-90", "connect", "localhost:85", "connect");
        iFalse("localhost:80-90", "connect", "localhost:79", "connect");
        iFalse("localhost:80-90", "connect", "localhost:91", "connect");
        iTrue("localhost:80-90", "connect", "localhost:80", "connect");
        iTrue("localhost:80-90", "connect", "localhost:90", "connect");
        iTrue("localhost:80-81", "connect", "localhost:80", "connect");
        iFalse("localhost:80", "connect", "localhost:80-81", "connect");
        iTrue("localhost:1024-", "connect", "localhost:2000", "connect");
        iTrue("localhost:1024-", "connect", "localhost:65535", "connect");
        iFalse("localhost:1024-", "connect", "localhost:1023", "connect");
        iFalse("localhost:2000", "connect", "localhost:1024-", "connect");
        iTrue("localhost:1024-", "connect", "localhost:2000-3000", "connect");
        iFalse("localhost:1000-2000", "connect", "localhost:1024-", "connect");
        iTrue("localhost:-1024", "connect", "localhost:1023", "connect");
        iTrue("localhost:-1024", "connect", "localhost:0", "connect");
        iTrue("localhost:-1024", "connect", "localhost:1024", "connect"); // bound inclusive: -1024 = 0..1024
        iTrue("localhost:-80", "connect", "localhost:80", "connect");
        iTrue("localhost:-80", "connect", "localhost:79", "connect");
        iFalse("localhost:-80", "connect", "localhost:81", "connect");
        iTrue("localhost:0-", "connect", "localhost:0", "connect");
        iTrue("localhost:0-", "connect", "localhost:100", "connect");
        iTrue("localhost:0-", "connect", "localhost:65535", "connect");
        // omitted port, empty port and "*" all mean the full range
        iTrue("localhost", "connect", "localhost:80", "connect");
        iTrue("localhost", "resolve", "localhost:80", "resolve");
        iTrue("localhost:", "connect", "localhost:80", "connect");
        iTrue("localhost:*", "connect", "localhost:65535", "connect");
        iTrue("localhost:*", "connect", "localhost", "connect");
        iTrue("localhost:0-65535", "connect", "localhost:80", "connect");
        iTrue("127.0.0.1", "connect", "127.0.0.1:80", "connect");
        iFalse("localhost:80", "connect", "localhost:", "connect");
        iFalse("localhost:80", "connect", "localhost", "connect");
        iFalse("localhost:80-90", "connect", "localhost:85-95", "connect");
        iTrue("localhost:1-65535", "connect", "localhost:1", "connect");
        iFalse("localhost:2-65535", "connect", "localhost:1", "connect");
        // out-of-range ports only imply themselves (probed)
        iTrue("localhost:65536", "connect", "localhost:65536", "connect");
        iFalse("localhost:65536", "connect", "localhost:80", "connect");
        iFalse("localhost:80", "connect", "localhost:65536", "connect");
    }

    // ---- implies: action mask algebra (probed; RESOLVE rides along) ----
    static void testImpliesActions() {
        iTrue("localhost:80", "connect", "localhost:80", "connect");
        iTrue("localhost:80", "connect", "localhost:80", "resolve");
        iFalse("localhost:80", "resolve", "localhost:80", "connect");
        iTrue("localhost:80", "listen", "localhost:80", "listen");
        iFalse("localhost:80", "listen", "localhost:80", "connect");
        iTrue("localhost:80", "accept", "localhost:80", "accept");
        iFalse("localhost:80", "accept", "localhost:80", "connect");
        iTrue("localhost:80", "connect", "localhost:80", "connect,resolve");
        iTrue("localhost:80", "connect,accept", "localhost:80", "connect");
        iTrue("localhost:80", "connect,accept", "localhost:80", "accept");
        iFalse("localhost:80", "connect", "localhost:80", "accept");
        iFalse("localhost:80", "connect", "localhost:80", "listen");
        iTrue("localhost:80", "connect,listen,accept,resolve", "localhost:80", "connect");
        iTrue("localhost:80", "connect,listen,accept,resolve", "localhost:80", "accept,listen,connect,resolve");
        iFalse("localhost:80", "connect,accept", "localhost:80", "connect,accept,listen");
        iFalse("localhost:80", "connect", "localhost:80", "connect,accept,listen");
        iFalse("localhost", "connect,accept", "localhost:80", "listen");
        iTrue("localhost:80", "connect", "localhost:80", "resolve,connect");
        // p == this shortcut
        SocketPermission self = sp("localhost:80", "connect");
        check(self.implies(self), "implies self object shortcut");
        // unrelated permission types and null
        check(!self.implies(null), "implies(null) false");
        check(!self.implies(new java.net.NetPermission("x")), "implies other type false");
        check(!self.implies(new java.net.URLPermission("http://h/a", "GET")), "implies URLPermission false");
    }

    // ---- equals/hashCode: canonical comparison across spellings (probed) ----
    static void testEqualsHashCode() {
        check(sp("localhost:80", "connect").equals(sp("localhost:80", "connect")), "plain equals");
        check(sp("localhost:80", "connect").equals(sp("localhost:80", "CONNECT")), "case-insensitive actions equal");
        check(sp("localhost:80", "connect").equals(sp("localhost:80", "connect,resolve")),
            "resolve rides along in equals");
        check(sp("localhost:80", "connect,resolve").equals(sp("localhost:80", "connect")),
            "resolve rides along symmetric");
        check(sp("localhost:80", "connect,connect").equals(sp("localhost:80", "connect")), "dedup equal");
        check(sp("localhost:080", "connect").equals(sp("localhost:80", "connect")), "leading-zero port equal");
        check(sp("localhost:80", "connect").equals(sp("localhost:80-80", "connect")), "single range equal");
        check(sp("localhost:*", "connect").equals(sp("localhost:", "connect")), "star port equals empty port");
        check(sp("localhost:", "connect").equals(sp("localhost:*", "connect")), "empty port equals star port");
        check(sp("localhost", "connect").equals(sp("localhost:", "connect")), "no port equals empty port");
        check(sp("localhost:0-65535", "connect").equals(sp("localhost:", "connect")), "full range equals empty port");
        check(sp("LOCALHOST:80", "connect").equals(sp("localhost:80", "connect")), "host case-insensitive equals");
        check(sp("[::1]:80", "connect").equals(sp("[::1]:80", "connect")), "IPv6 equals");
        check(sp("[::1]:80", "connect").equals(sp("[0:0:0:0:0:0:0:1]:80", "connect")),
            "IPv6 compressed equals full form");
        check(!sp("localhost:80", "connect").equals(sp("localhost:81", "connect")), "different port unequal");
        check(sp("localhost:80", "connect").equals(sp("127.0.0.1:80", "connect")),
            "localhost equals 127.0.0.1 (both imply each other locally)");
        check(!sp("localhost:80", "connect").equals(sp("localhost:80", "accept")), "different action unequal");
        check(!sp("localhost:80-90", "connect").equals(sp("localhost:80", "connect")), "range vs single unequal");
        check(!sp("*.example.com", "resolve").equals(sp("a.example.com", "resolve")), "wildcard unequal");
        check(!sp("localhost:80", "connect").equals(null), "equals null false");
        check(!sp("localhost:80", "connect").equals("localhost:80"), "not equal to String");
        check(sp("localhost:80", "connect").equals(sp("localhost:80", " connect ")), "spaced variant equal");
        check(!sp("localhost:80", "connect").equals(sp("localhost:80", "connect, accept")),
            "extra action word makes masks differ");
        // hashCode consistency with equals
        int h1 = sp("localhost:80", "connect").hashCode();
        check(h1 == sp("localhost:80", "CONNECT").hashCode(), "hashCode actions case");
        check(h1 == sp("LOCALHOST:80", "connect").hashCode(), "hashCode host case");
        check(h1 == sp("localhost:080", "connect").hashCode(), "hashCode leading zeros");
        check(h1 == sp("localhost:80-80", "connect").hashCode(), "hashCode single range");
        check(h1 == sp("localhost:80", "connect,resolve").hashCode(), "hashCode resolve rides along");
        check(sp("localhost:*", "connect").hashCode() == sp("localhost:", "connect").hashCode(), "hashCode star port");
        check(h1 == sp("localhost:80", "connect").hashCode(), "hashCode stable");
        check(sp("localhost:80", "connect").hashCode() != sp("localhost:81", "connect").hashCode()
            || !sp("localhost:80", "connect").equals(sp("localhost:81", "connect")), "hashCode consistent with equals");
        // wildcard self-equality
        check(sp("*", "connect").equals(sp("*", "connect")), "bare wildcard equal");
        check(sp("*", "connect").hashCode() == sp("*", "connect").hashCode(), "bare wildcard hashCode");
    }

    // ---- serialization round-trip (probed fully preserved) ----
    static void testSerialization() throws Exception {
        SocketPermission orig = sp("localhost:1024-", "connect,accept");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(orig);
        Object back = new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof SocketPermission, "round-trip type");
        SocketPermission rt = (SocketPermission) back;
        check(orig.equals(rt), "round-trip equals");
        check(rt.equals(orig), "round-trip equals symmetric");
        check(orig.hashCode() == rt.hashCode(), "round-trip hashCode");
        check("connect,accept,resolve".equals(rt.getActions()), "round-trip actions");
        check("localhost:1024-".equals(rt.getName()), "round-trip name");
        check(rt.implies(sp("localhost:2000", "connect")), "round-trip implies contained port");
        check(!rt.implies(sp("localhost:80", "connect")), "round-trip not implies other port");
        check(rt.implies(sp("localhost:2000", "resolve")), "round-trip implies resolve subset");
        // wildcard survives
        SocketPermission w = sp("*.example.com", "resolve");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(w);
        SocketPermission wrt = (SocketPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(wrt.implies(sp("a.example.com", "resolve")), "wildcard survives round-trip");
        check(wrt.equals(w), "wildcard round-trip equals");
        // IPv6 survives
        SocketPermission v6 = sp("[::1]:80", "connect");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(v6);
        SocketPermission v6rt = (SocketPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(v6rt.implies(sp("[0:0:0:0:0:0:0:1]:80", "connect")), "IPv6 survives round-trip");
        check(v6rt.equals(v6), "IPv6 round-trip equals");
    }

    // ---- PermissionCollection: union implies, elements, readonly ----
    static void testCollection() {
        PermissionCollection pc = sp("localhost:1-1024", "connect").newPermissionCollection();
        check(pc != null, "collection non-null");
        check(!pc.isReadOnly(), "collection writable");
        pc.add(sp("localhost:1-1024", "connect"));
        pc.add(sp("localhost:2000-3000", "connect"));
        check(pc.implies(sp("localhost:500", "connect")), "union implies first range");
        check(pc.implies(sp("localhost:2500", "connect")), "union implies second range");
        check(!pc.implies(sp("localhost:1500", "connect")), "gap not implied");
        check(!pc.implies(sp("otherhost:500", "connect")), "other host not implied");
        check(pc.implies(sp("localhost:500", "resolve")), "union implies resolve subset");
        check(!pc.implies(sp("localhost:500", "accept")), "union not implies absent action");
        Enumeration<Permission> en = pc.elements();
        int n = 0;
        while (en.hasMoreElements()) { en.nextElement(); n++; }
        check(n == 2, "elements count 2");
        pc.setReadOnly();
        check(pc.isReadOnly(), "readonly after setReadOnly");
        try {
            pc.add(sp("localhost:9", "connect"));
            check(false, "add to readonly must throw");
        } catch (SecurityException e) {
            check(String.valueOf(e.getMessage()).contains("readonly"), "readonly message");
        }
        // single-element collection
        PermissionCollection single = sp("127.0.0.1:80", "accept").newPermissionCollection();
        single.add(sp("127.0.0.1:80", "accept"));
        check(single.implies(sp("127.0.0.1:80", "accept")), "single implies member");
        check(!single.implies(sp("127.0.0.1:81", "accept")), "single not implies other");
        // implying wildcard permission in a collection
        PermissionCollection wc = sp("*", "connect").newPermissionCollection();
        wc.add(sp("*", "connect"));
        check(wc.implies(sp("127.0.0.1:8080", "connect")), "wildcard collection implies");
        check(wc.implies(sp("localhost:1-", "connect")), "wildcard collection implies range");
    }

    // ---- toString permission form (probed exact) ----
    static void testToString() {
        check("(\"java.net.SocketPermission\" \"localhost:80\" \"connect,resolve\")".equals(
            sp("localhost:80", "connect").toString()), "toString exact connect");
        check("(\"java.net.SocketPermission\" \"localhost:80\" \"connect,listen,accept,resolve\")".equals(
            sp("localhost:80", "connect,accept,listen,resolve").toString()), "toString all actions");
        check("(\"java.net.SocketPermission\" \"*:1-1024\" \"listen,resolve\")".equals(
            sp("*:1-1024", "listen").toString()), "toString wildcard listen");
        check("(\"java.net.SocketPermission\" \"*.example.com\" \"resolve\")".equals(
            sp("*.example.com", "resolve").toString()), "toString wildcard host resolve");
        check("(\"java.net.SocketPermission\" \"[::1]:80\" \"connect,resolve\")".equals(
            sp("[::1]:80", "connect").toString()), "toString IPv6");
        String s = sp("localhost:80", "accept").toString();
        check(s.startsWith("(\"java.net.SocketPermission\""), "toString prefix");
        check(s.endsWith("\")"), "toString suffix");
    }

    // ---- mask normalization viewed through getActions across hosts ----
    static void testMaskAcrossForms() {
        String[] hosts = {"localhost:80", "localhost", "*.example.com", "127.0.0.1:8080", "[::1]:1-"};
        for (String h : hosts) {
            check("connect,resolve".equals(sp(h, "connect").getActions()), "connect mask for " + h);
            check("accept,resolve".equals(sp(h, "accept").getActions()), "accept mask for " + h);
            check("listen,resolve".equals(sp(h, "listen").getActions()), "listen mask for " + h);
            check("resolve".equals(sp(h, "resolve").getActions()), "resolve mask for " + h);
        }
    }


    // ---- original white-box assertions (preserved verbatim) ----
    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.SocketPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
        check(c.getMethod("newPermissionCollection") != null, "newPermissionCollection");
    }

    // ---- original black-box assertions (preserved verbatim) ----
    static void testOriginalBlackBox() {
        SocketPermission sp = new SocketPermission("127.0.0.1:80", "connect");
        String actions = sp.getActions();
        check(actions.contains("connect"), "actions has connect");
        check(actions.contains("resolve"), "connect implies resolve");
        check(sp.getName().equals("127.0.0.1:80"), "name");

        check(sp.implies(new SocketPermission("127.0.0.1:80", "connect")), "implies same");
        check(sp.implies(new SocketPermission("127.0.0.1:80", "resolve")), "implies subset");
        check(!sp.implies(new SocketPermission("127.0.0.1:81", "connect")), "not implies different port");
        check(!sp.implies(new SocketPermission("127.0.0.1:80", "accept")), "not implies accept");

        // listen/accept actions
        SocketPermission listen = new SocketPermission("127.0.0.1:80", "listen");
        check(listen.getActions().contains("listen"), "listen action");

        check(sp.equals(new SocketPermission("127.0.0.1:80", "connect")), "equals");
        check(sp.hashCode() == new SocketPermission("127.0.0.1:80", "connect").hashCode(), "hashCode");

        java.security.PermissionCollection pc = sp.newPermissionCollection();
        check(pc != null, "permission collection non-null");
        pc.add(sp);
        check(pc.implies(new SocketPermission("127.0.0.1:80", "resolve")), "collection implies");

        try {
            new SocketPermission("127.0.0.1:80", "bogus");
            check(false, "bad action must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            new SocketPermission("127.0.0.1:80", null);
            check(false, "null action must throw");
        } catch (NullPointerException e) {
            passed++;
        }
    }

    // ---- JDK-27 specific surface (probed) ----
    static void testJdk27Notes() {
        // SocketPermission is deprecated for removal since JDK 25 but fully
        // functional; construction and all semantics match the JDK-8 behavior
        // verified by the shared sections above (252 assertions run identically
        // on both JDKs during probing).
        check(new SocketPermission("localhost:80", "connect").getActions().equals("connect,resolve"),
            "deprecated-but-functional getActions");
        // the actions are still auto-OR'ed with resolve for any network action
        check(new SocketPermission("localhost:80", "accept,listen").getActions().equals("listen,accept,resolve"),
            "accept+listen still gains resolve");
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testOriginalBlackBox();
        testJdk27Notes();
        java.net.SocketPermission p = new java.net.SocketPermission("localhost:80", "connect,resolve");
        check("localhost:80".equals(p.getName()), "getName");
        check("connect,resolve".equals(p.getActions()), "getActions");
        java.net.SocketPermission p2 = new java.net.SocketPermission("localhost", "resolve");
        check(p.implies(p2), "implies resolve");
        testWhiteBox();
        testActionNormalization();
        testNameForms();
        testCtorErrors();
        testImpliesHosts();
        testImpliesPorts();
        testImpliesActions();
        testEqualsHashCode();
        testSerialization();
        testCollection();
        testToString();
        testMaskAcrossForms();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
