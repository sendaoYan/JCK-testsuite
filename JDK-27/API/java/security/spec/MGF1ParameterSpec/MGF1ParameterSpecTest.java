/*
 * @test
 * @summary java.security.spec.MGF1ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main MGF1ParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.MGF1ParameterSpec;

public class MGF1ParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = MGF1ParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getDigestAlgorithm","toString"});
    }

    static void testBlackBox() {
        MGF1ParameterSpec spec = new MGF1ParameterSpec("SHA-256");
        check("SHA-256".equals(spec.getDigestAlgorithm()), "constructor getDigestAlgorithm");
        check(spec.toString().contains("SHA-256"), "toString contains digest");
        check(spec.toString().contains("MGF1ParameterSpec"), "toString contains class name");

        check("SHA-1".equals(MGF1ParameterSpec.SHA1.getDigestAlgorithm()), "SHA1");
        check("SHA-224".equals(MGF1ParameterSpec.SHA224.getDigestAlgorithm()), "SHA224");
        check("SHA-256".equals(MGF1ParameterSpec.SHA256.getDigestAlgorithm()), "SHA256");
        check("SHA-384".equals(MGF1ParameterSpec.SHA384.getDigestAlgorithm()), "SHA384");
        check("SHA-512".equals(MGF1ParameterSpec.SHA512.getDigestAlgorithm()), "SHA512");
        check("SHA-512/224".equals(MGF1ParameterSpec.SHA512_224.getDigestAlgorithm()), "SHA512_224");
        check("SHA-512/256".equals(MGF1ParameterSpec.SHA512_256.getDigestAlgorithm()), "SHA512_256");
        check("SHA3-224".equals(MGF1ParameterSpec.SHA3_224.getDigestAlgorithm()), "SHA3_224");
        check("SHA3-256".equals(MGF1ParameterSpec.SHA3_256.getDigestAlgorithm()), "SHA3_256");
        check("SHA3-384".equals(MGF1ParameterSpec.SHA3_384.getDigestAlgorithm()), "SHA3_384");
        check("SHA3-512".equals(MGF1ParameterSpec.SHA3_512.getDigestAlgorithm()), "SHA3_512");

        check(spec instanceof java.security.spec.AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：null 语义/值语义边界 ----
        checkThrowsMsg(java.lang.NullPointerException.class, () -> new java.security.spec.MGF1ParameterSpec(null),
                "digest algorithm", "null digest -> NPE");
        check("".equals(new java.security.spec.MGF1ParameterSpec("").getDigestAlgorithm()),
                "empty digest name accepted at spec level (no validation)");
        check(new java.security.spec.MGF1ParameterSpec("SHA-256").getDigestAlgorithm().equals("SHA-256"), "ctor passthrough");
        check(!new java.security.spec.MGF1ParameterSpec("SHA-256").equals(new java.security.spec.MGF1ParameterSpec("SHA-256")),
                "no equals override: identity semantics");
        java.security.spec.MGF1ParameterSpec mgfOne = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(mgfOne.hashCode() == mgfOne.hashCode(), "hashCode stable");
        check(!(mgfOne instanceof java.io.Serializable), "not Serializable");
        check(java.security.spec.AlgorithmParameterSpec.class.isAssignableFrom(java.security.spec.MGF1ParameterSpec.class),
                "isAssignableFrom AlgorithmParameterSpec");
        check(new java.security.spec.MGF1ParameterSpec[0].getClass().getComponentType() == java.security.spec.MGF1ParameterSpec.class,
                "array component type");
    }

    // ---- helpers for contract deepening (appended) ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String sub, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(sub),
                        msg + ": message contains \"" + sub + "\" (actual: " + t.getMessage() + ")");
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static byte[] serializeToBytes(Object o) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(o);
            return bos.toByteArray();
        } catch (java.io.IOException e) {
            throw new AssertionError("serialize failed: " + e, e);
        }
    }

    static Object deserializeFromBytes(byte[] b) {
        try {
            return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            throw new AssertionError("deserialize failed: " + e, e);
        }
    }
}
