/*
 * @test
 * @summary java.security.spec.ECParameterSpec conformance: white-box structure + black-box behavior.
 * @run main ECParameterSpecTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.EllipticCurve;

public class ECParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = ECParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getCurve", "getGenerator", "getOrder", "getCofactor"});
    }

    static void testBlackBox() {
        ECFieldFp field = new ECFieldFp(new BigInteger("23"));
        EllipticCurve curve = new EllipticCurve(field, BigInteger.ONE, BigInteger.ONE);
        ECPoint g = new ECPoint(BigInteger.ONE, BigInteger.ONE);
        ECParameterSpec spec = new ECParameterSpec(curve, g, new BigInteger("7"), 1);
        check(curve.equals(spec.getCurve()), "getCurve");
        check(g.equals(spec.getGenerator()), "getGenerator");
        check(new BigInteger("7").equals(spec.getOrder()), "getOrder");
        check(spec.getCofactor() == 1, "getCofactor");
        check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
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
