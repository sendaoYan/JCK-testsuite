/*
 * @test
 * @summary java.security.spec.EdECPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main EdECPublicKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.EdECPoint;
import java.security.spec.EdECPublicKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;

public class EdECPublicKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = EdECPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams", "getPoint"});
    }

    static void testBlackBox() {
        EdECPoint point = new EdECPoint(false, BigInteger.TEN);
        EdECPublicKeySpec spec = new EdECPublicKeySpec(NamedParameterSpec.ED25519, point);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(point.equals(spec.getPoint()), "getPoint");
        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
