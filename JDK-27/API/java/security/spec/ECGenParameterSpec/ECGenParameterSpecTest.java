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

    static void testContracts() {
        // constructor matrix (probed: NPE with 'stdName must not be null' on JDK-27)
        checkThrows(NullPointerException.class,
                () -> new ECGenParameterSpec(null), "null stdName -> NPE");
        check("".equals(new ECGenParameterSpec("").getName()), "empty name legal (probed)");
        check("BrainPool999".equals(new ECGenParameterSpec("BrainPool999").getName()),
                "arbitrary name accepted verbatim");
        check("SECp256R1".equals(new ECGenParameterSpec("SECp256R1").getName()), "case preserved");
        String[] std = {"secp256r1", "secp384r1", "secp521r1", "secp256k1", "sect283r1"};
        for (String name : std) check(name.equals(new ECGenParameterSpec(name).getName()), "round-trip " + name);
        // no equals/hashCode override: identity semantics (probed)
        ECGenParameterSpec s = new ECGenParameterSpec("secp256r1");
        check(!s.equals(new ECGenParameterSpec("secp256r1")), "equal-content not equal (probed)");
        check(s.equals(s), "reflexive");
        // JDK-9+ shape: extends NamedParameterSpec
        check(ECGenParameterSpec.class.getSuperclass() == NamedParameterSpec.class,
                "superclass is NamedParameterSpec (JDK-9+)");
        check(new ECGenParameterSpec("X25519").getName().equals("X25519"),
                "constructor name verbatim");
        check(!java.io.Serializable.class.isAssignableFrom(ECGenParameterSpec.class), "not Serializable");
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
