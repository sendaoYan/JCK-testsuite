/*
 * @test
 * @summary sun.security.validator.ValidatorException conformance: internal package reflection.
 * @modules java.base/sun.security.validator:+open
 * @run main ValidatorExceptionTest
 */
import java.lang.reflect.*;
import java.util.*;

public class ValidatorExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.validator.ValidatorException", false, ValidatorExceptionTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.validator.ValidatorException".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.security.cert.CertificateException".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("getErrorCertificate", "getErrorType"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("T_ALGORITHM_DISABLED", "T_CA_EXTENSIONS", "T_CERT_EXPIRED", "T_EE_EXTENSIONS", "T_NAME_CHAINING", "T_NO_TRUST_ANCHOR", "T_SIGNATURE_ERROR", "T_UNTRUSTED_CERT"))), "fields");
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
