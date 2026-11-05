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

    // ---- deepened (batch j): ctor edges, identity equals, serialization ----

    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testCtorEdges() throws Exception {
        // empty name and empty value are legal (only null is rejected)
        Role empty = new Role("", new ArrayList<ObjectName>());
        check("".equals(empty.getRoleName()) && empty.getRoleValue().isEmpty(), "empty name/value accepted");
        check("role name: ; role value: ".equals(empty.toString()), "toString empty form, got: " + empty.toString());
        // generics are erased at runtime: the constructor performs no per-element type check
        List raw = new ArrayList();
        raw.add("not-an-objectname");
        Role lax = new Role("lax", raw);
        check(lax.getRoleValue().size() == 1 && "not-an-objectname".equals(lax.getRoleValue().get(0)),
              "raw list with non-ObjectName element accepted (no element validation)");
        raw.add(null);
        Role nullElem = new Role("ne", raw);
        check(nullElem.getRoleValue().size() == 2 && nullElem.getRoleValue().get(1) == null,
              "null element accepted and preserved");
        // getRoleValue exposes the live internal list: structural edits through it change Role state
        Role live = new Role("live", new ArrayList<ObjectName>(java.util.Arrays.asList(on("d:t=1"))));
        live.getRoleValue().add(on("d:t=2"));
        check(live.getRoleValue().size() == 2, "getRoleValue returns the live internal list");
        // setters defensively copy (complement of the ctor check above)
        List<ObjectName> repl = new ArrayList<ObjectName>();
        repl.add(on("d:k=a"));
        live.setRoleValue(repl);
        repl.add(on("d:k=b"));
        check(live.getRoleValue().size() == 1, "setRoleValue defensively copies its argument");
        live.setRoleName("");
        check("".equals(live.getRoleName()), "setRoleName accepts empty string");
        // roleValueToString preserves insertion order (no sorting)
        check("d:z=1\nd:a=2\nd:m=3".equals(Role.roleValueToString(
                new ArrayList<ObjectName>(java.util.Arrays.asList(on("d:z=1"), on("d:a=2"), on("d:m=3"))))),
              "roleValueToString preserves insertion order");
    }

    // Role does not override equals/hashCode: identity semantics (verified on JDK-27)
    static void testIdentityEquals() {
        Role s1 = new Role("x", new ArrayList<ObjectName>(java.util.Arrays.asList(on("d:t=a"))));
        Role s2 = new Role("x", new ArrayList<ObjectName>(java.util.Arrays.asList(on("d:t=a"))));
        check(!s1.equals(s2), "no equals override: same-content Roles are not equal");
        check(s1.equals(s1), "identity equals is reflexive");
        check(!s1.equals("a string"), "equals against an unrelated type is false");
        check(s1.hashCode() == System.identityHashCode(s1), "hashCode is the Object identity hash");
        // clone produces a fresh instance whose value list is independent
        Role c = (Role) s1.clone();
        s1.getRoleValue().clear();
        check(c.getRoleValue().size() == 1, "clone unaffected by later mutation of the original");
    }

    // serialization round-trip with order preservation and locked serialVersionUID
    static void testSerialization() throws Exception {
        Role orig = new Role("ser", new ArrayList<ObjectName>(
                java.util.Arrays.asList(on("d:t=1"), on("d:t=2"), on("d:t=3"))));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        Role back = (Role) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("ser".equals(back.getRoleName()), "serial round-trip name");
        check(back.getRoleValue().size() == 3
           && back.getRoleValue().get(0).equals(on("d:t=1"))
           && back.getRoleValue().get(1).equals(on("d:t=2"))
           && back.getRoleValue().get(2).equals(on("d:t=3")), "serial preserves value order");
        check(back.getRoleValue() instanceof ArrayList, "deserialized value is an ArrayList");
        orig.getRoleValue().clear();
        check(back.getRoleValue().size() == 3, "deserialized copy independent of the original");
        check(java.io.ObjectStreamClass.lookup(Role.class).getSerialVersionUID() == -279985518429862552L,
              "Role serialVersionUID is -279985518429862552L");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCtorEdges();
        testIdentityEquals();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
