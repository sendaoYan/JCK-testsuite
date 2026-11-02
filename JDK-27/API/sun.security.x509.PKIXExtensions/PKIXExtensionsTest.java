/*
 * @test
 * @summary sun.security.x509.PKIXExtensions conformance: internal package reflection.
 * @modules java.base/sun.security.x509:+open
 * @run main PKIXExtensionsTest
 */
import java.lang.reflect.*;
import java.util.*;

public class PKIXExtensionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.PKIXExtensions", false, PKIXExtensionsTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.x509.PKIXExtensions".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<String>()), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("AuthInfoAccess_Id", "AuthorityKey_Id", "BasicConstraints_Id", "CRLDistributionPoints_Id", "CRLNumber_Id", "CertificateIssuer_Id", "CertificatePolicies_Id", "DeltaCRLIndicator_Id", "ExtendedKeyUsage_Id", "FreshestCRL_Id", "HoldInstructionCode_Id", "InhibitAnyPolicy_Id", "InvalidityDate_Id", "IssuerAlternativeName_Id", "IssuingDistributionPoint_Id", "KeyUsage_Id", "NameConstraints_Id", "OCSPNoCheck_Id", "OCSPNonce_Id", "PolicyConstraints_Id", "PolicyMappings_Id", "PrivateKeyUsage_Id", "ReasonCode_Id", "SubjectAlternativeName_Id", "SubjectDirectoryAttributes_Id", "SubjectInfoAccess_Id", "SubjectKey_Id"))), "fields");
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
