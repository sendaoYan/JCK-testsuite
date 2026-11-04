/*
 * @test
 * @summary javax.crypto.AEADBadTagException conformance: white-box structure + black-box behavior.
 * @run main AEADBadTagExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AEADBadTagExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> cls = Class.forName("javax.crypto.AEADBadTagException");
        int mods = cls.getModifiers();
        check(Modifier.isPublic(mods), "class is public");
        check(!Modifier.isInterface(mods) && !Modifier.isAbstract(mods), "class is concrete");
        check(!Modifier.isFinal(mods), "class is not final");
        check(cls.getSuperclass().getName().equals("javax.crypto.BadPaddingException"),
                "direct superclass is javax.crypto.BadPaddingException");
        check(Exception.class.isAssignableFrom(cls), "assignable to java.lang.Exception (checked)");
        check(!RuntimeException.class.isAssignableFrom(cls), "not assignable to RuntimeException");
        check(java.io.Serializable.class.isAssignableFrom(cls), "Serializable (inherited from Throwable)");
        check(cls.getDeclaredMethods().length == 0,
                "declares no methods of its own (behavior inherited from Throwable chain)");
        Field[] fs = cls.getDeclaredFields();
        check(fs.length == 1 && fs[0].getName().equals("serialVersionUID"),
                "single declared field serialVersionUID");
        check(fs[0].getType() == long.class && Modifier.isPrivate(fs[0].getModifiers())
                && Modifier.isStatic(fs[0].getModifiers()) && Modifier.isFinal(fs[0].getModifiers()),
                "serialVersionUID is private static final long");
        Constructor<?>[] cs = cls.getDeclaredConstructors();
        check(cs.length == 2, "exactly two declared constructors");
        boolean noArg = false, strArg = false;
        for (Constructor<?> c : cs) {
            check(Modifier.isPublic(c.getModifiers()), "constructor is public");
            if (c.getParameterCount() == 0) noArg = true;
            if (c.getParameterCount() == 1 && c.getParameterTypes()[0] == String.class) strArg = true;
        }
        check(noArg && strArg, "constructor set is exactly {(), (java.lang.String)}");
        check(ObjectStreamClass.lookup(cls).getSerialVersionUID() == -488059093241685509L,
                "serialVersionUID value matches JDK source");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor behavior (preserved from earlier shallow test)
        AEADBadTagException e0 = new AEADBadTagException();
        check(e0.getMessage() == null, "no-arg ctor: message is null");
        check(e0.getCause() == null, "no-arg ctor: cause is null");
        AEADBadTagException e1 = new AEADBadTagException("msg");
        check("msg".equals(e1.getMessage()), "String ctor: message is exact");
        check(e1.toString().equals("javax.crypto.AEADBadTagException: msg"),
                "toString is <binary name>: <message>");
        check(e1.getLocalizedMessage() == e1.getMessage(),
                "getLocalizedMessage delegates to getMessage by default");
        check(new AEADBadTagException().getStackTrace().length >= 1,
                "stack trace captured at construction");

        // Cause wiring via inherited initCause (this class has no cause ctor)
        IllegalStateException cause = new IllegalStateException("root");
        check(e1.initCause(cause) == e1, "initCause returns this");
        check(e1.getCause() == cause, "initCause stores the same reference");
        try {
            e1.initCause(new Exception("second"));
            check(false, "second initCause must fail");
        } catch (IllegalStateException expected) {
            check(true, "re-initializing the cause throws IllegalStateException");
        }

        // Suppressed exceptions and stack trace control (inherited Throwable behavior)
        ArithmeticException sup = new ArithmeticException("suppressed");
        e1.addSuppressed(sup);
        check(e1.getSuppressed().length == 1 && e1.getSuppressed()[0] == sup,
                "addSuppressed/getSuppressed round-trip with same reference");
        e1.setStackTrace(new StackTraceElement[]
                { new StackTraceElement("Fake", "m", "Fake.java", 42) });
        check(e1.getStackTrace().length == 1 && e1.getStackTrace()[0].getClassName().equals("Fake")
                        && e1.getStackTrace()[0].getLineNumber() == 42,
                "setStackTrace/getStackTrace round-trip");

        // Inheritance-chain behavior (preserved from earlier shallow test)
        check(e1 instanceof BadPaddingException, "extends javax.crypto.BadPaddingException");
        check(e1 instanceof GeneralSecurityException, "is java.security.GeneralSecurityException");
        Exception up = e1;
        check(up instanceof AEADBadTagException, "upcast to Exception keeps specific instanceof");
        try {
            throw e1;
        } catch (BadPaddingException bySuper) {
            check(bySuper == e1, "thrown instance caught by BadPaddingException handler, same object");
        }
        try {
            throw e0;
        } catch (Exception byException) {
            check(byException == e0, "thrown instance caught by Exception handler");
        }

        // Serialization round-trip (Throwable is Serializable, serialVersionUID fixed)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(e1);
        }
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof AEADBadTagException && "msg".equals(((Throwable) copy).getMessage()),
                "serialization preserves class and message");

        // Real trigger chain: AES/GCM tag verification failure on tampered ciphertext.
        // Deterministic: fixed key, fixed 12-byte IV, fixed 128-bit tag, fixed plaintext.
        byte[] keyBytes = new byte[16];
        for (int i = 0; i < 16; i++) keyBytes[i] = (byte) i;
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        byte[] iv = new byte[12];
        for (int i = 0; i < 12; i++) iv[i] = (byte) (0x40 + i);
        byte[] pt = "attack at dawn!".getBytes(StandardCharsets.UTF_8);   // 15 bytes
        Cipher enc = Cipher.getInstance("AES/GCM/NoPadding");
        enc.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, iv));
        byte[] ct = enc.doFinal(pt);
        check(ct.length == pt.length + 16, "GCM ciphertext length = plaintext + 16-byte tag");
        Cipher dec = Cipher.getInstance("AES/GCM/NoPadding");
        dec.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
        check(Arrays.equals(pt, dec.doFinal(ct)), "untampered ciphertext round-trips");
        byte[] tamperedBody = ct.clone();  tamperedBody[0] ^= 0x01;
        byte[] tamperedTag  = ct.clone();  tamperedTag[ct.length - 1] ^= 0x01;
        for (byte[] bad : new byte[][] { tamperedBody, tamperedTag }) {
            Cipher d = Cipher.getInstance("AES/GCM/NoPadding");
            d.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(128, iv));
            try {
                d.doFinal(bad);
                check(false, "tampered GCM ciphertext must fail tag verification");
            } catch (BadPaddingException bySuper) {  // nearest crypto supertype that declares it
                check(bySuper.getClass() == AEADBadTagException.class,
                        "tag mismatch surfaces as exact AEADBadTagException, "
                                + "catchable as BadPaddingException");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
