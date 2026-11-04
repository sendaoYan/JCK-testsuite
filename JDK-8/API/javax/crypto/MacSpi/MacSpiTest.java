/*
 * @test
 * @summary javax.crypto.MacSpi conformance: white-box structure + black-box behavior.
 * @run main MacSpiTest
 */

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.security.Key;
import java.security.Provider;
import java.security.Security;
import java.security.spec.AlgorithmParameterSpec;
import javax.crypto.Mac;
import javax.crypto.MacSpi;
import javax.crypto.spec.SecretKeySpec;

public class MacSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.MacSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getInterfaces().length == 0, "implements no interfaces");
        boolean pubCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) pubCtor = true;
        check(pubCtor, "public no-arg constructor");
        // abstract engine methods, exactly protected
        String[] abstractMethods = {"engineGetMacLength", "engineInit",
                "engineUpdate", "engineDoFinal", "engineReset"};
        for (String n : abstractMethods) {
            boolean foundAbstract = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n) && Modifier.isAbstract(m.getModifiers())
                        && Modifier.isProtected(m.getModifiers())) foundAbstract = true;
            check(foundAbstract, "protected abstract " + n);
        }
        // engineUpdate(ByteBuffer) exists but is NOT abstract (concrete default)
        boolean bbConcrete = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("engineUpdate")
                    && m.getParameterTypes()[0].getName().equals("java.nio.ByteBuffer")
                    && Modifier.isProtected(m.getModifiers()) && !Modifier.isAbstract(m.getModifiers()))
                bbConcrete = true;
        check(bbConcrete, "engineUpdate(ByteBuffer) protected with default implementation");
        // clone() is public, concrete, throws CloneNotSupportedException
        boolean cloneOk = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("clone") && Modifier.isPublic(m.getModifiers())
                    && !Modifier.isAbstract(m.getModifiers()))
                cloneOk = true;
        check(cloneOk, "public concrete clone()");
        // signatures of the abstract core
        boolean sigOk = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("engineInit")) {
                Class<?>[] ps = m.getParameterTypes();
                if (ps.length == 2 && ps[0].getName().equals("java.security.Key")
                        && ps[1].getName().equals("java.security.spec.AlgorithmParameterSpec")) sigOk = true;
            }
        }
        check(sigOk, "engineInit(Key, AlgorithmParameterSpec) signature");
        boolean doFinalSig = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("engineDoFinal") && m.getReturnType() == byte[].class
                    && m.getParameterCount() == 0) doFinalSig = true;
        check(doFinalSig, "byte[] engineDoFinal() signature");
        boolean lenSig = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("engineGetMacLength") && m.getReturnType() == int.class
                    && m.getParameterCount() == 0) lenSig = true;
        check(lenSig, "int engineGetMacLength() signature");
        boolean updSigs = true;
        for (String sig : new String[]{"engineUpdate(byte)", "engineUpdate(byte[],int,int)"}) {
            boolean f = false;
            for (Method m : c.getDeclaredMethods()) if (m.toString().contains(sig)) f = true;
            if (!f) updSigs = false;
        }
        check(updSigs, "engineUpdate overloads signatures");
    }

    // ---- Part 2: black-box (behavioral) ----

    // minimal concrete subclass: fixed MAC length 7, counts protected-method invocations
    static class FixedMacSpi extends MacSpi implements Cloneable {
        int macLength = 7;
        int inits, updates, doFinals, resets, totalUpdates;
        byte lastByte = 0;
        Key lastKey;
        AlgorithmParameterSpec lastSpec;

        public int macLength() { return engineGetMacLength(); }
        public void init(Key k, AlgorithmParameterSpec s) { engineInit(k, s); }
        public void update(byte b) { engineUpdate(b); }
        public byte[] doFinal() { return engineDoFinal(); }
        public void reset() { engineReset(); }
        public int feed(ByteBuffer b) { int before = updates; engineUpdate(b); return updates - before; }

        protected int engineGetMacLength() { return macLength; }
        protected void engineInit(Key key, AlgorithmParameterSpec spec) {
            inits++; lastKey = key; lastSpec = spec;
        }
        protected void engineUpdate(byte b) { updates++; totalUpdates++; lastByte = b; }
        protected void engineUpdate(byte[] in, int off, int len) { updates++; totalUpdates++; }
        protected byte[] engineDoFinal() {
            doFinals++;
            byte[] r = new byte[macLength];
            r[0] = (byte) updates;                     // observable state
            return r;
        }
        protected void engineReset() { resets++; updates = 0; }
    }

    // subclass that does NOT implement Cloneable
    static class NonCloneableSpi extends MacSpi {
        protected int engineGetMacLength() { return 0; }
        protected void engineInit(Key key, AlgorithmParameterSpec spec) { }
        protected void engineUpdate(byte b) { }
        protected void engineUpdate(byte[] in, int off, int len) { }
        protected byte[] engineDoFinal() { return new byte[0]; }
        protected void engineReset() { }
    }

    static void testBlackBox() throws Exception {
        FixedMacSpi spi = new FixedMacSpi();

        // each abstract method, once implemented, is directly invocable and returns our fixed values
        check(spi.macLength() == 7, "engineGetMacLength returns subclass value");
        SecretKeySpec key = new SecretKeySpec(new byte[16], "MOCK");
        spi.init(key, null);
        check(spi.inits == 1 && spi.lastKey == key && spi.lastSpec == null,
                "engineInit receives key and spec");
        spi.update((byte) 42);
        check(spi.updates == 1 && spi.lastByte == 42, "engineUpdate(byte) receives the byte");
        byte[] tag = spi.doFinal();
        check(tag.length == 7 && tag[0] == 1, "engineDoFinal returns length-7 tag reflecting update count");
        spi.reset();
        check(spi.resets == 1, "engineReset invoked");
        spi.update((byte) 1);
        byte[] tag2 = spi.doFinal();                   // updates was reset to 0 by engineReset
        check(tag2[0] == 1, "state cleared between doFinal calls via engineReset semantics");

        // default engineUpdate(ByteBuffer) delegates to engineUpdate(byte[], int, int)
        ByteBuffer heap = ByteBuffer.wrap(new byte[]{9, 8, 7, 6});
        int delegated = spi.feed(heap);
        check(delegated == 1, "default engineUpdate(ByteBuffer) delegates to engineUpdate(byte[],int,int)");
        check(!heap.hasRemaining(), "default engineUpdate consumes the whole buffer");
        ByteBuffer direct = ByteBuffer.allocateDirect(4);
        direct.put(new byte[]{1, 2, 3, 4});
        direct.flip();
        check(spi.feed(direct) == 1, "default engineUpdate handles direct buffers through the array path");

        // clone(): Cloneable subclass -> same class, state copied
        FixedMacSpi clone = (FixedMacSpi) spi.clone();
        check(clone != spi && clone.getClass() == FixedMacSpi.class, "clone() of Cloneable SPI returns copy");
        check(clone.macLength() == spi.macLength() && clone.resets == spi.resets,
                "clone carries SPI state");
        // Non-Cloneable subclass -> CloneNotSupportedException
        NonCloneableSpi ncs = new NonCloneableSpi();
        try {
            ncs.clone();
            check(false, "clone() of non-Cloneable SPI must throw");
        } catch (CloneNotSupportedException e) {
            check(true, "clone() without Cloneable -> CloneNotSupportedException");
        }

        // full lifecycle through the Mac engine wrapper (protected ctor Mac(MacSpi,...))
        driveWrapperMac(Security.getProvider("SunJCE"), new FixedMacSpi());

        // a second independent SPI instance is unaffected by the first
        FixedMacSpi other = new FixedMacSpi();
        int spiUpdatesBefore = spi.updates;
        other.init(key, null);
        other.update((byte) 5);
        check(other.updates == 1 && spi.updates == spiUpdatesBefore, "SPI instances keep independent state");
        check(other.doFinal()[0] == 1, "fresh SPI starts from clean state");
        check(other.inits == 1 && other.resets == 0, "fresh SPI only saw its own calls");
    }

    // engine wrapper built through the protected Mac constructor
    static class WrapperMac extends Mac {
        WrapperMac(MacSpi s, Provider p, String a) { super(s, p, a); }
    }

    // drive a Mac engine around the user SPI and verify the dispatch chain
    static void driveWrapperMac(Provider p, FixedMacSpi spi) throws Exception {
        WrapperMac mac = new WrapperMac(spi, p, "MOCKMAC");
        mac.init(new SecretKeySpec(new byte[16], "MOCK"));
        mac.update((byte) 3);
        mac.update(new byte[]{4, 5}, 0, 2);
        byte[] out = mac.doFinal();
        check(mac.getMacLength() == 7, "Mac wrapper reports SPI mac length");
        check(out.length == 7, "Mac wrapper returns SPI doFinal bytes");
        check(spi.inits == 1 && spi.totalUpdates == 2 && spi.doFinals == 1,
                "Mac wrapper -> SPI dispatch counts (init=" + spi.inits
                + " update=" + spi.totalUpdates + " doFinal=" + spi.doFinals + ")");
        mac.update((byte) 6);
        mac.reset();
        check(spi.resets == 2, "Mac.reset() reaches engineReset (once from doFinal, once explicit)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
