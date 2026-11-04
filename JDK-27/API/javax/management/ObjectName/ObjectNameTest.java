/*
 * @test
 * @summary javax.management.ObjectName conformance: white-box structure + black-box behavior.
 * @run main ObjectNameTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class ObjectNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static void checkMalformed(String s, String expectedMsg) {
        try {
            new ObjectName(s);
            throw new AssertionError("expected MalformedObjectNameException for: " + s);
        } catch (MalformedObjectNameException e) {
            if (expectedMsg != null && !e.getMessage().equals(expectedMsg))
                throw new AssertionError("message mismatch for " + s + ": got [" + e.getMessage() + "] want [" + expectedMsg + "]");
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.ObjectName");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Comparable.class.isAssignableFrom(c), "implements Comparable");
        check(QueryExp.class.isAssignableFrom(c), "implements QueryExp");
        check(hasMethod(c, "getCanonicalName"), "has getCanonicalName");
        check(hasMethod(c, "getDomain"), "has getDomain");
        check(hasMethod(c, "getKeyPropertyList"), "has getKeyPropertyList");
        check(hasMethod(c, "isPattern"), "has isPattern");
        check(hasMethod(c, "apply"), "has apply");
        check(hasMethod(c, "compareTo"), "has compareTo");
        check(ObjectName.WILDCARD.isPattern(), "WILDCARD is pattern");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- construction and basic getters ---
        ObjectName on = new ObjectName("domain:key=value");
        check("domain".equals(on.getDomain()), "getDomain");
        check("value".equals(on.getKeyProperty("key")), "getKeyProperty present");
        check(on.getKeyProperty("missing") == null, "getKeyProperty absent null");
        check(on.getKeyPropertyList().size() == 1, "key property list size");
        check("key=value".equals(on.getKeyPropertyListString()), "getKeyPropertyListString");
        check("key=value".equals(on.getCanonicalKeyPropertyListString()), "getCanonicalKeyPropertyListString");
        check("domain:key=value".equals(on.getCanonicalName()), "getCanonicalName");

        // --- alternative constructors / factories ---
        check(new ObjectName("domain", "key", "value").equals(on), "domain,key,value ctor");
        Hashtable<String, String> table = new Hashtable<String, String>();
        table.put("key", "value");
        check(new ObjectName("domain", table).equals(on), "domain,table ctor");
        check(ObjectName.getInstance("domain:key=value").equals(on), "getInstance(String)");
        check(ObjectName.getInstance("domain", "key", "value").equals(on), "getInstance(domain,key,value)");
        check(ObjectName.getInstance("domain", table).equals(on), "getInstance(domain,table)");
        check(ObjectName.getInstance(on) == on, "getInstance(ObjectName) returns same for exact class");

        // --- canonical ordering ---
        ObjectName sorted = new ObjectName("d:b=2,a=1");
        check("d:a=1,b=2".equals(sorted.getCanonicalName()), "canonical name sorted");
        check("a=1,b=2".equals(sorted.getCanonicalKeyPropertyListString()), "canonical key property list sorted");

        // --- equals / hashCode: canonical form based ---
        check(on.equals(new ObjectName("domain:key=value")), "equals same name");
        check(!on.equals(new ObjectName("domain:key=other")), "different value not equal");
        check(!on.equals(new ObjectName("other:key=value")), "different domain not equal");
        check(!on.equals(null) && !on.equals("domain:key=value"), "not equal null/String");
        check(on.hashCode() == new ObjectName("domain:key=value").hashCode(), "equal hashCode");

        // --- pattern predicates ---
        ObjectName plain = new ObjectName("d:k=v");
        check(!plain.isPattern() && !plain.isDomainPattern() && !plain.isPropertyPattern(), "non-pattern predicates");

        ObjectName all = new ObjectName("*:*");
        check(all.isPattern() && all.isDomainPattern() && all.isPropertyPattern(), "wildcard predicates");
        check(all.isPropertyListPattern(), "wildcard is property list pattern");

        ObjectName domPat = new ObjectName("*.example.com:k=v");
        check(domPat.isDomainPattern() && !domPat.isPropertyPattern(), "domain-only pattern");

        ObjectName valPat = new ObjectName("d:k=*");
        check(valPat.isPattern() && valPat.isPropertyPattern() && valPat.isPropertyValuePattern(), "value pattern predicates");
        check(!valPat.isPropertyListPattern(), "value-only pattern not list pattern");
        check(valPat.isPropertyValuePattern("k"), "isPropertyValuePattern(key)");

        ObjectName listPat = new ObjectName("d:k=v,*");
        check(listPat.isPropertyListPattern(), "property list pattern");
        check(!listPat.isPropertyValuePattern(), "list pattern not value pattern");

        // isPropertyValuePattern(String) on missing key throws IllegalArgumentException
        boolean threw = false;
        try { valPat.isPropertyValuePattern("absent"); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "isPropertyValuePattern(missing key) throws IllegalArgumentException");

        // --- apply(): pattern matching against concrete names ---
        check(all.apply(new ObjectName("anything:any=key")), "*:* matches everything");
        check(domPat.apply(new ObjectName("svc.example.com:k=v")), "domain wildcard matches");
        check(!domPat.apply(new ObjectName("svc.other.net:k=v")), "domain wildcard mismatch");
        check(valPat.apply(new ObjectName("d:k=something")), "value wildcard matches");
        check(!valPat.apply(new ObjectName("d:other=something")), "value wildcard wrong key");
        check(listPat.apply(new ObjectName("d:k=v")), "list pattern matches subset");
        check(listPat.apply(new ObjectName("d:k=v,extra=1")), "list pattern matches superset");
        check(!listPat.apply(new ObjectName("d:other=v")), "list pattern requires listed key");

        // ? single-char wildcard (JDK ObjectName does not support [...] character classes)
        check(new ObjectName("d:k=?").apply(new ObjectName("d:k=x")), "? matches single char");
        check(!new ObjectName("d:k=?").apply(new ObjectName("d:k=xy")), "? does not match two chars");
        check(new ObjectName("d:k=[ab]").apply(new ObjectName("d:k=[ab]")), "[ is treated literally");
        check(!new ObjectName("d:k=[ab]").apply(new ObjectName("d:k=a")), "[ab] is not a character class");

        // apply(plain) equals canonical equality; apply(pattern) to a pattern name is false
        check(plain.apply(new ObjectName("d:k=v")), "exact apply");
        check(!plain.apply(new ObjectName("d:k=w")), "exact apply mismatch");
        check(!plain.apply(new ObjectName("d:k=*")), "apply to pattern name returns false");

        // --- compareTo ---
        check(on.compareTo(new ObjectName("domain:key=value")) == 0, "compareTo equal");
        check(new ObjectName("a:k=v").compareTo(new ObjectName("b:k=v")) < 0, "compareTo domain ordering");
        check(new ObjectName("b:k=v").compareTo(new ObjectName("a:k=v")) > 0, "compareTo domain ordering reverse");

        // --- quote / unquote ---
        check(ObjectName.unquote(ObjectName.quote("plain")).equals("plain"), "quote/unquote round trip plain");
        check(ObjectName.unquote(ObjectName.quote("a b=c")).equals("a b=c"), "quote/unquote round trip special chars");
        check(ObjectName.unquote(ObjectName.quote("a*b?c")).equals("a*b?c"), "quote escapes wildcards");
        check("\"plain\"".equals(ObjectName.quote("plain")), "quote wraps in double quotes");

        // --- MalformedObjectNameException with precise messages (spec-stable) ---
        checkMalformed(":", "Key properties cannot be empty");
        checkMalformed("domain:", "Key properties cannot be empty");
        checkMalformed("domainonly", "Key properties cannot be empty"); // no colon -> no key properties
        checkMalformed("domain:key", "Unterminated key property part"); // key with no '='
        checkMalformed("domain:=v", "Invalid key (empty)");             // empty key
        checkMalformed("domain:key=value,", "Invalid ending comma");    // trailing comma

        // empty string is the wildcard "*:*" (not malformed)
        check(new ObjectName("").equals(ObjectName.WILDCARD), "empty string equals WILDCARD");
        check(new ObjectName("").isPattern(), "empty string is a pattern");

        // empty key-property value is legal
        ObjectName emptyValue = new ObjectName("domain:k=");
        check("domain".equals(emptyValue.getDomain()) && "".equals(emptyValue.getKeyProperty("k")),
                "empty value legal");
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
        testWhiteBox();
        testBlackBox();
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
