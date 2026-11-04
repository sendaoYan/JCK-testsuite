/*
 * @test
 * @summary java.security.PKCS12Attribute conformance: white-box structure + black-box behavior.
 * @run main PKCS12AttributeTest
 */

import java.security.PKCS12Attribute;
import java.security.*;
import java.util.*;

public class PKCS12AttributeTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        PKCS12Attribute attr = new PKCS12Attribute("1.2.840.113549.1.9.20", "hello");
        check("1.2.840.113549.1.9.20".equals(attr.getName()), "getName");
        check("hello".equals(attr.getValue()), "getValue");
        byte[] enc = attr.getEncoded();
        check(enc != null && enc.length > 0, "getEncoded non-empty");
        PKCS12Attribute attr2 = new PKCS12Attribute(enc);
        check(attr.equals(attr2), "equals from encoded");
        check(attr.hashCode() == attr2.hashCode(), "hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        PKCS12Attribute a1 = new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName");
        check(a1.getName().equals("1.2.840.113549.1.9.20"), "name");
        check(a1.getValue().equals("JCK FriendlyName"), "value");
        check(a1.equals(new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName")), "equals");
        check(!a1.equals(new PKCS12Attribute("1.2.840.113549.1.9.20", "other")), "not equals");
        check(a1.hashCode() == new PKCS12Attribute("1.2.840.113549.1.9.20", "JCK FriendlyName").hashCode(), "hashCode");
        check(a1.toString().contains("1.2.840.113549.1.9.20") && a1.toString().contains("JCK FriendlyName"), "toString");
        byte[] der = a1.getEncoded();
        check(der != null && der.length > 0, "encoded");
        // DER round trip through the byte[] constructor
        PKCS12Attribute a2 = new PKCS12Attribute(der);
        check(a2.equals(a1), "DER round trip equals");
        check(a2.getName().equals(a1.getName()), "round trip name");
        check(a2.getValue().equals(a1.getValue()), "round trip value");
        // second well-known attribute: localKeyId 1.2.840.113549.1.9.21
        PKCS12Attribute ld = new PKCS12Attribute("1.2.840.113549.1.9.21", "1234");
        check(ld.getName().equals("1.2.840.113549.1.9.21"), "localKey id");
        check(ld.getValue().equals("1234"), "localKey value");
        // null arguments rejected
        try { new PKCS12Attribute(null, "v"); check(false, "null name NPE"); }
        catch (NullPointerException npe) { check(true, "null name NPE"); }
        try { new PKCS12Attribute("1.2.3", (String) null); check(false, "null value NPE"); }
        catch (NullPointerException npe) { check(true, "null value NPE"); }

    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void ctMsg(Class<? extends Throwable> expected, String frag, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(frag)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but nothing was thrown");
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02X", x));
        return sb.toString();
    }

    static void testContractDeep() throws Exception {
        Class<?> c = PKCS12Attribute.class;

        // ---- structure: final class, KeyStore.Entry.Attribute, exactly two public ctors ----
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers())
                && java.lang.reflect.Modifier.isPublic(c.getModifiers()), "final public class");
        check(KeyStore.Entry.Attribute.class.isAssignableFrom(c), "implements KeyStore.Entry.Attribute");
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 2, "exactly 2 constructors: (String,String) and (byte[]), no no-arg ctor");

        // ---- constructor exception matrix ----
        ct(NullPointerException.class, () -> new PKCS12Attribute(null, "v"), "ctor(null name) -> NPE");
        ct(NullPointerException.class, () -> new PKCS12Attribute("1.2.3", (String) null), "ctor(null value) -> NPE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: name",
                () -> new PKCS12Attribute("not-an-oid", "v"), "ctor(non-OID name) -> IAE");
        ct(IllegalArgumentException.class, () -> new PKCS12Attribute("1.2.x", "v"), "ctor(malformed OID) -> IAE");
        ct(NullPointerException.class, () -> new PKCS12Attribute((byte[]) null), "byte[] ctor(null) -> NPE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: encoded",
                () -> new PKCS12Attribute(new byte[]{1}), "byte[] ctor(garbage) -> IAE");
        ctMsg(IllegalArgumentException.class, "Incorrect format: encoded",
                () -> new PKCS12Attribute(new byte[0]), "byte[] ctor(empty) -> IAE");

        // ---- DER encoding contract ----
        PKCS12Attribute a = new PKCS12Attribute("1.2.3.4", "value");
        check("300E06032A030431070C0576616C7565".equals(hex(a.getEncoded())),
                "DER: SEQUENCE(0x30){ OID(06) 1.2.3.4, SET(31){ UTF8String(0C) \"value\" } } exactly");
        check(a.getEncoded().length == 16, "encoded length 16");
        byte[] e1 = a.getEncoded();
        check(a.getEncoded() != e1, "getEncoded returns a fresh copy on every call");
        e1[e1.length - 1] = 0x00;
        check(a.getEncoded()[15] == (byte)0x65, "mutating a returned copy does not corrupt the attribute");
        byte[] input = a.getEncoded().clone();
        PKCS12Attribute b = new PKCS12Attribute(input);
        input[0] = 0x31;
        check(hex(b.getEncoded()).equals("300E06032A030431070C0576616C7565"),
                "byte[] ctor defensively copies its input");

        // ---- byte[] ctor round-trip ----
        check("1.2.3.4".equals(b.getName()) && "value".equals(b.getValue()), "byte[] ctor round-trips name/value");
        PKCS12Attribute uni = new PKCS12Attribute("1.2.3.4", "välue-π");
        PKCS12Attribute uni2 = new PKCS12Attribute(uni.getEncoded());
        check(uni2.getValue().equals("välue-π"), "unicode value survives UTF-8 DER round-trip");

        // ---- equals / hashCode are value-based ----
        PKCS12Attribute same = new PKCS12Attribute("1.2.3.4", "value");
        check(a.equals(a) && a.equals(same) && same.equals(a), "equals reflexive and symmetric on same values");
        check(!a.equals(new PKCS12Attribute("1.2.3.4", "other")), "different value not equals");
        check(!a.equals(new PKCS12Attribute("1.2.3.5", "value")), "different name not equals");
        check(!a.equals(null) && !a.equals("1.2.3.4=value"), "equals null and non-attribute false");
        check(a.hashCode() == same.hashCode() && a.hashCode() == b.hashCode(),
                "hashCode consistent across equal instances");

        // ---- toString ----
        check("1.2.3.4=value".equals(a.toString()), "toString is exactly \"name=value\"");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- contract deepening: ctor exception matrix + DER bytes + defensive copies + equals/hashCode ----
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
