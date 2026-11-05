/*
 * @test
 * @summary javax.management.relation.RoleUnresolvedList conformance: white-box structure + black-box behavior.
 * @run main RoleUnresolvedListTest
 */
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleUnresolved;
import javax.management.relation.RoleUnresolvedList;

@SuppressWarnings({"rawtypes", "unchecked"})
public class RoleUnresolvedListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static RoleUnresolved ru(String name) {
        return new RoleUnresolved(name, null, RoleStatus.NO_ROLE_WITH_NAME);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleUnresolvedList");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleUnresolvedList must be public");
        check(!c.isInterface(), "RoleUnresolvedList must be a class");
        check(c.getSuperclass() == java.util.ArrayList.class, "RoleUnresolvedList must extend ArrayList");
        c.getConstructor(); c.getConstructor(int.class); c.getConstructor(List.class);
        c.getMethod("asList");
        c.getMethod("add", RoleUnresolved.class);
        c.getMethod("add", int.class, RoleUnresolved.class);
        c.getMethod("set", int.class, RoleUnresolved.class);
        c.getMethod("addAll", RoleUnresolvedList.class);
        c.getMethod("addAll", int.class, RoleUnresolvedList.class);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        check(rul.size() == 0, "new RoleUnresolvedList empty");

        RoleUnresolved r1 = ru("a");
        RoleUnresolved r2 = ru("b");
        RoleUnresolved r3 = ru("c");

        rul.add(r1);
        check(rul.size() == 1 && ((RoleUnresolved) rul.get(0)).getRoleName().equals("a"), "add appends");
        rul.add(0, r2);
        check(((RoleUnresolved) rul.get(0)).getRoleName().equals("b"), "add(index) inserts");
        RoleUnresolved old = (RoleUnresolved) rul.get(1);
        rul.set(1, r3);
        check("a".equals(old.getRoleName()) && "c".equals(((RoleUnresolved) rul.get(1)).getRoleName()), "set replaces");

        RoleUnresolvedList other = new RoleUnresolvedList();
        other.add(ru("d")); other.add(ru("e"));
        check(rul.addAll(other), "addAll reports change");
        check(rul.size() == 4, "addAll size");

        RoleUnresolvedList prefix = new RoleUnresolvedList();
        prefix.add(ru("z"));
        check(rul.addAll(0, prefix), "addAll(index) reports change");
        check("z".equals(((RoleUnresolved) rul.get(0)).getRoleName()), "addAll(index) inserts at index");

        List<String> names = new ArrayList<>();
        for (Object o : rul) names.add(((RoleUnresolved) o).getRoleName());
        check("z,b,c,d,e".equals(String.join(",", names)), "insertion order preserved, got: " + names);

        // asList live view + type-safe switch
        RoleUnresolvedList viewList = new RoleUnresolvedList();
        viewList.add(ru("v"));
        List<RoleUnresolved> view = viewList.asList();
        check(view.size() == 1, "asList view size");
        viewList.add(ru("w"));
        check(view.size() == 2, "asList live view");
        boolean threw = false;
        try { ((java.util.ArrayList) viewList).add("not-a-role-unresolved"); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "after asList(), raw add(Object) non-RoleUnresolved must throw");

        // constructor rejects non-RoleUnresolved
        List raw = new ArrayList(); raw.add("oops");
        try { new RoleUnresolvedList(raw); check(false, "ctor with non-RoleUnresolved must throw"); }
        catch (IllegalArgumentException e) { /* wrapped ClassCastException */ }

        try { new RoleUnresolvedList((List<RoleUnresolved>) null); check(false, "null ctor must throw"); }
        catch (IllegalArgumentException e) { check("Null parameter".equals(e.getMessage()), "null ctor message"); }
        try { new RoleUnresolvedList().add((RoleUnresolved) null); check(false, "add(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "add null message"); }
        try { new RoleUnresolvedList().set(0, (RoleUnresolved) null); check(false, "set(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "set null message"); }
        try { new RoleUnresolvedList().addAll(0, (RoleUnresolvedList) null); check(false, "addAll(index,null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "addAll null message"); }
        check(new RoleUnresolvedList().addAll((RoleUnresolvedList) null), "addAll(null) returns true (documented)");
    }

    // ---- deepened (batch j): strict typing, bounds, iteration, serialization ----

    // JDK-27 (unlike JDK-8) rejects non-RoleUnresolved elements through the raw Object overloads immediately
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testStrictTyping() {
        boolean threw = false;
        try { new RoleUnresolvedList().add("not-a-role-unresolved"); }
        catch (IllegalArgumentException e) {
            threw = true;
            check(e.getCause() instanceof ClassCastException, "raw add IAE wraps a ClassCastException");
        }
        check(threw, "raw add(non-RoleUnresolved) before asList() throws IAE immediately (JDK-7+ semantics)");
        threw = false;
        try { new RoleUnresolvedList().addAll(java.util.Arrays.asList("x", "y")); }
        catch (IllegalArgumentException e) { threw = true; }
        check(threw, "raw addAll(Collection) with non-RoleUnresolved throws IAE");
        // a Role is not a RoleUnresolved: the list is strictly typed to its own element kind
        threw = false;
        try {
            List rawWithRole = new ArrayList();
            rawWithRole.add(new javax.management.relation.Role("r", new ArrayList<ObjectName>()));
            new RoleUnresolvedList(rawWithRole);
        } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "ctor List containing a Role (not RoleUnresolved) throws IAE");
        // null passes the internal cast: raw add((Object)null) is accepted even in type-safe mode
        RoleUnresolvedList safe = new RoleUnresolvedList();
        safe.asList();
        check(safe.add((Object) null), "raw add((Object)null) accepted (null passes the cast)");
        check(safe.size() == 1 && safe.get(0) == null, "null element actually stored");
    }

    static void testBoundsAndCapacity() {
        try { new RoleUnresolvedList(-1); check(false, "negative capacity must throw"); }
        catch (IllegalArgumentException e) { check("Illegal Capacity: -1".equals(e.getMessage()),
              "negative capacity msg: " + e.getMessage()); }
        boolean threw = false;
        try { new RoleUnresolvedList().add(5, ru("x")); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "add(index, ...) beyond size throws IOOBE");
        threw = false;
        try { new RoleUnresolvedList().get(0); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "get on empty list throws IOOBE");
        threw = false;
        try { new RoleUnresolvedList().set(0, ru("x")); } catch (IndexOutOfBoundsException e) { threw = true; }
        check(threw, "set on empty list throws IOOBE");
        // the ArrayList-level raw set returns the replaced element
        RoleUnresolvedList rul = new RoleUnresolvedList();
        rul.add(ru("a"));
        rul.add(ru("b"));
        java.util.ArrayList<Object> asArrayList = rul;
        Object previous = asArrayList.set(1, ru("c"));
        check("b".equals(((RoleUnresolved) previous).getRoleName()), "raw set returns the previous element");
        check("c".equals(((RoleUnresolved) rul.get(1)).getRoleName()), "set replaces in place");
    }

    static void testIterationAndRemoval() {
        RoleUnresolvedList rul = new RoleUnresolvedList();
        rul.add(ru("a"));
        rul.add(ru("b"));
        java.util.Iterator<Object> it = rul.iterator();
        it.next();
        it.remove();
        check(rul.size() == 1 && "b".equals(((RoleUnresolved) rul.get(0)).getRoleName()),
              "iterator.remove drops the first element");
        RoleUnresolved remaining = (RoleUnresolved) rul.get(0);
        check(rul.remove(remaining), "remove(Object) removes by identity");
        check(rul.isEmpty() && !rul.contains(remaining), "list empty, contains false after removal");
    }

    static void testSerialization() throws Exception {
        RoleUnresolvedList orig = new RoleUnresolvedList();
        orig.add(new RoleUnresolved("u1", null, RoleStatus.NO_ROLE_WITH_NAME));
        orig.add(new RoleUnresolved("u2", null, RoleStatus.MORE_THAN_MAX_ROLE_DEGREE));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        RoleUnresolvedList back = (RoleUnresolvedList) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back.size() == 2, "serial round-trip size");
        check("u1".equals(((RoleUnresolved) back.get(0)).getRoleName())
           && ((RoleUnresolved) back.get(0)).getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
              "serial first element content");
        check(((RoleUnresolved) back.get(1)).getProblemType() == RoleStatus.MORE_THAN_MAX_ROLE_DEGREE,
              "serial second element problem type");
        back.add(ru("u3"));
        check(back.size() == 3, "deserialized list remains mutable");
        check(java.io.ObjectStreamClass.lookup(RoleUnresolvedList.class).getSerialVersionUID()
              == 4054902803091433324L, "RoleUnresolvedList serialVersionUID is 4054902803091433324L");
        Object cl = orig.clone();
        check(cl instanceof RoleUnresolvedList, "inherited clone() keeps the RoleUnresolvedList runtime type");
        check(((RoleUnresolvedList) cl).size() == 2 && ((RoleUnresolvedList) cl).get(0) == orig.get(0),
              "clone is shallow: elements shared");
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
