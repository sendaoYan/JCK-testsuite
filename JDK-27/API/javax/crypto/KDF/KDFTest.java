/*
 * @test
 * @summary javax.crypto.KDF conformance: white-box structure + black-box behavior.
 * @run main KDFTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.lang.reflect.*;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import java.util.HexFormat;

public class KDFTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // RFC 5869 test case 1 (HKDF-SHA256), vectors verified against the JDK 27 implementation
    static final String TC1_OKM =
        "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf34007208d5b887185865";
    static final String TC1_PRK =
        "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5";
    static final String TC2_OKM =
        "b11e398dc80327a1c8e7f78c596a49344f012eda2d4efad8a050cc4c19afa97c"
        + "59045a99cac7827271cb41c65e590e09da3275600c2f09b8367793a9aca3db71"
        + "cc30c58179ec3e87c14c01d5c1f3434f1d87";
    static final String TC3_OKM =
        "8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d"
        + "9d201395faa4b61a96c8";

    static byte[] tc1Ikm() { byte[] b = new byte[22]; Arrays.fill(b, (byte) 0x0b); return b; }
    static byte[] tc1Salt() { byte[] b = new byte[13]; for (int i = 0; i < 13; i++) b[i] = (byte) i; return b; }
    static byte[] tc1Info() { byte[] b = new byte[10]; for (int i = 0; i < 10; i++) b[i] = (byte) (0xf0 + i); return b; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.KDF");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        boolean publicCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) publicCtor = true;
        check(!publicCtor, "no public constructor (factory only)");
        String[] expect = {"getInstance", "getAlgorithm", "getProviderName",
                           "getParameters", "deriveKey", "deriveData"};
        for (String n : expect) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "method " + n);
        }
        int getInstanceOverloads = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("getInstance")) getInstanceOverloads++;
        check(getInstanceOverloads == 6, "6 getInstance overloads");
        Method dk = c.getDeclaredMethod("deriveKey", String.class, AlgorithmParameterSpec.class);
        boolean nsae = false;
        for (Class<?> t : dk.getExceptionTypes())
            if (t.getName().equals("java.security.NoSuchAlgorithmException")) nsae = true;
        check(nsae, "deriveKey declares NoSuchAlgorithmException");
        check(c.getDeclaredMethod("deriveData", AlgorithmParameterSpec.class)
                .getReturnType() == byte[].class, "deriveData returns byte[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- preserved original scenario: fixed small inputs ---
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        check("HKDF-SHA256".equals(kdf.getAlgorithm()), "getAlgorithm");
        check(kdf.getProviderName() != null, "getProviderName non-null");
        check(kdf.getParameters() == null, "no-arg getInstance has null parameters");
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3, 4, 5}, "Generic");
        HKDFParameterSpec spec = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{9}).thenExpand(new byte[]{7}, 32);
        SecretKey out = kdf.deriveKey("AES", spec);
        check("AES".equals(out.getAlgorithm()), "deriveKey algorithm");
        check(out.getEncoded().length == 32, "deriveKey length 32");
        check(kdf.deriveData(spec).length == 32, "deriveData length 32");
        check(Arrays.equals(kdf.deriveKey("AES", spec).getEncoded(), kdf.deriveData(spec)),
                "deriveKey encoded == deriveData");

        // --- RFC 5869 TC1: extract-then-expand, exact OKM ---
        HKDFParameterSpec tc1 = HKDFParameterSpec.ofExtract()
            .addIKM(tc1Ikm()).addSalt(tc1Salt()).thenExpand(tc1Info(), 42);
        byte[] okm = kdf.deriveData(tc1);
        check(TC1_OKM.equals(HexFormat.of().formatHex(okm)), "RFC 5869 TC1 exact OKM");
        check(Arrays.equals(kdf.deriveData(tc1), okm), "derivation deterministic");

        // extract-only returns the exact RFC PRK; expand-only from it reproduces TC1 OKM
        SecretKey prk = KDF.getInstance("HKDF-SHA256").deriveKey("Generic",
            HKDFParameterSpec.ofExtract().addIKM(tc1Ikm()).addSalt(tc1Salt()).extractOnly());
        check(TC1_PRK.equals(HexFormat.of().formatHex(prk.getEncoded()))
                && prk.getEncoded().length == 32, "RFC 5869 TC1 exact PRK");
        byte[] expanded = KDF.getInstance("HKDF-SHA256").deriveData(
            HKDFParameterSpec.expandOnly(prk, tc1Info(), 42));
        check(TC1_OKM.equals(HexFormat.of().formatHex(expanded)), "expand-only exact OKM");
        check(Arrays.equals(expanded, okm), "split extract/expand == combined");

        // deriveKey with the TC1 spec yields byte-identical material and honors the algorithm name
        SecretKey aes42 = kdf.deriveKey("AES", tc1);
        check("AES".equals(aes42.getAlgorithm())
                && Arrays.equals(aes42.getEncoded(), okm), "deriveKey(TC1) == deriveData(TC1)");

        // RFC 5869 TC2 (longer inputs) and TC3 (empty salt and info) exact vectors
        byte[] ikm2 = new byte[80]; for (int i = 0; i < 80; i++) ikm2[i] = (byte) i;
        byte[] salt2 = new byte[80]; for (int i = 0; i < 80; i++) salt2[i] = (byte) (0x60 + i);
        byte[] info2 = new byte[80]; for (int i = 0; i < 80; i++) info2[i] = (byte) (0xb0 + i);
        check(TC2_OKM.equals(HexFormat.of().formatHex(KDF.getInstance("HKDF-SHA256").deriveData(
            HKDFParameterSpec.ofExtract().addIKM(ikm2).addSalt(salt2).thenExpand(info2, 82)))),
            "RFC 5869 TC2 exact OKM");
        check(TC3_OKM.equals(HexFormat.of().formatHex(KDF.getInstance("HKDF-SHA256").deriveData(
            HKDFParameterSpec.ofExtract().addIKM(tc1Ikm()).addSalt(new byte[0])
                .thenExpand(new byte[0], 42)))),
            "RFC 5869 TC3 exact OKM (empty salt+info)");

        // provider selection and configuration overloads
        KDF byName = KDF.getInstance("HKDF-SHA256", "SunJCE");
        KDF byProv = KDF.getInstance("HKDF-SHA256", Security.getProvider("SunJCE"));
        KDF nullParams = KDF.getInstance("HKDF-SHA256", (KDFParameters) null);
        check("HKDF-SHA256".equals(byName.getAlgorithm())
                && "HKDF-SHA256".equals(byProv.getAlgorithm())
                && "HKDF-SHA256".equals(nullParams.getAlgorithm()), "provider variants algorithm");
        check(nullParams.getParameters() == null, "null KDFParameters keeps params null");
        check(Arrays.equals(byName.deriveData(tc1), okm) && Arrays.equals(byProv.deriveData(tc1), okm),
                "all overloads derive identical OKM");
        check("SunJCE".equals(kdf.getProviderName()), "getProviderName SunJCE");

        // error paths
        try {
            KDF.getInstance("NoSuchKDF-xyz");
            check(false, "unknown algorithm must fail");
        } catch (NoSuchAlgorithmException ex) { check(true, "unknown -> NoSuchAlgorithmException"); }
        try {
            KDF.getInstance("HKDF-SHA256", "NoSuchProvider-xyz");
            check(false, "unknown provider must fail");
        } catch (NoSuchProviderException ex) { check(true, "unknown -> NoSuchProviderException"); }
        try {
            kdf.deriveKey("", spec);
            check(false, "empty algorithm must fail");
        } catch (NoSuchAlgorithmException ex) { check(true, "empty alg -> NoSuchAlgorithmException"); }
        try {
            kdf.deriveKey(null, spec);
            check(false, "null algorithm must fail");
        } catch (NullPointerException ex) { check(true, "null alg -> NullPointerException"); }
        try {
            kdf.deriveData(null);
            check(false, "null spec must fail");
        } catch (NullPointerException ex) { check(true, "null spec -> NullPointerException"); }
        try {
            KDF.getInstance("HKDF-SHA256").deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(tc1Ikm()).addSalt(tc1Salt()).thenExpand(tc1Info(), 0));
            check(false, "zero length must fail");
        } catch (IllegalArgumentException ex) { check(true, "length 0 -> IllegalArgumentException"); }
        try {
            KDF.getInstance("HKDF-SHA256").deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(tc1Ikm()).addSalt(tc1Salt()).thenExpand(tc1Info(), 255 * 32 + 1));
            check(false, "over-long output must fail");
        } catch (InvalidAlgorithmParameterException ex) {
            check(true, "length > 255*HashLen -> InvalidAlgorithmParameterException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
