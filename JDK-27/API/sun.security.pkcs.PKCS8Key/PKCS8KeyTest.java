/*
 * @test
 * @summary sun.security.pkcs.PKCS8Key conformance: internal package reflection.
 * @modules java.base/sun.security.pkcs:+open
 * @run main PKCS8KeyTest
 */
import java.lang.reflect.*;
import java.util.*;

public class PKCS8KeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.pkcs.PKCS8Key", false, PKCS8KeyTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.pkcs.PKCS8Key".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("java.security.PrivateKey", "sun.security.util.InternalPrivateKey"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("clear", "equals", "getAlgorithm", "getAlgorithmId", "getEncoded", "getFormat", "getPubKeyEncoded", "getVersion", "hasPublicKey", "hashCode", "parseKey", "writeReplace"))), "methods");
        check(fieldNames(c).equals(new TreeSet<>(Arrays.asList("V1", "V2", "algid", "encodedKey", "privKeyMaterial", "pubKeyEncoded"))), "fields");
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
