/*
 * @test
 * @summary javax.crypto.NoSuchPaddingException conformance: white-box structure + black-box behavior.
 * @run main NoSuchPaddingExceptionTest
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
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class NoSuchPaddingExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> cls = Class.forName("javax.crypto.NoSuchPaddingException");
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
        check(ObjectStreamClass.lookup(cls).getSerialVersionUID() == -4572885201200175466L,
                "serialVersionUID value matches JDK source");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor behavior (preserved from earlier shallow test)
        NoSuchPaddingException e0 = new NoSuchPaddingException();
        check(e0.getMessage() == null, "no-arg ctor: message is null");
        check(e0.getCause() == null, "no-arg ctor: cause is null");
        NoSuchPaddingException e1 = new NoSuchPaddingException("msg");
        check("msg".equals(e1.getMessage()), "String ctor: message is exact");
        check(e1.toString().equals("javax.crypto.NoSuchPaddingException: msg"),
                "toString is <binary name>: <message>");
        check(e1.getLocalizedMessage() == e1.getMessage(),
                "getLocalizedMessage delegates to getMessage by default");
        check(new NoSuchPaddingException().getStackTrace().length >= 1,
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
        check(up instanceof NoSuchPaddingException, "upcast to Exception keeps specific instanceof");
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
        check(copy instanceof NoSuchPaddingException && "msg".equals(((Throwable) copy).getMessage()),
                "serialization preserves class and message");

        // Real trigger chain: provider-explicit Cipher.getInstance with an unsupported padding.
        // SunJCE advertises SupportedPaddings=NOPADDING|PKCS5Padding|ISO10126Padding for "AES",
        // so any other padding name fails with NoSuchPaddingException
        // (mirrors test/jdk/javax/crypto/Cipher/TestGetInstance.java "XYZPadding").
        Provider sunjce = Security.getProvider("SunJCE");
        check(sunjce != null, "SunJCE provider is present");
        try {
            Cipher.getInstance("AES/CBC/NoSuchPad123", "SunJCE");
            check(false, "unsupported padding must fail (provider-name form)");
        } catch (NoSuchPaddingException nspe) {
            check(nspe.getClass() == NoSuchPaddingException.class, "exact type, provider-name form");
        }
        try {
            Cipher.getInstance("AES/CBC/NoSuchPad123", sunjce);
            check(false, "unsupported padding must fail (provider-object form)");
        } catch (GeneralSecurityException viaSuper) {   // caught by supertype
            check(viaSuper.getClass() == NoSuchPaddingException.class,
                    "exact type, provider-object form, catchable as GeneralSecurityException");
        }
        // Contrast: a padding the provider advertises resolves on the same call shape
        Cipher ok = Cipher.getInstance("AES/CBC/PKCS5Padding", "SunJCE");
        check(ok != null && ok.getProvider().getName().equals("SunJCE"),
                "supported padding resolves through the same provider");
        // Contrast: without an explicit provider the global search fails earlier with
        // NoSuchAlgorithmException ("Cannot find any provider supporting ..."), showing the
        // NoSuchPaddingException contract applies to the provider-scoped lookup
        try {
            Cipher.getInstance("AES/CBC/NoSuchPad123");
            check(false, "1-arg form must fail for unknown padding");
        } catch (NoSuchAlgorithmException expected) {
            check(expected instanceof GeneralSecurityException,
                    "1-arg global search reports NoSuchAlgorithmException instead");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
