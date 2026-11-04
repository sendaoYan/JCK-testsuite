/*
 * @test
 * @summary java.security.spec.DSAPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main DSAPublicKeySpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.DSAPublicKeySpec;
import java.security.spec.KeySpec;

public class DSAPublicKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = DSAPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getY", "getP", "getQ", "getG"});
    }

    static void testBlackBox() {
        DSAPublicKeySpec spec = new DSAPublicKeySpec(new BigInteger("1"), new BigInteger("2"), new BigInteger("3"), new BigInteger("4"));
        check(new BigInteger("1").equals(spec.getY()), "getY");
        check(new BigInteger("2").equals(spec.getP()), "getP");
        check(new BigInteger("3").equals(spec.getQ()), "getQ");
        check(new BigInteger("4").equals(spec.getG()), "getG");
        check(spec instanceof KeySpec, "instanceof KeySpec");
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
