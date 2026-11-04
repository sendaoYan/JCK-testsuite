/*
 * @test
 * @summary javax.crypto.spec.IvParameterSpec conformance: white-box structure + black-box behavior.
 * @run main IvParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class IvParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.IvParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
                "class is concrete, non-final");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.AlgorithmParameterSpec"::equals),
                "implements java.security.spec.AlgorithmParameterSpec");
        check(c.getDeclaredConstructor(byte[].class).getModifiers() == Modifier.PUBLIC,
                "public ctor (byte[])");
        check(c.getDeclaredConstructor(byte[].class, int.class, int.class).getModifiers()
                == Modifier.PUBLIC, "public ctor (byte[], int, int)");
        for (String m : new String[] {"getIV"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] iv = new byte[16];
        // --- original shallow assertions (kept) ---
        IvParameterSpec s = new IvParameterSpec(iv);
        check(Arrays.equals(iv, s.getIV()), "getIV");
        IvParameterSpec s2 = new IvParameterSpec(iv, 4, 8);
        check(s2.getIV().length == 8, "offset ctor");

        // --- defensive copies ---
        byte[] ivFixed = new byte[16];
        for (int i = 0; i < 16; i++) ivFixed[i] = (byte) (0xf0 + i);
        IvParameterSpec fixed = new IvParameterSpec(ivFixed);
        ivFixed[0] = (byte)0x0a;
        check(fixed.getIV()[0] == (byte)0xf0, "ctor copies IV (external mutation invisible)");
        byte[] g1 = fixed.getIV();
        byte[] g2 = fixed.getIV();
        check(g1 != g2, "getIV returns a new array each call");
        check(Arrays.equals(g1, g2), "repeated getIV contents equal");
        g1[0] = (byte)0x00;
        check(fixed.getIV()[0] == (byte)0xf0, "getIV result copy is detached");

        // --- offset ctor exact subrange ---
        IvParameterSpec sub = new IvParameterSpec(ivFixed, 4, 8);
        check(Arrays.equals(sub.getIV(), Arrays.copyOfRange(ivFixed, 4, 12)),
                "offset ctor IV equals src subrange");
        IvParameterSpec empty = new IvParameterSpec(ivFixed, 8, 0);
        check(empty.getIV().length == 0, "zero-length IV accepted by class");

        // --- exception conditions ---
        try { new IvParameterSpec(null); check(false, "null iv 1-arg should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new IvParameterSpec(null, 0, 0); check(false, "null iv 3-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new IvParameterSpec(iv, -2, iv.length); check(false, "negative offset should throw AIOOBE"); }
        catch (ArrayIndexOutOfBoundsException e) { passed++; }
        try { new IvParameterSpec(iv, 0, -2); check(false, "negative len should throw AIOOBE"); }
        catch (ArrayIndexOutOfBoundsException e) { passed++; }
        try { new IvParameterSpec(new byte[4], 2, 3); check(false, "offset+len > length should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }

        // --- real chain: AES/CBC/PKCS5Padding with fixed key/IV ---
        byte[] keyBytes = new byte[16];
        for (int i = 0; i < 16; i++) keyBytes[i] = (byte) i;
        byte[] ivCbc = new byte[16];
        for (int i = 0; i < 16; i++) ivCbc[i] = (byte) (0x10 + i);
        byte[] pt = {(byte)0x6b, (byte)0xc1, (byte)0xbe, (byte)0xe2,
                     (byte)0x2e, (byte)0x40, (byte)0x9f, (byte)0x96,
                     (byte)0xe9, (byte)0x3d, (byte)0x7e, (byte)0x11,
                     (byte)0x73, (byte)0x93, (byte)0x17, (byte)0x2a};
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher enc = Cipher.getInstance("AES/CBC/PKCS5Padding");
        enc.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivCbc));
        byte[] ct = enc.doFinal(pt);
        check(hex(ct).equals(
                "f9a20cc59b05fa9abd2b260a89cc751da89d0c07c360b850889a701269c896ee"),
                "AES/CBC known-answer ciphertext (fixed key/IV/PT)");
        check(Arrays.equals(enc.getIV(), ivCbc), "Cipher.getIV equals spec IV after init");
        Cipher enc2 = Cipher.getInstance("AES/CBC/PKCS5Padding");
        enc2.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivCbc.clone()));
        check(Arrays.equals(ct, enc2.doFinal(pt)), "same IV reproduces same ciphertext");
        Cipher dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
        dec.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(ivCbc));
        check(Arrays.equals(pt, dec.doFinal(ct)), "CBC decrypt round trip");
        // different IV -> different ciphertext, and old IV cannot decrypt it
        byte[] ivOther = new byte[16];
        Arrays.fill(ivOther, (byte)0x55);
        Cipher enc3 = Cipher.getInstance("AES/CBC/PKCS5Padding");
        enc3.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivOther));
        check(!Arrays.equals(ct, enc3.doFinal(pt)), "different IV yields different ciphertext");

        // --- AES/CBC/NoPadding block-aligned round trip via offset ctor spec ---
        byte[] pt2 = new byte[32];
        for (int i = 0; i < 32; i++) pt2[i] = (byte) (i + 1);
        byte[] src = new byte[24];
        for (int i = 0; i < 24; i++) src[i] = (byte) (0xa0 + i);
        Cipher encN = Cipher.getInstance("AES/CBC/NoPadding");
        encN.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(src, 8, 16));
        byte[] ct2 = encN.doFinal(pt2);
        Cipher decN = Cipher.getInstance("AES/CBC/NoPadding");
        decN.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(src, 8, 16));
        check(Arrays.equals(pt2, decN.doFinal(ct2)), "NoPadding round trip via offset-ctor IV");
        check(ct2.length == 32, "NoPadding preserves length");
    }

    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
