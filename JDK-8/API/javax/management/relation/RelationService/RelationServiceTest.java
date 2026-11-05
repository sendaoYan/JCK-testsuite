/*
 * @test
 * @summary javax.management.relation.RelationService conformance: concrete class reflection.
 * @run main RelationServiceTest
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.InstanceNotFoundException;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.ObjectName;
import javax.management.relation.InvalidRelationIdException;
import javax.management.relation.InvalidRoleValueException;
import javax.management.relation.Relation;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationNotification;
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationSupport;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RoleResult;
import javax.management.relation.RoleStatus;
import javax.management.relation.RoleUnresolved;
import javax.management.relation.RoleNotFoundException;
import javax.management.relation.RoleUnresolvedList;

public class RelationServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationService");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.NotificationBroadcasterSupport".equals(c.getSuperclass().getName()), "superclass javax.management.NotificationBroadcasterSupport");
        check(Class.forName("javax.management.relation.RelationServiceMBean").isAssignableFrom(c), "implements/extends javax.management.relation.RelationServiceMBean");
        check(Class.forName("javax.management.MBeanRegistration").isAssignableFrom(c), "implements/extends javax.management.MBeanRegistration");
        check(Class.forName("javax.management.NotificationListener").isAssignableFrom(c), "implements/extends javax.management.NotificationListener");
        check(hasMethod(c, "addRelation"), "declares addRelation");
        check(hasMethod(c, "addRelationType"), "declares addRelationType");
        check(hasMethod(c, "checkRoleReading"), "declares checkRoleReading");

        deepCreateRelationLoop();
        deepRoleConstraints();
        deepAccessModesAndStatus();
        deepMultiRoleResults();
        deepPurgeSemantics();
        deepNotificationLoop();
        deepExternalRelation();
        deepRelationSupportAsRelationMBean();
        deepRemoveRelationTypeAndRoleMap();
        deepUnregisteredServiceRejection();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened (batch l): local MBeanServer closed loops ----

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

    /** Recording user Relation, exposed as a standard MBean via ExtRelMBean extends Relation. */
    public static interface ExtRelMBean extends Relation { }
    public static class ExtRel implements ExtRelMBean {
        final String id;
        final ObjectName svc;
        final String type;
        Role role;
        final List<String> calls = new ArrayList<String>();
        ExtRel(String id, ObjectName svc, String type, Role r) { this.id = id; this.svc = svc; this.type = type; this.role = r; }
        public List<ObjectName> getRole(String roleName) throws RoleNotFoundException {
            calls.add("getRole:" + roleName);
            if (!role.getRoleName().equals(roleName)) throw new RoleNotFoundException(roleName);
            return role.getRoleValue();
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
            calls.add("cardinality:" + roleName);
            if (!role.getRoleName().equals(roleName)) throw new RoleNotFoundException(roleName);
            return Integer.valueOf(role.getRoleValue().size());
        }
        public RoleResult getAllRoles() { calls.add("getAllRoles"); return getRoles(new String[]{ role.getRoleName() }); }
        public RoleList retrieveAllRoles() {
            calls.add("retrieveAllRoles");
            RoleList rl = new RoleList();
            rl.add((Role) role.clone());
            return rl;
        }
        public void setRole(Role r) {
            calls.add("setRole:" + r.getRoleName());
            role = (Role) r.clone();
        }
        public RoleResult setRoles(RoleList list) {
            calls.add("setRoles");
            for (Object o : list) setRole((Role) o);
            return new RoleResult(list, new RoleUnresolvedList());
        }
        public void handleMBeanUnregistration(ObjectName name, String roleName) {
            calls.add("handleMBeanUnregistration:" + name + ":" + roleName);
            List<ObjectName> v = new ArrayList<ObjectName>(role.getRoleValue());
            v.remove(name);
            role = new Role(role.getRoleName(), v);
        }
        public Map<ObjectName, List<String>> getReferencedMBeans() {
            calls.add("getReferencedMBeans");
            Map<ObjectName, List<String>> m = new HashMap<ObjectName, List<String>>();
            for (ObjectName n : role.getRoleValue()) m.put(n, new ArrayList<String>(Arrays.asList(role.getRoleName())));
            return m;
        }
        public String getRelationTypeName() { return type; }
        public ObjectName getRelationServiceName() { return svc; }
        public String getRelationId() { return id; }
    }

    static class Rec implements NotificationListener {
        final List<Notification> got = new ArrayList<Notification>();
        public void handleNotification(Notification n, Object h) { got.add(n); }
    }

    interface Thrower { void run() throws Throwable; }
    static ObjectName ron(String s) { try { return new ObjectName(s); } catch (Exception e) { throw new RuntimeException(e); } }
    static List<ObjectName> ol(ObjectName... names) { return new ArrayList<ObjectName>(Arrays.asList(names)); }
    static Role roleOf(String name, ObjectName... names) { return new Role(name, ol(names)); }
    static RoleList roleListOf(Role... rs) { RoleList rl = new RoleList(); for (Role r : rs) rl.add(r); return rl; }
    static void checkThrowsX(Class<? extends Throwable> type, String msg, Thrower r, String label) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t) && (msg == null || msg.equals(t.getMessage()))) { passed++; return; }
            throw new AssertionError(label + ": expected " + type.getSimpleName() + "('" + msg + "') but got "
                    + t.getClass().getName() + "('" + t.getMessage() + "')", t);
        }
        throw new AssertionError(label + ": expected " + type.getSimpleName() + " but nothing was thrown");
    }

    static final String RSVC = "jck.rsvc:type=RelationService";
    static RoleInfo counterRole(String name, int min, int max) throws Exception {
        return new RoleInfo(name, Counter.class.getName(), true, true, min, max, "d");
    }

    /** createRelation closed loop: notification + every query method exact. */
    static void deepCreateRelationLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        ObjectName a = ron("jck.rsvc:type=A");
        ObjectName b = ron("jck.rsvc:type=B");
        ObjectName c = ron("jck.rsvc:type=C");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        server.registerMBean(new Counter(), c);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        Rec l = new Rec();
        server.addNotificationListener(rsvcName, l, null, null);

        rs.createRelation("r1", "jck.T", roleListOf(roleOf("memberRole", a, b)));
        check(l.got.size() == 1, "createRelation emits exactly one creation notification");
        RelationNotification cn = (RelationNotification) l.got.get(0);
        check(RelationNotification.RELATION_BASIC_CREATION.equals(cn.getType()), "creation notification type basic");
        check("Creation of relation r1".equals(cn.getMessage()), "creation notification message");
        check(rsvcName.equals(cn.getSource()), "notification source rewritten to service ObjectName");
        check("r1".equals(cn.getRelationId()), "creation notification relation id");
        check("jck.T".equals(cn.getRelationTypeName()), "creation notification type name");
        check(cn.getObjectName() == null, "internal relation has null objectName");
        check(cn.getRoleName() == null, "creation notification roleName null");
        check(cn.getOldRoleValue().isEmpty() && cn.getNewRoleValue().isEmpty(), "creation notification role values empty");
        check(cn.getMBeansToUnregister().isEmpty(), "creation notification unregister list empty");

        check(rs.getAllRelationIds().size() == 1 && rs.getAllRelationIds().contains("r1"), "getAllRelationIds contains r1");
        check(rs.getAllRelationTypeNames().size() == 1 && rs.getAllRelationTypeNames().contains("jck.T"), "getAllRelationTypeNames contains jck.T");
        check("jck.T".equals(rs.getRelationTypeName("r1")), "getRelationTypeName exact");
        check(Boolean.TRUE.equals(rs.hasRelation("r1")), "hasRelation true");
        check(rs.isRelationMBean("r1") == null, "internal relation is not an MBean");
        check(rs.isRelation(a) == null, "plain member MBean is not a relation");
        check(rs.getRole("r1", "memberRole").equals(ol(a, b)), "getRole exact value");
        check(rs.getRoleCardinality("r1", "memberRole").intValue() == 2, "getRoleCardinality 2");
        RoleList all = rs.getAllRoles("r1").getRoles();
        check(all.size() == 1 && "memberRole".equals(((Role) all.get(0)).getRoleName())
                && ((Role) all.get(0)).getRoleValue().equals(ol(a, b)), "getAllRoles exact role");
        check(rs.getAllRoles("r1").getRolesUnresolved().isEmpty(), "getAllRoles no unresolved");
        Map<ObjectName, List<String>> refs = rs.getReferencedMBeans("r1");
        check(refs.size() == 2 && refs.get(a).equals(Arrays.asList("memberRole"))
                && refs.get(b).equals(Arrays.asList("memberRole")), "getReferencedMBeans exact");
        check(rs.findRelationsOfType("jck.T").equals(Arrays.asList("r1")), "findRelationsOfType exact");
        Map<String, List<String>> fr = rs.findReferencingRelations(a, null, null);
        check(fr.size() == 1 && fr.get("r1").equals(Arrays.asList("memberRole")), "findReferencingRelations unfiltered");
        check(rs.findReferencingRelations(a, "jck.T", "memberRole").size() == 1, "findReferencingRelations filtered hit");
        check(rs.findReferencingRelations(a, "jck.T", "noRole").isEmpty(), "findReferencingRelations role filter excludes");
        check(rs.findReferencingRelations(a, "noType", null).isEmpty(), "findReferencingRelations type filter excludes");
        Map<ObjectName, List<String>> assoc = rs.findAssociatedMBeans(a, null, null);
        check(assoc.size() == 1 && assoc.get(b).equals(Arrays.asList("r1")), "findAssociatedMBeans exact");
        check(rs.findAssociatedMBeans(c, null, null).isEmpty(), "findAssociatedMBeans empty for outsider");
    }

    /** Role constraint matrix: exact exception types and messages. */
    static void deepRoleConstraints() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        final ObjectName a = ron("jck.rsvc:type=A");
        final ObjectName b = ron("jck.rsvc:type=B");
        final ObjectName c = ron("jck.rsvc:type=C");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        server.registerMBean(new Counter(), c);
        final ObjectName t = ron("jck.rsvc:type=Text");
        server.registerMBean(new Text(), t);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        rs.createRelation("r1", "jck.T", roleListOf(roleOf("memberRole", a, b)));

        checkThrowsX(javax.management.relation.RoleNotFoundException.class, "bogusRole does not exist in relation.",
                new Thrower() { public void run() throws Throwable { rs.getRole("r1", "bogusRole"); } }, "getRole unknown role");
        checkThrowsX(RelationNotFoundException.class, "No relation associated to relation id noRel",
                new Thrower() { public void run() throws Throwable { rs.getRole("noRel", "memberRole"); } }, "getRole unknown relation");
        checkThrowsX(InvalidRoleValueException.class, "memberRole has a number of MBean references less than the expected minimum degree.",
                new Thrower() { public void run() throws Throwable { rs.setRole("r1", roleOf("memberRole")); } }, "setRole below min degree");
        checkThrowsX(InvalidRoleValueException.class, "memberRole has a number of MBean references greater than the expected maximum degree.",
                new Thrower() { public void run() throws Throwable { rs.setRole("r1", roleOf("memberRole", a, b, c)); } }, "setRole above max degree");
        checkThrowsX(InvalidRoleValueException.class, "memberRole has a reference to null or to an MBean not registered.",
                new Thrower() { public void run() throws Throwable { rs.setRole("r1", roleOf("memberRole", a, ron("jck.rsvc:type=Ghost"))); } }, "setRole unregistered member");
        checkThrowsX(InvalidRoleValueException.class, "memberRole has an MBean reference to an MBean not of the expected class of references for that role.",
                new Thrower() { public void run() throws Throwable { rs.setRole("r1", roleOf("memberRole", a, t)); } }, "setRole wrong-class member");
        checkThrowsX(javax.management.relation.RoleNotFoundException.class, "bogusRole does not exist in relation.",
                new Thrower() { public void run() throws Throwable { rs.setRole("r1", roleOf("bogusRole", a)); } }, "setRole unknown role name");
        checkThrowsX(RelationNotFoundException.class, "No relation associated to relation id noRel",
                new Thrower() { public void run() throws Throwable { rs.setRole("noRel", roleOf("memberRole", a)); } }, "setRole unknown relation");
        checkThrowsX(InvalidRelationIdException.class, "There is already a relation with id r1",
                new Thrower() { public void run() throws Throwable { rs.createRelation("r1", "jck.T", new RoleList()); } }, "createRelation duplicate id");
        checkThrowsX(RelationTypeNotFoundException.class, "No relation type created in the Relation Service with the name jck.Nope",
                new Thrower() { public void run() throws Throwable { rs.createRelation("rX", "jck.Nope", new RoleList()); } }, "createRelation unknown type");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.getRole(null, "memberRole"); } }, "getRole null id");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.getRole("r1", null); } }, "getRole null role");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.removeRelation(null); } }, "removeRelation null");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.getReferencedMBeans(null); } }, "getReferencedMBeans null");
        check(rs.getRole("r1", "memberRole").equals(ol(a, b)), "failed setRole calls leave role unchanged");
    }

    /** Read/write access modes + RoleStatus constant values. */
    static void deepAccessModesAndStatus() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        final ObjectName a = ron("jck.rsvc:type=A");
        server.registerMBean(new Counter(), a);
        rs.createRelationType("jck.silent", new RoleInfo[]{ new RoleInfo("silentRole", Counter.class.getName(), false, false, 0, 2, "d") });
        rs.createRelationType("jck.opt", new RoleInfo[]{ counterRole("optRole", 0, 2) });
        rs.createRelation("rs1", "jck.silent", roleListOf(roleOf("silentRole", a)));
        checkThrowsX(javax.management.relation.RoleNotFoundException.class, "silentRole is not readable.",
                new Thrower() { public void run() throws Throwable { rs.getRole("rs1", "silentRole"); } }, "getRole unreadable");
        checkThrowsX(javax.management.relation.RoleNotFoundException.class, "silentRole is not writable.",
                new Thrower() { public void run() throws Throwable { rs.setRole("rs1", roleOf("silentRole", a)); } }, "setRole unwritable");
        RoleResult all = rs.getAllRoles("rs1");
        check(all.getRoles().isEmpty() && all.getRolesUnresolved().size() == 1, "getAllRoles unreadable yields unresolved only");
        RoleUnresolved ru = (RoleUnresolved) all.getRolesUnresolved().get(0);
        check("silentRole".equals(ru.getRoleName()) && ru.getProblemType() == RoleStatus.ROLE_NOT_READABLE,
                "unresolved problem type ROLE_NOT_READABLE");
        check(rs.checkRoleReading("silentRole", "jck.silent").intValue() == RoleStatus.ROLE_NOT_READABLE, "checkRoleReading unreadable");
        check(rs.checkRoleReading("nope", "jck.silent").intValue() == RoleStatus.NO_ROLE_WITH_NAME, "checkRoleReading unknown role");
        check(rs.checkRoleWriting(roleOf("silentRole", a), "jck.silent", Boolean.FALSE).intValue() == RoleStatus.ROLE_NOT_WRITABLE,
                "checkRoleWriting unwritable");
        check(RoleStatus.NO_ROLE_WITH_NAME == 1 && RoleStatus.ROLE_NOT_READABLE == 2 && RoleStatus.ROLE_NOT_WRITABLE == 3, "RoleStatus values 1-3");
        check(RoleStatus.LESS_THAN_MIN_ROLE_DEGREE == 4 && RoleStatus.MORE_THAN_MAX_ROLE_DEGREE == 5, "RoleStatus values 4-5");
        check(RoleStatus.REF_MBEAN_OF_INCORRECT_CLASS == 6 && RoleStatus.REF_MBEAN_NOT_REGISTERED == 7, "RoleStatus values 6-7");
        check(RoleStatus.isRoleStatus(4) && !RoleStatus.isRoleStatus(0), "isRoleStatus bounds");
        rs.createRelation("ro", "jck.opt", new RoleList());
        check(rs.getRole("ro", "optRole").isEmpty(), "min-degree-0 role defaults to empty");
        check(rs.getRoleCardinality("ro", "optRole").intValue() == 0, "defaulted role cardinality 0");
        rs.createRelationType("jck.need", new RoleInfo[]{ counterRole("needRole", 1, 2) });
        checkThrowsX(InvalidRoleValueException.class, "needRole has a number of MBean references less than the expected minimum degree.",
                new Thrower() { public void run() throws Throwable { rs.createRelation("rn", "jck.need", new RoleList()); } },
                "createRelation missing mandatory role");
        checkThrowsX(InvalidRoleValueException.class, "needRole has a number of MBean references less than the expected minimum degree.",
                new Thrower() { public void run() throws Throwable { rs.createRelation("rn2", "jck.need", null); } },
                "createRelation null roleList with mandatory role");
        // recorded wart: null RoleList with min-degree-0 role hits unguarded roleList.asList() -> NPE (both JDKs)
        try { rs.createRelation("rn3", "jck.opt", null); check(false, "null roleList min-0 must NPE (recorded actual)"); }
        catch (NullPointerException e) { passed++; }
    }

    /** getRoles/setRoles mixed success+failure RoleResult structure. */
    static void deepMultiRoleResults() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        ObjectName a = ron("jck.rsvc:type=A");
        ObjectName b = ron("jck.rsvc:type=B");
        ObjectName c = ron("jck.rsvc:type=C");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        server.registerMBean(new Counter(), c);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        rs.createRelation("r1", "jck.T", roleListOf(roleOf("memberRole", a, b)));

        RoleResult mixed = rs.getRoles("r1", new String[]{ "memberRole", "bogusRole" });
        check(mixed.getRoles().size() == 1, "getRoles success part size");
        Role ok = (Role) mixed.getRoles().get(0);
        check("memberRole".equals(ok.getRoleName()) && ok.getRoleValue().equals(ol(a, b)), "getRoles success part value");
        check(mixed.getRolesUnresolved().size() == 1, "getRoles unresolved part size");
        RoleUnresolved ru = (RoleUnresolved) mixed.getRolesUnresolved().get(0);
        check("bogusRole".equals(ru.getRoleName()), "unresolved role name");
        check(ru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME, "unresolved problem type NO_ROLE_WITH_NAME");
        check(ru.getRoleValue() == null, "getRoles unresolved has null value");

        RoleResult srr = rs.setRoles("r1", roleListOf(roleOf("memberRole", c), roleOf("bogusRole", a)));
        check(srr.getRoles().size() == 1 && ((Role) srr.getRoles().get(0)).getRoleValue().equals(ol(c)), "setRoles success part");
        check(srr.getRolesUnresolved().size() == 1, "setRoles unresolved part");
        RoleUnresolved sru = (RoleUnresolved) srr.getRolesUnresolved().get(0);
        check("bogusRole".equals(sru.getRoleName()) && sru.getProblemType() == RoleStatus.NO_ROLE_WITH_NAME,
                "setRoles unresolved details");
        check(sru.getRoleValue().equals(ol(a)), "setRoles unresolved keeps attempted value");
        check(rs.getRole("r1", "memberRole").equals(ol(c)), "setRoles applied the successful role");
        // below-min role inside setRoles is reported as unresolved, role not applied
        RoleResult srr2 = rs.setRoles("r1", roleListOf(roleOf("memberRole")));
        check(srr2.getRoles().isEmpty() && srr2.getRolesUnresolved().size() == 1, "setRoles below-min unresolved only");
        RoleUnresolved sru2 = (RoleUnresolved) srr2.getRolesUnresolved().get(0);
        check(sru2.getProblemType() == RoleStatus.LESS_THAN_MIN_ROLE_DEGREE, "setRoles below-min problem type");
        check(rs.getRole("r1", "memberRole").equals(ol(c)), "setRoles failure leaves role unchanged");
    }

    /** immediatePurge=true vs false + drop-below-min-degree relation removal. */
    static void deepPurgeSemantics() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        ObjectName a = ron("jck.rsvc:type=A");
        ObjectName b = ron("jck.rsvc:type=B");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        Rec l = new Rec();
        server.addNotificationListener(rsvcName, l, null, null);
        rs.createRelation("r1", "jck.T", roleListOf(roleOf("memberRole", a, b)));
        l.got.clear();

        server.unregisterMBean(b);
        check(rs.getRole("r1", "memberRole").equals(ol(a)), "immediate purge removes unregistered member");
        check(rs.getReferencedMBeans("r1").size() == 1, "reference map shrinks after immediate purge");
        check(rs.findReferencingRelations(b, null, null).isEmpty(), "unregistered member no longer referenced");
        check(rs.findReferencingRelations(a, null, null).size() == 1, "remaining member still referenced");
        check(l.got.size() == 1 && RelationNotification.RELATION_BASIC_UPDATE.equals(l.got.get(0).getType()),
                "member purge emits a role update notification");
        RelationNotification pu = (RelationNotification) l.got.get(0);
        check("memberRole".equals(pu.getRoleName()) && pu.getOldRoleValue().equals(ol(a, b))
                && pu.getNewRoleValue().equals(ol(a)), "purge update notification old/new values");
        l.got.clear();

        server.unregisterMBean(a);
        check(Boolean.FALSE.equals(rs.hasRelation("r1")), "purge below min degree removes relation");
        check(rs.getAllRelationIds().isEmpty(), "relation id list empty after purge removal");
        check(l.got.size() == 1 && RelationNotification.RELATION_BASIC_REMOVAL.equals(l.got.get(0).getType()),
                "purge removal notification type");
        check("Removal of relation r1".equals(l.got.get(0).getMessage()), "purge removal notification message");

        MBeanServer server2 = MBeanServerFactory.newMBeanServer();
        ObjectName rsvc2 = ron("jck.rsvc2:type=RelationService");
        RelationService rs2 = new RelationService(false);
        server2.registerMBean(rs2, rsvc2);
        ObjectName a2 = ron("jck.rsvc2:type=A");
        ObjectName b2 = ron("jck.rsvc2:type=B");
        server2.registerMBean(new Counter(), a2);
        server2.registerMBean(new Counter(), b2);
        rs2.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        rs2.createRelation("r2", "jck.T", roleListOf(roleOf("memberRole", a2, b2)));
        server2.unregisterMBean(a2);
        check(rs2.getRole("r2", "memberRole").equals(ol(a2, b2)), "deferred purge keeps stale value");
        rs2.purgeRelations();
        check(rs2.getRole("r2", "memberRole").equals(ol(b2)), "explicit purgeRelations applies member removal");
    }

    /** Full notification lifecycle through a registered service MBean. */
    static void deepNotificationLoop() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        final ObjectName a = ron("jck.rsvc:type=A");
        final ObjectName b = ron("jck.rsvc:type=B");
        final ObjectName c = ron("jck.rsvc:type=C");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        server.registerMBean(new Counter(), c);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        Rec l = new Rec();
        server.addNotificationListener(rsvcName, l, null, null);
        rs.createRelation("r1", "jck.T", roleListOf(roleOf("memberRole", a, b)));
        rs.setRole("r1", roleOf("memberRole", c));
        rs.setRole("r1", roleOf("memberRole", a, b));
        check(l.got.size() == 3, "creation + two updates received");
        RelationNotification u1 = (RelationNotification) l.got.get(1);
        check(RelationNotification.RELATION_BASIC_UPDATE.equals(u1.getType()), "update notification type");
        check("r1".equals(u1.getRelationId()) && "jck.T".equals(u1.getRelationTypeName()), "update notification ids");
        check("memberRole".equals(u1.getRoleName()), "update notification role name");
        check(u1.getOldRoleValue().equals(ol(a, b)), "update notification old value");
        check(u1.getNewRoleValue().equals(ol(c)), "update notification new value");
        check(u1.getObjectName() == null, "internal relation update objectName null");
        check(u1.getMBeansToUnregister().isEmpty(), "update notification unregister list empty");
        String want = "Value of role memberRole has changed\nOld value:\n" + a + "\n" + b + "\nNew value:\n" + c;
        check(want.equals(u1.getMessage()), "update notification exact message");
        RelationNotification u2 = (RelationNotification) l.got.get(2);
        check(u2.getOldRoleValue().equals(ol(c)) && u2.getNewRoleValue().equals(ol(a, b)), "second update old/new swapped correctly");
        check(l.got.get(0).getSequenceNumber() < l.got.get(1).getSequenceNumber()
                && l.got.get(1).getSequenceNumber() < l.got.get(2).getSequenceNumber(), "sequence numbers strictly increasing");

        rs.removeRelation("r1");
        check(l.got.size() == 4, "removal notification received");
        RelationNotification rm = (RelationNotification) l.got.get(3);
        check(RelationNotification.RELATION_BASIC_REMOVAL.equals(rm.getType()), "removal notification type");
        check("Removal of relation r1".equals(rm.getMessage()), "removal notification message");
        check("r1".equals(rm.getRelationId()), "removal notification relation id");
        check(rm.getMBeansToUnregister().isEmpty(), "removal notification unregister list empty");
        checkThrowsX(RelationNotFoundException.class, "No relation associated to relation id ghost",
                new Thrower() { public void run() throws Throwable { rs.sendRelationCreationNotification("ghost"); } },
                "sendRelationCreationNotification unknown relation");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.sendRoleUpdateNotification(null, null, null); } },
                "sendRoleUpdateNotification null");

        rs.createRelation("r9", "jck.T", roleListOf(roleOf("memberRole", a)));
        rs.sendRelationCreationNotification("r9");
        RelationNotification c2 = (RelationNotification) l.got.get(l.got.size() - 1);
        check(RelationNotification.RELATION_BASIC_CREATION.equals(c2.getType()), "manual creation notification type");
        check("Creation of relation r9".equals(c2.getMessage()), "manual creation notification message");
        rs.sendRoleUpdateNotification("r9", roleOf("memberRole", b), ol(a));
        RelationNotification u3 = (RelationNotification) l.got.get(l.got.size() - 1);
        check(RelationNotification.RELATION_BASIC_UPDATE.equals(u3.getType()) && "memberRole".equals(u3.getRoleName()),
                "manual update notification");
        check(u3.getOldRoleValue().equals(ol(a)) && u3.getNewRoleValue().equals(ol(b)), "manual update old/new values");
        rs.sendRelationRemovalNotification("r9", ol(a));
        RelationNotification r2 = (RelationNotification) l.got.get(l.got.size() - 1);
        check(RelationNotification.RELATION_BASIC_REMOVAL.equals(r2.getType()), "manual removal notification type");
        check(r2.getMBeansToUnregister().equals(ol(a)), "manual removal unregister list preserved");
        MBeanNotificationInfo[] ni = rs.getNotificationInfo();
        check(ni.length == 1 && ni[0].getNotifTypes().length == 6, "getNotificationInfo exposes 6 relation types");
        check(Arrays.asList(ni[0].getNotifTypes()).containsAll(Arrays.asList(
                RelationNotification.RELATION_BASIC_CREATION, RelationNotification.RELATION_MBEAN_CREATION,
                RelationNotification.RELATION_BASIC_UPDATE, RelationNotification.RELATION_MBEAN_UPDATE,
                RelationNotification.RELATION_BASIC_REMOVAL, RelationNotification.RELATION_MBEAN_REMOVAL)),
                "getNotificationInfo type constants");
    }

    /** addRelation: user Relation MBean routed through the service. */
    static void deepExternalRelation() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        final ObjectName c = ron("jck.rsvc:type=C");
        server.registerMBean(new Counter(), c);
        rs.createRelationType("jck.T2", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        ExtRel ext = new ExtRel("ext1", rsvcName, "jck.T2", roleOf("memberRole", c));
        final ObjectName extName = ron("jck.rsvc:type=ExtRel");
        server.registerMBean(ext, extName);
        Rec l = new Rec();
        server.addNotificationListener(rsvcName, l, null, null);

        rs.addRelation(extName);
        check(rs.getAllRelationIds().contains("ext1"), "addRelation registers external relation id");
        check(extName.equals(rs.isRelationMBean("ext1")), "isRelationMBean returns relation MBean name");
        check("ext1".equals(rs.isRelation(extName)), "isRelation returns relation id");
        check("jck.T2".equals(rs.getRelationTypeName("ext1")), "getRelationTypeName for external relation");
        check(l.got.size() == 1 && RelationNotification.RELATION_MBEAN_CREATION.equals(l.got.get(0).getType()),
                "addRelation emits RELATION_MBEAN_CREATION");
        check(((RelationNotification) l.got.get(0)).getObjectName().equals(extName), "mbean creation notification objectName");

        check(rs.getRole("ext1", "memberRole").equals(ol(c)), "service getRole routed to user Relation");
        check(ext.calls.contains("getRole:memberRole"), "user Relation getRole really invoked");
        check(rs.getReferencedMBeans("ext1").get(c).equals(Arrays.asList("memberRole")), "getReferencedMBeans routed");
        check(ext.calls.contains("getReferencedMBeans"), "user Relation getReferencedMBeans really invoked");
        check(rs.findReferencingRelations(c, "jck.T2", null).size() == 1, "external relation participates in queries");
        ext.calls.clear();
        rs.setRole("ext1", roleOf("memberRole", c));
        check(ext.calls.contains("setRole:memberRole"), "service setRole routed to user Relation");
        check(l.got.size() == 1, "user Relation update without sendRoleUpdateNotification emits nothing");

        rs.removeRelation("ext1");
        check(rs.isRelation(extName) == null, "removeRelation detaches external relation");
        check(server.isRegistered(extName), "relation MBean stays registered after removeRelation");
        checkThrowsX(RelationNotFoundException.class, "No relation associated to relation id ext1",
                new Thrower() { public void run() throws Throwable { rs.getRole("ext1", "memberRole"); } },
                "getRole after removeRelation");

        ObjectName plain = ron("jck.rsvc:type=Plain");
        server.registerMBean(new Counter(), plain);
        checkThrowsX(NoSuchMethodException.class, "This MBean does not implement the Relation interface.",
                new Thrower() { public void run() throws Throwable { rs.addRelation(plain); } },
                "addRelation non-Relation MBean");
        checkThrowsX(InstanceNotFoundException.class, "jck.rsvc:type=Ghost",
                new Thrower() { public void run() throws Throwable { rs.addRelation(ron("jck.rsvc:type=Ghost")); } },
                "addRelation unregistered MBean");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.addRelation(null); } }, "addRelation null");
    }

    /** RelationSupport registered as an MBean then added as a relation. */
    static void deepRelationSupportAsRelationMBean() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        ObjectName a = ron("jck.rsvc:type=A");
        server.registerMBean(new Counter(), a);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        Rec l = new Rec();
        server.addNotificationListener(rsvcName, l, null, null);

        RelationSupport sup = new RelationSupport("mrel1", rsvcName, server, "jck.T", roleListOf(roleOf("memberRole", a)));
        ObjectName supName = ron("jck.rsvc:type=RelSup");
        server.registerMBean(sup, supName);
        check(!sup.isInRelationService().booleanValue(), "management flag false before addRelation");
        rs.addRelation(supName);
        check(supName.equals(rs.isRelationMBean("mrel1")), "isRelationMBean for RelationSupport relation");
        check("mrel1".equals(rs.isRelation(supName)), "isRelation for RelationSupport relation");
        check(sup.isInRelationService().booleanValue(), "addRelation sets management flag");
        check(l.got.size() == 1 && RelationNotification.RELATION_MBEAN_CREATION.equals(l.got.get(0).getType()),
                "creation notification uses mbean variant");
        rs.setRole("mrel1", roleOf("memberRole", a));
        check(l.got.size() == 2 && RelationNotification.RELATION_MBEAN_UPDATE.equals(l.got.get(1).getType()),
                "update notification uses mbean variant");
        check(((RelationNotification) l.got.get(1)).getObjectName().equals(supName), "mbean update notification objectName");
        RoleList back = sup.retrieveAllRoles();
        check(back.size() == 1 && ((Role) back.get(0)).getRoleValue().equals(ol(a)), "RelationSupport role updated via service");

        RelationSupport sup2 = new RelationSupport("mrel2", rsvcName, "jck.T", roleListOf(roleOf("memberRole", a)));
        ObjectName sup2Name = ron("jck.rsvc:type=RelSup2");
        server.registerMBean(sup2, sup2Name);
        rs.addRelation(sup2Name);
        check(rs.getRole("mrel2", "memberRole").equals(ol(a)), "4-arg ctor captures server via preRegister");
    }

    /** removeRelationType removes dependent relations; updateRoleMap moves the service map only. */
    static void deepRemoveRelationTypeAndRoleMap() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName rsvcName = ron(RSVC);
        final RelationService rs = new RelationService(true);
        server.registerMBean(rs, rsvcName);
        final ObjectName a = ron("jck.rsvc:type=A");
        final ObjectName b = ron("jck.rsvc:type=B");
        server.registerMBean(new Counter(), a);
        server.registerMBean(new Counter(), b);
        rs.createRelationType("jck.T", new RoleInfo[]{ counterRole("memberRole", 1, 2) });
        rs.createRelation("ra", "jck.T", roleListOf(roleOf("memberRole", a)));

        rs.updateRoleMap("ra", roleOf("memberRole", b), ol(a));
        check(rs.findReferencingRelations(b, null, null).size() == 1, "updateRoleMap registers new reference in service map");
        check(rs.findReferencingRelations(a, null, null).isEmpty(), "updateRoleMap drops old reference from service map");
        check(rs.getRole("ra", "memberRole").equals(ol(a)), "updateRoleMap leaves relation value untouched");
        checkThrowsX(IllegalArgumentException.class, "Invalid parameter.",
                new Thrower() { public void run() throws Throwable { rs.updateRoleMap(null, null, null); } }, "updateRoleMap null");

        rs.removeRelationType("jck.T");
        check(!rs.getAllRelationTypeNames().contains("jck.T"), "removeRelationType unregisters type");
        check(Boolean.FALSE.equals(rs.hasRelation("ra")), "relations of removed type are removed");
        check(rs.getAllRelationIds().isEmpty(), "relation ids empty after type removal");
        checkThrowsX(RelationTypeNotFoundException.class, "No relation type created in the Relation Service with the name jck.T",
                new Thrower() { public void run() throws Throwable { rs.createRelation("ra2", "jck.T", new RoleList()); } },
                "createRelation after removeRelationType");
        checkThrowsX(RelationTypeNotFoundException.class, "No relation type created in the Relation Service with the name jck.T",
                new Thrower() { public void run() throws Throwable { rs.removeRelationType("jck.T"); } },
                "removeRelationType unknown type");
    }

    /** Unregistered RelationService rejects relation operations. */
    static void deepUnregisteredServiceRejection() throws Exception {
        final RelationService rs = new RelationService(true);
        checkThrowsX(RelationServiceNotRegisteredException.class, "Relation Service not registered in the MBean Server.",
                new Thrower() { public void run() throws Throwable { rs.createRelation("x", "t", new RoleList()); } },
                "createRelation unregistered service");
        checkThrowsX(RelationServiceNotRegisteredException.class, "Relation Service not registered in the MBean Server.",
                new Thrower() { public void run() throws Throwable { rs.removeRelation("x"); } },
                "removeRelation unregistered service");
        checkThrowsX(RelationServiceNotRegisteredException.class, "Relation Service not registered in the MBean Server.",
                new Thrower() { public void run() throws Throwable { rs.purgeRelations(); } },
                "purgeRelations unregistered service");
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName n = ron(RSVC);
        RelationService rs2 = new RelationService(true);
        server.registerMBean(rs2, n);
        rs2.isActive();
        server.unregisterMBean(n);
        // recorded actual behavior: postDeregister does not reset the flag, isActive() still passes (both JDKs)
        rs2.isActive();
        passed++;
    }
}
