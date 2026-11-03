/*
 * @test
 * @summary java.io.InvalidObjectException conformance: white-box structure + black-box
 *          behavior (constructors incl. cause; real triggers: an ObjectInputValidation
 *          callback rejecting the graph, and readUnshared over a back-reference).
 * @run main InvalidObjectExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputValidation;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class InvalidObjectExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    static class Rejected implements Serializable {
        @Serial
        private static final long serialVersionUID = 710L;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    throw new InvalidObjectException("invariant broken");
                }
            }, 0);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.InvalidObjectException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        int ctors = 0;
        for (Constructor<?> k : c.getConstructors()) ctors++;
        check(ctors == 2, "two public constructors");
        check(!RuntimeException.class.isAssignableFrom(c), "checked exception");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        InvalidObjectException e = new InvalidObjectException("bad object");
        check("bad object".equals(e.getMessage()) && e.getCause() == null, "(String) message");
        Exception cause = new IllegalStateException("root");
        InvalidObjectException e2 = new InvalidObjectException("bad2", cause);
        check("bad2".equals(e2.getMessage()) && e2.getCause() == cause,
              "(String, Throwable) carries the cause");

        // 2. real trigger: registerValidation callback throwing InvalidObjectException
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Rejected())))) {
            ois.readObject();
            check(false, "a rejecting validation must abort readObject");
        } catch (InvalidObjectException ioe) {
            check("invariant broken".equals(ioe.getMessage()),
                  "validation exception propagated with its message");
        }

        // 3. real trigger: reading a back-reference through readUnshared
        String shared = "dup";
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(shared);
            oos.writeObject(shared);      // second write is a back-reference
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            check("dup".equals(ois.readObject()), "first read ok");
            try {
                ois.readUnshared();
                check(false, "back-reference read as unshared must fail");
            } catch (InvalidObjectException expected) {
                passed++;
            }
        }

        // 4. caught as ObjectStreamException and IOException
        try {
            throw new InvalidObjectException("x");
        } catch (java.io.IOException io) {
            check(io instanceof InvalidObjectException, "catchable as IOException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
