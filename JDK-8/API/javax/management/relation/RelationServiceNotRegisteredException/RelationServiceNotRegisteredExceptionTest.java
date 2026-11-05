/*
 * @test
 * @summary javax.management.relation.RelationServiceNotRegisteredException conformance: exception hierarchy and construction.
 * @run main RelationServiceNotRegisteredExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationService;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class RelationServiceNotRegisteredExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.RelationServiceNotRegisteredException x = new javax.management.relation.RelationServiceNotRegisteredException("msg");
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
        RelationServiceNotRegisteredException noArg = new RelationServiceNotRegisteredException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        RelationServiceNotRegisteredException withMsg = new RelationServiceNotRegisteredException("contract-marker-RelationServiceNotRegisteredException");
        check("contract-marker-RelationServiceNotRegisteredException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(RelationServiceNotRegisteredException.class).getSerialVersionUID();
        check(suid == 8454744887157122910L, "serialVersionUID locked across releases: " + suid);
        RelationServiceNotRegisteredException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-RelationServiceNotRegisteredException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(RelationServiceNotRegisteredException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        String expected = "Relation Service not registered in the MBean Server.";
        RelationService unregistered = new RelationService(false);
        checkThrowsMsg(RelationServiceNotRegisteredException.class, () -> unregistered.isActive(),
                "isActive before registration", expected);
        checkThrowsMsg(RelationServiceNotRegisteredException.class,
                () -> unregistered.createRelation("R", "jck.T", counterRole(observed(1))),
                "createRelation before registration", expected);
        checkThrowsMsg(RelationServiceNotRegisteredException.class, () -> unregistered.purgeRelations(),
                "purgeRelations before registration", expected);
        checkThrowsMsg(RelationServiceNotRegisteredException.class,
                () -> unregistered.removeRelationType("jck.T"),
                "removeRelationType before registration", expected);
        // pure queries do not require registration: getAllRelationIds answers empty
        check(unregistered.getAllRelationIds().isEmpty(),
              "getAllRelationIds works without registration and answers an empty list");
        // once registered through preRegister the very same instance stops throwing
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        server.registerMBean(unregistered, new ObjectName("jck.rel:type=LateRegistered"));
        unregistered.isActive();
        passed++; // no exception: registered service is active
        // positive contrast with the standard fixture
        setUpRelationService();
        relationService.isActive();
        passed++; // no exception: the fixture service is registered and active
    }
}
