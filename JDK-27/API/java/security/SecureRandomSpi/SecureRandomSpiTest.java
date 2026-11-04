/*
 * @test
 * @summary java.security.SecureRandomSpi conformance: white-box structure + black-box behavior.
 * @run main SecureRandomSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.SecureRandomParameters;
import java.security.SecureRandomSpi;
import java.util.Arrays;

public class SecureRandomSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SecureRandomSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkInterfaces(c, new String[]{"java.io.Serializable"});
        checkMethods(c, new String[]{"toString"});
    }

    static void testBlackBox() throws Exception {
        MyRandomSpi s = new MyRandomSpi();
        byte[] b = new byte[5];
        s.doSetSeed(new byte[]{1, 2});
        s.doNextBytes(b);
        check(Arrays.equals(b, new byte[]{7, 7, 7, 7, 7}), "engineNextBytes fills buffer");
        byte[] seed = s.doGenerateSeed(4);
        check(seed.length == 4 && Arrays.equals(seed, new byte[]{3, 3, 3, 3}), "engineGenerateSeed length/content");
        check(s.doGetParameters() == null, "default engineGetParameters == null");
        check(s.toString().equals("MyRandomSpi"), "toString == simple name");
        checkThrows(UnsupportedOperationException.class, () -> s.doNextBytes(b, null));
        checkThrows(UnsupportedOperationException.class, () -> s.doReseed(null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }


    // throwing-check helper that accepts checked exceptions and a message
    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    // ---- contract deepening: JDK-9+ parameterized SPI surface ----
    static class DeepSpi extends SecureRandomSpi implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        int seedCalls, nextCalls, genCalls, paramsNextCalls, reseedCalls, getParamsCalls;
        SecureRandomParameters lastParams;
        protected void engineSetSeed(byte[] seed) { seedCalls++; }
        protected void engineNextBytes(byte[] bytes) { nextCalls++; Arrays.fill(bytes, (byte)0x5A); }
        protected byte[] engineGenerateSeed(int numBytes) { genCalls++; return new byte[numBytes]; }
        DeepSpi(SecureRandomParameters p) { super(p); }
        void doNext(byte[] b, SecureRandomParameters p) { engineNextBytes(b, p); }
        SecureRandomParameters doGetParams2() { return engineGetParameters(); }
        void doReseed(SecureRandomParameters p) { engineReseed(p); }
        SecureRandomParameters doGetParameters() { return engineGetParameters(); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = SecureRandomSpi.class;
        // Structure: 3 abstract + parameterized defaults added by JDK 9
        java.lang.reflect.Constructor<?> pc = c.getDeclaredConstructor(SecureRandomParameters.class);
        check(java.lang.reflect.Modifier.isProtected(pc.getModifiers()), "protected SecureRandomParameters constructor (JDK 9+)");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("engineNextBytes", byte[].class, SecureRandomParameters.class);
        check(!Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers()), "engineNextBytes(byte[],params) default impl");
        m = c.getDeclaredMethod("engineReseed", SecureRandomParameters.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineReseed(params) default impl");
        m = c.getDeclaredMethod("engineGetParameters");
        check(!Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers()), "engineGetParameters default impl");
        m = c.getDeclaredMethod("toString");
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), "JDK 9+ declares public toString");

        // Behavior of the JDK defaults: every parameterized path is unsupported unless overridden
        DeepSpi spi = new DeepSpi(new SecureRandomParameters() { });
        ct(UnsupportedOperationException.class, () -> spi.doNext(new byte[2], null),
                "default engineNextBytes(b,null) throws UOE (no plain fallback)");
        ct(UnsupportedOperationException.class, () -> spi.doNext(new byte[2], new SecureRandomParameters() { }),
                "default engineNextBytes(b,params) throws UOE");
        ct(UnsupportedOperationException.class, () -> spi.doReseed(null),
                "default engineReseed(null) throws UOE");
        ct(UnsupportedOperationException.class, () -> spi.doReseed(new SecureRandomParameters() { }),
                "default engineReseed(params) throws UOE");
        check(spi.doGetParams2() == null, "default engineGetParameters returns null");
        check(spi.toString().equals("DeepSpi"), "toString == simple class name");
        // plain engine methods still work alongside the parameterized ones
        byte[] b = new byte[3];
        spi.engineNextBytes(b);
        check(spi.nextCalls == 1 && b[0] == (byte)0x5A, "plain engineNextBytes unaffected");

        // Serialization round-trip preserves the SPI subtype
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(spi); }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof DeepSpi, "serialization round-trip preserves SPI subtype");
        byte[] b2 = new byte[2];
        ((DeepSpi) back).engineNextBytes(b2);
        check(b2[0] == (byte)0x5A, "deserialized SPI still functional");

        // Facade determinism loop: SecureRandom drives the SUN SHA1PRNG SPI
        java.security.SecureRandom r1 = java.security.SecureRandom.getInstance("SHA1PRNG");
        r1.setSeed(42L);
        java.security.SecureRandom r2 = java.security.SecureRandom.getInstance("SHA1PRNG");
        r2.setSeed(42L);
        byte[] x1 = new byte[16]; r1.nextBytes(x1);
        byte[] x2 = new byte[16]; r2.nextBytes(x2);
        check(Arrays.equals(x1, x2), "facade SHA1PRNG setSeed-before-use is reproducible (SUN SPI contract)");
        check(java.security.SecureRandom.getInstance("SHA1PRNG").generateSeed(8).length == 8,
                "facade generateSeed(8) length 8");
    }


    static class MyRandomSpi extends SecureRandomSpi {
        protected void engineSetSeed(byte[] seed) { }
        protected void engineNextBytes(byte[] bytes) { Arrays.fill(bytes, (byte) 7); }
        protected byte[] engineGenerateSeed(int numBytes) { byte[] r = new byte[numBytes]; Arrays.fill(r, (byte) 3); return r; }
        void doSetSeed(byte[] s) { engineSetSeed(s); }
        void doNextBytes(byte[] b) { engineNextBytes(b); }
        byte[] doGenerateSeed(int n) { return engineGenerateSeed(n); }
        void doNextBytes(byte[] b, SecureRandomParameters p) { engineNextBytes(b, p); }
        void doReseed(SecureRandomParameters p) { engineReseed(p); }
        SecureRandomParameters doGetParameters() { return engineGetParameters(); }
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
