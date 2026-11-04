/*
 * @test
 * @summary javax.crypto.spec.HPKEParameterSpec conformance: white-box structure + black-box behavior.
 * @run main HPKEParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class HPKEParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = HPKEParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"of", "withInfo", "withPsk", "withEncapsulation", "withAuthKey",
                "kem_id", "kdf_id", "aead_id", "info", "psk", "psk_id", "authKey", "encapsulation", "toString"});
        check(c.getConstructors().length == 0, "no public ctor (static factory of() only)");
        // constants: exact RFC 9180 identifiers
        check(HPKEParameterSpec.KEM_DHKEM_P_256_HKDF_SHA256 == 0x10, "KEM P-256 id");
        check(HPKEParameterSpec.KEM_DHKEM_P_384_HKDF_SHA384 == 0x11, "KEM P-384 id");
        check(HPKEParameterSpec.KEM_DHKEM_P_521_HKDF_SHA512 == 0x12, "KEM P-521 id");
        check(HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256 == 0x20, "KEM X25519 id");
        check(HPKEParameterSpec.KEM_DHKEM_X448_HKDF_SHA512 == 0x21, "KEM X448 id");
        check(HPKEParameterSpec.KDF_HKDF_SHA256 == 0x1 && HPKEParameterSpec.KDF_HKDF_SHA384 == 0x2
                && HPKEParameterSpec.KDF_HKDF_SHA512 == 0x3, "KDF ids");
        check(HPKEParameterSpec.AEAD_AES_128_GCM == 0x1 && HPKEParameterSpec.AEAD_AES_256_GCM == 0x2
                && HPKEParameterSpec.AEAD_CHACHA20_POLY1305 == 0x3, "AEAD ids");
        check(HPKEParameterSpec.EXPORT_ONLY == 0xffff, "EXPORT_ONLY id");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) constants / factory / withInfo basics
        check(HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256 != HPKEParameterSpec.KEM_DHKEM_P_256_HKDF_SHA256,
                "distinct KEM ids");
        HPKEParameterSpec hp = HPKEParameterSpec.of(HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256,
                HPKEParameterSpec.KDF_HKDF_SHA256, HPKEParameterSpec.AEAD_AES_128_GCM);
        check(hp.kem_id() == HPKEParameterSpec.KEM_DHKEM_X25519_HKDF_SHA256, "kem_id");
        check(hp.kdf_id() == HPKEParameterSpec.KDF_HKDF_SHA256, "kdf_id");
        check(hp.aead_id() == HPKEParameterSpec.AEAD_AES_128_GCM, "aead_id");
        HPKEParameterSpec hp2 = hp.withInfo(new byte[]{1, 2});
        check(Arrays.equals(new byte[]{1, 2}, hp2.info()), "withInfo");
        check(hp.toString() != null, "toString non-null");
        check(hp instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");

        // of(): defaults are empty info / no psk / empty psk_id / no encapsulation / no auth key
        check(hp.info().length == 0, "default info empty");
        check(hp.psk() == null, "default psk null");
        check(hp.psk_id().length == 0, "default psk_id empty");
        check(hp.encapsulation() == null, "default encapsulation null");
        check(hp.authKey() == null, "default authKey null");
        // of() boundary values accepted, out-of-range rejected
        check(HPKEParameterSpec.of(0, 0, 0).kem_id() == 0, "of(0,0,0) accepted");
        check(HPKEParameterSpec.of(65535, 65535, 65535).aead_id() == 65535, "of(65535,...) accepted");
        try { HPKEParameterSpec.of(-1, 0, 0); check(false, "kem_id -1 must throw"); }
        catch (IllegalArgumentException e) { check(true, "of kem_id -1 IAE"); }
        try { HPKEParameterSpec.of(0, 65536, 0); check(false, "kdf_id 65536 must throw"); }
        catch (IllegalArgumentException e) { check(true, "of kdf_id 65536 IAE"); }
        try { HPKEParameterSpec.of(0, 0, -2); check(false, "aead_id -2 must throw"); }
        catch (IllegalArgumentException e) { check(true, "of aead_id -2 IAE"); }

        // withInfo: immutable update, defensive copy, validation
        byte[] infoBuf = new byte[]{5, 6};
        HPKEParameterSpec wi = hp.withInfo(infoBuf);
        infoBuf[0] = 99;
        check(wi.info()[0] == 5, "withInfo defensive copy");
        check(hp.info().length == 0, "withInfo leaves original untouched");
        check(wi.kem_id() == hp.kem_id() && wi.kdf_id() == hp.kdf_id() && wi.aead_id() == hp.aead_id(),
                "withInfo preserves ids");
        try { hp.withInfo(null); check(false, "withInfo null must throw"); }
        catch (NullPointerException e) { check(true, "withInfo null NPE"); }
        try { hp.withInfo(new byte[0]); check(false, "withInfo empty must throw"); }
        catch (IllegalArgumentException e) { check(true, "withInfo empty IAE"); }

        // withPsk: validation of psk length and psk_id, defensive copy, identity of psk
        SecretKey pskShort = new SecretKeySpec(new byte[31], "AES");
        SecretKey psk = new SecretKeySpec(new byte[32], "AES");
        byte[] pid = new byte[]{1, 2, 3};
        try { hp.withPsk(pskShort, new byte[]{1}); check(false, "short psk must throw"); }
        catch (IllegalArgumentException e) { check(true, "withPsk short psk IAE"); }
        try { hp.withPsk(null, new byte[]{1}); check(false, "null psk must throw"); }
        catch (NullPointerException e) { check(true, "withPsk null psk NPE"); }
        try { hp.withPsk(psk, null); check(false, "null psk_id must throw"); }
        catch (NullPointerException e) { check(true, "withPsk null psk_id NPE"); }
        try { hp.withPsk(psk, new byte[0]); check(false, "empty psk_id must throw"); }
        catch (IllegalArgumentException e) { check(true, "withPsk empty psk_id IAE"); }
        HPKEParameterSpec wp = hp.withPsk(psk, pid);
        pid[0] = 99;
        check(wp.psk_id()[0] == 1, "withPsk psk_id defensive copy");
        check(wp.psk() == psk, "withPsk psk identity");
        check(wp.info().length == 0 && hp.psk() == null, "withPsk leaves original untouched");

        // withEncapsulation / withAuthKey: NPE + defensive copy / identity
        byte[] encBuf = new byte[32];
        Arrays.fill(encBuf, (byte) 0xAB);
        try { hp.withEncapsulation(null); check(false, "withEncapsulation null must throw"); }
        catch (NullPointerException e) { check(true, "withEncapsulation null NPE"); }
        HPKEParameterSpec we = hp.withEncapsulation(encBuf);
        encBuf[0] = 99;
        check(we.encapsulation()[0] == (byte) 0xAB, "withEncapsulation defensive copy");
        try { hp.withAuthKey(null); check(false, "withAuthKey null must throw"); }
        catch (NullPointerException e) { check(true, "withAuthKey null NPE"); }

        // toString: exact deterministic format for each mode
        check(hp.toString().equals("HPKEParameterSpec{kem_id=32, kdf_id=1, aead_id=1, info=(empty), mode_base}"),
                "toString mode_base exact");
        check(HPKEParameterSpec.of(0x20, 1, 1).withPsk(psk, new byte[]{'i'}).toString().endsWith(", mode_psk}"),
                "toString mode_psk");
        KeyPair authKp = KeyPairGenerator.getInstance("X25519").generateKeyPair();
        check(HPKEParameterSpec.of(0x20, 1, 1).withAuthKey(authKp.getPublic()).toString().endsWith(", mode_auth}"),
                "toString mode_auth");
        check(HPKEParameterSpec.of(0x20, 1, 1).withInfo(new byte[]{'a', 'b'}).toString()
                .contains("info=6162 (\"ab\")"), "toString ascii info");
        check(HPKEParameterSpec.of(0x20, 1, 1).withInfo(new byte[]{0x00}).toString().contains("info=00,"),
                "toString binary info");

        // real chain: full RFC 9180 HPKE exchange driven by HPKEParameterSpec (SunJCE "HPKE" cipher)
        KeyPair receiver = KeyPairGenerator.getInstance("X25519").generateKeyPair();
        byte[] info = new byte[]{1, 2, 3};
        Cipher sender = Cipher.getInstance("HPKE");
        sender.init(Cipher.ENCRYPT_MODE, receiver.getPublic(), hp.withInfo(info));
        byte[] encapsulation = sender.getIV();
        check(encapsulation.length == 32, "X25519 encapsulation 32 bytes");
        byte[] pt = "hi HPKE".getBytes("UTF-8");
        byte[] ct = sender.doFinal(pt);
        check(ct.length == pt.length + 16, "AEAD ciphertext = plaintext + 16B tag");
        Cipher recvr = Cipher.getInstance("HPKE");
        recvr.init(Cipher.DECRYPT_MODE, receiver.getPrivate(),
                hp.withInfo(info).withEncapsulation(encapsulation));
        check(Arrays.equals(recvr.doFinal(ct), pt), "HPKE base mode roundtrip");
        // a receiver using a different info value fails tag verification
        Cipher wrong = Cipher.getInstance("HPKE");
        wrong.init(Cipher.DECRYPT_MODE, receiver.getPrivate(),
                hp.withInfo(new byte[]{9, 9}).withEncapsulation(encapsulation));
        try {
            wrong.doFinal(ct);
            check(false, "wrong info must fail");
        } catch (AEADBadTagException e) { check(true, "wrong info AEADBadTagException"); }
        // psk mode roundtrip
        Cipher pskSnd = Cipher.getInstance("HPKE");
        pskSnd.init(Cipher.ENCRYPT_MODE, receiver.getPublic(), hp.withPsk(psk, new byte[]{'i', 'd'}));
        byte[] pskCt = pskSnd.doFinal(pt);
        Cipher pskRcv = Cipher.getInstance("HPKE");
        pskRcv.init(Cipher.DECRYPT_MODE, receiver.getPrivate(),
                hp.withPsk(psk, new byte[]{'i', 'd'}).withEncapsulation(pskSnd.getIV()));
        check(Arrays.equals(pskRcv.doFinal(pskCt), pt), "HPKE psk mode roundtrip");
        // auth mode roundtrip (sender's private key authenticates, public key verifies)
        KeyPair senderKp = KeyPairGenerator.getInstance("X25519").generateKeyPair();
        Cipher authSnd = Cipher.getInstance("HPKE");
        authSnd.init(Cipher.ENCRYPT_MODE, receiver.getPublic(), hp.withAuthKey(senderKp.getPrivate()));
        byte[] authCt = authSnd.doFinal(pt);
        Cipher authRcv = Cipher.getInstance("HPKE");
        authRcv.init(Cipher.DECRYPT_MODE, receiver.getPrivate(),
                hp.withAuthKey(senderKp.getPublic()).withEncapsulation(authSnd.getIV()));
        check(Arrays.equals(authRcv.doFinal(authCt), pt), "HPKE auth mode roundtrip");
        // export-only AEAD: encapsulation still produced, encryption unsupported
        HPKEParameterSpec export = HPKEParameterSpec.of(0x20, 1, HPKEParameterSpec.EXPORT_ONLY);
        Cipher exp = Cipher.getInstance("HPKE");
        exp.init(Cipher.ENCRYPT_MODE, receiver.getPublic(), export);
        check(exp.getIV().length == 32, "export-only encapsulation 32 bytes");
        try {
            exp.doFinal(new byte[]{1});
            check(false, "export-only doFinal must throw");
        } catch (UnsupportedOperationException e) { check(true, "export-only doFinal UnsupportedOperationException"); }
        // the cipher requires an HPKEParameterSpec at init time
        try {
            Cipher.getInstance("HPKE").init(Cipher.ENCRYPT_MODE, receiver.getPublic());
            check(false, "init without spec must throw");
        } catch (java.security.InvalidKeyException e) { check(true, "init without spec InvalidKeyException"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
}
