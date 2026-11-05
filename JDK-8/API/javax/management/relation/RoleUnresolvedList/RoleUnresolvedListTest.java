/*
 * @test
 * @summary javax.management.relation.RoleUnresolvedList conformance: add/asList.
 * @run main RoleUnresolvedListTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleUnresolvedListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        RoleUnresolved ru = new RoleUnresolved("r", new java.util.ArrayList<ObjectName>(), 1);
        rul.add(ru);
        check(rul.size() == 1, "size");
        check(rul.asList().get(0) == ru, "asList");
        testConstructorForms();
        testConstructorRejectsNonRoleUnresolved();
        testAddSetGetAndBounds();
        testAddAllForms();
        testLegacyUntypedAccess();
        testIterationAndRemoval();
        testAsListLiveView();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch j): full contract coverage ----

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

    static RoleUnresolved unresOf(String name, int problemType) {
        return new RoleUnresolved(name, null, problemType);
    }

    // ()/(int)/(List<RoleUnresolved>) constructors, capacity validation, null rejection
    static void testConstructorForms() {
        check(new RoleUnresolvedList().isEmpty(), "no-arg ctor builds an empty list");
        check(new RoleUnresolvedList(8).isEmpty(), "int ctor builds an empty list");
        try { new RoleUnresolvedList(-1); check(false, "negative capacity must throw"); }
        catch (IllegalArgumentException e) { check("Illegal Capacity: -1".equals(e.getMessage()),
              "negative capacity msg: " + e.getMessage()); }
        java.util.List<RoleUnresolved> src = new java.util.ArrayList<RoleUnresolved>();
        src.add(unresOf("a", RoleStatus.NO_ROLE_WITH_NAME));
        src.add(unresOf("b", RoleStatus.ROLE_NOT_READABLE));
        RoleUnresolvedList copied = new RoleUnresolvedList(src);
        check(copied.size() == 2, "List ctor copies all elements");
        check(((RoleUnresolved) copied.get(0)).getRoleName().equals("a")
           && ((RoleUnresolved) copied.get(1)).getRoleName().equals("b"), "List ctor preserves order");
        check(((RoleUnresolved) copied.get(0)) == src.get(0), "List ctor keeps element identity (shallow)");
        try { new RoleUnresolvedList((java.util.List<RoleUnresolved>) null); check(false, "null List ctor must throw"); }
        catch (IllegalArgumentException e) { check("Null parameter".equals(e.getMessage()), "null List ctor msg"); }
    }

    // List-ctor type check wraps ClassCastException in IllegalArgumentException
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testConstructorRejectsNonRoleUnresolved() {
        java.util.List raw = new java.util.ArrayList();
        raw.add("oops");
        try { new RoleUnresolvedList(raw); check(false, "List ctor with non-RoleUnresolved must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("cannot be cast to"),
                  "non-RoleUnresolved ctor IAE wraps a ClassCastException, got: " + e.getMessage());
            check(e.getCause() instanceof ClassCastException, "ctor IAE cause is ClassCastException");
        }
        // a Role is not a RoleUnresolved: the list is strictly typed to its own element kind
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            @SuppressWarnings("rawtypes")
            public void run() {
                java.util.List mixed = new java.util.ArrayList();
                try { mixed.add(new Role("r", new java.util.ArrayList<ObjectName>())); }
                catch (Exception e) { throw new RuntimeException(e); }
                new RoleUnresolvedList(mixed);
            }
        }, "List ctor containing a Role (not RoleUnresolved) must throw IAE");
    }

    // add/set/get happy paths, index bounds, null handling with exact messages
    static void testAddSetGetAndBounds() {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        RoleUnresolved a = unresOf("a", RoleStatus.NO_ROLE_WITH_NAME);
        RoleUnresolved b = unresOf("b", RoleStatus.ROLE_NOT_WRITABLE);
        RoleUnresolved c = unresOf("c", RoleStatus.REF_MBEAN_NOT_REGISTERED);
        rul.add(a);
        check(rul.size() == 1 && rul.get(0) == a, "add(RoleUnresolved) appends, identity preserved");
        rul.add(0, b);
        check(rul.get(0) == b && rul.get(1) == a, "add(index, RoleUnresolved) inserts and shifts");
        java.util.ArrayList<Object> asArrayList = rul;
        Object previous = asArrayList.set(1, c);
        check(previous == a, "raw set(index, element) returns the previous element");
        check(rul.get(1) == c, "set replaces in place");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleUnresolvedList().add(5, unresOf("x", 1)); }
        }, "add(index, ...) beyond size must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleUnresolvedList().get(0); }
        }, "get on empty list must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleUnresolvedList().set(0, unresOf("x", 1)); }
        }, "set on empty list must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { rul.get(99); }
        }, "get beyond size must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { rul.set(-1, a); }
        }, "set with negative index must throw IOOBE");
        try { new RoleUnresolvedList().add((RoleUnresolved) null); check(false, "add(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add(null) msg"); }
        try { new RoleUnresolvedList().add(0, (RoleUnresolved) null); check(false, "add(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add(0,null) msg"); }
        try { new RoleUnresolvedList().set(0, (RoleUnresolved) null); check(false, "set(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()),
              "set(0,null) msg (RoleUnresolvedList omits the trailing period, unlike RoleList): " + e.getMessage()); }
    }

    // addAll(RoleUnresolvedList) / addAll(int, RoleUnresolvedList) semantics including null handling
    static void testAddAllForms() {
        RoleUnresolvedList base = new RoleUnresolvedList();
        base.add(unresOf("a", 1));
        RoleUnresolvedList more = new RoleUnresolvedList();
        more.add(unresOf("b", 2));
        more.add(unresOf("c", 3));
        check(base.addAll(more), "addAll(RoleUnresolvedList) returns true when changed");
        check(base.size() == 3, "addAll appends at the end");
        check(((RoleUnresolved) base.get(1)).getRoleName().equals("b")
           && ((RoleUnresolved) base.get(2)).getRoleName().equals("c"), "addAll preserves source order");
        check(!base.addAll(new RoleUnresolvedList()), "addAll(empty) returns false");
        check(base.addAll((RoleUnresolvedList) null), "addAll(null) is documented to return true");
        check(base.size() == 3, "addAll(null) changes nothing");
        RoleUnresolvedList front = new RoleUnresolvedList();
        front.add(unresOf("z", 4));
        check(base.addAll(0, front), "addAll(index, ...) returns true");
        check(((RoleUnresolved) base.get(0)).getRoleName().equals("z"), "addAll(index) inserts at the index");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { base.addAll(99, front); }
        }, "addAll(index, ...) beyond size must throw IOOBE");
        try { base.addAll(0, null); check(false, "addAll(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()),
              "addAll(0,null) msg (no trailing period in RoleUnresolvedList): " + e.getMessage()); }
    }

    // JDK-8 legacy compatibility: raw untyped adds tolerated before asList(); rejected afterwards
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testLegacyUntypedAccess() {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        check(rul.add("not-a-role-unresolved"), "JDK-8: raw add(non-RoleUnresolved) tolerated before asList()");
        check(rul.size() == 1, "tainted list actually holds the element");
        boolean asListThrew = false;
        try { rul.asList(); }
        catch (IllegalArgumentException e) {
            asListThrew = true;
            check(e.getMessage() != null && e.getMessage().contains("cannot be cast to"),
                  "asList() on a tainted list throws IAE wrapping CCE, got: " + e.getMessage());
        }
        check(asListThrew, "asList() after tainting must throw");
        RoleUnresolvedList rul2 = new RoleUnresolvedList();
        rul2.addAll(java.util.Arrays.asList("x", "y"));
        check(rul2.size() == 2, "JDK-8: raw addAll(Collection) with non-RoleUnresolved tolerated before asList()");
        final RoleUnresolvedList safe = new RoleUnresolvedList();
        safe.asList();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { safe.add("bad"); }
        }, "add(non-RoleUnresolved) after asList() must throw IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() {
                RoleUnresolvedList s = new RoleUnresolvedList();
                s.asList();
                s.listIterator().add("bad");
            }
        }, "listIterator().add(non-RoleUnresolved) after asList() must throw IAE");
        // null passes the internal cast: raw add((Object)null) accepted even in type-safe mode
        check(safe.add((Object) null), "raw add((Object)null) accepted after asList()");
        check(safe.size() == 1 && safe.get(0) == null, "null element actually stored");
    }

    // iteration order, iterator remove, remove/contains/indexOf/toArray
    static void testIterationAndRemoval() {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        rul.add(unresOf("a", 1));
        rul.add(unresOf("b", 2));
        StringBuilder seen = new StringBuilder();
        for (Object o : rul) seen.append(((RoleUnresolved) o).getRoleName()).append(";");
        check("a;b;".equals(seen.toString()), "iteration follows insertion order, got: " + seen);
        java.util.Iterator<Object> it = rul.iterator();
        it.next();
        it.remove();
        check(rul.size() == 1 && ((RoleUnresolved) rul.get(0)).getRoleName().equals("b"),
              "iterator.remove drops the first element");
        RoleUnresolved remaining = (RoleUnresolved) rul.get(0);
        check(rul.remove(remaining), "remove(Object) removes by identity");
        check(rul.isEmpty(), "list empty after removal");
        check(!rul.contains(remaining), "contains false after removal");
        rul.add(unresOf("c", 3));
        check(rul.indexOf(remaining) == -1 && rul.indexOf(rul.get(0)) == 0, "indexOf by identity");
        Object[] arr = rul.toArray();
        check(arr.length == 1 && arr[0] instanceof RoleUnresolved, "toArray returns the elements");
        rul.clear();
        check(rul.isEmpty(), "clear empties the list");
    }

    // asList() is a live bidirectional view, not a snapshot
    static void testAsListLiveView() {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        RoleUnresolved a = unresOf("a", 1);
        rul.add(a);
        java.util.List<RoleUnresolved> view = rul.asList();
        check(view.size() == 1 && view.get(0) == a, "view exposes current contents");
        rul.add(unresOf("b", 2));
        check(view.size() == 2, "adds through the list are visible in the view");
        view.remove(0);
        check(rul.size() == 1 && ((RoleUnresolved) rul.get(0)).getRoleName().equals("b"),
              "removals through the view are visible in the list");
        view.add(unresOf("c", 3));
        check(rul.size() == 2, "adds through the view are visible in the list");
        check(view == rul.asList(), "asList() returns the same underlying list (a cast view)");
    }

    // serialization round-trip + locked serialVersionUID
    static void testSerialization() throws Exception {
        RoleUnresolvedList orig = new RoleUnresolvedList();
        orig.add(unresOf("u1", RoleStatus.NO_ROLE_WITH_NAME));
        orig.add(unresOf("u2", RoleStatus.MORE_THAN_MAX_ROLE_DEGREE));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        RoleUnresolvedList back = (RoleUnresolvedList) ois.readObject();
        ois.close();
        check(back.size() == 2, "serial round-trip size");
        check(((RoleUnresolved) back.get(0)).getRoleName().equals("u1")
           && ((RoleUnresolved) back.get(0)).getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
              "serial first element content");
        check(((RoleUnresolved) back.get(1)).getProblemType() == RoleStatus.MORE_THAN_MAX_ROLE_DEGREE,
              "serial second element problem type");
        back.add(unresOf("u3", 1));
        check(back.size() == 3, "deserialized list remains mutable");
        check(java.io.ObjectStreamClass.lookup(RoleUnresolvedList.class).getSerialVersionUID()
              == 4054902803091433324L, "RoleUnresolvedList serialVersionUID is 4054902803091433324L");
        Object cl = orig.clone();
        check(cl instanceof RoleUnresolvedList, "inherited clone() keeps the RoleUnresolvedList runtime type");
        check(((RoleUnresolvedList) cl).size() == 2 && ((RoleUnresolvedList) cl).get(0) == orig.get(0),
              "clone is shallow: elements shared");
    }
}
