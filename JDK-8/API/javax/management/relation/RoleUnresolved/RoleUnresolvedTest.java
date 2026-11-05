/*
 * @test
 * @summary javax.management.relation.RoleUnresolved conformance: name/value/problem.
 * @run main RoleUnresolvedTest
 */

import javax.management.*;
import javax.management.relation.*;
public class RoleUnresolvedTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        java.util.List<ObjectName> l = new java.util.ArrayList<ObjectName>();
        l.add(new ObjectName("d:type=x"));
        RoleUnresolved ru = new RoleUnresolved("rname", l, RoleStatus.NO_ROLE_WITH_NAME);
        check("rname".equals(ru.getRoleName()), "role name");
        check(ru.getRoleValue().size() == 1, "role value");
        check(ru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME, "problem type");
        testProblemTypeMatrix();
        testCtorAndNullSemantics();
        testSetters();
        testToStringForms();
        testCloneAndEquals();
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

    // every RoleStatus constant is a legal problem type; anything else is rejected with exact message
    static void testProblemTypeMatrix() {
        for (int t = 1; t <= 7; t++) {
            RoleUnresolved ru = new RoleUnresolved("n" + t, null, t);
            check(ru.getProblemType() == t, "ctor accepts problemType " + t);
        }
        int[] bad = { 0, 8, -1, Integer.MIN_VALUE, Integer.MAX_VALUE };
        for (int t : bad) {
            try { new RoleUnresolved("n", null, t); check(false, "ctor must reject problemType " + t); }
            catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()),
                  "ctor rejects problemType " + t + " with exact msg, got: " + e.getMessage()); }
        }
        RoleUnresolved ru = new RoleUnresolved("n", null, 1);
        for (int t = 1; t <= 7; t++) {
            ru.setProblemType(t);
            check(ru.getProblemType() == t, "setProblemType round-trips " + t);
        }
        try { ru.setProblemType(0); check(false, "setProblemType(0) must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "setProblemType(0) msg"); }
        try { ru.setProblemType(8); check(false, "setProblemType(8) must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "setProblemType(8) msg"); }
        try { ru.setProblemType(-1); check(false, "setProblemType(-1) must throw"); }
        catch (IllegalArgumentException e) { check("Incorrect problem type.".equals(e.getMessage()), "setProblemType(-1) msg"); }
    }

    // null name rejected with exact message; null value legal (read-access failures carry no value)
    static void testCtorAndNullSemantics() throws Exception {
        try { new RoleUnresolved(null, null, 1); check(false, "null name must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "ctor null name msg"); }
        RoleUnresolved nv = new RoleUnresolved("nv", null, RoleStatus.ROLE_NOT_READABLE);
        check(nv.getRoleValue() == null, "null role value is legal and read back as null");
        check("nv".equals(nv.getRoleName()), "name round-trip");
        RoleUnresolved empty = new RoleUnresolved("", new java.util.ArrayList<ObjectName>(), 1);
        check("".equals(empty.getRoleName()) && empty.getRoleValue().isEmpty(), "empty name and empty value legal");
        // defensive copy of the provided value
        java.util.List<ObjectName> src = names("d:t=a", "d:t=b");
        RoleUnresolved ru = new RoleUnresolved("cp", src, 3);
        check(ru.getRoleValue() != src, "ctor defensively copies the value list");
        src.add(new ObjectName("d:t=c"));
        check(ru.getRoleValue().size() == 2, "source mutation after construction is invisible");
        // the empty string name is fine, but null is the only rejected form
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { new RoleUnresolved(null, new java.util.ArrayList<ObjectName>(), 1); }
        }, "ctor null name with non-null value must throw IAE");
    }

    // setter semantics: name round-trip, value replacement/copy/clear, problem type validation
    static void testSetters() throws Exception {
        RoleUnresolved ru = new RoleUnresolved("orig", names("d:t=a"), 1);
        ru.setRoleName("renamed");
        check("renamed".equals(ru.getRoleName()), "setRoleName round-trip");
        ru.setRoleName("");
        check("".equals(ru.getRoleName()), "setRoleName accepts empty string");
        try { ru.setRoleName(null); check(false, "setRoleName(null) must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "setRoleName(null) msg"); }
        java.util.List<ObjectName> repl = names("d:t=z");
        ru.setRoleValue(repl);
        check(ru.getRoleValue().size() == 1 && ru.getRoleValue().get(0).equals(new ObjectName("d:t=z")),
              "setRoleValue installs new content");
        repl.add(new ObjectName("d:t=w"));
        check(ru.getRoleValue().size() == 1, "setRoleValue defensively copies its argument");
        ru.setRoleValue(null);
        check(ru.getRoleValue() == null, "setRoleValue(null) explicitly clears the value");
        ru.setRoleValue(new java.util.ArrayList<ObjectName>());
        check(ru.getRoleValue() != null && ru.getRoleValue().isEmpty(), "setRoleValue(empty) keeps a non-null empty list");
        ru.setProblemType(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS);
        check(ru.getProblemType() == RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS, "setProblemType with a named constant");
    }

    // exact toString formats for the with-value and without-value forms
    static void testToStringForms() {
        RoleUnresolved withVal = new RoleUnresolved("r", names("d:type=a", "d:type=b"), RoleStatus.ROLE_NOT_READABLE);
        check("role name: r; value: d:type=a, d:type=b; problem type: 2".equals(withVal.toString()),
              "toString with value exact, got: " + withVal);
        RoleUnresolved noVal = new RoleUnresolved("r", null, RoleStatus.NO_ROLE_WITH_NAME);
        check("role name: r; problem type: 1".equals(noVal.toString()),
              "toString null value omits the value segment, got: " + noVal);
        RoleUnresolved emptyVal = new RoleUnresolved("e", new java.util.ArrayList<ObjectName>(), 7);
        check("role name: e; value: ; problem type: 7".equals(emptyVal.toString()),
              "toString empty value keeps the empty value segment, got: " + emptyVal);
    }

    // clone independence and Object-identity equals (RoleUnresolved overrides neither)
    static void testCloneAndEquals() {
        RoleUnresolved orig = new RoleUnresolved("cl", names("d:t=a", "d:t=b"), 5);
        Object cl = orig.clone();
        check(cl instanceof RoleUnresolved, "clone returns a RoleUnresolved");
        RoleUnresolved copy = (RoleUnresolved) cl;
        check(copy != orig, "clone is a new instance");
        check("cl".equals(copy.getRoleName()), "clone copies the name");
        check(copy.getProblemType() == 5, "clone copies the problem type");
        check(copy.getRoleValue().equals(orig.getRoleValue()), "clone copies the value content");
        check(copy.getRoleValue() != orig.getRoleValue(), "clone value is an independent list");
        orig.getRoleValue().clear();
        check(copy.getRoleValue().size() == 2, "clone unaffected by later mutation of the original");
        // cloning an instance whose value is null keeps it null
        RoleUnresolved nullValClone = (RoleUnresolved) new RoleUnresolved("nv", null, 2).clone();
        check(nullValClone.getRoleValue() == null && nullValClone.getProblemType() == 2,
              "clone preserves a null value");
        // identity equals
        RoleUnresolved s1 = new RoleUnresolved("x", null, 1);
        RoleUnresolved s2 = new RoleUnresolved("x", null, 1);
        check(!s1.equals(s2), "no equals override: same-content instances are not equal");
        check(s1.equals(s1), "identity equals is reflexive");
        check(s1.hashCode() == System.identityHashCode(s1), "hashCode is the Object identity hash");
    }

    // serialization round-trip + locked serialVersionUID
    static void testSerialization() throws Exception {
        RoleUnresolved orig = new RoleUnresolved("ser", names("d:t=1", "d:t=2", "d:t=3"),
                RoleStatus.MORE_THAN_MAX_ROLE_DEGREE);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(orig);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        RoleUnresolved back = (RoleUnresolved) ois.readObject();
        ois.close();
        check("ser".equals(back.getRoleName()), "serial name");
        check(back.getProblemType() == RoleStatus.MORE_THAN_MAX_ROLE_DEGREE, "serial problem type");
        check(back.getRoleValue().size() == 3, "serial value size");
        check(back.getRoleValue().get(0).equals(new ObjectName("d:t=1"))
           && back.getRoleValue().get(1).equals(new ObjectName("d:t=2"))
           && back.getRoleValue().get(2).equals(new ObjectName("d:t=3")),
              "serial preserves value order");
        check(java.io.ObjectStreamClass.lookup(RoleUnresolved.class).getSerialVersionUID()
              == -48350262537070138L, "RoleUnresolved serialVersionUID is -48350262537070138L");
    }
}
