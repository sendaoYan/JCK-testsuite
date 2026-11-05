/*
 * @test
 * @summary javax.management.relation.InvalidRelationTypeException conformance: exception hierarchy and construction.
 * @run main InvalidRelationTypeExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.InvalidRelationTypeException;
import javax.management.relation.RelationService;
import javax.management.relation.RelationTypeSupport;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class InvalidRelationTypeExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.InvalidRelationTypeException x = new javax.management.relation.InvalidRelationTypeException("msg");
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
        InvalidRelationTypeException noArg = new InvalidRelationTypeException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        InvalidRelationTypeException withMsg = new InvalidRelationTypeException("contract-marker-InvalidRelationTypeException");
        check("contract-marker-InvalidRelationTypeException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(InvalidRelationTypeException.class).getSerialVersionUID();
        check(suid == 3007446608299169961L, "serialVersionUID locked across releases: " + suid);
        InvalidRelationTypeException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-InvalidRelationTypeException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(InvalidRelationTypeException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        setUpRelationService();
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> relationService.createRelationType("jck.Empty", new RoleInfo[0]),
                "createRelationType with an empty RoleInfo array", "No role info provided.");
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> relationService.createRelationType("jck.NullElem", new RoleInfo[]{ null }),
                "createRelationType with a null role info element", "Null role info provided.");
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> relationService.createRelationType("jck.Dup", new RoleInfo[]{
                        new RoleInfo("twin", "java.lang.String"), new RoleInfo("twin", "java.lang.String") }),
                "createRelationType with two role infos of the same name",
                "Two role infos provided for role twin");
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> relationService.createRelationType("jck.T",
                        new RoleInfo[]{ new RoleInfo("counterRole", "java.lang.String") }),
                "createRelationType reusing a declared type name",
                "There is already a relation type in the Relation Service with name jck.T");
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> relationService.addRelationType(new RelationTypeSupport("jck.T",
                        new RoleInfo[]{ new RoleInfo("counterRole", "java.lang.String") })),
                "addRelationType with an already declared type name",
                "There is already a relation type in the Relation Service with name jck.T");
        // the same validation fires on the standalone relation type object
        checkThrowsMsg(InvalidRelationTypeException.class,
                () -> new RelationTypeSupport("jck.Local", new RoleInfo[]{ null }),
                "RelationTypeSupport constructor with a null role info", "Null role info provided.");
        // failed declarations left nothing behind; the original type is untouched
        check(!relationService.getAllRelationTypeNames().contains("jck.Empty"),
              "no type named jck.Empty was registered");
        check(relationService.getRoleInfos("jck.T").size() == 1,
              "the original jck.T declaration is untouched");
    }
}
