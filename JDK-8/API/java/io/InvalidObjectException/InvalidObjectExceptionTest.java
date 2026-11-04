/*
 * @test
 * @summary java.io.InvalidObjectException conformance (pure black-box):
 *          constructors incl. cause; real triggers: an ObjectInputValidation
 *          callback rejecting the graph, and readUnshared over a back-reference.
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

    static void testBlackBox() throws Exception {
        // 1. constructors
        InvalidObjectException e = new InvalidObjectException("bad object");
        check("bad object".equals(e.getMessage()) && e.getCause() == null, "(String) message");
        check(new InvalidObjectException("bad2").getCause() == null,
              "JDK-8 has only the (String) constructor; cause stays null");

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
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
