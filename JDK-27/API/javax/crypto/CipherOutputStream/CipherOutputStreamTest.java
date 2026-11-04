/*
 * @test
 * @summary javax.crypto.CipherOutputStream conformance: white-box structure + black-box behavior.
 * @run main CipherOutputStreamTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class CipherOutputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // fixed vectors (deterministic across runs)
    static final byte[] KEY16 = new byte[16];
    static final byte[] IV12  = new byte[12];
    static final byte[] IV16  = new byte[16];
    static final byte[] PT37  = new byte[37];
    static {
        for (int i = 0; i < 16; i++) KEY16[i] = (byte) i;
        for (int i = 0; i < 12; i++) IV12[i] = (byte) (0xA0 + i);
        for (int i = 0; i < 16; i++) IV16[i] = (byte) (0x10 + i);
        for (int i = 0; i < 37; i++) PT37[i] = (byte) (i * 3);
    }
    static final SecretKeySpec AES_KEY = new SecretKeySpec(KEY16, "AES");
    static final GCMParameterSpec GCM128 = new GCMParameterSpec(128, IV12);
    static final IvParameterSpec CBC_SPEC = new IvParameterSpec(IV16);

    static Cipher newCipher(String t, int mode, Key k, AlgorithmParameterSpec ps) throws Exception {
        Cipher c = Cipher.getInstance(t);
        c.init(mode, k, ps);
        return c;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.CipherOutputStream");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface()
                && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        check(c.getSuperclass().getName().equals("java.io.FilterOutputStream"),
                "extends java.io.FilterOutputStream");
        boolean pubCtor = false, protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 2) pubCtor = true;
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 1) protCtor = true;
        }
        check(pubCtor, "public CipherOutputStream(OutputStream, Cipher)");
        check(protCtor, "protected CipherOutputStream(OutputStream) uses NullCipher");
        String[] methods = {"write", "flush", "close"};
        for (String n : methods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n)) { found = true; break; }
            check(found, "method " + n + " declared");
        }
        int writeOverloads = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("write")) writeOverloads++;
        check(writeOverloads == 3, "3 write overloads (int / byte[] / byte[],int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // ===== original assertions kept: NullCipher passthrough =====
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NullCipher nc = new NullCipher();
        nc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        CipherOutputStream cos = new CipherOutputStream(bos, nc);
        cos.write('x');
        cos.write("yz".getBytes("UTF-8"));
        cos.flush();
        cos.close();
        check(Arrays.equals(new byte[]{(byte) 'x', (byte) 'y', (byte) 'z'}, bos.toByteArray()),
                "null cipher passthrough");

        // ===== AES/CBC/PKCS5Padding: data complete only after close() =====
        ByteArrayOutputStream cbos = new ByteArrayOutputStream();
        CipherOutputStream ccos = new CipherOutputStream(cbos,
                newCipher("AES/CBC/PKCS5Padding", Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC));
        ccos.write(PT37);
        ccos.flush(); // flush() does not flush cipher-buffered partial blocks
        check(cbos.size() == 32, "37B written: only 2 full blocks (32B) flushed before close");
        ccos.close();
        check(cbos.size() == 48, "close() emits final partial block: 37B -> 48B with PKCS5");
        boolean doubleCloseOk = true;
        try { ccos.close(); } catch (IOException io) { doubleCloseOk = false; }
        check(doubleCloseOk, "second close() is a no-op");
        // decrypt the streamed ciphertext back
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(cbos.toByteArray()),
                newCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC));
        byte[] back = cis.readAllBytes();
        cis.close();
        check(Arrays.equals(PT37, back), "COS->CIS CBC cross round trip");

        // ===== write patterns: chunked and byte-at-a-time equal one-shot =====
        Cipher one = Cipher.getInstance("AES/CBC/PKCS5Padding");
        one.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        ByteArrayOutputStream oneOut = new ByteArrayOutputStream();
        CipherOutputStream oneCos = new CipherOutputStream(oneOut, one);
        oneCos.write(PT37);
        oneCos.close();

        Cipher ch = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ch.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        ByteArrayOutputStream chOut = new ByteArrayOutputStream();
        CipherOutputStream chCos = new CipherOutputStream(chOut, ch);
        chCos.write(PT37, 0, 5);
        chCos.write(PT37, 5, 10);
        chCos.write(PT37, 15, 22);
        chCos.close();
        check(Arrays.equals(oneOut.toByteArray(), chOut.toByteArray()),
                "chunked writes (5+10+22) equal one-shot write");

        Cipher by = Cipher.getInstance("AES/CBC/PKCS5Padding");
        by.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        ByteArrayOutputStream byOut = new ByteArrayOutputStream();
        CipherOutputStream byCos = new CipherOutputStream(byOut, by);
        for (int i = 0; i < 10; i++) byCos.write(PT37[i] & 0xff);
        byCos.write(PT37, 10, 27);
        byCos.close();
        check(Arrays.equals(oneOut.toByteArray(), byOut.toByteArray()),
                "byte-at-a-time write(int) x10 then write(byte[],off,len) equal one-shot");

        // streamed CBC output equals Cipher.doFinal directly
        Cipher direct = Cipher.getInstance("AES/CBC/PKCS5Padding");
        direct.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        check(Arrays.equals(direct.doFinal(PT37), oneOut.toByteArray()),
                "streamed ciphertext equals Cipher.doFinal output");

        // ===== AES/GCM/NoPadding streaming =====
        Cipher g = Cipher.getInstance("AES/GCM/NoPadding");
        g.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        ByteArrayOutputStream gOut = new ByteArrayOutputStream();
        CipherOutputStream gCos = new CipherOutputStream(gOut, g);
        gCos.write(PT37, 0, 20);
        gCos.write(PT37, 20, 17);
        gCos.close();
        Cipher gDirect = Cipher.getInstance("AES/GCM/NoPadding");
        gDirect.init(Cipher.ENCRYPT_MODE, AES_KEY, GCM128);
        check(Arrays.equals(gDirect.doFinal(PT37), gOut.toByteArray()),
                "GCM streamed output (tag included) equals direct doFinal");

        // tampered GCM decryption through COS: close() swallows the exception (unlike CIS read)
        byte[] badCt = gOut.toByteArray().clone();
        badCt[3] ^= 0x55;
        ByteArrayOutputStream badOut = new ByteArrayOutputStream();
        CipherOutputStream badCos = new CipherOutputStream(badOut,
                newCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, AES_KEY, GCM128));
        boolean noThrow = true;
        try {
            badCos.write(badCt);
            badCos.close();
        } catch (IOException io) {
            noThrow = false;
        }
        check(noThrow && badOut.size() == 0,
                "GCM tamper through COS: close() throws nothing and no plaintext leaked");
        boolean badDoubleClose = true;
        try { badCos.close(); } catch (IOException io) { badDoubleClose = false; }
        check(badDoubleClose, "close() idempotent after swallowed AEAD failure");

        // ===== protected constructor path uses a NullCipher (subclass) =====
        class Sub extends CipherOutputStream {
            Sub(OutputStream os) { super(os); }
        }
        ByteArrayOutputStream subOut = new ByteArrayOutputStream();
        Sub sub = new Sub(subOut);
        sub.write(new byte[]{7, 7});
        sub.close();
        check(subOut.size() == 2 && subOut.toByteArray()[0] == 7 && subOut.toByteArray()[1] == 7,
                "protected ctor: NullCipher pass-through via subclass");

        // ===== NullCipher COS accepts every write form =====
        ByteArrayOutputStream nOut = new ByteArrayOutputStream();
        CipherOutputStream nCos = new CipherOutputStream(nOut, new NullCipher());
        nCos.write(65);                                  // write(int)
        nCos.write(new byte[]{66});                      // write(byte[])
        nCos.write(new byte[]{0, 67, 68, 0}, 1, 2);      // write(byte[],off,len)
        nCos.close();
        check(Arrays.equals("ABCD".getBytes("UTF-8"), nOut.toByteArray()),
                "NullCipher COS: write(int)/write(byte[])/write(byte[],off,len) all pass through");

        // empty plaintext through COS: CBC+PKCS5 still emits a full padding block on close
        Cipher eC = Cipher.getInstance("AES/CBC/PKCS5Padding");
        eC.init(Cipher.ENCRYPT_MODE, AES_KEY, CBC_SPEC);
        ByteArrayOutputStream eOut = new ByteArrayOutputStream();
        CipherOutputStream eCos = new CipherOutputStream(eOut, eC);
        eCos.close();
        check(eOut.size() == 16, "empty write + close emits exactly one padding block (16B)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
