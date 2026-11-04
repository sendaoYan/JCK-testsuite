/*
 * @test
 * @summary sun.security.x509.X509CertImpl conformance: internal package reflection.
 * @modules java.base/sun.security.x509:+open
 * @run main X509CertImplTest
 */
import java.lang.reflect.*;
import java.util.*;

public class X509CertImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.X509CertImpl", false, X509CertImplTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.x509.X509CertImpl".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.security.cert.X509Certificate".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("sun.security.util.DerEncoder"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("checkValidity", "encode", "getAuthKeyId", "getAuthorityInfoAccessExtension", "getAuthorityKeyIdentifierExtension", "getBasicConstraints", "getBasicConstraintsExtension", "getCRLDistributionPointsExtension", "getCertificatePoliciesExtension", "getCriticalExtensionOIDs", "getEncoded", "getEncodedInternal", "getExtendedKeyUsage", "getExtendedKeyUsageExtension", "getExtension", "getExtensionValue", "getFingerprint", "getInfo", "getIssuerAlternativeNameExtension", "getIssuerAlternativeNames", "getIssuerDN", "getIssuerUniqueID", "getIssuerX500Principal", "getKeyUsage", "getNameConstraintsExtension", "getNonCriticalExtensionOIDs", "getNotAfter", "getNotBefore", "getPolicyConstraintsExtension", "getPolicyMappingsExtension", "getPrivateKeyUsageExtension", "getPublicKey", "getSerialNumber", "getSerialNumberObject", "getSigAlg", "getSigAlgName", "getSigAlgOID", "getSigAlgParams", "getSignature", "getSubjectAlternativeNameExtension", "getSubjectAlternativeNames", "getSubjectDN", "getSubjectKeyId", "getSubjectKeyIdentifierExtension", "getSubjectUniqueID", "getSubjectX500Principal", "getTBSCertificate", "getUnparseableExtension", "getVersion", "hasUnsupportedCriticalExtension", "isSelfIssued", "isSelfSigned", "newSigned", "newX509CertImpl", "toImpl", "toString", "verify"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("NAME", "algId", "info", "signature"))), "fields");
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
