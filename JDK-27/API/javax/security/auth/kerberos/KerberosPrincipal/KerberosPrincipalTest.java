/*
 * @test
 * @summary javax.security.auth.kerberos.KerberosPrincipal conformance: white-box structure + black-box behavior.
 * @run main KerberosPrincipalTest
 */

import java.security.Principal;
import java.io.Serializable;

public class KerberosPrincipalTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " [expected " + type.getSimpleName() + ", got " + t + "]", t);
        }
        throw new AssertionError(msg + " [expected " + type.getSimpleName() + "]");
    }
    static boolean hasInterface(Class<?> c, Class<?> want) {
        for (Class<?> i : c.getInterfaces()) if (i == want) return true;
        return false;
    }
    static boolean declares(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.kerberos.KerberosPrincipal");
        check(!c.isInterface(), "is a class");
        check(!c.isEnum(), "not an enum");
        check(!c.isAnnotation(), "not an annotation");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass() == Object.class, "directly extends Object");
        check(hasInterface(c, Principal.class), "implements java.security.Principal");
        check(hasInterface(c, Serializable.class), "implements java.io.Serializable");
        for (String m : new String[]{"getName", "getRealm", "getNameType", "toString", "equals", "hashCode"}) {
            check(declares(c, m), "method " + m + " declared on class");
        }
        // public int name-type constants
        check(java.lang.reflect.Modifier.isPublic(c.getField("KRB_NT_PRINCIPAL").getModifiers()), "KRB_NT_PRINCIPAL public");
        check(java.lang.reflect.Modifier.isFinal(c.getField("KRB_NT_PRINCIPAL").getModifiers()), "KRB_NT_PRINCIPAL final");
        check(java.lang.reflect.Modifier.isStatic(c.getField("KRB_NT_PRINCIPAL").getModifiers()), "KRB_NT_PRINCIPAL static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        javax.security.auth.kerberos.KerberosPrincipal p;

        // name-type constants (RFC 4120 section 6.2)
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_UNKNOWN == 0, "KRB_NT_UNKNOWN == 0");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL == 1, "KRB_NT_PRINCIPAL == 1");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_INST == 2, "KRB_NT_SRV_INST == 2");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_HST == 3, "KRB_NT_SRV_HST == 3");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_XHST == 4, "KRB_NT_SRV_XHST == 4");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_UID == 5, "KRB_NT_UID == 5");
        check(javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_ENTERPRISE == 10, "KRB_NT_ENTERPRISE == 10");

        // constructor(String): full principal with realm -> no default realm needed
        p = new javax.security.auth.kerberos.KerberosPrincipal("alice@EXAMPLE.COM");
        check("alice@EXAMPLE.COM".equals(p.getName()), "getName full principal");
        check("EXAMPLE.COM".equals(p.getRealm()), "getRealm");
        check(p.getNameType() == javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL, "default nameType KRB_NT_PRINCIPAL");
        check(p.toString().equals(p.getName()), "toString == getName");

        // constructor(String, int) with explicit name types (verified against JDK-27)
        p = new javax.security.auth.kerberos.KerberosPrincipal("user@REALM", javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL);
        check("user@REALM".equals(p.getName()), "getName KRB_NT_PRINCIPAL");
        check("REALM".equals(p.getRealm()), "getRealm KRB_NT_PRINCIPAL");
        check(p.getNameType() == javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_PRINCIPAL, "nameType KRB_NT_PRINCIPAL preserved");

        p = new javax.security.auth.kerberos.KerberosPrincipal("krbtgt/REALM@REALM", javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_INST);
        check("krbtgt/REALM@REALM".equals(p.getName()), "getName KRB_NT_SRV_INST");
        check("REALM".equals(p.getRealm()), "getRealm KRB_NT_SRV_INST");
        check(p.getNameType() == javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_INST, "nameType KRB_NT_SRV_INST preserved");

        p = new javax.security.auth.kerberos.KerberosPrincipal("host/foo.example.com@EXAMPLE.COM", javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_HST);
        check("host/foo.example.com@EXAMPLE.COM".equals(p.getName()), "getName KRB_NT_SRV_HST");
        check("EXAMPLE.COM".equals(p.getRealm()), "getRealm KRB_NT_SRV_HST");
        check(p.getNameType() == javax.security.auth.kerberos.KerberosPrincipal.KRB_NT_SRV_HST, "nameType KRB_NT_SRV_HST preserved");

        // equals / hashCode: based solely on getName()
        javax.security.auth.kerberos.KerberosPrincipal a = new javax.security.auth.kerberos.KerberosPrincipal("alice@EXAMPLE.COM");
        javax.security.auth.kerberos.KerberosPrincipal b = new javax.security.auth.kerberos.KerberosPrincipal("alice@EXAMPLE.COM");
        javax.security.auth.kerberos.KerberosPrincipal c2 = new javax.security.auth.kerberos.KerberosPrincipal("bob@EXAMPLE.COM");
        check(a.equals(a), "equals self");
        check(a.equals(b), "equals same name");
        check(b.equals(a), "equals symmetric");
        check(!a.equals(c2), "equals different name");
        check(!a.equals(null), "equals null false");
        check(!a.equals("alice@EXAMPLE.COM"), "equals non-principal false");
        check(a.hashCode() == b.hashCode(), "hashCode same name");
        check(a.hashCode() == a.getName().hashCode(), "hashCode == getName().hashCode()");

        // realm case is preserved verbatim
        javax.security.auth.kerberos.KerberosPrincipal lower = new javax.security.auth.kerberos.KerberosPrincipal("alice@example.com");
        check("example.com".equals(lower.getRealm()), "realm case preserved");
        check(!a.equals(lower), "equals is case-sensitive");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
