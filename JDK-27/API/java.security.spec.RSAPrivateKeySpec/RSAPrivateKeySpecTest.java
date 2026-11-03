/*
 * @test
 * @summary java.security.spec.RSAPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAPrivateKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.NamedParameterSpec;

public class RSAPrivateKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getModulus","getPrivateExponent","getParams"});
    }

    static void testBlackBox() {
        BigInteger mod = new BigInteger("33"), exp = new BigInteger("7");
        RSAPrivateKeySpec spec = new RSAPrivateKeySpec(mod, exp);
        check(mod.equals(spec.getModulus()), "getModulus");
        check(exp.equals(spec.getPrivateExponent()), "getPrivateExponent");
        check(spec.getParams() == null, "getParams null");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAPrivateKeySpec spec2 = new RSAPrivateKeySpec(mod, exp, params);
        check(mod.equals(spec2.getModulus()), "3-arg getModulus");
        check(exp.equals(spec2.getPrivateExponent()), "3-arg getPrivateExponent");
        check(params == spec2.getParams(), "3-arg getParams");

        check(spec instanceof java.security.spec.KeySpec, "instanceof KeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
