/*
 * @test
 * @summary java.security.spec.AlgorithmParameterSpec conformance: white-box structure + black-box behavior.
 * @run main AlgorithmParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

public class AlgorithmParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.spec.AlgorithmParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 0, "no methods (marker interface)");
    }

    static void testBlackBox() {
        DSAParameterSpec d = new DSAParameterSpec(BigInteger.ONE, BigInteger.TWO, BigInteger.TEN);
        check(d instanceof AlgorithmParameterSpec, "DSAParameterSpec implements AlgorithmParameterSpec");
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
