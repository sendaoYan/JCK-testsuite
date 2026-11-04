/*
 * @test
 * @summary java.security.spec.EncodedKeySpec conformance: abstract class hierarchy and key methods.
 * @run main EncodedKeySpecTest
 */

public class EncodedKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.spec.EncodedKeySpec");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.spec.KeySpec"), "implements java.security.spec.KeySpec");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getFormat"), "declares getFormat");

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
        check(!java.lang.reflect.Modifier.isFinal(java.security.spec.EncodedKeySpec.class.getModifiers()),
                "class is extensible (non-final), proven by subclass");
        check(mine.hashCode() == mine.hashCode(), "subclass hashCode stable");
        check(mine.toString().contains("MyEncodedSpec"), "subclass default toString contains subclass name");
        check(!(mine instanceof java.io.Serializable), "EncodedKeySpec hierarchy not Serializable");
        System.out.println("PASS (" + passed + " assertions)");
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
}
