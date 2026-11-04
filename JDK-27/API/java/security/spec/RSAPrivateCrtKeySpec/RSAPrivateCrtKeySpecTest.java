/*
 * @test
 * @summary java.security.spec.RSAPrivateCrtKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAPrivateCrtKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.NamedParameterSpec;

public class RSAPrivateCrtKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAPrivateCrtKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.security.spec.RSAPrivateKeySpec"), "superclass RSAPrivateKeySpec");
        checkMethods(c, new String[]{"getPublicExponent","getPrimeP","getPrimeQ","getPrimeExponentP","getPrimeExponentQ","getCrtCoefficient"});
    }

    static void testBlackBox() {
        BigInteger[] v = new BigInteger[8];
        for (int i = 0; i < 8; i++) v[i] = BigInteger.valueOf(i + 1);
        RSAPrivateCrtKeySpec spec = new RSAPrivateCrtKeySpec(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7]);
        check(v[0].equals(spec.getModulus()), "getModulus");
        check(v[1].equals(spec.getPublicExponent()), "getPublicExponent");
        check(v[2].equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(v[3].equals(spec.getPrimeP()), "getPrimeP");
        check(v[4].equals(spec.getPrimeQ()), "getPrimeQ");
        check(v[5].equals(spec.getPrimeExponentP()), "getPrimeExponentP");
        check(v[6].equals(spec.getPrimeExponentQ()), "getPrimeExponentQ");
        check(v[7].equals(spec.getCrtCoefficient()), "getCrtCoefficient");
        check(spec.getParams() == null, "getParams null");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAPrivateCrtKeySpec spec2 = new RSAPrivateCrtKeySpec(v[0], v[1], v[2], v[3], v[4], v[5], v[6], v[7], params);
        check(params == spec2.getParams(), "9-arg getParams");
        check(v[3].equals(spec2.getPrimeP()), "9-arg getPrimeP");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
