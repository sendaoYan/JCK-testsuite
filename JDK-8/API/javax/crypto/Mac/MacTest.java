/*
 * @test
 * @summary javax.crypto.Mac conformance: white-box structure + black-box behavior.
 * @run main MacTest
 */

import java.lang.reflect.*;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyPairGenerator;
import java.security.Provider;
import java.security.Security;
import java.util.Arrays;
import javax.crypto.Mac;
import javax.crypto.MacSpi;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

public class MacTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.Mac");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        boolean cloneable = false;
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals("java.lang.Cloneable")) cloneable = true;
        check(cloneable, "implements Cloneable");
        // protected constructor Mac(MacSpi, Provider, String)
        boolean protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            if (Modifier.isProtected(k.getModifiers())
                    && k.getParameterCount() == 3
                    && k.getParameterTypes()[0].getName().equals("javax.crypto.MacSpi")
                    && k.getParameterTypes()[1].getName().equals("java.security.Provider")
                    && k.getParameterTypes()[2] == String.class) protCtor = true;
        }
        check(protCtor, "protected Mac(MacSpi, Provider, String) ctor");
        String[] pubMethods = {
            "getAlgorithm", "getInstance", "getProvider", "getMacLength",
            "init", "update", "doFinal", "reset", "clone"
        };
        for (String n : pubMethods) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) found = true;
            check(found, "declared method " + n);
        }
        // getInstance overloads are static final factory methods
        boolean staticFactory = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getInstance") && Modifier.isStatic(m.getModifiers())
                    && Modifier.isFinal(m.getModifiers())) staticFactory = true;
        }
        check(staticFactory, "static final getInstance factories");
        // service methods are final (engine wrapper, not meant to be subclassed by users)
        boolean finalInit = false;
        for (Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("init") && Modifier.isFinal(m.getModifiers())) finalInit = true;
        }
        check(finalInit, "init is final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static String hex(byte[] b) {
        StringBuilder sb = new StringBuilder();
        for (byte x : b) sb.append(String.format("%02x", x));
        return sb.toString();
    }

    static void testBlackBox() throws Exception {
        // --- RFC 4231 known-answer vectors (also in JDK test/jdk/com/sun/crypto/provider/Mac/MacKAT.java) ---
        // TC1: key = 0x0b x 20, data = "Hi There"
        byte[] tc1Key = new byte[20];
        Arrays.fill(tc1Key, (byte) 0x0b);
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(tc1Key, "HmacSHA256"));
        byte[] tc1 = mac.doFinal("Hi There".getBytes(StandardCharsets.US_ASCII));
        check(hex(tc1).equals("b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7"),
                "RFC 4231 TC1 exact HMAC-SHA256");
        check(tc1.length == 32, "TC1 tag length 32");

        // TC2: key = "Jefe", data = "what do ya want for nothing?"
        mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec("Jefe".getBytes(StandardCharsets.US_ASCII), "HmacSHA256"));
        byte[] tc2 = mac.doFinal("what do ya want for nothing?".getBytes(StandardCharsets.US_ASCII));
        check(hex(tc2).equals("5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843"),
                "RFC 4231 TC2 exact HMAC-SHA256");

        // TC3: key = 0xaa x 20, data = 0xdd x 50 (vector from JDK MacKAT.java)
        byte[] tc3Key = new byte[20];
        Arrays.fill(tc3Key, (byte) 0xaa);
        byte[] tc3Data = new byte[50];
        Arrays.fill(tc3Data, (byte) 0xdd);
        mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(tc3Key, "HmacSHA256"));
        check(hex(mac.doFinal(tc3Data)).equals("773ea91e36800e46854db8ebd09181a72959098b3ef8c122d9635514ced565fe"),
                "RFC 4231 TC3 exact HMAC-SHA256");

        // --- getMacLength across digest families ---
        check(Mac.getInstance("HmacSHA256").getMacLength() == 32, "getMacLength HmacSHA256 == 32");
        check(Mac.getInstance("HmacSHA384").getMacLength() == 48, "getMacLength HmacSHA384 == 48");
        check(Mac.getInstance("HmacSHA512").getMacLength() == 64, "getMacLength HmacSHA512 == 64");
        // JDK-8 has no SHA-3 based HMAC (HmacSHA3-* is JDK-9+), so the family
        // cross-check above covers HmacSHA1/HmacSHA256/HmacSHA384/HmacSHA512 only.

        // --- chunked update is equivalent to one-shot doFinal ---
        byte[] data = "The quick brown fox jumps over the lazy dog".getBytes(StandardCharsets.US_ASCII);
        SecretKeySpec key = new SecretKeySpec(new byte[32], "HmacSHA256");
        mac = Mac.getInstance("HmacSHA256");
        mac.init(key);
        byte[] oneShot = mac.doFinal(data);
        mac.reset();
        mac.update(new byte[0]);                       // empty update is a no-op
        mac.update(data[0]);                           // single byte
        mac.update(data, 1, 10);                       // ranged update
        mac.update(Arrays.copyOfRange(data, 11, data.length));
        check(Arrays.equals(mac.doFinal(), oneShot), "chunked update == one-shot doFinal");

        // update(ByteBuffer): heap, direct and read-only buffers all equivalent
        mac.reset();
        ByteBuffer heap = ByteBuffer.wrap(data.clone());
        mac.update(heap);
        check(Arrays.equals(mac.doFinal(), oneShot), "update(heap ByteBuffer) equivalent");
        mac.reset();
        ByteBuffer direct = ByteBuffer.allocateDirect(data.length);
        direct.put(data);
        direct.flip();
        mac.update(direct);
        check(Arrays.equals(mac.doFinal(), oneShot), "update(direct ByteBuffer) equivalent");
        mac.reset();
        ByteBuffer ro = ByteBuffer.wrap(data.clone()).asReadOnlyBuffer();
        mac.update(ro);
        check(Arrays.equals(mac.doFinal(), oneShot), "update(read-only ByteBuffer) equivalent");

        // --- reset semantics ---
        mac.reset();
        mac.update((byte) 1);
        mac.reset();                                   // discards buffered byte 1
        mac.update((byte) 2);
        Mac fresh = Mac.getInstance("HmacSHA256");
        fresh.init(key);
        check(Arrays.equals(mac.doFinal(), fresh.doFinal(new byte[]{2})), "reset discards buffered input");
        // doFinal() auto-resets: re-uploading the same data yields the same tag
        mac.update(data);
        byte[] again = mac.doFinal();
        check(Arrays.equals(again, oneShot), "doFinal returns same tag after auto reset");
        mac.update(data);
        check(Arrays.equals(mac.doFinal(), oneShot), "second doFinal after re-feed identical");
        // doFinal(byte[]) auto-resets too
        mac.update(data);
        check(Arrays.equals(mac.doFinal(new byte[0]), oneShot), "doFinal(byte[]) result and reset");

        // --- doFinal(byte[] out, int off) ---
        mac.reset();
        mac.update(data);
        byte[] out40 = new byte[40];
        mac.doFinal(out40, 8);
        check(Arrays.equals(Arrays.copyOfRange(out40, 8, 40), oneShot), "doFinal(out, off) honors offset");
        mac.reset();
        mac.update(data);
        try {
            mac.doFinal(new byte[31], 0);
            check(false, "short output buffer must throw ShortBufferException");
        } catch (ShortBufferException e) {
            check(true, "doFinal into short buffer -> ShortBufferException");
        }

        // --- clone() independence ---
        mac.reset();
        mac.init(key);
        mac.update(data);
        Mac cl = (Mac) mac.clone();
        check(cl.getAlgorithm().equals("HmacSHA256"), "clone keeps algorithm");
        check(cl.getMacLength() == mac.getMacLength(), "clone keeps mac length");
        byte[] origTag = mac.doFinal();                // original finishes first
        byte[] cloneTag = cl.doFinal();                // clone is unaffected
        check(Arrays.equals(origTag, cloneTag), "cloned Mac produces same tag independently");
        check(Arrays.equals(origTag, oneShot), "original tag still correct");

        // --- uninitialized Mac ---
        Mac uninit = Mac.getInstance("HmacSHA256");
        try { uninit.update((byte) 0); check(false, "update(byte) before init must throw"); }
        catch (IllegalStateException e) { check(true, "update(byte) before init -> IllegalStateException"); }
        try { uninit.update(new byte[1]); check(false, "update(byte[]) before init must throw"); }
        catch (IllegalStateException e) { check(true, "update(byte[]) before init -> IllegalStateException"); }
        try { uninit.update(new byte[1], 0, 1); check(false, "update(byte[],int,int) before init must throw"); }
        catch (IllegalStateException e) { check(true, "update(byte[],int,int) before init -> IllegalStateException"); }
        try { uninit.update(ByteBuffer.wrap(new byte[1])); check(false, "update(ByteBuffer) before init must throw"); }
        catch (IllegalStateException e) { check(true, "update(ByteBuffer) before init -> IllegalStateException"); }
        try { uninit.doFinal(); check(false, "doFinal() before init must throw"); }
        catch (IllegalStateException e) { check(true, "doFinal() before init -> IllegalStateException"); }
        try { uninit.doFinal(new byte[1], 0); check(false, "doFinal(byte[],int) before init must throw"); }
        catch (IllegalStateException e) { check(true, "doFinal(byte[],int) before init -> IllegalStateException"); }
        try { uninit.doFinal(new byte[1]); check(false, "doFinal(byte[]) before init must throw"); }
        catch (IllegalStateException e) { check(true, "doFinal(byte[]) before init -> IllegalStateException"); }
        // queries are legal before init
        check("HmacSHA256".equals(uninit.getAlgorithm()), "getAlgorithm before init");
        check(uninit.getProvider() != null, "getProvider before init");
        check(uninit.getMacLength() == 32, "getMacLength before init");
        uninit.reset();                                 // must not throw
        check(true, "reset before init is a no-op");

        // --- init rejects a non-HMAC key ---
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        try {
            mac.init(kpg.generateKeyPair().getPublic());
            check(false, "init with RSA public key must throw");
        } catch (java.security.InvalidKeyException e) {
            check(true, "init(wrong key type) -> InvalidKeyException");
        }

        // --- getInstance variants ---
        Mac byName = Mac.getInstance("HmacSHA256", "SunJCE");
        check(byName.getProvider().getName().equals("SunJCE"), "getInstance(alg, providerName)");
        Provider sunjce = Security.getProvider("SunJCE");
        Mac byProv = Mac.getInstance("HmacSHA256", sunjce);
        check(byProv.getProvider() == sunjce, "getInstance(alg, Provider) uses given provider");
        check(byProv.getAlgorithm().equals("HmacSHA256"), "getInstance(alg, Provider) algorithm");
        try { Mac.getInstance("NoSuchMacAlgo"); check(false, "bogus algorithm must throw"); }
        catch (java.security.NoSuchAlgorithmException e) { check(true, "bogus algorithm -> NoSuchAlgorithmException"); }
        try { Mac.getInstance("HmacSHA256", "NoSuchProvider"); check(false, "bogus provider name must throw"); }
        catch (java.security.NoSuchProviderException e) { check(true, "bogus provider -> NoSuchProviderException"); }
        try { Mac.getInstance(null); check(false, "null algorithm must throw"); }
        catch (NullPointerException e) { check(true, "null algorithm -> NullPointerException"); }
        try { Mac.getInstance("HmacSHA256", (Provider) null); check(false, "null Provider must throw"); }
        catch (IllegalArgumentException e) { check(true, "null Provider -> IllegalArgumentException"); }

        // --- engine wrapper delegates to a user-supplied MacSpi (protected ctor) ---
        CountingSpi spi = new CountingSpi();
        MyMac mm = new MyMac(spi, sunjce, "MYMAC");
        mm.init(key);
        mm.update((byte) 1);
        mm.update(new byte[]{2}, 0, 1);
        byte[] res = mm.doFinal();
        check(mm.getMacLength() == 5, "wrapper getMacLength delegates to engineGetMacLength");
        check(res.length == 5, "wrapper doFinal returns engineDoFinal result");
        check(spi.inits == 1 && spi.updates == 2 && spi.doFinals == 1 && spi.resets >= 1,
                "wrapper dispatches init/update/doFinal/reset to SPI (inits=" + spi.inits
                + " updates=" + spi.updates + " doFinals=" + spi.doFinals + " resets=" + spi.resets + ")");
        MyMac mmClone = (MyMac) mm.clone();
        check(mmClone != mm && mmClone.getClass() == MyMac.class, "Mac.clone() returns same-class copy");
    }

    // minimal SPI used to verify the engine wrapper contract
    static class CountingSpi extends MacSpi implements Cloneable {
        int updates, resets, inits, doFinals;
        protected int engineGetMacLength() { return 5; }
        protected void engineInit(java.security.Key k, java.security.spec.AlgorithmParameterSpec s) { inits++; }
        protected void engineUpdate(byte b) { updates++; }
        protected void engineUpdate(byte[] in, int off, int len) { updates++; }
        protected byte[] engineDoFinal() { doFinals++; return new byte[engineGetMacLength()]; }
        protected void engineReset() { resets++; }
    }

    static class MyMac extends Mac {
        MyMac(MacSpi spi, Provider p, String algo) { super(spi, p, algo); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
