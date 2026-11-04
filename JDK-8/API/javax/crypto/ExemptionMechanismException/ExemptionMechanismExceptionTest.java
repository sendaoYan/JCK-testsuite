/*
 * @test
 * @summary javax.crypto.ExemptionMechanismException conformance: white-box structure + black-box behavior.
 * @run main ExemptionMechanismExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.GeneralSecurityException;
import javax.crypto.ExemptionMechanismException;

public class ExemptionMechanismExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // a method whose contract declares the checked exception
    static void thrower(String what) throws ExemptionMechanismException {
        throw new ExemptionMechanismException("mechanism failed: " + what);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.crypto.ExemptionMechanismException");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()) && !Modifier.isFinal(c.getModifiers()),
                "concrete non-final class");
        check(c.getSuperclass().getName().equals("java.security.GeneralSecurityException"),
                "direct superclass java.security.GeneralSecurityException");
        boolean noArg = false, stringArg = false;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg = true;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) stringArg = true;
        }
        check(noArg, "public no-arg constructor");
        check(stringArg, "public (String) constructor");
        // JDK-27 surface (per javap): exactly these two public constructors
        check(c.getConstructors().length == 2, "exactly two public constructors");
        boolean noCauseCtor = false;
        try {
            c.getConstructor(String.class, Throwable.class);
        } catch (NoSuchMethodException e) { noCauseCtor = true; }
        check(noCauseCtor, "no (String,Throwable) constructor in this release");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // legacy assertions: message ctor, hierarchy, no-arg null message
        ExemptionMechanismException e = new ExemptionMechanismException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        check(e instanceof java.security.GeneralSecurityException, "is java.security.GeneralSecurityException");
        check(new ExemptionMechanismException().getMessage() == null, "no-arg message null");

        // no-arg variant: null message and null cause, but usable stack trace
        ExemptionMechanismException empty = new ExemptionMechanismException();
        check(empty.getMessage() == null, "no-arg getMessage null");
        check(empty.getCause() == null, "no-arg getCause null");
        check(empty.getStackTrace() != null && empty.getStackTrace().length > 0, "no-arg captures stack trace");

        // message round-trips through the Throwable accessors
        ExemptionMechanismException m = new ExemptionMechanismException("blob unusable");
        check("blob unusable".equals(m.getMessage()), "getMessage");
        check("blob unusable".equals(m.getLocalizedMessage()), "getLocalizedMessage mirrors message");
        check(m.toString().contains("ExemptionMechanismException") && m.toString().contains("blob unusable"),
                "toString contains class name and message");

        // cause is initially absent but can be supplied through the Throwable API
        check(m.getCause() == null, "cause initially null");
        Throwable root = new IllegalStateException("root cause");
        m.initCause(root);
        check(m.getCause() == root, "initCause records cause");
        check(m.getCause().getMessage().equals("root cause"), "cause message preserved");

        // throw/catch chain through the checked-exception contract
        try {
            thrower("key recovery");
            check(false, "thrower must throw");
        } catch (ExemptionMechanismException ex) {
            check("mechanism failed: key recovery".equals(ex.getMessage()), "thrown exception carries message");
        }
        try {
            thrower("key escrow");
        } catch (GeneralSecurityException ex) {
            check(ex instanceof ExemptionMechanismException, "caught as GeneralSecurityException");
        }

        // suppressed exceptions behave like any other Throwable
        ExemptionMechanismException sup = new ExemptionMechanismException("outer");
        ExemptionMechanismException inner = new ExemptionMechanismException("suppressed");
        sup.addSuppressed(inner);
        check(sup.getSuppressed().length == 1 && sup.getSuppressed()[0] == inner, "addSuppressed/getSuppressed");

        // the exception is serializable and its message survives a round trip
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new ExemptionMechanismException("serialized problem"));
        }
        Object back = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof ExemptionMechanismException, "deserializes to the same class");
        check("serialized problem".equals(((ExemptionMechanismException) back).getMessage()),
                "message preserved across serialization");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
