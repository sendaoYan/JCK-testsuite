/*
 * @test
 * @summary sun.security.ssl.Alert conformance: internal package reflection.
 * @modules java.base/sun.security.ssl:+open
 * @run main AlertTest
 */
import java.lang.reflect.*;
import java.util.*;

public class AlertTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.ssl.Alert", false, AlertTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.ssl.Alert".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("java.lang.Enum".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<String>()), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("nameOf", "valueOf", "values"))), "methods");
        check(fieldNames(c).equals(new TreeSet<String>()), "fields");
        check(enumConstantNames(c).equals(new TreeSet<>(Arrays.asList("ACCESS_DENIED", "BAD_CERTIFICATE", "BAD_CERT_HASH_VALUE", "BAD_CERT_STATUS_RESPONSE", "BAD_RECORD_MAC", "CERTIFICATE_EXPIRED", "CERTIFICATE_REQUIRED", "CERTIFICATE_REVOKED", "CERTIFICATE_UNKNOWN", "CERT_UNOBTAINABLE", "CLOSE_NOTIFY", "DECODE_ERROR", "DECOMPRESSION_FAILURE", "DECRYPTION_FAILED", "DECRYPT_ERROR", "EXPORT_RESTRICTION", "HANDSHAKE_FAILURE", "ILLEGAL_PARAMETER", "INAPPROPRIATE_FALLBACK", "INSUFFICIENT_SECURITY", "INTERNAL_ERROR", "MISSING_EXTENSION", "NO_APPLICATION_PROTOCOL", "NO_CERTIFICATE", "NO_RENEGOTIATION", "PROTOCOL_VERSION", "RECORD_OVERFLOW", "UNEXPECTED_MESSAGE", "UNKNOWN_CA", "UNKNOWN_PSK_IDENTITY", "UNRECOGNIZED_NAME", "UNSUPPORTED_CERTIFICATE", "UNSUPPORTED_EXTENSION", "USER_CANCELED"))), "enumConstants");
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
