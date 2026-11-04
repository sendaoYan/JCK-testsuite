/*
 * @test
 * @summary javax.crypto.Cipher conformance: white-box structure + black-box behavior.
 * @run main CipherTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class CipherTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    // returns true if executable threw exactly the expected exception type
    interface Body { void run() throws Exception; }
    static boolean throwsType(Class<? extends Throwable> t, Body b) {
        try { b.run(); return false; }
        catch (Throwable x) { return x.getClass().equals(t); }
    }

    // fixed deterministic vectors (hex-style fill, no randomness)
    static final byte[] KEY16 = new byte[16];
    static final byte[] IV12  = new byte[12];
    static final byte[] IV16  = new byte[16];
    static final byte[] PT32  = new byte[32];
    static final byte[] PT37  = new byte[37];
    static final byte[] PT5   = new byte[5];
    static {
        for (int i = 0; i < 16; i++) KEY16[i] = (byte) i;
        for (int i = 0; i < 12; i++) IV12[i] = (byte) (0xA0 + i);
        for (int i = 0; i < 16; i++) IV16[i] = (byte) (0x10 + i);
        for (int i = 0; i < 32; i++) PT32[i] = (byte) (0x40 + i);
        for (int i = 0; i < 37; i++) PT37[i] = (byte) (i * 3);
        for (int i = 0; i < 5;  i++) PT5[i]  = (byte) (0x70 + i);
    }
    static final SecretKeySpec AES_KEY = new SecretKeySpec(KEY16, "AES");
    static final GCMParameterSpec GCM128 = new GCMParameterSpec(128, IV12);
    static final IvParameterSpec CBC_SPEC = new IvParameterSpec(IV16);

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.Cipher");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(!Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete, extensible (NullCipher subclasses it)");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass Object");
        // protected constructor Cipher(CipherSpi, Provider, String)
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers())
                    && k.getParameterCount() == 3
                    && k.getParameterTypes()[0].getName().equals("javax.crypto.CipherSpi")
                    && k.getParameterTypes()[1].getName().equals("java.security.Provider")) {
                protCtor = true;
            }
        }
        check(protCtor, "protected Cipher(CipherSpi, Provider, String) exists");
        // mode / key-type constants
        String[] consts = {"ENCRYPT_MODE", "DECRYPT_MODE", "WRAP_MODE", "UNWRAP_MODE",
                           "PUBLIC_KEY", "PRIVATE_KEY", "SECRET_KEY"};
        for (String n : consts) {
            Field f = c.getDeclaredField(n);
            check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers())
                    && f.getType() == int.class, "static final int " + n);
        }
        // public API surface
        String[] methods = {"getInstance", "getProvider", "getAlgorithm", "getBlockSize",
                "getOutputSize", "getIV", "getParameters", "getExemptionMechanism",
                "init", "update", "doFinal", "wrap", "unwrap",
                "getMaxAllowedKeyLength", "getMaxAllowedParameterSpec", "updateAAD"};
        for (String n : methods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n)) { found = true; break; }
            check(found, "method " + n + " declared");
        }
        // public factory overloads
        int getInstanceOverloads = 0;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getInstance")) getInstanceOverloads++;
        check(getInstanceOverloads == 3, "3 getInstance overloads (String / String,String / String,Provider)");
        // update: 6 overloads, doFinal: 7 overloads, init: 7 overloads
        int nUp = 0, nFin = 0, nInit = 0;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("update")) nUp++;
            else if (m.getName().equals("doFinal")) nFin++;
            else if (m.getName().equals("init")) nInit++;
        }
        check(nUp == 5 && nFin == 7 && nInit == 8, "update x5 / doFinal x7 / init x8 overloads");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // constants (kept from original test)
        check(Cipher.ENCRYPT_MODE == 1 && Cipher.DECRYPT_MODE == 2 && Cipher.WRAP_MODE == 3 && Cipher.UNWRAP_MODE == 4, "mode constants");
        check(Cipher.PUBLIC_KEY == 1 && Cipher.PRIVATE_KEY == 2 && Cipher.SECRET_KEY == 3, "key type constants");

        // ---- AES/GCM/NoPadding: fixed key + IV round trips ----
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        check("AES/GCM/NoPadding".equals(c.getAlgorithm()), "getAlgorithm");
        check(c.getProvider() != null, "getProvider non-null");
        check(c.getBlockSize() == 16, "getBlockSize 16");
        Cipher c2 = Cipher.getInstance("AES/GCM/NoPadding", c.getProvider());
        check(c2.getAlgorithm().equals("AES/GCM/NoPadding"), "provider getInstance (Provider object)");
        Cipher cByName = Cipher.getInstance("AES/GCM/NoPadding", c.getProvider().getName());
        check(cByName.getProvider().equals(c.getProvider()), "provider getInstance (provider name)");
        check(throwsType(NoSuchProviderException.class,
                () -> Cipher.getInstance("AES/GCM/NoPadding", "NoSuchProviderXYZ")), "unknown provider name -> NoSuchProviderException");

        // init + getters
        c.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        check(Arrays.equals(IV12, c.getIV()), "getIV equals the GCM IV used at init");
        check(c.getParameters() != null && "GCM".equals(c.getParameters().getAlgorithm()),
                "getParameters non-null, algorithm GCM");
        check(c.getExemptionMechanism() == null, "getExemptionMechanism null without exemption");

        // 5B plaintext (not block-aligned), two independent instances
        byte[] ct5a = c.doFinal(PT5);
        Cipher c2e = Cipher.getInstance("AES/GCM/NoPadding");
        c2e.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        byte[] ct5b = c2e.doFinal(PT5);
        check(ct5a.length == PT5.length + 16, "GCM ciphertext length (payload + tag)");
        check(Arrays.equals(ct5a, ct5b), "GCM deterministic: same key+IV -> identical ciphertext");
        Cipher d5 = Cipher.getInstance("AES/GCM/NoPadding");
        d5.init(Cipher.DECRYPT_MODE, AES_KEY, GCM128);
        check(Arrays.equals(PT5, d5.doFinal(ct5a)), "GCM 5B round trip (kept from original)");

        // getOutputSize accounting (encrypt: pt + 16-byte tag; decrypt: exact pt)
        Cipher g1 = Cipher.getInstance("AES/GCM/NoPadding");
        g1.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        check(g1.getOutputSize(5) == 21 && g1.getOutputSize(37) == 53, "GCM encrypt getOutputSize = n + tag");
        byte[] ct37 = g1.doFinal(PT37);
        check(ct37.length == 53, "GCM 37B ciphertext length 53");
        Cipher g1d = Cipher.getInstance("AES/GCM/NoPadding");
        g1d.init(Cipher.DECRYPT_MODE, AES_KEY, GCM128);
        check(g1d.getOutputSize(ct37.length) == 37, "GCM decrypt getOutputSize = plaintext length");

        // AAD: covered by round trip
        Cipher ea = Cipher.getInstance("AES/GCM/NoPadding");
        ea.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        ea.updateAAD("hdr".getBytes("UTF-8"));
        byte[] ctAad = ea.doFinal(PT37);
        Cipher da = Cipher.getInstance("AES/GCM/NoPadding");
        da.init(Cipher.DECRYPT_MODE, AES_KEY, GCM128);
        da.updateAAD("hdr".getBytes("UTF-8"));
        check(Arrays.equals(PT37, da.doFinal(ctAad)), "GCM round trip with updateAAD");
        // AAD via ByteBuffer
        Cipher eab = Cipher.getInstance("AES/GCM/NoPadding");
        eab.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        eab.updateAAD(ByteBuffer.wrap(new byte[]{1, 2, 3}));
        byte[] ctBb = eab.doFinal(new byte[4]);
        Cipher dab = Cipher.getInstance("AES/GCM/NoPadding");
        dab.init(Cipher.DECRYPT_MODE, AES_KEY, GCM128);
        dab.updateAAD(new byte[]{1, 2, 3});
        check(dab.doFinal(ctBb).length == 4, "GCM round trip with ByteBuffer AAD");
        // AAD after update is rejected
        Cipher eLate = Cipher.getInstance("AES/GCM/NoPadding");
        eLate.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        eLate.update(new byte[5]);
        check(throwsType(IllegalStateException.class, () -> eLate.updateAAD(new byte[2])),
                "GCM updateAAD after update -> IllegalStateException");

        // multi-part GCM equals one-shot
        Cipher gm = Cipher.getInstance("AES/GCM/NoPadding");
        gm.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        java.io.ByteArrayOutputStream parts = new java.io.ByteArrayOutputStream();
        byte[] p1 = gm.update(PT32, 0, 20);
        byte[] p2 = gm.update(PT32, 20, 12);
        if (p1 != null) parts.write(p1);
        if (p2 != null) parts.write(p2);
        parts.write(gm.doFinal());
        Cipher gOne = Cipher.getInstance("AES/GCM/NoPadding");
        gOne.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        check(Arrays.equals(gOne.doFinal(PT32), parts.toByteArray()),
                "GCM multi-part (20+12) equals one-shot");

        // tampered ciphertext / wrong key -> AEADBadTagException
        byte[] tampered = ct37.clone();
        tampered[0] ^= 1;
        Cipher dT = Cipher.getInstance("AES/GCM/NoPadding");
        dT.init(Cipher.DECRYPT_MODE, AES_KEY, GCM128);
        check(throwsType(AEADBadTagException.class, () -> dT.doFinal(tampered)),
                "GCM tampered ciphertext -> AEADBadTagException");
        Cipher dW = Cipher.getInstance("AES/GCM/NoPadding");
        dW.init(Cipher.DECRYPT_MODE, new SecretKeySpec(new byte[16], "AES"), GCM128);
        check(throwsType(AEADBadTagException.class, () -> dW.doFinal(ct37)),
                "GCM wrong key -> AEADBadTagException");
        // SunJCE rejects re-using the same IV for another GCM encryption
        Cipher gRe = Cipher.getInstance("AES/GCM/NoPadding");
        gRe.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        gRe.doFinal(PT5);
        check(throwsType(InvalidAlgorithmParameterException.class,
                () -> gRe.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128)),
                "GCM re-init with same key+IV -> InvalidAlgorithmParameterException (IV reuse guard)");

        // ---- AES/CBC/NoPadding ----
        Cipher cbc = Cipher.getInstance("AES/CBC/NoPadding");
        cbc.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(cbc.getBlockSize() == 16 && Arrays.equals(IV16, cbc.getIV()),
                "CBC block size 16 and getIV equals init IV");
        check(cbc.getParameters() != null && "AES".equals(cbc.getParameters().getAlgorithm()),
                "CBC getParameters non-null, algorithm AES");
        check(cbc.getOutputSize(0) == 0 && cbc.getOutputSize(16) == 16 && cbc.getOutputSize(20) == 20,
                "CBC NoPadding encrypt getOutputSize");
        byte[] oneShot = cbc.doFinal(PT32);
        // chunked 4+4+8+16 must reproduce one-shot exactly (separate instance, same IV)
        Cipher cbcMulti = Cipher.getInstance("AES/CBC/NoPadding");
        cbcMulti.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        java.io.ByteArrayOutputStream chunks = new java.io.ByteArrayOutputStream();
        for (int[] seg : new int[][]{{0, 4}, {4, 4}, {8, 8}, {16, 16}}) {
            byte[] u = cbcMulti.update(PT32, seg[0], seg[1]);
            if (u != null) chunks.write(u);
        }
        chunks.write(cbcMulti.doFinal());
        check(Arrays.equals(oneShot, chunks.toByteArray()) && chunks.size() == 32,
                "CBC chunked (4+4+8+16) update+doFinal equals one-shot");
        // buffering observable through update() return lengths
        Cipher cbcBuf = Cipher.getInstance("AES/CBC/NoPadding");
        cbcBuf.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        byte[] u4 = cbcBuf.update(PT32, 0, 4);
        byte[] u12 = cbcBuf.update(PT32, 4, 12);
        check((u4 == null || u4.length == 0) && u12 != null && u12.length == 16,
                "CBC update buffers partial block, emits when a full block completes");
        // chunked decryption reproduces plaintext
        Cipher cbcDec = Cipher.getInstance("AES/CBC/NoPadding");
        cbcDec.init(Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC);
        java.io.ByteArrayOutputStream back = new java.io.ByteArrayOutputStream();
        byte[] dd1 = cbcDec.update(oneShot, 0, 16);
        byte[] dd2 = cbcDec.update(oneShot, 16, 16);
        if (dd1 != null) back.write(dd1);
        if (dd2 != null) back.write(dd2);
        back.write(cbcDec.doFinal());
        check(Arrays.equals(PT32, back.toByteArray()), "CBC chunked decryption round trip");
        // CBC re-init with same IV is legal and deterministic
        Cipher cbcRe = Cipher.getInstance("AES/CBC/NoPadding");
        cbcRe.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        byte[] r1 = cbcRe.doFinal(PT32);
        cbcRe.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(Arrays.equals(r1, cbcRe.doFinal(PT32)), "CBC re-init with same IV deterministic");
        // non block-multiple -> IllegalBlockSizeException
        Cipher cbcBad = Cipher.getInstance("AES/CBC/NoPadding");
        cbcBad.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(throwsType(IllegalBlockSizeException.class, () -> cbcBad.doFinal(new byte[20])),
                "CBC NoPadding non-multiple input -> IllegalBlockSizeException");
        // ByteBuffer doFinal equals array doFinal
        Cipher cbcBB = Cipher.getInstance("AES/CBC/NoPadding");
        cbcBB.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        ByteBuffer outBB = ByteBuffer.allocate(32);
        int nBB = cbcBB.doFinal(ByteBuffer.wrap(PT32), outBB);
        check(nBB == 32 && Arrays.equals(oneShot, outBB.array()), "CBC doFinal(ByteBuffer) equals doFinal(byte[])");
        // doFinal into caller buffer at an offset
        Cipher cbcOff = Cipher.getInstance("AES/CBC/NoPadding");
        cbcOff.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        byte[] outArr = new byte[40];
        int nOff = cbcOff.doFinal(PT32, 0, 32, outArr, 3);
        check(nOff == 32 && Arrays.equals(oneShot, Arrays.copyOfRange(outArr, 3, 35)),
                "doFinal(byte[],int,int,byte[],int) writes at output offset");
        // update + doFinal into the same buffer
        Cipher cbcPipe = Cipher.getInstance("AES/CBC/NoPadding");
        cbcPipe.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        byte[] pipe = new byte[32];
        int w1 = cbcPipe.update(PT32, 0, 32, pipe, 0);
        int w2 = cbcPipe.doFinal(pipe, w1);
        check(w1 == 32 && w2 == 0 && Arrays.equals(oneShot, Arrays.copyOf(pipe, 32)),
                "update(in,off,len,out) + doFinal(out,len) pipeline");

        // ---- default transformation ("AES" == AES/ECB/PKCS5Padding) ----
        Cipher aesOnly = Cipher.getInstance("AES");
        aesOnly.init(Cipher.ENCRYPT_MODE, AES_KEY);
        check("AES".equals(aesOnly.getAlgorithm()) && aesOnly.getIV() == null
                && aesOnly.getParameters() == null && aesOnly.doFinal(PT5).length == 16,
                "getInstance(\"AES\"): ECB, no IV, default PKCS5 pads 5B -> 16B");

        // ---- state / argument validation ----
        Cipher uninit = Cipher.getInstance("AES/CBC/NoPadding");
        check(throwsType(IllegalStateException.class, () -> uninit.getOutputSize(5)),
                "getOutputSize before init -> IllegalStateException");
        check(throwsType(IllegalStateException.class, () -> uninit.update(new byte[4])),
                "update before init -> IllegalStateException");
        check(throwsType(IllegalStateException.class, () -> uninit.doFinal()),
                "doFinal before init -> IllegalStateException");
        Cipher negTest = Cipher.getInstance("AES/CBC/NoPadding");
        negTest.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(throwsType(IllegalArgumentException.class, () -> negTest.getOutputSize(-1)),
                "getOutputSize(-1) -> IllegalArgumentException");
        // exception taxonomy for factory + init
        check(throwsType(NoSuchAlgorithmException.class, () -> Cipher.getInstance("NoSuchSuch/ECB/NoPadding")),
                "unknown algorithm -> NoSuchAlgorithmException");
        check(throwsType(NoSuchAlgorithmException.class, () -> Cipher.getInstance("AES/CBC/NoSuchPad")),
                "unknown padding -> NoSuchAlgorithmException (transformation lookup fails first)");
        Cipher badKey = Cipher.getInstance("AES/CBC/NoPadding");
        check(throwsType(InvalidKeyException.class,
                () -> badKey.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[20], "AES"), CBC_SPEC)),
                "AES with 20-byte key -> InvalidKeyException");
        Cipher shortBuf = Cipher.getInstance("AES/CBC/NoPadding");
        shortBuf.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(throwsType(ShortBufferException.class, () -> shortBuf.doFinal(PT32, 0, 32, new byte[8])),
                "doFinal into 8-byte output -> ShortBufferException");
        check(Cipher.getMaxAllowedKeyLength("AES") == Integer.MAX_VALUE,
                "unlimited crypto policy: getMaxAllowedKeyLength(AES) == Integer.MAX_VALUE");

        // ---- RSA wrap / unwrap of an AES key ----
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        byte[] wrappedKeyBytes = new byte[32];
        for (int i = 0; i < 32; i++) wrappedKeyBytes[i] = (byte) (0x60 + i);
        SecretKey toWrap = new SecretKeySpec(wrappedKeyBytes, "AES");
        Cipher wrapper = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        wrapper.init(Cipher.WRAP_MODE, kp.getPublic());
        byte[] wrapped = wrapper.wrap(toWrap);
        check(wrapped.length == 256, "RSA-2048 OAEP wrapped key is 256 bytes");
        Cipher unwrapper = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        unwrapper.init(Cipher.UNWRAP_MODE, kp.getPrivate());
        Key unwrapped = unwrapper.unwrap(wrapped, "AES", Cipher.SECRET_KEY);
        check(unwrapped instanceof SecretKey && "AES".equals(unwrapped.getAlgorithm())
                && Arrays.equals(wrappedKeyBytes, unwrapped.getEncoded()),
                "unwrapped key equals original SecretKey (encoded + algorithm + type)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
