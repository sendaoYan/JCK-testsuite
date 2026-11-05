/*
 * @test
 * @summary javax.management.relation.RelationTypeNotFoundException conformance: exception hierarchy and construction.
 * @run main RelationTypeNotFoundExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationService;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.RelationTypeSupport;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class RelationTypeNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.RelationTypeNotFoundException x = new javax.management.relation.RelationTypeNotFoundException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.relation.RelationException").isAssignableFrom(c), "extends javax.management.relation.RelationException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testContractBasics();
        testRealTrigger();
        System.out.println("PASS (" + passed + " assertions)");
    }

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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String msg, String exactMsg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && exactMsg.equals(t.getMessage())) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(\"" + exactMsg
                    + "\") but got " + t.getClass().getName() + "(\"" + t.getMessage() + "\")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + "(\"" + exactMsg + "\") but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    // ---- deepened (batch k): local RelationService closed-loop fixture ----

    public static interface ObservedMBean { int getCount(); void setCount(int count); }

    public static class Observed implements ObservedMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int count) { this.count = count; }
    }

    static MBeanServer relationServer;
    static ObjectName relationServiceName;
    static RelationService relationService;

    static ObjectName observed(int i) {
        try { return new ObjectName("jck.rel:type=Observed,instance=" + i); }
        catch (Exception e) { throw new RuntimeException(e); }
    }

    static void setUpRelationService() throws Exception {
        relationServer = MBeanServerFactory.newMBeanServer();
        relationServiceName = new ObjectName("jck.rel:type=RelationService");
        relationService = new RelationService(true);
        relationServer.registerMBean(relationService, relationServiceName);
        for (int i = 1; i <= 3; i++) relationServer.registerMBean(new Observed(), observed(i));
        relationService.createRelationType("jck.T", new RoleInfo[]{
                new RoleInfo("counterRole", Observed.class.getName(), true, true, 1, 2, "counter role") });
    }

    static RoleList counterRole(ObjectName... members) throws Exception {
        RoleList list = new RoleList();
        list.add(new Role("counterRole", Arrays.asList(members)));
        return list;
    }

    // ---- deepened (batch k): constructor chain, cause, serialVersionUID, serialization ----

    static void testContractBasics() throws Exception {
        RelationTypeNotFoundException noArg = new RelationTypeNotFoundException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        RelationTypeNotFoundException withMsg = new RelationTypeNotFoundException("contract-marker-RelationTypeNotFoundException");
        check("contract-marker-RelationTypeNotFoundException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(RelationTypeNotFoundException.class).getSerialVersionUID();
        check(suid == 1274155316284300752L, "serialVersionUID locked across releases: " + suid);
        RelationTypeNotFoundException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-RelationTypeNotFoundException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(RelationTypeNotFoundException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        setUpRelationService();
        String expected = "No relation type created in the Relation Service with the name jck.NoSuchType";
        checkThrowsMsg(RelationTypeNotFoundException.class,
                () -> relationService.createRelation("R2", "jck.NoSuchType", counterRole(observed(1))),
                "createRelation with an unknown relation type", expected);
        checkThrowsMsg(RelationTypeNotFoundException.class,
                () -> relationService.getRoleInfos("jck.NoSuchType"),
                "getRoleInfos with an unknown relation type", expected);
        checkThrowsMsg(RelationTypeNotFoundException.class,
                () -> relationService.getRoleInfo("jck.NoSuchType", "counterRole"),
                "getRoleInfo with an unknown relation type", expected);
        checkThrowsMsg(RelationTypeNotFoundException.class,
                () -> relationService.removeRelationType("jck.NoSuchType"),
                "removeRelationType with an unknown relation type", expected);
        // positive contrast: the declared type resolves and no relation leaked from the failures
        check(relationService.getRoleInfos("jck.T").size() == 1, "the declared type resolves its role infos");
        check(relationService.getRoleInfo("jck.T", "counterRole").getName().equals("counterRole"),
              "the declared type resolves a role info by name");
        check(relationService.getAllRelationIds().isEmpty(),
              "failed creations left no relation behind");
    }
}
