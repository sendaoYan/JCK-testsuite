/*
 * @test
 * @summary java.io.NotActiveException conformance (pure black-box): both
 *          constructors; real triggers from ObjectOutputStream and
 *          ObjectInputStream for every serialization call that requires an active
 *          (de)serialization context.
 * @run main NotActiveExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;

public class NotActiveExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new NotActiveException().getMessage() == null, "no-arg message null");
        check("not in writeObject".equals(new NotActiveException("not in writeObject").getMessage()),
              "(String) message kept");

        // 2. triggers on the write side: calls needing an active writeObject context
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            for (int i = 0; i < 3; i++) {
                try {
                    if (i == 0) oos.defaultWriteObject();
                    else if (i == 1) oos.putFields();
                    else oos.writeFields();
                    check(false, "write-side call " + i + " must throw NotActiveException");
                } catch (NotActiveException expected) {
                    passed++;
                }
            }
        }

        // 3. triggers on the read side: calls needing an active readObject context
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject("payload");
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            for (int i = 0; i < 3; i++) {
                try {
                    if (i == 0) ois.defaultReadObject();
                    else if (i == 1) ois.readFields();
                    else ois.registerValidation(() -> {}, 0);
                    check(false, "read-side call " + i + " must throw NotActiveException");
                } catch (NotActiveException expected) {
                    passed++;
                }
            }
        }

        // 4. the same calls succeed from inside a Serializable class's callbacks
        class Proper implements Serializable {
            private static final long serialVersionUID = 720L;
            int v;
            boolean writeOk, readOk;
            private void writeObject(ObjectOutputStream out)
                    throws java.io.IOException {
                out.defaultWriteObject();                       // active here
                writeOk = true;
            }
            private void readObject(ObjectInputStream in)
                    throws java.io.IOException, ClassNotFoundException {
                in.defaultReadObject();                         // active here
                readOk = true;
            }
        }
        Proper p = new Proper();
        p.v = 3;
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(p);
        }
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            Proper back = (Proper) ois.readObject();
            check(back.readOk && back.v == 3, "defaultReadObject works inside readObject");
        }
        check(p.writeOk, "defaultWriteObject works inside writeObject");

        // 5. caught as ObjectStreamException
        try {
            throw new NotActiveException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof NotActiveException, "catchable as ObjectStreamException");
        }
    }

    public static void main(String[] args) throws Exception {
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
