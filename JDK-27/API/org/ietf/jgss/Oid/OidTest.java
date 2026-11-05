/*
 * @test
 * @summary org.ietf.jgss.Oid conformance: white-box structure + black-box behavior.
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
        Oid krb5 = new Oid("1.2.840.113554.1.2.2");
        check("1.2.840.113554.1.2.2".equals(krb5.toString()), "Kerberos OID toString");
        check(krb5.equals(new Oid("1.2.840.113554.1.2.2")), "equals same value");
        check(krb5.hashCode() == new Oid("1.2.840.113554.1.2.2").hashCode(), "hashCode same value");
        check(!krb5.equals(new Oid("1.3.6.1.5.5.2")), "not equals different value");
        check(!krb5.equals("1.2.840.113554.1.2.2"), "not equals String");
        check(!krb5.equals(null), "not equals null");

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

        // getDER round trip + clone semantics
        byte[] der = krb5.getDER();
        check(der.length > 0, "getDER non-empty");
        check(krb5.equals(new Oid(der)), "DER round trip via byte[]");
        check(krb5.equals(new Oid(new ByteArrayInputStream(der))), "DER round trip via InputStream");
        der[0] = (byte) 0xFF;
        check(krb5.equals(new Oid(krb5.getDER())), "getDER returns a clone (mutating result does not corrupt)");

        // DER-text duality: canonical Kerberos v5 DER bytes <-> dotted form
        byte[] krb5Der = {6, 9, 42, -122, 72, -122, -9, 18, 1, 2, 2};
        Oid fromDer = new Oid(krb5Der);
        check("1.2.840.113554.1.2.2".equals(fromDer.toString()), "DER bytes -> dotted toString");
        check(fromDer.equals(krb5), "DER-built equals string-built");
        check(java.util.Arrays.equals(krb5Der, krb5.getDER()), "string-built getDER -> canonical DER bytes");

        // second well-known OID
        Oid dn = new Oid("2.5.13.1");
        check("2.5.13.1".equals(dn.toString()), "2.5.13.1 toString");
        check(dn.equals(new Oid(dn.getDER())), "2.5.13.1 DER round trip");

        // exact offending string appears in the message
        try { new Oid("bogus"); check(false, "expected GSSException for bogus"); }
        catch (GSSException e) {
            check("Improperly formatted Object Identifier String - bogus".equals(e.getMessage()),
                    "invalid OID message includes offending string");
        }

        // containedIn exact membership two-state
        check(new Oid("1.2").containedIn(new Oid[]{new Oid("1.2.3"), new Oid("1.2")}),
                "containedIn true two-state member");
        check(!new Oid("1.4").containedIn(new Oid[]{new Oid("1.2.3"), new Oid("1.9")}),
                "containedIn false two-state non-member");

        // null byte[] -> NPE (implementation dereferences before validating)
        try { new Oid((byte[]) null); check(false, "expected NPE for null DER bytes"); }
        catch (NullPointerException expected) { check(true, "null DER bytes -> NullPointerException"); }

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
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
