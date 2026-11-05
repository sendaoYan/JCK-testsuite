/*
 * @test
 * @summary org.ietf.jgss.Oid conformance: constructors, DER-text duality, equality, null/error semantics.
 * @run main OidTest
 */

import org.ietf.jgss.*;
import java.io.ByteArrayInputStream;
import java.lang.reflect.*;
import java.util.Arrays;

public class OidTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasCtor(Class<?> c, Class<?>... p) { try { c.getDeclaredConstructor(p); return true; } catch (Exception e) { return false; } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.Oid");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "is class not interface");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(hasCtor(c, String.class), "ctor(String)");
        check(hasCtor(c, java.io.InputStream.class), "ctor(InputStream)");
        check(hasCtor(c, byte[].class), "ctor(byte[])");
        check(hasMethod(c, "toString"), "toString");
        check(hasMethod(c, "equals"), "equals");
        check(hasMethod(c, "hashCode"), "hashCode");
        check(hasMethod(c, "getDER"), "getDER");
        check(hasMethod(c, "containedIn"), "containedIn");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // original smoke assertions (kept from the pre-deepening version)
        org.ietf.jgss.Oid oid = new org.ietf.jgss.Oid("1.2.3.4");
        check("1.2.3.4".equals(oid.toString()), "toString");
        check(oid.equals(new org.ietf.jgss.Oid("1.2.3.4")), "equals");
        check(oid.containedIn(new org.ietf.jgss.Oid[]{new org.ietf.jgss.Oid("1.2.3.4")}), "containedIn");
        check(oid.getDER() != null, "getDER");
        org.ietf.jgss.Oid oid2 = new org.ietf.jgss.Oid(new byte[]{0x06, 0x03, 0x2A, 0x03, 0x04});
        check("1.2.3.4".equals(oid2.toString()), "byte[] ctor");

        Oid krb5 = new Oid("1.2.840.113554.1.2.2");
        check("1.2.840.113554.1.2.2".equals(krb5.toString()), "Kerberos OID toString");
        check(krb5.equals(new Oid("1.2.840.113554.1.2.2")), "equals same value");
        check(krb5.hashCode() == new Oid("1.2.840.113554.1.2.2").hashCode(), "hashCode same value");
        check(!krb5.equals(new Oid("1.3.6.1.5.5.2")), "not equals different value");
        check(!krb5.equals("1.2.840.113554.1.2.2"), "not equals String");
        check(!krb5.equals(null), "not equals null");

        // DER-text duality: the canonical Kerberos v5 DER bytes and the dotted
        // form denote the same Oid in both directions
        byte[] krb5Der = {6, 9, 42, -122, 72, -122, -9, 18, 1, 2, 2};
        Oid fromDer = new Oid(krb5Der);
        check("1.2.840.113554.1.2.2".equals(fromDer.toString()), "DER bytes -> dotted toString");
        check(fromDer.equals(krb5), "DER-built equals string-built");
        check(Arrays.equals(krb5Der, krb5.getDER()), "string-built getDER -> canonical DER bytes");

        // a second well-known OID
        Oid dn = new Oid("2.5.13.1");
        check("2.5.13.1".equals(dn.toString()), "2.5.13.1 toString");
        check(dn.equals(new Oid(dn.getDER())), "2.5.13.1 DER round trip");

        // equality/hashCode/DER are canonicalized (leading zeros), toString preserves input
        Oid a = new Oid("1.2.3");
        Oid b = new Oid("01.02.03");
        check(a.equals(b), "canonical equals with leading zeros");
        check(a.hashCode() == b.hashCode(), "canonical hashCode with leading zeros");
        check(Arrays.equals(a.getDER(), b.getDER()), "canonical getDER with leading zeros");
        check("1.2.3".equals(a.toString()), "toString 1.2.3");
        check("01.02.03".equals(b.toString()), "toString preserves original input 01.02.03");

        // containedIn
        check(krb5.containedIn(new Oid[]{new Oid("1.2.840.113554.1.2.2")}), "containedIn present");
        check(krb5.containedIn(new Oid[]{new Oid("1.3.6.1.5.5.2"), new Oid("1.2.840.113554.1.2.2")}), "containedIn among others");
        check(!krb5.containedIn(new Oid[]{new Oid("1.3.6.1.5.5.2")}), "containedIn absent");
        check(!krb5.containedIn(new Oid[0]), "containedIn empty array");
        check(new Oid("1.2").containedIn(new Oid[]{new Oid("1.2.3"), new Oid("1.2")}),
                "containedIn true two-state member");
        check(!new Oid("1.4").containedIn(new Oid[]{new Oid("1.2.3"), new Oid("1.9")}),
                "containedIn false two-state non-member");

        // getDER round trip + clone semantics
        byte[] der = krb5.getDER();
        check(der.length > 0, "getDER non-empty");
        check(krb5.equals(new Oid(der)), "DER round trip via byte[]");
        check(krb5.equals(new Oid(new ByteArrayInputStream(der))), "DER round trip via InputStream");
        der[0] = (byte) 0xFF;
        check(krb5.equals(new Oid(krb5.getDER())), "getDER returns a clone (mutating result does not corrupt)");

        // invalid OID strings -> GSSException FAILURE (there is no BAD_OID constant)
        String[] bad = { "", "abc", "bogus", "1..2", "1.-2", ".1.2", "1.2.", "a.b.c", " 1.2.3", "1.2.3 ", "1.2.3.4.-5" };
        for (String s : bad) {
            try {
                new Oid(s);
                check(false, "expected GSSException for invalid OID: '" + s + "'");
            } catch (GSSException e) {
                check(e.getMajor() == GSSException.FAILURE, "invalid OID major == FAILURE for '" + s + "'");
                check(e.getMajorString() != null, "invalid OID majorString non-null for '" + s + "'");
            }
        }
        check("Improperly formatted Object Identifier String - bogus".equals(
                newGSSExceptionMessageFor("bogus")), "invalid OID message contains offending string");

        // invalid DER bytes -> GSSException FAILURE
        byte[][] badDer = { new byte[0], new byte[]{1, 2, 3}, new byte[]{6, 0} };
        for (byte[] d : badDer) {
            try {
                new Oid(d);
                check(false, "expected GSSException for bad DER");
            } catch (GSSException e) {
                check(e.getMajor() == GSSException.FAILURE, "bad DER major == FAILURE");
            }
        }

        // null byte[] -> NPE (implementation dereferences before validating)
        try {
            new Oid((byte[]) null);
            check(false, "expected NPE for null DER bytes");
        } catch (NullPointerException expected) {
            check(true, "null DER bytes -> NullPointerException");
        }

        // toString is null-safe on every path exercised
        check(new Oid("1.2.3").toString() != null, "toString non-null");
    }

    static String newGSSExceptionMessageFor(String bad) {
        try { new Oid(bad); return null; }
        catch (GSSException e) { return e.getMessage(); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
