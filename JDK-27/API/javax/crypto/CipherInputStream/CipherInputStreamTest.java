/*
 * @test
 * @summary javax.crypto.CipherInputStream conformance: white-box structure + black-box behavior.
 * @run main CipherInputStreamTest
 */

import javax.crypto.*;
import javax.crypto.spec.*;
import java.io.*;
import java.lang.reflect.*;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class CipherInputStreamTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface Body { void run() throws Exception; }
    static boolean throwsType(Class<? extends Throwable> t, Body b) {
        try { b.run(); return false; }
        catch (Throwable x) { return x.getClass().equals(t); }
    }

    // fixed vectors (no randomness, deterministic across runs)
    static final byte[] KEY16 = new byte[16];
    static final byte[] IV12  = new byte[12];
    static final byte[] IV16  = new byte[16];
    static final byte[] PT37  = new byte[37];
    static final byte[] PT32  = new byte[32];
    static {
        for (int i = 0; i < 16; i++) KEY16[i] = (byte) i;
        for (int i = 0; i < 12; i++) IV12[i] = (byte) (0xA0 + i);
        for (int i = 0; i < 16; i++) IV16[i] = (byte) (0x10 + i);
        for (int i = 0; i < 37; i++) PT37[i] = (byte) (i * 3);
        for (int i = 0; i < 32; i++) PT32[i] = (byte) (0x40 + i);
    }
    static final SecretKeySpec AES_KEY = new SecretKeySpec(KEY16, "AES");
    static final GCMParameterSpec GCM128 = new GCMParameterSpec(128, IV12);
    static final IvParameterSpec CBC_SPEC = new IvParameterSpec(IV16);

    static Cipher newCipher(String t, int mode, Key k, AlgorithmParameterSpec ps) throws Exception {
        Cipher c = Cipher.getInstance(t);
        c.init(mode, k, ps);
        return c;
    }
    static byte[] cosEncrypt(String t, Key k, AlgorithmParameterSpec ps, byte[] pt) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        CipherOutputStream cos = new CipherOutputStream(bos, newCipher(t, Cipher.ENCRYPT_MODE, k, ps));
        cos.write(pt);
        cos.close();
        return bos.toByteArray();
    }
    static byte[] readAll(InputStream in, int bufSize) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (bufSize <= 0) { int b; while ((b = in.read()) != -1) out.write(b); }
        else { byte[] buf = new byte[bufSize]; int n; while ((n = in.read(buf)) != -1) out.write(buf, 0, n); }
        return out.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.CipherInputStream");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface()
                && !Modifier.isAbstract(c.getModifiers()), "public concrete class");
        check(c.getSuperclass().getName().equals("java.io.FilterInputStream"),
                "extends java.io.FilterInputStream");
        boolean pubCtor = false, protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isPublic(k.getModifiers()) && k.getParameterCount() == 2) pubCtor = true;
            if (Modifier.isProtected(k.getModifiers()) && k.getParameterCount() == 1) protCtor = true;
        }
        check(pubCtor, "public CipherInputStream(InputStream, Cipher)");
        check(protCtor, "protected CipherInputStream(InputStream) uses NullCipher");
        String[] methods = {"read", "skip", "available", "close", "markSupported"};
        for (String n : methods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n)) { found = true; break; }
            check(found, "method " + n + " declared");
        }
        int readOverloads = 0;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("read")) readOverloads++;
        check(readOverloads == 3, "3 read overloads (int / byte[] / byte[],int,int)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // ===== original assertions kept: NullCipher + CipherOutputStream passthrough =====
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        NullCipher nc = new NullCipher();
        nc.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(new byte[16], "AES"));
        CipherOutputStream cos = new CipherOutputStream(bos, nc);
        cos.write("abc".getBytes("UTF-8"));
        cos.close();
        byte[] enc = bos.toByteArray();
        check(enc.length == 3, "null cipher passthrough length");
        CipherInputStream cis = new CipherInputStream(new ByteArrayInputStream(enc), nc);
        check(cis.read() == (int) 'a', "read first byte");
        check(cis.read() == (int) 'b' && cis.read() == (int) 'c' && cis.read() == -1, "read remaining");
        cis.close();

        // ===== AES/CBC/PKCS5Padding streaming round trip =====
        byte[] ctCbc = cosEncrypt("AES/CBC/PKCS5Padding", AES_KEY, CBC_SPEC, PT37);
        CipherInputStream cis1 = new CipherInputStream(new ByteArrayInputStream(ctCbc),
                newCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC));
        byte[] got1 = readAll(cis1, 0);
        cis1.close();
        check(Arrays.equals(PT37, got1) && got1.length == 37, "CBC stream byte-at-a-time read round trip");

        CipherInputStream cis2 = new CipherInputStream(new ByteArrayInputStream(ctCbc),
                newCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC));
        byte[] got2 = readAll(cis2, 7);
        cis2.close();
        check(Arrays.equals(PT37, got2), "CBC stream read(byte[7]) loop round trip");

        // available() accounting mid-stream (CBC emits plaintext during update)
        CipherInputStream cisA = new CipherInputStream(new ByteArrayInputStream(ctCbc),
                newCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC));
        int first = cisA.read();
        check(first == (PT37[0] & 0xff) && cisA.available() == 31,
                "available() counts processed-but-unread bytes (PKCS5 decrypt holds last block; 32-1=31)");
        // skip() only skips already-processed buffered bytes
        long skipped = cisA.skip(3);
        check(skipped == 3, "skip(3) skips 3 buffered bytes");
        byte[] rest = readAll(cisA, 16);
        cisA.close();
        check(rest.length == 33 && (3 + 1 + rest.length) == PT37.length
                && PT37[4] == rest[0] && PT37[6] == rest[2],
                "skip advances within the plaintext stream, rest reads to exact EOF");
        check(cisA.available() == 0, "available() == 0 at EOF");

        // ===== AES/GCM/NoPadding streaming round trip =====
        byte[] ctGcm = cosEncrypt("AES/GCM/NoPadding", AES_KEY, GCM128, PT37);
        CipherInputStream gis = new CipherInputStream(new ByteArrayInputStream(ctGcm),
                newCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, AES_KEY, GCM128));
        int g0 = gis.read();
        check(g0 == (PT37[0] & 0xff) && gis.available() == 36,
                "GCM: first read() buffers whole authenticated plaintext (37-1 left)");
        ByteArrayOutputStream gout = new ByteArrayOutputStream();
        gout.write(g0);
        byte[] grest = readAll(gis, 0);
        gis.close();
        gout.write(grest);
        check(Arrays.equals(PT37, gout.toByteArray()), "GCM stream byte-at-a-time round trip");

        CipherInputStream gis2 = new CipherInputStream(new ByteArrayInputStream(ctGcm),
                newCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, AES_KEY, GCM128));
        check(Arrays.equals(PT37, readAll(gis2, 16)), "GCM stream read(byte[16]) loop round trip");
        gis2.close();

        // tampered GCM ciphertext: read() throws IOException wrapping AEADBadTagException
        byte[] badCt = ctGcm.clone();
        badCt[0] ^= 1;
        CipherInputStream gisBad = new CipherInputStream(new ByteArrayInputStream(badCt),
                newCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, AES_KEY, GCM128));
        boolean sawIo = false;
        try {
            if (gisBad.read() != -1) check(false, "tampered GCM read should not return data");
        } catch (IOException io) {
            sawIo = io.getCause() instanceof AEADBadTagException;
        }
        check(sawIo, "tampered GCM stream: read() -> IOException with AEADBadTagException cause");
        // close() after the failed read does not throw again
        boolean closeOk = true;
        try { gisBad.close(); } catch (IOException io) { closeOk = false; }
        check(closeOk, "close() after tamper failure is quiet");
        // closing an unread tampered stream is quiet too (exceptions suppressed on close)
        CipherInputStream gisBad2 = new CipherInputStream(new ByteArrayInputStream(badCt),
                newCipher("AES/GCM/NoPadding", Cipher.DECRYPT_MODE, AES_KEY, GCM128));
        boolean closeOk2 = true;
        try { gisBad2.close(); } catch (IOException io) { closeOk2 = false; }
        check(closeOk2, "close() without reading a tampered stream throws nothing");

        // ===== NullCipher-backed stream: buffering, skip, available, mark =====
        CipherInputStream nis = new CipherInputStream(
                new ByteArrayInputStream(new byte[]{10, 20, 30, 40, 50}), new NullCipher());
        check(!nis.markSupported(), "markSupported() false");
        check(nis.available() == 0, "available() 0 before first read");
        check(nis.read() == 10 && nis.available() == 4, "read() then available() == 4");
        check(nis.skip(2) == 2 && nis.read() == 40, "skip(2) skips 20,30; next read 40");
        byte[] tail = new byte[3];
        check(nis.read(tail, 0, 3) == 1 && tail[0] == 50, "read(b,off,len) returns last buffered byte");
        check(nis.read() == -1 && nis.read(tail) == -1 && nis.available() == 0, "EOF: read() and read(byte[]) return -1");
        nis.close();
        // double close is a no-op
        boolean doubleCloseOk = true;
        try { nis.close(); } catch (IOException io) { doubleCloseOk = false; }
        check(doubleCloseOk, "second close() is a no-op");
        // read(b,off,len) with len==0 returns 0 while data pending
        CipherInputStream nis2 = new CipherInputStream(
                new ByteArrayInputStream(new byte[]{1, 2}), new NullCipher());
        byte[] zero = new byte[2];
        check(nis2.read(zero, 0, 0) == 0, "read(b, off, 0) returns 0 without consuming");
        check(nis2.read() == 1 && nis2.read() == 2, "bytes intact after zero-length read");
        nis2.close();

        // ===== protected constructor path uses a NullCipher (subclass) =====
        class Sub extends CipherInputStream {
            Sub(InputStream is) { super(is); }
        }
        Sub sub = new Sub(new ByteArrayInputStream(new byte[]{9, 8}));
        check(sub.read() == 9 && sub.read() == 8 && sub.read() == -1,
                "protected ctor: NullCipher pass-through via subclass");
        sub.close();

        // empty ciphertext stream -> immediate EOF for CBC decrypt
        byte[] emptyCt = cosEncrypt("AES/CBC/PKCS5Padding", AES_KEY, CBC_SPEC, new byte[0]);
        CipherInputStream cisEmpty = new CipherInputStream(new ByteArrayInputStream(emptyCt),
                newCipher("AES/CBC/PKCS5Padding", Cipher.DECRYPT_MODE, AES_KEY, CBC_SPEC));
        check(cisEmpty.read() == -1, "encryption of empty plaintext yields empty plaintext stream");
        cisEmpty.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
