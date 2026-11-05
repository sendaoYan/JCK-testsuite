/*
 * @test
 * @summary javax.management.relation.Role conformance: name/value.
 * @run main RoleTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<ObjectName> l = new java.util.ArrayList<ObjectName>();
        l.add(new ObjectName("d:type=x"));
        Role r = new Role("rname", l);
        check("rname".equals(r.getRoleName()), "role name");
        check(r.getRoleValue().size() == 1, "role value size");
        check(r.clone() != r, "clone");
        testCtorNullAndTypeLaxity();
        testGettersLiveAndDefensiveCopy();
        testCloneEqualsHashCode();
        testToStringFormats();
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

    static java.util.List<ObjectName> names(String... specs) {
        java.util.List<ObjectName> out = new java.util.ArrayList<ObjectName>();
        for (String s : specs) {
            try { out.add(new ObjectName(s)); } catch (Exception e) { throw new RuntimeException(e); }
        }
        return out;
    }

    // constructor null matrix, empty-name legality, and the absence of element type validation
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void testCtorNullAndTypeLaxity() {
        try { new Role(null, new java.util.ArrayList<ObjectName>()); check(false, "ctor null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "ctor null name msg: " + e.getMessage()); }
        try { new Role("r", null); check(false, "ctor null value must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "ctor null value msg: " + e.getMessage()); }
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new Role(null, null); }
        }, "ctor both null must throw IAE");
        // empty name and empty value are legal (only null is rejected)
        Role empty = new Role("", new java.util.ArrayList<ObjectName>());
        check("".equals(empty.getRoleName()), "empty role name accepted");
        check(empty.getRoleValue().isEmpty(), "empty role value accepted");
        // generics are erased at runtime: the constructor performs no per-element type check
        java.util.List raw = new java.util.ArrayList();
        raw.add("not-an-objectname");
        Role lax = new Role("lax", raw);
        check(lax.getRoleValue().size() == 1, "raw list with non-ObjectName element accepted (no element validation)");
        check("not-an-objectname".equals(lax.getRoleValue().get(0)), "raw element preserved as-is");
        java.util.List withNull = new java.util.ArrayList();
        withNull.add(null);
        Role nullElem = new Role("ne", withNull);
        check(nullElem.getRoleValue().size() == 1 && nullElem.getRoleValue().get(0) == null,
              "null element accepted and preserved");
    }

    // getter liveness + defensive copies in constructor and setters
    static void testGettersLiveAndDefensiveCopy() throws Exception {
        java.util.List<ObjectName> src = names("d:k=1", "d:k=2");
        Role r = new Role("live", src);
        check(r.getRoleValue() != src, "constructor defensively copies the source list");
        src.add(new ObjectName("d:k=3"));
        check(r.getRoleValue().size() == 2, "source mutation after construction is invisible");
        // getRoleValue exposes the live internal list: structural edits through it change Role state
        r.getRoleValue().add(new ObjectName("d:k=9"));
        check(r.getRoleValue().size() == 3, "getRoleValue returns the live internal list");
        // setRoleValue swaps in a defensive copy of the new list
        java.util.List<ObjectName> repl = names("d:k=a");
        r.setRoleValue(repl);
        check(r.getRoleValue().size() == 1 && r.getRoleValue().get(0).equals(new ObjectName("d:k=a")),
              "setRoleValue installs new content");
        repl.add(new ObjectName("d:k=b"));
        check(r.getRoleValue().size() == 1, "setRoleValue defensively copies its argument");
        try { r.setRoleValue(null); check(false, "setRoleValue(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleValue(null) msg"); }
        r.setRoleName("renamed");
        check("renamed".equals(r.getRoleName()), "setRoleName round-trip");
        r.setRoleName("");
        check("".equals(r.getRoleName()), "setRoleName accepts empty string");
        try { r.setRoleName(null); check(false, "setRoleName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleName(null) msg"); }
    }

    // clone independence and Object-identity equals/hashCode (Role overrides neither)
    static void testCloneEqualsHashCode() throws Exception {
        Role a = new Role("n", names("d:t=a", "d:t=b"));
        Object cl = a.clone();
        check(cl instanceof Role, "clone returns a Role");
        Role c = (Role) cl;
        check(c != a, "clone is a new instance");
        check(c.getRoleName().equals(a.getRoleName()), "clone copies the name");
        check(c.getRoleValue().equals(a.getRoleValue()), "clone copies the value content");
        check(c.getRoleValue() != a.getRoleValue(), "clone value is an independent list");
        a.getRoleValue().add(new ObjectName("d:t=c"));
        check(c.getRoleValue().size() == 2, "clone is unaffected by later mutation of the original");
        // Role does not override equals/hashCode: same content does NOT compare equal
        Role s1 = new Role("x", names("d:t=a"));
        Role s2 = new Role("x", names("d:t=a"));
        check(!s1.equals(s2), "no equals override: same-content Roles are not equal");
        check(s1.equals(s1), "identity equals is reflexive");
        check(!s1.equals("a string"), "equals against an unrelated type is false");
        check(s1.hashCode() == System.identityHashCode(s1), "hashCode is the Object identity hash");
    }

    // exact toString / roleValueToString formats and ordering
    static void testToStringFormats() throws Exception {
        Role r = new Role("myRole", names("d:type=a", "d:type=b"));
        check("role name: myRole; role value: d:type=a, d:type=b".equals(r.toString()),
              "toString exact format, got: " + r.toString());
        Role single = new Role("s", names("only:k=v"));
        check("role name: s; role value: only:k=v".equals(single.toString()), "toString single value");
        check("role name: ; role value: ".equals(new Role("", new java.util.ArrayList<ObjectName>()).toString()),
              "toString with empty name and value");
        String joined = Role.roleValueToString(names("d:type=a", "d:type=b", "d:type=c"));
        check("d:type=a\nd:type=b\nd:type=c".equals(joined),
              "roleValueToString joins with newline, got: " + joined.replace('\n', '|'));
        check("d:z=1\nd:a=2\nd:m=3".equals(Role.roleValueToString(names("d:z=1", "d:a=2", "d:m=3"))),
              "roleValueToString preserves insertion order (no sorting)");
        check("".equals(Role.roleValueToString(new java.util.ArrayList<ObjectName>())),
              "roleValueToString of empty list is empty string");
        try { Role.roleValueToString(null); check(false, "roleValueToString(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter".equals(e.getMessage()), "roleValueToString(null) msg"); }
    }

    // serialization round-trip with order preservation and locked serialVersionUID
    static void testSerialization() throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        Role orig = new Role("ser", names("d:t=1", "d:t=2", "d:t=3"));
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        Role back = (Role) ois.readObject();
        ois.close();
        check("ser".equals(back.getRoleName()), "serial round-trip name");
        check(back.getRoleValue().size() == 3, "serial round-trip value size");
        check(back.getRoleValue().get(0).equals(new ObjectName("d:t=1"))
           && back.getRoleValue().get(1).equals(new ObjectName("d:t=2"))
           && back.getRoleValue().get(2).equals(new ObjectName("d:t=3")),
              "serial round-trip preserves value order");
        check(back.getRoleValue() instanceof java.util.ArrayList, "deserialized value is an ArrayList");
        orig.getRoleValue().clear();
        check(back.getRoleValue().size() == 3, "deserialized copy is independent of the original");
        check(java.io.ObjectStreamClass.lookup(Role.class).getSerialVersionUID() == -279985518429862552L,
              "Role serialVersionUID is -279985518429862552L");
    }
}
