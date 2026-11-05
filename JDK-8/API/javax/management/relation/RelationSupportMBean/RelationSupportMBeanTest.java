/*
 * @test
 * @summary javax.management.relation.RelationSupportMBean conformance: interface reflection.
 * @run main RelationSupportMBeanTest
 */
import java.util.ArrayList;
import java.util.Arrays;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationSupportMBean;

public class RelationSupportMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupportMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.relation.Relation").isAssignableFrom(c), "implements/extends javax.management.relation.Relation");
        check(hasMethod(c, "isInRelationService"), "declares isInRelationService");
        check(hasMethod(c, "setRelationServiceManagementFlag"), "declares setRelationServiceManagementFlag");
        deepInterfaceSurface();
        deepFlagContract();
        deepMBeanRoutingAndProxy();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): interface surface, flag contract, MBean routing, proxy ----

    public static interface CounterMBean { int getCount(); void setCount(int v); void reset(); int add(int d); }
    public static class Counter implements CounterMBean {
        int c;
        public int getCount() { return c; }
        public void setCount(int v) { c = v; }
        public void reset() { c = 0; }
        public int add(int d) { c += d; return c; }
    }

    static ObjectName uon(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }

    static void deepInterfaceSurface() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationSupportMBean");
        java.util.Set<String> own = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) own.add(m.getName());
        check(own.equals(new java.util.TreeSet<String>(Arrays.asList("isInRelationService", "setRelationServiceManagementFlag"))),
                "interface declares exactly its two own methods: " + own);
        check(c.getMethod("isInRelationService").getReturnType() == Boolean.class,
                "isInRelationService returns boxed Boolean (hence not a JMX is-getter)");
        check(c.getMethod("setRelationServiceManagementFlag", Boolean.class).getExceptionTypes().length == 1
                && c.getMethod("setRelationServiceManagementFlag", Boolean.class).getExceptionTypes()[0] == IllegalArgumentException.class,
                "setRelationServiceManagementFlag declares IllegalArgumentException");
        java.util.Set<String> all = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) all.add(m.getName());
        check(all.size() == 14, "interface total surface = 12 Relation methods + 2 own (found " + all.size() + ")");
        check(javax.management.relation.RelationSupportMBean.class.isAssignableFrom(javax.management.relation.RelationSupport.class),
                "RelationSupport implements RelationSupportMBean");
    }

    static void deepFlagContract() throws Exception {
        ObjectName relSvc = uon("jck.rsupmb:type=RelationService");
        final javax.management.relation.RelationSupport sup =
                new javax.management.relation.RelationSupport("f1", relSvc, "myType", null);
        RelationSupportMBean iface = sup;
        check(Boolean.FALSE.equals(iface.isInRelationService()), "flag defaults to false");
        iface.setRelationServiceManagementFlag(Boolean.TRUE);
        check(Boolean.TRUE.equals(iface.isInRelationService()), "flag set to true");
        iface.setRelationServiceManagementFlag(Boolean.FALSE);
        check(Boolean.FALSE.equals(iface.isInRelationService()), "flag set back to false");
        try { iface.setRelationServiceManagementFlag(null); check(false, "null flag must throw"); }
        catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null flag message"); }
    }

    static void deepMBeanRoutingAndProxy() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvc = uon("jck.rsupmb:type=RelationService");
        javax.management.relation.RelationService rs = new javax.management.relation.RelationService(true);
        server.registerMBean(rs, rsvc);
        ObjectName a = uon("jck.rsupmb:type=A");
        server.registerMBean(new Counter(), a);
        rs.createRelationType("jck.T", new javax.management.relation.RoleInfo[]{ new javax.management.relation.RoleInfo("memberRole", Counter.class.getName(), true, true, 1, 2, "d") });
        javax.management.relation.RoleList rl = new javax.management.relation.RoleList();
        rl.add(new javax.management.relation.Role("memberRole", new ArrayList<ObjectName>(Arrays.asList(a))));
        final javax.management.relation.RelationSupport sup = new javax.management.relation.RelationSupport("mrel", rsvc, server, "jck.T", rl);
        final ObjectName supName = uon("jck.rsupmb:type=RelSup");
        server.registerMBean(sup, supName);

        check("mrel".equals(server.getAttribute(supName, "RelationId")), "server reads RelationId of relation MBean");
        server.setAttribute(supName, new javax.management.Attribute("RelationServiceManagementFlag", Boolean.TRUE));
        check(Boolean.TRUE.equals(sup.isInRelationService()), "write-only flag attribute routes to implementation");

        RelationSupportMBean proxy = javax.management.MBeanServerInvocationHandler.newProxyInstance(
                server, supName, RelationSupportMBean.class, false);
        check("mrel".equals(proxy.getRelationId()), "proxy getRelationId routed via server attribute");
        proxy.setRelationServiceManagementFlag(Boolean.FALSE);
        check(Boolean.FALSE.equals(sup.isInRelationService()), "proxy flag setter routed via server attribute");
        proxy.setRelationServiceManagementFlag(Boolean.TRUE);
        check(Boolean.TRUE.equals(sup.isInRelationService()), "proxy flag setter true");
        // recorded actual: isInRelationService() is not an MBean attribute (boxed Boolean), so proxy read fails
        try { proxy.isInRelationService(); check(false, "proxy isInRelationService must fail"); }
        catch (java.lang.reflect.UndeclaredThrowableException e) {
            check(e.getCause() instanceof javax.management.AttributeNotFoundException, "proxy isInRelationService fails with AttributeNotFoundException");
        }
        check(sup instanceof javax.management.relation.RelationSupportMBean
                && sup instanceof javax.management.relation.Relation
                && sup instanceof javax.management.MBeanRegistration, "RelationSupport interface set");
    }
}
