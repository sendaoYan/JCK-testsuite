/*
 * @test
 * @summary sun.security.pkcs.ContentInfo conformance: internal package reflection.
 * @modules java.base/sun.security.pkcs:+open
 * @run main ContentInfoTest
 */
import java.lang.reflect.*;
import java.util.*;

public class ContentInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.pkcs.ContentInfo", false, ContentInfoTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.pkcs.ContentInfo".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("sun.security.util.DerEncoder"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("encode", "getContent", "getContentBytes", "getContentType", "getData", "toString"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("DATA_OID", "DIGESTED_DATA_OID", "ENCRYPTED_DATA_OID", "ENVELOPED_DATA_OID", "NETSCAPE_CERT_SEQUENCE_OID", "OLD_DATA_OID", "OLD_SIGNED_DATA_OID", "PKCS7_OID", "SIGNED_AND_ENVELOPED_DATA_OID", "SIGNED_DATA_OID", "TIMESTAMP_TOKEN_INFO_OID"))), "fields");
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
