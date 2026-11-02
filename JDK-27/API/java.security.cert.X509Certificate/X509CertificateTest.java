/*
 * @test
 * @summary java.security.cert.X509Certificate conformance: type shape and members.
 * @run main X509CertificateTest
 */

import java.lang.reflect.*;

public class X509CertificateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509Certificate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.security.cert.Certificate"), "superclass");
        checkInterfaces(c, new String[]{"java.security.cert.X509Extension", "java.security.BinaryEncodable"});
        checkMethods(c, new String[]{"checkValidity", "getBasicConstraints", "getExtendedKeyUsage", "getIssuerAlternativeNames", "getIssuerDN", "getIssuerUniqueID", "getIssuerX500Principal", "getKeyUsage", "getNotAfter", "getNotBefore", "getSerialNumber", "getSigAlgName", "getSigAlgOID", "getSigAlgParams", "getSignature", "getSubjectAlternativeNames", "getSubjectDN", "getSubjectUniqueID", "getSubjectX500Principal", "getTBSCertificate", "getVersion", "verify"});
        System.out.println("PASS (" + passed + " assertions)");
    }
    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }
    static void checkInterfaces(Class<?> c, String[] names) {
        for (String n : names) check(hasInterface(c, n), "implements " + n);
    }
    static void checkMethods(Class<?> c, String[] names) {
        for (String n : names) check(hasMethod(c, n), "method " + n);
    }
    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static void checkEnumConstants(Class<?> c, String[] names) {
        Object[] cs = c.getEnumConstants();
        check(cs != null, "enum constants");
        check(cs.length == names.length, "enum constant count");
        for (String n : names) check(hasConst(cs, n), "constant " + n);
    }
    static boolean hasConst(Object[] cs, String name) {
        for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true;
        return false;
    }
}
