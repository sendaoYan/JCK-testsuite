/*
 * @test
 * @summary javax.management.relation.RelationServiceMBean conformance: white-box structure + light behavior.
 * @run main RelationServiceMBeanTest
 */
import javax.management.relation.RelationService;
import java.util.ArrayList;
import java.util.Arrays;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceMBean;

public class RelationServiceMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationServiceMBean");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "RelationServiceMBean must be public");
        check(c.isInterface(), "RelationServiceMBean must be an interface");

        c.getMethod("isActive");
        c.getMethod("getPurgeFlag");
        c.getMethod("setPurgeFlag", boolean.class);
        c.getMethod("createRelationType", String.class, javax.management.relation.RoleInfo[].class);
        c.getMethod("addRelationType", javax.management.relation.RelationType.class);
        c.getMethod("getAllRelationTypeNames");
        c.getMethod("getRoleInfos", String.class);
        c.getMethod("getRoleInfo", String.class, String.class);
        c.getMethod("removeRelationType", String.class);
        c.getMethod("createRelation", String.class, String.class, javax.management.relation.RoleList.class);
        c.getMethod("isRelationMBean", String.class);
        c.getMethod("isRelation", javax.management.ObjectName.class);
        c.getMethod("hasRelation", String.class);
        c.getMethod("getAllRelationIds");
        c.getMethod("checkRoleReading", String.class, String.class);
        c.getMethod("checkRoleWriting", javax.management.relation.Role.class, String.class, Boolean.class);
        c.getMethod("getRole", String.class, String.class);
        c.getMethod("getRoles", String.class, String[].class);
        c.getMethod("getAllRoles", String.class);
        c.getMethod("getRoleCardinality", String.class, String.class);
        c.getMethod("setRole", String.class, javax.management.relation.Role.class);
        c.getMethod("setRoles", String.class, javax.management.relation.RoleList.class);
        c.getMethod("getReferencedMBeans", String.class);
        c.getMethod("getRelationTypeName", String.class);
        c.getMethod("findReferencingRelations", javax.management.ObjectName.class, String.class, String.class);
        c.getMethod("findAssociatedMBeans", javax.management.ObjectName.class, String.class, String.class);
        c.getMethod("findRelationsOfType", String.class);
    }

    // ---- Part 2: light behavior (implementation linkage) ----
    static void testBlackBox() throws Exception {
        check(RelationServiceMBean.class.isAssignableFrom(RelationService.class),
              "RelationService implements RelationServiceMBean");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepImplementedByClosure();
        deepInterfaceReferenceFlow();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): implemented-by closure + interface-reference real flow + server routing ----

    public static interface CounterMBean { int getCount(); void setCount(int v); void reset(); int add(int d); }
    public static class Counter implements CounterMBean {
        int c;
        public int getCount() { return c; }
        public void setCount(int v) { c = v; }
        public void reset() { c = 0; }
        public int add(int d) { c += d; return c; }
    }

    static ObjectName mon(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }

    /** Every method declared by the interface must be implemented (public) by RelationService. */
    static void deepImplementedByClosure() throws Exception {
        Class<?> iface = Class.forName("javax.management.relation.RelationServiceMBean");
        Class<?> impl = Class.forName("javax.management.relation.RelationService");
        int n = 0;
        for (java.lang.reflect.Method m : iface.getMethods()) {
            impl.getMethod(m.getName(), m.getParameterTypes());
            n++;
        }
        check(n >= 30, "RelationServiceMBean declares at least 30 methods (found " + n + ")");
        check(iface.getMethods().length == n, "no duplicate erasure in interface surface");
        check(!java.lang.reflect.Modifier.isAbstract(impl.getModifiers()), "implementing class is concrete");
    }

    /** Real closed loop through an interface reference and through the MBeanServer. */
    static void deepInterfaceReferenceFlow() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = mon("jck.rsmb:type=RelationService");
        RelationService svc = new RelationService(true);
        server.registerMBean(svc, rsvcName);
        ObjectName a = mon("jck.rsmb:type=A");
        server.registerMBean(new Counter(), a);
        RelationServiceMBean iface = svc;

        iface.createRelationType("jck.T", new javax.management.relation.RoleInfo[]{ new javax.management.relation.RoleInfo("memberRole", Counter.class.getName(), true, true, 1, 2, "d") });
        check(iface.getAllRelationTypeNames().contains("jck.T"), "interface createRelationType visible");
        javax.management.relation.RoleList rl = new javax.management.relation.RoleList();
        rl.add(new javax.management.relation.Role("memberRole", new ArrayList<ObjectName>(Arrays.asList(a))));
        iface.createRelation("r1", "jck.T", rl);
        check(Boolean.TRUE.equals(iface.hasRelation("r1")), "interface createRelation + hasRelation");
        check(iface.getAllRelationIds().contains("r1"), "interface getAllRelationIds");
        check(iface.getRole("r1", "memberRole").equals(new ArrayList<ObjectName>(Arrays.asList(a))), "interface getRole value");
        check("jck.T".equals(iface.getRelationTypeName("r1")), "interface getRelationTypeName");
        iface.isActive();
        passed++;

        // server attribute/operation routing agrees with the interface reference
        check(Boolean.TRUE.equals(server.getAttribute(rsvcName, "PurgeFlag")), "server attribute PurgeFlag");
        check(new ArrayList<Object>((java.util.List<?>) server.getAttribute(rsvcName, "AllRelationIds")).equals(iface.getAllRelationIds()),
                "server attribute AllRelationIds equals interface call");
        server.setAttribute(rsvcName, new javax.management.Attribute("PurgeFlag", Boolean.FALSE));
        check(!iface.getPurgeFlag(), "server setAttribute PurgeFlag visible through interface");
        iface.setPurgeFlag(true);
        check(Boolean.TRUE.equals(server.getAttribute(rsvcName, "PurgeFlag")), "interface setPurgeFlag visible through server");
        Object invoked = server.invoke(rsvcName, "getRole",
                new Object[]{ "r1", "memberRole" }, new String[]{ "java.lang.String", "java.lang.String" });
        check(invoked.equals(iface.getRole("r1", "memberRole")), "server invoke getRole equals interface call");
        RelationServiceMBean proxy = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, rsvcName, RelationServiceMBean.class, false);
        check(proxy.getAllRelationIds().contains("r1"), "MBeanServerInvocationHandler proxy routes to service");
        check(proxy.getRole("r1", "memberRole").equals(invoked), "proxy getRole equals direct call");
        iface.removeRelation("r1");
        check(Boolean.FALSE.equals(iface.hasRelation("r1")), "interface removeRelation effective");
        check(proxy.getAllRelationIds().isEmpty(), "proxy observes removal");
    }
}
