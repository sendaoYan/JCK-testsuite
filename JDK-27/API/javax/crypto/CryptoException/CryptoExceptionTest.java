/*
 * @test
 * @summary javax.crypto.CryptoException conformance: white-box structure + black-box behavior.
 * @enablePreview
 * @run main CryptoExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.crypto.CryptoException;
import javax.crypto.EncryptedPrivateKeyInfo;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class CryptoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // CryptoException extends RuntimeException, so this compiles with no throws clause;
    // if the class ever became checked this method would not compile.
    static RuntimeException throwUnchecked() {
        throw new CryptoException("unchecked");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> cls = Class.forName("javax.crypto.CryptoException");
        int mods = cls.getModifiers();
        check(Modifier.isPublic(mods), "class is public");
        check(Modifier.isFinal(mods), "class is final");
        check(!Modifier.isInterface(mods) && !Modifier.isAbstract(mods), "class is concrete");
        check(cls.getSuperclass().getName().equals("java.lang.RuntimeException"),
                "direct superclass is java.lang.RuntimeException");
        check(RuntimeException.class.isAssignableFrom(cls), "assignable to RuntimeException (unchecked)");
        check(!java.security.GeneralSecurityException.class.isAssignableFrom(cls),
                "NOT a GeneralSecurityException: unchecked alternative per javadoc");
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
        check(cs.length == 4, "exactly four declared constructors");
        boolean noArg = false, strArg = false, strThr = false, thr = false;
        for (Constructor<?> c : cs) {
            check(Modifier.isPublic(c.getModifiers()), "constructor is public");
            Class<?>[] ps = c.getParameterTypes();
            if (ps.length == 0) noArg = true;
            if (ps.length == 1 && ps[0] == String.class) strArg = true;
            if (ps.length == 1 && ps[0] == Throwable.class) thr = true;
            if (ps.length == 2 && ps[0] == String.class && ps[1] == Throwable.class) strThr = true;
        }
        check(noArg && strArg && strThr && thr,
                "constructor set is exactly {(), (String), (String,Throwable), (Throwable)}");
        check(ObjectStreamClass.lookup(cls).getSerialVersionUID() == -6824337376392797817L,
                "serialVersionUID value matches JDK source");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor matrix: all four constructors (preserved from earlier shallow test)
        CryptoException e0 = new CryptoException();
        check(e0.getMessage() == null, "no-arg ctor: message is null");
        check(e0.getCause() == null, "no-arg ctor: cause is null");
        CryptoException e1 = new CryptoException("msg");
        check("msg".equals(e1.getMessage()), "String ctor: message is exact");
        check(e1.toString().equals("javax.crypto.CryptoException: msg"),
                "toString is <binary name>: <message>");
        check(e1.getLocalizedMessage() == e1.getMessage(),
                "getLocalizedMessage delegates to getMessage by default");
        Throwable cause = new Throwable("cause");
        CryptoException e2 = new CryptoException("m", cause);
        check("m".equals(e2.getMessage()), "(String,Throwable) ctor: message is exact");
        check(e2.getCause() == cause, "(String,Throwable) ctor: cause is the same reference");
        CryptoException e3 = new CryptoException(cause);
        check(e3.getCause() == cause, "(Throwable) ctor: cause is the same reference");
        check(cause.toString().equals(e3.getMessage()),
                "(Throwable) ctor: message is cause.toString()");
        check(new CryptoException("m", null).getCause() == null,
                "(String,null) permitted: cause stays null");
        check(new CryptoException().getStackTrace().length >= 1,
                "stack trace captured at construction");

        // Unchecked nature: thrown from a helper with no throws clause, caught as RuntimeException
        try {
            throwUnchecked();
            check(false, "throwUnchecked must throw");
        } catch (RuntimeException bySuper) {
            check(bySuper.getClass() == CryptoException.class,
                    "thrown instance caught as RuntimeException, exact type preserved");
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
        check(e1 instanceof RuntimeException, "is RuntimeException");
        Exception up = e1;
        check(up instanceof CryptoException, "upcast to Exception keeps specific instanceof");
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
        check(copy instanceof CryptoException && "msg".equals(((Throwable) copy).getMessage()),
                "serialization preserves class and message");

        // Real trigger chain: EncryptedPrivateKeyInfo.encrypt(...) wraps provider failures in
        // CryptoException (JDK-27 src, java.base/javax/crypto/EncryptedPrivateKeyInfo.java,
        // encryptImpl: "throw new CryptoException(e)"). Fixed key and inputs; fully deterministic.
        SecretKey aes = new SecretKeySpec(new byte[16], "AES");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(new byte[] { 0x30, (byte) 0x82, 0x01, 0x00 });
        try {
            EncryptedPrivateKeyInfo.encrypt(spec, aes, "NoSuchCipher/XYZ/NoPadding", null, null, null);
            check(false, "unsupported cipher algorithm must fail");
        } catch (CryptoException ce) {
            check(ce.getCause() instanceof NoSuchAlgorithmException,
                    "wraps NoSuchAlgorithmException raised by Cipher.getInstance");
        }
        try {
            EncryptedPrivateKeyInfo.encrypt(spec, new SecretKeySpec(new byte[7], "AES"),
                    "AES/GCM/NoPadding", null, null, null);
            check(false, "invalid key must fail");
        } catch (CryptoException ce) {
            check(ce.getCause() instanceof InvalidKeyException,
                    "wraps InvalidKeyException raised by Cipher.init");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
