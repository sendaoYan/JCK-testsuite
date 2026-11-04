/*
 * @test
 * @summary java.io.ObjectInputFilter.FilterInfo conformance: white-box structure
 *          (callback interface with the five context accessors) + black-box
 *          behavior (real deserialization reports the exact serialClass,
 *          arrayLength, depth, references and streamBytes; nested objects grow
 *          depth; String takes a fast path that never triggers the filter).
 * @run main FilterInfoTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectInputFilter;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class FilterInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Node implements Serializable {
        private static final long serialVersionUID = 1L;
        Node next;                                           // nested serializable
    }

    static class Rec {
        Class<?> serialClass; long arrayLength, depth, references, streamBytes;
        Rec(ObjectInputFilter.FilterInfo i) {
            serialClass = i.serialClass();
            arrayLength = i.arrayLength();
            depth = i.depth();
            references = i.references();
            streamBytes = i.streamBytes();
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.ObjectInputFilter$FilterInfo");
        check(c.isInterface(), "is an interface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getMethods().length == 5, "exactly five abstract accessors");
        check(c.getMethod("serialClass").getReturnType() == Class.class, "serialClass() -> Class");
        for (String m : new String[]{"arrayLength", "depth", "references", "streamBytes"})
            check(c.getMethod(m).getReturnType() == long.class, m + "() -> long");
        for (Method m : c.getMethods())
            check(Modifier.isAbstract(m.getModifiers()), "abstract " + m.getName());
        check(c.getFields().length == 0, "no declared constants");
        // the interface is a functional callback: a lambda implements it
        ObjectInputFilter f = info -> ObjectInputFilter.Status.UNDECIDED;
        check(f != null, "lambda implements the enclosing filter");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new Node());                     // custom class, no nesting yet
        }
        List<Rec> recs = new ArrayList<>();
        ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()));
        ois.setObjectInputFilter(info -> {
            recs.add(new Rec(info));
            return ObjectInputFilter.Status.ALLOWED;
        });
        Node n = (Node) ois.readObject();
        check(recs.size() == 1, "one callback for the root class: " + recs.size());
        Rec r = recs.get(0);
        check(r.serialClass == Node.class, "serialClass of the root object");
        check(r.arrayLength == -1, "arrayLength is -1 for a non-array: " + r.arrayLength);
        check(r.depth == 1, "depth of the root object is 1: " + r.depth);
        check(r.references == 1, "first reference is counted: " + r.references);
        check(r.streamBytes > 0, "streamBytes advances: " + r.streamBytes);
        check(n.next == null, "object deserialized intact");

        // 2. arrays report their length with the array class; the filter sees the
        //    array twice (class descriptor stage, then the length check stage)
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos2)) {
            oos.writeObject(new String[7]);
        }
        recs.clear();
        ObjectInputStream ois2 = new ObjectInputStream(
                new ByteArrayInputStream(bos2.toByteArray()));
        ois2.setObjectInputFilter(info -> {
            recs.add(new Rec(info));
            return ObjectInputFilter.Status.ALLOWED;
        });
        ois2.readObject();
        check(!recs.isEmpty(), "array produced callbacks: " + recs.size());
        boolean sawArrayClass = false, sawLength = false;
        for (Rec a : recs) {
            sawArrayClass |= a.serialClass == String[].class;
            sawLength |= a.arrayLength == 7;
            check(a.depth == 1, "array stays at depth 1: " + a.depth);
            check(a.references >= 1, "array reference counted");
        }
        check(sawArrayClass, "array class reported");
        check(sawLength, "array length 7 reported");
        check(recs.get(0).arrayLength == -1, "descriptor-stage callback has no length yet");

        // 3. a nested object grows the depth; references keep counting
        ByteArrayOutputStream bos3 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos3)) {
            Node chain = new Node();
            chain.next = new Node();
            oos.writeObject(chain);
        }
        recs.clear();
        ObjectInputStream ois3 = new ObjectInputStream(
                new ByteArrayInputStream(bos3.toByteArray()));
        ois3.setObjectInputFilter(info -> {
            recs.add(new Rec(info));
            return ObjectInputFilter.Status.ALLOWED;
        });
        Node chain = (Node) ois3.readObject();
        check(chain.next != null, "chain intact");
        check(recs.get(0).serialClass == Node.class && recs.get(0).depth == 1,
              "root callback reports Node at depth 1");
        boolean sawNested = false;
        for (int i = 1; i < recs.size(); i++) {
            Rec a = recs.get(i);
            sawNested |= a.depth == 2;
            check(a.references > recs.get(0).references, "references grow past the root");
            check(a.streamBytes >= recs.get(0).streamBytes, "streamBytes never decreases");
        }
        check(sawNested, "nested object reported at depth 2");
        check(recs.size() >= 2, "nested graph produced extra callbacks: " + recs.size());

        // 4. String deserialization is a fast path: the filter is never consulted
        ByteArrayOutputStream bos4 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos4)) {
            oos.writeObject("hello");
        }
        recs.clear();
        ObjectInputStream ois4 = new ObjectInputStream(
                new ByteArrayInputStream(bos4.toByteArray()));
        ois4.setObjectInputFilter(info -> {
            recs.add(new Rec(info));
            return ObjectInputFilter.Status.REJECTED;        // would reject anything else
        });
        check("hello".equals(ois4.readObject()),
              "String reads succeed without consulting the filter");
        check(recs.isEmpty(), "no callback happened for a String");

        // 5. streamBytes is cumulative across objects in one stream
        ByteArrayOutputStream bos5 = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos5)) {
            oos.writeObject(new Node());
            oos.writeObject(new Node());
        }
        recs.clear();
        ObjectInputStream ois5 = new ObjectInputStream(
                new ByteArrayInputStream(bos5.toByteArray()));
        ois5.setObjectInputFilter(info -> {
            recs.add(new Rec(info));
            return ObjectInputFilter.Status.ALLOWED;
        });
        ois5.readObject();
        ois5.readObject();
        long classCallbacks = recs.stream().filter(x -> x.serialClass == Node.class).count();
        check(classCallbacks == 1, "class descriptor reported once for both instances: "
                  + classCallbacks);
        check(recs.size() >= 2, "both instances produced callbacks: " + recs.size());
        check(recs.get(recs.size() - 1).references >= 2, "second object counted");
        check(recs.get(recs.size() - 1).streamBytes > recs.get(0).streamBytes,
              "bytes grew between objects");
    }

    // ================= deep additions (contract level) =================

    // ---- exactly five declared methods; not a functional interface ----
    static void testNotFunctional() throws Exception {
        check(ObjectInputFilter.FilterInfo.class.getDeclaredMethods().length == 5,
              "exactly five declared methods (all abstract accessors)");
        check(ObjectInputFilter.FilterInfo.class
                  .getAnnotation(java.lang.FunctionalInterface.class) == null,
              "not @FunctionalInterface (five abstract methods)");
    }

    // ---- int[3]: the filter is consulted twice (descriptor stage, length stage) ----
    static void testArrayTwoStages() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(new int[3]);
        }
        List<Rec> recs = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()))) {
            ois.setObjectInputFilter(i -> { recs.add(new Rec(i)); return ObjectInputFilter.Status.ALLOWED; });
            check(((int[]) ois.readObject()).length == 3, "int[3] read back");
        }
        check(recs.size() == 2, "exactly two callbacks for an array: " + recs.size());
        check(recs.get(0).serialClass == int[].class && recs.get(0).arrayLength == -1,
              "first callback: array class, length unknown (-1)");
        check(recs.get(1).serialClass == int[].class && recs.get(1).arrayLength == 3,
              "second callback: the true length 3");
        check(recs.get(0).depth == 1 && recs.get(1).depth == 1,
              "both stages report depth 1");
    }

    // ---- enum deserialization consults the filter for the enum class and for
    //      the abstract java.lang.Enum superclass, at increasing depth/refs ----
    static void testEnumDoubleCallback() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(java.time.DayOfWeek.FRIDAY);
        }
        List<Rec> recs = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()))) {
            ois.setObjectInputFilter(i -> { recs.add(new Rec(i)); return ObjectInputFilter.Status.ALLOWED; });
            check(ois.readObject() == java.time.DayOfWeek.FRIDAY, "enum read back");
        }
        check(recs.size() == 2, "two callbacks for an enum: " + recs.size());
        check(recs.get(0).serialClass == java.time.DayOfWeek.class
                  && recs.get(0).depth == 1 && recs.get(0).references == 1,
              "first callback: DayOfWeek at depth 1, references 1");
        check(recs.get(1).serialClass == java.lang.Enum.class
                  && recs.get(1).depth == 2 && recs.get(1).references == 2,
              "second callback: java.lang.Enum at depth 2, references 2");
    }

    // ---- a three-level object chain: the nested-instance callbacks carry no class ----
    static void testThreeLevelChain() throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            Node a = new Node();
            a.next = new Node();
            a.next.next = new Node();
            oos.writeObject(a);
        }
        List<Rec> recs = new ArrayList<>();
        try (ObjectInputStream ois = new ObjectInputStream(
                new ByteArrayInputStream(bos.toByteArray()))) {
            ois.setObjectInputFilter(i -> { recs.add(new Rec(i)); return ObjectInputFilter.Status.ALLOWED; });
            ois.readObject();
        }
        check(recs.size() == 3, "one callback per level: " + recs.size());
        check(recs.get(0).serialClass == Node.class && recs.get(0).depth == 1,
              "root callback carries the class at depth 1");
        check(recs.get(1).serialClass == null && recs.get(1).depth == 2,
              "second level: serialClass is null (no new descriptor), depth 2");
        check(recs.get(2).serialClass == null && recs.get(2).depth == 3,
              "third level: serialClass null, depth 3");
        check(recs.get(0).references < recs.get(1).references
                  && recs.get(1).references < recs.get(2).references,
              "references strictly increase with each level");
        check(recs.get(0).streamBytes < recs.get(1).streamBytes
                  && recs.get(1).streamBytes < recs.get(2).streamBytes,
              "streamBytes strictly increase with each level");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNotFunctional();
        testArrayTwoStages();
        testEnumDoubleCallback();
        testThreeLevelChain();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
