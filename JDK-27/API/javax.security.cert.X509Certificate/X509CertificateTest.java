/*
 * @test
 * @summary javax.security.cert.X509Certificate conformance: deprecated X509 certificate abstract class.
 * @run main X509CertificateTest
 */

import java.lang.reflect.*;

public class X509CertificateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.cert.X509Certificate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("javax.security.cert.Certificate"), "extends javax.security.cert.Certificate");
        check(hasPublicCtor(c), "public constructor");
        checkMethods(c, new String[]{"checkValidity","getVersion","getSerialNumber","getIssuerDN","getSubjectDN","getNotBefore","getNotAfter","getSigAlgName","getSigAlgOID","getSigAlgParams","getInstance"});
        System.out.println("PASS (" + passed + " assertions)");
    }
}
