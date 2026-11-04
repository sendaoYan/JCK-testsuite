/*
 * @test
 * @summary java.security.spec.ECPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main ECPrivateKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.KeySpec;

public class ECPrivateKeySpecTest {
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
        Class<?> c = ECPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getS", "getParams"});
    }

    static void testBlackBox() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec params = new ECParameterSpec(curve, new ECPoint(BigInteger.ONE, BigInteger.ONE), new BigInteger("7"), 1);
        ECPrivateKeySpec spec = new ECPrivateKeySpec(new BigInteger("5"), params);
        check(new BigInteger("5").equals(spec.getS()), "getS");
        check(params.equals(spec.getParams()), "getParams");
        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        ECParameterSpec ps = new ECParameterSpec(new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE),
                new ECPoint(BigInteger.ONE, BigInteger.ONE), new BigInteger("7"), 1);
        BigInteger s = BigInteger.valueOf(5);
        // constructor null matrix (probed)
        checkThrows(NullPointerException.class, () -> new ECPrivateKeySpec(null, ps),
                "null s -> NPE (probed 's is null')");
        checkThrows(NullPointerException.class, () -> new ECPrivateKeySpec(s, null),
                "null params -> NPE (probed 'params is null')");
        // no range validation of the scalar (probed)
        check(new ECPrivateKeySpec(BigInteger.ZERO, ps).getS().intValue() == 0, "scalar 0 accepted");
        check(new ECPrivateKeySpec(BigInteger.valueOf(-9), ps).getS().intValue() == -9,
                "negative scalar accepted");
        BigInteger huge = new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16);
        check(new ECPrivateKeySpec(huge, ps).getS().equals(huge), "order-sized scalar accepted");
        // getters round-trip / identity semantics (probed)
        ECPrivateKeySpec r = new ECPrivateKeySpec(s, ps);
        check(r.getS().equals(s) && r.getParams().equals(ps), "round-trip");
        check(!r.equals(new ECPrivateKeySpec(s, ps)), "equal-content not equal (probed)");
        check(r.equals(r), "reflexive");
        check(!r.equals(null), "equals null false");
        check(r instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(!java.io.Serializable.class.isAssignableFrom(ECPrivateKeySpec.class), "not Serializable");
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
