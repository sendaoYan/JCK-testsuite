/*
 * @test
 * @summary javax.crypto.CipherSpi conformance: white-box structure + black-box behavior.
 * @run main CipherSpiTest
 */

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

import javax.crypto.*;

public class CipherSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasPublicCtor(Class<?> c) {
        for (Constructor<?> k : c.getConstructors()) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.CipherSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass Object");
        check(hasPublicCtor(c), "public constructor");
        // method presence (kept from the original shallow test)
        checkMethods(c, new String[]{"engineSetMode", "engineSetPadding", "engineGetBlockSize",
                "engineGetOutputSize", "engineGetIV", "engineInit", "engineUpdate",
                "engineDoFinal", "engineWrap", "engineUnwrap"});
        // complete engine* surface incl. newer members
        checkMethods(c, new String[]{"engineGetParameters", "engineGetKeySize", "engineUpdateAAD"});

        // exactly 12 abstract engine methods, each protected
        String[][] ABSTRACT = {
            {"engineSetMode", "1"}, {"engineSetPadding", "1"},
            {"engineGetBlockSize", "0"}, {"engineGetOutputSize", "1"},
            {"engineGetIV", "0"}, {"engineGetParameters", "0"},
            {"engineInit", "3"}, {"engineInit", "4"}, {"engineInit", "4"},
            {"engineUpdate", "3"}, {"engineUpdate", "5"},
            {"engineDoFinal", "3"}, {"engineDoFinal", "5"},
        };
        int abstractCount = 0;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().startsWith("engine") && Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount == ABSTRACT.length, "12 abstract engine methods (found " + abstractCount + ")");
        for (String[] sig : ABSTRACT) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(sig[0]) && m.getParameterCount() == Integer.parseInt(sig[1])
                        && Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers())) {
                    found = true;
                    break;
                }
            }
            check(found, "protected abstract " + sig[0] + "/" + sig[1] + "p");
        }
        // concrete (default) protected methods that subclasses inherit
        String[][] DEFAULTS = {
            {"engineUpdate", "2"}, {"engineDoFinal", "2"},
            {"engineWrap", "1"}, {"engineUnwrap", "3"}, {"engineGetKeySize", "1"},
            {"engineUpdateAAD", "3"}, {"engineUpdateAAD", "1"},
        };
        for (String[] sig : DEFAULTS) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals(sig[0]) && m.getParameterCount() == Integer.parseInt(sig[1])
                        && !Modifier.isAbstract(m.getModifiers()) && Modifier.isProtected(m.getModifiers())) {
                    found = true;
                    break;
                }
            }
            check(found, "protected concrete (default) " + sig[0] + "/" + sig[1] + "p");
        }
    }
    static void checkMethods(Class<?> c, String[] ns) {
        for (String n : ns) check(hasMethod(c, n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    // Minimal concrete SPI with fixed, observable return values.
    static class MiniSpi extends CipherSpi {
        protected void engineSetMode(String mode) throws NoSuchAlgorithmException {
            if (!"ECB".equals(mode)) throw new NoSuchAlgorithmException("mode " + mode);
        }
        protected void engineSetPadding(String padding) throws NoSuchPaddingException {
            if (!"NoPadding".equals(padding)) throw new NoSuchPaddingException(padding);
        }
        protected int engineGetBlockSize() { return 7; }
        protected int engineGetOutputSize(int inputLen) { return inputLen + 1; }
        protected byte[] engineGetIV() { return new byte[]{1, 2, 3, 4}; }
        protected AlgorithmParameters engineGetParameters() { return null; }
        protected void engineInit(int opmode, Key key, SecureRandom random) { }
        protected void engineInit(int opmode, Key key, AlgorithmParameterSpec params, SecureRandom random) { }
        protected void engineInit(int opmode, Key key, AlgorithmParameters params, SecureRandom random) { }
        protected byte[] engineUpdate(byte[] input, int off, int len) {
            return Arrays.copyOfRange(input, off, off + len);
        }
        protected int engineUpdate(byte[] input, int off, int len, byte[] output, int outOff) {
            System.arraycopy(input, off, output, outOff, len);
            return len;
        }
        protected byte[] engineDoFinal(byte[] input, int off, int len) {
            return Arrays.copyOfRange(input, off, off + len);
        }
        protected int engineDoFinal(byte[] input, int off, int len, byte[] output, int outOff) {
            System.arraycopy(input, off, output, outOff, len);
            return len;
        }

        static void exercise() throws Exception {
            MiniSpi spi = new MiniSpi();
            // abstract-method dispatch: subclass values surface through the SPI
            check(spi.engineGetBlockSize() == 7, "engineGetBlockSize dispatch");
            check(spi.engineGetOutputSize(11) == 12, "engineGetOutputSize dispatch");
            check(Arrays.equals(spi.engineGetIV(), new byte[]{1, 2, 3, 4}), "engineGetIV dispatch");
            check(spi.engineGetParameters() == null, "engineGetParameters dispatch");
            // engineUpdate / engineDoFinal copy semantics
            check(Arrays.equals(spi.engineUpdate(new byte[]{1, 2, 3}, 0, 3), new byte[]{1, 2, 3}),
                    "engineUpdate(byte[],off,len) returns processed range");
            byte[] out5 = new byte[8];
            int n5 = spi.engineUpdate(new byte[]{1, 2, 3}, 0, 3, out5, 2);
            check(n5 == 3 && Arrays.equals(out5, new byte[]{0, 0, 1, 2, 3, 0, 0, 0}),
                    "engineUpdate 5-arg writes at outOff and returns length");
            check(Arrays.equals(spi.engineDoFinal(new byte[]{4}, 0, 1), new byte[]{4}),
                    "engineDoFinal(byte[],off,len) returns processed range");
            byte[] outD = new byte[4];
            int nD = spi.engineDoFinal(new byte[]{4, 5}, 0, 2, outD, 1);
            check(nD == 2 && Arrays.equals(outD, new byte[]{0, 4, 5, 0}),
                    "engineDoFinal 5-arg writes at outOff and returns length");
            // engineSetMode / engineSetPadding argument checks
            boolean modeBad = false, padBad = false;
            try { spi.engineSetMode("XYZ"); } catch (NoSuchAlgorithmException e) { modeBad = true; }
            try { spi.engineSetPadding("BadPad"); } catch (NoSuchPaddingException e) { padBad = true; }
            spi.engineSetMode("ECB");
            spi.engineSetPadding("NoPadding");
            check(modeBad && padBad, "engineSetMode -> NoSuchAlgorithmException, engineSetPadding -> NoSuchPaddingException");
            // engineInit overloads are callable no-ops
            SecretKey key = new javax.crypto.spec.SecretKeySpec(new byte[16], "AES");
            spi.engineInit(Cipher.ENCRYPT_MODE, key, (SecureRandom) null);
            spi.engineInit(Cipher.DECRYPT_MODE, key, (AlgorithmParameterSpec) null, (SecureRandom) null);
            spi.engineInit(Cipher.UNWRAP_MODE, key, (AlgorithmParameters) null, (SecureRandom) null);
            check(true, "engineInit 3 overloads callable");
            // inherited concrete default engineUpdate(ByteBuffer, ByteBuffer): bufferCrypt
            ByteBuffer bi = ByteBuffer.wrap(new byte[]{9, 8, 7});
            ByteBuffer bo = ByteBuffer.allocate(8);
            int nBB = spi.engineUpdate(bi, bo);
            check(nBB == 3 && bo.position() == 3 && bi.position() == 3
                    && bo.get(0) == 9 && bo.get(2) == 7,
                    "default engineUpdate(ByteBuffer,ByteBuffer): copies bytes, advances positions");
            ByteBuffer bi2 = ByteBuffer.wrap(new byte[]{5});
            ByteBuffer bo2 = ByteBuffer.allocate(8);
            int nBB2 = spi.engineDoFinal(bi2, bo2);
            check(nBB2 == 1 && bo2.position() == 1 && bo2.get(0) == 5,
                    "default engineDoFinal(ByteBuffer,ByteBuffer): copies through bufferCrypt");
            // zero-length update is short-circuited
            check(spi.engineUpdate(ByteBuffer.allocate(0), ByteBuffer.allocate(0)) == 0,
                    "default engineUpdate with empty input returns 0");
            // null buffers rejected
            boolean npe = false;
            try { spi.engineUpdate(null, null); } catch (NullPointerException e) { npe = true; }
            check(npe, "default engineUpdate(null, null) -> NullPointerException");
            // insufficient output buffer -> ShortBufferException (via engineGetOutputSize)
            boolean sbe = false;
            try {
                spi.engineUpdate(ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5}), ByteBuffer.allocate(2));
            } catch (ShortBufferException e) { sbe = true; }
            check(sbe, "default engineUpdate with 2B output for 5B input -> ShortBufferException");
            // unoverridden defaults throw UnsupportedOperationException
            boolean w = false, u = false, ks = false, aad1 = false, aad2 = false;
            try { spi.engineWrap(key); } catch (UnsupportedOperationException e) { w = true; }
            try { spi.engineUnwrap(new byte[2], "AES", Cipher.SECRET_KEY); } catch (UnsupportedOperationException e) { u = true; }
            try { spi.engineGetKeySize(key); } catch (UnsupportedOperationException e) { ks = true; }
            try { spi.engineUpdateAAD(new byte[]{1}, 0, 1); } catch (UnsupportedOperationException e) { aad1 = true; }
            try { spi.engineUpdateAAD(ByteBuffer.wrap(new byte[]{1})); } catch (UnsupportedOperationException e) { aad2 = true; }
            check(w && u && ks && aad1 && aad2,
                    "default engineWrap/engineUnwrap/engineGetKeySize/engineUpdateAAD x2 -> UnsupportedOperationException");
        }
    }

    static void testBlackBox() throws Exception {
        // the concrete-subclass behavior must be exercised from inside the subclass
        // (protected members), so delegate to MiniSpi.exercise
        MiniSpi.exercise();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
