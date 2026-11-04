/*
 * @test
 * @summary sun.security.rsa.RSAPublicKeyImpl conformance: internal package reflection.
 * @modules java.base/sun.security.rsa:+open
 * @run main RSAPublicKeyImplTest
 */
import java.lang.reflect.*;
import java.util.*;

public class RSAPublicKeyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.rsa.RSAPublicKeyImpl", false, RSAPublicKeyImplTest.class.getClassLoader());
        check(c != null, "loaded");
        check("sun.security.rsa.RSAPublicKeyImpl".equals(c.getName()), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check("sun.security.x509.X509Key".equals(c.getSuperclass() == null ? "" : c.getSuperclass().getName()), "superclass");
        check(interfaceNames(c).equals(new TreeSet<>(Arrays.asList("java.security.interfaces.RSAPublicKey"))), "interfaces");
        check(methodNames(c).equals(new TreeSet<>(Arrays.asList("getAlgorithm", "getModulus", "getParams", "getPublicExponent", "newKey", "parseKeyBits", "toString"))), "methods");
        check(fieldNames(c).equals(new TreeSet<String>()), "fields");
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
