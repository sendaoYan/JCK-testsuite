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

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }


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

    static void testContracts() {
        // single-arg constructor: prehash off, no context (probed)
        EdDSAParameterSpec off = new EdDSAParameterSpec(false);
        check(!off.isPrehash() && !off.getContext().isPresent(), "1-arg ctor: no prehash, empty context");
        EdDSAParameterSpec on = new EdDSAParameterSpec(true);
        check(on.isPrehash() && !on.getContext().isPresent(), "1-arg ctor: prehash, empty context");
        // null context -> NPE (probed 'context may not be null')
        checkThrows(NullPointerException.class,
                () -> new EdDSAParameterSpec(true, null), "null context -> NPE");
        // context length limit: 255 (probed -> InvalidParameterException)
        checkThrows(java.security.InvalidParameterException.class,
                () -> new EdDSAParameterSpec(true, new byte[256]),
                "256-byte context -> InvalidParameterException (probed)");
        EdDSAParameterSpec max = new EdDSAParameterSpec(true, new byte[255]);
        check(max.getContext().isPresent() && max.getContext().get().length == 255,
                "255-byte context accepted at boundary");
        // empty context array: present with length 0 (probed)
        EdDSAParameterSpec empty = new EdDSAParameterSpec(true, new byte[0]);
        check(empty.getContext().isPresent() && empty.getContext().get().length == 0,
                "empty context present, length 0 (probed)");
        // no equals/hashCode override: identity semantics (probed)
        EdDSAParameterSpec a = new EdDSAParameterSpec(true, new byte[]{1, 2});
        check(!a.equals(new EdDSAParameterSpec(true, new byte[]{1, 2})), "equal-content not equal (probed)");
        check(a.equals(a), "reflexive");
        check(a.toString().startsWith("java.security.spec.EdDSAParameterSpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(EdDSAParameterSpec.class), "not Serializable");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
