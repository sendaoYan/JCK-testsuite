/*
 * @test
 * @summary javax.crypto.KeyGenerator conformance: white-box structure + black-box behavior.
 * @run main KeyGeneratorTest
 */

import java.lang.reflect.*;
import java.security.InvalidParameterException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.SecureRandom;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class KeyGeneratorTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KeyGenerator");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        // protected constructor KeyGenerator(KeyGeneratorSpi, Provider, String)
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 3
                    && k.getParameterTypes()[0].getName().equals("javax.crypto.KeyGeneratorSpi")
                    && k.getParameterTypes()[1].getName().equals("java.security.Provider")) protCtor = true;
        }
        check(protCtor, "protected KeyGenerator(KeyGeneratorSpi, Provider, String) ctor");
        String[] methods = {"getAlgorithm", "getInstance", "getProvider", "init", "generateKey"};
        for (String n : methods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declared method " + n);
        }
        boolean factoryStatic = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getInstance") && Modifier.isStatic(m.getModifiers())
                    && Modifier.isFinal(m.getModifiers())) factoryStatic = true;
        check(factoryStatic, "static final getInstance factories");
        boolean genFinal = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("generateKey") && Modifier.isPublic(m.getModifiers())
                    && m.getReturnType().getName().equals("javax.crypto.SecretKey")) genFinal = true;
        check(genFinal, "public SecretKey generateKey()");
        // init overloads: (SecureRandom), (AlgorithmParameterSpec), (AlgorithmParameterSpec, SecureRandom),
        // (int), (int, SecureRandom)
        int initOverloads = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("init")) initOverloads++;
        check(initOverloads == 5, "5 init overloads, found " + initOverloads);
    }

    // ---- Part 2: black-box (behavioral) ----
    static byte[] gcmRoundtrip(SecretKey key, byte[] plain) throws Exception {
        byte[] iv = new byte[12];
        for (int i = 0; i < 12; i++) iv[i] = (byte) (0x10 + i);
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = enc.doFinal(plain);
        check(ct.length == plain.length + 16, "GCM ciphertext = plaintext + 128-bit tag");
        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        return dec.doFinal(ct);
    }

    static void testBlackBox() throws Exception {
        // --- AES 128 ---
        KeyGenerator kg = KeyGenerator.getInstance("AES");
        check("AES".equals(kg.getAlgorithm()), "getAlgorithm");
        check(kg.getProvider() != null && kg.getProvider().getName().length() > 0, "getProvider non-null");
        kg.init(128);
        SecretKey k128 = kg.generateKey();
        check("AES".equals(k128.getAlgorithm()), "generated key algorithm AES");
        check("RAW".equals(k128.getFormat()), "generated key format RAW");
        check(k128.getEncoded().length == 128 / 8, "128-bit key encodes to 16 bytes");
        check(k128 instanceof SecretKey, "generated key is a SecretKey");

        // --- AES 256 (unlimited crypto policy) ---
        KeyGenerator kg256 = KeyGenerator.getInstance("AES");
        kg256.init(256);
        SecretKey k256 = kg256.generateKey();
        check(k256.getEncoded().length == 256 / 8, "256-bit key encodes to 32 bytes");
        check("AES".equals(k256.getAlgorithm()), "256-bit key algorithm");

        // --- two generated keys differ; both are usable for a full AES/GCM roundtrip ---
        byte[] plain = "JCK-27 behavioral test plaintext".getBytes("UTF-8");
        SecretKey first = kg.generateKey();
        SecretKey second = kg.generateKey();
        check(!Arrays.equals(first.getEncoded(), second.getEncoded()), "two generateKey() calls yield different keys");
        check(Arrays.equals(gcmRoundtrip(first, plain), plain), "first generated key completes AES/GCM roundtrip");
        check(Arrays.equals(gcmRoundtrip(second, plain), plain), "second generated key completes AES/GCM roundtrip");
        check(Arrays.equals(gcmRoundtrip(k256, plain), plain), "256-bit generated key completes roundtrip");

        // --- invalid key sizes ---
        for (int bad : new int[]{129, 0, -5}) {
            try {
                KeyGenerator.getInstance("AES").init(bad);
                check(false, "init(" + bad + ") must throw InvalidParameterException");
            } catch (InvalidParameterException e) {
                check(true, "init(" + bad + ") -> InvalidParameterException");
            }
        }

        // --- HmacSHA256 keys: default 256-bit key material ---
        KeyGenerator kgHmac = KeyGenerator.getInstance("HmacSHA256");
        SecretKey hmacKey = kgHmac.generateKey();
        check("HmacSHA256".equals(hmacKey.getAlgorithm()), "HmacSHA256 key algorithm");
        check("RAW".equals(hmacKey.getFormat()), "HmacSHA256 key format RAW");
        check(hmacKey.getEncoded().length == 32, "HmacSHA256 default key is 32 bytes (256-bit)");
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(hmacKey);
        check(mac.doFinal(new byte[0]).length == 32, "generated HMAC key usable in Mac");

        // --- init(SecureRandom) / init(int, SecureRandom) ---
        KeyGenerator kgSR = KeyGenerator.getInstance("AES");
        kgSR.init(128, new SecureRandom());
        check(kgSR.generateKey().getEncoded().length == 16, "init(int, SecureRandom) honored");
        KeyGenerator kgSR2 = KeyGenerator.getInstance("AES");
        kgSR2.init(new SecureRandom());
        SecretKey noSize = kgSR2.generateKey();
        check("AES".equals(noSize.getAlgorithm()) && Arrays.equals(gcmRoundtrip(noSize, plain), plain),
                "init(SecureRandom) alone yields a working AES key");

        // --- generateKey() without any init uses the provider default size ---
        SecretKey def = KeyGenerator.getInstance("AES").generateKey();
        int defLen = def.getEncoded().length;
        check(defLen == 16 || defLen == 24 || defLen == 32, "default AES key size is a valid AES size, got " + defLen);

        // --- getInstance variants ---
        Provider sunjce = java.security.Security.getProvider("SunJCE");
        KeyGenerator byName = KeyGenerator.getInstance("AES", "SunJCE");
        check(byName.getProvider().getName().equals("SunJCE"), "getInstance(alg, providerName)");
        KeyGenerator byProv = KeyGenerator.getInstance("AES", sunjce);
        check(byProv.getProvider() == sunjce, "getInstance(alg, Provider) uses given provider");
        check("AES".equals(byProv.getAlgorithm()), "getInstance(alg, Provider) algorithm");
        try { KeyGenerator.getInstance("NoSuchKeyAlgo"); check(false, "bogus algorithm must throw"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "bogus algorithm -> NoSuchAlgorithmException"); }
        try { KeyGenerator.getInstance("AES", "NoSuchProvider"); check(false, "bogus provider must throw"); }
        catch (NoSuchProviderException e) { check(true, "bogus provider -> NoSuchProviderException"); }
        try { KeyGenerator.getInstance(null); check(false, "null algorithm must throw"); }
        catch (NullPointerException e) { check(true, "null algorithm -> NullPointerException"); }

        // --- a SecretKeySpec built from a generated key's encoding behaves identically ---
        SecretKeySpec rebuilt = new SecretKeySpec(k128.getEncoded(), "AES");
        check(Arrays.equals(rebuilt.getEncoded(), k128.getEncoded()), "rebuilt SecretKeySpec matches generated encoding");
        check(Arrays.equals(gcmRoundtrip(rebuilt, plain), plain), "rebuilt key completes roundtrip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
