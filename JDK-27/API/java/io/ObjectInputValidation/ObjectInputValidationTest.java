/*
 * @test
 * @summary java.io.ObjectInputValidation conformance: white-box structure + black-box
 *          behavior (validateObject callbacks registered from readObject: run after the
 *          graph is complete, ordered by descending priority, may throw
 *          InvalidObjectException to abort, the same object validated once per read).
 * @run main ObjectInputValidationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.ObjectInputValidation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class ObjectInputValidationTest {
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

    static class Payload implements Serializable {
        @Serial
        private static final long serialVersionUID = 760L;
        int v;
        transient List<String> events;              // not serialized
        Payload(int v) { this.v = v; }
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            if (events == null) events = new ArrayList<>();   // set by the reader side
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    events.add("validate:" + v);
                }
            }, 5);
        }
    }

    static class Ordered implements Serializable {
        @Serial
        private static final long serialVersionUID = 761L;
        static final List<String> log = new ArrayList<>();
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(() -> log.add("prio1"), 1);
            s.registerValidation(() -> log.add("prio10"), 10);
            s.registerValidation(() -> log.add("prio0"), 0);
        }
    }

    static class Rejector implements Serializable {
        @Serial
        private static final long serialVersionUID = 762L;
        int v;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            final int captured = v;
            s.registerValidation(() -> {
                if (captured > 100) throw new InvalidObjectException("v too large: " + captured);
            }, 0);
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputValidation");
        check(c.isInterface(), "is an interface");
        Method m = c.getMethod("validateObject");
        check(Modifier.isAbstract(m.getModifiers()), "validateObject abstract");
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == InvalidObjectException.class,
              "declares throws InvalidObjectException");
        check(ObjectInputStream.class.getMethod("registerValidation", c, int.class) != null,
              "ObjectInputStream.registerValidation takes an ObjectInputValidation");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. validateObject runs after the whole object has been read
        Payload p = new Payload(42);
        p.events = null;
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(p)))) {
            Payload back = (Payload) ois.readObject();
            check(back.events.equals(List.of("validate:42")),
                  "callback saw the fully-populated object");
        }

        // 2. multiple callbacks run in descending priority order
        Ordered.log.clear();
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Ordered())))) {
            ois.readObject();
        }
        check(Ordered.log.equals(List.of("prio10", "prio1", "prio0")),
              "priority order 10,1,0: " + Ordered.log);

        // 3. a rejecting callback aborts readObject with InvalidObjectException
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            Rejector r = new Rejector();
            r.v = 500;
            oos.writeObject(r);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            check(false, "v=500 must be rejected");
        } catch (InvalidObjectException ioe) {
            check(ioe.getMessage().equals("v too large: 500"),
                  "rejection message: " + ioe.getMessage());
        }
        bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            Rejector r = new Rejector();
            r.v = 5;
            oos.writeObject(r);
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.readObject();
            passed++;                                 // v=5 accepted, no exception
        }

        // 4. validations registered outside readObject are refused (NotActiveException)
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser("x")))) {
            try {
                ois.registerValidation(() -> {}, 0);
                check(false, "registerValidation outside readObject must throw");
            } catch (java.io.NotActiveException expected) {
                passed++;
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
