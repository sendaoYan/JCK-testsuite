/*
 * @test
 * @summary java.security.spec.DSAParameterSpec conformance: white-box structure + black-box behavior.
 * @run main DSAParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.interfaces.DSAParams;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.DSAParameterSpec;

public class DSAParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = DSAParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec", "java.security.interfaces.DSAParams"});
        checkMethods(c, new String[]{"getP", "getQ", "getG"});
    }

    static void testBlackBox() {
        BigInteger p = new BigInteger("23"), q = new BigInteger("11"), g = new BigInteger("5");
        DSAParameterSpec spec = new DSAParameterSpec(p, q, g);
        check(p.equals(spec.getP()), "getP");
        check(q.equals(spec.getQ()), "getQ");
        check(g.equals(spec.getG()), "getG");
        check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
        check(spec instanceof DSAParams, "instanceof DSAParams");
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
