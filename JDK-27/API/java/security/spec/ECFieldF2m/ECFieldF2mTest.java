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
