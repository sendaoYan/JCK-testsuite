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

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
