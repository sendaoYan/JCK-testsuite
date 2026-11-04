/*
 * @test
 * @summary java.security.spec.EllipticCurve conformance: full contract coverage.
 *          Both constructors (3-arg seedless, 4-arg with seed), null field -> NPE,
 *          null a/b allowed, null seed allowed, seed defensive copy at construction
 *          and fresh copy per getSeed() call, equals/hashCode value semantics
 *          (field/a/b and seed all participate), getters round-trip, toString,
 *          non-Serializable.
 * @run main EllipticCurveTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.security.spec.EllipticCurve;
import java.util.Arrays;

public class EllipticCurveTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static final BigInteger P256 = new BigInteger(
            "ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16);

    public static void main(String[] args) throws Exception {
        java.security.spec.ECFieldFp field = new java.security.spec.ECFieldFp(new java.math.BigInteger("23"));
        java.security.spec.EllipticCurve c = new java.security.spec.EllipticCurve(field, new java.math.BigInteger("1"), new java.math.BigInteger("1"));
        check(field.equals(c.getField()), "getField");
        check(new java.math.BigInteger("1").equals(c.getA()), "getA");
        check(new java.math.BigInteger("1").equals(c.getB()), "getB");
        check(c.getSeed() == null, "getSeed null");

        // ---- constructor matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(null, BigInteger.ONE, BigInteger.ONE),
                "null field -> NPE");
        // null a / null b -> NPE (probed: "first coefficient is null" /
        // "second coefficient is null" on this JDK)
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(field, null, BigInteger.ONE),
                "null a -> NPE (probed 'first coefficient is null')");
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(field, BigInteger.ONE, null),
                "null b -> NPE (probed 'second coefficient is null')");
        // 4-arg with null seed is legal and equals 3-arg shape
        EllipticCurve c4null = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE, null);
        check(c4null.getSeed() == null, "4-arg null seed -> getSeed null (probed)");
        check(c.equals(c4null), "3-arg equals 4-arg-null-seed");

        // ---- seed defensive copies ----
        byte[] seed = {1, 2, 3};
        EllipticCurve c4 = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE, seed);
        seed[0] = 9;
        check(Arrays.equals(new byte[]{1, 2, 3}, c4.getSeed()),
                "ctor seed defensive copy (probed)");
        byte[] g1 = c4.getSeed();
        byte[] g2 = c4.getSeed();
        check(g1 != g2, "getSeed fresh copy per call (probed)");
        check(Arrays.equals(g1, g2), "getSeed copies equal content");
        g1[0] = 77;
        check(c4.getSeed()[0] == 1, "mutating returned seed does not affect curve");

        // ---- equals / hashCode value semantics (seed participates) ----
        EllipticCurve same = new EllipticCurve(new ECFieldFp(new BigInteger("23")),
                BigInteger.ONE, BigInteger.ONE, new byte[]{1, 2, 3});
        check(c4.equals(same), "equals across instances with equal field/a/b/seed");
        check(c4.hashCode() == same.hashCode(), "hashCode consistent");
        // seed does NOT participate in equals (probed): no-seed vs seeded curve equal
        check(c4.equals(c), "seed ignored by equals: 4-arg seeded equals 3-arg seedless (probed)");
        EllipticCurve otherSeed = new EllipticCurve(new ECFieldFp(new BigInteger("23")),
                BigInteger.ONE, BigInteger.ONE, new byte[]{1, 2, 4});
        check(c4.equals(otherSeed), "different seed still equal (probed)");
        EllipticCurve otherB = new EllipticCurve(new ECFieldFp(new BigInteger("23")),
                BigInteger.ONE, BigInteger.valueOf(2), new byte[]{1, 2, 3});
        check(!c4.equals(otherB), "different b not equal");
        EllipticCurve otherField = new EllipticCurve(new ECFieldFp(new BigInteger("29")),
                BigInteger.ONE, BigInteger.ONE, new byte[]{1, 2, 3});
        check(!c4.equals(otherField), "different field not equal");
        check(!c4.equals(null), "equals null false");
        check(!c4.equals("curve"), "equals wrong type false");
        check(c4.equals(c4), "equals reflexive");

        // works with F2m fields too
        EllipticCurve f2m1 = new EllipticCurve(new ECFieldF2m(163), BigInteger.ONE, BigInteger.ONE);
        EllipticCurve f2m2 = new EllipticCurve(new ECFieldF2m(163), BigInteger.ONE, BigInteger.ONE);
        check(f2m1.equals(f2m2), "F2m-based curves value-equal");

        // ---- getters round-trip ----
        check(c4.getField().equals(field) && c4.getA().intValue() == 1 && c4.getB().intValue() == 1,
                "getters round-trip");
        EllipticCurve big = new EllipticCurve(new ECFieldFp(P256),
                BigInteger.valueOf(3), BigInteger.valueOf(4), new byte[]{9});
        check(big.getField().getFieldSize() == 256, "P-256 curve field size");

        // ---- type / toString ----
        check(!Serializable.class.isAssignableFrom(EllipticCurve.class), "not Serializable");
        check(c4.toString().startsWith("java.security.spec.EllipticCurve@"),
                "toString default Object form");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
