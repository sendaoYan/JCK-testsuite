/*
 * @test
 * @summary java.io.ObjectInputFilter conformance: white-box structure + black-box behavior
 *          (checkInput contract against FilterInfo; allowFilter/rejectFilter/merge/
 *          rejectUndecidedClass combinators; wiring into ObjectInputStream with
 *          rejection and with allow-lists; Status enum).
 * @run main ObjectInputFilterTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InvalidClassException;
import java.io.ObjectInputFilter;
import java.io.ObjectInputFilter.FilterInfo;
import java.io.ObjectInputFilter.Status;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.Serial;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ObjectInputFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Widget implements Serializable {
        @Serial
        private static final long serialVersionUID = 500L;
        int v = 3;
    }

    static class Gadget implements Serializable {
        @Serial
        private static final long serialVersionUID = 502L;
        int v = 4;
    }

    static FilterInfo info(final Class<?> c, final long arrayLen, final long depth,
                           final long refs, final long bytes) {
        return new FilterInfo() {
            public Class<?> serialClass() { return c; }
            public long arrayLength() { return arrayLen; }
            public long depth() { return depth; }
            public long references() { return refs; }
            public long streamBytes() { return bytes; }
        };
    }

    static byte[] ser(Object o) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(o);
        }
        return bos.toByteArray();
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputFilter");
        check(c.isInterface(), "is an interface");
        check(Modifier.isAbstract(c.getMethod("checkInput", FilterInfo.class).getModifiers()),
              "checkInput abstract");
        for (String m : new String[]{"allowFilter", "rejectFilter", "merge", "rejectUndecidedClass"})
            check(hasStatic(c, m), "static combinator " + m);
        Class<?> config = Class.forName("java.io.ObjectInputFilter$Config");
        check(Modifier.isFinal(config.getModifiers()), "Config is final");
        check(config.getDeclaredMethods().length > 0, "Config has methods");
        Class<?> status = Class.forName("java.io.ObjectInputFilter$Status");
        check(status.isEnum(), "Status is an enum");
        check(status.getEnumConstants().length == 3, "Status has 3 constants");
        Class<?> fi = Class.forName("java.io.ObjectInputFilter$FilterInfo");
        check(fi.isInterface(), "FilterInfo is an interface");
        for (String m : new String[]{"serialClass", "arrayLength", "depth", "references", "streamBytes"})
            check(has(fi, m), "FilterInfo accessor " + m);
    }

    static boolean hasStatic(Class<?> c, String m) {
        for (Method mm : c.getDeclaredMethods())
            if (mm.getName().equals(m) && Modifier.isStatic(mm.getModifiers())) return true;
        return false;
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. a hand-written filter consults every FilterInfo accessor while a real
        //    object graph is deserialized
        final java.util.List<String> seen = new java.util.ArrayList<>();
        ObjectInputFilter recording = f -> {
            if (f.serialClass() == Widget.class) {
                seen.add("widget:arr=" + f.arrayLength() + ",depth=" + f.depth()
                         + ",refs=" + f.references() + ",bytes=" + f.streamBytes());
            }
            return Status.ALLOWED;
        };
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Widget())))) {
            ois.setObjectInputFilter(recording);
            Widget w = (Widget) ois.readObject();
            check(w.v == 3, "allowed object read");
            check(!seen.isEmpty(), "filter invoked for the object class");
            String first = seen.get(0);
            check(first.contains("arr=-1") && first.contains("depth=1")
                  && first.contains("refs=1"), "FilterInfo values during readObject: " + first);
        }

        // 2. allowFilter: predicate match -> given status, miss -> other status
        ObjectInputFilter allow = ObjectInputFilter.allowFilter(
            cl -> cl == String.class, Status.REJECTED);
        check(allow.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "allowFilter match yields ALLOWED");
        check(allow.checkInput(info(Integer.class, -1, 1, 1, 0)) == Status.REJECTED,
              "allowFilter miss yields other status");
        check(allow.checkInput(info(null, -1, 1, 1, 0)) == Status.UNDECIDED,
              "null class leaves predicate filters UNDECIDED");

        // 3. rejectFilter: match -> REJECTED, miss -> other
        ObjectInputFilter reject = ObjectInputFilter.rejectFilter(
            cl -> cl == String.class, Status.ALLOWED);
        check(reject.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "rejectFilter match yields REJECTED");
        check(reject.checkInput(info(Integer.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "rejectFilter miss yields other status");

        // 4. merge: first non-UNDECIDED wins, REJECTED dominates
        ObjectInputFilter undecidedAllow = ObjectInputFilter.allowFilter(
            cl -> cl == Long.class, Status.UNDECIDED);
        ObjectInputFilter stringReject = ObjectInputFilter.rejectFilter(
            cl -> cl == String.class, Status.UNDECIDED);
        ObjectInputFilter merged = ObjectInputFilter.merge(undecidedAllow, stringReject);
        check(merged.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "merge: second filter decides when first is UNDECIDED");
        check(merged.checkInput(info(Integer.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "merge: both undecided -> UNDECIDED");
        ObjectInputFilter merged2 = ObjectInputFilter.merge(reject, allow);
        check(merged2.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "merge: REJECTED from the first filter wins");
        check(merged2.checkInput(info(Integer.class, -1, 1, 1, 0)) == Status.REJECTED,
              "merge: REJECTED from either filter dominates");

        // 5. rejectUndecidedClass upgrades class-based UNDECIDED to REJECTED
        ObjectInputFilter decided = ObjectInputFilter.rejectUndecidedClass(undecidedAllow);
        check(decided.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "UNDECIDED for a class becomes REJECTED");
        check(ObjectInputFilter.rejectUndecidedClass(allow)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "non-UNDECIDED passes through unchanged");
        check(ObjectInputFilter.rejectUndecidedClass(undecidedAllow)
                  .checkInput(info(null, -1, 1, 1, 0)) == Status.UNDECIDED,
              "no class present stays UNDECIDED");

        // 6. wiring: a rejecting filter aborts readObject of a real object
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Widget())))) {
            ois.setObjectInputFilter(ObjectInputFilter.Config.createFilter("!*"));
            try {
                ois.readObject();
                check(false, "reject-all filter must abort readObject");
            } catch (InvalidClassException ice) {
                check(ice.getMessage() != null && ice.getMessage().contains("REJECTED"),
                      "rejection message: " + ice.getMessage());
            }
        }

        // 6b. wiring: an allow-list filter admits the wanted class, rejects the other
        byte[] bytes;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Widget());
            oos.writeObject(new Gadget());
            bytes = bos.toByteArray();
        }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            ois.setObjectInputFilter(ObjectInputFilter.allowFilter(
                cl -> cl == Widget.class, Status.REJECTED));
            Widget w = (Widget) ois.readObject();
            check(w.v == 3, "allow-listed class deserializes");
            try {
                ois.readObject();
                check(false, "non-allow-listed class must be rejected");
            } catch (InvalidClassException expected) {
                passed++;
            }
        }

        // 7. Status enum behavior
        check(Status.valueOf("ALLOWED") == Status.ALLOWED
              && Status.valueOf("REJECTED") == Status.REJECTED
              && Status.valueOf("UNDECIDED") == Status.UNDECIDED, "Status.valueOf");
        check(Status.values().length == 3, "Status.values length");
    }

    // ================= deep additions (contract level) =================

    /** Snapshot of a FilterInfo taken inside the callback (the live view moves on). */
    static class Snap {
        Class<?> c; long arr, d, r, b;
        Snap(FilterInfo i) { c = i.serialClass(); arr = i.arrayLength();
                             d = i.depth(); r = i.references(); b = i.streamBytes(); }
    }

    // ---- static combinator surface: exact signatures ----
    static void testCombinatorSurface() throws Exception {
        java.lang.reflect.Method merge = ObjectInputFilter.class
                .getDeclaredMethod("merge", ObjectInputFilter.class, ObjectInputFilter.class);
        check(Modifier.isStatic(merge.getModifiers()) && Modifier.isPublic(merge.getModifiers()),
              "merge(ObjectInputFilter, ObjectInputFilter) is public static");
        check(ObjectInputFilter.class.getDeclaredMethod("allowFilter",
                  java.util.function.Predicate.class, Status.class).getReturnType()
                  == ObjectInputFilter.class,
              "allowFilter(Predicate<Class<?>>, Status) returns a filter");
        check(ObjectInputFilter.class.getDeclaredMethod("rejectUndecidedClass",
                  ObjectInputFilter.class).getReturnType() == ObjectInputFilter.class,
              "rejectUndecidedClass(ObjectInputFilter) returns a filter");
        check(ObjectInputFilter.class.getAnnotation(java.lang.FunctionalInterface.class) != null,
              "@FunctionalInterface: a lambda is a filter");
        check(ObjectInputFilter.class.getDeclaredMethods().length >= 5,
              "checkInput + four static combinators declared");
    }

    // ---- null-argument matrix (measured messages) ----
    static void testNullArguments() throws Exception {
        ObjectInputFilter allow = x -> Status.ALLOWED;
        java.util.function.Predicate<Class<?>> pred = c -> true;
        try {
            ObjectInputFilter.merge(null, allow);
            check(false, "merge(null, f) must throw");
        } catch (NullPointerException npe) {
            check("filter".equals(npe.getMessage()), "merge(null, f) NPE message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.merge(null, null);
            check(false, "merge(null, null) must throw");
        } catch (NullPointerException npe) {
            check("filter".equals(npe.getMessage()), "merge(null, null) NPE message names the first arg");
        }
        try {
            ObjectInputFilter.allowFilter(null, Status.REJECTED);
            check(false, "allowFilter(null predicate) must throw");
        } catch (NullPointerException npe) {
            check("predicate".equals(npe.getMessage()),
                  "allowFilter null predicate message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.rejectFilter(null, Status.REJECTED);
            check(false, "rejectFilter(null predicate) must throw");
        } catch (NullPointerException npe) {
            check("predicate".equals(npe.getMessage()),
                  "rejectFilter null predicate message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.allowFilter(pred, null);
            check(false, "allowFilter(pred, null) must throw");
        } catch (NullPointerException npe) {
            check("otherStatus".equals(npe.getMessage()),
                  "allowFilter null status message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.rejectFilter(pred, null);
            check(false, "rejectFilter(pred, null) must throw");
        } catch (NullPointerException npe) {
            check("otherStatus".equals(npe.getMessage()),
                  "rejectFilter null status message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.rejectUndecidedClass(null);
            check(false, "rejectUndecidedClass(null) must throw");
        } catch (NullPointerException npe) {
            check("filter".equals(npe.getMessage()),
                  "rejectUndecidedClass(null) NPE message: " + npe.getMessage());
        }
    }

    // ---- merge semantics: first REJECTED short-circuits, otherwise both run and
    //      REJECTED > ALLOWED > UNDECIDED dominates; merge(f, null) is the identity ----
    static void testMergeConsultationOrder() throws Exception {
        final int[] calls = new int[2];
        ObjectInputFilter undecided1 = f -> { calls[0]++; return Status.UNDECIDED; };
        ObjectInputFilter allowed2 = f -> { calls[1]++; return Status.ALLOWED; };
        check(ObjectInputFilter.merge(undecided1, allowed2)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "merge: UNDECIDED + ALLOWED -> ALLOWED");
        check(calls[0] == 1 && calls[1] == 1,
              "both filters are consulted when the first does not reject");

        // a REJECTED first short-circuits: the second is never invoked
        calls[0] = 0; calls[1] = 0;
        ObjectInputFilter rejectFirst = f -> { calls[0]++; return Status.REJECTED; };
        ObjectInputFilter allowSecond = f -> { calls[1]++; return Status.ALLOWED; };
        check(ObjectInputFilter.merge(rejectFirst, allowSecond)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "merge: REJECTED + ALLOWED -> REJECTED");
        check(calls[0] == 1 && calls[1] == 0,
              "a REJECTED first short-circuits: the second filter is not consulted");

        ObjectInputFilter plainAllow = f -> Status.ALLOWED;
        ObjectInputFilter plainReject = f -> Status.REJECTED;
        ObjectInputFilter plainUndecided = f -> Status.UNDECIDED;
        check(ObjectInputFilter.merge(plainAllow, plainReject)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "merge: ALLOWED + REJECTED -> REJECTED (the second can still veto)");
        check(ObjectInputFilter.merge(plainAllow, plainUndecided)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "merge: ALLOWED + UNDECIDED -> ALLOWED");
        check(ObjectInputFilter.merge(plainUndecided, plainUndecided)
                  .checkInput(info(String.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "merge: UNDECIDED + UNDECIDED -> UNDECIDED");

        // merge(filter, null) returns the first filter itself
        check(ObjectInputFilter.merge(plainReject, null) == plainReject,
              "merge(f, null) is the identity: the first filter is returned unchanged");

        // a filter returning null status is stopped by requireNonNull
        try {
            ObjectInputFilter.merge(f -> null, plainAllow)
                    .checkInput(info(String.class, -1, 1, 1, 0));
            check(false, "null status from the first filter must throw");
        } catch (NullPointerException npe) {
            check("status".equals(npe.getMessage()), "NPE message: " + npe.getMessage());
        }
        try {
            ObjectInputFilter.merge(plainAllow, f -> null)
                    .checkInput(info(String.class, -1, 1, 1, 0));
            check(false, "null status from the second filter must throw");
        } catch (NullPointerException npe) {
            check("other status".equals(npe.getMessage()), "NPE message: " + npe.getMessage());
        }
    }

    // ---- a throwing predicate propagates out of checkInput unchanged ----
    static void testPredicateThrowPropagation() throws Exception {
        ObjectInputFilter throwing = ObjectInputFilter.allowFilter(c -> {
            throw new IllegalStateException("predboom");
        }, Status.REJECTED);
        try {
            throwing.checkInput(info(String.class, -1, 1, 1, 0));
            check(false, "predicate exception must propagate from checkInput");
        } catch (IllegalStateException ise) {
            check("predboom".equals(ise.getMessage()),
                  "direct checkInput propagates the predicate's ISE unchanged");
        }
    }

    // ---- a filter that throws during a live read is treated as REJECTED ----
    static void testFilterThrowDuringReadMapped() throws Exception {
        try (ObjectInputStream ois =
                 new ObjectInputStream(new ByteArrayInputStream(ser(new Widget())))) {
            ois.setObjectInputFilter(f -> { throw new IllegalStateException("liveboom"); });
            try {
                ois.readObject();
                check(false, "a throwing filter must abort deserialization");
            } catch (InvalidClassException ice) {
                check(ice.getMessage() != null
                          && ice.getMessage().endsWith("filter status: REJECTED"),
                      "runtime exception from the filter is mapped to REJECTED, not propagated: "
                      + ice.getMessage());
            }
        }
    }

    // ---- setObjectInputFilter lifecycle on a live stream ----
    static void testSetFilterLifecycle() throws Exception {
        byte[] bytes = ser(new Widget());
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
            check(ois.getObjectInputFilter() == null, "no filter before setting");
            ObjectInputFilter f = x -> Status.ALLOWED;
            ois.setObjectInputFilter(f);
            check(ois.getObjectInputFilter() == f, "getObjectInputFilter returns the same instance");
            try {
                ois.setObjectInputFilter(x -> Status.ALLOWED);
                check(false, "a second setObjectInputFilter must throw");
            } catch (IllegalStateException ise) {
                check("filter can not be set more than once".equals(ise.getMessage()),
                      "ISE message: " + ise.getMessage());
            }
            ois.readObject();
            try {
                ois.setObjectInputFilter(x -> Status.ALLOWED);
                check(false, "setObjectInputFilter after a read must throw");
            } catch (IllegalStateException ise) {
                check("filter can not be set after an object has been read".equals(ise.getMessage()),
                      "ISE message after a read: " + ise.getMessage());
            }
        }
    }

    // ---- live FilterInfo for int[3]: descriptor stage then length stage ----
    static void testLiveArrayStages() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new int[3]);
        }
        final java.util.List<Snap> recs = new java.util.ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            ois.setObjectInputFilter(f -> { recs.add(new Snap(f)); return Status.ALLOWED; });
            check(((int[]) ois.readObject()).length == 3, "int[3] read back under an allowing filter");
        }
        check(recs.size() == 2, "exactly two callbacks for an array: " + recs.size());
        check(recs.get(0).c == int[].class && recs.get(0).arr == -1,
              "descriptor-stage callback: array class known, length not yet");
        check(recs.get(1).c == int[].class && recs.get(1).arr == 3,
              "length-stage callback reports the true length 3");
        check(recs.get(0).d == 1 && recs.get(1).d == 1, "the array stays at depth 1 in both stages");
    }

    // ---- allowFilter/rejectFilter full otherStatus matrix ----
    static void testOtherStatusMatrix() throws Exception {
        ObjectInputFilter a = ObjectInputFilter.allowFilter(c -> c == String.class, Status.UNDECIDED);
        check(a.checkInput(info(String.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "allowFilter match always yields ALLOWED regardless of otherStatus");
        check(a.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "allowFilter miss yields the given otherStatus (UNDECIDED)");
        ObjectInputFilter r = ObjectInputFilter.rejectFilter(c -> c == String.class, Status.UNDECIDED);
        check(r.checkInput(info(String.class, -1, 1, 1, 0)) == Status.REJECTED,
              "rejectFilter match always yields REJECTED");
        check(r.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.UNDECIDED,
              "rejectFilter miss yields the given otherStatus");
        check(r.checkInput(info(null, -1, 1, 1, 0)) == Status.UNDECIDED,
              "no class -> predicate misses -> otherStatus");
        ObjectInputFilter a2 = ObjectInputFilter.allowFilter(c -> c == String.class, Status.ALLOWED);
        check(a2.checkInput(info(Long.class, -1, 1, 1, 0)) == Status.ALLOWED,
              "allowFilter with otherStatus=ALLOWED lets misses through");
        check(a2.checkInput(info(null, -1, 1, 1, 0)) == Status.UNDECIDED,
              "null class forces UNDECIDED regardless of otherStatus");
        String ts = a.toString();
        check(ts.startsWith("predicate(") && ts.contains("ifTrue: ALLOWED")
                  && ts.contains("ifFalse:UNDECIDED"),
              "PredicateFilter toString exposes both statuses: " + ts);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCombinatorSurface();
        testNullArguments();
        testMergeConsultationOrder();
        testPredicateThrowPropagation();
        testFilterThrowDuringReadMapped();
        testSetFilterLifecycle();
        testLiveArrayStages();
        testOtherStatusMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
