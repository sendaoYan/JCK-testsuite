/*
 * @test
 * @summary java.security.spec.XECPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main XECPublicKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.XECPublicKeySpec;
import java.security.spec.NamedParameterSpec;

public class XECPublicKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = XECPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams","getU"});
    }

    static void testBlackBox() {
        XECPublicKeySpec spec = new XECPublicKeySpec(NamedParameterSpec.X25519, BigInteger.TEN);
        check(NamedParameterSpec.X25519.equals(spec.getParams()), "getParams");
        check(BigInteger.TEN.equals(spec.getU()), "getU");
        check(spec instanceof java.security.spec.KeySpec, "instanceof KeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
