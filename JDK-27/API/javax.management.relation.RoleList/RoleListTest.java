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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
