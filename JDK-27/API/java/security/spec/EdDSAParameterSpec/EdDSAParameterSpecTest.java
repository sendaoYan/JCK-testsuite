/*
 * @test
 * @summary java.security.spec.EdDSAParameterSpec conformance: white-box structure + black-box behavior.
 * @run main EdDSAParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.EdDSAParameterSpec;
import java.util.Arrays;

public class EdDSAParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = EdDSAParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"isPrehash", "getContext"});
    }

    static void testBlackBox() {
        // no context
        EdDSAParameterSpec spec = new EdDSAParameterSpec(false);
        check(!spec.isPrehash(), "isPrehash false");
        check(!spec.getContext().isPresent(), "no context (empty Optional)");

        // context provided
        byte[] ctx = { 1, 2, 3 };
        EdDSAParameterSpec spec2 = new EdDSAParameterSpec(true, ctx);
        check(spec2.isPrehash(), "isPrehash true");
        check(spec2.getContext().isPresent(), "context present");
        check(Arrays.equals(ctx, spec2.getContext().get()), "context equals input");

        // defensive copies
        ctx[0] = 99;
        check(spec2.getContext().get()[0] == 1, "context copy unaffected by input mutation");
        byte[] got = spec2.getContext().get();
        got[0] = 42;
        check(spec2.getContext().get()[0] == 1, "getContext returns fresh copy");

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
