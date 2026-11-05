/*
 * @test
 * @summary javax.management.relation.RoleList conformance: white-box structure + black-box behavior.
 * @run main RoleListTest
 */
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.relation.Role;
import javax.management.relation.RoleList;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RoleListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static Role role(String name) {
        List<ObjectName> v = new ArrayList<>();
        try { v.add(new ObjectName("d:type=" + name)); } catch (Exception e) { throw new RuntimeException(e); }
        return new Role(name, v);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleList");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleList must be public");
        check(!c.isInterface(), "RoleList must be a class");
        check(c.getSuperclass() == java.util.ArrayList.class, "RoleList must extend ArrayList");
        c.getConstructor();
        c.getConstructor(int.class);
        c.getConstructor(List.class);
        c.getMethod("asList");
        c.getMethod("add", Role.class);
        c.getMethod("add", int.class, Role.class);
        c.getMethod("set", int.class, Role.class);
        c.getMethod("addAll", RoleList.class);
        c.getMethod("addAll", int.class, RoleList.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RoleList rl = new RoleList();
        check(rl.size() == 0, "new RoleList empty");

        Role r1 = role("a");
        Role r2 = role("b");
        Role r3 = role("c");

        rl.add(r1);
        check(rl.size() == 1 && ((Role) rl.get(0)).getRoleName().equals("a"), "add(Role) appends");

        rl.add(0, r2);
        check(((Role) rl.get(0)).getRoleName().equals("b"), "add(index, Role) inserts");

        Role old = (Role) rl.get(1);
        rl.set(1, r3);
        check("a".equals(old.getRoleName()) && "c".equals(((Role) rl.get(1)).getRoleName()), "set(index, Role) replaces");

        RoleList other = new RoleList();
        other.add(role("d"));
        other.add(role("e"));
        check(rl.addAll(other), "addAll(RoleList) reports change");
        check(rl.size() == 4, "addAll(RoleList) size");

        RoleList prefix = new RoleList();
        prefix.add(role("z"));
        check(rl.addAll(0, prefix), "addAll(index, RoleList) reports change");
        check("z".equals(((Role) rl.get(0)).getRoleName()), "addAll(index, RoleList) inserts at index");

        // ordering is preserved
        List<String> names = new ArrayList<>();
        for (Object o : rl) names.add(((Role) o).getRoleName());
        check("z,b,c,d,e".equals(String.join(",", names)), "insertion order preserved, got: " + names);

        // asList is a live view and switches the list to type-safe mode
        RoleList viewList = new RoleList();
        viewList.add(role("v"));
        List<Role> view = viewList.asList();
        check(view.size() == 1, "asList view size");
        viewList.add(role("w"));
        check(view.size() == 2, "asList is a live view");
        boolean threw = false;
        try { ((java.util.ArrayList) viewList).add("not-a-role"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "after asList(), adding non-Role via raw add(Object) must throw");

        // constructor(List<Role>) rejects non-Role elements
        List raw = new ArrayList();
        raw.add("oops");
        try { new RoleList(raw); check(false, "ctor with non-Role must throw"); }
        catch (IllegalArgumentException e) { /* ClassCastException wrapped as IAE */ }

        // exact null-parameter messages
        try { new RoleList((List<Role>) null); check(false, "null list ctor must throw"); }
        catch (IllegalArgumentException e) { check("Null parameter".equals(e.getMessage()), "null ctor message"); }
        try { new RoleList().add((Role) null); check(false, "add(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add null message"); }
        try { new RoleList().add(0, (Role) null); check(false, "add(index,null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add(index) null message"); }
        try { new RoleList().set(0, (Role) null); check(false, "set(index,null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "set null message"); }
        try { new RoleList().addAll(0, (RoleList) null); check(false, "addAll(index,null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "addAll null message"); }
        check(new RoleList().addAll((RoleList) null), "addAll(null RoleList) returns true (documented)");
    }

    // ---- deepened (batch j): strict typing, bounds, iteration, serialization ----

    // JDK-27 (unlike JDK-8) rejects non-Role elements through the raw Object overloads immediately,
    // whether or not asList() has ever been called on the instance
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testStrictTyping() {
        boolean threw = false;
        try { new RoleList().add("not-a-role"); }
        catch (IllegalArgumentException e) {
            threw = true;
            check(e.getCause() instanceof ClassCastException, "raw add(non-Role) IAE wraps a ClassCastException");
        }
        check(threw, "raw add(non-Role) before asList() throws IAE immediately (JDK-7+ semantics)");
        threw = false;
        try { new RoleList().addAll(java.util.Arrays.asList("x", "y")); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "raw addAll(Collection) with non-Roles throws IAE");
        threw = false;
        try { new RoleList().addAll(0, java.util.Arrays.asList("x")); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "raw addAll(index, Collection) with non-Roles throws IAE");
        // null passes the internal cast: raw add((Object)null) is accepted even in type-safe mode
        RoleList safe = new RoleList();
        safe.asList();
        check(safe.add((Object) null), "raw add((Object)null) accepted (null passes the cast)");
        check(safe.size() == 1 && safe.get(0) == null, "null element actually stored");
    }

    static void testBoundsAndCapacity() {
        try { new RoleList(-1); check(false, "negative capacity must throw"); }
        catch (IllegalArgumentException e) { check("Illegal Capacity: -1".equals(e.getMessage()),
              "negative capacity msg: " + e.getMessage()); }
        boolean threw = false;
        try { new RoleList().add(5, role("x")); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "add(index, Role) beyond size throws IOOBE");
        threw = false;
        try { new RoleList().get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "get on empty list throws IOOBE");
        threw = false;
        try { new RoleList().set(0, role("x")); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "set on empty list throws IOOBE");
        // the ArrayList-level raw set returns the replaced element
        RoleList rl = new RoleList();
        rl.add(role("a"));
        rl.add(role("b"));
        java.util.ArrayList<Object> asArrayList = rl;
        Object previous = asArrayList.set(1, role("c"));
        check("b".equals(((Role) previous).getRoleName()), "raw set returns the previous element");
        check("c".equals(((Role) rl.get(1)).getRoleName()), "set replaces in place");
    }

    static void testIterationAndRemoval() {
        RoleList rl = new RoleList();
        rl.add(role("a"));
        rl.add(role("b"));
        java.util.Iterator<Object> it = rl.iterator();
        it.next();
        it.remove();
        check(rl.size() == 1 && "b".equals(((Role) rl.get(0)).getRoleName()),
              "iterator.remove drops the first element");
        Role remaining = (Role) rl.get(0);
        check(rl.remove(remaining), "remove(Object) removes by identity");
        check(rl.isEmpty() && !rl.contains(remaining), "list empty, contains false after removal");
        RoleList one = new RoleList();
        one.add(role("z"));
        Object[] arr = one.toArray();
        check(arr.length == 1 && arr[0] instanceof Role, "toArray returns the elements");
    }

    static void testSerialization() throws Exception {
        RoleList orig = new RoleList();
        orig.add(role("s1"));
        orig.add(role("s2"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        RoleList back = (RoleList) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.size() == 2, "serial round-trip size");
        check("s1".equals(((Role) back.get(0)).getRoleName())
           && "s2".equals(((Role) back.get(1)).getRoleName()), "serial preserves order");
        back.add(role("s3"));
        check(back.size() == 3, "deserialized list remains mutable");
        check(java.io.ObjectStreamClass.lookup(RoleList.class).getSerialVersionUID() == 5568344346499649313L,
              "RoleList serialVersionUID is 5568344346499649313L");
        // inherited ArrayList.clone() keeps the RoleList runtime type (shallow copy)
        Object cl = orig.clone();
        check(cl instanceof RoleList, "inherited clone() keeps the RoleList runtime type");
        check(((RoleList) cl).size() == 2 && ((RoleList) cl).get(0) == orig.get(0), "clone is shallow: elements shared");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStrictTyping();
        testBoundsAndCapacity();
        testIterationAndRemoval();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
