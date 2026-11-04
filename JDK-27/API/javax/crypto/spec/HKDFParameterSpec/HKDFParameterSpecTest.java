/*
 * @test
 * @summary javax.crypto.spec.HKDFParameterSpec conformance: white-box structure + black-box behavior.
 * @run main HKDFParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.KDF;
import javax.crypto.SecretKey;
import javax.crypto.spec.*;

public class HKDFParameterSpecTest {
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
        Class<?> c = Class.forName("javax.crypto.spec.HKDFParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"ofExtract", "expandOnly"});
        // nested types: final classes Builder/Extract/Expand/ExtractThenExpand
        Class<?> builder = Class.forName("javax.crypto.spec.HKDFParameterSpec$Builder");
        check(Modifier.isPublic(builder.getModifiers()) && Modifier.isFinal(builder.getModifiers()),
                "Builder public final");
        check(builder.getEnclosingClass() == c, "Builder nested in HKDFParameterSpec");
        for (String n : new String[]{"Extract", "Expand", "ExtractThenExpand"}) {
            Class<?> nested = Class.forName("javax.crypto.spec.HKDFParameterSpec$" + n);
            check(Modifier.isPublic(nested.getModifiers()) && Modifier.isFinal(nested.getModifiers()),
                    n + " public final");
            check(nested.getInterfaces().length == 1
                    && nested.getInterfaces()[0].getName().equals("javax.crypto.spec.HKDFParameterSpec"),
                    n + " implements HKDFParameterSpec");
        }
        check(Class.forName("javax.crypto.spec.HKDFParameterSpec$Extract").getDeclaredConstructors().length == 0
                || Modifier.isPrivate(Class.forName("javax.crypto.spec.HKDFParameterSpec$Extract")
                        .getDeclaredConstructors()[0].getModifiers()), "Extract private ctor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // every implementation of the interface is an AlgorithmParameterSpec
        HKDFParameterSpec.Extract ext = HKDFParameterSpec.ofExtract().addIKM(IKM).addSalt(SALT).extractOnly();
        HKDFParameterSpec.ExtractThenExpand ete =
                HKDFParameterSpec.ofExtract().addIKM(IKM).addSalt(SALT).thenExpand(INFO, 42);
        HKDFParameterSpec.Expand exp = HKDFParameterSpec.expandOnly(
                new SecretKeySpec(bytes(PRK1), "Generic"), INFO, 42);
        for (HKDFParameterSpec spec : new HKDFParameterSpec[]{ext, ete, exp}) {
            check(spec instanceof HKDFParameterSpec, "instanceof HKDFParameterSpec");
            check(spec instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");
        }

        // ofExtract() returns a fresh, independent Builder each call
        HKDFParameterSpec.Builder b1 = HKDFParameterSpec.ofExtract();
        HKDFParameterSpec.Builder b2 = HKDFParameterSpec.ofExtract();
        check(b1 != b2, "ofExtract new builder each call");
        b1.addIKM(IKM);
        check(b2.extractOnly().ikms().isEmpty(), "builders independent");

        // strongest assertion: interface-produced specs drive KDF("HKDF-SHA256")
        // to the exact RFC 5869 Test Case 1 OKM / PRK
        KDF kdf = KDF.getInstance("HKDF-SHA256");
        byte[] okm = kdf.deriveData(ete);
        check(java.util.HexFormat.of().formatHex(okm).equals(OKM1), "RFC 5869 TC1 exact OKM");
        check(okm.length == 42, "OKM length 42");
        SecretKey prk = kdf.deriveKey("PRK", ext);
        check(java.util.HexFormat.of().formatHex(prk.getEncoded()).equals(PRK1), "RFC 5869 TC1 exact PRK");
        // expandOnly built from that PRK reproduces the same OKM
        byte[] okm2 = kdf.deriveData(HKDFParameterSpec.expandOnly(prk, INFO, 42));
        check(Arrays.equals(okm, okm2), "extract+expand equals extract-then-expand");
        // deterministic across repeated derivations
        check(Arrays.equals(okm, kdf.deriveData(ete)), "derivation deterministic");

        // deriveKey through the interface: algorithm name and length honored
        SecretKey aes = kdf.deriveKey("AES", ete);
        check(aes.getAlgorithm().equals("AES") && aes.getEncoded().length == 42,
                "deriveKey AES 42 bytes");
    }

    static byte[] bytes(String hex) { return java.util.HexFormat.of().parseHex(hex); }
    static byte[] filled(int b, int len) { byte[] r = new byte[len]; Arrays.fill(r, (byte) b); return r; }

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
