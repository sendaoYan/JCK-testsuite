/*
 * @test
 * @summary javax.crypto.spec.DESedeKeySpec conformance: white-box structure + black-box behavior.
 * @run main DESedeKeySpecTest
 */

import java.lang.reflect.*;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;

public class DESedeKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = DESedeKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getKey", "isParityAdjusted"});
        check(DESedeKeySpec.DES_EDE_KEY_LEN == 24, "DES_EDE_KEY_LEN == 24");
        // two public constructors: (byte[]) and (byte[], int)
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == byte[].class) ctors++;
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == byte[].class
                    && k.getParameterTypes()[1] == int.class) ctors++;
        }
        check(ctors == 2, "two public ctors (byte[])/(byte[],int)");
        check(DESedeKeySpec.class.getField("DES_EDE_KEY_LEN").get(null).equals(24)
                && Modifier.isPublic(DESedeKeySpec.class.getField("DES_EDE_KEY_LEN").getModifiers())
                && Modifier.isStatic(DESedeKeySpec.class.getField("DES_EDE_KEY_LEN").getModifiers())
                && Modifier.isFinal(DESedeKeySpec.class.getField("DES_EDE_KEY_LEN").getModifiers()),
                "DES_EDE_KEY_LEN public static final int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fixed odd-parity 24-byte key material (each byte has odd bit count)
        byte[] key24 = oddParity(bytes("303132333435363738393a3b3c3d3e3f4041424344454647"));

        DESedeKeySpec s = new DESedeKeySpec(key24);
        byte[] got = s.getKey();
        check(got.length == 24, "getKey length 24");
        check(java.util.Arrays.equals(got, key24), "getKey exact bytes");
        // getKey defensive copy: mutating the returned array does not affect the spec
        got[0] = (byte) 0x99;
        check(s.getKey()[0] == key24[0], "getKey defensive copy");
        check(s.getKey() != s.getKey(), "getKey returns new array each call");
        // constructor defensive copy: mutating the source buffer after construction is invisible
        byte[] mut = key24.clone();
        DESedeKeySpec s2 = new DESedeKeySpec(mut);
        mut[5] = (byte) 0x77;
        check(s2.getKey()[5] == key24[5], "ctor defensive copy");
        check(s2.getKey().length == 24, "offset-less ctor key length");

        // offset constructor reads exactly bytes [offset, offset+24)
        byte[] buf = new byte[40];
        System.arraycopy(key24, 0, buf, 16, 24);
        DESedeKeySpec off = new DESedeKeySpec(buf, 16);
        check(java.util.Arrays.equals(off.getKey(), key24), "offset ctor exact window");

        // plain zero key of 24 bytes is constructible (no parity enforcement in ctor)
        check(new DESedeKeySpec(new byte[24]).getKey().length == 24, "zero key ctor ok");

        // isParityAdjusted: true for odd parity, false for even parity anywhere
        check(DESedeKeySpec.isParityAdjusted(key24, 0), "isParityAdjusted true");
        byte[] parity = new byte[24];
        java.util.Arrays.fill(parity, (byte) 1);
        check(DESedeKeySpec.isParityAdjusted(parity, 0), "isParityAdjusted 0x01 fill true");
        byte[] even = key24.clone();
        even[8] = (byte) 0x03; // 0x03 has two set bits (even parity) in the 2nd DES subkey
        check(!DESedeKeySpec.isParityAdjusted(even, 0), "isParityAdjusted false (even byte)");
        check(!DESedeKeySpec.isParityAdjusted(new byte[24], 0), "isParityAdjusted false (all zero)");
        // offset variant: window starting at 4
        byte[] win = new byte[32];
        System.arraycopy(key24, 0, win, 4, 24);
        check(DESedeKeySpec.isParityAdjusted(win, 4), "isParityAdjusted offset window true");
        win[5] = (byte) 0x05; // 0x05 has two set bits (even parity)
        check(!DESedeKeySpec.isParityAdjusted(win, 4), "isParityAdjusted offset window false");

        // instanceof contract
        check(new DESedeKeySpec(key24) instanceof KeySpec, "instanceof KeySpec");

        // error cases: short material -> InvalidKeyException
        try {
            new DESedeKeySpec(new byte[23]);
            check(false, "short ctor must throw");
        } catch (InvalidKeyException e) { check(true, "short ctor IKE"); }
        try {
            new DESedeKeySpec(key24, 1);
            check(false, "offset overflow ctor must throw");
        } catch (InvalidKeyException e) { check(true, "offset overflow ctor IKE"); }
        try {
            DESedeKeySpec.isParityAdjusted(new byte[]{1}, 0);
            check(false, "short isParityAdjusted must throw");
        } catch (InvalidKeyException e) { check(true, "short isParityAdjusted IKE"); }

        // real chain: spec -> SecretKeyFactory("DESede") -> Cipher DESede/CBC/PKCS5Padding roundtrip
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DESede");
        SecretKey k3 = skf.generateSecret(new DESedeKeySpec(key24));
        check(java.util.Arrays.equals(k3.getEncoded(), key24), "factory key material equals spec");
        check(k3.getAlgorithm().equals("DESede"), "factory key algorithm DESede");
        Cipher c3 = Cipher.getInstance("DESede/CBC/PKCS5Padding");
        byte[] iv = bytes("0001020304050607");
        c3.init(Cipher.ENCRYPT_MODE, k3, new IvParameterSpec(iv));
        byte[] ct = c3.doFinal("Hello DESede roundtrip 1234".getBytes("UTF-8"));
        check(ct.length == 32, "PKCS5 ciphertext length 32");
        c3.init(Cipher.DECRYPT_MODE, k3, new IvParameterSpec(iv));
        check(new String(c3.doFinal(ct), "UTF-8").equals("Hello DESede roundtrip 1234"),
                "DESede encrypt/decrypt roundtrip");
        // offset-built spec produces the same factory key material
        SecretKey k3off = skf.generateSecret(off);
        check(java.util.Arrays.equals(k3off.getEncoded(), key24), "offset spec same key material");
    }

    static byte[] bytes(String hex) {
        return java.util.HexFormat.of().parseHex(hex);
    }

    /** Adjust every byte to odd parity (DES requirement: each key byte has an odd number of set bits). */
    static byte[] oddParity(byte[] in) {
        byte[] out = in.clone();
        for (int i = 0; i < out.length; i++) {
            int b = out[i] & 0xFE;
            out[i] = (byte) (b | (Integer.bitCount(b) % 2 == 0 ? 1 : 0));
        }
        return out;
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
