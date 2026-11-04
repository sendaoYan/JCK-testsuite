/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec.Extract conformance: white-box structure + black-box behavior.
 * @run main ExtractTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class ExtractTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // RFC 5869 Test Case 1 (HKDF-SHA256)
    static final byte[] IKM = filled(0x0b, 22);
    static final byte[] SALT = bytes("000102030405060708090a0b0c");
    static final String PRK1 = "077709362c2e32df0ddc3f0dc47bba6390b6c73bb50f9c3122ec844ad7c2b3e5";
    // RFC 5869 Test Case 3 (empty salt, no info)
    static final String OKM3 = "8da4e775a563c18f715f802a063c5a31b8a11f5c5ee1879ec3454e5f3c738d2d"
            + "9d201395faa4b61a96c8";

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.HKDFParameterSpec$Extract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getInterfaces().length == 1
                && c.getInterfaces()[0].getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "implements HKDFParameterSpec");
        check(c.getEnclosingClass().getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                "nested in HKDFParameterSpec");
        checkMethods(c, new String[]{"ikms", "salts"});
        check(c.getConstructors().length == 0, "no public ctor (created via Builder.extractOnly)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) accessors
        SecretKeySpec ikm = new SecretKeySpec(new byte[]{1, 2, 3}, "Generic");
        HKDFParameterSpec.Extract ex = HKDFParameterSpec.ofExtract().addIKM(ikm).addSalt(new byte[]{4}).extractOnly();
        check(ex.ikms().size() == 1, "ikms size");
        check(ex.salts().size() == 1, "salts size");

        // empty builder -> empty lists
        HKDFParameterSpec.Extract empty = HKDFParameterSpec.ofExtract().extractOnly();
        check(empty.ikms().isEmpty(), "empty ikms");
        check(empty.salts().isEmpty(), "empty salts");

        // ordering and content of repeated adds
        HKDFParameterSpec.Extract multi = HKDFParameterSpec.ofExtract()
                .addIKM(new byte[]{1}).addIKM(new byte[]{2}).addSalt(new byte[]{3}).addSalt(new byte[]{4})
                .extractOnly();
        check(Arrays.equals(multi.ikms().get(0).getEncoded(), new byte[]{1}), "ikms order [0]");
        check(Arrays.equals(multi.ikms().get(1).getEncoded(), new byte[]{2}), "ikms order [1]");
        check(Arrays.equals(multi.salts().get(0).getEncoded(), new byte[]{3}), "salts order [0]");
        check(Arrays.equals(multi.salts().get(1).getEncoded(), new byte[]{4}), "salts order [1]");
        check(multi.ikms().get(0).getAlgorithm().equals("Generic"), "byte ikm converted to SecretKeySpec");

        // SecretKey identity preserved
        SecretKeySpec user = new SecretKeySpec(new byte[]{5}, "Generic");
        HKDFParameterSpec.Extract id = HKDFParameterSpec.ofExtract().addIKM(user).addSalt(user).extractOnly();
        check(id.ikms().get(0) == user && id.salts().get(0) == user, "SecretKey identity");

        // returned lists are unmodifiable
        try {
            ex.ikms().add(new SecretKeySpec(new byte[]{9}, "Generic"));
            check(false, "ikms must be unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "ikms unmodifiable"); }
        try {
            ex.salts().add(new SecretKeySpec(new byte[]{9}, "Generic"));
            check(false, "salts must be unmodifiable");
        } catch (UnsupportedOperationException e) { check(true, "salts unmodifiable"); }
        check(ex instanceof HKDFParameterSpec && ex instanceof AlgorithmParameterSpec,
                "instanceof hierarchy");

        // strongest chain: Extract spec drives KDF to the exact RFC 5869 TC1 PRK
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        HKDFParameterSpec.Extract tc1 =
                HKDFParameterSpec.ofExtract().addIKM(IKM).addSalt(SALT).extractOnly();
        SecretKey prk = kdf.deriveKey("PRK", tc1);
        check(java.util.HexFormat.of().formatHex(prk.getEncoded()).equals(PRK1),
                "RFC 5869 TC1 exact PRK via deriveKey");
        check(prk.getEncoded().length == 32, "PRK length 32");
        byte[] prkData = kdf.deriveData(tc1);
        check(prkData.length == 32 && Arrays.equals(prkData, prk.getEncoded()),
                "deriveData equals deriveKey for extract");
        // salt supplied as SecretKey equals salt supplied as bytes; missing salt equals empty salt
        SecretKey prkKeySalt = kdf.deriveKey("PRK", HKDFParameterSpec.ofExtract().addIKM(IKM)
                .addSalt(new SecretKeySpec(SALT, "Generic")).extractOnly());
        check(Arrays.equals(prkKeySalt.getEncoded(), prk.getEncoded()), "SecretKey salt equals byte salt");
        SecretKey noSalt = kdf.deriveKey("PRK", HKDFParameterSpec.ofExtract().addIKM(IKM).extractOnly());
        SecretKey emptySalt = kdf.deriveKey("PRK", HKDFParameterSpec.ofExtract().addIKM(IKM)
                .addSalt(new byte[0]).extractOnly());
        check(Arrays.equals(noSalt.getEncoded(), emptySalt.getEncoded()), "no salt equals empty salt");
        // RFC 5869 TC3: empty salt (no addSalt) + expand without info
        byte[] okm3 = kdf.deriveData(HKDFParameterSpec.ofExtract().addIKM(IKM).thenExpand(null, 42));
        check(java.util.HexFormat.of().formatHex(okm3).equals(OKM3), "RFC 5869 TC3 exact OKM");
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
