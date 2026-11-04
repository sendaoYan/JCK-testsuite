/*
 * @test
 * @summary java.security.MessageDigestSpi conformance: abstract class hierarchy and key methods.
 * @run main MessageDigestSpiTest
 */

public class MessageDigestSpiTest {
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

    static void ctMsg(Class<? extends Throwable> expected, String frag, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(frag)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but nothing was thrown");
    }

    // Recording subclass: overrides only the four abstract methods, so every other
    // method invoked below exercises the JDK-default implementation in MessageDigestSpi.
    static class RecSpi extends java.security.MessageDigestSpi {
        int arrCalls, byteCalls, resetCalls, digestCalls, lastOff = -1, lastLen = -1;
        protected void engineUpdate(byte input) { byteCalls++; }
        protected void engineUpdate(byte[] input, int offset, int len) { arrCalls++; lastOff = offset; lastLen = len; }
        protected byte[] engineDigest() { digestCalls++; return new byte[]{(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA}; }
        protected void engineReset() { resetCalls++; }
        int doGetDigestLength() { return engineGetDigestLength(); }
        int doDigest(byte[] buf, int off, int len) throws java.security.DigestException { return engineDigest(buf, off, len); }
        void doUpdate(java.nio.ByteBuffer b) { engineUpdate(b); }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.MessageDigestSpi.class;

        // ---- constructor & modifiers ----
        check(c.getConstructor() != null, "public no-arg constructor");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("engineUpdate", byte.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineUpdate(byte) abstract+protected");
        m = c.getDeclaredMethod("engineUpdate", byte[].class, int.class, int.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineUpdate(byte[],int,int) abstract");
        m = c.getDeclaredMethod("engineDigest");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineDigest() abstract");
        m = c.getDeclaredMethod("engineReset");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineReset abstract");
        m = c.getDeclaredMethod("engineUpdate", java.nio.ByteBuffer.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineUpdate(ByteBuffer) has default impl");
        m = c.getDeclaredMethod("engineGetDigestLength");
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetDigestLength has default impl");
        m = c.getDeclaredMethod("engineDigest", byte[].class, int.class, int.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineDigest(byte[],int,int) has default impl");
        m = c.getDeclaredMethod("clone");
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), "clone public");

        // ---- engineGetDigestLength default == 0 ----
        RecSpi d = new RecSpi();
        check(d.doGetDigestLength() == 0, "default engineGetDigestLength returns 0");

        // ---- engineDigest(buf,off,len) default derives from engineDigest() ----
        byte[] out = new byte[8];
        int n = d.doDigest(out, 2, 8);
        check(n == 4, "default engineDigest returns digest length");
        check(out[2] == (byte)0xAA && out[5] == (byte)0xAA, "default engineDigest copies digest at offset");
        check(out[6] == 0 && out[0] == 0, "default engineDigest writes only len digested bytes");
        check(d.digestCalls == 1, "default engineDigest delegates to engineDigest()");
        ctMsg(java.security.DigestException.class, "partial digests not returned",
                () -> d.doDigest(new byte[8], 0, 3), "engineDigest len < digestLength");
        ctMsg(java.security.DigestException.class, "insufficient space in the output buffer",
                () -> d.doDigest(new byte[3], 0, 8), "engineDigest buf too small");
        ctMsg(java.security.DigestException.class, "insufficient space in the output buffer",
                () -> d.doDigest(new byte[8], 9, 4), "engineDigest offset beyond buffer");
        ct(NullPointerException.class, () -> d.doDigest(null, 0, 4), "engineDigest null buffer -> NPE (buf.length dereferenced before checks)");

        // ---- engineUpdate(ByteBuffer) default: bulk path, consumes whole buffer ----
        RecSpi d2 = new RecSpi();
        java.nio.ByteBuffer heap = java.nio.ByteBuffer.wrap(new byte[]{1, 2, 3});
        d2.doUpdate(heap);
        check(d2.arrCalls == 1 && d2.byteCalls == 0, "engineUpdate(ByteBuffer) bulk-delegates to array variant");
        check(d2.lastOff == 0 && d2.lastLen == 3, "engineUpdate(ByteBuffer) passes correct off/len");
        check(heap.position() == 3, "engineUpdate(ByteBuffer) advances position to limit");
        java.nio.ByteBuffer direct = java.nio.ByteBuffer.allocateDirect(4);
        direct.put(new byte[]{9}).flip();
        RecSpi d3 = new RecSpi();
        d3.doUpdate(direct);
        check(d3.arrCalls == 1 && d3.byteCalls == 0, "engineUpdate(direct ByteBuffer) also bulk path");
        check(direct.position() == 1, "engineUpdate(direct ByteBuffer) consumed");
        RecSpi d4 = new RecSpi();
        d4.doUpdate(java.nio.ByteBuffer.wrap(new byte[0]));
        check(d4.arrCalls == 0 && d4.byteCalls == 0, "engineUpdate(empty ByteBuffer) is a no-op");

        // ---- clone default: not Cloneable -> CloneNotSupportedException ----
        ct(CloneNotSupportedException.class, () -> new RecSpi().doClone(), "clone default throws CNSE");

        // ---- facade equivalence: MessageDigest drives the SPI layer to a known vector ----
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] got = md.digest("abc".getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();
        for (byte b : got) hex.append(String.format("%02x", b));
        check("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".equals(hex.toString()),
                "facade SHA-256(\"abc\") known vector (FIPS 180-4)");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigestSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("engineDigest"), "declares engineDigest");
        check(ms.contains("engineGetDigestLength"), "declares engineGetDigestLength");
        check(ms.contains("engineReset"), "declares engineReset");
        check(ms.contains("engineUpdate"), "declares engineUpdate");

        // ---- contract deepening: default-method contracts via recording subclass ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
