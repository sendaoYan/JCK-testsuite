/*
 * @test
 * @summary javax.crypto.BadPaddingException conformance: white-box structure + black-box behavior.
 * @run main BadPaddingExceptionTest
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
import java.util.Arrays;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class BadPaddingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> cls = Class.forName("javax.crypto.BadPaddingException");
        int mods = cls.getModifiers();
        check(Modifier.isPublic(mods), "class is public");
        check(!Modifier.isInterface(mods) && !Modifier.isAbstract(mods), "class is concrete");
        check(!Modifier.isFinal(mods), "class is not final (AEADBadTagException extends it)");
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
        check(ObjectStreamClass.lookup(cls).getSerialVersionUID() == -5315033893984728443L,
                "serialVersionUID value matches JDK source");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor behavior (preserved from earlier shallow test)
        BadPaddingException e0 = new BadPaddingException();
        check(e0.getMessage() == null, "no-arg ctor: message is null");
        check(e0.getCause() == null, "no-arg ctor: cause is null");
        BadPaddingException e1 = new BadPaddingException("msg");
        check("msg".equals(e1.getMessage()), "String ctor: message is exact");
        check(e1.toString().equals("javax.crypto.BadPaddingException: msg"),
                "toString is <binary name>: <message>");
        check(e1.getLocalizedMessage() == e1.getMessage(),
                "getLocalizedMessage delegates to getMessage by default");
        check(new BadPaddingException().getStackTrace().length >= 1,
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
        check(up instanceof BadPaddingException, "upcast to Exception keeps specific instanceof");
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
        check(copy instanceof BadPaddingException && "msg".equals(((Throwable) copy).getMessage()),
                "serialization preserves class and message");

        // Real trigger chain: deterministic bad padding under AES/ECB/PKCS5Padding.
        // Encrypt 16 zero bytes -> two blocks C1||C2 (PKCS5 always appends a full pad block).
        // Forging C1||C1 decrypts to zeros||zeros whose last byte 0x00 is an invalid pad length,
        // which deterministically rejects as BadPaddingException.
        byte[] keyBytes = new byte[16];
        for (int i = 0; i < 16; i++) keyBytes[i] = (byte) i;
        SecretKey key = new SecretKeySpec(keyBytes, "AES");
        Cipher oracle = Cipher.getInstance("AES/ECB/PKCS5Padding");
        oracle.init(Cipher.ENCRYPT_MODE, key);
        byte[] ct = oracle.doFinal(new byte[16]);
        check(ct.length == 32, "16-byte plaintext encrypts to exactly two AES blocks under PKCS5Padding");
        Cipher dec = Cipher.getInstance("AES/ECB/PKCS5Padding");
        dec.init(Cipher.DECRYPT_MODE, key);
        check(Arrays.equals(dec.doFinal(ct), new byte[16]),
                "untampered ciphertext unpads to the original 16 zero bytes");
        byte[] forged = new byte[32];
        System.arraycopy(ct, 0, forged, 0, 16);
        System.arraycopy(ct, 0, forged, 16, 16);            // C1 || C1
        try {
            dec.doFinal(forged);
            check(false, "forged ciphertext with 0x00 final pad byte must be rejected");
        } catch (BadPaddingException bpe) {
            check(bpe.getClass() == BadPaddingException.class,
                    "invalid PKCS#5 padding surfaces as exact BadPaddingException");
        }
        // Boundary contrast: a non-block-multiple input is IllegalBlockSizeException, not BadPadding
        try {
            dec.doFinal(Arrays.copyOf(ct, 15));
            check(false, "15-byte ciphertext must fail");
        } catch (IllegalBlockSizeException ibe) {
            check(ibe instanceof GeneralSecurityException,
                    "truncated input reports IllegalBlockSizeException (not BadPaddingException)");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
