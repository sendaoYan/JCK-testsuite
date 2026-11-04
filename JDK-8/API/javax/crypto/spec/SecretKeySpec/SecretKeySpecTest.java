/*
 * @test
 * @summary javax.crypto.spec.SecretKeySpec conformance: white-box structure + black-box behavior.
 * @run main SecretKeySpecTest
 */

import java.lang.reflect.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecretKeySpecTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.spec.SecretKeySpec");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        // implements KeySpec and SecretKey
        String[] want = {"java.security.spec.KeySpec", "javax.crypto.SecretKey"};
        for (String n : want) {
            boolean found = false;
            for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) found = true;
            check(found, "implements " + n);
        }
        check(c.getInterfaces().length == 2, "exactly 2 direct interfaces");
        // both constructors are public
        int ctors = 0;
        for (Constructor<?> k : c.getDeclaredConstructors())
            if (Modifier.isPublic(k.getModifiers())) ctors++;
        check(ctors == 2, "2 public constructors (byte[],String) and (byte[],int,int,String)");
        String[] methods = {"getAlgorithm", "getFormat", "getEncoded", "hashCode", "equals"};
        for (String n : methods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declared method " + n);
        }
        // version difference: JDK 24+ adds a package-private clear() method (key destruction);
        // the JDK-8 class declares no such member.
        boolean hasClear = false;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("clear")) hasClear = true;
        check(!hasClear, "no clear() member on JDK-8 SecretKeySpec");
        boolean equalsTakesObject = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("equals") && m.getParameterTypes()[0] == Object.class)
                equalsTakesObject = true;
        check(equalsTakesObject, "equals(Object) signature");
        boolean encodedFresh = false;
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals("getEncoded") && m.getReturnType() == byte[].class)
                encodedFresh = true;
        check(encodedFresh, "byte[] getEncoded() signature");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        byte[] key16 = new byte[16];
        for (int i = 0; i < 16; i++) key16[i] = (byte) (0x40 + i);

        // --- ctor(byte[], String) accessors ---
        SecretKeySpec s = new SecretKeySpec(key16, "AES");
        check("AES".equals(s.getAlgorithm()), "getAlgorithm");
        check("RAW".equals(s.getFormat()), "getFormat RAW");
        check(s.getEncoded().length == 16, "getEncoded length");
        check(Arrays.equals(s.getEncoded(), key16), "getEncoded content");
        check("aes".equals(new SecretKeySpec(key16, "aes").getAlgorithm()),
                "getAlgorithm returns the name verbatim");

        // --- defensive copies: source array, returned arrays ---
        byte[] mut = key16.clone();
        SecretKeySpec defended = new SecretKeySpec(mut, "AES");
        mut[0] = (byte) 0xFF;                              // mutate after construction
        check(defended.getEncoded()[0] == key16[0], "constructor copies the source array");
        byte[] e1 = defended.getEncoded();
        byte[] e2 = defended.getEncoded();
        check(e1 != e2, "getEncoded returns a fresh array each call");
        check(Arrays.equals(e1, e2), "repeated getEncoded content identical");
        e1[0] = (byte) 0xFF;                                // mutate a returned copy
        check(defended.getEncoded()[0] == key16[0], "mutating a returned copy does not affect the key");

        // --- ctor(byte[], int, int, String): subarray extraction + defense ---
        byte[] big = new byte[64];
        big[16] = 1; big[31] = 2;
        SecretKeySpec sub = new SecretKeySpec(big, 16, 16, "AES");
        check(sub.getEncoded().length == 16, "offset ctor takes exactly len bytes");
        check(sub.getEncoded()[0] == 1 && sub.getEncoded()[15] == 2, "offset ctor extracts the right window");
        byte[] mutBig = big.clone();
        SecretKeySpec subDefended = new SecretKeySpec(mutBig, 16, 16, "AES");
        mutBig[16] = 99;
        check(subDefended.getEncoded()[0] == 1, "offset ctor copies the source array");
        SecretKeySpec zeroLen = new SecretKeySpec(key16, 5, 0, "AES");
        check(zeroLen.getEncoded().length == 0, "zero-length window yields empty key material");
        check("AES".equals(zeroLen.getAlgorithm()) && "RAW".equals(zeroLen.getFormat()),
                "zero-length spec still reports algorithm and format");

        // --- equals / hashCode contract ---
        SecretKeySpec same = new SecretKeySpec(key16.clone(), "AES");
        check(s.equals(same) && same.equals(s), "same bytes + same algorithm are equal");
        check(s.hashCode() == same.hashCode(), "equal keys share hashCode");
        check(!s.equals(new SecretKeySpec(new byte[16], "AES")), "different bytes are not equal");
        check(!s.equals(new SecretKeySpec(key16, "HmacSHA256")), "different algorithm is not equal");
        check(s.equals(new SecretKeySpec(key16.clone(), "aes")),
                "algorithm comparison ignores case");
        check(s.hashCode() == new SecretKeySpec(key16.clone(), "aes").hashCode(),
                "hashCode also ignores algorithm case");
        check(!s.equals(null), "equals(null) is false");
        check(!s.equals("AES"), "equals(non-key) is false");
        byte[] k24 = new byte[24];
        SecretKeySpec desede = new SecretKeySpec(k24, "DESede");
        check(desede.equals(new SecretKeySpec(k24.clone(), "TripleDES")),
                "DESede and TripleDES aliases compare equal");
        byte[] windowSrc = new byte[32];
        for (int i = 0; i < 32; i++) windowSrc[i] = (byte) i;
        SecretKeySpec w1 = new SecretKeySpec(windowSrc, 0, 16, "AES");
        SecretKeySpec w2 = new SecretKeySpec(windowSrc, 1, 16, "AES");
        check(!w1.equals(w2), "same source, different windows with different bytes are not equal");
        check(w1.equals(new SecretKeySpec(windowSrc.clone(), 0, 16, "AES")),
                "same window from a copied source is equal");

        // --- constructor exceptions ---
        try { new SecretKeySpec(null, "AES"); check(false, "null key must throw"); }
        catch (IllegalArgumentException e) { check(true, "null key -> IllegalArgumentException"); }
        catch (NullPointerException e) { check(false, "null key: wrong exception NPE"); }
        try { new SecretKeySpec(key16, null); check(false, "null algorithm must throw"); }
        catch (IllegalArgumentException e) { check(true, "null algorithm -> IllegalArgumentException"); }
        catch (NullPointerException e) { check(false, "null algorithm: wrong exception NPE"); }
        try { new SecretKeySpec(new byte[0], "AES"); check(false, "empty key must throw"); }
        catch (IllegalArgumentException e) { check(true, "empty key -> IllegalArgumentException"); }
        try { new SecretKeySpec(null, 0, 4, "AES"); check(false, "4-arg null key must throw"); }
        catch (IllegalArgumentException e) { check(true, "4-arg null key -> IllegalArgumentException"); }
        try { new SecretKeySpec(key16, 0, 4, null); check(false, "4-arg null algorithm must throw"); }
        catch (IllegalArgumentException e) { check(true, "4-arg null algorithm -> IllegalArgumentException"); }
        try { new SecretKeySpec(new byte[0], 0, 0, "AES"); check(false, "4-arg empty key must throw"); }
        catch (IllegalArgumentException e) { check(true, "4-arg empty key -> IllegalArgumentException"); }
        try { new SecretKeySpec(key16, 13, 4, "AES"); check(false, "window past the end must throw"); }
        catch (IllegalArgumentException e) { check(true, "offset+len > length -> IllegalArgumentException"); }
        try { new SecretKeySpec(key16, -1, 4, "AES"); check(false, "negative offset must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "negative offset -> ArrayIndexOutOfBoundsException"); }
        try { new SecretKeySpec(key16, 0, -4, "AES"); check(false, "negative length must throw"); }
        catch (ArrayIndexOutOfBoundsException e) { check(true, "negative length -> ArrayIndexOutOfBoundsException"); }

        // --- a SecretKeySpec is directly usable as a Cipher key ---
        byte[] iv = new byte[12];
        for (int i = 0; i < 12; i++) iv[i] = (byte) (0x60 + i);
        byte[] plain = "SecretKeySpec behavior test".getBytes(StandardCharsets.UTF_8);
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key16, "AES"), new GCMParameterSpec(128, iv));
        byte[] ct = enc.doFinal(plain);
        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key16, "AES"), new GCMParameterSpec(128, iv));
        check(Arrays.equals(dec.doFinal(ct), plain), "AES/GCM roundtrip driven by SecretKeySpec");
        check(ct.length == plain.length + 16, "GCM ciphertext length = plaintext + tag");
        // HMAC path with the same class, different algorithm name
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(new byte[32], "HmacSHA256"));
        check(mac.doFinal(new byte[0]).length == 32, "SecretKeySpec drives Mac HmacSHA256");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
