/*
 * @test
 * @summary javax.crypto.IllegalBlockSizeException conformance: white-box structure + black-box behavior.
 * @run main IllegalBlockSizeExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class IllegalBlockSizeExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> cls = Class.forName("javax.crypto.IllegalBlockSizeException");
        int mods = cls.getModifiers();
        check(Modifier.isPublic(mods), "class is public");
        check(!Modifier.isInterface(mods) && !Modifier.isAbstract(mods), "class is concrete");
        check(!Modifier.isFinal(mods), "class is not final");
        check(cls.getSuperclass().getName().equals("java.security.GeneralSecurityException"),
                "direct superclass is java.security.GeneralSecurityException");
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
        check(ObjectStreamClass.lookup(cls).getSerialVersionUID() == -1965144811953540392L,
                "serialVersionUID value matches JDK source");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor behavior (preserved from earlier shallow test)
        IllegalBlockSizeException e0 = new IllegalBlockSizeException();
        check(e0.getMessage() == null, "no-arg ctor: message is null");
        check(e0.getCause() == null, "no-arg ctor: cause is null");
        IllegalBlockSizeException e1 = new IllegalBlockSizeException("msg");
        check("msg".equals(e1.getMessage()), "String ctor: message is exact");
        check(e1.toString().equals("javax.crypto.IllegalBlockSizeException: msg"),
                "toString is <binary name>: <message>");
        check(e1.getLocalizedMessage() == e1.getMessage(),
                "getLocalizedMessage delegates to getMessage by default");
        check(new IllegalBlockSizeException().getStackTrace().length >= 1,
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
        check(e1 instanceof GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e1 instanceof GeneralSecurityException, "is java.security.GeneralSecurityException");
        Exception up = e1;
        check(up instanceof IllegalBlockSizeException, "upcast to Exception keeps specific instanceof");
        try {
            throw e1;
        } catch (GeneralSecurityException bySuper) {
            check(bySuper == e1, "thrown instance caught by GeneralSecurityException handler, same object");
        }
        try {
            throw e0;
        } catch (Exception byException) {
            check(byException == e0, "thrown instance caught by Exception handler");
        }

        // Serialization round-trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(e1);
        }
        Object copy = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(copy instanceof IllegalBlockSizeException && "msg".equals(((Throwable) copy).getMessage()),
                "serialization preserves class and message");

        // Real trigger chain: AES/CBC/NoPadding refuses non-block-multiple input (fixed bytes only)
        byte[] keyBytes = new byte[16];
        for (int i = 0; i < 16; i++) keyBytes[i] = (byte) i;
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        byte[] ivBytes = new byte[16];
        for (int i = 0; i < 16; i++) ivBytes[i] = (byte) (0x20 + i);
        IvParameterSpec iv = new IvParameterSpec(ivBytes);

        try {
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, iv);
            c.doFinal(new byte[5]);
            check(false, "5-byte input under NoPadding must fail");
        } catch (IllegalBlockSizeException e) {
            check(e.getClass() == IllegalBlockSizeException.class, "exact type from doFinal(byte[])");
        }

        // update() buffers, doFinal() reports the leftover partial block
        try {
            Cipher c = Cipher.getInstance("AES/CBC/NoPadding");
            c.init(Cipher.ENCRYPT_MODE, key, iv);
            c.update(new byte[5]);
            c.doFinal();
            check(false, "buffered partial block must fail at doFinal()");
        } catch (GeneralSecurityException viaSuper) {   // caught by supertype
            check(viaSuper.getClass() == IllegalBlockSizeException.class,
                    "exact type via update/doFinal split, catchable as GeneralSecurityException");
        }

        // Decrypting a non-block-multiple ciphertext under a padded block cipher also fails
        try {
            Cipher c = Cipher.getInstance("AES/ECB/PKCS5Padding");
            c.init(Cipher.DECRYPT_MODE, key);
            c.doFinal(new byte[15]);
            check(false, "15-byte ciphertext must fail");
        } catch (IllegalBlockSizeException e) {
            check(e instanceof GeneralSecurityException, "decrypt of truncated ciphertext fails too");
        }

        // Contrast: exact block-multiple input succeeds
        Cipher ok = Cipher.getInstance("AES/CBC/NoPadding");
        ok.init(Cipher.ENCRYPT_MODE, key, iv);
        check(ok.doFinal(new byte[16]).length == 16, "block-multiple input succeeds (16 bytes out)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
