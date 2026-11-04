/*
 * @test
 * @summary java.security.spec.NamedParameterSpec conformance: white-box structure + black-box behavior.
 * @run main NamedParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.NamedParameterSpec;

public class NamedParameterSpecTest {
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


    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = NamedParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getName"});
    }

    static void testBlackBox() {
        NamedParameterSpec spec = new NamedParameterSpec("X25519");
        check("X25519".equals(spec.getName()), "constructor getName");

        check("X25519".equals(NamedParameterSpec.X25519.getName()), "X25519");
        check("X448".equals(NamedParameterSpec.X448.getName()), "X448");
        check("Ed25519".equals(NamedParameterSpec.ED25519.getName()), "ED25519");
        check("Ed448".equals(NamedParameterSpec.ED448.getName()), "ED448");
        check("ML-DSA-44".equals(NamedParameterSpec.ML_DSA_44.getName()), "ML_DSA_44");
        check("ML-DSA-65".equals(NamedParameterSpec.ML_DSA_65.getName()), "ML_DSA_65");
        check("ML-DSA-87".equals(NamedParameterSpec.ML_DSA_87.getName()), "ML_DSA_87");
        check("ML-KEM-512".equals(NamedParameterSpec.ML_KEM_512.getName()), "ML_KEM_512");
        check("ML-KEM-768".equals(NamedParameterSpec.ML_KEM_768.getName()), "ML_KEM_768");
        check("ML-KEM-1024".equals(NamedParameterSpec.ML_KEM_1024.getName()), "ML_KEM_1024");

        check(spec instanceof java.security.spec.AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    static void testContracts() {
        // constructor matrix (probed: NPE 'stdName must not be null')
        checkThrows(NullPointerException.class, () -> new NamedParameterSpec(null), "null name -> NPE");
        check("".equals(new NamedParameterSpec("").getName()), "empty name accepted");
        check("Custom-Curve".equals(new NamedParameterSpec("Custom-Curve").getName()),
                "arbitrary name verbatim, case preserved");
        // no equals/hashCode override: identity semantics (probed)
        NamedParameterSpec a = new NamedParameterSpec("Foo");
        check(!a.equals(new NamedParameterSpec("Foo")), "equal-content not equal (probed)");
        check(a.equals(a), "reflexive");
        check(!a.equals(null), "equals null false");
        // constants are distinct instances with distinct names; 9 total (probed)
        NamedParameterSpec[] all = { NamedParameterSpec.X25519, NamedParameterSpec.X448,
                NamedParameterSpec.ED25519, NamedParameterSpec.ED448,
                NamedParameterSpec.ML_DSA_44, NamedParameterSpec.ML_DSA_65, NamedParameterSpec.ML_DSA_87,
                NamedParameterSpec.ML_KEM_512, NamedParameterSpec.ML_KEM_768, NamedParameterSpec.ML_KEM_1024 };
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (NamedParameterSpec n : all) { names.add(n.getName()); check(n.getName() != null, "name non-null"); }
        check(names.size() == all.length, "all " + all.length + " constant names distinct");
        check(NamedParameterSpec.ED25519 != NamedParameterSpec.ED448, "constants are distinct instances");
        check(NamedParameterSpec.ED25519 == NamedParameterSpec.ED25519, "constant is a stable singleton");
        // toString default form, not Serializable (probed)
        check(a.toString().startsWith("java.security.spec.NamedParameterSpec@"), "toString default form");
        check(!java.io.Serializable.class.isAssignableFrom(NamedParameterSpec.class), "not Serializable");
        // ECGenParameterSpec is a NamedParameterSpec (JDK-9+ shape)
        check(new java.security.spec.ECGenParameterSpec("secp256r1") instanceof NamedParameterSpec,
                "ECGenParameterSpec instanceof NamedParameterSpec");
    }
    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
