/*
 * @test
 * @summary java.security.SecureRandomSpi conformance: abstract class hierarchy and key methods.
 * @run main SecureRandomSpiTest
 */

public class SecureRandomSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    static class RecSpi extends java.security.SecureRandomSpi implements java.io.Serializable {
        private static final long serialVersionUID = 1L;
        int seedCalls, nextCalls, genCalls; byte[] lastSeed; int lastGenLen;
        byte[] fixedOutput = new byte[]{(byte)0x5A};
        protected void engineSetSeed(byte[] seed) { seedCalls++; lastSeed = seed.clone(); }
        protected void engineNextBytes(byte[] bytes) { nextCalls++; java.util.Arrays.fill(bytes, (byte)0x5A); }
        protected byte[] engineGenerateSeed(int numBytes) { genCalls++; lastGenLen = numBytes; return new byte[numBytes]; }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.SecureRandomSpi.class;

        // ---- structure: Serializable, exactly 3 abstract protected engine methods on JDK 8 ----
        check(java.io.Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 3, "JDK 8: exactly 3 abstract engine methods");
        check(names.equals(new java.util.HashSet<String>(java.util.Arrays.asList(
                "engineSetSeed", "engineNextBytes", "engineGenerateSeed"))),
                "JDK 8 engine method names exact: " + names);
        java.lang.reflect.Method m = c.getDeclaredMethod("engineNextBytes", byte[].class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineNextBytes protected");
        m = c.getDeclaredMethod("engineSetSeed", byte[].class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineSetSeed protected");
        m = c.getDeclaredMethod("engineGenerateSeed", int.class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineGenerateSeed protected");
        boolean declaresToString = false;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods()) if (mm.getName().equals("toString")) declaresToString = true;
        check(!declaresToString, "JDK 8 does NOT declare toString (Object default; JDK 9+ adds one)");

        // ---- pass-through ----
        RecSpi spi = new RecSpi();
        spi.engineSetSeed(new byte[]{9, 9});
        check(spi.seedCalls == 1 && spi.lastSeed.length == 2 && spi.lastSeed[0] == 9, "engineSetSeed pass-through");
        byte[] buf = new byte[4];
        spi.engineNextBytes(buf);
        check(spi.nextCalls == 1 && buf[0] == (byte)0x5A && buf[3] == (byte)0x5A, "engineNextBytes fills via subclass");
        byte[] seed = spi.engineGenerateSeed(5);
        check(spi.genCalls == 1 && seed.length == 5 && spi.lastGenLen == 5, "engineGenerateSeed pass-through");

        // ---- serialization: SecureRandomSpi is Serializable, subclass round-trips ----
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(spi); }
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof RecSpi && back instanceof java.security.SecureRandomSpi,
                "serialization round-trip preserves SPI subtype");
        byte[] buf2 = new byte[2];
        ((RecSpi) back).engineNextBytes(buf2);
        check(buf2[0] == (byte)0x5A, "deserialized SPI still functional");

        // ---- facade determinism loop: SecureRandom drives the SUN SHA1PRNG SPI ----
        java.security.SecureRandom r1 = java.security.SecureRandom.getInstance("SHA1PRNG");
        r1.setSeed(42L);
        java.security.SecureRandom r2 = java.security.SecureRandom.getInstance("SHA1PRNG");
        r2.setSeed(42L);
        byte[] b1 = new byte[16]; r1.nextBytes(b1);
        byte[] b2 = new byte[16]; r2.nextBytes(b2);
        check(java.util.Arrays.equals(b1, b2), "facade SHA1PRNG setSeed-before-use is reproducible (SUN SPI contract)");
        check(java.security.SecureRandom.getInstance("SHA1PRNG").generateSeed(8).length == 8,
                "facade generateSeed(8) length 8");
        java.security.SecureRandom def = new java.security.SecureRandom();
        check(def.getProvider() != null && def.getAlgorithm() != null,
                "default facade has provider and algorithm (SPI-backed)");
        byte[] nb = new byte[8]; def.nextBytes(nb);
        int nonzero = 0; for (byte x : nb) if (x != 0) nonzero++;
        check(nonzero >= 0, "facade nextBytes returned (statistical content not asserted)");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.SecureRandomSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGenerateSeed"), "declares engineGenerateSeed");
        check(ms.contains("engineNextBytes"), "declares engineNextBytes");
        check(ms.contains("engineSetSeed"), "declares engineSetSeed");

        // ---- contract deepening: pass-through + serialization + facade determinism ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
