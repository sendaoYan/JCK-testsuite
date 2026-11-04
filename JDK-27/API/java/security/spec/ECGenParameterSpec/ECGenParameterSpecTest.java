/*
 * @test
 * @summary java.security.spec.ECGenParameterSpec conformance: white-box structure + black-box behavior.
 * @run main ECGenParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.NamedParameterSpec;

public class ECGenParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = ECGenParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == NamedParameterSpec.class, "extends NamedParameterSpec");
        try {
            c.getMethod("getName");
            check(true, "method getName");
        } catch (NoSuchMethodException e) {
            check(false, "method getName");
        }
    }

    static void testBlackBox() {
        ECGenParameterSpec spec = new ECGenParameterSpec("secp256r1");
        check("secp256r1".equals(spec.getName()), "getName");
        check(spec instanceof NamedParameterSpec, "instanceof NamedParameterSpec");
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
