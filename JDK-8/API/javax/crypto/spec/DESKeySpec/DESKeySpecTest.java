/*
 * @test
 * @summary javax.crypto.spec.DESKeySpec conformance: white-box structure + black-box behavior (parity + weak keys).
 * @run main DESKeySpecTest
 */

import java.lang.reflect.*;
import java.security.InvalidKeyException;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.*;

public class DESKeySpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    /** All 16 semi-weak/weak DES keys from FIPS 74 (as listed in DESKeySpec source). */
    static final String[] WEAK_KEYS = {
        "0101010101010101", "FEFEFEFEFEFEFEFE", "1F1F1F1F0E0E0E0E", "E0E0E0E0F1F1F1F1",
        "01FE01FE01FE01FE", "1FE01FE00EF10EF1", "01E001E001F101F1", "1FFE1FFE0EFE0EFE",
        "011F011F010E010E", "E0FEE0FEF1FEF1FE", "FE01FE01FE01FE01", "E01FE01FF10EF10E",
        "E001E001F101F101", "FE1FFE1FFE0EFE0E", "1F011F010E010E01", "FEE0FEE0FEF1FEF1"
    };

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = DESKeySpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.KeySpec"});
        checkMethods(c, new String[]{"getKey", "isParityAdjusted", "isWeak"});
        check(DESKeySpec.DES_KEY_LEN == 8, "DES_KEY_LEN == 8");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == byte[].class) ctors++;
            if (k.getParameterCount() == 2 && k.getParameterTypes()[0] == byte[].class
                    && k.getParameterTypes()[1] == int.class) ctors++;
        }
        check(ctors == 2, "two public ctors (byte[])/(byte[],int)");
        check(DESKeySpec.class.getField("DES_KEY_LEN").get(null).equals(8)
                && Modifier.isStatic(DESKeySpec.class.getField("DES_KEY_LEN").getModifiers())
                && Modifier.isFinal(DESKeySpec.class.getField("DES_KEY_LEN").getModifiers()),
                "DES_KEY_LEN public static final int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // fixed, not weak, odd parity (the SecretKeyFactory("DES") output preserves odd parity)
        byte[] key8 = oddParity(bytes("4041424344454647"));

        DESKeySpec s = new DESKeySpec(key8);
        check(s.getKey().length == 8, "getKey length 8");
        check(java.util.Arrays.equals(s.getKey(), key8), "getKey exact bytes");
        // getKey defensive copy
        byte[] g = s.getKey();
        g[0] = (byte) 0x99;
        check(s.getKey()[0] == key8[0], "getKey defensive copy");
        check(s.getKey() != s.getKey(), "getKey new array each call");
        // ctor defensive copy
        byte[] mut = key8.clone();
        DESKeySpec s2 = new DESKeySpec(mut);
        mut[3] = (byte) 0x11;
        check(s2.getKey()[3] == key8[3], "ctor defensive copy");
        // offset ctor window
        byte[] buf = new byte[16];
        System.arraycopy(key8, 0, buf, 8, 8);
        DESKeySpec off = new DESKeySpec(buf, 8);
        check(java.util.Arrays.equals(off.getKey(), key8), "offset ctor exact window");

        // all-zero key is constructible (parity/weakness not enforced by ctor)
        check(new DESKeySpec(new byte[8]).getKey().length == 8, "zero key ctor ok");

        // isParityAdjusted: true / false
        byte[] parity = new byte[]{1, 1, 1, 1, 1, 1, 1, 1};
        check(DESKeySpec.isParityAdjusted(parity, 0), "isParityAdjusted 0x01 true");
        check(!DESKeySpec.isParityAdjusted(new byte[8], 0), "isParityAdjusted zero false");
        check(!DESKeySpec.isParityAdjusted(bytes("0303030303030303"), 0), "isParityAdjusted 0x03 false");
        byte[] win = new byte[16];
        System.arraycopy(parity, 0, win, 4, 8);
        check(DESKeySpec.isParityAdjusted(win, 4), "isParityAdjusted offset true");

        // isWeak: known weak key true, normal key false, full FIPS-74 table
        check(DESKeySpec.isWeak(bytes("0101010101010101"), 0), "isWeak 0x01.. true");
        check(!DESKeySpec.isWeak(key8, 0), "isWeak normal false");
        int weakHits = 0;
        for (String h : WEAK_KEYS) {
            if (DESKeySpec.isWeak(bytes(h), 0)) weakHits++;
        }
        check(weakHits == 16, "all 16 FIPS-74 weak keys detected");
        // weak detection with offset window
        byte[] wbuf = new byte[12];
        System.arraycopy(bytes("0101010101010101"), 0, wbuf, 4, 8);
        check(DESKeySpec.isWeak(wbuf, 4), "isWeak offset true");

        // instanceof contract
        check(new DESKeySpec(key8) instanceof KeySpec, "instanceof KeySpec");

        // error cases
        try { new DESKeySpec(new byte[7]); check(false, "short ctor must throw"); }
        catch (InvalidKeyException e) { check(true, "short ctor IKE"); }
        try { new DESKeySpec(key8, 2); check(false, "offset overflow ctor must throw"); }
        catch (InvalidKeyException e) { check(true, "offset overflow ctor IKE"); }
        try { DESKeySpec.isWeak(new byte[7], 0); check(false, "short isWeak must throw"); }
        catch (InvalidKeyException e) { check(true, "short isWeak IKE"); }
        try { DESKeySpec.isParityAdjusted(new byte[7], 0); check(false, "short isParityAdjusted must throw"); }
        catch (InvalidKeyException e) { check(true, "short isParityAdjusted IKE"); }

        // real chain: spec -> SecretKeyFactory("DES") -> Cipher DES/CBC/PKCS5Padding roundtrip
        SecretKeyFactory skf = SecretKeyFactory.getInstance("DES");
        SecretKey k1 = skf.generateSecret(new DESKeySpec(key8));
        check(java.util.Arrays.equals(k1.getEncoded(), key8), "factory key material equals spec");
        check(k1.getAlgorithm().equals("DES"), "factory key algorithm DES");
        Cipher c1 = Cipher.getInstance("DES/CBC/PKCS5Padding");
        byte[] iv = bytes("0001020304050607");
        c1.init(Cipher.ENCRYPT_MODE, k1, new IvParameterSpec(iv));
        byte[] ct = c1.doFinal("DES roundtrip!!".getBytes("UTF-8"));
        c1.init(Cipher.DECRYPT_MODE, k1, new IvParameterSpec(iv));
        check(new String(c1.doFinal(ct), "UTF-8").equals("DES roundtrip!!"), "DES encrypt/decrypt roundtrip");
        SecretKey k1off = skf.generateSecret(off);
        check(java.util.Arrays.equals(k1off.getEncoded(), key8), "offset spec same key material");
    }

    static byte[] bytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++)
            out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
        return out;
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
