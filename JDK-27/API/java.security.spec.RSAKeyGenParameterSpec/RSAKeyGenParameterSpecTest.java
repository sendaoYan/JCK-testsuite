/*
 * @test
 * @summary java.security.spec.RSAKeyGenParameterSpec conformance: white-box structure + black-box behavior.
 * @run main RSAKeyGenParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.NamedParameterSpec;

public class RSAKeyGenParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAKeyGenParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getKeysize","getPublicExponent","getKeyParams"});
    }

    static void testBlackBox() {
        check(RSAKeyGenParameterSpec.F0.equals(BigInteger.valueOf(3)), "F0 == 3");
        check(RSAKeyGenParameterSpec.F4.equals(BigInteger.valueOf(65537)), "F4 == 65537");

        RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4);
        check(spec.getKeysize() == 2048, "getKeysize");
        check(RSAKeyGenParameterSpec.F4.equals(spec.getPublicExponent()), "getPublicExponent");
        check(spec.getKeyParams() == null, "getKeyParams null");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAKeyGenParameterSpec spec2 = new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F0, params);
        check(spec2.getKeysize() == 3072, "3-arg getKeysize");
        check(RSAKeyGenParameterSpec.F0.equals(spec2.getPublicExponent()), "3-arg getPublicExponent");
        check(params == spec2.getKeyParams(), "3-arg getKeyParams");

        check(spec instanceof java.security.spec.AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
