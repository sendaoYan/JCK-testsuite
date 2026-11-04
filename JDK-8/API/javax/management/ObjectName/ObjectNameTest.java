/*
 * @test
 * @summary javax.management.ObjectName conformance: full contract coverage.
 *          constructors (String / domain+key+value / domain+Hashtable) and
 *          getInstance factories, precise MalformedObjectNameException /
 *          NullPointerException matrix, null semantics, canonical-name key
 *          sorting, domain/property/value/list pattern detection, wildcard
 *          pattern matching via apply(), quote/unquote escaping, equals/
 *          hashCode/compareTo ordering, and serialization round-trip.
 * @run main ObjectNameTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Hashtable;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

public class ObjectNameTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static ObjectName roundTrip(ObjectName n) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(n); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (ObjectName) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasicParsing() throws Exception {
        ObjectName n = new ObjectName("d:type=x,name=y");
        check("d".equals(n.getDomain()), "domain");
        check("x".equals(n.getKeyProperty("type")), "key property");
        check(n.getKeyPropertyList().size() == 2, "key property list size");
        check(!n.isPattern(), "not a pattern");
        ObjectName p = new ObjectName("d:type=*");
        check(p.isPattern() && p.isPropertyPattern(), "property pattern");
        check(n.equals(new ObjectName("d:type=x,name=y")), "equals");
        check(n.compareTo(new ObjectName("d:type=x,name=y")) == 0, "compareTo");
        check(new ObjectName(n.getCanonicalName()).equals(n), "canonical name");
        check("x".equals(ObjectName.unquote(ObjectName.quote("x"))), "quote/unquote");
        check(n.apply(n), "apply self");
    }

    static void testConstructorVariants() throws Exception {
        // String ctor vs getInstance(String)
        check(new ObjectName("d:k=v").equals(ObjectName.getInstance("d:k=v")), "getInstance(String)");

        // domain+key+value ctor
        ObjectName a = new ObjectName("d", "k", "v");
        check("d".equals(a.getDomain()) && "v".equals(a.getKeyProperty("k")), "3-arg ctor");
        check("d:k=v".equals(a.getCanonicalName()), "3-arg canonical");
        check(a.equals(ObjectName.getInstance("d", "k", "v")), "getInstance(domain,key,value)");

        // domain+Hashtable ctor; canonical name sorts keys lexically
        Hashtable<String, String> t = new Hashtable<String, String>();
        t.put("type", "x");
        t.put("name", "y");
        ObjectName b = new ObjectName("d", t);
        check("x".equals(b.getKeyProperty("type")) && "y".equals(b.getKeyProperty("name")), "Hashtable ctor");
        check("d:name=y,type=x".equals(b.getCanonicalName()), "Hashtable canonical sorted");
        check(b.equals(ObjectName.getInstance("d", t)), "getInstance(domain,Hashtable)");

        // getInstance(ObjectName) returns an equal (possibly identical) name
        ObjectName c = ObjectName.getInstance(b);
        check(c.equals(b), "getInstance(ObjectName) equal");
    }

    static void testCanonicalNameAndPatterns() throws Exception {
        ObjectName n = new ObjectName("d:type=x,name=y");
        check("d:name=y,type=x".equals(n.getCanonicalName()), "canonical sorts keys");
        check("name=y,type=x".equals(n.getCanonicalKeyPropertyListString()), "canonical key list string");
        check("type=x,name=y".equals(n.getKeyPropertyListString()), "key list preserves insertion order");

        ObjectName domainPat = new ObjectName("d*:type=x");
        check(domainPat.isDomainPattern() && !domainPat.isPropertyPattern(), "domain pattern");
        check(domainPat.isPattern(), "domain pattern is pattern");

        ObjectName valuePat = new ObjectName("d:type=*");
        check(valuePat.isPropertyValuePattern() && !valuePat.isPropertyListPattern(), "value pattern");
        check(valuePat.isPropertyValuePattern("type"), "value pattern for type");
        check(!new ObjectName("d:type=x").isPropertyValuePattern("type"), "non-pattern value");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { valuePat.isPropertyValuePattern("nope"); } },
                "isPropertyValuePattern unknown key");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { valuePat.isPropertyValuePattern(null); } },
                "isPropertyValuePattern null");

        ObjectName listPat = new ObjectName("d:type=x,*");
        check(listPat.isPropertyListPattern() && listPat.isPropertyValuePattern() == false, "list pattern");
        check("d:type=x,*".equals(listPat.getCanonicalName()), "list pattern canonical");

        ObjectName empty = new ObjectName("");
        check(empty.isPattern() && empty.isDomainPattern() && empty.isPropertyListPattern(), "empty name is *:*");
        check("*:*".equals(empty.getCanonicalName()), "empty canonical");
        check(empty.equals(ObjectName.WILDCARD), "empty equals WILDCARD");
        check("*:*".equals(ObjectName.WILDCARD.getCanonicalName()), "WILDCARD canonical");
    }

    static void testMalformedNames() {
        final String[] bad = {
            "d",          // no colon -> key properties cannot be empty
            ":",          // empty domain + no props
            "d:",         // empty key properties
            "d=x",        // '=' without ':' -> domain part must be specified
            "d:=x",       // empty key
            "d:type",     // unterminated key property part
            "d:k=v,",     // invalid ending comma
            "d:k=\"v",    // unterminated quoted value
            "d:*,*",      // two '*' in property list
            "d:k=v,=x",   // empty key in second property
            "d:k=v,name", // unterminated second key
        };
        for (final String s : bad) {
            checkThrows(MalformedObjectNameException.class,
                    new ThrowingRunnable() { public void run() throws Exception { new ObjectName(s); } },
                    "malformed name: " + s);
        }
        // invalid domain for Hashtable ctor
        checkThrows(MalformedObjectNameException.class,
                new ThrowingRunnable() {
                    public void run() throws Exception {
                        new ObjectName("a:b", "k", "v");
                    }
                }, "domain with ':' is invalid");
    }

    static void testNullSemantics() throws Exception {
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName((String) null); } },
                "String ctor null");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { ObjectName.getInstance((String) null); } },
                "getInstance(String) null");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName(null, "k", "v"); } },
                "3-arg null domain");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName("d", null, "v"); } },
                "3-arg null key");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName("d", "k", null); } },
                "3-arg null value");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName("d", (Hashtable<String, String>) null); } },
                "Hashtable ctor null table");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName(null, new Hashtable<String, String>() {{ put("k", "v"); }}); } },
                "Hashtable ctor null domain");
        checkThrows(MalformedObjectNameException.class,
                new ThrowingRunnable() { public void run() throws Exception { new ObjectName("d", new Hashtable<String, String>()); } },
                "Hashtable ctor empty table is malformed (not NPE)");

        ObjectName n = new ObjectName("d:type=x");
        check(n.getKeyProperty("missing") == null, "getKeyProperty missing is null");
        check(n.getKeyProperty(null) == null, "getKeyProperty null key returns null");
    }

    static void testPatternMatching() throws Exception {
        ObjectName n = new ObjectName("d:type=x");
        check(n.apply(new ObjectName("d:type=x")), "exact apply");
        check(!n.apply(new ObjectName("d:type=y")), "exact mismatch");

        ObjectName valuePat = new ObjectName("d:type=*");
        check(valuePat.apply(new ObjectName("d:type=x")), "value pattern apply");
        check(!valuePat.apply(new ObjectName("d:type=x,name=y")), "value pattern length mismatch");
        check(!valuePat.apply(valuePat), "pattern does not apply to pattern");

        ObjectName listPat = new ObjectName("d:type=x,*");
        check(listPat.apply(new ObjectName("d:type=x")), "list pattern apply minimal");
        check(listPat.apply(new ObjectName("d:type=x,name=y")), "list pattern apply extra");
        check(!listPat.apply(new ObjectName("d:name=y")), "list pattern missing required key");

        ObjectName domainPat = new ObjectName("d*:type=x");
        check(domainPat.apply(new ObjectName("domain:type=x")), "domain wildcard apply");
        check(!domainPat.apply(new ObjectName("e:type=x")), "domain wildcard mismatch");

        check(ObjectName.WILDCARD.apply(new ObjectName("d:type=x")), "*:* apply");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { n.apply(null); } },
                "apply(null)");
    }

    static void testQuoteUnquote() {
        check("\"a\\*b\"".equals(ObjectName.quote("a*b")), "quote asterisk");
        check("\"a\\?b\"".equals(ObjectName.quote("a?b")), "quote question mark");
        check("\"a\\\\b\"".equals(ObjectName.quote("a\\b")), "quote backslash");
        check("\"a\\\"b\"".equals(ObjectName.quote("a\"b")), "quote quote");
        check("a*b".equals(ObjectName.unquote(ObjectName.quote("a*b"))), "unquote round trip");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { ObjectName.unquote("not-quoted"); } },
                "unquote unquoted string");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { ObjectName.quote(null); } },
                "quote(null)");
    }

    static void testEqualsHashCodeCompareTo() throws Exception {
        ObjectName a = new ObjectName("d:type=x,name=y");
        ObjectName b = new ObjectName("d:name=y,type=x");
        check(a.equals(b) && b.equals(a), "equals symmetric across key order");
        check(a.hashCode() == b.hashCode(), "hashCode equal");
        check(!a.equals(null) && !a.equals("d:type=x,name=y"), "equals non-ObjectName");

        check(new ObjectName("a:type=x").compareTo(new ObjectName("b:type=x")) < 0, "compareTo domain order");
        check(new ObjectName("b:type=x").compareTo(new ObjectName("a:type=x")) > 0, "compareTo domain order reverse");
        check(new ObjectName("d:type=a").compareTo(new ObjectName("d:type=b")) < 0, "compareTo type order");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { a.compareTo(null); } },
                "compareTo(null)");
    }

    static void testSerialization() throws Exception {
        ObjectName n = new ObjectName("d:type=x,name=y");
        ObjectName back = roundTrip(n);
        check(n.equals(back) && back.equals(n), "round trip equal");
        check(n.getCanonicalName().equals(back.getCanonicalName()), "round trip canonical");
        check(n.compareTo(back) == 0, "round trip compareTo");
    }


    @SuppressWarnings("unchecked")
    static <T> T deepRoundTripValue(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (2026-11): quoted values / wildcard placement / domain matrix / factory / defensive copy ----

    static void deepMalformed(String s, String expectedMessage) {
        try {
            new ObjectName(s);
            throw new AssertionError("expected MalformedObjectNameException for: " + s);
        } catch (MalformedObjectNameException e) {
            check(expectedMessage.equals(e.getMessage()),
                    "malformed [" + s + "]: got [" + e.getMessage() + "] want [" + expectedMessage + "]");
        }
    }

    static void testQuotedValueContract() throws Exception {
        ObjectName q = new ObjectName("d:k=\"a,b\"");
        check("d".equals(q.getDomain()), "quoted value: domain");
        check("\"a,b\"".equals(q.getKeyProperty("k")), "quoted value: comma does not split, quotes retained");
        check(q.getKeyPropertyList().size() == 1, "quoted value: single key");
        check("k=\"a,b\"".equals(q.getKeyPropertyListString()), "quoted value: list string");
        check("k=\"a,b\"".equals(q.getCanonicalKeyPropertyListString()), "quoted value: canonical list string");
        check("d:k=\"a,b\"".equals(q.getCanonicalName()), "quoted value: canonical name");
        check(q.equals(new ObjectName("d:k=\"a,b\"")), "quoted value: equals");
        check(q.hashCode() == new ObjectName("d:k=\"a,b\"").hashCode(), "quoted value: hashCode");
        ObjectName e = new ObjectName("d:k=\"\"");
        check("\"\"".equals(e.getKeyProperty("k")) && "d:k=\"\"".equals(e.getCanonicalName()), "empty quoted value");
        check("\"a\\\"b\"".equals(new ObjectName("d:k=\"a\\\"b\"").getKeyProperty("k")), "escaped quote in value");
        check("\"a\\\\b\"".equals(new ObjectName("d:k=\"a\\\\b\"").getKeyProperty("k")), "escaped backslash in value");
        check("\"a\\nb\"".equals(new ObjectName("d:k=\"a\\nb\"").getKeyProperty("k")), "escaped newline in value");
        deepMalformed("d:k=\"a\\:b\"", "Invalid escape sequence '\\:' in quoted value");
        deepMalformed("d:k=\"bad\\escape\"", "Invalid escape sequence '\\e' in quoted value");
        deepMalformed("d:k=\"unterminated", "Unterminated quoted value");
        // unquoted empty value is legal on both sides (locked by probe)
        ObjectName empty = new ObjectName("d:k=");
        check("".equals(empty.getKeyProperty("k")), "unquoted empty value: value is empty string");
        check("d:k=".equals(empty.getCanonicalName()), "unquoted empty value: canonical name");
        check(!empty.isPattern(), "unquoted empty value: not a pattern");
        // quoted key is legal
        ObjectName qk = new ObjectName("d:\"q\"=v");
        check(qk.getKeyProperty("q") == null, "quoted key: not found by bare name");
        check("v".equals(qk.getKeyProperty("\"q\"")), "quoted key: found by quoted name");
        check("d:\"q\"=v".equals(qk.getCanonicalName()), "quoted key: canonical name");
    }

    static void testWildcardPlacement() throws Exception {
        // '*' is not required to be the last property: it is normalized to the tail
        ObjectName wc = new ObjectName("d:*,k=v");
        check(wc.isPattern() && wc.isPropertyPattern() && wc.isPropertyListPattern(),
                "'*' not at tail: still a property-list pattern");
        check("d:k=v,*".equals(wc.getCanonicalName()), "'*' normalized to the tail");
        check("v".equals(wc.getKeyProperty("k")), "'*' not at tail: explicit key kept");
        check(wc.apply(new ObjectName("d:k=v")), "normalized pattern matches the base name");
        check(wc.apply(new ObjectName("d:k=v,x=y")), "normalized pattern matches a superset");
        check(!wc.apply(new ObjectName("d:q=v")), "normalized pattern requires k=v");
        // keys are sorted before a trailing '*'
        ObjectName m1 = new ObjectName("d:b=2,a=1,*");
        check("d:a=1,b=2,*".equals(m1.getCanonicalName()), "keys sorted before trailing '*'");
        check(m1.equals(new ObjectName("d:a=1,b=2,*")), "wildcard order-insensitive equals");
        check(m1.isPropertyListPattern(), "trailing '*' is a list pattern");
        // per-key value wildcard
        ObjectName vw = new ObjectName("d:k=v,j=*");
        check(vw.isPropertyValuePattern("j"), "value wildcard for one key");
        check(!vw.isPropertyValuePattern("k"), "other key is not a value pattern");
        check(!vw.isPropertyListPattern(), "value wildcard is not a list pattern");
        // '*' in key position is not a wildcard
        deepMalformed("d:*=v", "Invalid character found after '*': end of name or ',' expected");
        deepMalformed("d:a*=v", "Invalid character '*' in key part of property");
    }

    static void testDomainCharacterMatrix() throws Exception {
        // '=' or ',' in the domain: the parser then sees no valid key properties
        deepMalformed("a=b:c", "Unterminated key property part");
        deepMalformed("a,b:c", "Unterminated key property part");
        // the first ':' ends the domain: a second ':' lands in the key part
        deepMalformed("a:b:c", "Invalid character ':' in key part of property");
        deepMalformed("::k=v", "Invalid character ':' in key part of property");
        // legal domain forms: wildcards and the empty domain
        check(new ObjectName("*:k=v").isDomainPattern(), "'*' domain pattern");
        check(new ObjectName("?:k=v").isDomainPattern(), "'?' domain pattern");
        check(!new ObjectName("*:k=v").isPropertyPattern(), "domain pattern is not a property pattern");
        check("".equals(new ObjectName(":k=v").getDomain()), "empty domain legal");
        check(!new ObjectName(":k=v").isPattern(), "empty domain exact name");
    }

    static void testValueCharacterMatrix() throws Exception {
        deepMalformed("d:k=a:b", "Invalid character ':' in value part of property");
        deepMalformed("d:k=a=b", "Invalid character '=' in value part of property");
        deepMalformed("d:k=v,,j=w", "Invalid character ',' in key part of property");
        deepMalformed("d:k=v;w=x", "Invalid character '=' in value part of property");
        check("a b".equals(new ObjectName("d:k=a b").getKeyProperty("k")), "space legal in unquoted value");
        check(";".equals(new ObjectName("d:k=;").getKeyProperty("k")), "semicolon legal in unquoted value");
    }

    static void testFactoryAndCaching() throws Exception {
        ObjectName a = ObjectName.getInstance("jck.factory:k=v");
        ObjectName b = ObjectName.getInstance("jck.factory:k=v");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "getInstance same string: equal");
        // no instance interning is performed (probed on both binaries; spec permits but does not require sharing)
        check(a != b, "getInstance same string: distinct instances (no interning)");
        check(ObjectName.getInstance(a) == a, "getInstance(ObjectName) returns the same exact-class instance");
        ObjectName p = ObjectName.getInstance("jck.factory:k=*");
        check(p.isPropertyValuePattern() && p.equals(new ObjectName("jck.factory:k=*")), "getInstance pattern string");
        ObjectName q = ObjectName.getInstance("jck.factory", "k", "v");
        check(q.equals(new ObjectName("jck.factory:k=v")), "getInstance(domain,key,value)");
        java.util.Hashtable<String, String> t = new java.util.Hashtable<String, String>();
        t.put("k", "v");
        check(ObjectName.getInstance("jck.factory", t).equals(new ObjectName("jck.factory:k=v")),
                "getInstance(domain,Hashtable)");
    }

    static void testDefensiveCopy() throws Exception {
        ObjectName n = new ObjectName("jck.defensive:k=v,j=w");
        java.util.Hashtable<String, String> table = n.getKeyPropertyList();
        check(table.size() == 2, "getKeyPropertyList has all keys");
        table.put("injected", "x"); // the returned table is a mutable copy...
        check(n.getKeyProperty("injected") == null, "...but a defensive copy: name unaffected");
        check(n.getKeyPropertyList().size() == 2, "name key set unchanged");
    }

    static void testQuoteFullEscapeSet() {
        check("\"\\n\"".equals(ObjectName.quote("\n")), "quote newline -> \\n");
        check("\"\t\"".equals(ObjectName.quote("\t")), "tab is not escaped");
        check("\"\"".equals(ObjectName.quote("")), "quote empty string");
        check("\"\\\\n\"".equals(ObjectName.quote("\\n")), "backslash-n -> \\\\n");
        check("\n".equals(ObjectName.unquote(ObjectName.quote("\n"))), "newline round trip");
        check("a b".equals(ObjectName.unquote(ObjectName.quote("a b"))), "space round trip");
        check("".equals(ObjectName.unquote("\"\"")), "unquote empty quoted string");
        check("*?\"\\".equals(ObjectName.unquote(ObjectName.quote("*?\"\\"))), "special chars round trip");
        try {
            ObjectName.unquote(null);
            check(false, "unquote(null) must throw");
        } catch (NullPointerException e) {
            passed++;
        }
        try {
            ObjectName.unquote("");
            check(false, "unquote(unquoted empty) must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            ObjectName.unquote("not-quoted");
            check(false, "unquote(unquoted) must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            ObjectName.unquote("\"a\\xb\"");
            check(false, "unquote(invalid escape) must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            ObjectName.unquote("\"abc");
            check(false, "unquote(missing end quote) must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
    }

    static void testValuePatternConstructors() throws Exception {
        ObjectName three = new ObjectName("d", "k", "*");
        check(three.isPropertyValuePattern() && three.isPattern(), "3-arg ctor: '*' value is a value pattern");
        check("*".equals(three.getKeyProperty("k")), "3-arg ctor: raw value kept");
        java.util.Hashtable<String, String> t = new java.util.Hashtable<String, String>();
        t.put("k", "a*b");
        ObjectName h = new ObjectName("d", t);
        check(h.isPropertyValuePattern() && h.isPropertyValuePattern("k"), "Hashtable ctor: '*' in value is a value pattern");
        check("a*b".equals(h.getKeyProperty("k")), "Hashtable ctor: raw value kept");
        // wildcard inside a quoted value is a value pattern and matches by substring
        ObjectName qp = new ObjectName("d:k=\"a,*\"");
        check(qp.isPropertyValuePattern(), "quoted value with '*' is a value pattern");
        check(qp.apply(new ObjectName("d:k=\"a,b\"")), "quoted value pattern applies");
        check(!qp.apply(new ObjectName("d:k=\"x,b\"")), "quoted value pattern mismatch");
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(ObjectName.class).getSerialVersionUID() == 1081892073854801359L,
                "serialVersionUID constant");
        ObjectName[] cases = {
            new ObjectName("jck.ser:a=1,b=2"),
            new ObjectName("jck.ser:*"),
            new ObjectName("jck.ser:k=*"),
            new ObjectName("jck.ser:k=\"a,b\""),
            new ObjectName("jck.ser:k=v,*"),
        };
        for (ObjectName n : cases) {
            ObjectName back = deepRoundTripValue(n);
            check(back.equals(n) && n.equals(back), "round-trip equal: " + n.getCanonicalName());
            check(back.isPattern() == n.isPattern()
                    && back.isDomainPattern() == n.isDomainPattern()
                    && back.isPropertyListPattern() == n.isPropertyListPattern()
                    && back.isPropertyValuePattern() == n.isPropertyValuePattern(),
                    "round-trip pattern flags: " + n.getCanonicalName());
            check(back.getCanonicalName().equals(n.getCanonicalName()), "round-trip canonical: " + n.getCanonicalName());
        }
        ObjectName quoted = deepRoundTripValue(new ObjectName("jck.ser:k=\"a,b\""));
        check("\"a,b\"".equals(quoted.getKeyProperty("k")), "round-trip quoted value");
    }

    public static void main(String[] args) throws Exception {
        testBasicParsing();
        testConstructorVariants();
        testCanonicalNameAndPatterns();
        testMalformedNames();
        testNullSemantics();
        testPatternMatching();
        testQuoteUnquote();
        testEqualsHashCodeCompareTo();
        testSerialization();
        testQuotedValueContract();
        testWildcardPlacement();
        testDomainCharacterMatrix();
        testValueCharacterMatrix();
        testFactoryAndCaching();
        testDefensiveCopy();
        testQuoteFullEscapeSet();
        testValuePatternConstructors();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
