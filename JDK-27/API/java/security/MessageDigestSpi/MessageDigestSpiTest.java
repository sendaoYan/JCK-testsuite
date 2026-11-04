/*
 * @test
 * @summary java.security.MessageDigestSpi conformance: white-box structure + black-box behavior.
 * @run main MessageDigestSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.security.DigestException;
import java.security.MessageDigestSpi;

public class MessageDigestSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigestSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"clone", "engineGetDigestLength", "engineUpdate", "engineDigest", "engineReset"});
    }

    static void testBlackBox() throws Exception {
        MyDigestSpi d = new MyDigestSpi();
        check(d.doGetDigestLength() == 0, "default engineGetDigestLength == 0");
        Object c = d.clone();
        check(c != null && c != d && (c instanceof MessageDigestSpi), "clone distinct instance");
        byte[] buf = new byte[8];
        int n = d.doDigest(buf, 2, 8);
        check(n == 4, "inherited engineDigest returns length");
        check(buf[2] == 9 && buf[3] == 9 && buf[4] == 9 && buf[5] == 9, "digest bytes written");
        ByteBuffer bb = ByteBuffer.wrap(new byte[]{1, 2, 3});
        d.doUpdate(bb);
        check(bb.position() == bb.limit(), "engineUpdate(ByteBuffer) advances position");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening: default-method contracts (recording subclass leaves every
    // non-abstract engine method at its JDK default; assertions below pin those defaults) ----
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

    static class DeepSpi extends MessageDigestSpi {
        int arrCalls, byteCalls, resetCalls, digestCalls, lastOff = -1, lastLen = -1;
        protected void engineUpdate(byte input) { byteCalls++; }
        protected void engineUpdate(byte[] input, int offset, int len) { arrCalls++; lastOff = offset; lastLen = len; }
        protected byte[] engineDigest() { digestCalls++; return new byte[]{(byte)0xAA,(byte)0xAA,(byte)0xAA,(byte)0xAA}; }
        protected void engineReset() { resetCalls++; }
        int doGetDigestLength() { return engineGetDigestLength(); }
        int doDigest(byte[] buf, int off, int len) throws DigestException { return engineDigest(buf, off, len); }
        void doUpdate(ByteBuffer b) { engineUpdate(b); }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = MessageDigestSpi.class;

        // ---- constructor & modifiers ----
        check(c.getConstructor() != null, "public no-arg constructor");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("engineUpdate", byte.class);
        check(Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers()), "engineUpdate(byte) abstract+protected");
        m = c.getDeclaredMethod("engineUpdate", byte[].class, int.class, int.class);
        check(Modifier.isAbstract(m.getModifiers()), "engineUpdate(byte[],int,int) abstract");
        m = c.getDeclaredMethod("engineDigest");
        check(Modifier.isAbstract(m.getModifiers()), "engineDigest() abstract");
        m = c.getDeclaredMethod("engineReset");
        check(Modifier.isAbstract(m.getModifiers()), "engineReset abstract");
        m = c.getDeclaredMethod("engineUpdate", ByteBuffer.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineUpdate(ByteBuffer) has default impl");
        m = c.getDeclaredMethod("engineGetDigestLength");
        check(!Modifier.isAbstract(m.getModifiers()), "engineGetDigestLength has default impl");
        m = c.getDeclaredMethod("engineDigest", byte[].class, int.class, int.class);
        check(!Modifier.isAbstract(m.getModifiers()), "engineDigest(byte[],int,int) has default impl");
        m = c.getDeclaredMethod("clone");
        check(Modifier.isPublic(m.getModifiers()), "clone public");

        // ---- engineGetDigestLength default == 0 ----
        DeepSpi d = new DeepSpi();
        check(d.doGetDigestLength() == 0, "default engineGetDigestLength returns 0");

        // ---- engineDigest(buf,off,len) default derives from engineDigest() ----
        byte[] out = new byte[8];
        int n = d.doDigest(out, 2, 8);
        check(n == 4, "default engineDigest returns digest length");
        check(out[2] == (byte)0xAA && out[5] == (byte)0xAA, "default engineDigest copies digest at offset");
        check(out[6] == 0 && out[0] == 0, "default engineDigest writes only len digested bytes");
        check(d.digestCalls == 1, "default engineDigest delegates to engineDigest()");
        ctMsg(DigestException.class, "partial digests not returned",
                () -> d.doDigest(new byte[8], 0, 3), "engineDigest len < digestLength");
        ctMsg(DigestException.class, "insufficient space in the output buffer",
                () -> d.doDigest(new byte[3], 0, 8), "engineDigest buf too small");
        ctMsg(DigestException.class, "insufficient space in the output buffer",
                () -> d.doDigest(new byte[8], 9, 4), "engineDigest offset beyond buffer");
        ct(NullPointerException.class, () -> d.doDigest(null, 0, 4), "engineDigest null buffer -> NPE (buf.length dereferenced before checks)");

        // ---- engineUpdate(ByteBuffer) default: bulk path, consumes whole buffer ----
        DeepSpi d2 = new DeepSpi();
        ByteBuffer heap = ByteBuffer.wrap(new byte[]{1, 2, 3});
        d2.doUpdate(heap);
        check(d2.arrCalls == 1 && d2.byteCalls == 0, "engineUpdate(ByteBuffer) bulk-delegates to array variant");
        check(d2.lastOff == 0 && d2.lastLen == 3, "engineUpdate(ByteBuffer) passes correct off/len");
        check(heap.position() == 3, "engineUpdate(ByteBuffer) advances position to limit");
        ByteBuffer direct = ByteBuffer.allocateDirect(4);
        direct.put(new byte[]{9}).flip();
        DeepSpi d3 = new DeepSpi();
        d3.doUpdate(direct);
        check(d3.arrCalls == 1 && d3.byteCalls == 0, "engineUpdate(direct ByteBuffer) also bulk path");
        check(direct.position() == 1, "engineUpdate(direct ByteBuffer) consumed");
        DeepSpi d4 = new DeepSpi();
        d4.doUpdate(ByteBuffer.wrap(new byte[0]));
        check(d4.arrCalls == 0 && d4.byteCalls == 0, "engineUpdate(empty ByteBuffer) is a no-op");

        // ---- clone default: not Cloneable -> CloneNotSupportedException ----
        ct(CloneNotSupportedException.class, () -> new DeepSpi().doClone(), "clone default throws CNSE");

        // ---- facade equivalence: MessageDigest drives the SPI layer to a known vector ----
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] got = md.digest("abc".getBytes("UTF-8"));
        StringBuilder hex = new StringBuilder();
        for (byte b : got) hex.append(String.format("%02x", b));
        check("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad".equals(hex.toString()),
                "facade SHA-256(\"abc\") known vector (FIPS 180-4)");
    }

    static class MyDigestSpi extends MessageDigestSpi implements Cloneable {
        protected void engineUpdate(byte input) { }
        protected void engineUpdate(byte[] input, int offset, int len) { }
        protected byte[] engineDigest() { return new byte[]{9, 9, 9, 9}; }
        protected void engineReset() { }
        int doGetDigestLength() { return engineGetDigestLength(); }
        int doDigest(byte[] buf, int off, int len) throws DigestException { return engineDigest(buf, off, len); }
        void doUpdate(ByteBuffer b) { engineUpdate(b); }
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
