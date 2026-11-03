/*
 * @test
 * @summary java.io.NotActiveException conformance: white-box structure + black-box
 *          behavior (both constructors; real triggers from ObjectOutputStream and
 *          ObjectInputStream for every serialization call that requires an active
 *          (de)serialization context).
 * @run main NotActiveExceptionTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.NotActiveException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NotActiveExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.NotActiveException");
        check(c.getSuperclass().getName().equals("java.io.ObjectStreamException"),
              "extends ObjectStreamException");
        int noArg = 0, str = 0;
        for (Constructor<?> k : c.getConstructors()) {
            if (k.getParameterCount() == 0) noArg++;
            if (k.getParameterCount() == 1 && k.getParameterTypes()[0] == String.class) str++;
        }
        check(noArg == 1 && str == 1, "two public constructors");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. constructors
        check(new NotActiveException().getMessage() == null, "no-arg message null");
        check("not in writeObject".equals(new NotActiveException("not in writeObject").getMessage()),
              "(String) message kept");

        // 2. triggers on the write side: calls needing an active writeObject context
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            for (int i = 0; i < 3; i++) {
                try {
                    switch (i) {
                        case 0 -> oos.defaultWriteObject();
                        case 1 -> oos.putFields();
                        default -> oos.writeFields();
                    }
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
                    switch (i) {
                        case 0 -> ois.defaultReadObject();
                        case 1 -> ois.readFields();
                        default -> ois.registerValidation(() -> {}, 0);
                    }
                    check(false, "read-side call " + i + " must throw NotActiveException");
                } catch (NotActiveException expected) {
                    passed++;
                }
            }
        }

        // 4. the same calls succeed from inside a Serializable class's callbacks
        class Proper implements Serializable {
            @Serial
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
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
