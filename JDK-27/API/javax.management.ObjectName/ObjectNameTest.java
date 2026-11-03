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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
