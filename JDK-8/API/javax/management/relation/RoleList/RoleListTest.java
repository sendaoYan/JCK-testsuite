/*
 * @test
 * @summary javax.management.relation.RoleList conformance: add/asList.
 * @run main RoleListTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        RoleList rl = new RoleList();
        Role r = new Role("r", new java.util.ArrayList<ObjectName>());
        rl.add(r);
        check(rl.size() == 1, "size");
        check(rl.asList().get(0) == r, "asList");
        testConstructorForms();
        testConstructorRejectsNonRole();
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

    static Role roleOf(String name, String onSpec) {
        java.util.List<ObjectName> v = new java.util.ArrayList<ObjectName>();
        try { v.add(new ObjectName(onSpec)); } catch (Exception e) { throw new RuntimeException(e); }
        return new Role(name, v);
    }

    // ()/(int)/(List<Role>) constructors, capacity validation, null rejection
    static void testConstructorForms() {
        check(new RoleList().isEmpty(), "no-arg ctor builds an empty list");
        check(new RoleList(8).isEmpty(), "int ctor builds an empty list");
        try { new RoleList(-1); check(false, "negative capacity must throw"); }
        catch (IllegalArgumentException e) { check("Illegal Capacity: -1".equals(e.getMessage()),
              "negative capacity msg: " + e.getMessage()); }
        java.util.List<Role> src = new java.util.ArrayList<Role>();
        src.add(roleOf("a", "d:t=a"));
        src.add(roleOf("b", "d:t=b"));
        RoleList copied = new RoleList(src);
        check(copied.size() == 2, "List ctor copies all elements");
        check(((Role) copied.get(0)).getRoleName().equals("a")
           && ((Role) copied.get(1)).getRoleName().equals("b"), "List ctor preserves order");
        check(((Role) copied.get(0)) == src.get(0), "List ctor keeps element identity (shallow)");
        try { new RoleList((java.util.List<Role>) null); check(false, "null List ctor must throw"); }
        catch (IllegalArgumentException e) { check("Null parameter".equals(e.getMessage()), "null List ctor msg"); }
    }

    // List-Role ctor wraps ClassCastException in IllegalArgumentException
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testConstructorRejectsNonRole() {
        java.util.List raw = new java.util.ArrayList();
        raw.add("oops");
        try { new RoleList(raw); check(false, "List ctor with non-Role must throw"); }
        catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("cannot be cast to"),
                  "non-Role ctor IAE wraps a ClassCastException, got: " + e.getMessage());
            check(e.getCause() instanceof ClassCastException, "non-Role ctor IAE cause is ClassCastException");
        }
        // a single bad element among good ones is enough
        java.util.List mixed = new java.util.ArrayList();
        mixed.add(roleOf("ok", "d:t=ok"));
        mixed.add("bad");
        try { new RoleList(mixed); check(false, "mixed List ctor must throw"); }
        catch (IllegalArgumentException e) { check(true, "mixed List ctor throws IAE"); }
    }

    // add/set/get happy paths, index bounds, null handling with exact messages
    static void testAddSetGetAndBounds() {
        RoleList rl = new RoleList();
        Role a = roleOf("a", "d:t=a");
        Role b = roleOf("b", "d:t=b");
        Role c = roleOf("c", "d:t=c");
        rl.add(a);
        check(rl.size() == 1 && rl.get(0) == a, "add(Role) appends, identity preserved");
        rl.add(0, b);
        check(rl.get(0) == b && rl.get(1) == a, "add(index, Role) inserts and shifts");
        // set(int, Role) returns void; the ArrayList-level raw set returns the replaced element
        java.util.ArrayList<Object> asArrayList = rl;
        Object previous = asArrayList.set(1, c);
        check(previous == a, "raw set(index, element) returns the previous element");
        check(rl.get(1) == c, "set replaces in place");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleList().add(5, roleOf("x", "d:t=x")); }
        }, "add(index, Role) beyond size must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleList().get(0); }
        }, "get on empty list must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { new RoleList().set(0, roleOf("x", "d:t=x")); }
        }, "set on empty list must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { rl.get(99); }
        }, "get beyond size must throw IOOBE");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { rl.set(-1, a); }
        }, "set with negative index must throw IOOBE");
        // null Role arguments
        try { new RoleList().add((Role) null); check(false, "add(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add(null) msg"); }
        try { new RoleList().add(0, (Role) null); check(false, "add(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add(0,null) msg"); }
        try { new RoleList().set(0, (Role) null); check(false, "set(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "set(0,null) msg"); }
    }

    // addAll(RoleList) / addAll(int, RoleList) semantics including documented null handling
    static void testAddAllForms() {
        RoleList base = new RoleList();
        base.add(roleOf("a", "d:t=a"));
        RoleList more = new RoleList();
        more.add(roleOf("b", "d:t=b"));
        more.add(roleOf("c", "d:t=c"));
        check(base.addAll(more), "addAll(RoleList) returns true when changed");
        check(base.size() == 3, "addAll appends at the end");
        check(((Role) base.get(1)).getRoleName().equals("b")
           && ((Role) base.get(2)).getRoleName().equals("c"), "addAll preserves source order");
        check(!base.addAll(new RoleList()), "addAll(empty RoleList) returns false");
        check(base.addAll((RoleList) null), "addAll(null RoleList) is documented to return true");
        check(base.size() == 3, "addAll(null) changes nothing");
        RoleList front = new RoleList();
        front.add(roleOf("z", "d:t=z"));
        check(base.addAll(0, front), "addAll(index, RoleList) returns true");
        check(((Role) base.get(0)).getRoleName().equals("z"), "addAll(index) inserts at the index");
        checkThrows(IndexOutOfBoundsException.class, new ThrowingRunnable() {
            public void run() { base.addAll(99, front); }
        }, "addAll(index, RoleList) beyond size must throw IOOBE");
        try { base.addAll(0, null); check(false, "addAll(0, null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "addAll(0,null) msg"); }
    }

    // JDK-8 legacy compatibility: before asList() is ever called, raw untyped adds are tolerated
    // (the list is "tainted" and a later asList() throws); afterwards every add is type-checked
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testLegacyUntypedAccess() {
        RoleList rl = new RoleList();
        check(rl.add("not-a-role"), "JDK-8: raw add(non-Role) tolerated before asList()");
        check(rl.size() == 1, "tainted list actually holds the element");
        boolean asListThrew = false;
        try { rl.asList(); }
        catch (IllegalArgumentException e) {
            asListThrew = true;
            check(e.getMessage() != null && e.getMessage().contains("cannot be cast to"),
                  "asList() on a tainted list throws IAE wrapping CCE, got: " + e.getMessage());
        }
        check(asListThrew, "asList() after tainting must throw");
        // collection variant taints as well
        RoleList rl2 = new RoleList();
        rl2.addAll(java.util.Arrays.asList("x", "y"));
        check(rl2.size() == 2, "JDK-8: raw addAll(Collection) with non-Roles tolerated before asList()");
        RoleList rl2b = new RoleList();
        rl2b.addAll(0, java.util.Arrays.asList("x"));
        check(rl2b.size() == 1, "JDK-8: raw addAll(index, Collection) tolerated before asList()");
        // a fresh list flips into type-safe mode once asList() has been called
        final RoleList safe = new RoleList();
        safe.asList();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { safe.add("bad"); }
        }, "add(non-Role) after asList() must throw IAE");
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() {
                RoleList s = new RoleList();
                s.asList();
                s.listIterator().add("bad");
            }
        }, "listIterator().add(non-Role) after asList() must throw IAE");
        // null passes the internal cast: raw add((Object)null) is accepted even in type-safe mode
        check(safe.add((Object) null), "raw add((Object)null) accepted after asList()");
        check(safe.size() == 1 && safe.get(0) == null, "null element actually stored");
    }

    // iteration order, iterator remove, remove/contains/indexOf/toArray
    static void testIterationAndRemoval() {
        RoleList rl = new RoleList();
        rl.add(roleOf("a", "d:t=a"));
        rl.add(roleOf("b", "d:t=b"));
        StringBuilder seen = new StringBuilder();
        for (Object o : rl) seen.append(((Role) o).getRoleName()).append(";");
        check("a;b;".equals(seen.toString()), "iteration follows insertion order, got: " + seen);
        java.util.Iterator<Object> it = rl.iterator();
        it.next();
        it.remove();
        check(rl.size() == 1 && ((Role) rl.get(0)).getRoleName().equals("b"), "iterator.remove drops the first element");
        Role remaining = (Role) rl.get(0);
        check(rl.remove(remaining), "remove(Object) removes by identity");
        check(rl.isEmpty(), "list empty after removal");
        check(!rl.contains(remaining), "contains false after removal");
        rl.add(roleOf("c", "d:t=c"));
        check(rl.indexOf(remaining) == -1 && rl.indexOf(rl.get(0)) == 0, "indexOf by identity");
        Object[] arr = rl.toArray();
        check(arr.length == 1 && arr[0] instanceof Role, "toArray returns the elements");
        // clear works through the ArrayList contract
        rl.clear();
        check(rl.isEmpty(), "clear empties the list");
    }

    // asList() is a live bidirectional view, not a snapshot
    static void testAsListLiveView() {
        RoleList rl = new RoleList();
        Role a = roleOf("a", "d:t=a");
        rl.add(a);
        java.util.List<Role> view = rl.asList();
        check(view.size() == 1 && view.get(0) == a, "view exposes current contents");
        rl.add(roleOf("b", "d:t=b"));
        check(view.size() == 2, "adds through the RoleList are visible in the view");
        view.remove(0);
        check(rl.size() == 1 && ((Role) rl.get(0)).getRoleName().equals("b"),
              "removals through the view are visible in the RoleList");
        view.add(roleOf("c", "d:t=c"));
        check(rl.size() == 2, "adds through the view are visible in the RoleList");
        check(view == rl.asList(), "asList() returns the same underlying list (a cast view)");
    }

    // serialization round-trip incl. nested Role contents + locked serialVersionUID
    static void testSerialization() throws Exception {
        java.util.List<ObjectName> two = new java.util.ArrayList<ObjectName>();
        try {
            two.add(new ObjectName("d:t=1"));
            two.add(new ObjectName("d:t=2"));
        } catch (Exception e) { throw new RuntimeException(e); }
        RoleList orig = new RoleList();
        orig.add(new Role("s1", two));
        orig.add(roleOf("s2", "d:t=3"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        RoleList back = (RoleList) ois.readObject();
        ois.close();
        check(back.size() == 2, "serial round-trip size");
        check(((Role) back.get(0)).getRoleName().equals("s1"), "serial first element name");
        check(((Role) back.get(0)).getRoleValue().size() == 2, "serial nested role value size");
        check(((Role) back.get(0)).getRoleValue().get(0).equals(new ObjectName("d:t=1")), "serial nested order");
        back.add(roleOf("s3", "d:t=4"));
        check(back.size() == 3, "deserialized list remains mutable");
        check(java.io.ObjectStreamClass.lookup(RoleList.class).getSerialVersionUID() == 5568344346499649313L,
              "RoleList serialVersionUID is 5568344346499649313L");
        // inherited ArrayList.clone() keeps the RoleList runtime type (shallow copy)
        Object cl = orig.clone();
        check(cl instanceof RoleList, "inherited clone() keeps the RoleList runtime type");
        check(((RoleList) cl).size() == 2 && ((RoleList) cl).get(0) == orig.get(0), "clone is shallow: elements shared");
    }
}
