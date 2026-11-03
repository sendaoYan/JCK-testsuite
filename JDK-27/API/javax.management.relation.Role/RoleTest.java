/*
 * @test
 * @summary javax.management.relation.Role conformance: white-box structure + black-box behavior.
 * @run main RoleTest
 */
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.relation.Role;

public class RoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Role");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Role must be public");
        check(!c.isInterface(), "Role must be a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Role must be concrete");
        check(java.io.Serializable.class.isAssignableFrom(c), "Role must implement Serializable");
        c.getConstructor(String.class, List.class);
        c.getMethod("getRoleName");
        c.getMethod("getRoleValue");
        c.getMethod("setRoleName", String.class);
        c.getMethod("setRoleValue", List.class);
        c.getMethod("toString");
        c.getMethod("clone");
        c.getMethod("roleValueToString", List.class);
        check(c.getMethod("roleValueToString", List.class).getReturnType() == String.class, "roleValueToString returns String");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName a = on("d:type=a");
        ObjectName b = on("d:type=b");
        ObjectName c3 = on("d:type=c");

        List<ObjectName> list2 = new ArrayList<>();
        list2.add(a); list2.add(b);

        // construction + getters
        Role r = new Role("roleA", list2);
        check("roleA".equals(r.getRoleName()), "getRoleName after construct");
        check(r.getRoleValue().equals(list2), "getRoleValue returns role value");
        check(r.getRoleValue().size() == 2, "role value size");

        // defensive copy on construction
        list2.add(c3);
        check(r.getRoleValue().size() == 2, "role value must be defensively copied on construction");

        // setters
        r.setRoleName("roleB");
        check("roleB".equals(r.getRoleName()), "setRoleName then getRoleName");
        List<ObjectName> one = new ArrayList<>(); one.add(a);
        r.setRoleValue(one);
        check(r.getRoleValue().size() == 1 && r.getRoleValue().get(0).equals(a), "setRoleValue then getRoleValue");

        // toString
        Role r2 = new Role("myRole", new ArrayList<>(java.util.Arrays.asList(a, b)));
        check("role name: myRole; role value: d:type=a, d:type=b".equals(r2.toString()),
              "toString format, got: " + r2.toString());

        // roleValueToString uses newline separation (per API contract)
        check("d:type=a\nd:type=b".equals(Role.roleValueToString(new ArrayList<>(java.util.Arrays.asList(a, b)))),
              "roleValueToString newline separation");
        check("".equals(Role.roleValueToString(new ArrayList<ObjectName>())), "roleValueToString empty list");

        // clone independence
        Role r3 = new Role("cloneMe", new ArrayList<>(java.util.Arrays.asList(a, b)));
        Object cl = r3.clone();
        check(cl instanceof Role, "clone returns a Role");
        Role r3c = (Role) cl;
        check(r3c != r3, "clone is a distinct instance");
        check("cloneMe".equals(r3c.getRoleName()), "clone preserves name");
        check(r3c.getRoleValue().equals(r3.getRoleValue()), "clone preserves value");
        check(r3c.getRoleValue() != r3.getRoleValue(), "clone value must be an independent list");

        // exceptions with exact messages
        try { new Role(null, new ArrayList<ObjectName>()); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "null name message: " + e.getMessage()); }
        try { new Role("r", null); check(false, "null value must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "null value message"); }
        try { r.setRoleName(null); check(false, "setRoleName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleName null message"); }
        try { r.setRoleValue(null); check(false, "setRoleValue(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleValue null message"); }
        try { Role.roleValueToString(null); check(false, "roleValueToString(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "roleValueToString null message"); }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
