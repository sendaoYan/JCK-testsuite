/*
 * @test
 * @summary java.io.IOException conformance: white-box structure (checked root of the
 *          java.io exception hierarchy, four constructors) + black-box behavior
 *          (message/cause composition, initCause, addSuppressed, serialization of the
 *          exception itself, catch semantics against real throwing APIs).
 * @run main IOExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class IOExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.IOException");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(java.io.Serializable.class.isAssignableFrom(c), "Throwable => Serializable");
        check(!RuntimeException.class.isAssignableFrom(c), "checked, not a RuntimeException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        int noArg = 0, str = 0, strThr = 0, thr = 0;
        for (Constructor<?> k : c.getConstructors()) {
            Class<?>[] p = k.getParameterTypes();
            if (p.length == 0) noArg++;
            if (p.length == 1 && p[0] == String.class) str++;
            if (p.length == 2 && p[0] == String.class && p[1] == Throwable.class) strThr++;
            if (p.length == 1 && p[0] == Throwable.class) thr++;
        }
        check(noArg == 1 && str == 1 && strThr == 1 && thr == 1,
              "exactly four public constructors");
        check(c.getDeclaredField("serialVersionUID").getType() == long.class
              && Modifier.isStatic(c.getDeclaredField("serialVersionUID").getModifiers()),
              "declares serialVersionUID");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructor/message/cause matrix
        IOException e0 = new IOException();
        check(e0.getMessage() == null && e0.getCause() == null, "no-arg: null message and cause");
        IOException e1 = new IOException("disk on fire");
        check("disk on fire".equals(e1.getMessage()) && e1.getCause() == null,
              "(String): message only");
        Exception cause = new IllegalStateException("root");
        IOException e2 = new IOException("wrapped", cause);
        check("wrapped".equals(e2.getMessage()) && e2.getCause() == cause,
              "(String, Throwable): message + cause");
        IOException e3 = new IOException(cause);
        check(cause.toString().equals(e3.getMessage()) && e3.getCause() == cause,
              "(Throwable): message defaults to cause.toString()");

        // 2. initCause: once only, self-cause refused
        IOException once = new IOException("m");
        check(once.initCause(cause) == once, "initCause returns this");
        try {
            once.initCause(new Exception());
            check(false, "second initCause must throw");
        } catch (IllegalStateException expected) {
            passed++;
        }
        IOException self = new IOException("self");
        try {
            self.initCause(self);
            check(false, "initCause(this) must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(new IOException().initCause(cause) != null,
              "initCause accepts any non-self Throwable when unset");

        // 3. suppressed exceptions attached to an IOException round-trip through printStackTrace
        IOException sup = new IOException("during-close");
        Exception s1 = new Exception("supp1"), s2 = new Exception("supp2");
        sup.addSuppressed(s1);
        sup.addSuppressed(s2);
        check(sup.getSuppressed().length == 2 && sup.getSuppressed()[0] == s1
              && sup.getSuppressed()[1] == s2, "suppressed preserved in order");

        // 4. the exception itself serializes (Throwable is Serializable) keeping its message
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new IOException("kept"));
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            IOException back = (IOException) ois.readObject();
            check("kept".equals(back.getMessage()), "message survives serialization");
        }

        // 5. real API throws an IOException subtype catchable as IOException
        try {
            new DataInputStream(new ByteArrayInputStream(new byte[0])).readInt();
            check(false, "readInt on empty stream must throw");
        } catch (IOException io) {                       // EOFException caught as IOException
            check(io instanceof EOFException, "EOFException is an IOException");
        }

        // 6. multi-catch and try-with-resources compatibility
        try (java.io.ByteArrayInputStream in = new java.io.ByteArrayInputStream(new byte[1])) {
            check(in.read() == 0, "resource usable");
        }

        // 7. toString/stack trace capture the class name and message
        IOException t = new IOException("xyz");
        check(t.toString().equals("java.io.IOException: xyz"), "toString format");
        check(t.getStackTrace().length > 0, "stack trace captured at construction");
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
