/*
 * @test
 * @summary javax.crypto.spec.PBEKeySpec conformance: white-box structure + black-box behavior.
 * @run main PBEKeySpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class PBEKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.PBEKeySpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
                "class is concrete, non-final");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.KeySpec"::equals),
                "implements java.security.spec.KeySpec");
        check(c.getDeclaredConstructor(char[].class).getModifiers() == Modifier.PUBLIC,
                "public ctor (char[])");
        check(c.getDeclaredConstructor(char[].class, byte[].class, int.class)
                .getModifiers() == Modifier.PUBLIC, "public ctor (char[], byte[], int)");
        check(c.getDeclaredConstructor(char[].class, byte[].class, int.class, int.class)
                .getModifiers() == Modifier.PUBLIC,
                "public ctor (char[], byte[], int, int)");
        for (String m : new String[] {"clearPassword", "getPassword", "getSalt",
                "getIterationCount", "getKeyLength"}) {
            Method dm;
            try {
                dm = c.getDeclaredMethod(m);
            } catch (NoSuchMethodException e) {
                throw new AssertionError("method " + m + " missing");
            }
            check(Modifier.isPublic(dm.getModifiers()) && Modifier.isFinal(dm.getModifiers()),
                    m + " is public final");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        char[] pw = "secret".toCharArray();
        byte[] salt = new byte[]{1, 2, 3};
        // --- original shallow assertions (kept) ---
        PBEKeySpec s = new PBEKeySpec(pw, salt, 1000, 128);
        check(Arrays.equals(pw, s.getPassword()), "getPassword");
        check(Arrays.equals(salt, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 1000, "getIterationCount");
        check(s.getKeyLength() == 128, "getKeyLength");
        s.clearPassword();
        boolean ise = false;
        try { s.getPassword(); } catch (IllegalStateException e) { ise = true; }
        check(ise, "clearPassword makes getPassword throw IllegalStateException");

        // --- defensive copies of password and salt ---
        char[] pw2 = "another".toCharArray();
        byte[] salt2 = {(byte)0x0a, (byte)0x1b, (byte)0x2c, (byte)0x3d,
                        (byte)0x4e, (byte)0x5f, (byte)0x60, (byte)0x71};
        PBEKeySpec spec = new PBEKeySpec(pw2, salt2, 2048, 256);
        pw2[0] = 'X';
        salt2[0] = (byte)0xff;
        check(spec.getPassword()[0] == 'a', "ctor clones password (external mutation invisible)");
        check(spec.getSalt()[0] == (byte)0x0a, "ctor clones salt (external mutation invisible)");
        char[] p1 = spec.getPassword();
        char[] p2 = spec.getPassword();
        check(p1 != p2 && Arrays.equals(p1, p2), "getPassword returns a fresh copy each call");
        p1[0] = 'Z';
        check(spec.getPassword()[0] == 'a', "getPassword result copy is detached");
        byte[] sa1 = spec.getSalt();
        sa1[0] = 0;
        check(spec.getSalt()[0] == (byte)0x0a, "getSalt result copy is detached");

        // --- password-only constructor: salt/iter/keylen defaults ---
        PBEKeySpec pwOnly = new PBEKeySpec("only".toCharArray());
        check(Arrays.equals("only".toCharArray(), pwOnly.getPassword()), "password-only getPassword");
        check(pwOnly.getSalt() == null, "password-only getSalt is null");
        check(pwOnly.getIterationCount() == 0, "password-only iterationCount defaults 0");
        check(pwOnly.getKeyLength() == 0, "password-only keyLength defaults 0");
        check(new PBEKeySpec(null).getPassword().length == 0, "null password becomes empty");
        check(new PBEKeySpec(new char[0]).getPassword().length == 0, "empty password stays empty");
        check(new PBEKeySpec(null, salt, 5).getSalt() != null, "null pw with salt still records salt");
        // 3-arg ctor: no key length preference
        PBEKeySpec three = new PBEKeySpec(pw2, salt, 4096);
        check(three.getKeyLength() == 0 && three.getIterationCount() == 4096,
                "3-arg ctor leaves keyLength 0");

        // --- validation: NPE / IAE ---
        try { new PBEKeySpec(pw, null, 1000); check(false, "null salt 3-arg should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new PBEKeySpec(pw, null, 1000, 128); check(false, "null salt 4-arg should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new PBEKeySpec(pw, new byte[0], 1000); check(false, "empty salt 3-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new PBEKeySpec(pw, new byte[0], 1000, 128); check(false, "empty salt 4-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new PBEKeySpec(pw, salt, 0); check(false, "iterationCount=0 3-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }
        try { new PBEKeySpec(pw, salt, 1000, 0); check(false, "keyLength=0 4-arg should throw IAE"); }
        catch (IllegalArgumentException e) { passed++; }

        // --- clearPassword lifecycle ---
        PBEKeySpec cleared = new PBEKeySpec(pw2, salt, 2048, 256);
        cleared.clearPassword();
        try {
            cleared.getPassword();
            check(false, "cleared getPassword should throw ISE");
        } catch (IllegalStateException e) { passed++; }
        cleared.clearPassword(); // idempotent
        try {
            cleared.getPassword();
            check(false, "second clear keeps getPassword throwing ISE");
        } catch (IllegalStateException e) { passed++; }
        check(Arrays.equals(cleared.getSalt(), salt) && cleared.getIterationCount() == 2048
                && cleared.getKeyLength() == 256,
                "after clearPassword salt/iterationCount/keyLength remain readable");

        // --- real chain: PBKDF2WithHmacSHA256 derives identical keys from identical specs ---
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] kSalt = {(byte)0x0a, (byte)0x1b, (byte)0x2c, (byte)0x3d,
                        (byte)0x4e, (byte)0x5f, (byte)0x60, (byte)0x71};
        PBEKeySpec k1 = new PBEKeySpec("topsecret".toCharArray(), kSalt, 2048, 256);
        PBEKeySpec k2 = new PBEKeySpec("topsecret".toCharArray(), kSalt.clone(), 2048, 256);
        SecretKey d1 = skf.generateSecret(k1);
        SecretKey d2 = skf.generateSecret(k2);
        check(Arrays.equals(d1.getEncoded(), d2.getEncoded()), "PBKDF2 derivation is reproducible");
        check(d1.getEncoded().length == 32, "derived key length = keyLength/8 = 32 bytes");
        check(hex(d1.getEncoded()).equals(
                "fd3fe870957e68eb8e321960e0190cb7e14168dffde66e5edd7c967fed2262a1"),
                "PBKDF2 known-answer for fixed password/salt/iterations/keyLength");
        // different iteration count -> different key
        SecretKey d3 = skf.generateSecret(new PBEKeySpec("topsecret".toCharArray(), kSalt, 4096, 256));
        check(!Arrays.equals(d1.getEncoded(), d3.getEncoded()),
                "different iterationCount derives different key");
        // cleared spec cannot be consumed by a factory: getPassword()'s IllegalStateException
        // is reported wrapped as InvalidKeySpecException by SecretKeyFactory.generateSecret
        PBEKeySpec clearedSpec = new PBEKeySpec("topsecret".toCharArray(), kSalt, 2048, 256);
        clearedSpec.clearPassword();
        try {
            skf.generateSecret(clearedSpec);
            check(false, "factory should reject cleared spec");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(e.getCause() instanceof IllegalStateException,
                    "cleared password surfaces as IllegalStateException cause");
        }
        // derived key material drives a real AES-GCM round trip
        Cipher gcm = Cipher.getInstance("AES/GCM/NoPadding");
        gcm.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(d1.getEncoded(), "AES"),
                new javax.crypto.spec.GCMParameterSpec(128,
                        new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}));
        byte[] gcmCt = gcm.doFinal(new byte[]{0x42});
        Cipher gcmDec = Cipher.getInstance("AES/GCM/NoPadding");
        gcmDec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(d2.getEncoded(), "AES"),
                new javax.crypto.spec.GCMParameterSpec(128,
                        new byte[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}));
        check(gcmDec.doFinal(gcmCt)[0] == (byte)0x42,
                "PBKDF2-derived key completes AES/GCM round trip");
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
