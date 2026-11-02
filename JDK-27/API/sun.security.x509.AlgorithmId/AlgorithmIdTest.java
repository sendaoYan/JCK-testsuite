/*
 * @test
 * @summary sun.security.x509.AlgorithmId conformance: internal package reflection.
 * @modules java.base/sun.security.x509:+open
 * @run main AlgorithmIdTest
 */
import java.lang.reflect.*;
import java.util.*;

public class AlgorithmIdTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.x509.AlgorithmId", false, AlgorithmIdTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.x509.AlgorithmId".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("java.io.Serializable", "sun.security.util.DerEncoder"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("clearAliasOidsTable", "decodeParams", "encode", "equals", "equalsOID", "get", "getAlgorithmId", "getEncodedParams", "getName", "getOID", "getParameters", "hashCode", "paramsToString", "parse", "toString"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("DSA_oid", "EC_oid", "MD2_oid", "MD5_oid", "MGF1_oid", "RSAEncryption_oid", "RSASSA_PSS_oid", "SHA224_oid", "SHA256_oid", "SHA384_oid", "SHA3_224_oid", "SHA3_256_oid", "SHA3_384_oid", "SHA3_512_oid", "SHA512_224_oid", "SHA512_256_oid", "SHA512_oid", "SHAKE128_256_oid", "SHAKE256_512_oid", "SHA_oid", "encodedParams"))), "fields");
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
