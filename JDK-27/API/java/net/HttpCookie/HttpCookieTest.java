/*
 * @test
 * @summary java.net.HttpCookie conformance: white-box structure + black-box behavior.
 * @run main HttpCookieTest
 */

import java.lang.reflect.*;
import java.net.*;
import java.util.*;

public class HttpCookieTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Th { void run() throws Throwable; }
    // Exact-type (+optionally exact-message) negative helper. Counts one assertion on success.
    static void ex(Th r, String type, String msg, String label) {
        try { r.run(); throw new AssertionError(label + ": no exception thrown"); }
        catch (Throwable t) {
            if (!type.equals(t.getClass().getName()))
                throw new AssertionError(label + ": wrong type " + t);
            if (msg != null && !msg.equals(t.getMessage()))
                throw new AssertionError(label + ": wrong msg " + t.getMessage());
            passed++;
        }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.HttpCookie");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(Cloneable.class.isAssignableFrom(c), "Cloneable");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        check(c.getConstructor(String.class, String.class) != null, "ctor(name,value)");
        check(c.getMethod("parse", String.class) != null, "parse");
        check(c.getMethod("domainMatches", String.class, String.class) != null, "domainMatches");
        check(c.getMethod("getMaxAge") != null, "getMaxAge");
        check(c.getMethod("setMaxAge", long.class) != null, "setMaxAge");
        // parse(String) is the only public overload (no parse(List) exists in the public API)
        int overloads = 0;
        for (Method m : c.getMethods()) if (m.getName().equals("parse")) overloads++;
        check(overloads == 1, "single parse overload");
    }

    static void testBlackBox() throws Exception {
        HttpCookie c = new HttpCookie("k", "v");
        check(c.getName().equals("k"), "name");
        check(c.getValue().equals("v"), "value");
        check(c.getMaxAge() == -1, "default maxAge -1");
        check(c.getVersion() == 1, "default version 1");
        check(c.getPath() == null, "default path null");
        check(c.getDomain() == null, "default domain null");
        check(!c.hasExpired(), "default not expired");

        // max-age lifecycle
        c.setMaxAge(3600);
        check(c.getMaxAge() == 3600, "setMaxAge");
        check(!c.hasExpired(), "positive maxAge not expired");
        c.setMaxAge(0);
        check(c.getMaxAge() == 0, "setMaxAge 0");
        check(c.hasExpired(), "maxAge 0 expired");
        c.setMaxAge(-1);
        check(c.getMaxAge() == -1 && !c.hasExpired(), "maxAge -1 reset");

        // attribute setters/getters
        HttpCookie d = new HttpCookie("a", "b");
        d.setPath("/p"); d.setDomain("example.com"); d.setSecure(true);
        d.setHttpOnly(true); d.setComment("cmt"); d.setCommentURL("http://c/");
        d.setVersion(0); d.setPortlist("80,8080");
        check(d.getPath().equals("/p"), "path");
        check(d.getDomain().equals("example.com"), "domain");
        check(d.getSecure(), "secure");
        check(d.isHttpOnly(), "httpOnly");
        check(d.getComment().equals("cmt"), "comment");
        check(d.getCommentURL().equals("http://c/"), "commentURL");
        check(d.getVersion() == 0, "version");
        check(d.getPortlist().equals("80,8080"), "portlist");

        // toString / clone
        check(new HttpCookie("k", "v").toString().equals("k=\"v\""), "toString v1");
        HttpCookie orig = new HttpCookie("k", "v");
        HttpCookie cl = (HttpCookie) orig.clone();
        check(cl.equals(orig), "clone equal");
        cl.setValue("v2");
        check(orig.getValue().equals("v"), "clone independent value");

        // equals: name (ci) + domain (ci) + path (cs); ignores value
        HttpCookie e1 = new HttpCookie("N", "1"); e1.setDomain("EXAMPLE.com"); e1.setPath("/x");
        HttpCookie e2 = new HttpCookie("n", "2"); e2.setDomain("example.com"); e2.setPath("/x");
        check(e1.equals(e2), "equals ignores value, ci name/domain");
        e2.setPath("/y");
        check(!e1.equals(e2), "not equals different path");

        // parse
        List<HttpCookie> l = HttpCookie.parse("a=b; Path=/; Max-Age=3600");
        check(l.size() == 1, "parse single cookie");
        check(l.get(0).getName().equals("a") && l.get(0).getValue().equals("b"), "parsed name/value");
        check(l.get(0).getPath().equals("/"), "parsed path");
        check(l.get(0).getMaxAge() == 3600, "parsed maxAge");
        check(HttpCookie.parse("a=b; c=d").size() == 1, "semicolon is attribute separator");

        // domainMatches
        check(HttpCookie.domainMatches(".example.com", "www.example.com"), "domainMatches subdomain");
        check(HttpCookie.domainMatches("example.com", "example.com"), "domainMatches exact");
        check(!HttpCookie.domainMatches("foo.com", "bar.com"), "domainMatches mismatch");
        check(!HttpCookie.domainMatches(null, "x"), "domainMatches null domain");
    }

    // ===================== deepened contract tests (batch D) =====================

    static void testDefaultsMatrix() {
        HttpCookie c = new HttpCookie("n", "v");
        check(c.getName().equals("n"), "def name");
        check(c.getValue().equals("v"), "def value");
        check(c.getVersion() == 1, "def version 1");
        check(c.getComment() == null, "def comment null");
        check(c.getCommentURL() == null, "def commentURL null");
        check(c.getDomain() == null, "def domain null");
        check(c.getPortlist() == null, "def portlist null");
        check(!c.getDiscard(), "def discard false");
        check(!c.getSecure(), "def secure false");
        check(!c.isHttpOnly(), "def httpOnly false");
        check(c.getMaxAge() == -1, "def maxAge -1");
        check(c.getPath() == null, "def path null");
        check(!c.hasExpired(), "def not expired");
        check(c.hashCode() == 110, "def hashCode 110 (name lc only)");
        check(c.toString().equals("n=\"v\""), "def toString v1 quoted");
    }

    static void testCtorMatrix() {
        // null name -> NPE; message text differs JDK-8 vs JDK-27, assert type only
        ex(() -> new HttpCookie(null, "v"), "java.lang.NullPointerException", null, "ctor null name");
        // illegal names -> IAE "Illegal cookie name"
        // (tspecials = ",; " + control chars <0x20/>=0x7f + leading '$', probe/source verified)
        String[] badNames = {"", " ", "a b", "a;b", "a,b", "a\tb", "中文", "$x", "$Version", "a\u0001b", "a\u007fb"};
        for (String n : badNames)
            ex(() -> new HttpCookie(n, "v"), "java.lang.IllegalArgumentException", "Illegal cookie name",
                "ctor illegal name");
        // legal names: chars outside ",; " and ASCII range are all accepted
        String[] okNames = {"a-b", "a_b", "a.b", "a~b", "a@b", "a:b", "a=b", "a[b", "a]b", "a{b", "a}b",
            "a\"b", "a\\b", "A1", "x$y"};
        for (String n : okNames) {
            HttpCookie c = new HttpCookie(n, "v");
            check(c.getName().equals(n), "legal name roundtrip");
        }
        // name is trimmed
        check(new HttpCookie("  n  ", "v").getName().equals("n"), "ctor trims name");
        // value is unconstrained: null, empty, separators all legal
        HttpCookie nv = new HttpCookie("n", null);
        check(nv.getValue() == null, "ctor null value -> getValue null");
        check(nv.toString().equals("n=\"null\""), "null value stringifies as null");
        HttpCookie ev = new HttpCookie("n", "");
        check(ev.getValue().isEmpty(), "empty value legal");
        for (String v : new String[] {"v;w", "v,w", "v w", "v\tw", "\"q\""})
            check(new HttpCookie("n", v).getValue().equals(v), "value roundtrip [" + v + "]");
    }

    static void testSetValue() {
        HttpCookie c = new HttpCookie("n", "v");
        c.setValue("w");
        check(c.getValue().equals("w"), "setValue roundtrip");
        c.setValue(null);
        check(c.getValue() == null, "setValue null legal");
        check(c.toString().equals("n=\"null\""), "setValue null toString");
        c.setValue("");
        check(c.getValue().isEmpty(), "setValue empty legal");
        c.setValue("v");
        check(c.getValue().equals("v"), "setValue restore");
        // changing value does not change equals identity (name/domain/path only)
        HttpCookie a = new HttpCookie("n", "1"); a.setDomain("example.com"); a.setPath("/");
        HttpCookie b = new HttpCookie("n", "2"); b.setDomain("example.com"); b.setPath("/");
        check(a.equals(b), "equals ignores value after setValue");
    }

    static void testVersionMatrix() {
        HttpCookie c = new HttpCookie("n", "v");
        c.setVersion(0);
        check(c.getVersion() == 0, "setVersion 0");
        c.setVersion(1);
        check(c.getVersion() == 1, "setVersion 1");
        ex(() -> new HttpCookie("n", "v").setVersion(-1), "java.lang.IllegalArgumentException",
            "cookie version should be 0 or 1", "setVersion -1");
        ex(() -> new HttpCookie("n", "v").setVersion(2), "java.lang.IllegalArgumentException",
            "cookie version should be 0 or 1", "setVersion 2");
    }

    static void testAttrRoundTrip() {
        HttpCookie c = new HttpCookie("n", "v");
        c.setComment("c");
        check(c.getComment().equals("c"), "comment set");
        c.setComment(null);
        check(c.getComment() == null, "comment null reset");
        c.setCommentURL("http://u/");
        check(c.getCommentURL().equals("http://u/"), "commentURL set");
        c.setCommentURL(null);
        check(c.getCommentURL() == null, "commentURL null reset");
        c.setDiscard(true);
        check(c.getDiscard(), "discard true");
        c.setDiscard(false);
        check(!c.getDiscard(), "discard false");
        c.setPortlist("80,8080");
        check(c.getPortlist().equals("80,8080"), "portlist set");
        c.setPortlist(null);
        check(c.getPortlist() == null, "portlist null reset");
        c.setSecure(true);
        check(c.getSecure(), "secure true");
        c.setSecure(false);
        check(!c.getSecure(), "secure false");
        c.setHttpOnly(true);
        check(c.isHttpOnly(), "httpOnly true");
        c.setHttpOnly(false);
        check(!c.isHttpOnly(), "httpOnly false");
        c.setPath("/p");
        check(c.getPath().equals("/p"), "path set");
        c.setPath(null);
        check(c.getPath() == null, "path null reset");
        // setDomain normalizes to lowercase (probe + source verified: domain.toLowerCase())
        c.setDomain(".EXAMPLE.com");
        check(c.getDomain().equals(".example.com"), "setDomain lowercases");
        c.setDomain(null);
        check(c.getDomain() == null, "domain null reset");
        // comment attributes do not affect toString or equals
        HttpCookie a = new HttpCookie("n", "v"); a.setComment("x");
        HttpCookie b = new HttpCookie("n", "v");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "comment not in identity");
    }

    static void testMaxAgeExpiry() {
        HttpCookie c = new HttpCookie("n", "v");
        c.setMaxAge(3600);
        check(c.getMaxAge() == 3600 && !c.hasExpired(), "maxAge positive not expired");
        c.setMaxAge(0);
        check(c.getMaxAge() == 0 && c.hasExpired(), "maxAge 0 expired");
        c.setMaxAge(-1);
        check(c.getMaxAge() == -1 && !c.hasExpired(), "maxAge -1 not expired");
        c.setMaxAge(Long.MAX_VALUE);
        check(c.getMaxAge() == Long.MAX_VALUE && !c.hasExpired(), "maxAge Long.MAX not expired");
        c.setMaxAge(Long.MIN_VALUE);
        check(c.getMaxAge() == Long.MIN_VALUE, "maxAge Long.MIN stored");
        // JDK-27: negative maxAge other than -1 is NOT reported as expired (probe-verified;
        // JDK-8 reports hasExpired()==true for the same values - version divergence)
        c.setMaxAge(-5);
        check(!c.hasExpired(), "JDK-27: maxAge -5 not expired");
        c.setMaxAge(Long.MIN_VALUE);
        check(!c.hasExpired(), "JDK-27: maxAge Long.MIN not expired");
    }

    static void testParseMatrix() {
        HttpCookie c = HttpCookie.parse("name=value").get(0);
        check(c.getName().equals("name") && c.getValue().equals("value"), "bare name=value");
        check(HttpCookie.parse("Set-Cookie: a=b").get(0).getVersion() == 0, "Set-Cookie prefix v0");
        check(HttpCookie.parse("Set-Cookie2: a=b").get(0).getVersion() == 1, "Set-Cookie2 prefix v1");
        check(HttpCookie.parse("set-cookie: a=b").get(0).getName().equals("a"), "lowercase header ok");
        check(HttpCookie.parse("SET-COOKIE: a=b").get(0).getName().equals("a"), "uppercase header ok");
        check(HttpCookie.parse("  a=b").get(0).getName().equals("a"), "leading space trimmed");
        check(HttpCookie.parse("a= b ").get(0).getValue().equals("b"), "value trimmed");
        check(HttpCookie.parse("a=\"b\"").get(0).getValue().equals("b"), "quoted value stripped");
        check(HttpCookie.parse("a=b; c=d").size() == 1, "semicolon starts attribute section");
        check(HttpCookie.parse("a=b, c=d").size() == 1, "bare header: comma not a separator (v0)");
        List<HttpCookie> two = HttpCookie.parse("Set-Cookie2: a=b, c=d");
        check(two.size() == 2, "Set-Cookie2: comma separates (v1)");
        check(two.get(0).getName().equals("a") && two.get(1).getName().equals("c"), "both v1 cookies parsed");
        // attribute names are case-insensitive
        check(HttpCookie.parse("a=b; httponly").get(0).isHttpOnly(), "attr httponly lowercase");
        check(HttpCookie.parse("a=b; HTTPONLY").get(0).isHttpOnly(), "attr HTTPONLY");
        check(HttpCookie.parse("a=b; SECURE").get(0).getSecure(), "attr SECURE");
        check(HttpCookie.parse("a=b; DISCARD").get(0).getDiscard(), "attr DISCARD");
        check(HttpCookie.parse("a=b; MAX-AGE=5").get(0).getMaxAge() == 5, "attr MAX-AGE");
        check(HttpCookie.parse("a=b; PORT=80").get(0).getPortlist().equals("80"), "attr PORT");
        check(HttpCookie.parse("a=b; CommentURL=http://x/").get(0).getCommentURL().equals("http://x/"), "attr CommentURL");
    }

    static void testParseNegatives() {
        ex(() -> HttpCookie.parse((String) null), "java.lang.NullPointerException", null, "parse null");
        ex(() -> HttpCookie.parse(""), "java.lang.IllegalArgumentException", "Empty cookie header string", "parse empty");
        ex(() -> HttpCookie.parse(";"), "java.lang.IllegalArgumentException", "Empty cookie header string", "parse semicolon");
        ex(() -> HttpCookie.parse("novalue"), "java.lang.IllegalArgumentException",
            "Invalid cookie name-value pair", "parse no equals");
        ex(() -> HttpCookie.parse("name"), "java.lang.IllegalArgumentException",
            "Invalid cookie name-value pair", "parse bare name");
        ex(() -> HttpCookie.parse("  "), "java.lang.IllegalArgumentException",
            "Invalid cookie name-value pair", "parse spaces");
        ex(() -> HttpCookie.parse("=v"), "java.lang.IllegalArgumentException", "Illegal cookie name", "parse empty name");
        ex(() -> HttpCookie.parse("cookie: a=b"), "java.lang.IllegalArgumentException",
            "Illegal cookie name", "parse unrecognized header prefix");
        ex(() -> HttpCookie.parse("a b=c"), "java.lang.IllegalArgumentException",
            "Illegal cookie name", "parse space inside name");
        // v1 header splits on comma: "8080" fragment has no '=' -> invalid pair (both JDKs)
        ex(() -> HttpCookie.parse("Set-Cookie2: a=b; Port=80,8080"), "java.lang.IllegalArgumentException",
            "Invalid cookie name-value pair", "v1 comma splits portlist");
    }

    static void testParseAttrMatrix() {
        // v1 string: all attributes, max-age keeps version guess at 1
        HttpCookie v1 = HttpCookie.parse("Set-Cookie2: name=value; Path=/a; Domain=.example.com; "
            + "Max-Age=100; Secure; HttpOnly; Comment=c; CommentURL=http://u/; Discard; Port=80").get(0);
        check(v1.getName().equals("name") && v1.getValue().equals("value"), "v1 name/value");
        check(v1.getVersion() == 1, "v1 version");
        check(v1.getPath().equals("/a"), "v1 path");
        check(v1.getDomain().equals(".example.com"), "v1 domain keeps leading dot");
        check(v1.getMaxAge() == 100, "v1 maxAge");
        check(v1.getSecure(), "v1 secure");
        check(v1.isHttpOnly(), "v1 httpOnly");
        check(v1.getComment().equals("c"), "v1 comment");
        check(v1.getCommentURL().equals("http://u/"), "v1 commentURL");
        check(v1.getDiscard(), "v1 discard");
        check(v1.getPortlist().equals("80"), "v1 portlist");
        // v0 string: expires attr forces version 0; future date keeps it alive
        HttpCookie v0 = HttpCookie.parse("name=value; expires=Wed, 21 Oct 2099 07:28:00 GMT; "
            + "Path=/a; Domain=.example.com; Secure; HttpOnly; Comment=c; CommentURL=http://u/; Discard; Port=80").get(0);
        check(v0.getVersion() == 0, "v0 expires forces version 0");
        check(v0.getPath().equals("/a"), "v0 path");
        check(v0.getDomain().equals(".example.com"), "v0 domain");
        check(v0.getMaxAge() > 0, "v0 future expires -> positive maxAge");
        check(v0.getSecure(), "v0 secure");
        check(v0.isHttpOnly(), "v0 httpOnly");
        check(v0.getComment().equals("c"), "v0 comment");
        check(v0.getCommentURL().equals("http://u/"), "v0 commentURL");
        check(v0.getDiscard(), "v0 discard");
        check(v0.getPortlist().equals("80"), "v0 portlist");
        check(!v0.hasExpired(), "v0 future expires not expired");
        // parse lowercases the domain attribute value (setDomain does NOT)
        check(HttpCookie.parse("a=b; domain=EXAMPLE.com").get(0).getDomain().equals("example.com"),
            "parse lowercases domain attr");
    }

    static void testParseVersionGuess() {
        // guessCookieVersion: expires->0; version=/max-age/set-cookie2->1; else 0;
        // the guessed version then overrides any version attribute's value
        check(HttpCookie.parse("a=b").get(0).getVersion() == 0, "guess: bare -> 0");
        check(HttpCookie.parse("a=b; version=1").get(0).getVersion() == 1, "guess: version attr -> 1");
        check(HttpCookie.parse("a=b; version=0").get(0).getVersion() == 1, "guess overrides version=0 attr");
        check(HttpCookie.parse("a=b; Max-Age=100").get(0).getVersion() == 1, "guess: max-age -> 1");
        check(HttpCookie.parse("a=b; expires=Wed, 21 Oct 2099 07:28:00 GMT").get(0).getVersion() == 0,
            "guess: expires -> 0 even with version attr absent");
        check(HttpCookie.parse("Set-Cookie: a=b; version=1").get(0).getVersion() == 1, "Set-Cookie + version attr -> 1");
        ex(() -> HttpCookie.parse("a=b; version=2").get(0), "java.lang.IllegalArgumentException",
            "cookie version should be 0 or 1", "version attr 2 rejected");
    }

    static void testParseExpiryAttrs() {
        HttpCookie z = HttpCookie.parse("a=b; max-age=0").get(0);
        check(z.getMaxAge() == 0 && z.hasExpired(), "max-age=0 expired");
        HttpCookie n5 = HttpCookie.parse("a=b; max-age=-5").get(0);
        check(n5.getMaxAge() == -5, "negative max-age attr accepted");
        HttpCookie past = HttpCookie.parse("a=b; expires=Wed, 21 Oct 2015 07:28:00 GMT").get(0);
        check(past.hasExpired(), "past expires -> expired");
        // JDK-27 normalizes a past expires date to maxAge==0 (JDK-8 keeps it negative - divergence)
        check(past.getMaxAge() == 0, "JDK-27: past expires -> maxAge 0");
        HttpCookie bad = HttpCookie.parse("a=b; expires=notadate").get(0);
        check(bad.getMaxAge() == 0 && bad.hasExpired(), "unparseable date -> immediate expiry");
        HttpCookie future = HttpCookie.parse("a=b; expires=Wed, 21 Oct 2099 07:28:00 GMT").get(0);
        check(future.getMaxAge() > 0 && !future.hasExpired(), "future expires -> alive");
        // JDK-27 (JDK-8213254): max-age takes precedence over expires in either order
        HttpCookie me = HttpCookie.parse("a=b; max-age=100; expires=Wed, 21 Oct 2015 07:28:00 GMT").get(0);
        check(me.getMaxAge() == 100, "JDK-27: max-age wins over later expires");
        HttpCookie em = HttpCookie.parse("a=b; expires=Wed, 21 Oct 2015 07:28:00 GMT; max-age=100").get(0);
        check(em.getMaxAge() == 100, "JDK-27: max-age wins over earlier expires");
        // JDK-27 ignores a non-numeric max-age (JDK-8 throws IAE - divergence)
        check(HttpCookie.parse("a=b; max-age=abc").get(0).getMaxAge() == -1, "JDK-27: bad max-age ignored");
    }

    static void testDomainMatchesMatrix() {
        check(HttpCookie.domainMatches(".example.com", "www.example.com"), "dm dot subdomain");
        check(HttpCookie.domainMatches(".example.com", "example.com"), "dm dot apex");
        check(!HttpCookie.domainMatches("example.com", "www.example.com"), "dm bare domain not subdomain");
        check(HttpCookie.domainMatches("example.com", "example.com"), "dm bare exact");
        check(HttpCookie.domainMatches(".EXAMPLE.com", "WWW.Example.COM"), "dm case-insensitive");
        check(!HttpCookie.domainMatches(".example.com", "www.other.com"), "dm different domain");
        check(!HttpCookie.domainMatches(".example.com", "example.org"), "dm different tld");
        check(!HttpCookie.domainMatches(".example.com", "notexample.com"), "dm suffix-only host");
        check(!HttpCookie.domainMatches("a.example.com", "b.example.com"), "dm sibling hosts");
        check(!HttpCookie.domainMatches(null, "x.com"), "dm null domain");
        check(!HttpCookie.domainMatches(".example.com", null), "dm null host");
        check(!HttpCookie.domainMatches(null, null), "dm both null");
        check(!HttpCookie.domainMatches(".com", "foo.com"), "dm TLD-like rejected");
        check(!HttpCookie.domainMatches(".", "x.com"), "dm bare dot rejected");
        check(HttpCookie.domainMatches(".example.com", ".example.com"), "dm dot host literal");
        check(HttpCookie.domainMatches("a.b", "a.b"), "dm two-label bare exact");
        check(HttpCookie.domainMatches(".a.b", "b.a.b"), "dm dot two-label");
        check(!HttpCookie.domainMatches("localhost", "localhost"), "dm single-label bare rejected");
        check(!HttpCookie.domainMatches(".localhost", "x.localhost"), "dm dot single label rejected");
        check(!HttpCookie.domainMatches("example.com", null), "dm bare null host");
    }

    static void testEqualsHashCode() {
        HttpCookie a = new HttpCookie("N", "1"); a.setDomain("EXAMPLE.com"); a.setPath("/x");
        HttpCookie b = new HttpCookie("n", "2"); b.setDomain("example.com"); b.setPath("/x");
        check(a.equals(b), "eq ci name+domain ignores value");
        check(a.hashCode() == b.hashCode(), "hashCode consistent with equals");
        b.setMaxAge(999);
        check(a.equals(b), "eq ignores maxAge");
        b.setVersion(0);
        check(a.equals(b), "eq ignores version");
        b.setSecure(true);
        check(a.equals(b), "eq ignores secure");
        b.setComment("c");
        check(a.equals(b), "eq ignores comment");
        HttpCookie otherPath = new HttpCookie("n", "3"); otherPath.setDomain("example.com"); otherPath.setPath("/y");
        check(!a.equals(otherPath), "eq path-sensitive");
        HttpCookie noPath = new HttpCookie("n", "3"); noPath.setDomain("example.com");
        check(!a.equals(noPath), "eq null path vs set path");
        check(!a.equals(null), "eq null false");
        check(!a.equals("string"), "eq non-cookie false");
        check(a.equals(a), "eq reflexive");
        check(b.equals(a), "eq symmetric");
        check(new HttpCookie("a", "1").equals(new HttpCookie("a", "2")), "eq both-null domain/path");
        check(new HttpCookie("a", "1").hashCode() == new HttpCookie("a", "2").hashCode(), "hc same-default pair");
        // documented formula: lc(name).hashCode() + lc(domain).hashCode() + path.hashCode()
        HttpCookie f = new HttpCookie("n", "v"); f.setDomain("example.com");
        check(f.hashCode() == "n".toLowerCase().hashCode() + "example.com".toLowerCase().hashCode(),
            "hc formula name+domain");
        f.setPath("/p");
        check(f.hashCode() == "n".toLowerCase().hashCode() + "example.com".toLowerCase().hashCode()
            + "/p".hashCode(), "hc formula name+domain+path");
        check(new HttpCookie("n", "v").hashCode() == 110, "hc default exact");
    }

    static void testToStringForms() {
        HttpCookie v0 = new HttpCookie("k", "v"); v0.setVersion(0);
        check(v0.toString().equals("k=v"), "toString v0");
        check(new HttpCookie("k", "v").toString().equals("k=\"v\""), "toString v1");
        HttpCookie dp = new HttpCookie("k", "v"); dp.setDomain("example.com"); dp.setPath("/p");
        check(dp.toString().equals("k=\"v\";$Path=\"/p\";$Domain=\"example.com\""), "toString v1 path before domain");
        HttpCookie po = new HttpCookie("k", "v"); po.setPortlist("80,8080");
        check(po.toString().equals("k=\"v\";$Port=\"80,8080\""), "toString v1 portlist");
        HttpCookie v0attrs = new HttpCookie("k", "v"); v0attrs.setVersion(0);
        v0attrs.setDomain("example.com"); v0attrs.setPath("/p"); v0attrs.setPortlist("80");
        check(v0attrs.toString().equals("k=v"), "toString v0 drops attributes");
        HttpCookie cm = new HttpCookie("k", "v"); cm.setComment("c");
        check(cm.toString().equals("k=\"v\""), "comment not in toString");
        HttpCookie e0 = new HttpCookie("k", ""); e0.setVersion(0);
        check(e0.toString().equals("k="), "toString v0 empty value unquoted");
        check(new HttpCookie("k", "").toString().equals("k=\"\""), "toString v1 empty value quoted");
        HttpCookie q0 = new HttpCookie("k", "\"vv\""); q0.setVersion(0);
        check(q0.toString().equals("k=\"vv\""), "toString v0 keeps inner quotes");
        HttpCookie n0 = new HttpCookie("k", null); n0.setVersion(0);
        check(n0.toString().equals("k=null"), "toString v0 null value literal");
        check(new HttpCookie("k", null).toString().equals("k=\"null\""), "toString v1 null value literal");
    }

    static void testCloneSemantics() throws Exception {
        HttpCookie orig = new HttpCookie("k", "v");
        orig.setComment("c"); orig.setCommentURL("http://u/"); orig.setDomain("example.com");
        orig.setPath("/p"); orig.setPortlist("80"); orig.setSecure(true); orig.setHttpOnly(true);
        orig.setDiscard(true); orig.setMaxAge(100);
        HttpCookie cl = (HttpCookie) orig.clone();
        check(cl != orig, "clone distinct object");
        check(cl.equals(orig), "clone equals");
        check(cl.getName().equals("k") && cl.getValue().equals("v"), "clone name/value");
        check(cl.getComment().equals("c"), "clone comment");
        check(cl.getCommentURL().equals("http://u/"), "clone commentURL");
        check(cl.getDomain().equals("example.com"), "clone domain");
        check(cl.getPath().equals("/p"), "clone path");
        check(cl.getPortlist().equals("80"), "clone portlist");
        check(cl.getSecure() && cl.isHttpOnly() && cl.getDiscard(), "clone flags");
        check(cl.getMaxAge() == 100, "clone maxAge");
        check(cl.hashCode() == orig.hashCode(), "clone hashCode");
        cl.setValue("v2");
        check(orig.getValue().equals("v"), "clone field independence");
        cl.setDomain("other.com");
        check(orig.getDomain().equals("example.com"), "clone domain independence");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDefaultsMatrix();
        testCtorMatrix();
        testSetValue();
        testVersionMatrix();
        testAttrRoundTrip();
        testMaxAgeExpiry();
        testParseMatrix();
        testParseNegatives();
        testParseAttrMatrix();
        testParseVersionGuess();
        testParseExpiryAttrs();
        testDomainMatchesMatrix();
        testEqualsHashCode();
        testToStringForms();
        testCloneSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
