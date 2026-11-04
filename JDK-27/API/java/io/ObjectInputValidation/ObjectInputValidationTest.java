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
    static Object de(byte[] b) throws Exception {
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(b));
        Object r = ois.readObject();
        ois.close();
        return r;
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


    // ================= deep additions (contract level) =================

    static class VInner implements Serializable {
        private static final long serialVersionUID = 770L;   // fixed for cross-version stability
        int v;
    }

    static class VNested implements Serializable {
        private static final long serialVersionUID = 771L;
        VInner inner;
    }

    /** validateObject asserts the whole graph is already restored. */
    static class VGraph implements Serializable {
        private static final long serialVersionUID = 772L;
        VNested n;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    if (n == null || n.inner == null || n.inner.v != 77)
                        throw new InvalidObjectException("graph incomplete at callback");
                }
            }, 5);
        }
    }

    /** Full priority matrix, registered in shuffled order. */
    static class VPrio implements Serializable {
        private static final long serialVersionUID = 773L;
        static final java.util.List<String> log = new java.util.ArrayList<String>();
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("neg7"); } }, -7);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("mid5"); } }, 5);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("top10"); } }, 10);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("zero0"); } }, 0);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("one1"); } }, 1);
        }
    }

    /** registerValidation(null, ...) is rejected during the read. */
    static class VNull implements Serializable {
        private static final long serialVersionUID = 774L;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(null, 0);
        }
    }

    /** A callback throwing an unchecked exception aborts readObject with it. */
    static class VRt implements Serializable {
        private static final long serialVersionUID = 775L;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { throw new IllegalStateException("vrboom"); } }, 3);
        }
    }

    /** InvalidObjectException with a cause chain passes through untouched. */
    static class VCause implements Serializable {
        private static final long serialVersionUID = 776L;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() throws InvalidObjectException {
                    InvalidObjectException ioe = new InvalidObjectException("wrapped");
                    try { throw new IllegalStateException("root"); }
                    catch (IllegalStateException rt) { ioe.initCause(rt); }
                    throw ioe;
                } }, 3);
        }
    }

    /** Validates exactly once per readObject call. */
    static class VOnce implements Serializable {
        private static final long serialVersionUID = 777L;
        static int count;
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { count++; } }, 0);
        }
    }

    /** Equal priorities run in reverse registration order (LIFO), verified stable. */
    static class VEq implements Serializable {
        private static final long serialVersionUID = 778L;
        static final java.util.List<String> log = new java.util.ArrayList<String>();
        private void readObject(ObjectInputStream s)
                throws java.io.IOException, ClassNotFoundException {
            s.defaultReadObject();
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("a"); } }, 2);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("b"); } }, 2);
            s.registerValidation(new ObjectInputValidation() {
                public void validateObject() { log.add("c"); } }, 2);
        }
    }

    // ---- equal priorities: reverse registration order ----
    static void testEqualPriorityLifo() throws Exception {
        VEq.log.clear();
        ObjectInputStream ois =
                new ObjectInputStream(new java.io.ByteArrayInputStream(ser(new VEq())));
        ois.readObject();
        ois.close();
        check(VEq.log.size() == 3, "all equal-priority callbacks fired: " + VEq.log);
        check("c".equals(VEq.log.get(0)), "last registered runs first (LIFO): " + VEq.log);
        check("b".equals(VEq.log.get(1)), "middle registration second: " + VEq.log);
        check("a".equals(VEq.log.get(2)), "first registered runs last: " + VEq.log);
    }

    // ---- interface structure ----
    static void testInterfaceStructure() throws Exception {
        Class<?> c = ObjectInputValidation.class;
        check(c.isInterface(), "ObjectInputValidation is an interface");
        check(c.getDeclaredMethods().length == 1, "exactly one method");
        java.lang.reflect.Method m = c.getMethod("validateObject");
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "validateObject is abstract");
        check(m.getReturnType() == void.class, "validateObject returns void");
        check(m.getExceptionTypes().length == 1
              && m.getExceptionTypes()[0] == InvalidObjectException.class,
              "declares exactly throws InvalidObjectException");
        check(c.getFields().length == 0, "no constants");
        check(ObjectInputStream.class.getMethod("registerValidation", c, int.class) != null,
              "ObjectInputStream.registerValidation takes an ObjectInputValidation");
    }

    // ---- strict descending priority order, negatives last ----
    static void testPriorityMatrix() throws Exception {
        VPrio.log.clear();
        ObjectInputStream ois =
                new ObjectInputStream(new java.io.ByteArrayInputStream(ser(new VPrio())));
        ois.readObject();
        ois.close();
        check(VPrio.log.size() == 5, "all five callbacks fired: " + VPrio.log);
        check("top10".equals(VPrio.log.get(0)), "highest priority (10) first");
        check("mid5".equals(VPrio.log.get(1)), "priority 5 second");
        check("one1".equals(VPrio.log.get(2)), "priority 1 third");
        check("zero0".equals(VPrio.log.get(3)), "priority 0 fourth");
        check("neg7".equals(VPrio.log.get(4)), "negative priority last");
    }

    // ---- the object graph is complete when validateObject runs ----
    static void testGraphCompleteAtCallback() throws Exception {
        VGraph g = new VGraph();
        g.n = new VNested();
        g.n.inner = new VInner();
        g.n.inner.v = 77;
        VGraph back = (VGraph) de(ser(g));            // throws if the graph were incomplete
        check(back != null && back.n != null && back.n.inner != null,
              "nested graph instances present after read");
        check(back.n.inner.v == 77, "callback saw a fully restored nested graph");
    }

    // ---- null callback ----
    static void testNullCallback() throws Exception {
        try {
            de(ser(new VNull()));
            check(false, "null callback must be rejected");
        } catch (InvalidObjectException e) {
            check("null callback".equals(e.getMessage()),
                  "InvalidObjectException 'null callback': " + e.getMessage());
        }
    }

    // ---- exception semantics from validateObject ----
    static void testExceptionPropagation() throws Exception {
        try {
            de(ser(new VRt()));
            check(false, "runtime exception from callback must propagate");
        } catch (IllegalStateException e) {
            check("vrboom".equals(e.getMessage()), "unchecked exception propagates unchanged");
        }
        try {
            de(ser(new VCause()));
            check(false, "InvalidObjectException from callback must propagate");
        } catch (InvalidObjectException e) {
            check("wrapped".equals(e.getMessage()), "IOE propagates with its message");
            check(e.getCause() instanceof IllegalStateException
                  && "root".equals(e.getCause().getMessage()),
                  "cause chain preserved through the callback");
        }
    }

    // ---- validation is scoped to a single readObject call ----
    static void testPerReadScope() throws Exception {
        VOnce.count = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(new VOnce());
        oos.writeObject(new VOnce());
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        ois.readObject();
        check(VOnce.count == 1, "one callback after the first readObject: " + VOnce.count);
        ois.readObject();
        ois.close();
        check(VOnce.count == 2, "callback fired exactly once per readObject: " + VOnce.count);
        try {
            ois.registerValidation(new ObjectInputValidation() {
                public void validateObject() { } }, 0);
            check(false, "registerValidation after reads must throw");
        } catch (java.io.NotActiveException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testInterfaceStructure();
        testPriorityMatrix();
        testGraphCompleteAtCallback();
        testNullCallback();
        testExceptionPropagation();
        testPerReadScope();
        testEqualPriorityLifo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
