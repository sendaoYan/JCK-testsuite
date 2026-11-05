/*
 * @test
 * @summary javax.naming.ldap.Rdn conformance: white-box structure + black-box behavior.
 * @run main RdnTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.*;
import javax.naming.InvalidNameException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.ldap.Rdn;
import java.util.Arrays;

public class RdnTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.Rdn");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(Comparable.class.isAssignableFrom(c), "Comparable");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == String.class), "ctor(String)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == Attributes.class), "ctor(Attributes)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 1 && x.getParameterTypes()[0] == Rdn.class), "ctor(Rdn)");
        check(Arrays.stream(c.getConstructors()).anyMatch(x -> x.getParameterCount() == 2), "ctor(String,Object)");
        for (String m : new String[]{"getValue", "getType", "toString", "compareTo", "equals", "hashCode", "toAttributes", "size", "escapeValue", "unescapeValue"}) {
            check(hasMethod(c, m), m + " present");
        }
    }

    static void testBlackBox() throws Exception {
        Rdn r = new Rdn("cn=John");
        check("cn".equals(r.getType()), "type");
        check("John".equals(r.getValue()), "value");
        check("cn=John".equals(r.toString()), "toString");
        check(r.size() == 1, "size 1");

        // multi-valued RDN
        Rdn multi = new Rdn("cn=John+sn=Doe");
        check(multi.size() == 2, "multi size 2");
        check("cn".equals(multi.getType()), "multi type first");
        check("John".equals(multi.getValue()), "multi value first");
        check("cn=John+sn=Doe".equals(multi.toString()), "multi toString");

        // Rdn(String,Object) escapes value
        Rdn esc = new Rdn("cn", "a,b+c");
        check("a,b+c".equals(esc.getValue()), "object value unescaped");
        check("cn=a\\,b\\+c".equals(esc.toString()), "toString escapes");

        // copy constructor
        Rdn copy = new Rdn(esc);
        check(copy.equals(esc), "copy equals");
        check(copy.toString().equals(esc.toString()), "copy toString");

        // toAttributes
        Attributes attrs = multi.toAttributes();
        check(attrs.size() == 2, "toAttributes size");
        check("John".equals(attrs.get("cn").get()), "toAttributes cn");
        check("Doe".equals(attrs.get("sn").get()), "toAttributes sn");

        // escape/unescape round trip
        check("a\\,b\\+c".equals(Rdn.escapeValue("a,b+c")), "escapeValue");
        check("a,b+c".equals(Rdn.unescapeValue("a\\,b\\+c")), "unescapeValue");

        // equals / hashCode / compareTo
        check(r.equals(new Rdn("cn=John")), "equals");
        check(r.hashCode() == new Rdn("cn=John").hashCode(), "hashCode");
        check(r.compareTo(new Rdn("cn=John")) == 0, "compareTo equal");
        check(new Rdn("cn=a").compareTo(new Rdn("cn=b")) < 0, "compareTo ordering");

        // invalid
        try {
            new Rdn("=x");
            check(false, "invalid should throw");
        } catch (InvalidNameException expected) {
            check(true, "invalid throws InvalidNameException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dRdnParseAndAccessors();
        dRdnEscapeStatics();
        dRdnEquality();
        dRdnMultivalue();
        dRdnAttributesCtor();
        dRdnNegative();
        dRdnSerialization();
        System.out.println("deep: Rdn contracts OK");
    }

    static void dRdnParseAndAccessors() throws Exception {
        Rdn r = new Rdn("cn=duke");
        check("cn".equals(r.getType()), "getType");
        check("duke".equals(r.getValue()), "getValue");
        check("cn=duke".equals(r.toString()), "toString");
        check(r.size() == 1, "single-valued size 1");
        // hex value form yields a byte[] value
        Rdn hex = new Rdn("cn=#4869");
        check(hex.getValue() instanceof byte[], "hex form -> byte[] value");
        check(java.util.Arrays.equals((byte[]) hex.getValue(), new byte[]{0x48, 0x69}),
                "hex bytes decoded");
        check("cn".equals(hex.getType()), "hex form keeps type");
        // escapes decode in getValue
        Rdn esc = new Rdn("cn=a\\,b");
        check("a,b".equals(esc.getValue()), "escaped comma decoded in value");
        check(esc.toString().contains("\\,"), "toString re-escapes");
    }

    static void dRdnEscapeStatics() {
        String tricky = "a,b+c=d\"\\ #x; ";
        String escaped = Rdn.escapeValue(tricky);
        for (char c : new char[]{',', '+', '=', '"', '\\', '#', ';'})
            check(escaped.indexOf("\\" + c) >= 0, "escape covers " + c);
        check(escaped.endsWith("\\ "), "trailing space escaped");
        check(tricky.equals(Rdn.unescapeValue(escaped)), "unescape inverts escape");
        check("duke".equals(Rdn.unescapeValue("duke")), "plain value unchanged");
        check("OK".equals(Rdn.unescapeValue("\\4F\\4B")), "hex pair unescape");
        // byte[] escaping produces the #hex form; parse inverts it
        byte[] b = {0x48, 0x69};
        check("#4869".equals(Rdn.escapeValue(b)), "byte[] -> #hex");
        try {
            Object back = new Rdn("cn=" + Rdn.escapeValue(b)).getValue();
            check(java.util.Arrays.equals((byte[]) back, b), "hex form round-trips to bytes");
        } catch (InvalidNameException e) { throw new AssertionError(e); }
        // unescape of the # form yields bytes
        Object u = Rdn.unescapeValue("#4869");
        check(u instanceof byte[] && java.util.Arrays.equals((byte[]) u, b),
                "unescapeValue(#..) -> byte[]");
        // non-ASCII characters are not escaped by escapeValue(String)
        check("é".equals(Rdn.escapeValue("é")), "non-ASCII untouched");
        try {
            Rdn.unescapeValue("\\4");
            check(false, "single hex digit should fail");
        } catch (IllegalArgumentException e) {
            check(true, "bad hex escape -> IAE");
        }
    }

    static void dRdnEquality() throws Exception {
        check(new Rdn("cn=duke").equals(new Rdn("CN=duke")), "type case-insensitive");
        check(new Rdn("cn=Duke").equals(new Rdn("cn=duke")), "value case-insensitive");
        check(new Rdn("CN=x").hashCode() == new Rdn("cn=x").hashCode(), "hashCode agrees");
        check(!new Rdn("cn=a").equals(new Rdn("cn=b")), "different values unequal");
        check(!new Rdn("cn=a").equals("cn=a"), "non-Rdn never equal");
        check(new Rdn("cn=a").compareTo(new Rdn("cn=b")) < 0, "compareTo order");
        check(new Rdn("cn=a").compareTo(new Rdn("CN=a")) == 0, "compareTo case-insensitive");
        boolean cce = false;
        try { new Rdn("cn=a").compareTo("x"); }
        catch (ClassCastException e) { cce = true; }
        check(cce, "compareTo non-Rdn -> CCE");
        // copy constructor
        Rdn orig = new Rdn("cn=duke");
        Rdn copy = new Rdn(orig);
        check(copy.equals(orig), "copy constructor equal");
        check(copy != orig, "copy is a new instance");
    }

    static void dRdnMultivalue() throws Exception {
        Rdn mv = new Rdn("cn=duke+sn=elliot");
        check(mv.size() == 2, "multi-value size counts pairs");
        check("cn".equals(mv.getType()), "first type reported");
        check("duke".equals(mv.getValue()), "first value reported");
        check(new Rdn("sn=elliot+cn=duke").equals(mv), "multi-value equality order-free");
        check(new Rdn("sn=elliot+cn=duke").hashCode() == mv.hashCode(), "multi-value hashCode");
        check(mv.compareTo(new Rdn("sn=elliot+cn=duke")) == 0, "multi-value compareTo order-free");
        check(mv.toString().contains("+"), "toString keeps multi-value form");
    }

    static void dRdnAttributesCtor() throws Exception {
        javax.naming.directory.BasicAttributes at = new javax.naming.directory.BasicAttributes();
        at.put(new javax.naming.directory.BasicAttribute("cn", "duke"));
        at.put(new javax.naming.directory.BasicAttribute("sn", "elliot"));
        Rdn r = new Rdn(at);
        check(r.size() == 2, "attributes ctor converts pairs");
        check(r.equals(new Rdn("cn=duke+sn=elliot")), "attributes ctor equality");
        boolean npe = false;
        try { new Rdn((javax.naming.directory.Attributes) null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null Attributes -> NPE");
        boolean ine = false;
        try { new Rdn(new javax.naming.directory.BasicAttributes()); }
        catch (InvalidNameException e) { ine = true; }
        check(ine, "empty Attributes -> InvalidNameException");
        // toAttributes() rebuilds the attribute set
        javax.naming.directory.Attributes back = new Rdn("cn=duke+sn=elliot").toAttributes();
        check(back.size() == 2, "toAttributes size");
        check("duke".equals(back.get("cn").get()), "toAttributes value");
    }

    static void dRdnNegative() throws Exception {
        String[] bad = {"noequals", "=duke", "cn", "+", ",", "cn=a\\", "\\4", "cn=a,,ou=b", "cn=duke,ou=x"};
        for (String s : bad) {
            boolean ine = false;
            try { new Rdn(s); }
            catch (InvalidNameException e) { ine = true; }
            check(ine, "invalid RDN: " + s);
        }
        // tolerated forms
        try {
            check(new Rdn("cn=").getValue().equals(""), "empty value tolerated");
            check(new Rdn("cn=a+").size() == 1, "trailing + ignored (single pair)");
            check(new Rdn("cn=a\\ ").size() == 1, "escaped space tolerated");
        } catch (InvalidNameException e) {
            throw new AssertionError("tolerated form rejected: " + e.getMessage());
        }
        // null input
        boolean npe = false;
        try { new Rdn((String) null); }
        catch (NullPointerException e) { npe = true; }
        check(npe, "null String -> NPE");
    }

    static void dRdnSerialization() throws Exception {
        Rdn r = new Rdn("cn=duke+sn=elliot");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(r); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            Rdn back = (Rdn) ois.readObject();
            check(back.equals(r), "round-trip equal");
            check(back.size() == 2, "round-trip multi-value size");
        }
    }

}
