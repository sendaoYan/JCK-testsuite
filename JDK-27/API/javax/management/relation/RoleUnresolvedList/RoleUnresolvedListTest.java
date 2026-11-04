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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
