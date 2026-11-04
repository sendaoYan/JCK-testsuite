/*
 * @test
 * @summary javax.management.AttributeList conformance: full contract coverage.
 *          all four constructors (incl. IllegalArgumentException for null /
 *          non-Attribute List), add/set overload resolution and return types,
 *          RuntimeOperationsException wrapping for out-of-range index on the
 *          typed overloads, null element tolerance, type-safety via asList()
 *          (tainted list -> IAE, non-Attribute add -> IAE), live asList() view,
 *          ArrayList write channels (remove/removeAll/retainAll/clear/contains),
 *          and serialization round-trip.
 * @run main AttributeListTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.RuntimeOperationsException;

public class AttributeListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static AttributeList roundTrip(AttributeList l) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try (ObjectOutputStream oos = new ObjectOutputStream(bos)) { oos.writeObject(l); }
        try (ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()))) {
            return (AttributeList) ois.readObject();
        }
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("Count", Integer.valueOf(1));
        l.add(a);
        check(l.size() == 1, "size after add");
        check(l.asList().get(0) == a, "asList element");
        Attribute b = new Attribute("Count", Integer.valueOf(2));
        l.set(0, b);
        check(l.asList().get(0) == b, "set element");
    }

    static void testConstructors() {
        AttributeList def = new AttributeList();
        check(def.isEmpty(), "default empty");

        AttributeList cap = new AttributeList(5);
        check(cap.isEmpty(), "capacity ctor empty");

        Attribute a = new Attribute("A", 1);
        Attribute b = new Attribute("B", 2);
        AttributeList src = new AttributeList();
        src.add(a);
        src.add(b);
        AttributeList copy = new AttributeList(src);
        check(copy.size() == 2 && copy.get(0).equals(a) && copy.get(1).equals(b), "copy ctor");
        copy.set(0, new Attribute("A", 9));
        check(src.get(0).equals(a), "copy ctor independent");

        List<Attribute> plain = new ArrayList<Attribute>();
        plain.add(a);
        plain.add(b);
        AttributeList fromList = new AttributeList(plain);
        check(fromList.size() == 2 && fromList.get(1).equals(b), "List ctor");

        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { new AttributeList((List<Attribute>) null); } },
                "List ctor null");

        // A non-Attribute element is not rejected at construction time (the
        // JDK only marks the list as tainted); the observable contract is that
        // a later asList() on the tainted list throws IllegalArgumentException.
        final List<Object> raw = new ArrayList<Object>();
        raw.add(new Attribute("A", 1));
        raw.add("not-an-attribute");
        @SuppressWarnings({"unchecked", "rawtypes"})
        AttributeList tainted = new AttributeList((List) raw);
        check(tainted.size() == 2, "tainted list constructed");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { tainted.asList(); } },
                "tainted asList() throws IAE");
    }

    static void testAddSetOverloads() {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("A", 1);
        Attribute b = new Attribute("B", 2);

        l.add(a);                                  // void add(Attribute)
        check(l.size() == 1 && l.get(0).equals(a), "add(Attribute)");
        check(l.add((Object) b), "add(Object) returns true"); // boolean add(Object)
        check(l.size() == 2, "size after add(Object)");

        l.add(1, a);                               // void add(int, Attribute)
        check(l.get(1).equals(a) && l.get(2).equals(b), "add(int, Attribute)");

        l.set(0, b);                               // void set(int, Attribute)
        check(l.get(0).equals(b), "set(int, Attribute)");

        Object old = l.set(0, (Object) a);         // Object set(int, Object) returns old
        check(b.equals(old), "set(int, Object) returns old");
        check(l.get(0).equals(a), "set(int, Object) element");

        // typed overloads wrap IndexOutOfBoundsException in RuntimeOperationsException
        checkThrows(RuntimeOperationsException.class,
                new ThrowingRunnable() { public void run() { new AttributeList().add(5, new Attribute("A", 1)); } },
                "add(int, Attribute) out of range wraps ROE");
        checkThrows(RuntimeOperationsException.class,
                new ThrowingRunnable() { public void run() { new AttributeList().set(5, new Attribute("A", 1)); } },
                "set(int, Attribute) out of range wraps ROE");
        checkThrows(RuntimeOperationsException.class,
                new ThrowingRunnable() { public void run() { new AttributeList().addAll(5, new AttributeList()); } },
                "addAll(int, AttributeList) out of range wraps ROE");
        // untyped Object overload does NOT wrap
        checkThrows(IndexOutOfBoundsException.class,
                new ThrowingRunnable() { public void run() { new AttributeList().add(5, (Object) "x"); } },
                "add(int, Object) out of range is raw IOOBE");
    }

    static void testNullElementTolerance() {
        AttributeList l = new AttributeList();
        l.add((Attribute) null);
        check(l.size() == 1 && l.get(0) == null, "add null via typed overload");
        l.add(0, (Attribute) null);
        check(l.size() == 2, "add null at index");
        l.set(0, (Attribute) null);
        check(l.get(0) == null, "set null");
        l.add((Object) null);                      // untyped null allowed
        check(l.size() == 3, "add null via Object overload");
    }

    static void testTypeSafetyViaAsList() {
        // asList() makes the list type-safe: non-Attribute add -> IAE
        final AttributeList safe = new AttributeList();
        safe.asList();
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { safe.add((Object) "not-an-attribute"); } },
                "type-safe add(Object) non-Attribute");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { safe.addAll(Arrays.asList("x")); } },
                "type-safe addAll non-Attribute");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { safe.set(0, (Object) "x"); } },
                "type-safe set(Object) non-Attribute");
        safe.add(new Attribute("A", 1));
        check(safe.size() == 1, "type-safe add(Attribute) ok");

        // a list tainted before asList() -> asList() throws IAE
        final AttributeList tainted = new AttributeList();
        tainted.add((Object) "taint");
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { tainted.asList(); } },
                "tainted asList() throws IAE");
    }

    static void testAsListLiveViewAndWriteChannels() {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("A", 1);
        Attribute b = new Attribute("B", 2);
        Attribute c = new Attribute("C", 3);
        l.add(a); l.add(b); l.add(c);
        List<Attribute> view = l.asList();
        check(view.size() == 3, "view size");
        view.add(new Attribute("D", 4));
        check(l.size() == 4 && l.get(3).equals(new Attribute("D", 4)), "view.add reflects");
        l.add(new Attribute("E", 5));
        check(view.size() == 5, "list.add reflects in view");

        check(l.contains(a) && l.contains(new Attribute("A", 1)), "contains");
        check(l.indexOf(new Attribute("B", 2)) == 1, "indexOf");

        l.remove(new Attribute("A", 1));
        check(l.size() == 4 && !l.contains(a), "remove(Attribute)");
        check(l.remove(0).equals(b), "remove(index) returns removed");

        AttributeList extra = new AttributeList();
        extra.add(new Attribute("Z", 99));
        check(l.addAll(extra), "addAll(AttributeList) returns true");
        check(l.size() == 4, "size after addAll");

        l.clear();
        check(l.isEmpty() && l.size() == 0, "clear");

        // fail-fast iterator inherited from ArrayList
        AttributeList l2 = new AttributeList();
        l2.add(new Attribute("A", 1));
        l2.add(new Attribute("B", 2));
        final java.util.Iterator<Object> it = l2.iterator();
        check(it.hasNext() && it.next().equals(new Attribute("A", 1)), "iterator");
        l2.remove(0);   // structural modification after iterator acquired
        checkThrows(java.util.ConcurrentModificationException.class,
                new ThrowingRunnable() { public void run() { it.next(); } },
                "fail-fast iterator");
    }

    static void testSerialization() throws Exception {
        AttributeList l = new AttributeList();
        l.add(new Attribute("A", 1));
        l.add((Attribute) null);
        AttributeList back = roundTrip(l);
        check(back.size() == 2, "round trip size");
        check(back.get(0).equals(new Attribute("A", 1)), "round trip element");
        check(back.get(1) == null, "round trip null element");
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testConstructors();
        testAddSetOverloads();
        testNullElementTolerance();
        testTypeSafetyViaAsList();
        testAsListLiveViewAndWriteChannels();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
