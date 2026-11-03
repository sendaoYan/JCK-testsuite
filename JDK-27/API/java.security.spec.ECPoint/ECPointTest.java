/*
 * @test
 * @summary java.security.spec.ECPoint conformance: white-box structure + black-box behavior.
 * @run main ECPointTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECPoint;

public class ECPointTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = ECPoint.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkMethods(c, new String[]{"getAffineX", "getAffineY", "equals", "hashCode"});
        check(c.getField("POINT_INFINITY") != null, "field POINT_INFINITY");
    }

    static void testBlackBox() {
        ECPoint p = new ECPoint(BigInteger.ONE, BigInteger.TWO);
        check(BigInteger.ONE.equals(p.getAffineX()), "getAffineX");
        check(BigInteger.TWO.equals(p.getAffineY()), "getAffineY");

        // point at infinity
        ECPoint inf = ECPoint.POINT_INFINITY;
        check(inf != null, "POINT_INFINITY non-null");
        check(inf.getAffineX() == null, "POINT_INFINITY x null");
        check(inf.getAffineY() == null, "POINT_INFINITY y null");
        check(inf.equals(ECPoint.POINT_INFINITY), "POINT_INFINITY equals self");
        check(inf.hashCode() == 0, "POINT_INFINITY hashCode 0");
        check(!inf.equals(new ECPoint(BigInteger.ZERO, BigInteger.ZERO)), "POINT_INFINITY distinct from (0,0)");

        // equality and hashing for affine points
        check(p.equals(new ECPoint(BigInteger.ONE, BigInteger.TWO)), "equals");
        check(p.hashCode() == new ECPoint(BigInteger.ONE, BigInteger.TWO).hashCode(), "hashCode");
        check(!p.equals(new ECPoint(BigInteger.ONE, BigInteger.ONE)), "unequal y");
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
