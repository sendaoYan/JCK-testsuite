/*
 * @test
 * @summary javax.crypto.spec.OAEPParameterSpec conformance: white-box structure + black-box behavior.
 * @run main OAEPParameterSpecTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.MGF1ParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.OAEPParameterSpec;
import javax.crypto.spec.PSource;

public class OAEPParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.OAEPParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
                "class is concrete, non-final");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.AlgorithmParameterSpec"::equals),
                "implements java.security.spec.AlgorithmParameterSpec");
        check(c.getDeclaredConstructor(String.class, String.class,
                AlgorithmParameterSpec.class, PSource.class).getModifiers() == Modifier.PUBLIC,
                "public ctor (String, String, AlgorithmParameterSpec, PSource)");
        Field def = c.getDeclaredField("DEFAULT");
        check(Modifier.isPublic(def.getModifiers()) && Modifier.isStatic(def.getModifiers())
                && Modifier.isFinal(def.getModifiers()), "DEFAULT is public static final");
        check(def.getType() == c, "DEFAULT field type OAEPParameterSpec");
        check(def.isAnnotationPresent(Deprecated.class), "DEFAULT is @Deprecated (since 19)");
        for (String m : new String[] {"getDigestAlgorithm", "getMGFAlgorithm",
                "getMGFParameters", "getPSource"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- original shallow assertions (kept) ---
        check(OAEPParameterSpec.DEFAULT != null, "DEFAULT non-null");
        OAEPParameterSpec s = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
        check("SHA-256".equals(s.getDigestAlgorithm()), "getDigestAlgorithm");
        check("MGF1".equals(s.getMGFAlgorithm()), "getMGFAlgorithm");
        check(s.getMGFParameters() == MGF1ParameterSpec.SHA256, "getMGFParameters");
        check(s.getPSource() == PSource.PSpecified.DEFAULT, "getPSource");

        // --- DEFAULT constant contents (PKCS#1 defaults) ---
        check("SHA-1".equals(OAEPParameterSpec.DEFAULT.getDigestAlgorithm()),
                "DEFAULT digest is SHA-1");
        check("MGF1".equals(OAEPParameterSpec.DEFAULT.getMGFAlgorithm()),
                "DEFAULT mgf is MGF1");
        check(OAEPParameterSpec.DEFAULT.getMGFParameters() == MGF1ParameterSpec.SHA1,
                "DEFAULT mgf params is MGF1ParameterSpec.SHA1");
        check(OAEPParameterSpec.DEFAULT.getPSource() == PSource.PSpecified.DEFAULT,
                "DEFAULT pSource is PSource.PSpecified.DEFAULT");

        // --- full getter round trip for a custom spec ---
        byte[] label = {0x01, 0x02, 0x03, 0x04};
        PSource.PSpecified labeled = new PSource.PSpecified(label);
        OAEPParameterSpec custom = new OAEPParameterSpec("SHA-512", "MGF1",
                MGF1ParameterSpec.SHA512, labeled);
        check("SHA-512".equals(custom.getDigestAlgorithm()), "custom digest");
        check("MGF1".equals(custom.getMGFAlgorithm()), "custom mgf");
        check(custom.getMGFParameters() instanceof MGF1ParameterSpec
                && "SHA-512".equals(((MGF1ParameterSpec) custom.getMGFParameters()).getDigestAlgorithm()),
                "custom mgf params digest");
        check(custom.getPSource() == labeled && Arrays.equals(((PSource.PSpecified) custom.getPSource()).getValue(), label),
                "custom pSource identity + label content");
        // mgfSpec == null is documented to be returned as null
        OAEPParameterSpec nullMgf = new OAEPParameterSpec("SHA-256", "MGF1", null,
                PSource.PSpecified.DEFAULT);
        check(nullMgf.getMGFParameters() == null, "null mgfSpec round-trips as null");

        // --- NPE for null digest / mgf / pSrc ---
        try { new OAEPParameterSpec(null, "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            check(false, "null digest should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new OAEPParameterSpec("SHA-256", null, MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT);
            check(false, "null mgf should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new OAEPParameterSpec("SHA-256", "MGF1", MGF1ParameterSpec.SHA256, null);
            check(false, "null pSrc should throw NPE"); }
        catch (NullPointerException e) { passed++; }

        // --- real chain: RSA/ECB/OAEPWithSHA-256AndMGF1Padding round trip ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        byte[] msg = new byte[32];
        for (int i = 0; i < 32; i++) msg[i] = (byte) i;
        OAEPParameterSpec sha256Spec = new OAEPParameterSpec("SHA-256", "MGF1",
                MGF1ParameterSpec.SHA256, labeled);
        Cipher enc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        enc.init(Cipher.ENCRYPT_MODE, kp.getPublic(), sha256Spec);
        byte[] ct = enc.doFinal(msg);
        check(ct.length == 256, "2048-bit RSA OAEP ciphertext is 256 bytes");
        Cipher enc2 = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        enc2.init(Cipher.ENCRYPT_MODE, kp.getPublic(), sha256Spec);
        check(!Arrays.equals(ct, enc2.doFinal(msg)),
                "OAEP uses random seed: two encryptions differ");
        Cipher dec = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        dec.init(Cipher.DECRYPT_MODE, kp.getPrivate(), sha256Spec);
        check(Arrays.equals(msg, dec.doFinal(ct)), "RSA OAEP SHA-256 round trip with label");
        // decrypting with a different label (empty DEFAULT) fails
        Cipher decWrong = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        decWrong.init(Cipher.DECRYPT_MODE, kp.getPrivate(), new OAEPParameterSpec("SHA-256",
                "MGF1", MGF1ParameterSpec.SHA256, PSource.PSpecified.DEFAULT));
        try {
            decWrong.doFinal(ct);
            check(false, "label mismatch should fail padding check");
        } catch (javax.crypto.BadPaddingException e) { passed++; }
        // digest mismatch in spec also fails
        Cipher decSha1 = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        decSha1.init(Cipher.DECRYPT_MODE, kp.getPrivate(), new OAEPParameterSpec("SHA-1",
                "MGF1", MGF1ParameterSpec.SHA1, labeled));
        try {
            decSha1.doFinal(ct);
            check(false, "digest mismatch should fail padding check");
        } catch (javax.crypto.BadPaddingException e) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
