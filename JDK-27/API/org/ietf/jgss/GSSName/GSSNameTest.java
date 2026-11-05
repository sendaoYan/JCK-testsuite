/*
 * @test
 * @summary org.ietf.jgss.GSSName conformance: white-box structure + black-box behavior.
 * @run main GSSNameTest
 */

import org.ietf.jgss.*;
import java.lang.reflect.*;
import java.util.Arrays;

public class GSSNameTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static Field field(Class<?> c, String n) { try { return c.getDeclaredField(n); } catch (NoSuchFieldException e) { return null; } }
    static void checkConstOid(Class<?> c, String n) {
        Field f = field(c, n);
        check(f != null && Modifier.isPublic(f.getModifiers()) && Modifier.isStatic(f.getModifiers())
                && Modifier.isFinal(f.getModifiers()) && f.getType() == Oid.class,
                "public static final Oid " + n);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("org.ietf.jgss.GSSName");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkConstOid(c, "NT_HOSTBASED_SERVICE");
        checkConstOid(c, "NT_USER_NAME");
        checkConstOid(c, "NT_MACHINE_UID_NAME");
        checkConstOid(c, "NT_STRING_UID_NAME");
        checkConstOid(c, "NT_ANONYMOUS");
        checkConstOid(c, "NT_EXPORT_NAME");
        String[] names = { "equals", "hashCode", "canonicalize", "export", "toString",
                "getStringNameType", "isAnonymous", "isMN" };
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // standard name-type Oid constants (exact values)
        check(GSSName.NT_USER_NAME != null, "NT_USER_NAME non-null");
        check("1.2.840.113554.1.2.1.1".equals(GSSName.NT_USER_NAME.toString()), "NT_USER_NAME value");
        check("1.2.840.113554.1.2.1.4".equals(GSSName.NT_HOSTBASED_SERVICE.toString()), "NT_HOSTBASED_SERVICE value");
        check("1.3.6.1.5.6.4".equals(GSSName.NT_EXPORT_NAME.toString()), "NT_EXPORT_NAME value");
        check("1.2.840.113554.1.2.1.2".equals(GSSName.NT_MACHINE_UID_NAME.toString()), "NT_MACHINE_UID_NAME value");
        check("1.2.840.113554.1.2.1.3".equals(GSSName.NT_STRING_UID_NAME.toString()), "NT_STRING_UID_NAME value");
        check("1.3.6.1.5.6.3".equals(GSSName.NT_ANONYMOUS.toString()), "NT_ANONYMOUS value");
        check(GSSName.NT_USER_NAME.equals(new Oid("1.2.840.113554.1.2.1.1")), "NT_USER_NAME equals Oid");

        // name-type constants are pairwise distinct
        Oid[] types = { GSSName.NT_USER_NAME, GSSName.NT_HOSTBASED_SERVICE, GSSName.NT_EXPORT_NAME,
                GSSName.NT_MACHINE_UID_NAME, GSSName.NT_STRING_UID_NAME, GSSName.NT_ANONYMOUS };
        for (int i = 0; i < types.length; i++) {
            for (int j = i + 1; j < types.length; j++) {
                check(!types[i].equals(types[j]), "name-type constants distinct");
            }
        }

        // default manager can create a deterministic Kerberos principal name (explicit realm,
        // no krb5.conf / network required)
        GSSManager m = GSSManager.getInstance();
        check(m != null, "GSSManager.getInstance non-null");
        GSSName n1 = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
        GSSName n2 = m.createName("user@EXAMPLE.COM", GSSName.NT_USER_NAME);
        check("user@EXAMPLE.COM".equals(n1.toString()), "created name toString");
        check(!n1.isAnonymous(), "created name is not anonymous");
        check(n1.isMN(), "created name is MN");
        check(GSSName.NT_USER_NAME.equals(n1.getStringNameType()), "getStringNameType == NT_USER_NAME");
        check(n1.equals(n2), "equal names");
        check(n1.hashCode() == n2.hashCode(), "equal names equal hashCode");
        check(!n1.equals(m.createName("other@EXAMPLE.COM", GSSName.NT_USER_NAME)), "different names not equal");
        check(!n1.equals("user@EXAMPLE.COM"), "name not equals String");
        // GSSNameImpl.equals dereferences the other name before its null check ->
        // NPE (deterministic on JDK 8 and JDK 27; documented implementation quirk)
        try {
            n1.equals(null);
            check(false, "expected NPE for GSSName.equals(null)");
        } catch (NullPointerException expected) {
            check(true, "GSSName.equals(null) -> NPE (implementation quirk)");
        }

        // canonicalize to Kerberos mech is identity-equivalent for an MN
        Oid krb5 = new Oid("1.2.840.113554.1.2.2");
        GSSName canonical = n1.canonicalize(krb5);
        check(n1.equals(canonical), "canonicalize(krb5) equals original");

        // export -> re-import (NT_EXPORT_NAME) round trip
        byte[] exported = n1.export();
        check(exported.length > 0, "export non-empty");
        GSSName reimported = m.createName(exported, GSSName.NT_EXPORT_NAME);
        check(n1.equals(reimported), "export/re-import equals original");
        check(reimported.isMN(), "re-imported name is MN");
        check(Arrays.equals(exported, n1.export()), "export is deterministic");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
