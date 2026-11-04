/*
 * @test
 * @summary java.security.spec.ECParameterSpec conformance: white-box structure + black-box behavior.
 * @run main ECParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public class ECParameterSpecTest {
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
        Class<?> c = ECParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getCurve", "getGenerator", "getOrder", "getCofactor"});
    }

    static void testBlackBox() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECPoint g = new ECPoint(BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec spec = new ECParameterSpec(curve, g, new BigInteger("7"), 1);
        check(curve.equals(spec.getCurve()), "getCurve");
        check(g.equals(spec.getGenerator()), "getGenerator");
        check(new BigInteger("7").equals(spec.getOrder()), "getOrder");
        check(spec.getCofactor() == 1, "getCofactor");
        check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    static void testContracts() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECPoint g = new ECPoint(BigInteger.ONE, BigInteger.ONE);
        // constructor null matrix (probed: distinct messages)
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(null, g, BigInteger.valueOf(7), 1),
                "null curve -> NPE (probed 'curve is null')");
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(curve, null, BigInteger.valueOf(7), 1),
                "null generator -> NPE (probed 'g is null')");
        checkThrows(NullPointerException.class,
                () -> new ECParameterSpec(curve, g, null, 1),
                "null order -> NPE (probed 'n is null')");
        // cofactor validation (probed)
        checkThrows(IllegalArgumentException.class,
                () -> new ECParameterSpec(curve, g, BigInteger.valueOf(7), 0),
                "cofactor 0 -> IAE (probed 'h is not positive')");
        checkThrows(IllegalArgumentException.class,
                () -> new ECParameterSpec(curve, g, BigInteger.valueOf(7), -1),
                "cofactor -1 -> IAE");
        check(new ECParameterSpec(curve, g, BigInteger.valueOf(7), Integer.MAX_VALUE).getCofactor()
                == Integer.MAX_VALUE, "large cofactor accepted");
        // no equals/hashCode override: identity semantics (probed)
        ECParameterSpec s1 = new ECParameterSpec(curve, g, BigInteger.valueOf(7), 1);
        check(!s1.equals(new ECParameterSpec(curve, g, BigInteger.valueOf(7), 1)),
                "equal-content instances not equal (probed)");
        check(s1.equals(s1), "reflexive");
        // NIST P-256 round-trip
        EllipticCurve p256Curve = new EllipticCurve(
                new ECFieldFp(new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16)),
                new BigInteger("ffffffff00000001000000000000000000000000fffffffffffffffffffffffc", 16),
                new BigInteger("5ac635d8aa3a93e7b3ebbd55769886bc651d06b0cc53b0f63bce3c3e27d2604b", 16));
        ECParameterSpec p256 = new ECParameterSpec(p256Curve,
                new ECPoint(BigInteger.ONE, BigInteger.TEN),
                new BigInteger("ffffffff00000000ffffffffffffffffbce6faada7179e84f3b9cac2fc632551", 16), 1);
        check(p256.getCurve().equals(p256Curve), "P-256 curve round-trip");
        check(p256.getOrder().bitLength() == 256, "P-256 order 256 bits");
        check(p256.getCurve().getField().getFieldSize() == 256, "P-256 field size");
        check(!java.io.Serializable.class.isAssignableFrom(ECParameterSpec.class), "not Serializable");
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
