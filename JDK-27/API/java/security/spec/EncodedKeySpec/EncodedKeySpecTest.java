/*
 * @test
 * @summary java.security.spec.EncodedKeySpec conformance: white-box structure + black-box behavior.
 * @run main EncodedKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class EncodedKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name){for(Class<?> i:c.getInterfaces())if(i.getName().equals(name))return true;return false;}
    static void checkInterfaces(Class<?> c, String[] names){for(String n:names)check(hasInterface(c,n),"implements "+n);}
    static void checkMethods(Class<?> c, String[] names){for(String n:names)check(hasMethod(c,n),"method "+n);}
    static boolean hasMethod(Class<?> c, String name){for(Method m:c.getDeclaredMethods())if(m.getName().equals(name))return true;return false;}

    static void testWhiteBox() {
        Class<?> c = EncodedKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getAlgorithm","getEncoded","getFormat"});
        boolean abs = false;
        try { Method m = c.getDeclaredMethod("getFormat"); abs = Modifier.isAbstract(m.getModifiers()); } catch (NoSuchMethodException e) { }
        check(abs, "getFormat abstract");
    }

    static void testBlackBox() {
        byte[] key = new byte[]{1,2,3,4};
        X509EncodedKeySpec x = new X509EncodedKeySpec(key, "RSA");
        check(x.getAlgorithm() != null && !x.getAlgorithm().isEmpty(), "getAlgorithm non-null");
        check("RSA".equals(x.getAlgorithm()), "getAlgorithm passed");
        byte[] e = x.getEncoded();
        check(e != key, "getEncoded defensive copy");
        check(Arrays.equals(key, e), "getEncoded content");
        e[0] = 99;
        check(x.getEncoded()[0] == 1, "getEncoded fresh copy each call");

        PKCS8EncodedKeySpec p = new PKCS8EncodedKeySpec(key);
        check(p.getAlgorithm() == null, "getAlgorithm null single-arg");
        check(EncodedKeySpec.class.isInstance(x), "subclass instanceof EncodedKeySpec");
        check(EncodedKeySpec.class.isInstance(p), "PKCS8 subclass instanceof EncodedKeySpec");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- 契约深化（appended, 第 2 批） ----
    static void testContract() {
        // ---- 契约深化：防御拷贝/抽象骨架/扩展性/值语义 ----
        checkThrows(NullPointerException.class, () -> new java.security.spec.PKCS8EncodedKeySpec(null),
                "concrete subclass ctor with null bytes -> NPE");
        byte[] eksRaw = {9, 8, 7};
        MyEncodedSpec mine = new MyEncodedSpec(eksRaw);
        eksRaw[0] = 1;
        check(mine.getEncoded()[0] == 9, "constructor clones bytes (defensive copy)");
        byte[] eksG1 = mine.getEncoded();
        byte[] eksG2 = mine.getEncoded();
        check(eksG1 != eksG2, "getEncoded returns a fresh array per call");
        check(java.util.Arrays.equals(eksG1, eksG2), "repeated getEncoded calls yield equal content");
        eksG1[0] = 0;
        check(mine.getEncoded()[0] == 9, "mutating a returned copy does not affect the spec");
        check("MY".equals(mine.getFormat()), "subclass supplies getFormat");
        check(mine instanceof java.security.spec.EncodedKeySpec, "subclass instanceof EncodedKeySpec");
        check(mine instanceof java.security.spec.KeySpec, "subclass instanceof KeySpec (inherited contract)");
        check(new MyEncodedSpec(new byte[0]).getEncoded().length == 0, "empty byte[] legal for subclass");
        java.security.spec.PKCS8EncodedKeySpec eksA = new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1});
        java.security.spec.PKCS8EncodedKeySpec eksB = new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1});
        check(!eksA.equals(eksB), "no equals override: identity semantics even for identical content");
        check(eksA.equals(eksA), "self-equality");
        check(eksA.hashCode() == eksA.hashCode(), "hashCode stable within instance");
        check(eksA.toString().contains("PKCS8EncodedKeySpec"), "default toString contains class name");
        try {
            java.lang.reflect.Method fmt = java.security.spec.EncodedKeySpec.class.getDeclaredMethod("getFormat");
            check(java.lang.reflect.Modifier.isAbstract(fmt.getModifiers()), "getFormat is abstract (subclass obligation)");
            java.lang.reflect.Method enc = java.security.spec.EncodedKeySpec.class.getDeclaredMethod("getEncoded");
            check(!java.lang.reflect.Modifier.isAbstract(enc.getModifiers()), "getEncoded is concrete in EncodedKeySpec");
        } catch (NoSuchMethodException e) {
            throw new AssertionError("expected methods missing", e);
        }
        check(java.security.spec.EncodedKeySpec.class.isAssignableFrom(MyEncodedSpec.class), "isAssignableFrom custom subclass");
        // ---- 契约深化（JDK-9+）：算法名通道 ----
        MyAlgEncodedSpec algSpec = new MyAlgEncodedSpec(new byte[]{5, 6}, "XYZ");
        check("XYZ".equals(algSpec.getAlgorithm()), "(byte[],String) super ctor exposes algorithm name");
        check(java.util.Arrays.equals(new byte[]{5, 6}, algSpec.getEncoded()), "algorithm ctor keeps bytes");
        check(new MyEncodedSpec(new byte[]{1}).getAlgorithm() == null, "legacy ctor leaves algorithm null");
        checkThrows(NullPointerException.class, () -> new MyAlgEncodedSpec(new byte[]{1}, null),
                "null algorithm name -> NPE");
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

    static class MyEncodedSpec extends java.security.spec.EncodedKeySpec {
        MyEncodedSpec(byte[] encodedKey) { super(encodedKey); }
        public String getFormat() { return "MY"; }
    }

    static class MyAlgEncodedSpec extends java.security.spec.EncodedKeySpec {
        MyAlgEncodedSpec(byte[] encodedKey, String algorithm) { super(encodedKey, algorithm); }
        public String getFormat() { return "MYALG"; }
    }
}
