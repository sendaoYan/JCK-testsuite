/*
 * @test
 * @summary java.security.spec.RSAPublicKeySpec conformance: white-box structure + black-box behavior.
 * @run main RSAPublicKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.spec.RSAPublicKeySpec;
import java.security.spec.NamedParameterSpec;

public class RSAPublicKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = RSAPublicKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getModulus","getPublicExponent","getParams"});
    }

    static void testBlackBox() {
        BigInteger mod = new BigInteger("33"), exp = new BigInteger("3");
        RSAPublicKeySpec spec = new RSAPublicKeySpec(mod, exp);
        check(mod.equals(spec.getModulus()), "getModulus");
        check(exp.equals(spec.getPublicExponent()), "getPublicExponent");
        check(spec.getParams() == null, "getParams null");

        NamedParameterSpec params = NamedParameterSpec.X25519;
        RSAPublicKeySpec spec2 = new RSAPublicKeySpec(mod, exp, params);
        check(mod.equals(spec2.getModulus()), "3-arg getModulus");
        check(exp.equals(spec2.getPublicExponent()), "3-arg getPublicExponent");
        check(params == spec2.getParams(), "3-arg getParams");

        check(spec instanceof java.security.spec.KeySpec, "instanceof KeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：构造变体/引用保持/值语义/KeyFactory 闭环 ----
        java.math.BigInteger pm = java.math.BigInteger.valueOf(35);
        java.math.BigInteger pe = java.math.BigInteger.valueOf(7);
        java.security.spec.RSAPublicKeySpec rp = new java.security.spec.RSAPublicKeySpec(pm, pe);
        check(rp.getModulus() == pm, "getModulus returns the same BigInteger reference");
        check(rp.getPublicExponent() == pe, "getPublicExponent returns the same BigInteger reference");
        check(rp.getParams() == null, "2-arg ctor leaves params null");
        java.security.spec.MGF1ParameterSpec rpParams = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(new java.security.spec.RSAPublicKeySpec(pm, pe, rpParams).getParams() == rpParams,
                "3-arg ctor params passthrough");
        check(new java.security.spec.RSAPublicKeySpec(null, null).getModulus() == null, "null-args ctor: modulus null");
        check(new java.security.spec.RSAPublicKeySpec(null, null).getPublicExponent() == null, "null-args ctor: exponent null");
        check(rp instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.RSAPublicKeySpec.class), "isAssignableFrom KeySpec");
        check(!java.security.spec.RSAPublicKeySpec.class.isAssignableFrom(java.security.spec.RSAPrivateKeySpec.class),
                "public and private specs are siblings");
        check(!rp.equals(new java.security.spec.RSAPublicKeySpec(pm, pe)), "no equals override: identity semantics");
        check(rp.hashCode() == rp.hashCode(), "hashCode stable");
        check(rp.toString().contains("RSAPublicKeySpec"), "default toString contains class name");
        check(!(rp instanceof java.io.Serializable), "not Serializable");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            java.security.KeyPair kp = kpg.generateKeyPair();
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.RSAPublicKeySpec fromKey = kf.getKeySpec(kp.getPublic(), java.security.spec.RSAPublicKeySpec.class);
            check(fromKey.getPublicExponent().intValue() == 65537, "spec exponent matches generated key (F4 default)");
            check(fromKey.getModulus().bitLength() == 512, "spec modulus bit length matches generated key");
            java.security.PublicKey restored = kf.generatePublic(fromKey);
            check(restored.equals(kp.getPublic()), "round-trip: spec -> generatePublic restores the same key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }
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
