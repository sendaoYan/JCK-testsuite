/*
 * @test
 * @summary javax.management.relation.RelationSupport conformance: concrete class reflection.
 * @run main RelationSupportTest
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.Relation;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationSupport;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleNotFoundException;
import javax.management.relation.RoleUnresolved;

public class RelationSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.relation.RelationSupportMBean").isAssignableFrom(c), "implements/extends javax.management.relation.RelationSupportMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(hasMethod(c, "getAllRoles"), "declares getAllRoles");
        check(hasMethod(c, "getReferencedMBeans"), "declares getReferencedMBeans");
        check(hasMethod(c, "getRelationId"), "declares getRelationId");
        deepStandaloneWithoutService();
        deepBogusServiceName();
        deepWithRegisteredService();
        deepAsMBean();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): standalone vs attached service usage, MBean registration ----

    public static interface CounterMBean { int getCount(); void setCount(int v); void reset(); int add(int d); }
    public static class Counter implements CounterMBean {
        int c;
        public int getCount() { return c; }
        public void setCount(int v) { c = v; }
        public void reset() { c = 0; }
        public int add(int d) { c += d; return c; }
    }
    public static interface TextMBean { String getText(); void setText(String s); }
    public static class Text implements TextMBean {
        String t = "x";
        public String getText() { return t; }
        public void setText(String s) { t = s; }
    }

    static ObjectName son(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static List<ObjectName> sol(ObjectName... names) { return new ArrayList<ObjectName>(Arrays.asList(names)); }
    static RoleList sroleList(String roleName, ObjectName... names) {
        RoleList rl = new RoleList();
        rl.add(new Role(roleName, sol(names)));
        return rl;
    }
    interface SThrower { void run() throws Throwable; }
    static void scheckThrows(Class<? extends Throwable> type, String msg, SThrower r, String label) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t) && (msg == null || msg.equals(t.getMessage()))) { passed++; return; }
            throw new AssertionError(label + ": expected " + type.getSimpleName() + "('" + msg + "') but got "
                    + t.getClass().getName() + "('" + t.getMessage() + "')", t);
        }
        throw new AssertionError(label + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    /** 4-arg ctor, never registered: local-map operations work, service-dependent operations fail. */
    static void deepStandaloneWithoutService() throws Exception {
        ObjectName relSvc = son("jck.rsup:type=RelationService");
        ObjectName m1 = son("jck.rsup:type=m1");
        final RelationSupport sup = new RelationSupport("sup1", relSvc, "myType", sroleList("roleA", m1));
        check(Integer.valueOf(1).equals(sup.getRoleCardinality("roleA")), "standalone cardinality without service");
        check(sup.retrieveAllRoles().size() == 1
                && "roleA".equals(((Role) sup.retrieveAllRoles().get(0)).getRoleName()), "standalone retrieveAllRoles");
        scheckThrows(RoleNotFoundException.class, "nope does not exist in relation.", new SThrower() { public void run() throws Throwable { sup.getRole("nope"); } }, "standalone getRole unknown role");
        // recorded actual: known role read requires the Relation Service via MBeanServer; no server -> NPE (both JDKs)
        try { sup.getRole("roleA"); check(false, "standalone known-role getRole must fail"); }
        catch (NullPointerException e) { passed++; }
        catch (RelationServiceNotRegisteredException e) { passed++; }
        // multi-role read: unknown roles are reported as unresolved without service access
        RoleResult rr = sup.getRoles(new String[]{ "nope" });
        check(rr.getRoles().isEmpty() && rr.getRolesUnresolved().size() == 1, "standalone getRoles unknown role unresolved");
        check(((RoleUnresolved) rr.getRolesUnresolved().get(0)).getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
                "standalone getRoles problem type");
        try { sup.getAllRoles(); check(false, "standalone getAllRoles must fail"); }
        catch (NullPointerException e) { passed++; }
        catch (RelationServiceNotRegisteredException e) { passed++; }
        check(sup.getReferencedMBeans().size() == 1, "standalone getReferencedMBeans");
    }

    /** 5-arg ctor: real server, service registered at a bogus name -> RelationServiceNotRegisteredException. */
    static void deepBogusServiceName() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName ghost = son("jck.ghost:type=RS");
        ObjectName m1 = son("jck.rsup:type=m1");
        final RelationSupport sup = new RelationSupport("sup2", ghost, server, "myType", sroleList("roleA", m1));
        scheckThrows(RelationServiceNotRegisteredException.class, null, new SThrower() { public void run() throws Throwable { sup.getRole("roleA"); } },
                "bogus service name getRole");
        scheckThrows(RelationServiceNotRegisteredException.class, null, new SThrower() { public void run() throws Throwable { sup.setRole(new Role("roleA", sol(m1))); } },
                "bogus service name setRole");
    }

    /** 5-arg ctor with a real registered service: reads work, writes need addRelation first. */
    static void deepWithRegisteredService() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvc = son("jck.rsup:type=RelationService");
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvc);
        final ObjectName a = son("jck.rsup:type=A");
        final ObjectName t = son("jck.rsup:type=Text");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Text(), t);
        rs.createRelationType("jck.T", new RoleInfo[]{ new RoleInfo("memberRole", Counter.class.getName(), true, true, 1, 2, "d") });
        final RelationSupport sup = new RelationSupport("sup3", rsvc, server, "jck.T", sroleList("memberRole", a));
        check(sup.getRole("memberRole").equals(sol(a)), "attached-service getRole returns internal value");
        check(Integer.valueOf(1).equals(sup.getRoleCardinality("memberRole")), "attached-service cardinality");
        // member registration/class are NOT re-checked on read: unregistered name still readable if present in the map
        RelationSupport supRaw = new RelationSupport("sup4", rsvc, server, "jck.T", sroleList("memberRole", son("jck.rsup:type=Ghost")));
        check(supRaw.getRole("memberRole").size() == 1, "read does not verify member registration");
        // writes: constraints validated against the service-managed type first
        scheckThrows(InvalidRoleValueException.class, "memberRole has a number of MBean references less than the expected minimum degree.",
                new SThrower() { public void run() throws Throwable { sup.setRole(new Role("memberRole", new ArrayList<ObjectName>())); } }, "below min degree");
        scheckThrows(InvalidRoleValueException.class, "memberRole has an MBean reference to an MBean not of the expected class of references for that role.",
                new SThrower() { public void run() throws Throwable { sup.setRole(new Role("memberRole", sol(a, t))); } }, "wrong-class member");
        scheckThrows(RoleNotFoundException.class, "bogusRole does not exist in relation.",
                new SThrower() { public void run() throws Throwable { sup.setRole(new Role("bogusRole", sol(a))); } }, "unknown role name");
        // valid value but relation not added to the service: notification path fails with RelationNotFoundException
        scheckThrows(RelationNotFoundException.class, "No relation associated to relation id sup3",
                new SThrower() { public void run() throws Throwable { sup.setRole(new Role("memberRole", sol(a))); } }, "setRole before addRelation");
    }

    /** Registered as an MBean: exposed management surface + server routing. */
    static void deepAsMBean() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvc = son("jck.rsup:type=RelationService");
        RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvc);
        ObjectName a = son("jck.rsup:type=A");
        server.registerMBean(new Counter(), a);
        rs.createRelationType("jck.T", new RoleInfo[]{ new RoleInfo("memberRole", Counter.class.getName(), true, true, 1, 2, "d") });
        final RelationSupport sup = new RelationSupport("mrel", rsvc, server, "jck.T", sroleList("memberRole", a));
        final ObjectName supName = son("jck.rsup:type=RelSup");
        server.registerMBean(sup, supName);

        check("mrel".equals(server.getAttribute(supName, "RelationId")), "MBean attribute RelationId");
        check("jck.T".equals(server.getAttribute(supName, "RelationTypeName")), "MBean attribute RelationTypeName");
        check(rsvc.equals(server.getAttribute(supName, "RelationServiceName")), "MBean attribute RelationServiceName");
        check(((Map<?, ?>) server.getAttribute(supName, "ReferencedMBeans")).size() == 1, "MBean attribute ReferencedMBeans");
        check(server.getAttribute(supName, "AllRoles") instanceof RoleResult, "MBean attribute AllRoles");
        List<String> attrNames = new ArrayList<String>();
        for (javax.management.MBeanAttributeInfo ai : server.getMBeanInfo(supName).getAttributes()) attrNames.add(ai.getName());
        java.util.Collections.sort(attrNames);
        check("AllRoles,ReferencedMBeans,RelationId,RelationServiceManagementFlag,RelationServiceName,RelationTypeName,Role".equals(
                String.join(",", attrNames)), "exact MBean attribute surface: " + attrNames);
        // isInRelationService returns Boolean (not primitive) so JMX introspection does not expose it
        try { server.getAttribute(supName, "InRelationService"); check(false, "InRelationService must not be an attribute"); }
        catch (javax.management.AttributeNotFoundException e) { passed++; }
        server.setAttribute(supName, new javax.management.Attribute("RelationServiceManagementFlag", Boolean.TRUE));
        check(Boolean.TRUE.equals(sup.isInRelationService()), "write-only flag attribute routes to setter");
        try {
            server.setAttribute(supName, new javax.management.Attribute("RelationServiceManagementFlag", null));
            check(false, "null flag via server must fail");
        } catch (javax.management.RuntimeMBeanException e) {
            check(e.getCause() instanceof IllegalArgumentException && "Invalid parameter.".equals(e.getCause().getMessage()),
                    "null flag via server wraps IAE");
        }
        RoleList roles = (RoleList) server.invoke(supName, "retrieveAllRoles", null, null);
        check(roles.size() == 1 && ((Role) roles.get(0)).getRoleValue().equals(sol(a)), "invoke retrieveAllRoles");
        Integer card = (Integer) server.invoke(supName, "getRoleCardinality", new Object[]{ "memberRole" }, new String[]{ "java.lang.String" });
        check(Integer.valueOf(1).equals(card), "invoke getRoleCardinality");

        rs.addRelation(supName);
        check(supName.equals(rs.isRelationMBean("mrel")), "addRelation links MBean to relation id");
        check(Boolean.TRUE.equals(sup.isInRelationService()), "addRelation sets management flag");
        RoleResult up = rs.setRoles("mrel", sroleList("memberRole", a));
        check(up.getRoles().size() == 1 && up.getRolesUnresolved().isEmpty(), "service setRoles on relation MBean");
        check(((Role) sup.retrieveAllRoles().get(0)).getRoleValue().equals(sol(a)), "role value after service setRoles");

        // Relation interface transparency on the implementation
        Relation iface = sup;
        check("mrel".equals(iface.getRelationId()) && "jck.T".equals(iface.getRelationTypeName())
                && rsvc.equals(iface.getRelationServiceName()), "Relation interface id/type/service getters");
        check(iface.getReferencedMBeans().get(a).equals(Arrays.asList("memberRole")), "Relation interface referenced mbeans");
    }
}
