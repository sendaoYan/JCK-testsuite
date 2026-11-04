/*
 * @test
 * @summary java.security.spec.ECPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main ECPublicKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;

public class ECPublicKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = ECPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getW", "getParams"});
    }

    static void testBlackBox() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec params = new ECParameterSpec(curve, new ECPoint(BigInteger.ONE, BigInteger.ONE), new BigInteger("7"), 1);
        ECPoint w = new ECPoint(BigInteger.ONE, BigInteger.TWO);
        ECPublicKeySpec spec = new ECPublicKeySpec(w, params);
        check(w.equals(spec.getW()), "getW");
        check(params.equals(spec.getParams()), "getParams");
        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        ECParameterSpec ps = new ECParameterSpec(new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE),
                new ECPoint(BigInteger.ONE, BigInteger.ONE), new BigInteger("7"), 1);
        ECPoint w = new ECPoint(BigInteger.valueOf(6), BigInteger.valueOf(4));
        // constructor null matrix (probed)
        checkThrows(NullPointerException.class, () -> new ECPublicKeySpec(null, ps),
                "null w -> NPE (probed 'w is null')");
        checkThrows(NullPointerException.class, () -> new ECPublicKeySpec(w, null),
                "null params -> NPE (probed 'params is null')");
        // POINT_INFINITY rejected (probed)
        checkThrows(IllegalArgumentException.class,
                () -> new ECPublicKeySpec(ECPoint.POINT_INFINITY, ps),
                "w == POINT_INFINITY -> IAE (probed)");
        // getters round-trip
        ECPublicKeySpec r = new ECPublicKeySpec(w, ps);
        check(r.getW().equals(w) && r.getParams().equals(ps), "round-trip");
        check(r.getW().getAffineX().intValue() == 6 && r.getW().getAffineY().intValue() == 4,
                "coordinates preserved");
        // no equals override (probed)
        check(!r.equals(new ECPublicKeySpec(w, ps)), "equal-content not equal (probed)");
        check(r.equals(r), "reflexive");
        check(!r.equals(null), "equals null false");
        check(r instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(r.toString().startsWith("java.security.spec.ECPublicKeySpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(ECPublicKeySpec.class), "not Serializable");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
