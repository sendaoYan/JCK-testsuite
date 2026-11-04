/*
 * @test
 * @summary java.net.URLPermission conformance: name/actions/implies.
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

    // ---- white-box structure (javap -public java.net.URLPermission, JDK-8) ----
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
        // URLPermission.newPermissionCollection() returns null on JDK-8 (probed)
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

    // ---- constructors and default actions (probed on JDK-8) ----
    static void testCtors() {
        check("http://example.com/*".equals(up("http://example.com/*", "GET").getName()),
            "name echoed verbatim");
        check("*:*".equals(new URLPermission("http://example.com/*").getActions()),
            "1-arg ctor defaults to *:*");
        check("GET:".equals(up("http://example.com/*", "GET").getActions()), "GET normalizes to GET:");
        check("GET:Accept".equals(up("http://example.com/a/b", "GET:Accept").getActions()),
            "method:header preserved");
        check("GET:".equals(up("http://example.com/a/b", "GET:").getActions()), "GET: stays GET:");
        // JDK-8 getActions BUG (JDK-8070739 era): separators between methods and
        // between headers are dropped; the canonical separators are absent
        check("GETPOST:".equals(up("http://example.com/a/b", "GET,POST").getActions()),
            "multi methods concatenated without comma (JDK-8)");
        check("GETPOST:X-Y".equals(up("http://example.com/a/b", "get,post:X-Y").getActions()),
            "methods folded to upper case in getActions");
        check("GETPOST:".equals(up("http://example.com/a/b", "GET,,POST").getActions()),
            "empty method word skipped");
        check("GET:BarX-Foo".equals(up("http://example.com/a/b", "GET:Bar,X-Foo").getActions()),
            "multi headers concatenated without comma (JDK-8)");
        // method and header lists are SORTED lexicographically at construction
        // (Collections.sort in init) before getActions concatenates them
        check("DELETEGETPOST:".equals(up("http://h/x", "GET,POST,DELETE").getActions()),
            "methods sorted before concatenation");
        check("AMZ:".equals(up("http://h/x", "Z,A,M").getActions()), "unsorted input becomes sorted output");
        check("GET:X-BarX-Foo".equals(up("http://h/x", "GET:X-Foo,X-Bar").getActions()),
            "headers sorted before concatenation");
        check("GET:ABC".equals(up("http://h/x", "GET:a,b,c").getActions()),
            "header words upper-cased then sorted");
        check("DELETEGETHEADOPTIONSPOSTPUT:".equals(
            up("http://h/x", "GET,POST,DELETE,HEAD,OPTIONS,PUT").getActions()), "six methods sorted");
        check(":".equals(up("http://example.com/a/b", "").getActions()), "empty actions normalize to :");
        check(":".equals(up("http://example.com/a/b", ":").getActions()), "colon-only actions normalize to :");
        check(":Header".equals(up("http://example.com/a/b", ":header").getActions()),
            "header word capitalized when no methods");
        // scheme/host case is preserved in getName
        check("HTTP://EXAMPLE.COM/A".equals(up("HTTP://EXAMPLE.COM/A", "GET").getName()),
            "name keeps original case");
        // query is kept in the name on JDK-8 (probed; JDK-9+ strips it)
        check("http://h:8080/a/b?x=1".equals(up("http://h:8080/a/b?x=1", "GET").getName()),
            "query kept in name (JDK-8)");
    }

    // ---- ctor error paths (probed on JDK-8) ----
    static void testCtorErrors() {
        failsMsg(IllegalArgumentException.class, "White space not allowed in methods",
            "space inside methods", () -> up("http://h/a", "GET : Accept"));
        failsMsg(IllegalArgumentException.class, "White space not allowed in headers",
            "space inside headers", () -> up("http://h/a", "GET:X-Foo X-Bar"));
        failsMsg(IllegalArgumentException.class, "Invalid actions string", "two colons rejected",
            () -> up("http://h/a", "a:b:c"));
        fails(NullPointerException.class, "null url 2-arg", () -> up(null, "GET"));
        fails(NullPointerException.class, "null url 1-arg", () -> new URLPermission(null));
        // no-authority URL: JDK-8 leaks a StringIndexOutOfBoundsException (probed)
        fails(StringIndexOutOfBoundsException.class, "file URL empty authority (JDK-8)", () -> up("file:///tmp/x", "GET"));
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
        // contiguity is over sorted order, not the caller's order: G,P,D sorts to
        // [D,G,P] which contains [D,P] as a non-contiguous pair -> false
        iFalse("http://h/a", "G,P,D", "http://h/a", "P,D");
        iTrue("http://h/a", "A,B,C", "http://h/a", "B,C");
        iTrue("http://h/a", "PUT,POST,DELETE", "http://h/a", "POST,DELETE");
        iTrue("http://h/a", "*", "http://h/a", "*");
        iTrue("http://h/a", "*,GET", "http://h/a", "DELETE");
        // a method-less this-permission cannot imply (probed: empty target implies works)
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
        // JDK-8 keeps the query in the permission name: query must match exactly
        iTrue("http://h/a/b?x=1", "GET", "http://h/a/b?x=1", "GET");
        iFalse("http://h/a/b", "GET", "http://h/a/b?x=1", "GET");
        iFalse("http://h/a/b?x=1", "GET", "http://h/a/b", "GET");
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
        check(up("http://h/a", "GET").equals(up("http://h/a", "GET")) , "reflexive");
        // wildcard URLs
        check(up("http://h/*", "GET").equals(up("http://h/*", "GET")), "wildcard equal");
        check(!up("http://h/*", "GET").equals(up("http://h/-", "GET")), "different wildcards unequal");
        check(up("http://h/-", "GET").hashCode() == up("http://h/-", "GET").hashCode(), "wildcard hashCode");
        // header order does not matter
        check(up("http://h/a", "GET:X-Foo,X-Bar").equals(up("http://h/a", "GET:X-Bar,X-Foo")),
            "header order equal");
    }

    // ---- serialization: single-method round-trip exact; multi-method round-trip
    //      corrupts implies on JDK-8 (actions string is written comma-less) ----
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
        check(mrt.equals(multi), "multi round-trip still equals fresh (list semantics survive)");
        // JDK-8 BUG (probed): the deserialized multi-method permission no longer
        // implies its subset, because the serialized action string "GETPOST:Accept"
        // re-parses as ONE method named GETPOST
        check(!mrt.implies(up("http://h/a/b", "POST")),
            "multi round-trip loses per-method implies (JDK-8 bug)");
        check(!mrt.implies(up("http://h/a/b", "GET")),
            "multi round-trip loses GET too (JDK-8 bug)");
        // single-method + no-header permission round-trips cleanly
        URLPermission solo = up("http://h/*", "GET");
        bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(solo);
        URLPermission srt = (URLPermission) new java.io.ObjectInputStream(
            new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(srt.implies(up("http://h/x", "GET")), "single round-trip wildcard implies");
        check(srt.equals(solo), "single round-trip wildcard equals");
    }

    // ---- JDK-8 specific quirks (probed; JDK-9+ differs, see DESIGN.md) ----
    static void testJdk8Quirks() {
        // empty/colon-only method lists make implies() blow up with an
        // IndexOutOfBoundsException instead of returning false (JDK-8 bug)
        try {
            up("http://h/a", "").implies(up("http://h/a", "GET"));
            check(false, "empty-actions implies must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            passed++;
        }
        try {
            up("http://h/a", ":").implies(up("http://h/a", "GET"));
            check(false, "colon-only implies must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            passed++;
        }
        try {
            up("http://h/a", ":Header").implies(up("http://h/a", "GET:Header"));
            check(false, "method-less implies must throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException e) {
            passed++;
        }
        // file URLs (empty authority) leak a raw StringIndexOutOfBoundsException (JDK-8 bug)
        try {
            new URLPermission("file:///tmp/x", "GET");
            check(false, "file URL must throw");
        } catch (StringIndexOutOfBoundsException e) {
            passed++;
        } catch (IllegalArgumentException e) {
            check(false, "JDK-8 leaks StringIndexOutOfBoundsException for empty authority");
        }
    }

    public static void main(String[] args) throws Exception {
        java.net.URLPermission p = new java.net.URLPermission("http://example.com/*");
        check("http://example.com/*".equals(p.getName()), "getName");
        check("*:*".equals(p.getActions()), "getActions default");
        java.net.URLPermission g = new java.net.URLPermission("http://example.com/*", "GET");
        check("GET:".equals(g.getActions()), "getActions GET");
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
        testJdk8Quirks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
