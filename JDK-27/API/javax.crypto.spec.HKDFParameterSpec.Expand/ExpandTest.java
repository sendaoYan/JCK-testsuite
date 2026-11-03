/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Expand conformance: white-box structure + black-box behavior.
 * @run main ExpandTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class ExpandTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // RFC 5869 Test Case 1 (HKDF-SHA256)
    static final byte[] IKM = filled(0x0b, 22);
    static final byte[] SALT = bytes("000102030405060708090a0b0c");
    static final byte[] INFO = bytes("f0f1f2f3f4f5f6f7f8f9");
    static final String OKM1 = "3cb25f25faacd57a90434f64d0362f2a2d2d0a90cf1a5a4c5db02d56ecc4c5bf"
            + "34007208d5b887185865";
    static final String PRK1 = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.HKDFParameterSpec$Expand");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0].getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "implements HKDFParameterSpec");
        check(c.getEnclosingClass().getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "nested in HKDFParameterSpec");
        checkMethods(c, new String[]{"prk", "info", "length"});
        check(c.getConstructors().length == 0, "no public ctor (created via HKDFParameterSpec.expandOnly)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) accessors
        SecretKeySpec prk = new SecretKeySpec(new byte[32], "Generic");
        byte[] info = new byte[]{1, 2};
        HKDFParameterSpec.Expand ex = HKDFParameterSpec.expandOnly(prk, info, 32);
        check(ex.prk().getEncoded().length == 32, "prk accessor");
        check(Arrays.equals(info, ex.info()), "info accessor");
        check(ex.length() == 32, "length accessor");

        // prk identity, info defensive copy, various lengths
        check(ex.prk() == prk, "prk identity preserved");
        byte[] mutated = ex.info();
        mutated[0] = 99;
        check(ex.info()[0] == 1, "info defensive copy");
        check(ex.info() != ex.info(), "info new array each call");
        check(HKDFParameterSpec.expandOnly(prk, info, 1).length() == 1, "length 1");
        check(HKDFParameterSpec.expandOnly(prk, info, 255 * 32).length() == 8160, "length 255*32");
        check(HKDFParameterSpec.expandOnly(prk, new byte[0], 16).info().length == 0, "empty info kept empty");
        check(ex instanceof HKDFParameterSpec && ex instanceof AlgorithmParameterSpec,
                "instanceof hierarchy");

        // null info allowed and surfaced as null
        check(HKDFParameterSpec.expandOnly(prk, null, 10).info() == null, "null info stays null");

        // null prk -> NPE; length 0/negative -> IllegalArgumentException
        try { HKDFParameterSpec.expandOnly(null, null, 5); check(false, "null prk must throw"); }
        catch (NullPointerException e) { check(true, "null prk NPE"); }
        try { HKDFParameterSpec.expandOnly(prk, null, 0); check(false, "length 0 must throw"); }
        catch (IllegalArgumentException e) { check(true, "length 0 IAE"); }
        try { HKDFParameterSpec.expandOnly(prk, null, -7); check(false, "negative length must throw"); }
        catch (IllegalArgumentException e) { check(true, "negative length IAE"); }

        // strongest chain: RFC 5869 TC1 PRK -> expandOnly -> exact OKM; equals combined extract-then-expand
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        SecretKey prk1 = kdf.deriveKey("PRK", HKDFParameterSpec.ofExtract()
                .addIKM(IKM).addSalt(SALT).extractOnly());
        check(java.util.HexFormat.of().formatHex(prk1.getEncoded()).equals(PRK1), "reference PRK exact");
        byte[] okm = kdf.deriveData(HKDFParameterSpec.expandOnly(prk1, INFO, 42));
        check(java.util.HexFormat.of().formatHex(okm).equals(OKM1), "RFC 5869 TC1 exact OKM");
        byte[] combined = kdf.deriveData(HKDFParameterSpec.ofExtract()
                .addIKM(IKM).addSalt(SALT).thenExpand(INFO, 42));
        check(Arrays.equals(okm, combined), "expandOnly equals extract-then-expand");
        // a PRK shorter than the HMAC output length is rejected by the KDF implementation
        try {
            kdf.deriveData(HKDFParameterSpec.expandOnly(
                    new SecretKeySpec(new byte[16], "Generic"), INFO, 32));
            check(false, "short prk must be rejected");
        } catch (java.security.InvalidAlgorithmParameterException e) {
            check(true, "short prk rejected by KDF");
        }
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
