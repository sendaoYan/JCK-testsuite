/*
 * @test
 * @summary java.security.spec.EdECPoint conformance: white-box structure + black-box behavior.
 * @run main EdECPointTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.EdECPoint;

public class EdECPointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = EdECPoint.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkMethods(c, new String[]{"isXOdd", "getY"});
    }

    static void testBlackBox() {
        EdECPoint p = new EdECPoint(true, BigInteger.TEN);
        check(p.isXOdd(), "isXOdd true");
        check(BigInteger.TEN.equals(p.getY()), "getY");
        EdECPoint p2 = new EdECPoint(false, BigInteger.ONE);
        check(!p2.isXOdd(), "isXOdd false");
        check(BigInteger.ONE.equals(p2.getY()), "getY false-x point");
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
