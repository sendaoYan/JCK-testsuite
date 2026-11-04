/*
 * @test
 * @summary java.security.spec.ECFieldF2m conformance: white-box structure + black-box behavior.
 * @run main ECFieldF2mTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldF2m;

public class ECFieldF2mTest {
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
        Class<?> c = ECFieldF2m.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.ECField"});
        checkMethods(c, new String[]{"getFieldSize", "getM", "getReductionPolynomial", "getMidTermsOfReductionPolynomial", "equals", "hashCode"});
    }

    static void testBlackBox() {
        // normal basis: int m
        ECFieldF2m f = new ECFieldF2m(5);
        check(f.getFieldSize() == 5, "getFieldSize");
        check(f.getM() == 5, "getM");
        check(f.getMidTermsOfReductionPolynomial() == null, "no mid terms");
        check(f.getReductionPolynomial() == null, "no reduction polynomial");
        check(f.equals(new ECFieldF2m(5)), "equals normal basis");

        // polynomial basis via int[] middle terms
        ECFieldF2m f2 = new ECFieldF2m(5, new int[] { 1 });
        check(f2.getM() == 5, "getM with mid terms");
        check(f2.getMidTermsOfReductionPolynomial().length == 1, "mid terms length");
        check(f2.getMidTermsOfReductionPolynomial()[0] == 1, "mid term value");
        check(new BigInteger("35").equals(f2.getReductionPolynomial()), "reduction polynomial from mid terms");

        // polynomial basis via BigInteger reduction polynomial
        ECFieldF2m f3 = new ECFieldF2m(5, new BigInteger("35"));
        check(new BigInteger("35").equals(f3.getReductionPolynomial()), "getReductionPolynomial");
        check(f3.getMidTermsOfReductionPolynomial().length == 1, "mid terms from rp length");
        check(f3.getMidTermsOfReductionPolynomial()[0] == 1, "mid term from rp value");

        // equality across equivalent constructors
        check(f2.equals(new ECFieldF2m(5, new int[] { 1 })), "equals same mid terms");
        check(f2.equals(f3), "equals int[] vs BigInteger rp");
        check(f2.hashCode() == f3.hashCode(), "hashCode int[] vs BigInteger rp");
        check(!f2.equals(new ECFieldF2m(6, new int[] { 1 })), "unequal m");
        check(!f2.equals(new ECFieldF2m(5, new int[] { 2 })), "unequal mid term");

        // defensive copy of middle terms
        int[] got = f2.getMidTermsOfReductionPolynomial();
        got[0] = 99;
        check(f2.getMidTermsOfReductionPolynomial()[0] == 1, "mid terms defensive copy");

        check(f instanceof ECField, "instanceof ECField");
    }

    static void testContracts() {
        // constructor exception matrix (probed on JDK-27)
        checkThrows(IllegalArgumentException.class, () -> new ECFieldF2m(0), "m == 0 -> IAE");
        checkThrows(IllegalArgumentException.class, () -> new ECFieldF2m(-5), "m < 0 -> IAE");
        checkThrows(NullPointerException.class, () -> new ECFieldF2m(163, (int[]) null), "ks null -> NPE");
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
        check(new ECFieldF2m(163, new int[]{8, 2, 1}).getM() == 163, "ks {8,2,1} legal");
        check(java.util.Arrays.equals(new ECFieldF2m(163, new int[]{74}).getMidTermsOfReductionPolynomial(), new int[]{74}),
                "trinomial ks {74} midTerms");
        // rp constructor rules (probed): valid B-163/B-233, all invalid forms -> IAE
        BigInteger rp163 = BigInteger.ONE.shiftLeft(163).setBit(7).setBit(6).setBit(3).add(BigInteger.ONE);
        BigInteger rp233 = BigInteger.ONE.shiftLeft(233).setBit(74).add(BigInteger.ONE);
        check(java.util.Arrays.equals(new ECFieldF2m(163, rp163).getMidTermsOfReductionPolynomial(), new int[]{7, 6, 3}),
                "rp163 midTerms {7,6,3} (probed)");
        check(java.util.Arrays.equals(new ECFieldF2m(233, rp233).getMidTermsOfReductionPolynomial(), new int[]{74}),
                "rp233 midTerms {74} (probed)");
        checkThrows(NullPointerException.class, () -> new ECFieldF2m(163, (BigInteger) null), "rp null -> NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, rp163.shiftRight(1)), "rp bitLength < m+1 -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, rp163.clearBit(0)), "rp without x^0 -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, rp163.clearBit(163)), "rp without x^m -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, rp163.setBit(200)), "rp bits above m -> IAE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldF2m(163, rp163.negate()), "negative rp -> IAE");
        // cross-constructor equality rp vs ks (probed)
        ECFieldF2m rpTri = new ECFieldF2m(233, rp233);
        check(rpTri.equals(new ECFieldF2m(233, new int[]{74})), "rp form equals ks form");
        check(rpTri.hashCode() == new ECFieldF2m(233, new int[]{74}).hashCode(), "hashCode across forms");
        // equals matrix extras
        check(!new ECFieldF2m(163).equals(new ECFieldF2m(164)), "different m not equal");
        check(!new ECFieldF2m(163).equals(null), "equals null false");
        check(!new ECFieldF2m(163).equals(new java.security.spec.ECFieldFp(BigInteger.valueOf(2))), "wrong type false");
        check(!java.io.Serializable.class.isAssignableFrom(ECFieldF2m.class), "not Serializable");
        // ctor ks defensive copy
        int[] mut = {7, 2, 1};
        ECFieldF2m d = new ECFieldF2m(163, mut);
        mut[0] = 99;
        check(java.util.Arrays.equals(d.getMidTermsOfReductionPolynomial(), new int[]{7, 2, 1}),
                "ctor ks defensive copy (probed)");
        int[] m1 = d.getMidTermsOfReductionPolynomial(), m2 = d.getMidTermsOfReductionPolynomial();
        check(m1 != m2 && java.util.Arrays.equals(m1, m2), "midTerms fresh copy per call");
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
