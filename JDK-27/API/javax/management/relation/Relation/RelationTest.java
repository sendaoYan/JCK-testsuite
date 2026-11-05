/*
 * @test
 * @summary javax.management.relation.Relation conformance: white-box structure + light behavior.
 * @run main RelationTest
 */
import java.util.List;
import java.util.Arrays;
import javax.management.relation.Relation;
import javax.management.relation.Role;
import javax.management.relation.RoleList;
import javax.management.relation.RoleUnresolvedList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.management.ObjectName;
import javax.management.relation.RoleNotFoundException;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleUnresolved;
import javax.management.relation.RoleResult;
import javax.management.relation.RelationSupport;

public class RelationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Relation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "Relation must be public");
        check(c.isInterface(), "Relation must be an interface");

        c.getMethod("getRole", String.class);
        c.getMethod("getRoles", String[].class);
        c.getMethod("getRoleCardinality", String.class);
        c.getMethod("getAllRoles");
        c.getMethod("retrieveAllRoles");
        c.getMethod("setRole", javax.management.relation.Role.class);
        c.getMethod("setRoles", javax.management.relation.RoleList.class);
        c.getMethod("handleMBeanUnregistration", javax.management.ObjectName.class, String.class);
        c.getMethod("getReferencedMBeans");
        c.getMethod("getRelationTypeName");
        c.getMethod("getRelationServiceName");
        c.getMethod("getRelationId");

        check(c.getMethod("getRole", String.class).getReturnType() == java.util.List.class, "getRole returns List");
        check(c.getMethod("getAllRoles").getReturnType() == javax.management.relation.RoleResult.class, "getAllRoles returns RoleResult");
        check(c.getMethod("retrieveAllRoles").getReturnType() == javax.management.relation.RoleList.class, "retrieveAllRoles returns RoleList");
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(Relation.class.isAssignableFrom(RelationSupport.class),
              "RelationSupport implements Relation");
        // All methods are exercised indirectly through RelationSupport's own
        // behavioral tests; here we verify the interface is implementable by
        // its documented implementation class.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepInterfaceSurface();
        deepRecordingImplementation();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): recording implementation, exception propagation, RelationSupport conformance ----

    /** Recording Relation implementation exposing every interface method's contract. */
    static class RecRel implements Relation {
        final List<String> calls = new ArrayList<String>();
        List<ObjectName> roleValue = new ArrayList<ObjectName>();
        Role lastSetRole;
        ObjectName lastUnregName;
        String lastUnregRole;
        public List<ObjectName> getRole(String roleName) throws RoleNotFoundException {
            calls.add("getRole:" + roleName);
            if (!"roleA".equals(roleName)) throw new RoleNotFoundException(roleName);
            return roleValue;
        }
        public RoleResult getRoles(String[] roleNames) {
            calls.add("getRoles");
            RoleResult rr = new RoleResult(new RoleList(), new RoleUnresolvedList());
            for (String rn : roleNames) {
                try { rr.getRoles().add(new Role(rn, getRole(rn))); }
                catch (RoleNotFoundException e) { rr.getRolesUnresolved().add(new RoleUnresolved(rn, null, RoleStatus.NO_ROLE_WITH_NAME)); }
            }
            return rr;
        }
        public Integer getRoleCardinality(String roleName) throws RoleNotFoundException {
            calls.add("getRoleCardinality:" + roleName);
            if (!"roleA".equals(roleName)) throw new RoleNotFoundException(roleName);
            return Integer.valueOf(roleValue.size());
        }
        public RoleResult getAllRoles() { calls.add("getAllRoles"); return getRoles(new String[]{ "roleA" }); }
        public RoleList retrieveAllRoles() {
            calls.add("retrieveAllRoles");
            RoleList rl = new RoleList();
            if (!roleValue.isEmpty()) rl.add(new Role("roleA", roleValue));
            return rl;
        }
        public void setRole(Role role) { calls.add("setRole"); lastSetRole = role; roleValue = role.getRoleValue(); }
        public RoleResult setRoles(RoleList list) {
            calls.add("setRoles");
            for (Object o : list) setRole((Role) o);
            return new RoleResult(list, new RoleUnresolvedList());
        }
        public void handleMBeanUnregistration(ObjectName name, String roleName) {
            calls.add("handleMBeanUnregistration");
            lastUnregName = name;
            lastUnregRole = roleName;
            List<ObjectName> v = new ArrayList<ObjectName>(roleValue);
            v.remove(name);
            roleValue = v;
        }
        public Map<ObjectName, List<String>> getReferencedMBeans() {
            calls.add("getReferencedMBeans");
            Map<ObjectName, List<String>> m = new HashMap<ObjectName, List<String>>();
            for (ObjectName n : roleValue) m.put(n, new ArrayList<String>(Arrays.asList("roleA")));
            return m;
        }
        public String getRelationTypeName() { calls.add("getRelationTypeName"); return "jck.recType"; }
        public ObjectName getRelationServiceName() { calls.add("getRelationServiceName"); return ion("jck.rec:type=RelationService"); }
        public String getRelationId() { calls.add("getRelationId"); return "rec1"; }
    }

    static ObjectName ion(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static List<ObjectName> iol(ObjectName... names) { return new ArrayList<ObjectName>(Arrays.asList(names)); }

    static void deepInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.Relation");
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.equals(new java.util.TreeSet<String>(Arrays.asList(
                "getRole", "getRoles", "getRoleCardinality", "getAllRoles", "retrieveAllRoles",
                "setRole", "setRoles", "handleMBeanUnregistration",
                "getReferencedMBeans", "getRelationTypeName", "getRelationServiceName", "getRelationId"))),
                "Relation declares exactly the 12 documented methods: " + names);
        Class<?>[] thrown = c.getMethod("getRole", String.class).getExceptionTypes();
        java.util.Set<String> exNames = new java.util.TreeSet<String>();
        for (Class<?> e : thrown) exNames.add(e.getName());
        check(exNames.equals(new java.util.TreeSet<String>(Arrays.asList(
                "java.lang.IllegalArgumentException",
                "javax.management.relation.RoleNotFoundException",
                "javax.management.relation.RelationServiceNotRegisteredException"))),
                "getRole declared exceptions");
        thrown = c.getMethod("setRole", javax.management.relation.Role.class).getExceptionTypes();
        exNames = new java.util.TreeSet<String>();
        for (Class<?> e : thrown) exNames.add(e.getName());
        check(exNames.equals(new java.util.TreeSet<String>(Arrays.asList(
                "java.lang.IllegalArgumentException",
                "javax.management.relation.RoleNotFoundException",
                "javax.management.relation.RelationTypeNotFoundException",
                "javax.management.relation.InvalidRoleValueException",
                "javax.management.relation.RelationServiceNotRegisteredException",
                "javax.management.relation.RelationNotFoundException"))),
                "setRole declared exceptions");
        check(c.getMethod("getAllRoles").getReturnType() == javax.management.relation.RoleResult.class,
                "getAllRoles returns RoleResult");
        check(c.getMethod("retrieveAllRoles").getReturnType() == javax.management.relation.RoleList.class,
                "retrieveAllRoles returns RoleList");
        check(c.getMethod("getReferencedMBeans").getReturnType() == Map.class, "getReferencedMBeans returns Map");
    }

    static void deepRecordingImplementation() throws Exception {
        RecRel r = new RecRel();
        Relation rel = r;
        ObjectName m1 = ion("jck.rec:type=m1");
        ObjectName m2 = ion("jck.rec:type=m2");
        r.roleValue = iol(m1, m2);

        check(rel.getRole("roleA").equals(iol(m1, m2)), "interface getRole returns recorded value");
        check(r.calls.contains("getRole:roleA"), "getRole call recorded");
        try { rel.getRole("nope"); check(false, "unknown role must throw"); }
        catch (RoleNotFoundException e) { check("nope".equals(e.getMessage()), "declared RoleNotFoundException propagates"); }
        check(Integer.valueOf(2).equals(rel.getRoleCardinality("roleA")), "interface getRoleCardinality");
        RoleResult rr = rel.getRoles(new String[]{ "roleA", "nope" });
        check(rr.getRoles().size() == 1 && rr.getRolesUnresolved().size() == 1, "interface getRoles mixed result");
        check(rel.getAllRoles().getRoles().size() == 1, "interface getAllRoles");
        check(rel.retrieveAllRoles().size() == 1, "interface retrieveAllRoles");
        rel.setRole(new javax.management.relation.Role("roleA", iol(m2)));
        check(r.lastSetRole != null && r.lastSetRole.getRoleValue().equals(iol(m2)), "interface setRole reaches implementation");
        check(rel.getRole("roleA").equals(iol(m2)), "setRole effect visible through getRole");
        RoleList two = new RoleList();
        two.add(new javax.management.relation.Role("roleA", iol(m1)));
        RoleResult srr = rel.setRoles(two);
        check(srr.getRoles().size() == 1 && r.roleValue.equals(iol(m1)), "interface setRoles");
        rel.handleMBeanUnregistration(m1, "roleA");
        check(m1.equals(r.lastUnregName) && "roleA".equals(r.lastUnregRole), "handleMBeanUnregistration callback recorded");
        check(r.roleValue.isEmpty(), "implementation applied unregistration effect");
        check(rel.getReferencedMBeans().isEmpty(), "interface getReferencedMBeans after removal");
        check("jck.recType".equals(rel.getRelationTypeName()), "interface getRelationTypeName");
        check("jck.rec:type=RelationService".equals(rel.getRelationServiceName().toString()), "interface getRelationServiceName");
        check("rec1".equals(rel.getRelationId()), "interface getRelationId");
        check(Relation.class.isAssignableFrom(javax.management.relation.RelationSupport.class),
                "RelationSupport implements Relation");
    }
}
