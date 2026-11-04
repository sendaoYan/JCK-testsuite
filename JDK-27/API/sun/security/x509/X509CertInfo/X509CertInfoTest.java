/*
 * @test
 * @summary sun.security.x509.X509CertInfo conformance: internal package reflection.
 * @modules java.base/sun.security.x509:+open
 * @run main X509CertInfoTest
 */
import java.lang.reflect.*;
import java.util.*;

public class X509CertInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.X509CertInfo", false, X509CertInfoTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.x509.X509CertInfo".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("encode", "equals", "getAlgorithmId", "getEncodedInfo", "getExtensions", "getIssuer", "getIssuerUniqueId", "getKey", "getSerialNumber", "getSubject", "getSubjectUniqueId", "getValidity", "getVersion", "hashCode", "setAlgorithmId", "setExtensions", "setIssuer", "setIssuerUniqueId", "setKey", "setSerialNumber", "setSubject", "setSubjectUniqueId", "setValidity", "setVersion", "toString"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("ALGORITHM_ID", "DN_NAME", "EXTENSIONS", "ISSUER", "ISSUER_ID", "KEY", "NAME", "SERIAL_NUMBER", "SUBJECT", "SUBJECT_ID", "VALIDITY", "VERSION", "algId", "extensions", "interval", "issuer", "issuerUniqueId", "pubKey", "serialNum", "subject", "subjectUniqueId", "version"))), "fields");
        System.out.println("PASS (" + passed + " assertions)");
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mm = m.getModifiers();
            if (Modifier.isPublic(mm) || Modifier.isProtected(mm)) s.add(m.getName());
        }
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) continue;
            int fm = fld.getModifiers();
            if (Modifier.isPublic(fm) || Modifier.isProtected(fm)) s.add(fld.getName());
        }
        return s;
    }
    static Set<String> enumConstantNames(Class<?> c) {
        Set<String> s = new TreeSet<>();
        for (Field fld : c.getDeclaredFields()) {
            if (fld.isEnumConstant()) s.add(fld.getName());
        }
        return s;
    }
}
