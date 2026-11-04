/*
 * @test
 * @summary java.security.spec.ECGenParameterSpec conformance: full contract coverage.
 *          getName round-trip for standard and arbitrary names, null -> NPE,
 *          empty string legal (probed), no equals/hashCode override (identity
 *          semantics, probed), instanceof AlgorithmParameterSpec, JDK-8 shape:
 *          implements AlgorithmParameterSpec directly with superclass Object.
 * @run main ECGenParameterSpecTest
 */

import java.io.Serializable;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.ECGenParameterSpec;

public class ECGenParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    public static void main(String[] args) throws Exception {
        java.security.spec.ECGenParameterSpec s = new java.security.spec.ECGenParameterSpec("secp256r1");
        check("secp256r1".equals(s.getName()), "getName");

        // ---- constructor matrix (probed) ----
        checkThrows(NullPointerException.class,
                () -> new ECGenParameterSpec(null), "null stdName -> NPE (probed)");
        check("".equals(new ECGenParameterSpec("").getName()),
                "empty name accepted, getName empty (probed)");
        // arbitrary names are not validated against a curve database
        check("BrainPool999".equals(new ECGenParameterSpec("BrainPool999").getName()),
                "arbitrary name accepted verbatim");
        // case is preserved, no normalization
        check("SECp256R1".equals(new ECGenParameterSpec("SECp256R1").getName()),
                "name case preserved");

        // ---- getName stable across calls ----
        ECGenParameterSpec n = new ECGenParameterSpec("secp384r1");
        check(n.getName().equals(n.getName()) && n.getName() == n.getName(),
                "getName returns same value (and same instance) across calls");

        // ---- standard NIST/SEC names round-trip ----
        String[] std = {"secp256r1", "secp384r1", "secp521r1", "secp256k1", "sect283r1"};
        for (String name : std) {
            check(name.equals(new ECGenParameterSpec(name).getName()),
                    "round-trip " + name);
        }

        // ---- no equals/hashCode override: identity semantics (probed) ----
        check(!s.equals(new ECGenParameterSpec("secp256r1")),
                "equal-content instances not equal (probed: no equals override)");
        check(s.equals(s), "reflexive");

        // ---- interop: usable where AlgorithmParameterSpec is expected ----
        java.util.List<AlgorithmParameterSpec> l = new java.util.ArrayList<AlgorithmParameterSpec>();
        l.add(s); l.add(new ECGenParameterSpec("secp521r1"));
        check(l.size() == 2 && l.contains(s), "usable in AlgorithmParameterSpec collections");

        // ---- JDK-8 shape: direct AlgorithmParameterSpec, superclass Object ----
        check(s instanceof AlgorithmParameterSpec, "implements AlgorithmParameterSpec");
        check(ECGenParameterSpec.class.getSuperclass() == Object.class,
                "JDK-8 superclass is Object (JDK-9+ it is NamedParameterSpec)");

        // ---- not Serializable ----
        check(!Serializable.class.isAssignableFrom(ECGenParameterSpec.class),
                "not Serializable");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
