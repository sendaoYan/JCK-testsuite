/*
 * @test
 * @summary javax.management.relation.InvalidRelationIdException conformance: exception hierarchy and construction.
 * @run main InvalidRelationIdExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.InvalidRelationIdException;
import javax.management.relation.RelationService;
import javax.management.relation.RelationSupport;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class InvalidRelationIdExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.InvalidRelationIdException x = new javax.management.relation.InvalidRelationIdException("msg");
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
        InvalidRelationIdException noArg = new InvalidRelationIdException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        InvalidRelationIdException withMsg = new InvalidRelationIdException("contract-marker-InvalidRelationIdException");
        check("contract-marker-InvalidRelationIdException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(InvalidRelationIdException.class).getSerialVersionUID();
        check(suid == -7115040321202754171L, "serialVersionUID locked across releases: " + suid);
        InvalidRelationIdException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-InvalidRelationIdException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(InvalidRelationIdException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        setUpRelationService();
        relationService.createRelation("dup", "jck.T", counterRole(observed(1)));
        checkThrowsMsg(InvalidRelationIdException.class,
                () -> relationService.createRelation("dup", "jck.T", counterRole(observed(2))),
                "createRelation reusing the id of an internal relation",
                "There is already a relation with id dup");
        // an external relation added through addRelation reserves its id as well
        RelationSupport external =
                new RelationSupport("ext", relationServiceName, relationServer, "jck.T", counterRole(observed(1)));
        relationServer.registerMBean(external, new ObjectName("jck.rel:type=RelationExt"));
        relationService.addRelation(new ObjectName("jck.rel:type=RelationExt"));
        checkThrowsMsg(InvalidRelationIdException.class,
                () -> relationService.createRelation("ext", "jck.T", counterRole(observed(2))),
                "createRelation reusing the id of an external relation",
                "There is already a relation with id ext");
        // a null relation id is a plain parameter error, not this exception
        checkThrowsMsg(IllegalArgumentException.class,
                () -> relationService.createRelation(null, "jck.T", counterRole(observed(3))),
                "createRelation with a null id", "Invalid parameter.");
        // the original relations are intact
        check(Boolean.TRUE.equals(relationService.hasRelation("dup"))
              && Boolean.TRUE.equals(relationService.hasRelation("ext")),
              "both original relations still exist after the rejected duplicates");
    }
}
