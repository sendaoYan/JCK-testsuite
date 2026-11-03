/*
 * @test
 * @summary java.security.spec.RSAMultiPrimePrivateCrtKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAMultiPrimePrivateCrtKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAMultiPrimePrivateCrtKeySpec;
import java.security.spec.RSAOtherPrimeInfo;
import java.security.spec.NamedParameterSpec;

public class RSAMultiPrimePrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAMultiPrimePrivateCrtKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.RSAPrivateKeySpec"), "superclass RSAPrivateKeySpec");
        checkMethods(c, new String[]{"getPublicExponent","getPrimeP","getPrimeQ","getPrimeExponentP","getPrimeExponentQ","getCrtCoefficient","getOtherPrimeInfo"});
    }

    static void testBlackBox() {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAOtherPrimeInfo[] other = new RSAOtherPrimeInfo[] {
            new RSAOtherPrimeInfo(new BigInteger("11"), new BigInteger("13"), new BigInteger("17"))
        };
        RSAMultiPrimePrivateCrtKeySpec spec = new RSAMultiPrimePrivateCrtKeySpec(
            v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], other);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        check(spec.getParams() == null, "getParams null");

        RSAOtherPrimeInfo[] got = spec.getOtherPrimeInfo();
        check(got != other, "getOtherPrimeInfo defensive copy");
        check(got.length == 1, "getOtherPrimeInfo length");
        check(new BigInteger("11").equals(got[0].getPrime()), "other prime");
        got[0] = new RSAOtherPrimeInfo(new BigInteger("1"), new BigInteger("1"), new BigInteger("1"));
        check(spec.getOtherPrimeInfo()[0].getPrime().equals(new BigInteger("11")), "getOtherPrimeInfo unaffected by mutation");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAMultiPrimePrivateCrtKeySpec spec2 = new RSAMultiPrimePrivateCrtKeySpec(
            v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], other, params);
        check(params == spec2.getParams(), "10-arg getParams");
        check(spec2.getOtherPrimeInfo().length == 1, "10-arg getOtherPrimeInfo length");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
