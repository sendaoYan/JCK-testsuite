/*
 * @test
 * @summary java.security.spec.ECFieldFp conformance: white-box structure + black-box behavior.
 * @run main ECFieldFpTest
 */

import java.security.spec.ECFieldF2m;
import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.ECField;
import java.security.spec.ECFieldFp;

public class ECFieldFpTest {
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
        Class<?> c = ECFieldFp.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.ECField"});
        checkMethods(c, new String[]{"getFieldSize", "getP", "equals", "hashCode"});
    }

    static void testBlackBox() {
        BigInteger p = new BigInteger("23");
        ECFieldFp f = new ECFieldFp(p);
        check(p.equals(f.getP()), "getP");
        check(f.getFieldSize() == 5, "getFieldSize 5 (23 < 32)");
        ECFieldFp f2 = new ECFieldFp(new BigInteger("23"));
        check(f.equals(f2), "equals");
        check(f.hashCode() == f2.hashCode(), "hashCode");
        check(!f.equals(new ECFieldFp(new BigInteger("29"))), "unequal p");
        check(!f.equals("23"), "unequal to non-ECFieldFp");
        check(f instanceof ECField, "instanceof ECField");
    }

    static void testContracts() {
        // constructor matrix (probed on JDK-27)
        checkThrows(NullPointerException.class, () -> new ECFieldFp(null), "null p -> NPE");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldFp(BigInteger.valueOf(-7)), "negative p -> IAE 'p is not positive'");
        checkThrows(IllegalArgumentException.class,
                () -> new ECFieldFp(BigInteger.ZERO), "zero p -> IAE");
        check(new ECFieldFp(BigInteger.ONE).getFieldSize() == 1, "p == 1 legal, size 1");
        check(new ECFieldFp(BigInteger.valueOf(22)).getP().intValue() == 22,
                "composite p accepted (no primality check)");
        // field size == bit length across magnitudes
        check(new ECFieldFp(new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16)).getFieldSize() == 256, "P-256 p -> 256");
        check(new ECFieldFp(BigInteger.valueOf(2)).getFieldSize() == 2, "2 -> 2");
        // equals matrix
        ECFieldFp a = new ECFieldFp(BigInteger.valueOf(23));
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "equals null false");
        check(a.equals(new ECFieldFp(new BigInteger("17".replace("17","23")))), "value equals");
        check(!a.equals(new ECFieldFp(BigInteger.valueOf(29))), "different p");
        check(!ECFieldF2m.class.isInstance(a), "not an ECFieldF2m");
        // toString / serializable shape
        check(a.toString().startsWith("java.security.spec.ECFieldFp@"), "toString default Object form");
        check(!java.io.Serializable.class.isAssignableFrom(ECFieldFp.class), "not Serializable");
        // getP stable
        ECFieldFp big = new ECFieldFp(new BigInteger("ffffffff00000001000000000000000000000000ffffffffffffffffffffffff", 16));
        check(big.getP().equals(big.getP()), "getP stable");
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
