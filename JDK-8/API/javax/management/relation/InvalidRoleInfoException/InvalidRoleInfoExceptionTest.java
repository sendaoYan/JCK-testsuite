/*
 * @test
 * @summary javax.management.relation.InvalidRoleInfoException conformance: exception hierarchy and construction.
 * @run main InvalidRoleInfoExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.InvalidRoleInfoException;
import javax.management.relation.RelationService;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class InvalidRoleInfoExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.InvalidRoleInfoException x = new javax.management.relation.InvalidRoleInfoException("msg");
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
        InvalidRoleInfoException noArg = new InvalidRoleInfoException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        InvalidRoleInfoException withMsg = new InvalidRoleInfoException("contract-marker-InvalidRoleInfoException");
        check("contract-marker-InvalidRoleInfoException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(InvalidRoleInfoException.class).getSerialVersionUID();
        check(suid == 7517834705158932074L, "serialVersionUID locked across releases: " + suid);
        InvalidRoleInfoException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-InvalidRoleInfoException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(InvalidRoleInfoException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        // real production trigger: RoleInfo degree validation (ROLE_CARDINALITY_INFINITY is -1)
        check(RoleInfo.ROLE_CARDINALITY_INFINITY == -1, "ROLE_CARDINALITY_INFINITY is the documented -1");
        checkThrowsMsg(InvalidRoleInfoException.class,
                () -> new RoleInfo("bad", "java.lang.String", true, true, 3, 2, "min above max"),
                "minDegree greater than a finite maxDegree",
                "Minimum degree 3 is greater than maximum degree 2");
        checkThrowsMsg(InvalidRoleInfoException.class,
                () -> new RoleInfo("bad", "java.lang.String", true, true, -1, 5, "infinite min, finite max"),
                "minDegree infinity with a finite maxDegree",
                "Minimum degree -1 is greater than maximum degree 5");
        checkThrowsMsg(InvalidRoleInfoException.class,
                () -> new RoleInfo("bad", "java.lang.String", true, true, -2, -1, "below -1"),
                "degree below ROLE_CARDINALITY_INFINITY",
                "Minimum or maximum degree has an illegal value, must be [0, ROLE_CARDINALITY_INFINITY].");
        // boundary-legal degree combinations construct without complaint
        check(new RoleInfo("ok", "java.lang.String", true, true, 0, 0, "exactly one").getMaxDegree() == 0,
              "0..0 degrees are legal");
        check(new RoleInfo("ok", "java.lang.String", true, true, -1, -1, "both infinite").getMinDegree() == -1,
              "-1..-1 degrees are legal (both infinite)");
        // an invalid RoleInfo can never reach the Relation Service: construction rejects it first,
        // so a well-formed type built from legal degrees is what the service accepts
        setUpRelationService();
        relationService.createRelationType("jck.Good", new RoleInfo[]{
                new RoleInfo("ok", "java.lang.String", true, true, 1, 1, "legal degrees") });
        check(relationService.getRoleInfos("jck.Good").size() == 1,
              "the legal-degree type was declared in the Relation Service");
    }
}
