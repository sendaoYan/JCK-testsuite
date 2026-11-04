/*
 * @test
 * @summary java.net.URLPermission conformance: name/actions/implies (JDK-27; class deprecated for removal since 25 but functional).
 * @run main URLPermissionTest
 */

import java.net.URLPermission;

public class URLPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static URLPermission up(String url, String act) { return new URLPermission(url, act); }
    static void i(String u1, String a1, String u2, String a2, boolean want) {
        boolean got = up(u1, a1).implies(up(u2, a2));
        check(got == want, "implies([" + u1 + "|" + a1 + "] -> [" + u2 + "|" + a2 + "]) expected "
            + want + " got " + got);
    }
    static void iTrue(String u1, String a1, String u2, String a2) { i(u1, a1, u2, a2, true); }
    static void iFalse(String u1, String a1, String u2, String a2) { i(u1, a1, u2, a2, false); }
    interface ThrowR { void run() throws Exception; }
    static void failsMsg(Class<? extends Throwable> ex, String needle, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t) && String.valueOf(t.getMessage()).contains(needle)) { passed++; return; }
            throw new AssertionError(m + " wrong exception/message " + t);
        }
        throw new AssertionError(m + " no exception");
    }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    // ---- original white-box assertions (preserved verbatim) ----
    static void testOriginalWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
    }

    // ---- original black-box assertions (preserved verbatim) ----
    static void testOriginalBlackBox() {
        URLPermission exact = new URLPermission("http://example.com/path", "GET:Accept");
        check(exact.getActions().equals("GET:Accept"), "actions normalized");
        check(exact.implies(new URLPermission("http://example.com/path", "GET:Accept")), "implies self");
        check(!exact.implies(new URLPermission("http://example.com/path/deeper", "GET:Accept")), "exact path not imply deeper");
        check(!exact.implies(new URLPermission("http://example.com/other", "GET:Accept")), "not imply other path");

        // single-arg constructor => wildcard methods and headers
        URLPermission any = new URLPermission("http://example.com/path");
        check(any.getActions().equals("*:*"), "single-arg wildcard actions");

        // "/-" suffix: recursive prefix match
        URLPermission prefix = new URLPermission("http://example.com/path/-", "GET:Accept");
        check(prefix.implies(new URLPermission("http://example.com/path/deeper", "GET:Accept")), "/- implies deeper");
        check(!prefix.implies(new URLPermission("http://example.com/path", "GET:Accept")), "/- not imply exact (no slash)");

        // "/*" suffix: immediate children only
        URLPermission star = new URLPermission("http://example.com/path/*", "GET:Accept");
        check(star.implies(new URLPermission("http://example.com/path/child", "GET:Accept")), "/* implies child");
        check(!star.implies(new URLPermission("http://example.com/path/deeper/x", "GET:Accept")), "/* not imply nested");

        check(exact.equals(new URLPermission("http://example.com/path", "GET:Accept")), "equals");
        check(exact.hashCode() == new URLPermission("http://example.com/path", "GET:Accept").hashCode(), "hashCode");
        check(!exact.equals(prefix), "not equals different path");

        try {
            new URLPermission("example.com/path");
            check(false, "no scheme must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            new URLPermission("not a url");
            check(false, "bad url must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
    }

    // ---- white-box structure (javap -public java.net.URLPermission, JDK-27) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URLPermission");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final");
        check(java.security.Permission.class.isAssignableFrom(c), "extends Permission");
        check(c.getSuperclass() == java.security.Permission.class, "direct Permission subclass");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(c.getConstructor(String.class) != null, "ctor(String)");
        check(c.getConstructor(String.class, String.class) != null, "ctor(String,String)");
        check(c.getMethod("getActions") != null, "getActions");
        check(c.getMethod("implies", java.security.Permission.class) != null, "implies");
        check(c.getMethod("equals", Object.class) != null, "equals");
        check(c.getMethod("hashCode") != null, "hashCode");
        check(c.getMethod("newPermissionCollection") != null, "newPermissionCollection declared");
        // URLPermission.newPermissionCollection() returns null (probed, both JDKs)
        check(up("http://h/a", "GET").newPermissionCollection() == null,
            "newPermissionCollection returns null");
        // no getMethodNames/getHeaderNames/getURL accessors exist (never added)
        boolean absent = false;
        try { c.getMethod("getMethodNames"); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "getMethodNames absent");
        absent = false;
        try { c.getMethod("getHeaderNames"); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "getHeaderNames absent");
        absent = false;
        try { c.getMethod("getURL"); } catch (NoSuchMethodException e) { absent = true; }
        check(absent, "getURL absent");
    }

    // ---- constructors and default actions (probed on JDK-27) ----
    static void testCtors() {
        check("http://example.com/*".equals(up("http://example.com/*", "GET").getName()),
            "name echoed verbatim");
        check("*:*".equals(new URLPermission("http://example.com/*").getActions()),
            "1-arg ctor defaults to *:*");
        check("GET:".equals(up("http://example.com/*", "GET").getActions()), "GET normalizes to GET:");
        check("GET:Accept".equals(up("http://example.com/a/b", "GET:Accept").getActions()),
            "method:header preserved");
        check("GET:".equals(up("http://example.com/a/b", "GET:").getActions()), "GET: stays GET:");
        // JDK-9+ fixed JDK-8070739: methods and headers are comma-separated in getActions
        check("GET,POST:".equals(up("http://example.com/a/b", "GET,POST").getActions()),
            "multi methods comma-separated (JDK-9+)");
        check("GET,POST:X-Y".equals(up("http://example.com/a/b", "get,post:X-Y").getActions()),
            "methods folded to upper case in getActions");
        check("GET,POST:".equals(up("http://example.com/a/b", "GET,,POST").getActions()),
            "empty method word skipped");
        check("GET:Bar,X-Foo".equals(up("http://example.com/a/b", "GET:Bar,X-Foo").getActions()),
            "multi headers comma-separated (JDK-9+)");
        check(":".equals(up("http://example.com/a/b", "").getActions()), "empty actions normalize to :");
        check(":".equals(up("http://example.com/a/b", ":").getActions()), "colon-only actions normalize to :");
        check(":Header".equals(up("http://example.com/a/b", ":header").getActions()),
            "header word capitalized when no methods");
        // method and header lists are SORTED lexicographically at construction
        // (Collections.sort in init) before getActions joins them
        check("DELETE,GET,POST:".equals(up("http://h/x", "GET,POST,DELETE").getActions()),
            "methods sorted before joining");
        check("A,M,Z:".equals(up("http://h/x", "Z,A,M").getActions()), "unsorted input becomes sorted output");
        check("GET:X-Bar,X-Foo".equals(up("http://h/x", "GET:X-Foo,X-Bar").getActions()),
            "headers sorted before joining");
        check("GET:A,B,C".equals(up("http://h/x", "GET:a,b,c").getActions()),
            "header words upper-cased then sorted");
        check("DELETE,GET,HEAD,OPTIONS,POST,PUT:".equals(
            up("http://h/x", "GET,POST,DELETE,HEAD,OPTIONS,PUT").getActions()), "six methods sorted");
        // scheme/host case is preserved in getName
        check("HTTP://EXAMPLE.COM/A".equals(up("HTTP://EXAMPLE.COM/A", "GET").getName()),
            "name keeps original case");
        // query is STRIPPED from the stored name on JDK-9+ (probed; JDK-8 keeps it)
        check("http://h:8080/a/b".equals(up("http://h:8080/a/b?x=1", "GET").getName()),
            "query stripped from name (JDK-9+)");
    }

    // ---- ctor error paths (probed on JDK-27) ----
    static void testCtorErrors() {
        failsMsg(IllegalArgumentException.class, "White space not allowed in methods",
            "space inside methods", () -> up("http://h/a", "GET : Accept"));
        failsMsg(IllegalArgumentException.class, "White space not allowed in headers",
            "space inside headers", () -> up("http://h/a", "GET:X-Foo X-Bar"));
        failsMsg(IllegalArgumentException.class, "Invalid actions string", "two colons rejected",
            () -> up("http://h/a", "a:b:c"));
        fails(NullPointerException.class, "null url 2-arg", () -> up(null, "GET"));
        fails(NullPointerException.class, "null url 1-arg", () -> new URLPermission(null));
        // no-authority URL: JDK-9+ reports a proper IllegalArgumentException
        // (JDK-8 leaked a StringIndexOutOfBoundsException here)
        failsMsg(IllegalArgumentException.class, "Invalid URL: authority is empty",
            "file URL empty authority (JDK-9+)", () -> up("file:///tmp/x", "GET"));
        // a URL with no scheme is rejected
        fails(IllegalArgumentException.class, "no scheme", () -> up("example.com/a", "GET"));
    }

    // ---- implies: method-list algebra. Both lists are SORTED at construction and
    //      implies() requires the target list to be a CONTIGUOUS sub-list
    //      (Collections.indexOfSubList) of this sorted list, unless this[0]=="*" ----
    static void testImpliesMethods() {
        iTrue("http://h/a", "GET", "http://h/a", "GET");
        iFalse("http://h/a", "GET", "http://h/a", "POST");
        iTrue("http://h/a", "GET,POST", "http://h/a", "POST");
        iTrue("http://h/a", "GET,POST", "http://h/a", "GET");
        iFalse("http://h/a", "GET", "http://h/a", "GET,POST");
        iTrue("http://h/a", "*", "http://h/a", "DELETE");
        iTrue("http://h/a", "*", "http://h/a", "GET,POST");
        iTrue("http://h/a", "get", "http://h/a", "GET");
        iTrue("http://h/a", "GET", "http://h/a", "get");
        iTrue("http://h/a", "GET,POST,DELETE", "http://h/a", "DELETE");
        iTrue("http://h/a", "GET,POST,DELETE", "http://h/a", "GET");
        iTrue("http://h/a", "GET,POST,DELETE", "http://h/a", "GET,POST");
        iTrue("http://h/a", "GET,POST,DELETE", "http://h/a", "POST,DELETE,GET");
        // sorted [DELETE,GET,POST] does not contain the contiguous run [DELETE,POST]
        iFalse("http://h/a", "GET,POST,DELETE", "http://h/a", "POST,DELETE");
        iFalse("http://h/a", "POST,DELETE", "http://h/a", "GET,POST");
        // contiguity is over sorted order, not the caller's order
        iFalse("http://h/a", "G,P,D", "http://h/a", "P,D");
        iTrue("http://h/a", "A,B,C", "http://h/a", "B,C");
        iTrue("http://h/a", "PUT,POST,DELETE", "http://h/a", "POST,DELETE");
        iTrue("http://h/a", "*", "http://h/a", "*");
        iTrue("http://h/a", "*,GET", "http://h/a", "DELETE");
        // empty target method list is implied by anything (probed on JDK-27)
        iTrue("http://h/a", "GET", "http://h/a", "");
        iTrue("http://h/a", "*", "http://h/a", "");
    }

    // ---- implies: path algebra (probed: "/-" recursive, "/*" single level) ----
    static void testImpliesPaths() {
        iTrue("http://h/a/b", "GET", "http://h/a/b", "GET");
        iFalse("http://h/a/b", "GET", "http://h/a/b/c", "GET");
        iFalse("http://h/a", "GET", "http://h/ab", "GET");
        iFalse("http://h/A", "GET", "http://h/a", "GET");
        iTrue("http://h/a/*", "GET", "http://h/a/b", "GET");
        iTrue("http://h/a/*", "GET", "http://h/a/", "GET");
        iFalse("http://h/a/*", "GET", "http://h/a/b/c", "GET");
        iFalse("http://h/a/*", "GET", "http://h/a", "GET");
        iTrue("http://h/*", "GET", "http://h/x", "GET");
        iTrue("http://h/*", "GET", "http://h/", "GET");
        iFalse("http://h/*", "GET", "http://h/anything/here", "GET");
        iTrue("http://h/-", "GET", "http://h/a", "GET");
        iTrue("http://h/-", "GET", "http://h/", "GET");
        iTrue("http://h/-", "GET", "http://h/a/b?x=1", "GET");
        iTrue("http://h/a/-", "GET", "http://h/a/b/c", "GET");
        iFalse("http://h/a/-", "GET", "http://h/a", "GET");
        iFalse("http://h/a/-", "GET", "http://h/ab", "GET");
        // JDK-9+ strips the query before comparing: query-insensitive both ways
        // (JDK-8 kept the query in the name and required an exact match)
        iTrue("http://h/a/b?x=1", "GET", "http://h/a/b?x=1", "GET");
        iTrue("http://h/a/b", "GET", "http://h/a/b?x=1", "GET");
        iTrue("http://h/a/b?x=1", "GET", "http://h/a/b", "GET");
        iTrue("http://h/a/b?x=1", "GET", "http://h/a/b?y=2", "GET");
    }

    // ---- implies: scheme, host, port (probed) ----
    static void testImpliesAuthority() {
        iFalse("http://h/a", "GET", "https://h/a", "GET");
        iFalse("https://h/a", "GET", "http://h/a", "GET");
        iTrue("http://h:80/a", "GET", "http://h/a", "GET");
        iTrue("http://h/a", "GET", "http://h:80/a", "GET");
        iFalse("http://h:8080/a", "GET", "http://h/a", "GET");
        iFalse("http://h/a", "GET", "http://h:8080/a", "GET");
        iTrue("http://H/a", "GET", "http://h/a", "GET");
        iTrue("http://h/a", "GET", "http://H/a", "GET");
        iFalse("http://h/a", "GET", "http://other/a", "GET");
        iTrue("http://*.example.com/a", "GET", "http://b.example.com/a", "GET");
        iTrue("http://*.example.com/a", "GET", "http://a.b.example.com/a", "GET");
        iFalse("http://*.example.com/a", "GET", "http://example.com/a", "GET");
        iTrue("http://h/a", "GET", "http://h/a", "GET"); // sanity anchor
    }

    // ---- implies: header algebra (probed: whole-value "*" only; case-insensitive) ----
    static void testImpliesHeaders() {
        iTrue("http://h/a", "GET:*", "http://h/a", "GET:X-Foo");
        iTrue("http://h/a", "GET:*", "http://h/a", "GET:X-Foo,X-Bar");
        iTrue("http://h/a", "GET:X-Foo", "http://h/a", "GET:X-Foo");
        iTrue("http://h/a", "GET:X-Foo", "http://h/a", "GET:x-foo");
        iTrue("http://h/a", "GET:x-foo", "http://h/a", "GET:X-Foo");
        iFalse("http://h/a", "GET:X-Foo", "http://h/a", "GET:X-Foo,X-Bar");
        iTrue("http://h/a", "GET:X-Foo,X-Bar", "http://h/a", "GET:X-Foo");
        iFalse("http://h/a", "GET:X-*", "http://h/a", "GET:X-Foo");
        iFalse("http://h/a", "GET:X-Foo*", "http://h/a", "GET:X-FooBar");
        iTrue("http://h/a", "GET:X-Foo", "http://h/a", "GET:");
        iTrue("http://h/a", "GET", "http://h/a", "GET:");
        iFalse("http://h/a", "GET:", "http://h/a", "GET:X-Foo");
        iTrue("http://h/a", "GET:X-Foo,*", "http://h/a", "GET:X-Foo");
        iTrue("http://h/a", "GET:X-Foo,*", "http://h/a", "GET:X-Bar");
        iTrue("http://h/a", "GET:*", "http://h/a", "GET");
    }

    // ---- implies: cross-type / null / self (probed) ----
    static void testImpliesMisc() {
        URLPermission u = up("http://h/a", "*:*");
        check(u.implies(u), "implies self");
        check(!u.implies(null), "implies(null) false");
        check(!u.implies(new java.net.SocketPermission("localhost:80", "connect")),
            "implies other permission type false");
        // *:* implies every method and every header set on the same URL
        check(u.implies(up("http://h/a", "GET:Accept")), "*:* implies GET:Accept");
        check(u.implies(up("http://h/a", "DELETE:X-Foo,X-Bar")), "*:* implies DELETE with headers");
    }

    // ---- equals/hashCode (probed) ----
    static void testEqualsHashCode() {
        check(up("http://h/a", "GET").equals(up("http://h/a", "GET")), "plain equal");
        check(up("http://h/a", "GET").equals(up("http://h/a", "get")), "method case equal");
        check(up("http://h/a", "GET,POST").equals(up("http://h/a", "POST,GET")), "method order equal");
        check(up("HTTP://H/A", "GET").equals(up("http://h/A", "GET")), "scheme/host case equal");
        check(up("HTTP://H/A", "GET").hashCode() == up("http://h/A", "GET").hashCode(),
            "scheme/host case hashCode");
        check(!up("http://h/a", "GET").equals(up("http://h/a", "GET:Accept")),
            "header set difference unequal");
        check(!up("http://h/a", "GET").equals(up("http://h/b", "GET")), "path difference unequal");
        check(!up("http://h/a", "GET").equals(up("https://h/a", "GET")), "scheme difference unequal");
        check(!up("http://h/a", "GET").equals(up("http://h:8080/a", "GET")), "port difference unequal");
        check(!up("http://h/a", "GET").equals(null), "equals null false");
        check(!up("http://h/a", "GET").equals("http://h/a"), "not equal to String");
        check(up("http://h/a", "GET").equals(up("http://h:80/a", "GET")), "default port 80 equal");
        check(up("http://h/a", "GET").hashCode() == up("http://h:80/a", "GET").hashCode(),
            "default port 80 hashCode");
        check(up("http://h/a", "GET,POST").hashCode() == up("http://h/a", "POST,GET").hashCode(),
            "method order hashCode");
        check(up("http://h/a", "GET").equals(up("http://h/a", "GET")), "reflexive");
        // wildcard URLs
        check(up("http://h/*", "GET").equals(up("http://h/*", "GET")), "wildcard equal");
        check(!up("http://h/*", "GET").equals(up("http://h/-", "GET")), "different wildcards unequal");
        check(up("http://h/-", "GET").hashCode() == up("http://h/-", "GET").hashCode(), "wildcard hashCode");
        // header order does not matter
        check(up("http://h/a", "GET:X-Foo,X-Bar").equals(up("http://h/a", "GET:X-Bar,X-Foo")),
            "header order equal");
        // query is stripped from the name, so a query difference is NOT visible (JDK-9+)
        check(up("http://h/a/b?x=1", "GET").equals(up("http://h/a/b", "GET")),
            "query stripped: equals ignores query (JDK-9+)");
    }

    // ---- serialization: exact round-trip for single AND multi method lists
    //      (JDK-9+ fixed the JDK-8 comma-loss corruption) ----
    static void testSerialization() throws Exception {
        URLPermission orig = up("http://h/a/b", "GET:Accept");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(orig);
        URLPermission rt = (URLPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(rt.equals(orig), "single-method round-trip equals");
        check(orig.equals(rt), "single-method round-trip equals symmetric");
        check(orig.hashCode() == rt.hashCode(), "round-trip hashCode");
        check("http://h/a/b".equals(rt.getName()), "round-trip name");
        check("GET:Accept".equals(rt.getActions()), "round-trip actions");
        check(rt.implies(up("http://h/a/b", "GET:Accept")), "round-trip implies self target");

        URLPermission multi = up("http://h/a/b", "GET,POST:Accept");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(multi);
        URLPermission mrt = (URLPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(mrt.equals(multi), "multi round-trip equals fresh");
        check("GET,POST:Accept".equals(mrt.getActions()), "multi round-trip actions exact (JDK-9+)");
        check(mrt.implies(up("http://h/a/b", "POST")),
            "multi round-trip keeps per-method implies (JDK-9+ fixed JDK-8 corruption)");
        check(mrt.implies(up("http://h/a/b", "GET")), "multi round-trip keeps GET implies");
        // single-method + no-header permission round-trips cleanly
        URLPermission solo = up("http://h/*", "GET");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(solo);
        URLPermission srt = (URLPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(srt.implies(up("http://h/x", "GET")), "single round-trip wildcard implies");
        check(srt.equals(solo), "single round-trip wildcard equals");
    }

    // ---- JDK-27 specific behavior (probed; JDK-8 differs, see DESIGN.md) ----
    static void testJdk27Specific() {
        // empty/colon-only method lists no longer blow up with
        // IndexOutOfBoundsException (the JDK-8 bug): implies simply returns false
        check(!up("http://h/a", "").implies(up("http://h/a", "GET")),
            "empty-actions implies returns false (no IOOBE on JDK-9+)");
        check(!up("http://h/a", ":").implies(up("http://h/a", "GET")),
            "colon-only implies returns false");
        check(!up("http://h/a", ":Header").implies(up("http://h/a", "GET:Header")),
            "method-less implies returns false");
        // a method-less permission still implies the empty target
        check(up("http://h/a", ":").implies(up("http://h/a", "")),
            "method-less implies empty target true");
    }

    public static void main(String[] args) throws Exception {
        testOriginalWhiteBox();
        testOriginalBlackBox();
        testWhiteBox();
        testCtors();
        testCtorErrors();
        testImpliesMethods();
        testImpliesPaths();
        testImpliesAuthority();
        testImpliesHeaders();
        testImpliesMisc();
        testEqualsHashCode();
        testSerialization();
        testJdk27Specific();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
