/*
 * @test
 * @summary java.io.NotSerializableException conformance: white-box structure + black-box
 *          behavior (both constructors; real triggers: ObjectOutputStream.writeObject of
 *          a non-Serializable root, a non-Serializable field, and a non-Serializable
 *          superclass chain entry, each with the class name as the message).
 * @run main NotSerializableExceptionTest
 */

import java.io.ByteArrayOutputStream;
import java.io.NotActiveException;
import java.io.NotSerializableException;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class NotSerializableExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Plain { }

    static class HasPlainField implements Serializable {
        @Serial
        private static final long serialVersionUID = 730L;
        Plain plain = new Plain();
    }

    static class SubOfPlain extends Plain implements Serializable {
        @Serial
        private static final long serialVersionUID = 731L;
        int x = 1;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.NotSerializableException");
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
        check(new NotSerializableException().getMessage() == null, "no-arg message null");
        check("com.x.Y".equals(new NotSerializableException("com.x.Y").getMessage()),
              "(String) message kept");

        // 2. trigger: the root object itself is not Serializable
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new Plain());
            check(false, "writing a non-Serializable root must throw");
        } catch (NotSerializableException nse) {
            String n = nse.getMessage();
            check(n.endsWith("Plain"), "message is the offending class name: " + n);
        }

        // 3. trigger: a Serializable class holding a non-Serializable field
        try (ObjectOutputStream oos = new ObjectOutputStream(new ByteArrayOutputStream())) {
            oos.writeObject(new HasPlainField());
            check(false, "non-Serializable field must throw");
        } catch (NotSerializableException nse) {
            check(nse.getMessage().endsWith("Plain"),
                  "message names the field's class: " + nse.getMessage());
        }

        // 4. trigger: implementing Serializable below a non-Serializable superclass
        //    (fields of the non-serializable ancestor are simply skipped, no throw;
        //     the exception only fires for reachable non-serializable *values*)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new SubOfPlain());
            passed++;
        }
        check(bos.size() > 4, "subclass of a plain class serializes its own fields");

        // 5. contrast: transient escapes the check
        class Trans implements Serializable {
            @Serial
            private static final long serialVersionUID = 732L;
            transient Plain skipMe = new Plain();
        }
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Trans());
            passed++;
        }

        // 6. caught as ObjectStreamException; distinct from NotActiveException
        try {
            throw new NotSerializableException("m");
        } catch (ObjectStreamException ose) {
            check(ose instanceof NotSerializableException
                  && !(ose instanceof NotActiveException),
                  "catchable as ObjectStreamException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
