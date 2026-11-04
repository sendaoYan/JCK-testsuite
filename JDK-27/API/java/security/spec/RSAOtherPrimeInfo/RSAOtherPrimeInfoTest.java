/*
 * @test
 * @summary java.security.spec.RSAOtherPrimeInfo conformance: white-box structure + black-box behavior.
 * @run main RSAOtherPrimeInfoTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAOtherPrimeInfo;

public class RSAOtherPrimeInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAOtherPrimeInfo.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getInterfaces().length == 0, "no interfaces");
        checkMethods(c, new String[]{"getPrime","getExponent","getCrtCoefficient"});
    }

    static void testBlackBox() {
        BigInteger prime = new BigInteger("5");
        BigInteger exponent = new BigInteger("7");
        BigInteger crt = new BigInteger("9");
        RSAOtherPrimeInfo info = new RSAOtherPrimeInfo(prime, exponent, crt);
        check(prime.equals(info.getPrime()), "getPrime");
        check(exponent.equals(info.getExponent()), "getExponent");
        check(crt.equals(info.getCrtCoefficient()), "getCrtCoefficient");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
