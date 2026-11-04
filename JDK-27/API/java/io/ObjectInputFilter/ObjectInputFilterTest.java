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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
