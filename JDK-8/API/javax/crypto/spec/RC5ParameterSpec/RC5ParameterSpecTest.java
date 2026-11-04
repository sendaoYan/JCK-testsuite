/*
 * @test
 * @summary javax.crypto.spec.RC5ParameterSpec conformance: white-box structure + black-box behavior.
 * @run main RC5ParameterSpecTest
 */

import java.lang.reflect.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;
import javax.crypto.spec.RC5ParameterSpec;

public class RC5ParameterSpecTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final byte[] IV8 = bytes("0001020304050607");
    static final byte[] IV4 = bytes("08090a0b");

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = RC5ParameterSpec.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "concrete class");
        check(!Modifier.isFinal(c.getModifiers()) && !Modifier.isAbstract(c.getModifiers()), "plain class");
        check(c.getSuperclass() == Object.class, "extends Object");
        checkInterfaces(c, new String[]{"java.security.spec.AlgorithmParameterSpec"});
        checkMethods(c, new String[]{"getVersion", "getRounds", "getWordSize", "getIV", "equals", "hashCode"});
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] t = k.getParameterTypes();
            if (t.length == 3 && t[0] == int.class && t[1] == int.class && t[2] == int.class) ctors++;
            if (t.length == 4 && t[0] == int.class && t[1] == int.class && t[2] == int.class
                    && t[3] == byte[].class) ctors++;
            if (t.length == 5 && t[0] == int.class && t[1] == int.class && t[2] == int.class
                    && t[3] == byte[].class && t[4] == int.class) ctors++;
        }
        check(ctors == 3 && c.getConstructors().length == 3, "three public ctors");
        check(RC5ParameterSpec.class.getDeclaredMethod("equals", Object.class).getDeclaringClass()
                == RC5ParameterSpec.class, "equals overridden");
        check(RC5ParameterSpec.class.getDeclaredMethod("hashCode").getDeclaringClass()
                == RC5ParameterSpec.class, "hashCode overridden");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // (preserved) basics
        RC5ParameterSpec s = new RC5ParameterSpec(0, 16, 32, IV8);
        check(s.getVersion() == 0, "getVersion");
        check(s.getRounds() == 16, "getRounds");
        check(s.getWordSize() == 32, "getWordSize");
        check(Arrays.equals(IV8, s.getIV()), "getIV");
        check(s.equals(new RC5ParameterSpec(0, 16, 32, IV8)), "equals");

        // no-IV ctor and IV dimensions follow the word size: block = 2 * (wordSize / 8)
        RC5ParameterSpec noIv = new RC5ParameterSpec(0x10, 12, 32);
        check(noIv.getVersion() == 0x10, "no-IV version");
        check(noIv.getRounds() == 12, "no-IV rounds");
        check(noIv.getWordSize() == 32, "no-IV wordSize");
        check(noIv.getIV() == null, "no-IV ctor IV null");
        RC5ParameterSpec ws16 = new RC5ParameterSpec(0, 8, 16, IV4);
        check(Arrays.equals(ws16.getIV(), IV4), "wordSize 16 uses 4-byte IV");
        // offset ctor window
        byte[] longBuf = bytes("deadbeef" + "0001020304050607" + "cafebabe");
        RC5ParameterSpec off = new RC5ParameterSpec(0, 16, 32, longBuf, 4);
        check(Arrays.equals(off.getIV(), IV8), "offset ctor IV window");
        check(off.equals(s), "offset ctor equals plain");
        check(new RC5ParameterSpec(0, 16, 32, IV8, 0).getIV().length == 8, "IV length 8 for ws 32");

        // constructor performs no range validation of version/rounds/wordSize (stored verbatim)
        RC5ParameterSpec zero = new RC5ParameterSpec(1, 0, 16);
        check(zero.getRounds() == 0 && zero.getVersion() == 1 && zero.getWordSize() == 16,
                "zero rounds stored verbatim");

        // IV defensive copies both on construction and on access
        byte[] mut = IV8.clone();
        RC5ParameterSpec defC = new RC5ParameterSpec(0, 16, 32, mut);
        mut[0] = (byte) 0x99;
        check(defC.getIV()[0] == IV8[0], "ctor defensive copy of IV");
        byte[] got = defC.getIV();
        got[1] = (byte) 0x77;
        check(defC.getIV()[1] == IV8[1], "getIV defensive copy");
        check(defC.getIV() != defC.getIV(), "getIV new array each call");

        // equals/hashCode contract
        check(noIv.equals(new RC5ParameterSpec(0x10, 12, 32)), "null-IV equals null-IV");
        check(!noIv.equals(new RC5ParameterSpec(0x10, 12, 32, IV8)), "null IV != with IV");
        check(!noIv.equals(new RC5ParameterSpec(0x10, 13, 32)), "different rounds not equal");
        check(!noIv.equals(new RC5ParameterSpec(0x11, 12, 32)), "different version not equal");
        check(!noIv.equals(new RC5ParameterSpec(0x10, 12, 16)), "different wordSize not equal");
        check(!s.equals(new RC5ParameterSpec(0, 16, 32, bytes("0706050403020100"))), "different IV not equal");
        check(noIv.hashCode() == new RC5ParameterSpec(0x10, 12, 32).hashCode(), "null-IV hashCode equal");
        check(s.hashCode() == new RC5ParameterSpec(0, 16, 32, IV8).hashCode(), "hashCode contract");
        check(s.equals(s), "equals reflexive");
        check(!s.equals(null) && !s.equals("RC5"), "equals null/other type false");
        check(s instanceof AlgorithmParameterSpec, "instanceof AlgorithmParameterSpec");

        // error cases: null IV / too-short IV -> IAE; negative offset -> ArrayIndexOutOfBoundsException
        try { new RC5ParameterSpec(0, 12, 32, null); check(false, "null IV must throw"); }
        catch (IllegalArgumentException e) { check(true, "null IV IAE"); }
        try { new RC5ParameterSpec(0, 12, 32, new byte[7]); check(false, "short IV must throw"); }
        catch (IllegalArgumentException e) { check(true, "short IV IAE"); }
        try { new RC5ParameterSpec(0, 12, 32, new byte[9], 2); check(false, "short offset IV must throw"); }
        catch (IllegalArgumentException e) { check(true, "short offset IV IAE"); }
        try { new RC5ParameterSpec(0, 12, 32, IV8, -1); check(false, "negative offset must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "negative offset AIOOBE"); }
    }

    static byte[] bytes(String hex) {
        byte[] out = new byte[hex.length() / 2];
        for (int i = 0; i < out.length; i++)
            out[i] = (byte) Integer.parseInt(hex.substring(2 * i, 2 * i + 2), 16);
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
