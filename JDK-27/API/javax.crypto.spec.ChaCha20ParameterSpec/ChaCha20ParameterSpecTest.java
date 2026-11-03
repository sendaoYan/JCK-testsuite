/*
 * @test
 * @summary javax.crypto.spec.ChaCha20ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main ChaCha20ParameterSpecTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.ChaCha20ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class ChaCha20ParameterSpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (cond) { passed++; } else { throw new AssertionError(msg); }
    }

    static final byte[] RFC7539_KEY = java.util.HexFormat.of().parseHex(
            "000102030405060708090a0b0c0d0e0f101112131415161718191a1b1c1d1e1f");
    static final byte[] RFC7539_NONCE = java.util.HexFormat.of().parseHex(
            "000000000000004a00000000");
    static final byte[] RFC7539_PT = java.util.HexFormat.of().parseHex(
            "4c616469657320616e642047656e746c656d656e206f662074686520636c6173" +
            "73206f66202739393a204966204920636f756c64206f6666657220796f75206f" +
            "6e6c79206f6e652074697020666f7220746865206675747572652c2073756e73" +
            "637265656e20776f756c642062652069742e");
    static final byte[] RFC7539_CT = java.util.HexFormat.of().parseHex(
            "6e2e359a2568f98041ba0728dd0d6981e97e7aec1d4360c20a27afccfd9fae0b" +
            "f91b65c5524733ab8f593dabcd62b3571639d624e65152ab8f530c359f0861d8" +
            "07ca0dbf500d6a6156a38e088a22b65e52bc514d16ccf806818ce91ab7793736" +
            "5af90bbf74a35be6b40b8eedf2785e42874d");

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.ChaCha20ParameterSpec");
        check(Modifier.isPublic(c.getModifiers()), "class is public");
        check(Modifier.isFinal(c.getModifiers()), "class is final");
        check(!Modifier.isAbstract(c.getModifiers()), "class is concrete");
        check(c.getSuperclass() == Object.class, "direct superclass java.lang.Object");
        check(Arrays.stream(c.getInterfaces()).map(Class::getName)
                .anyMatch("java.security.spec.AlgorithmParameterSpec"::equals),
                "implements java.security.spec.AlgorithmParameterSpec");
        check(c.getDeclaredConstructor(byte[].class, int.class).getModifiers() == Modifier.PUBLIC,
                "public ctor (byte[], int)");
        for (String m : new String[] {"getNonce", "getCounter"}) {
            boolean found = false;
            for (Method dm : c.getDeclaredMethods()) {
                if (dm.getName().equals(m) && Modifier.isPublic(dm.getModifiers())) { found = true; break; }
            }
            check(found, "public method " + m + " exists");
        }
        try {
            check(c.getDeclaredMethod("getCounter").getReturnType() == int.class,
                    "getCounter returns int");
        } catch (NoSuchMethodException e) { throw new AssertionError("getCounter missing"); }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] nonce = new byte[12];
        // --- original shallow assertions (kept) ---
        ChaCha20ParameterSpec s = new ChaCha20ParameterSpec(nonce, 7);
        check(Arrays.equals(nonce, s.getNonce()), "getNonce");
        check(s.getCounter() == 7, "getCounter");

        // --- defensive copies of the nonce ---
        ChaCha20ParameterSpec fixed = new ChaCha20ParameterSpec(RFC7539_NONCE.clone(), 1);
        byte[] n1 = fixed.getNonce();
        byte[] n2 = fixed.getNonce();
        check(n1 != n2, "getNonce returns a new array each call");
        check(Arrays.equals(n1, n2) && n1.length == 12, "repeated getNonce contents equal");
        n1[0] = (byte)0xff;
        check(fixed.getNonce()[0] == RFC7539_NONCE[0], "getNonce result copy is detached");
        byte[] mut = RFC7539_NONCE.clone();
        ChaCha20ParameterSpec copy = new ChaCha20ParameterSpec(mut, 0);
        mut[11] = (byte)0x42;
        check(copy.getNonce()[11] == RFC7539_NONCE[11], "ctor copies nonce (external mutation invisible)");
        check(fixed.getCounter() == 1 && copy.getCounter() == 0, "getCounter round trip");

        // --- nonce length validation: exactly 12 bytes ---
        for (int badLen : new int[] {0, 1, 11, 13, 16}) {
            try {
                new ChaCha20ParameterSpec(new byte[badLen], 0);
                check(false, "nonce length " + badLen + " should throw IAE");
            } catch (IllegalArgumentException e) { passed++; }
        }
        try {
            new ChaCha20ParameterSpec(null, 0);
            check(false, "null nonce should throw NPE");
        } catch (NullPointerException e) { passed++; }

        // --- real chain: ChaCha20 cipher, RFC 7539 2.4.2 known answer (counter = 1) ---
        SecretKey key = new SecretKeySpec(RFC7539_KEY, "ChaCha20");
        Cipher enc = Cipher.getInstance("ChaCha20");
        enc.init(Cipher.ENCRYPT_MODE, key, new ChaCha20ParameterSpec(RFC7539_NONCE, 1));
        byte[] ct = enc.doFinal(RFC7539_PT);
        check(Arrays.equals(ct, RFC7539_CT), "RFC 7539 sample vector ciphertext (12B nonce, counter 1)");
        Cipher encAgain = Cipher.getInstance("ChaCha20");
        encAgain.init(Cipher.ENCRYPT_MODE, key, new ChaCha20ParameterSpec(RFC7539_NONCE.clone(), 1));
        check(Arrays.equals(ct, encAgain.doFinal(RFC7539_PT)),
                "same nonce/counter reproduces the same keystream");
        Cipher dec = Cipher.getInstance("ChaCha20");
        dec.init(Cipher.DECRYPT_MODE, key, new ChaCha20ParameterSpec(RFC7539_NONCE, 1));
        check(Arrays.equals(RFC7539_PT, dec.doFinal(ct)), "ChaCha20 decrypt round trip");
        // wrong counter cannot decrypt (keystream shifted)
        Cipher decWrongCtr = Cipher.getInstance("ChaCha20");
        decWrongCtr.init(Cipher.DECRYPT_MODE, key, new ChaCha20ParameterSpec(RFC7539_NONCE, 2));
        check(!Arrays.equals(RFC7539_PT, decWrongCtr.doFinal(ct)),
                "counter=2 yields wrong plaintext");
        // wrong nonce cannot decrypt
        Cipher decWrongNonce = Cipher.getInstance("ChaCha20");
        decWrongNonce.init(Cipher.DECRYPT_MODE, key,
                new ChaCha20ParameterSpec(new byte[12], 1));
        check(!Arrays.equals(RFC7539_PT, decWrongNonce.doFinal(ct)),
                "all-zero nonce yields wrong plaintext");

        // --- second known answer: RFC 7539 vector 1, all-zero key/nonce/PT, counter = 0 ---
        SecretKey zeroKey = new SecretKeySpec(new byte[32], "ChaCha20");
        byte[] zeroPt = new byte[64];
        Cipher zero = Cipher.getInstance("ChaCha20");
        zero.init(Cipher.ENCRYPT_MODE, zeroKey, new ChaCha20ParameterSpec(new byte[12], 0));
        byte[] zeroCt = zero.doFinal(zeroPt);
        check(java.util.HexFormat.of().formatHex(zeroCt).equals(
                "76b8e0ada0f13d90405d6ae55386bd28bdd219b8a08ded1aa836efcc8b770dc7" +
                "da41597c5157488d7724e03fb8d84a376a43b8f41518a11cc387b669b2ee6586"),
                "RFC 7539 all-zero vector keystream (counter 0)");
        Cipher zeroDec = Cipher.getInstance("ChaCha20");
        zeroDec.init(Cipher.DECRYPT_MODE, zeroKey, new ChaCha20ParameterSpec(new byte[12], 0));
        check(Arrays.equals(zeroPt, zeroDec.doFinal(zeroCt)), "all-zero vector round trip");
        // stream cipher: ciphertext length equals plaintext length
        check(zeroCt.length == 64, "ChaCha20 preserves plaintext length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
