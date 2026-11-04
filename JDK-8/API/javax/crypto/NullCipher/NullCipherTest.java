/*
 * @test
 * @summary javax.crypto.NullCipher conformance: white-box structure + black-box behavior.
 * @run main NullCipherTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.Arrays;

public class NullCipherTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static final byte[] PT = {1, 2, 3, 4, 5};

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.NullCipher");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface()
                && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        check(c.getSuperclass().getName().equals("javax.crypto.Cipher"),
                "extends javax.crypto.Cipher");
        boolean pubNoArg = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 0) pubNoArg = true;
        check(pubNoArg, "public NullCipher() constructor");
        // all Cipher behavior is inherited; NullCipher declares only its constructor
        check(c.getDeclaredMethods().length == 0, "no new methods; behavior fully inherited from Cipher");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // ===== original assertions kept =====
        NullCipher nc = new NullCipher();
        check(nc instanceof Cipher, "extends Cipher");
        check(nc.getBlockSize() == 1, "block size 1");
        nc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        byte[] in = new byte[]{1, 2, 3};
        check(Arrays.equals(in, nc.doFinal(in)), "identity doFinal");

        // ===== identity semantics: copies, not the same reference =====
        byte[] pt5 = PT.clone();
        byte[] out = nc.doFinal(pt5);
        check(Arrays.equals(pt5, out) && pt5 != out, "doFinal returns an equal copy, not the same array");
        byte[] upd = nc.update(pt5);
        check(Arrays.equals(pt5, upd) && pt5 != upd, "update(byte[]) returns an equal copy");
        check(Arrays.equals(new byte[]{2, 3, 4}, nc.update(pt5, 1, 3)),
                "update(byte[],off,len) returns the given subrange");
        byte[] at = new byte[4];
        int nAt = nc.update(pt5, 2, 2, at, 1);
        check(nAt == 2 && at[1] == 3 && at[2] == 4 && at[0] == 0 && at[3] == 0,
                "update(byte[],off,len,out,outOff) copies at outOff, returns length");
        int nFin = nc.doFinal(pt5, 0, 3, at, 0);
        check(nFin == 3 && at[0] == 1 && at[1] == 2 && at[2] == 3,
                "doFinal(byte[],off,len,out,outOff) copies at outOff");
        // multi-part then finish
        byte[] mid = nc.update(new byte[]{9});
        check(mid != null && mid.length == 1 && mid[0] == 9, "multi-part update passes through");
        byte[] finish = nc.doFinal();
        check(finish == null || finish.length == 0, "no-arg doFinal after update terminates cleanly");

        // ===== usable without any init (per javadoc: no state, never IllegalStateException) =====
        NullCipher raw = new NullCipher();
        check(Arrays.equals(PT, raw.update(PT)), "update() before init works");
        check(Arrays.equals(PT, raw.doFinal(PT)), "doFinal() before init works");
        check(raw.getOutputSize(5) == 5 && raw.getOutputSize(0) == 0,
                "getOutputSize before init: identity (NullCipher exempt from initialized check)");

        // ===== metadata getters: identity cipher has no provider/algorithm/params =====
        NullCipher meta = new NullCipher();
        check(meta.getAlgorithm() == null, "getAlgorithm null (no transformation)");
        check(meta.getProvider() == null, "getProvider null (not provider-backed)");
        check(meta.getParameters() == null, "getParameters null");
        check(meta.getExemptionMechanism() == null, "getExemptionMechanism null");
        check(meta.getIV() != null && meta.getIV().length == 8,
                "getIV returns fixed non-null 8-byte array");
        check(throwsIAE(meta), "getOutputSize(-1) -> IllegalArgumentException");

        // ===== init accepts every mode and any key without throwing =====
        SecretKeySpec anyKey = new SecretKeySpec(new byte[16], "AES");
        NullCipher modes = new NullCipher();
        modes.init(Cipher.ENCRYPT_MODE, anyKey);
        byte[] e1 = modes.doFinal(PT);
        modes.init(Cipher.DECRYPT_MODE, new SecretKeySpec(new byte[8], "DES"));
        check(Arrays.equals(PT, modes.doFinal(PT)), "DECRYPT_MODE init then identity doFinal");
        modes.init(Cipher.WRAP_MODE, anyKey);
        modes.init(Cipher.UNWRAP_MODE, anyKey);
        check(true, "init in ENCRYPT/DECRYPT/WRAP/UNWRAP modes never throws");
        modes.init(Cipher.ENCRYPT_MODE, anyKey, new GCMParameterSpec(128, new byte[12]));
        check(Arrays.equals(e1, modes.doFinal(PT)), "init with AlgorithmParameterSpec ignored, output unchanged");
        modes.init(Cipher.ENCRYPT_MODE, anyKey, (java.security.SecureRandom) null);
        check(Arrays.equals(e1, modes.doFinal(PT)), "init with SecureRandom ignored, output unchanged");

        // ===== unsupported operations inherit CipherSpi defaults =====
        boolean w = false, u = false, aad = false;
        try { modes.wrap(anyKey); } catch (UnsupportedOperationException ex) { w = true; }
        try { modes.unwrap(new byte[8], "AES", Cipher.SECRET_KEY); } catch (UnsupportedOperationException ex) { u = true; }
        try { modes.updateAAD(new byte[3]); } catch (UnsupportedOperationException ex) { aad = true; }
        check(w && u && aad, "wrap/unwrap/updateAAD -> UnsupportedOperationException");

        // ===== stream composition: pass-through in both directions =====
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CipherOutputStream cos = new CipherOutputStream(bos, new NullCipher());
        cos.write(PT);
        cos.close();
        check(Arrays.equals(PT, bos.toByteArray()), "CipherOutputStream writes plaintext unchanged");
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(bos.toByteArray()), new NullCipher());
        byte[] readBack = new byte[PT.length];
        int total = 0, r;
        while ((r = cis.read(readBack, total, PT.length - total)) != -1) total += r;
        cis.close();
        check(total == PT.length && Arrays.equals(PT, readBack), "CipherInputStream reads plaintext unchanged");
    }
    static boolean throwsIAE(NullCipher nc) {
        try { nc.getOutputSize(-1); return false; }
        catch (IllegalArgumentException e) { return true; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
