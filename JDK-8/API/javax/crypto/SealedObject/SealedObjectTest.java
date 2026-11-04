/*
 * @test
 * @summary javax.crypto.SealedObject conformance: white-box structure + black-box behavior.
 * @run main SealedObjectTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import java.security.Key;
import java.security.NoSuchProviderException;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SealedObject;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SealedObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // fixed AES-128 key and GCM IV (12 bytes) + 128-bit tag: fully deterministic
    static final SecretKeySpec KEY = new SecretKeySpec(hex("000102030405060708090a0b0c0d0e0f"), "AES");
    static final GCMParameterSpec GCM = new GCMParameterSpec(128, hex("101112131415161718191a1b"));
    static final SecretKeySpec WRONG_KEY = new SecretKeySpec(hex("ff0102030405060708090a0b0c0d0e0f"), "AES");

    static byte[] hex(String s) {
        byte[] r = new byte[s.length() / 2];
        for (int i = 0; i < r.length; i++)
            r[i] = (byte) Integer.parseInt(s.substring(2 * i, 2 * i + 2), 16);
        return r;
    }

    interface Thunk { void run() throws Exception; }
    static void expect(Class<? extends Throwable> t, Thunk r) {
        try {
            r.run();
        } catch (Throwable e) {
            if (t.isInstance(e)) { passed++; return; }
            throw new AssertionError("expected " + t.getName() + " but got " + e, e);
        }
        throw new AssertionError("expected " + t.getName() + " but nothing was thrown");
    }

    static Cipher sealCipher() throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.ENCRYPT_MODE, KEY, GCM);
        return c;
    }

    static Cipher openCipher() throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, KEY, GCM);
        return c;
    }

    static Cipher wrongOpenCipher() throws Exception {
        Cipher c = Cipher.getInstance("AES/GCM/NoPadding");
        c.init(Cipher.DECRYPT_MODE, WRONG_KEY, GCM);
        return c;
    }

    // small serializable value type with value semantics
    static class Point implements Serializable {
        private static final long serialVersionUID = 1L;
        final int x; final int y; final String label;
        Point(int x, int y, String label) { this.x = x; this.y = y; this.label = label; }
        @Override public boolean equals(Object o) {
            if (!(o instanceof Point)) return false;
            Point p = (Point) o;
            return p.x == x && p.y == y && label.equals(p.label);
        }
        @Override public int hashCode() { return 31 * x + y; }
    }

    // subclass exposing the protected SealedObject(SealedObject) copy constructor
    static class CopiedSealed extends SealedObject {
        CopiedSealed(SealedObject so) { super(so); }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.SealedObject");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "concrete class");
        boolean serial = false;
        for (Class<?> i : c.getInterfaces())
            if (i.getName().equals("java.io.Serializable")) serial = true;
        check(serial, "implements java.io.Serializable");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "direct superclass java.lang.Object");
        boolean pubCtor = false, protCtor = false;
        for (Constructor<?> k : c.getDeclaredConstructors()) {
            Class<?>[] ps = k.getParameterTypes();
            if (Modifier.isPublic(k.getModifiers()) && ps.length == 2
                    && ps[0].getName().equals("java.io.Serializable")
                    && ps[1].getName().equals("javax.crypto.Cipher")) pubCtor = true;
            if (Modifier.isProtected(k.getModifiers()) && ps.length == 1
                    && ps[0].getName().equals("javax.crypto.SealedObject")) protCtor = true;
        }
        check(pubCtor, "public (Serializable, Cipher) constructor");
        check(protCtor, "protected (SealedObject) copy constructor");
        String[] finals = {"getAlgorithm", "getObject"};
        for (String n : finals) {
            boolean found = false;
            for (Method m : c.getDeclaredMethods())
                if (m.getName().equals(n)) found = Modifier.isFinal(m.getModifiers());
            check(found, "final method " + n);
        }
        boolean protField = false;
        for (java.lang.reflect.Field f : c.getDeclaredFields())
            if (f.getName().equals("encodedParams") && Modifier.isProtected(f.getModifiers())) protField = true;
        check(protField, "protected byte[] encodedParams field");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        legacyAesEcbRoundTrip();
        gcmRoundTrips();
        failurePaths();
        serializationAndCopy();
    }

    // legacy flow from the previous shallow test: plain "AES" cipher, zero key
    static void legacyAesEcbRoundTrip() throws Exception {
        Cipher aes = Cipher.getInstance("AES");
        SecretKeySpec key = new SecretKeySpec(new byte[16], "AES");
        aes.init(Cipher.ENCRYPT_MODE, key);
        SealedObject so = new SealedObject("secret", aes);
        check("AES".equals(so.getAlgorithm()), "getAlgorithm");
        Cipher d = Cipher.getInstance("AES");
        d.init(Cipher.DECRYPT_MODE, key);
        check("secret".equals(so.getObject(d)), "getObject(cipher)");
        check("secret".equals(so.getObject(key)), "getObject(key)");
    }

    // GCM: fixed key+IV, string and custom objects, all three getObject overloads
    static void gcmRoundTrips() throws Exception {
        SealedObject so = new SealedObject("JCK-sealed--object", sealCipher());
        check("AES/GCM/NoPadding".equals(so.getAlgorithm()), "getAlgorithm returns sealing transformation");

        check("JCK-sealed--object".equals(so.getObject((Key) KEY)), "getObject(Key) round trip");
        check("JCK-sealed--object".equals(so.getObject(KEY, "SunJCE")), "getObject(Key,String) round trip");
        check("JCK-sealed--object".equals(so.getObject(openCipher())), "getObject(Cipher) round trip");

        // unsealing twice yields an equal (fresh) object each time
        Object a = so.getObject(KEY);
        Object b = so.getObject(KEY);
        check(a != b && a.equals(b), "each unseal deserializes a fresh object");

        // custom serializable class with value semantics
        Point p = new Point(7, -3, "origin-marker");
        SealedObject soPoint = new SealedObject(p, sealCipher());
        Object recovered = soPoint.getObject(KEY);
        check(recovered instanceof Point, "custom class recovered with runtime type");
        check(recovered.equals(new Point(7, -3, "origin-marker")), "custom class field-by-field round trip");

        // sealing null keeps null recoverable
        SealedObject soNull = new SealedObject(null, sealCipher());
        check(soNull.getObject(KEY) == null, "null object seals to null");

        // a block cipher without padding rejects non-block-multiple content
        SecretKeySpec raw = new SecretKeySpec(new byte[16], "AES");
        Cipher ec = Cipher.getInstance("AES/ECB/NoPadding");
        ec.init(Cipher.ENCRYPT_MODE, raw);
        // 15 ASCII chars serialize to 18 bytes: not a multiple of 16
        expect(IllegalBlockSizeException.class, () -> new SealedObject("123456789012345", ec));
    }

    // wrong key / wrong cipher / null key / unknown provider
    static void failurePaths() throws Exception {
        SealedObject so = new SealedObject("tamper-check", sealCipher());

        // wrong key: GCM tag mismatch, wrapped by getObject(Key) as InvalidKeyException
        Exception ex = null;
        try { so.getObject((Key) WRONG_KEY); } catch (Exception e) { ex = e; }
        check(ex instanceof java.security.InvalidKeyException,
                "getObject(wrongKey) throws InvalidKeyException, got " + ex);
        check(ex instanceof GeneralSecurityException, "getObject(wrongKey) failure is a GeneralSecurityException");

        // the same failure surfaces raw from getObject(Cipher)
        expect(AEADBadTagException.class, () -> so.getObject(wrongOpenCipher()));

        // null key is rejected before any crypto happens
        expect(NullPointerException.class, () -> so.getObject((Key) null));
        expect(NullPointerException.class, () -> so.getObject(null, "SunJCE"));

        // unknown provider name
        expect(NoSuchProviderException.class, () -> so.getObject(KEY, "NoSuchProviderXYZ"));
    }

    // SealedObject is itself serializable; the protected copy constructor also clones state
    static void serializationAndCopy() throws Exception {
        SealedObject so = new SealedObject("serial-copy-target", sealCipher());

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(so);
        }
        SealedObject copy = (SealedObject) new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check("AES/GCM/NoPadding".equals(copy.getAlgorithm()), "deserialized sealed object keeps algorithm");
        check("serial-copy-target".equals(copy.getObject(KEY)), "deserialized sealed object still unseals");
        check("serial-copy-target".equals(copy.getObject(openCipher())), "deserialized sealed object unseals via cipher");

        // protected copy constructor
        CopiedSealed cs = new CopiedSealed(so);
        check("AES/GCM/NoPadding".equals(cs.getAlgorithm()), "copy constructor keeps algorithm");
        check("serial-copy-target".equals(cs.getObject(KEY)), "copy constructor keeps sealed content");

        // seal two equal objects under different IVs: both unseal to the same plaintext
        Cipher c1 = Cipher.getInstance("AES/GCM/NoPadding");
        c1.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(128, hex("202122232425262728292a2b")));
        SealedObject s1 = new SealedObject("same", c1);
        Cipher c2 = Cipher.getInstance("AES/GCM/NoPadding");
        c2.init(Cipher.ENCRYPT_MODE, KEY, new GCMParameterSpec(128, hex("303132333435363738393a3b")));
        SealedObject s2 = new SealedObject("same", c2);
        check(s1.getObject(KEY).equals("same") && s2.getObject(KEY).equals("same"),
                "equal plaintexts unseal identically across different IVs");
        check("same".equals(s1.getObject(KEY, "SunJCE")),
                "provider overload uses the parameters stored inside the sealed object");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
