/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.ExtractThenExpand conformance: white-box structure + black-box behavior.
 * @run main ExtractThenExpandTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class ExtractThenExpandTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // RFC 5869 Test Case 1 (HKDF-SHA256)
    static final byte[] IKM = filled(0x0b, 22);
    static final byte[] SALT = bytes("000102030405060708090a0b0c");
    static final byte[] INFO = bytes("f0f1f2f3f4f5f6f7f8f9");
    static final String OKM1 = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf"
            + "34007208d5b887185865";
    // RFC 5869 Test Case 2 (HKDF-SHA256, long inputs)
    static final byte[] IKM2 = bytes(
        "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f" +
        "202122232425262728292a2b2c2d2e2f303132333435363738393a3b3c3d3e3f" +
        "404142434445464748494a4b4c4d4e4f");
    static final byte[] SALT2 = bytes(
        "606162636465666768696a6b6c6d6e6f707172737475767778797a7b7c7d7e7f" +
        "808182838485868788898a8b8c8d8e8f909192939495969798999a9b9c9d9e9f" +
        "a0a1a2a3a4a5a6a7a8a9aaabacadaeaf");
    static final byte[] INFO2 = bytes(
        "b0b1b2b3b4b5b6b7b8b9babbbcbdbebfc0c1c2c3c4c5c6c7c8c9cacbcccdcec" +
        "fd0d1d2d3d4d5d6d7d8d9dadbdcdddedfe0e1e2e3e4e5e6e7e8e9eaebecedee" +
        "eff0f1f2f3f4f5f6f7f8f9fafbfcfdfeff");
    static final String OKM2 = "b11e398dc80327a1c8e7f78c596a49344f012eda2d4efad8a050cc4c19afa97c"
            + "59045a99cac7827271cb41c65e590e09da3275600c2f09b8367793a9aca3db71"
            + "cc30c58179ec3e87c14c01d5c1f3434f1d87";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.HKDFParameterSpec$ExtractThenExpand");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0].getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "implements HKDFParameterSpec");
        check(c.getEnclosingClass().getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "nested in HKDFParameterSpec");
        checkMethods(c, new String[]{"ikms", "salts", "info", "length"});
        check(c.getConstructors().length == 0, "no public ctor (created via Builder.thenExpand)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) accessors
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        byte[] info = new byte[]{7};
        HKDFParameterSpec.ExtractThenExpand ete = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{4}).thenExpand(info, 16);
        check(ete.ikms().size() == 1, "ikms size");
        check(ete.salts().size() == 1, "salts size");
        check(Arrays.equals(info, ete.info()), "info accessor");
        check(ete.length() == 16, "length accessor");

        // ikms/salts mirror the builder state; lists are unmodifiable
        HKDFParameterSpec.ExtractThenExpand multi = HKDFParameterSpec.ofExtract()
                .addIKM(new byte[]{1}).addIKM(new byte[]{2}).addSalt(new byte[]{3})
                .thenExpand(new byte[]{9}, 32);
        check(Arrays.equals(multi.ikms().get(0).getEncoded(), new byte[]{1}), "ikms order [0]");
        check(Arrays.equals(multi.ikms().get(1).getEncoded(), new byte[]{2}), "ikms order [1]");
        check(Arrays.equals(multi.salts().get(0).getEncoded(), new byte[]{3}), "salts order [0]");
        try {
            multi.ikms().add(new SecretKeySpec(new byte[]{9}, "Generic"));
            check(false, "ikms must be unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "ikms unmodifiable"); }
        try {
            multi.salts().add(new SecretKeySpec(new byte[]{9}, "Generic"));
            check(false, "salts must be unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "salts unmodifiable"); }

        // info defensive copy; null info allowed; length honored
        byte[] mut = info.clone();
        HKDFParameterSpec.ExtractThenExpand def = HKDFParameterSpec.ofExtract().addIKM(ikm).thenExpand(mut, 8);
        mut[0] = 99;
        check(def.info()[0] == 7, "info defensive copy");
        check(def.info() != def.info(), "info new array each call");
        check(HKDFParameterSpec.ofExtract().thenExpand(null, 1).info() == null, "null info stays null");
        check(HKDFParameterSpec.ofExtract().thenExpand(new byte[0], 255 * 32).length() == 8160,
                "max length 255*32");
        check(ete instanceof HKDFParameterSpec && ete instanceof AlgorithmParameterSpec,
                "instanceof hierarchy");

        // length validation
        try { HKDFParameterSpec.ofExtract().thenExpand(info, 0); check(false, "length 0 must throw"); }
        catch (IllegalArgumentException e) { check(true, "length 0 IAE"); }
        try { HKDFParameterSpec.ofExtract().thenExpand(info, -3); check(false, "negative length must throw"); }
        catch (IllegalArgumentException e) { check(true, "negative length IAE"); }

        // strongest chain: RFC 5869 TC1 and TC2 exact OKM through KDF("HKDF-SHA256")
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        byte[] okm1 = kdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(IKM).addSalt(SALT).thenExpand(INFO, 42));
        check(java.util.HexFormat.of().formatHex(okm1).equals(OKM1), "RFC 5869 TC1 exact OKM");
        check(okm1.length == 42, "TC1 OKM length");
        byte[] okm2 = kdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(IKM2).addSalt(SALT2).thenExpand(INFO2, 82));
        check(java.util.HexFormat.of().formatHex(okm2).equals(OKM2), "RFC 5869 TC2 exact OKM");
        check(okm2.length == 82, "TC2 OKM length");
        // extract-then-expand equals separated extract + expandOnly
        SecretKey prk = kdf.deriveKey("PRK", HKDFParameterSpec.ofExtract()
                .addIKM(IKM).addSalt(SALT).extractOnly());
        byte[] okmSeparated = kdf.deriveData(HKDFParameterSpec.expandOnly(prk, INFO, 42));
        check(Arrays.equals(okm1, okmSeparated), "combined equals separated");
        // split adds equal single adds (concatenation semantics)
        byte[] okmSplit = kdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(Arrays.copyOfRange(IKM, 0, 11)).addIKM(Arrays.copyOfRange(IKM, 11, 22))
                .addSalt(Arrays.copyOfRange(SALT, 0, 5)).addSalt(Arrays.copyOfRange(SALT, 5, 13))
                .thenExpand(INFO, 42));
        check(Arrays.equals(okm1, okmSplit), "split adds equal single adds");
        // deriveKey through the combined spec
        SecretKey aes = kdf.deriveKey("AES", HKDFParameterSpec.ofExtract()
                .addIKM(IKM).addSalt(SALT).thenExpand(INFO, 32));
        check(aes.getAlgorithm().equals("AES") && aes.getEncoded().length == 32,
                "deriveKey AES 32 bytes");
    }

    static byte[] bytes(String hex) { return java.util.HexFormat.of().parseHex(hex); }
    static byte[] filled(int b, int len) { byte[] r = new byte[len]; Arrays.fill(r, (byte) b); return r; }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
}
