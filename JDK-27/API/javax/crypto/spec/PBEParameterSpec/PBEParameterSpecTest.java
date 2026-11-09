/*
 * @test
 * @summary javax.crypto.spec.PBEParameterSpec conformance: white-box structure + black-box behavior.
 * @run main PBEParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

public class PBEParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.PBEParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()),
                "class is concrete, non-final");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.AlgorithmParameterSpec"::equals),
                "implements java.security.spec.AlgorithmParameterSpec");
        check(c.getDeclaredConstructor(byte[].class, int.class).getModifiers() == Modifier.PUBLIC,
                "public ctor (byte[], int)");
        check(c.getDeclaredConstructor(byte[].class, int.class, AlgorithmParameterSpec.class)
                .getModifiers() == Modifier.PUBLIC,
                "public ctor (byte[], int, AlgorithmParameterSpec)");
        for (String m : new String[] {"getSalt", "getIterationCount", "getParameterSpec"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] salt = new byte[]{1, 2, 3};
        // --- original shallow assertions (kept) ---
        PBEParameterSpec s = new PBEParameterSpec(salt, 1000);
        check(Arrays.equals(salt, s.getSalt()), "getSalt");
        check(s.getIterationCount() == 1000, "getIterationCount");
        check(s.getParameterSpec() == null, "default parameter spec null");

        // --- defensive copies of the salt ---
        byte[] saltFixed = {(byte)0x0a, (byte)0x1b, (byte)0x2c, (byte)0x3d,
                            (byte)0x4e, (byte)0x5f, (byte)0x60, (byte)0x71};
        PBEParameterSpec fixed = new PBEParameterSpec(saltFixed, 2048);
        saltFixed[0] = (byte)0xff;
        check(fixed.getSalt()[0] == (byte)0x0a, "ctor copies salt (external mutation invisible)");
        byte[] g1 = fixed.getSalt();
        byte[] g2 = fixed.getSalt();
        check(g1 != g2, "getSalt returns a new array each call");
        check(Arrays.equals(g1, g2), "repeated getSalt contents equal");
        g1[0] = 0x00;
        check(fixed.getSalt()[0] == (byte)0x0a, "getSalt result copy is detached");
        // class performs no range validation on iterationCount / salt length
        check(new PBEParameterSpec(new byte[0], 0).getIterationCount() == 0,
                "iterationCount=0 accepted by class (validation deferred to KDF/cipher)");
        check(new PBEParameterSpec(new byte[0], 0).getSalt().length == 0,
                "empty salt accepted by class");

        // --- nested AlgorithmParameterSpec (identity, since 1.8) ---
        IvParameterSpec inner = new IvParameterSpec(new byte[16]);
        PBEParameterSpec nested = new PBEParameterSpec(saltFixed, 4096, inner);
        check(nested.getParameterSpec() == inner, "3-arg ctor returns the same nested spec");
        check(nested.getIterationCount() == 4096 && Arrays.equals(nested.getSalt(), saltFixed),
                "3-arg ctor preserves salt and iteration count");
        PBEParameterSpec explicitNull = new PBEParameterSpec(saltFixed, 10, null);
        check(explicitNull.getParameterSpec() == null, "explicit null paramSpec allowed");
        // null salt -> NPE (both ctors; salt.clone() on null)
        try { new PBEParameterSpec(null, 1000); check(false, "null salt 2-arg should throw NPE"); }
        catch (NullPointerException e) { passed++; }
        try { new PBEParameterSpec(null, 1000, inner); check(false, "null salt 3-arg should throw NPE"); }
        catch (NullPointerException e) { passed++; }

        // --- real chain: PBEWithHmacSHA256AndAES_128 encrypt/decrypt driven by PBEParameterSpec ---
        char[] pw = "topsecret".toCharArray();
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
        PBEKeySpec keySpec = new PBEKeySpec(pw, saltFixed, 2048);
        SecretKey pbeKey = skf.generateSecret(keySpec);
        byte[] pt = "The quick brown fox jumps over the lazy dog".
                getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        Cipher enc = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        enc.init(Cipher.ENCRYPT_MODE, pbeKey, new PBEParameterSpec(saltFixed, 2048));
        byte[] ct = enc.doFinal(pt);
        byte[] iv = enc.getIV();
        check(iv != null && iv.length == 16, "PBES2 AES-128 cipher publishes a 16-byte IV");
        // decrypt needs salt+iterations plus the cipher IV nested in the same spec class
        Cipher dec = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        dec.init(Cipher.DECRYPT_MODE, pbeKey,
                new PBEParameterSpec(saltFixed, 2048, new IvParameterSpec(iv)));
        check(Arrays.equals(pt, dec.doFinal(ct)), "PBE round trip via 3-arg PBEParameterSpec");
        // wrong iteration count derives a different key -> decryption must not
        // recover the plaintext. The padding check usually throws
        // BadPaddingException, but it can (~1/256) validate by chance and return
        // garbage instead; accept either outcome rather than gambling on the
        // exception (a ~0.4% flake over a 10x -Xcomp run).
        Cipher decWrong = Cipher.getInstance("PBEWithHmacSHA256AndAES_128");
        decWrong.init(Cipher.DECRYPT_MODE, pbeKey,
                new PBEParameterSpec(saltFixed, 2049, new IvParameterSpec(iv)));
        try {
            byte[] wrong = decWrong.doFinal(ct);
            check(!Arrays.equals(pt, wrong), "wrong iteration count does not recover plaintext");
        } catch (javax.crypto.BadPaddingException e) { passed++; }
        keySpec.clearPassword();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
