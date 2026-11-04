/*
 * @test
 * @summary java.security.spec.ECFieldFp conformance: full contract coverage.
 *          Constructor matrix (null -> NPE, non-positive -> IAE "p is not positive",
 *          p == 1 legal), getFieldSize == p.bitLength(), getP round-trip (same value),
 *          equals/hashCode value semantics (equal p, different p, null, wrong type),
 *          toString format, and non-Serializable contract.
 * @run main ECFieldFpTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;

public class ECFieldFpTest {
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
        java.math.BigInteger p = new java.math.BigInteger("23");
        java.security.spec.ECFieldFp f = new java.security.spec.ECFieldFp(p);
        check(p.equals(f.getP()), "getP");
        check(f.getFieldSize() == 5, "getFieldSize");

        // ---- constructor matrix (probed: NPE for null, IAE for signum <= 0, p == 1 legal) ----
        checkThrows(NullPointerException.class, () -> new ECFieldFp(null), "ECFieldFp(null)");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldFp(BigInteger.valueOf(-7)), "ECFieldFp(-7)");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldFp(BigInteger.ZERO), "ECFieldFp(0)");
        check(new ECFieldFp(BigInteger.ONE).getFieldSize() == 1, "p == 1 legal, size 1");

        // primality is NOT validated: any positive BigInteger is accepted
        ECFieldFp comp = new ECFieldFp(BigInteger.valueOf(22)); // 22 composite
        check(comp.getP().intValue() == 22, "composite p accepted (no primality check)");

        // ---- getFieldSize == p.bitLength() across magnitudes ----
        check(new ECFieldFp(BigInteger.valueOf(23)).getFieldSize() == 5, "23 -> 5");
        check(new ECFieldFp(BigInteger.valueOf(2)).getFieldSize() == 2, "2 -> 2");
        check(new ECFieldFp(P256).getFieldSize() == 256, "NIST P-256 p -> 256");

        // ---- getP round-trip (value identity, stable across calls) ----
        ECFieldFp big = new ECFieldFp(P256);
        check(P256.equals(big.getP()), "getP round-trip P-256");
        check(big.getP().equals(big.getP()), "getP stable across calls");

        // ---- equals / hashCode value semantics ----
        ECFieldFp a = new ECFieldFp(BigInteger.valueOf(23));
        ECFieldFp b = new ECFieldFp(BigInteger.valueOf(23));
        ECFieldFp c = new ECFieldFp(BigInteger.valueOf(24));
        check(a.equals(b), "equals same p");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "hashCode equal for equal p");
        check(!a.equals(c), "equals different p");
        check(!a.equals(null), "equals null false");
        check(!a.equals("23"), "equals wrong type false");
        check(a.equals(a), "equals reflexive");

        // equal-valued but distinct BigInteger instances still equal
        ECFieldFp d = new ECFieldFp(new BigInteger("17", 10));
        ECFieldFp e = new ECFieldFp(new BigInteger(new byte[]{17}));
        check(d.equals(e), "equals across distinct BigInteger instances of same value");

        // ---- interface / type contract ----
        check(a instanceof ECField, "implements ECField");
        check(!ECFieldF2m.class.isInstance(a), "not ECFieldF2m");

        // ---- toString: default Object form (class@hexHash) ----
        String ts = a.toString();
        check(ts.startsWith("java.security.spec.ECFieldFp@"), "toString default Object form: " + ts);

        // ---- not Serializable (javadoc defines no serialization contract) ----
        check(!Serializable.class.isAssignableFrom(ECFieldFp.class), "ECFieldFp not Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
