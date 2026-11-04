/*
 * @test
 * @summary javax.crypto.spec.RC2ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main RC2ParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.*;

public class RC2ParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final byte[] IV = bytes("0001020304050607");

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = RC2ParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getEffectiveKeyBits", "getIV", "equals", "hashCode"});
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] t = k.getParameterTypes();
            if (t.length == 1 && t[0] == int.class) ctors++;
            if (t.length == 2 && t[0] == int.class && t[1] == byte[].class) ctors++;
            if (t.length == 3 && t[0] == int.class && t[1] == byte[].class && t[2] == int.class) ctors++;
        }
        check(ctors == 3 && c.getConstructors().length == 3, "three public ctors");
        // equals/hashCode override Object
        check(RC2ParameterSpec.class.getDeclaredMethod("equals", Object.class).getDeclaringClass()
                == RC2ParameterSpec.class, "equals overridden");
        check(RC2ParameterSpec.class.getDeclaredMethod("hashCode").getDeclaringClass()
                == RC2ParameterSpec.class, "hashCode overridden");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) basics
        RC2ParameterSpec s = new RC2ParameterSpec(64, IV);
        check(s.getEffectiveKeyBits() == 64, "getEffectiveKeyBits");
        check(Arrays.equals(IV, s.getIV()), "getIV");
        check(new RC2ParameterSpec(64).getIV() == null, "no IV is null");
        check(s.equals(new RC2ParameterSpec(64, IV)), "equals");
        check(s.hashCode() == new RC2ParameterSpec(64, IV).hashCode(), "hashCode");

        // offset ctor takes the 8-byte window starting at offset
        byte[] longBuf = bytes("AABBCCDDEEFF0011" + "0001020304050607" + "1122334455667788");
        RC2ParameterSpec off = new RC2ParameterSpec(128, longBuf, 8);
        check(off.getEffectiveKeyBits() == 128, "offset ctor effectiveKeyBits");
        check(Arrays.equals(off.getIV(), IV), "offset ctor IV window");
        check(off.equals(new RC2ParameterSpec(128, IV)), "offset ctor equals plain");
        // IV defensive copy on construction and on access
        byte[] mut = IV.clone();
        RC2ParameterSpec defC = new RC2ParameterSpec(64, mut);
        mut[0] = (byte) 0x99;
        check(defC.getIV()[0] == IV[0], "ctor defensive copy of IV");
        byte[] got = defC.getIV();
        got[1] = (byte) 0x77;
        check(defC.getIV()[1] == IV[1], "getIV defensive copy");
        check(defC.getIV() != defC.getIV(), "getIV new array each call");
        check(new RC2ParameterSpec(64, IV, 0).getIV().length == 8, "IV length 8");

        // equals/hashCode contract
        check(new RC2ParameterSpec(64).equals(new RC2ParameterSpec(64)), "null-IV equals null-IV");
        check(!new RC2ParameterSpec(64).equals(new RC2ParameterSpec(64, IV)), "null IV != with IV");
        check(!new RC2ParameterSpec(64, IV).equals(new RC2ParameterSpec(128, IV)), "different bits not equal");
        check(!new RC2ParameterSpec(64, IV).equals(new RC2ParameterSpec(64, bytes("0706050403020100"))),
                "different IV not equal");
        check(new RC2ParameterSpec(64).hashCode() == new RC2ParameterSpec(64).hashCode(),
                "null-IV hashCode equal");
        check(s.equals(s), "equals reflexive");
        check(!s.equals(null), "equals null false");
        check(!s.equals("RC2"), "equals other type false");
        check(s instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");

        // error cases: null IV / short IV -> IAE; negative offset -> ArrayIndexOutOfBoundsException
        try { new RC2ParameterSpec(64, null); check(false, "null IV must throw"); }
        catch (IllegalArgumentException e) { check(true, "null IV IAE"); }
        try { new RC2ParameterSpec(64, new byte[7]); check(false, "short IV must throw"); }
        catch (IllegalArgumentException e) { check(true, "short IV IAE"); }
        try { new RC2ParameterSpec(64, new byte[7], 0); check(false, "short IV offset must throw"); }
        catch (IllegalArgumentException e) { check(true, "short IV offset IAE"); }
        try { new RC2ParameterSpec(64, IV, -1); check(false, "negative offset must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "negative offset AIOOBE"); }

        // real chain: RC2ParameterSpec drives Cipher("RC2/CBC/PKCS5Padding") roundtrip
        SecretKeySpec key = new SecretKeySpec(bytes("000102030405060708090a0b0c0d0e0f"), "RC2");
        Cipher enc = Cipher.getInstance("RC2/CBC/PKCS5Padding");
        enc.init(Cipher.ENCRYPT_MODE, key, new RC2ParameterSpec(128, IV));
        // multi-block plaintext so a wrong IV corrupts only the first block
        // while later blocks and the padding stay valid (deterministic outcome)
        byte[] pt = "RC2 roundtrip with a longer payload!".getBytes("UTF-8");
        byte[] ct = enc.doFinal(pt);
        check(ct.length == 40, "RC2 ciphertext length 40");
        Cipher dec = Cipher.getInstance("RC2/CBC/PKCS5Padding");
        dec.init(Cipher.DECRYPT_MODE, key, new RC2ParameterSpec(128, IV));
        check(Arrays.equals(dec.doFinal(ct), pt), "RC2 cipher roundtrip via spec");
        // equal-but-distinct spec objects behave identically
        Cipher dec2 = Cipher.getInstance("RC2/CBC/PKCS5Padding");
        dec2.init(Cipher.DECRYPT_MODE, key, new RC2ParameterSpec(128, IV.clone()));
        check(Arrays.equals(dec2.doFinal(ct), pt), "cipher accepts equal spec copy");
        // different IV corrupts the first block
        Cipher dec3 = Cipher.getInstance("RC2/CBC/PKCS5Padding");
        dec3.init(Cipher.DECRYPT_MODE, key, new RC2ParameterSpec(128, bytes("0706050403020100")));
        check(!Arrays.equals(dec3.doFinal(ct), pt), "wrong IV yields wrong plaintext");
    }

    static byte[] bytes(String hex) { return java.util.HexFormat.of().parseHex(hex); }

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
