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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
