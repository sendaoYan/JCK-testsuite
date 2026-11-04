/*
 * @test
 * @summary java.security.spec.EdECPrivateKeySpec conformance: white-box structure + black-box behavior.
 * @run main EdECPrivateKeySpecTest
 */

import java.lang.reflect.*;
import java.security.spec.EdECPrivateKeySpec;
import java.security.spec.KeySpec;
import java.security.spec.NamedParameterSpec;
import java.util.Arrays;

public class EdECPrivateKeySpecTest {
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
        Class<?> c = EdECPrivateKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getParams", "getBytes"});
    }

    static void testBlackBox() {
        byte[] bytes = new byte[32];
        for (int i = 0; i < bytes.length; i++) bytes[i] = (byte) i;
        EdECPrivateKeySpec spec = new EdECPrivateKeySpec(NamedParameterSpec.ED25519, bytes);
        check(NamedParameterSpec.ED25519.equals(spec.getParams()), "getParams");
        check(Arrays.equals(bytes, spec.getBytes()), "getBytes");

        // defensive copy: mutate input after construction
        bytes[0] = 99;
        check(spec.getBytes()[0] == 0, "stored copy unaffected by input mutation");

        // defensive copy: mutate returned array
        byte[] got = spec.getBytes();
        got[0] = 42;
        check(spec.getBytes()[0] == 0, "getBytes returns fresh copy");

        check(spec instanceof KeySpec, "instanceof KeySpec");
    }

    static void testContracts() {
        // constructor null matrix (probed: 'params must not be null' / 'bytes must not be null')
        checkThrows(NullPointerException.class,
                () -> new EdECPrivateKeySpec(null, new byte[1]), "null params -> NPE");
        checkThrows(NullPointerException.class,
                () -> new EdECPrivateKeySpec(NamedParameterSpec.ED448, null), "null bytes -> NPE");
        // getParams returns the exact instance passed (identity, probed via existing same-ref checks)
        EdECPrivateKeySpec s = new EdECPrivateKeySpec(NamedParameterSpec.ED448, new byte[]{9});
        check(s.getParams() == NamedParameterSpec.ED448, "getParams identity round-trip");
        check(s.getBytes().length == 1 && s.getBytes()[0] == 9, "bytes round-trip");
        // empty byte array is legal
        check(new EdECPrivateKeySpec(NamedParameterSpec.ED25519, new byte[0]).getBytes().length == 0,
                "empty bytes legal");
        // no equals override: identity semantics (probed)
        check(!s.equals(new EdECPrivateKeySpec(NamedParameterSpec.ED448, new byte[]{9})),
                "equal-content not equal (probed)");
        check(s.equals(s), "reflexive");
        check(s.toString().startsWith("java.security.spec.EdECPrivateKeySpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(EdECPrivateKeySpec.class), "not Serializable");
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
