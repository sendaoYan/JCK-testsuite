/*
 * @test
 * @summary javax.management.AttributeList conformance: white-box structure + black-box behavior.
 * @run main AttributeListTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class AttributeListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.AttributeList");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(c.getSuperclass() == java.util.ArrayList.class, "extends ArrayList");
        check(hasMethod(c, "asList"), "has asList");
        check(hasMethod(c, "add"), "has add");
        check(hasMethod(c, "addAll"), "has addAll");
        check(hasMethod(c, "set"), "has set");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        AttributeList list = new AttributeList();
        check(list.isEmpty() && list.size() == 0, "starts empty");

        list.add(new Attribute("a", 1));
        list.add(new Attribute("b", 2));
        check(list.size() == 2, "size after add");
        check(list.get(0).equals(new Attribute("a", 1)), "get(0)");
        check(list.get(1).equals(new Attribute("b", 2)), "get(1)");

        // add(Object) returns boolean true (cast forces the Object overload)
        check(list.add((Object) new Attribute("c", 3)), "add(Object) returns true");
        list.add(1, new Attribute("x", 99));
        check(list.get(1).equals(new Attribute("x", 99)), "add(index, attribute)");

        // set(int, Attribute) replaces in place
        list.set(0, new Attribute("A", 10));
        check(list.get(0).equals(new Attribute("A", 10)), "set(int,Attribute)");

        // copy constructors
        List<Attribute> plain = new ArrayList<Attribute>();
        plain.add(new Attribute("p", 1));
        AttributeList fromList = new AttributeList(plain);
        check(fromList.size() == 1 && fromList.get(0).equals(new Attribute("p", 1)), "copy from List<Attribute>");
        AttributeList fromAttrList = new AttributeList(fromList);
        check(fromAttrList.equals(fromList), "copy from AttributeList");

        // addAll(AttributeList)
        AttributeList other = new AttributeList();
        other.add(new Attribute("q", 7));
        check(fromAttrList.addAll(other), "addAll(AttributeList) returns true");
        check(fromAttrList.size() == 2, "size after addAll");

        // asList() exposes typed view with same elements
        List<Attribute> view = fromAttrList.asList();
        check(view.size() == 2, "asList size");
        check(view.get(0) instanceof Attribute, "asList elements typed");

        // type-safety enforced once asList() has been called
        AttributeList typed = new AttributeList();
        typed.add(new Attribute("t", 1));
        typed.asList();
        boolean threw = false;
        try { typed.add("not-an-attribute"); } catch (IllegalArgumentException ex) { threw = true; }
        check(threw, "type-safe add(Object) rejects non-Attribute");
        check(typed.size() == 1, "rejected element not added");

        // set(int, Object) type-safe rejection
        boolean threwSet = false;
        try { typed.set(0, "not-an-attribute"); } catch (IllegalArgumentException ex) { threwSet = true; }
        check(threwSet, "type-safe set(int,Object) rejects non-Attribute");
    }


    @SuppressWarnings("unchecked")
    static <T> T deepRoundTripValue(T o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois =
                new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (2026-11): ArrayList contract surface ----

    static void testDeepConstructors() {
        check(new AttributeList().isEmpty(), "default ctor empty");
        check(new AttributeList(5).isEmpty(), "capacity ctor empty");
        Attribute a = new Attribute("A", Integer.valueOf(1));
        Attribute b = new Attribute("B", Integer.valueOf(2));
        AttributeList src = new AttributeList();
        src.add(a);
        src.add(b);
        AttributeList copy = new AttributeList(src);
        check(copy.size() == 2 && copy.get(0).equals(a) && copy.get(1).equals(b), "AttributeList copy ctor");
        copy.set(0, new Attribute("A", Integer.valueOf(9)));
        check(src.get(0).equals(a), "copy ctor independent");
        java.util.List<Attribute> plain = new java.util.ArrayList<Attribute>();
        plain.add(a);
        plain.add(b);
        AttributeList fromList = new AttributeList(plain);
        check(fromList.size() == 2 && fromList.get(1).equals(b), "List<Attribute> ctor");
        try {
            new AttributeList((java.util.List<Attribute>) null);
            check(false, "null List must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        // JDK-27 (unlike JDK-8): the List ctor rejects non-Attribute elements eagerly
        java.util.List<Object> raw = new java.util.ArrayList<Object>();
        raw.add(new Attribute("A", Integer.valueOf(1)));
        raw.add("not-an-attribute");
        try {
            @SuppressWarnings({"unchecked", "rawtypes"})
            AttributeList rejected = new AttributeList((java.util.List) raw);
            check(false, "ctor with non-Attribute must throw (got size " + rejected.size() + ")");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Not an Attribute"), "eager ctor rejection message");
        }
        try {
            new AttributeList(-1);
            check(false, "negative capacity must throw");
        } catch (IllegalArgumentException e) {
            passed++;
        }
    }

    static void testDeepAddSetOverloads() {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("A", Integer.valueOf(1));
        Attribute b = new Attribute("B", Integer.valueOf(2));
        l.add(a);
        check(l.size() == 1 && l.get(0).equals(a), "add(Attribute)");
        check(l.add((Object) b), "add(Object) returns true");
        check(l.size() == 2, "size after add(Object)");
        l.add(1, a);
        check(l.get(1).equals(a) && l.get(2).equals(b), "add(int, Attribute)");
        l.set(0, b);
        check(l.get(0).equals(b), "set(int, Attribute)");
        Object old = l.set(0, (Object) a);
        check(b.equals(old), "set(int, Object) returns old element");
        check(l.get(0).equals(a), "set(int, Object) element");
        // typed overloads wrap index errors in RuntimeOperationsException
        try {
            new AttributeList().add(5, new Attribute("A", Integer.valueOf(1)));
            check(false, "typed add(int,Attribute) out of range must wrap");
        } catch (javax.management.RuntimeOperationsException e) {
            check(e.getTargetException() instanceof IndexOutOfBoundsException, "wraps IndexOutOfBoundsException");
        }
        try {
            new AttributeList().set(5, new Attribute("A", Integer.valueOf(1)));
            check(false, "typed set(int,Attribute) out of range must wrap");
        } catch (javax.management.RuntimeOperationsException e) {
            passed++;
        }
        try {
            new AttributeList().addAll(5, new AttributeList());
            check(false, "typed addAll(int,AttributeList) out of range must wrap");
        } catch (javax.management.RuntimeOperationsException e) {
            passed++;
        }
        // untyped Object overload does NOT wrap (use a real Attribute: on JDK-27 the
        // element check runs before the range check)
        try {
            new AttributeList().add(5, (Object) new Attribute("A", Integer.valueOf(1)));
            check(false, "untyped add(int,Object) out of range must be a raw IOOBE");
        } catch (IndexOutOfBoundsException e) {
            check(!javax.management.RuntimeOperationsException.class.isInstance(e), "raw IndexOutOfBoundsException");
        }
        // JDK-27: non-Attribute is rejected before the range check, eagerly
        try {
            new AttributeList().add(0, (Object) "x");
            check(false, "untyped add(int,Object) with non-Attribute must throw");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("Not an Attribute"), "eager element check message");
        }
    }

    static void testDeepNullElementTolerance() {
        AttributeList l = new AttributeList();
        l.add((Attribute) null);
        check(l.size() == 1 && l.get(0) == null, "typed add(null)");
        l.add(0, (Attribute) null);
        check(l.size() == 2, "typed add(int, null)");
        l.set(0, (Attribute) null);
        check(l.get(0) == null, "typed set(int, null)");
        l.add((Object) null);
        check(l.size() == 3, "untyped add(null)");
    }

    static void testDeepTypeSafetyViaAsList() {
        // JDK-27 (unlike JDK-8): every untyped write channel checks its elements
        // eagerly - no taint state, asList() is not needed to make the list safe
        AttributeList l = new AttributeList();
        l.add(new Attribute("A", Integer.valueOf(1)));
        try {
            l.add((Object) "not-an-attribute");
            check(false, "add(Object) must reject non-Attribute");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        java.util.List<Object> bad = new java.util.ArrayList<Object>();
        bad.add("x");
        try {
            l.addAll(bad);
            check(false, "addAll(Collection) must reject non-Attribute");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            l.addAll(0, bad);
            check(false, "addAll(int,Collection) must reject non-Attribute");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            l.set(0, (Object) "x");
            check(false, "set(int,Object) must reject non-Attribute");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        check(l.size() == 1, "rejected elements not inserted");
        // null elements remain legal on every channel
        check(l.add((Object) null) && l.size() == 2, "untyped add(null) accepted");
        // asList() view still works and reflects the list
        java.util.List<Attribute> view = l.asList();
        check(view.size() == 2 && view.get(0).equals(new Attribute("A", Integer.valueOf(1))), "asList view");
    }

    static void testDeepViewAndWriteChannels() {
        AttributeList l = new AttributeList();
        Attribute a = new Attribute("A", Integer.valueOf(1));
        Attribute b = new Attribute("B", Integer.valueOf(2));
        Attribute c = new Attribute("C", Integer.valueOf(3));
        l.add(a);
        l.add(b);
        l.add(c);
        java.util.List<Attribute> view = l.asList();
        check(view.size() == 3, "asList size");
        view.add(new Attribute("D", Integer.valueOf(4)));
        check(l.size() == 4 && l.get(3).equals(new Attribute("D", Integer.valueOf(4))), "view.add writes through");
        l.add(new Attribute("E", Integer.valueOf(5)));
        check(view.size() == 5, "list.add reflects in view");
        check(l.contains(a) && l.contains(new Attribute("A", Integer.valueOf(1))), "contains");
        check(l.indexOf(new Attribute("B", Integer.valueOf(2))) == 1, "indexOf");
        check(l.remove(new Attribute("A", Integer.valueOf(1))), "remove(Object) returns true");
        check(l.size() == 4 && !l.contains(a), "remove(Object) removes");
        check(b.equals(l.remove(0)), "remove(int) returns removed element");
        AttributeList extra = new AttributeList();
        extra.add(new Attribute("Z", Integer.valueOf(99)));
        check(l.addAll(extra) && l.size() == 4, "addAll(AttributeList)");
        l.clear();
        check(l.isEmpty() && l.size() == 0, "clear");
        // iterator writes through and obeys the remove-before-next contract
        AttributeList l2 = new AttributeList();
        l2.add(a);
        l2.add(b);
        java.util.Iterator<Attribute> it = l2.asList().iterator();
        check(it.hasNext() && "A".equals(it.next().getName()), "iterator next");
        it.remove();
        check(l2.size() == 1 && "B".equals(((Attribute) l2.get(0)).getName()), "iterator remove writes through");
        java.util.Iterator<Attribute> it2 = l2.asList().iterator();
        try {
            it2.remove();
            check(false, "remove before next must throw IllegalStateException");
        } catch (IllegalStateException e) {
            passed++;
        }
        // fail-fast on structural modification during iteration
        AttributeList l3 = new AttributeList();
        l3.add(a);
        l3.add(b);
        java.util.Iterator<Attribute> fit = l3.asList().iterator();
        fit.next();
        l3.remove(0);
        try {
            fit.next();
            check(false, "fail-fast iterator must throw ConcurrentModificationException");
        } catch (java.util.ConcurrentModificationException e) {
            passed++;
        }
    }

    static void testDeepSerialization() throws Exception {
        check(java.io.ObjectStreamClass.lookup(AttributeList.class).getSerialVersionUID() == -4077085769279709076L,
                "serialVersionUID constant");
        AttributeList l = new AttributeList();
        l.add(new Attribute("A", Integer.valueOf(1)));
        l.add((Attribute) null);
        AttributeList back = deepRoundTripValue(l);
        check(back.size() == 2, "round-trip size");
        check(back.get(0).equals(new Attribute("A", Integer.valueOf(1))), "round-trip element");
        check(back.get(1) == null, "round-trip null element");
        back.add(new Attribute("B", Integer.valueOf(2)));
        check(back.size() == 3, "revived list still mutable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepConstructors();
        testDeepAddSetOverloads();
        testDeepNullElementTolerance();
        testDeepTypeSafetyViaAsList();
        testDeepViewAndWriteChannels();
        testDeepSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
