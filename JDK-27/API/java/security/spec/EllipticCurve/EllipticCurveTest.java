/*
 * @test
 * @summary java.security.spec.EllipticCurve conformance: white-box structure + black-box behavior.
 * @run main EllipticCurveTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.EllipticCurve;
import java.security.spec.ECFieldFp;
import java.util.Arrays;

public class EllipticCurveTest {
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


    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = EllipticCurve.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        checkMethods(c, new String[]{"getField","getA","getB","getSeed","equals","hashCode"});
    }

    static void testBlackBox() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve c = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        check(field.equals(c.getField()), "getField");
        check(BigInteger.ONE.equals(c.getA()), "getA");
        check(BigInteger.ONE.equals(c.getB()), "getB");
        check(c.getSeed() == null, "getSeed null");
        EllipticCurve c2 = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        check(c.equals(c2), "equals");
        check(c.hashCode() == c2.hashCode(), "hashCode");

        byte[] seed = new byte[]{1,2,3,4};
        EllipticCurve c3 = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE, seed);
        byte[] got = c3.getSeed();
        check(got != seed, "getSeed defensive copy");
        check(Arrays.equals(seed, got), "getSeed content");
        seed[0] = 99;
        check(c3.getSeed()[0] == 1, "constructor defensive copy of seed");
        check(c.equals(c3), "equals ignores seed");
        check(c.hashCode() == c3.hashCode(), "hashCode ignores seed");
    }

    static void testContracts() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        // constructor matrix (probed on JDK-27)
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(null, BigInteger.ONE, BigInteger.ONE), "null field -> NPE");
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(field, null, BigInteger.ONE),
                "null a -> NPE (probed 'first coefficient is null')");
        checkThrows(NullPointerException.class,
                () -> new EllipticCurve(field, BigInteger.ONE, null),
                "null b -> NPE (probed 'second coefficient is null')");
        // 4-arg with null seed legal; equals 3-arg shape
        EllipticCurve c4null = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE, null);
        check(c4null.getSeed() == null, "4-arg null seed -> getSeed null");
        check(new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE).equals(c4null),
                "3-arg equals 4-arg null seed");
        // getSeed fresh copy per call
        EllipticCurve c4 = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE, new byte[]{1, 2, 3});
        byte[] g1 = c4.getSeed(), g2 = c4.getSeed();
        check(g1 != g2 && java.util.Arrays.equals(g1, g2), "getSeed fresh copy per call");
        // equals: field/a/b participate
        check(!c4.equals(new EllipticCurve(new ECFieldFp(new BigInteger("29")), BigInteger.ONE, BigInteger.ONE, new byte[]{1,2,3})),
                "different field not equal");
        check(!c4.equals(new EllipticCurve(field, BigInteger.ONE, BigInteger.valueOf(2), new byte[]{1,2,3})),
                "different b not equal");
        check(!c4.equals(null), "equals null false");
        check(c4.equals(c4), "reflexive");
        // works over F2m fields
        check(new EllipticCurve(new java.security.spec.ECFieldF2m(163), BigInteger.ONE, BigInteger.ONE)
                .equals(new EllipticCurve(new java.security.spec.ECFieldF2m(163), BigInteger.ONE, BigInteger.ONE)),
                "F2m-based curves value-equal");
        check(!java.io.Serializable.class.isAssignableFrom(EllipticCurve.class), "not Serializable");
    }
    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
