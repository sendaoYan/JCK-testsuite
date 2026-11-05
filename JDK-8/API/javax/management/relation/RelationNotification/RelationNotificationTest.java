/*
 * @test
 * @summary javax.management.relation.RelationNotification conformance: concrete class reflection.
 * @run main RelationNotificationTest
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.Notification;
import javax.management.ObjectName;
import javax.management.relation.RelationNotification;

public class RelationNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationNotification");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.Notification".equals(c.getSuperclass().getName()), "superclass javax.management.Notification");
        check(hasMethod(c, "getMBeansToUnregister"), "declares getMBeansToUnregister");
        check(hasMethod(c, "getNewRoleValue"), "declares getNewRoleValue");
        check(hasMethod(c, "getObjectName"), "declares getObjectName");
        deepConstructorForms();
        deepSerialization();
        deepLiveServiceLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): constructor forms, serialization, live service loop ----

    public static interface CounterMBean { int getCount(); void setCount(int v); void reset(); int add(int d); }
    public static class Counter implements CounterMBean {
        int c;
        public int getCount() { return c; }
        public void setCount(int v) { c = v; }
        public void reset() { c = 0; }
        public int add(int d) { c += d; return c; }
    }

    static ObjectName non(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static List<ObjectName> nol(ObjectName... names) { return new ArrayList<ObjectName>(Arrays.asList(names)); }

    static void deepConstructorForms() throws Exception {
        ObjectName src = non("d:type=src");
        ObjectName rel = non("d:type=rel");
        List<ObjectName> m1 = nol(rel);

        RelationNotification removal = new RelationNotification(
                RelationNotification.RELATION_BASIC_REMOVAL, src, 7L, 70L, "gone", "rid", "rtype", null, m1);
        check(RelationNotification.RELATION_BASIC_REMOVAL.equals(removal.getType()), "basic removal type accepted by 6-arg ctor");
        check("gone".equals(removal.getMessage()), "removal message");
        check(removal.getSequenceNumber() == 7L && removal.getTimeStamp() == 70L, "removal sequence/timestamp");
        check(removal.getMBeansToUnregister().equals(m1), "removal unregister list exact");
        check(removal.getRoleName() == null, "removal roleName null");
        check(removal.getOldRoleValue().isEmpty() && removal.getNewRoleValue().isEmpty(), "removal role values empty");
        check(removal.getObjectName() == null, "null objectName preserved");

        RelationNotification mbeanCreate = new RelationNotification(
                RelationNotification.RELATION_MBEAN_CREATION, src, 8L, 80L, "m", "rid", "rtype", rel, null);
        check(RelationNotification.RELATION_MBEAN_CREATION.equals(mbeanCreate.getType()), "mbean creation type accepted");
        check(rel.equals(mbeanCreate.getObjectName()), "mbean creation objectName");
        check(mbeanCreate.getMBeansToUnregister().isEmpty(), "null unregister list normalized to empty");
        try {
            mbeanCreate.getMBeansToUnregister().add(rel);
            check(mbeanCreate.getMBeansToUnregister().isEmpty(), "normalized empty list rejects mutation or stays empty");
        } catch (UnsupportedOperationException e) {
            passed++; // recorded actual: null normalizes to the immutable Collections.emptyList() on both JDKs
        }

        RelationNotification mbeanRemoval = new RelationNotification(
                RelationNotification.RELATION_MBEAN_REMOVAL, src, 9L, 90L, "m", "rid", "rtype", rel, m1);
        check(RelationNotification.RELATION_MBEAN_REMOVAL.equals(mbeanRemoval.getType()), "mbean removal type accepted");

        RelationNotification mbeanUpdate = new RelationNotification(
                RelationNotification.RELATION_MBEAN_UPDATE, src, 10L, 100L, "m", "rid", "rtype", rel, "r", m1, new ArrayList<ObjectName>());
        check(RelationNotification.RELATION_MBEAN_UPDATE.equals(mbeanUpdate.getType()), "mbean update type accepted by 11-arg ctor");
        check(rel.equals(mbeanUpdate.getObjectName()), "mbean update objectName");
        check(mbeanUpdate.getOldRoleValue().isEmpty(), "empty old value normalized");
        check(mbeanUpdate.getSource() == src, "source reference preserved outside MBeanServer");

        try {
            new RelationNotification(RelationNotification.RELATION_MBEAN_UPDATE, src, 1L, 1L, "m", "id", "t", null, "r", null, new ArrayList<ObjectName>());
            check(false, "null new value must throw");
        } catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null new value message"); }
        try {
            new RelationNotification(RelationNotification.RELATION_MBEAN_UPDATE, src, 1L, 1L, "m", "id", "t", null, "r", m1, null);
            check(false, "null old value must throw");
        } catch (IllegalArgumentException e) { check("Invalid parameter.".equals(e.getMessage()), "null old value message"); }
        check(java.io.ObjectStreamClass.lookup(RelationNotification.class).getSerialVersionUID() == -6871117877523310399L,
                "serialVersionUID locked to -6871117877523310399L");
    }

    static void deepSerialization() throws Exception {
        ObjectName src = non("d:type=src");
        ObjectName rel = non("d:type=rel");
        List<ObjectName> m1 = nol(rel);

        RelationNotification create = new RelationNotification(
                RelationNotification.RELATION_BASIC_CREATION, src, 10L, 20L, "m", "id", "t", null, m1);
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(create);
        oos.close();
        Object back = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        RelationNotification c2 = (RelationNotification) back;
        check(c2 instanceof RelationNotification && c2 instanceof javax.management.Notification, "deserialized type");
        check("jmx.relation.creation.basic".equals(c2.getType()), "creation round-trip type");
        check("id".equals(c2.getRelationId()) && "t".equals(c2.getRelationTypeName()), "creation round-trip ids");
        check(c2.getObjectName() == null, "creation round-trip objectName");
        check(c2.getMBeansToUnregister().equals(m1), "creation round-trip unregister list");
        check(c2.getSequenceNumber() == 10L && c2.getTimeStamp() == 20L && "m".equals(c2.getMessage()), "creation round-trip inherited fields");
        check(src.equals(c2.getSource()), "creation round-trip source");

        RelationNotification update = new RelationNotification(
                RelationNotification.RELATION_BASIC_UPDATE, src, 11L, 21L, "m", "id", "t", rel, "roleX", m1, new ArrayList<ObjectName>());
        bos = new java.io.ByteArrayOutputStream();
        oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(update);
        oos.close();
        RelationNotification u2 = (RelationNotification) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check("jmx.relation.update.basic".equals(u2.getType()), "update round-trip type");
        check("roleX".equals(u2.getRoleName()), "update round-trip role name");
        check(u2.getNewRoleValue().equals(m1), "update round-trip new value");
        check(u2.getOldRoleValue().isEmpty(), "update round-trip old value");
        check(rel.equals(u2.getObjectName()), "update round-trip objectName");
    }

    static void deepLiveServiceLoop() throws Exception {
        javax.management.MBeanServer server = javax.management.MBeanServerFactory.newMBeanServer();
        ObjectName rsvc = non("jck.rn:type=RelationService");
        javax.management.relation.RelationService rs = new javax.management.relation.RelationService(true);
        server.registerMBean(rs, rsvc);
        final ObjectName a = non("jck.rn:type=A");
        final ObjectName b = non("jck.rn:type=B");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        rs.createRelationType("jck.T", new javax.management.relation.RoleInfo[]{ new javax.management.relation.RoleInfo("memberRole", Counter.class.getName(), true, true, 1, 2, "d") });
        final List<javax.management.Notification> got = new ArrayList<javax.management.Notification>();
        server.addNotificationListener(rsvc, new javax.management.NotificationListener() {
            public void handleNotification(javax.management.Notification n, Object h) { got.add(n); }
        }, null, null);

        javax.management.relation.RoleList rl = new javax.management.relation.RoleList();
        rl.add(new javax.management.relation.Role("memberRole", nol(a)));
        rs.createRelation("r1", "jck.T", rl);
        check(got.size() == 1 && got.get(0) instanceof RelationNotification, "live creation notification received");
        RelationNotification cn = (RelationNotification) got.get(0);
        check(cn.getRelationId().equals("r1") && "jck.T".equals(cn.getRelationTypeName()), "live creation ids");
        check(rsvc.equals(cn.getSource()), "live creation source is service ObjectName");
        check(cn.getRoleName() == null && cn.getNewRoleValue().isEmpty(), "live creation neutral role fields");

        rs.setRole("r1", new javax.management.relation.Role("memberRole", nol(a, b)));
        check(got.size() == 2, "live update notification received");
        RelationNotification un = (RelationNotification) got.get(1);
        check("jmx.relation.update.basic".equals(un.getType()), "live update type");
        check("memberRole".equals(un.getRoleName()), "live update role name");
        check(un.getOldRoleValue().equals(nol(a)), "live update old value");
        check(un.getNewRoleValue().equals(nol(a, b)), "live update new value");

        rs.removeRelation("r1");
        check(got.size() == 3 && "jmx.relation.removal.basic".equals(got.get(2).getType()), "live removal notification type");
        check("Removal of relation r1".equals(got.get(2).getMessage()), "live removal message");
        check(((RelationNotification) got.get(2)).getMBeansToUnregister().isEmpty(), "live removal unregister list empty");
    }
}
