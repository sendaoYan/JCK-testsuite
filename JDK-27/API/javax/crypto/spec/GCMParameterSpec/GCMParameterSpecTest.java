/*
 * @test
 * @summary javax.crypto.spec.GCMParameterSpec conformance: white-box structure + black-box behavior.
 * @run main GCMParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class GCMParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.GCMParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
                "class is concrete, non-final");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.AlgorithmParameterSpec"::equals),
                "implements java.security.spec.AlgorithmParameterSpec");
        // constructors
        check(c.getDeclaredConstructor(int.class, byte[].class).getModifiers() == Modifier.PUBLIC,
                "public ctor (int, byte[])");
        check(c.getDeclaredConstructor(int.class, byte[].class, int.class, int.class)
                .getModifiers() == Modifier.PUBLIC, "public ctor (int, byte[], int, int)");
        // methods
        for (String m : new String[] {"getTLen", "getIV"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
        try {
            Method g = c.getDeclaredMethod("getTLen");
            check(g.getReturnType() == int.class, "getTLen returns int");
        } catch (NoSuchMethodException e) { throw new AssertionError("getTLen missing"); }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] iv = new byte[12];
        // --- original shallow assertions (kept) ---
        GCMParameterSpec s = new GCMParameterSpec(128, iv);
        check(s.getTLen() == 128, "getTLen");
        check(Arrays.equals(iv, s.getIV()), "getIV");
        GCMParameterSpec s2 = new GCMParameterSpec(96, iv, 4, 8);
        check(s2.getTLen() == 96 && s2.getIV().length == 8, "offset ctor");

        // --- defensive copies ---
        byte[] iv2 = {(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13,
                      (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17,
                      (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b};
        GCMParameterSpec fixed = new GCMParameterSpec(128, iv2);
        iv2[0] = (byte)0xff;
        check(fixed.getIV()[0] == (byte)0x10, "ctor copies IV (external mutation invisible)");
        byte[] got1 = fixed.getIV();
        byte[] got2 = fixed.getIV();
        check(got1 != got2, "getIV returns a new array each call");
        check(Arrays.equals(got1, got2), "repeated getIV contents equal");
        got1[0] = (byte)0x00;
        check(fixed.getIV()[0] == (byte)0x10, "getIV result copy is detached");

        // --- offset ctor exact subrange ---
        byte[] buf = new byte[16];
        for (int i = 0; i < 16; i++) buf[i] = (byte) (0x10 + i);
        GCMParameterSpec sub = new GCMParameterSpec(96, buf, 2, 10);
        check(Arrays.equals(sub.getIV(), Arrays.copyOfRange(buf, 2, 12)),
                "offset ctor IV equals src subrange");
        check(sub.getTLen() == 96, "offset ctor tLen");
        // zero-length IV is accepted by the class itself
        GCMParameterSpec empty = new GCMParameterSpec(128, buf, 2, 0);
        check(empty.getIV().length == 0, "zero-length IV accepted by class");
        // tLen is only checked for negativity at the class level
        GCMParameterSpec zero = new GCMParameterSpec(0, buf);
        check(zero.getTLen() == 0, "tLen==0 accepted by class (validation deferred to Cipher)");

        // --- constructor exception conditions (IllegalArgumentException) ---
        int[][] badOffset = {{-1, 4}, {2, -4}, {2, 15}};
        for (int[] oo : badOffset) {
            try {
                new GCMParameterSpec(128, buf, oo[0], oo[1]);
                check(false, "offset=" + oo[0] + ",len=" + oo[1] + " should throw IAE");
            } catch (IllegalArgumentException e) { passed++; }
        }
        try { new GCMParameterSpec(-1, buf); check(false, "negative tLen should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new GCMParameterSpec(128, (byte[]) null); check(false, "null src 2-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new GCMParameterSpec(128, null, 2, 4); check(false, "null src 4-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }

        // --- real chain: AES/GCM/NoPadding round trip with fixed key/IV/PT ---
        byte[] keyBytes = {(byte)0x00, (byte)0x01, (byte)0x02, (byte)0x03,
                           (byte)0x04, (byte)0x05, (byte)0x06, (byte)0x07,
                           (byte)0x08, (byte)0x09, (byte)0x0a, (byte)0x0b,
                           (byte)0x0c, (byte)0x0d, (byte)0x0e, (byte)0x0f};
        byte[] ivFixed = {(byte)0x10, (byte)0x11, (byte)0x12, (byte)0x13,
                          (byte)0x14, (byte)0x15, (byte)0x16, (byte)0x17,
                          (byte)0x18, (byte)0x19, (byte)0x1a, (byte)0x1b};
        byte[] pt = {(byte)0x6b, (byte)0xc1, (byte)0xbe, (byte)0xe2,
                     (byte)0x2e, (byte)0x40, (byte)0x9f, (byte)0x96,
                     (byte)0xe9, (byte)0x3d, (byte)0x7e, (byte)0x11,
                     (byte)0x73, (byte)0x93, (byte)0x17, (byte)0x2a};
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        GCMParameterSpec encSpec = new GCMParameterSpec(128, ivFixed);
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, key, encSpec);
        byte[] ct = enc.doFinal(pt);
        check(ct.length == 32, "GCM output = 16B ciphertext + 16B tag");
        check(java.util.HexFormat.of().formatHex(ct).equals(
                "afefbd4d210f2979fee023e4b4b4fc149ce9f1a378a3fe6159c9cae06f4447e5"),
                "GCM known-answer ciphertext (fixed key/IV/PT)");
        Cipher again = Cipher.getInstance("AES/GCM/NoPadding");
        again.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, ivFixed.clone()));
        check(Arrays.equals(ct, again.doFinal(pt)), "same spec contents reproduce same ciphertext");
        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, ivFixed));
        check(Arrays.equals(pt, dec.doFinal(ct)), "GCM decrypt round trip");

        // --- AAD through the same spec + integrity failures ---
        Cipher aadEnc = Cipher.getInstance("AES/GCM/NoPadding");
        aadEnc.init(Cipher.ENCRYPT_MODE, key, encSpec);
        aadEnc.updateAAD("header".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        byte[] aadCt = aadEnc.doFinal(pt);
        Cipher aadDec = Cipher.getInstance("AES/GCM/NoPadding");
        aadDec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, ivFixed));
        aadDec.updateAAD("header".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        check(Arrays.equals(pt, aadDec.doFinal(aadCt)), "AAD round trip");
        Cipher wrongAad = Cipher.getInstance("AES/GCM/NoPadding");
        wrongAad.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, ivFixed));
        wrongAad.updateAAD("HEADER".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            wrongAad.doFinal(aadCt);
            check(false, "wrong AAD should throw AEADBadTagException");
        } catch (AEADBadTagException e) { passed++; }
        byte[] tampered = aadCt.clone();
        tampered[0] ^= (byte)0x01;
        Cipher tamperDec = Cipher.getInstance("AES/GCM/NoPadding");
        tamperDec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, ivFixed));
        tamperDec.updateAAD("header".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        try {
            tamperDec.doFinal(tampered);
            check(false, "tampered ciphertext should throw AEADBadTagException");
        } catch (AEADBadTagException e) { passed++; }

        // --- Cipher-level validation of values the spec class accepts ---
        try {
            Cipher bad = Cipher.getInstance("AES/GCM/NoPadding");
            bad.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(0, ivFixed));
            check(false, "tLen=0 should be rejected by Cipher.init");
        } catch (java.security.InvalidAlgorithmParameterException e) { passed++; }
        try {
            Cipher bad = Cipher.getInstance("AES/GCM/NoPadding");
            bad.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, new byte[0]));
            check(false, "empty IV should be rejected by Cipher.init");
        } catch (java.security.InvalidAlgorithmParameterException e) { passed++; }
        // 1-byte IV is valid GCM input (non-96-bit IV path)
        Cipher one = Cipher.getInstance("AES/GCM/NoPadding");
        one.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, new byte[]{(byte)0x99}));
        byte[] oneCt = one.doFinal(pt);
        Cipher oneDec = Cipher.getInstance("AES/GCM/NoPadding");
        oneDec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, new byte[]{(byte)0x99}));
        check(Arrays.equals(pt, oneDec.doFinal(oneCt)), "1-byte IV round trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
