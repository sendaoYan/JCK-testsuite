/*
 * @test
 * @summary javax.management.relation.RoleUnresolved conformance: white-box structure + black-box behavior.
 * @run main RoleUnresolvedTest
 */
import java.util.ArrayList;
import java.util.List;
import javax.management.ObjectName;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleUnresolved;

public class RoleUnresolvedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static ObjectName on(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RoleUnresolved");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RoleUnresolved must be public");
        check(!c.isInterface(), "RoleUnresolved must be a class");
        check(java.io.Serializable.class.isAssignableFrom(c), "RoleUnresolved must implement Serializable");
        c.getConstructor(String.class, List.class, int.class);
        c.getMethod("getRoleName"); c.getMethod("getRoleValue"); c.getMethod("getProblemType");
        c.getMethod("setRoleName", String.class); c.getMethod("setRoleValue", List.class);
        c.getMethod("setProblemType", int.class); c.getMethod("clone"); c.getMethod("toString");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ObjectName a = on("d:type=a");
        ObjectName b = on("d:type=b");
        List<ObjectName> val = new ArrayList<>(); val.add(a); val.add(b);

        RoleUnresolved ru = new RoleUnresolved("r", val, RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
        check("r".equals(ru.getRoleName()), "getRoleName");
        check(ru.getRoleValue().equals(val), "getRoleValue");
        check(ru.getProblemType() == RoleStatus.MORE_THAN_MAX_ROLE_DEGREE, "getProblemType");

        // value is defensively copied on construction
        val.add(on("d:type=c"));
        check(ru.getRoleValue().size() == 2, "role value defensively copied");

        // setters
        ru.setRoleName("r2");
        check("r2".equals(ru.getRoleName()), "setRoleName");
        List<ObjectName> one = new ArrayList<>(); one.add(a);
        ru.setRoleValue(one);
        check(ru.getRoleValue().size() == 1 && ru.getRoleValue().get(0).equals(a), "setRoleValue");
        ru.setRoleValue(null);
        check(ru.getRoleValue() == null, "setRoleValue(null) clears value");
        ru.setProblemType(RoleStatus.NO_ROLE_WITH_NAME);
        check(ru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME, "setProblemType");

        // toString with value and without value
        RoleUnresolved withVal = new RoleUnresolved("r", new ArrayList<>(java.util.Arrays.asList(a, b)), RoleStatus.ROLE_NOT_READABLE);
        check("role name: r; value: d:type=a, d:type=b; problem type: 2".equals(withVal.toString()),
              "toString with value, got: " + withVal.toString());
        RoleUnresolved noVal = new RoleUnresolved("r", null, RoleStatus.NO_ROLE_WITH_NAME);
        check("role name: r; problem type: 1".equals(noVal.toString()), "toString null value, got: " + noVal.toString());

        // clone independence
        Object cl = withVal.clone();
        check(cl instanceof RoleUnresolved, "clone returns RoleUnresolved");
        RoleUnresolved clc = (RoleUnresolved) cl;
        check(clc != withVal, "clone distinct instance");
        check(clc.getRoleValue().equals(withVal.getRoleValue()) && clc.getRoleValue() != withVal.getRoleValue(), "clone deep value");

        // exceptions
        try { new RoleUnresolved(null, null, RoleStatus.NO_ROLE_WITH_NAME); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null name message"); }
        try { new RoleUnresolved("r", null, 999); check(false, "bad problem type must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "bad problem message"); }
        try { ru.setRoleName(null); check(false, "setRoleName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleName null message"); }
        try { ru.setProblemType(0); check(false, "setProblemType(0) must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "setProblemType invalid message"); }
    }

    // ---- deepened (batch j): problem-type matrix, edges, identity equals, serialization ----

    static void testProblemTypeMatrix() {
        // every RoleStatus constant is a legal problem type, via ctor and setter
        for (int t = 1; t <= 7; t++) {
            check(new RoleUnresolved("n" + t, null, t).getProblemType() == t,
                  "ctor accepts problemType " + t);
        }
        RoleUnresolved ru = new RoleUnresolved("n", null, 1);
        for (int t = 1; t <= 7; t++) {
            ru.setProblemType(t);
            check(ru.getProblemType() == t, "setProblemType round-trips " + t);
        }
        // closed domain: 0, negatives, and anything above 7 rejected with the exact message
        int[] bad = { 0, 8, -1, Integer.MIN_VALUE, Integer.MAX_VALUE };
        for (int t : bad) {
            try { new RoleUnresolved("n", null, t); check(false, "ctor must reject problemType " + t); }
            catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()),
                  "ctor rejects problemType " + t + " with exact msg"); }
        }
        try { ru.setProblemType(8); check(false, "setProblemType(8) must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "setProblemType(8) msg"); }
    }

    static void testValueEdges() {
        // empty value list keeps the value segment in toString
        RoleUnresolved emptyVal = new RoleUnresolved("e", new ArrayList<ObjectName>(), 7);
        check("role name: e; value: ; problem type: 7".equals(emptyVal.toString()),
              "toString empty value keeps the value segment, got: " + emptyVal);
        // setRoleValue(null) clears; setRoleValue(empty) keeps a non-null empty list
        emptyVal.setRoleValue(null);
        check(emptyVal.getRoleValue() == null, "setRoleValue(null) clears the value");
        emptyVal.setRoleValue(new ArrayList<ObjectName>());
        check(emptyVal.getRoleValue() != null && emptyVal.getRoleValue().isEmpty(),
              "setRoleValue(empty) installs a non-null empty list");
        // empty name is legal everywhere null is rejected
        check("".equals(new RoleUnresolved("", null, 1).getRoleName()), "empty name accepted");
        // cloning an instance whose value is null keeps it null
        RoleUnresolved nvClone = (RoleUnresolved) new RoleUnresolved("nv", null, 2).clone();
        check(nvClone.getRoleValue() == null && nvClone.getProblemType() == 2 && "nv".equals(nvClone.getRoleName()),
              "clone preserves a null value, problem type, and name");
        // RoleUnresolved does not override equals/hashCode: identity semantics
        RoleUnresolved s1 = new RoleUnresolved("x", null, 1);
        RoleUnresolved s2 = new RoleUnresolved("x", null, 1);
        check(!s1.equals(s2), "no equals override: same-content instances are not equal");
        check(s1.hashCode() == System.identityHashCode(s1), "hashCode is the Object identity hash");
    }

    static void testSerialization() throws Exception {
        RoleUnresolved orig = new RoleUnresolved("ser",
                new ArrayList<ObjectName>(java.util.Arrays.asList(on("d:t=1"), on("d:t=2"), on("d:t=3"))),
                RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        RoleUnresolved back = (RoleUnresolved) new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("ser".equals(back.getRoleName()), "serial name");
        check(back.getProblemType() == RoleStatus.MORE_THAN_MAX_ROLE_DEGREE, "serial problem type");
        check(back.getRoleValue().size() == 3
           && back.getRoleValue().get(0).equals(on("d:t=1"))
           && back.getRoleValue().get(1).equals(on("d:t=2"))
           && back.getRoleValue().get(2).equals(on("d:t=3")), "serial preserves value order");
        check(java.io.ObjectStreamClass.lookup(RoleUnresolved.class).getSerialVersionUID()
              == -48350262537070138L, "RoleUnresolved serialVersionUID is -48350262537070138L");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testProblemTypeMatrix();
        testValueEdges();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
