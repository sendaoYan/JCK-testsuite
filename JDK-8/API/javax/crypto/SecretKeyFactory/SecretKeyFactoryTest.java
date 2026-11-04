/*
 * @test
 * @summary javax.crypto.SecretKeyFactory conformance: white-box structure + black-box behavior.
 * @run main SecretKeyFactoryTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeyFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.SecretKeyFactory");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 3
                    && k.getParameterTypes()[0].getName().equals("javax.crypto.SecretKeyFactorySpi")
                    && k.getParameterTypes()[1].getName().equals("java.security.Provider")) protCtor = true;
        }
        check(protCtor, "protected SecretKeyFactory(SecretKeyFactorySpi, Provider, String) ctor");
        String[] methods = {"getInstance", "getProvider", "getAlgorithm",
                "generateSecret", "getKeySpec", "translateKey"};
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
        Method gen = c.getDeclaredMethod("generateSecret", java.security.spec.KeySpec.class);
        check(gen.getReturnType().getName().equals("javax.crypto.SecretKey"), "generateSecret returns SecretKey");
        Method spec = c.getDeclaredMethod("getKeySpec", SecretKey.class, Class.class);
        check(java.security.spec.KeySpec.class.isAssignableFrom(spec.getReturnType()),
                "getKeySpec returns a KeySpec");
        Method tr = c.getDeclaredMethod("translateKey", SecretKey.class);
        check(tr.getReturnType().getName().equals("javax.crypto.SecretKey"), "translateKey returns SecretKey");
    }

    // ---- Part 2: black-box (behavioral) ----

    // 24 DES-odd-parity bytes: DESedeKeySpec keeps such a key byte-stable end to end
    static byte[] oddParityDesedeKey() {
        byte[] k = new byte[24];
        for (int i = 0; i < 24; i++) {
            int v = i + 1;
            if (Integer.bitCount(v & 0xFF) % 2 == 0) v |= 0x80;   // force odd DES parity
            k[i] = (byte) v;
        }
        return k;
    }

    // RFC 2898 PBKDF2 built only from javax.crypto.Mac, to cross-check the factory output
    static byte[] pbkdf2HmacSha256(byte[] password, byte[] salt, int iterations, int dkLen)
            throws Exception {
        Mac proto = Mac.getInstance("HmacSHA256");
        proto.init(new SecretKeySpec(password, "HmacSHA256"));
        int hLen = proto.getMacLength();
        byte[] out = new byte[dkLen];
        int pos = 0, block = 1;
        while (pos < dkLen) {
            Mac m = (Mac) proto.clone();
            m.update(salt);
            m.update((byte) (block >>> 24)); m.update((byte) (block >>> 16));
            m.update((byte) (block >>> 8)); m.update((byte) block);
            byte[] u = m.doFinal();
            byte[] t = u.clone();
            for (int c = 1; c < iterations; c++) {
                Mac inner = (Mac) proto.clone();
                u = inner.doFinal(u);
                for (int j = 0; j < hLen; j++) t[j] ^= u[j];
            }
            int n = Math.min(hLen, dkLen - pos);
            System.arraycopy(t, 0, out, pos, n);
            pos += n; block++;
        }
        return out;
    }

    static void testBlackBox() throws Exception {
        // ============ "DESede": spec -> key -> spec round trip ============
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
        check("DESede".equals(skf.getAlgorithm()), "getAlgorithm DESede");
        check(skf.getProvider() != null, "getProvider non-null");
        byte[] keyBytes = oddParityDesedeKey();
        SecretKey desedeKey = skf.generateSecret(new DESedeKeySpec(keyBytes));
        check("DESede".equals(desedeKey.getAlgorithm()), "generated key algorithm DESede");
        check("RAW".equals(desedeKey.getFormat()), "generated key format RAW");
        check(desedeKey.getEncoded().length == 24, "generated key is 24 bytes");
        check(Arrays.equals(desedeKey.getEncoded(), keyBytes),
                "odd-parity key material survives generation unchanged");

        DESedeKeySpec backSpec = (DESedeKeySpec) skf.getKeySpec(desedeKey, DESedeKeySpec.class);
        check(Arrays.equals(backSpec.getKey(), keyBytes), "getKeySpec(DESedeKeySpec) round trips key bytes");

        SecretKey translated = skf.translateKey(new SecretKeySpec(keyBytes, "DESede"));
        check("DESede".equals(translated.getAlgorithm()), "translateKey preserves algorithm");
        check(Arrays.equals(translated.getEncoded(), keyBytes), "translateKey preserves key material");
        // the translated key is a real provider key usable by a Cipher
        Cipher enc = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        enc.init(Cipher.ENCRYPT_MODE, translated, new IvParameterSpec(new byte[8]));
        byte[] pt = "desede roundtrip".getBytes(StandardCharsets.UTF_8);
        byte[] ct = enc.doFinal(pt);
        check(ct.length == 24, "17-byte DESede/CBC plaintext pads to 24 bytes");
        Cipher dec = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        dec.init(Cipher.DECRYPT_MODE, skf.generateSecret(new DESedeKeySpec(keyBytes)),
                new IvParameterSpec(new byte[8]));
        check(Arrays.equals(dec.doFinal(ct), pt), "factory keys decrypt what translated key encrypted");
        // translating a factory-made key returns an equivalent key
        SecretKey retranslated = skf.translateKey(desedeKey);
        check(Arrays.equals(retranslated.getEncoded(), desedeKey.getEncoded()),
                "translateKey of provider key is idempotent");

        // ============ "PBEWithHmacSHA256AndAES_128" ============
        char[] pass = "topsecret".toCharArray();
        byte[] salt = new byte[16];
        for (int i = 0; i < 16; i++) salt[i] = (byte) (0xA0 + i);
        SecretKeyFactory pbe = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        check("PBEWithHmacSHA256AndAES_128".equals(pbe.getAlgorithm()), "PBE factory algorithm");
        SecretKey pbeKey = pbe.generateSecret(new PBEKeySpec(pass, salt, 10000));
        check("PBEWithHmacSHA256AndAES_128".equals(pbeKey.getAlgorithm()), "PBE key algorithm");
        check("RAW".equals(pbeKey.getFormat()), "PBE key format RAW (password carrier)");
        check(Arrays.equals(pbeKey.getEncoded(), "topsecret".getBytes(StandardCharsets.UTF_8)),
                "SunJCE PBE key encodes the password bytes");
        SecretKey pbeKey2 = pbe.generateSecret(new PBEKeySpec(pass, salt, 10000));
        check(Arrays.equals(pbeKey.getEncoded(), pbeKey2.getEncoded()),
                "same PBEKeySpec input derives an equal key (deterministic)");
        // the derived key drives the matching PBE cipher end to end (AES-CBC inside PBES2)
        Cipher pbeEnc = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        pbeEnc.init(Cipher.ENCRYPT_MODE, pbeKey, new PBEParameterSpec(salt, 10000));
        byte[] pbePt = "pbe roundtrip".getBytes(StandardCharsets.UTF_8);
        byte[] pbeCt = pbeEnc.doFinal(pbePt);
        check(pbeCt.length == 16, "PBE AES-128/CBC ciphertext padded to block size");
        byte[] iv = pbeEnc.getIV();
        check(iv != null && iv.length == 16, "PBE cipher exposes the 16-byte AES IV");
        Cipher pbeDec = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        pbeDec.init(Cipher.DECRYPT_MODE, pbe.generateSecret(new PBEKeySpec(pass, salt, 10000)),
                new PBEParameterSpec(salt, 10000, new IvParameterSpec(iv)));
        check(Arrays.equals(pbeDec.doFinal(pbeCt), pbePt), "PBE key decrypts its own ciphertext");
        // a key from a wrong password cannot decrypt
        SecretKey wrongKey = pbe.generateSecret(new PBEKeySpec("wrongpass".toCharArray(), salt, 10000));
        Cipher badDec = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        badDec.init(Cipher.DECRYPT_MODE, wrongKey,
                new PBEParameterSpec(salt, 10000, new IvParameterSpec(iv)));
        try {
            badDec.doFinal(pbeCt);
            check(false, "wrong password must fail authentication");
        } catch (javax.crypto.BadPaddingException e) {
            check(true, "wrong password -> BadPaddingException");
        }

        // ============ "PBKDF2WithHmacSHA256": known-answer + determinism ============
        SecretKeyFactory pbkdf2 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        byte[] derived = pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4096, 256)).getEncoded();
        check(derived.length == 32, "dkLen 256 derives 32 bytes");
        check(Arrays.equals(derived, pbkdf2HmacSha256(
                "topsecret".getBytes(StandardCharsets.UTF_8), salt, 4096, 32)),
                "factory output equals RFC 2898 PBKDF2 built from Mac HmacSHA256");
        check(Arrays.equals(derived,
                pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4096, 256)).getEncoded()),
                "PBKDF2 derivation is deterministic");
        check(pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4096, 128)).getEncoded().length == 16,
                "dkLen 128 derives 16 bytes");
        // different salt or iteration count must change the derived key
        byte[] otherIter = pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4097, 256)).getEncoded();
        check(!Arrays.equals(derived, otherIter), "iteration count affects derivation");
        // RFC 6070 anchor for the PBKDF2 machinery (SHA-1 PRF, vector cited by JDK SecKeyFacSunJCEPrf.java)
        SecretKeyFactory sha1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        byte[] v = sha1.generateSecret(new PBEKeySpec("password".toCharArray(),
                "16-byte salt val".getBytes(StandardCharsets.US_ASCII), 4096, 160)).getEncoded();
        check(toHex(v).equals("d2cacd3f1d44af293c704f0b1005338d903c688c"),
                "RFC 6070 PBKDF2-HMAC-SHA1 known answer");
        // getKeySpec on a PBKDF2 key returns a usable PBEKeySpec
        SecretKey pbk = pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4096, 256));
        PBEKeySpec backPbk = (PBEKeySpec) pbkdf2.getKeySpec(pbk, PBEKeySpec.class);
        check(backPbk.getIterationCount() == 4096 && backPbk.getKeyLength() == 256,
                "getKeySpec(PBEKeySpec) reports iteration count and key length");
        // derived key material is directly usable as an AES key
        Cipher aesEnc = Cipher.getInstance("AES/GCM/NoPadding");
        aesEnc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(derived, "AES"),
                new GCMParameterSpec(128, salt));
        byte[] aesCt = aesEnc.doFinal("pbkdf2 key".getBytes(StandardCharsets.UTF_8));
        Cipher aesDec = Cipher.getInstance("AES/GCM/NoPadding");
        aesDec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(
                pbkdf2.generateSecret(new PBEKeySpec(pass, salt, 4096, 256)).getEncoded(), "AES"),
                new GCMParameterSpec(128, salt));
        check(Arrays.equals(aesDec.doFinal(aesCt), "pbkdf2 key".getBytes(StandardCharsets.UTF_8)),
                "PBKDF2-derived bytes work as an AES/GCM key");

        // ============ unavailable algorithms / invalid inputs ============
        for (String algo : new String[]{"AES", "ChaCha20"}) {
            try {
                SecretKeyFactory.getInstance(algo);
                check(false, algo + " has no SunJCE SecretKeyFactory");
            } catch (java.security.NoSuchAlgorithmException e) {
                check(true, "getInstance(\"" + algo + "\") -> NoSuchAlgorithmException");
            }
        }
        try {
            skf.generateSecret(new SecretKeySpec(new byte[16], "AES"));   // wrong spec type
            check(false, "DESede factory must reject foreign KeySpec");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(true, "generateSecret(wrong spec) -> InvalidKeySpecException");
        }
        try {
            new DESedeKeySpec(new byte[8]);                                // too short for DESede
            check(false, "DESedeKeySpec must reject short key material");
        } catch (java.security.InvalidKeyException e) {
            check(true, "DESedeKeySpec(short key) -> InvalidKeyException");
        }
        try {
            pbkdf2.getKeySpec(new SecretKeySpec(new byte[32], "PBKDF2WithHmacSHA256"), PBEKeySpec.class);
            check(false, "PBKDF2 factory must reject non-PBE keys in getKeySpec");
        } catch (java.security.spec.InvalidKeySpecException e) {
            check(true, "getKeySpec(non-PBE key) -> InvalidKeySpecException");
        }

        // ============ "DES" (assertions retained from the prior test) ============
        SecretKeyFactory desFactory = SecretKeyFactory.getInstance("DES");
        check("DES".equals(desFactory.getAlgorithm()), "legacy: getAlgorithm DES");
        check(desFactory.getProvider() != null, "legacy: getProvider non-null");
        SecretKeySpec desRaw = new SecretKeySpec(new byte[8], "DES");
        SecretKey desTranslated = desFactory.translateKey(desRaw);
        check("DES".equals(desTranslated.getAlgorithm()), "legacy: translateKey DES algorithm");
        SecretKey desGenerated = desFactory.generateSecret(new DESKeySpec(new byte[8]));
        check(desGenerated.getEncoded().length == 8, "legacy: generateSecret 8-byte DES key");
        check(desFactory.getKeySpec(desTranslated, DESKeySpec.class) != null,
                "legacy: getKeySpec(DESKeySpec)");

        // ============ getInstance variants ============
        Provider sunjce = Security.getProvider("SunJCE");
        SecretKeyFactory byName = SecretKeyFactory.getInstance("DESede", "SunJCE");
        check(byName.getProvider().getName().equals("SunJCE"), "getInstance(alg, providerName)");
        SecretKeyFactory byProv = SecretKeyFactory.getInstance("DESede", sunjce);
        check(byProv.getProvider() == sunjce, "getInstance(alg, Provider) uses given provider");
        check("DESede".equals(byProv.getAlgorithm()), "getInstance(alg, Provider) algorithm");
        try { SecretKeyFactory.getInstance("NoSuchAlgo"); check(false, "bogus algorithm must throw"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "bogus algorithm -> NoSuchAlgorithmException"); }
        try { SecretKeyFactory.getInstance("DESede", "NoSuchProvider"); check(false, "bogus provider must throw"); }
        catch (java.security.NoSuchProviderException e) { check(true, "bogus provider -> NoSuchProviderException"); }
    }

    static String toHex(byte[] b) {
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
