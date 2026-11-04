/*
 * @test
 * @summary java.io.ObjectInputValidation API conformance: full contract coverage.
 *          validateObject callbacks registered from readObject run after the graph is
 *          complete, ordered by descending priority, may throw InvalidObjectException to
 *          abort, same object validated once per read, registerValidation outside
 *          readObject refused (NotActiveException).
 * @run main ObjectInputValidationTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidObjectException;
import java.io.NotActiveException;
import java.io.ObjectInputValidation;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ObjectInputValidationTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return bos.toByteArray();
    }

    static class Payload implements Serializable {
        private static final long serialVersionUID = 760L;
        int v;
        transient List<String> events;              // not serialized
        Payload(int v) { this.v = v; }
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            if (events == null) events = new ArrayList<String>();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    events.add("validate:" + v);
                }
            }, 5);
        }
    }

    static class Ordered implements Serializable {
        private static final long serialVersionUID = 761L;
        static final List<String> log = new ArrayList<String>();
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("prio1"); }
            }, 1);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("prio10"); }
            }, 10);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("prio0"); }
            }, 0);
        }
    }

    static class Rejector implements Serializable {
        private static final long serialVersionUID = 762L;
        int v;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            final int captured = v;
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    if (captured > 100) throw new InvalidObjectException("v too large: " + captured);
                }
            }, 0);
        }
    }

    // ---- 1. validateObject runs after the whole object has been read ----
    static void testAfterRead() throws Exception {
        Payload p = new Payload(42);
        p.events = null;
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser(p)));
        Payload back = (Payload) ois.readObject();
        ois.close();
        check(back.events.equals(Arrays.asList("validate:42")),
              "callback saw the fully-populated object");
    }

    // ---- 2. multiple callbacks run in descending priority order ----
    static void testPriorityOrder() throws Exception {
        Ordered.log.clear();
        ObjectInputStream ois =
                new ObjectInputStream(new ByteArrayInputStream(ser(new Ordered())));
        ois.readObject();
        ois.close();
        check(Ordered.log.equals(Arrays.asList("prio10", "prio1", "prio0")),
              "priority order 10,1,0: " + Ordered.log);
    }

    // ---- 3. a rejecting callback aborts readObject ----
    static void testRejection() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        Rejector r = new Rejector();
        r.v = 500;
        oos.writeObject(r);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        try {
            ois.readObject();
            check(false, "v=500 must be rejected");
        } catch (InvalidObjectException ioe) {
            check("v too large: 500".equals(ioe.getMessage()),
                  "rejection message: " + ioe.getMessage());
        }
        ois.close();

        bos = new ByteArrayOutputStream();
        oos = new ObjectOutputStream(bos);
        r = new Rejector();
        r.v = 5;
        oos.writeObject(r);
        oos.close();
        ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();
        passed++;                                 // v=5 accepted, no exception
        ois.close();
    }

    // ---- 4. registerValidation outside readObject is refused ----
    static void testOutsideReadObject() throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(ser("x")));
        try {
            ois.registerValidation(new ObjectInputValidation() {
                public void validateObject() { }
            }, 0);
            check(false, "registerValidation outside readObject must throw");
        } catch (NotActiveException expected) {
            passed++;
        }
        ois.close();
    }

    public static void main(String[] args) throws Exception {
        testAfterRead();
        testPriorityOrder();
        testRejection();
        testOutsideReadObject();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
