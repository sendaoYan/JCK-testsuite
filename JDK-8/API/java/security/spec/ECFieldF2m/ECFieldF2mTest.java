/*
 * @test
 * @summary java.security.spec.ECFieldF2m conformance: full contract coverage.
 *          All three constructors (normal basis m; pentanomial/trinomial ks int[];
 *          reduction polynomial BigInteger) with full exception matrix
 *          (m <= 0 -> IAE, ks null -> NPE, ks length not 1/3 -> IAE,
 *          non-descending -> IAE, ks out of range -> IAE, rp invalid -> IAE,
 *          rp null -> NPE), cross-constructor equals/hashCode (rp vs ks),
 *          getM/getFieldSize/getReductionPolynomial/getMidTermsOfReductionPolynomial,
 *          defensive copies (ctor ks array, per-call fresh mid-terms), normal-basis
 *          null rp/midTerms, equals against wrong types.
 * @run main ECFieldF2mTest
 */

import java.io.Serializable;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;
import java.security.spec.ECFieldFp;
import java.util.Arrays;

public class ECFieldF2mTest {
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

    // B-163 pentanomial x^163 + x^7 + x^6 + x^3 + 1
    static final BigInteger RP163 =
            BigInteger.ONE.shiftLeft(163).setBit(7).setBit(6).setBit(3).add(BigInteger.ONE);
    // B-233 trinomial x^233 + x^74 + 1
    static final BigInteger RP233 =
            BigInteger.ONE.shiftLeft(233).setBit(74).add(BigInteger.ONE);

    public static void main(String[] args) throws Exception {
        java.security.spec.ECFieldF2m f = new java.security.spec.ECFieldF2m(113);
        check(f.getM() == 113, "getM");
        check(f.getFieldSize() == 113, "getFieldSize");

        // ---- constructor 1: normal basis ----
        checkThrows(IllegalArgumentException.class, () -> new ECFieldF2m(0), "ECFieldF2m(0)");
        checkThrows(IllegalArgumentException.class, () -> new ECFieldF2m(-5), "ECFieldF2m(-5)");
        ECFieldF2m normal = new ECFieldF2m(163);
        check(normal.getM() == 163 && normal.getFieldSize() == 163, "normal basis m/size");
        check(normal.getMidTermsOfReductionPolynomial() == null,
                "normal basis midTerms null (probed)");
        check(normal.getReductionPolynomial() == null,
                "normal basis rp null (probed)");
        check(normal.getReductionPolynomial() == null, "rp null normal");

        // ---- constructor 2: ks int[] matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new ECFieldF2m(163, (int[]) null), "ks null -> NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[0]), "ks empty -> IAE length");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{1, 0}), "ks length 2 -> IAE length");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{7, 2, 1, 0}), "ks length 4 -> IAE length");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{7, 7, 1}), "ks non-descending -> IAE order");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{7, 2, 0}), "ks[2] == 0 -> IAE range");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{7, 2, -1}), "ks[2] < 0 -> IAE range");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{0}), "ks[0] == 0 -> IAE range");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, new int[]{163}), "ks[0] == m -> IAE range");
        // legal: pentanomial and trinomial; ks values only need 0 < k < m and descending
        check(new ECFieldF2m(163, new int[]{8, 2, 1}).getM() == 163, "ks {8,2,1} legal (k < m)");
        check(new ECFieldF2m(163, new int[]{74}).getM() == 163, "trinomial ks {74} legal");

        // pentanomial mid-terms order and value
        ECFieldF2m penta = new ECFieldF2m(163, new int[]{7, 2, 1});
        check(Arrays.equals(new int[]{7, 2, 1},
                penta.getMidTermsOfReductionPolynomial()), "pentanomial midTerms {7,2,1}");
        ECFieldF2m tri = new ECFieldF2m(163, new int[]{74});
        check(Arrays.equals(new int[]{74},
                tri.getMidTermsOfReductionPolynomial()), "trinomial midTerms {74}");

        // defensive copy of constructor ks array
        int[] mut = {7, 2, 1};
        ECFieldF2m defensive = new ECFieldF2m(163, mut);
        mut[0] = 99;
        check(Arrays.equals(new int[]{7, 2, 1},
                defensive.getMidTermsOfReductionPolynomial()), "ctor ks defensive copy");

        // getMidTermsOfReductionPolynomial returns a fresh copy each call
        int[] m1 = defensive.getMidTermsOfReductionPolynomial();
        int[] m2 = defensive.getMidTermsOfReductionPolynomial();
        check(m1 != m2, "midTerms fresh copy per call");
        check(Arrays.equals(m1, m2), "midTerms copies equal content");
        m1[0] = 55;
        check(defensive.getMidTermsOfReductionPolynomial()[0] == 7,
                "mutating returned copy does not affect field");

        // ---- constructor 3: rp BigInteger matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new ECFieldF2m(163, (BigInteger) null), "rp null -> NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, RP163.shiftRight(1)),
                "rp bitLength < m+1 -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, RP163.clearBit(0)),
                "rp without x^0 term -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, RP163.clearBit(163)),
                "rp without x^m term -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, RP163.setBit(200)),
                "rp with bits above m -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, RP163.negate()), "negative rp -> IAE");

        // rp extraction: pentanomial and trinomial mid-terms
        ECFieldF2m rpPenta = new ECFieldF2m(163, RP163);
        check(Arrays.equals(new int[]{7, 6, 3},
                rpPenta.getMidTermsOfReductionPolynomial()), "rp163 midTerms {7,6,3} (probed)");
        check(RP163.equals(rpPenta.getReductionPolynomial()), "getReductionPolynomial round-trip");
        ECFieldF2m rpTri = new ECFieldF2m(233, RP233);
        check(Arrays.equals(new int[]{74},
                rpTri.getMidTermsOfReductionPolynomial()), "rp233 midTerms {74} (probed)");

        // ---- cross-constructor equality: rp form == ks form of same field ----
        check(rpTri.equals(new ECFieldF2m(233, new int[]{74})),
                "rp form equals ks form (trinomial)");
        check(rpTri.hashCode() == new ECFieldF2m(233, new int[]{74}).hashCode(),
                "hashCode consistent across forms");

        // ---- equals / hashCode matrix ----
        ECFieldF2m n1 = new ECFieldF2m(163);
        check(n1.equals(new ECFieldF2m(163)), "normal equals normal same m");
        check(n1.hashCode() == new ECFieldF2m(163).hashCode(), "normal hashCode same m");
        check(!n1.equals(rpPenta), "normal != polynomial basis");
        check(!n1.equals(new ECFieldF2m(164)), "different m not equal");
        check(!n1.equals(null), "equals null false");
        check(!n1.equals(new ECFieldFp(BigInteger.valueOf(2))), "equals wrong type false");
        check(penta.equals(new ECFieldF2m(163, new int[]{7, 2, 1})),
                "pentanomial value equals across instances");
        check(!penta.equals(tri), "pentanomial != trinomial");
        check(penta.equals(penta), "equals reflexive");

        // ---- type contract ----
        check(penta instanceof ECField, "implements ECField");
        check(!Serializable.class.isAssignableFrom(ECFieldF2m.class), "not Serializable");

        // ---- toString: default Object form ----
        check(new ECFieldF2m(163).toString().startsWith("java.security.spec.ECFieldF2m@"),
                "toString default Object form");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
