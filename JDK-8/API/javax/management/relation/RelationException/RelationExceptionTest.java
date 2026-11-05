/*
 * @test
 * @summary javax.management.relation.RelationException conformance: exception hierarchy and construction.
 * @run main RelationExceptionTest
 */

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationException;
import javax.management.relation.RelationService;
import javax.management.relation.RelationSupport;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationServiceNotRegisteredException;
import javax.management.relation.RelationTypeNotFoundException;
import javax.management.relation.RoleInfoNotFoundException;
import javax.management.relation.RoleNotFoundException;
import javax.management.relation.InvalidRelationIdException;
import javax.management.relation.InvalidRelationServiceException;
import javax.management.relation.InvalidRelationTypeException;
import javax.management.relation.InvalidRoleInfoException;
import javax.management.relation.InvalidRoleValueException;
public class RelationExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.management.relation.RelationException x = new javax.management.relation.RelationException("msg");
        Class<?> c = x.getClass();
        check(java.lang.Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(Class.forName("javax.management.JMException").isAssignableFrom(c), "extends javax.management.JMException");
        check(x.toString() != null, "toString non-null");
        x.getMessage(); x.getCause(); x.getStackTrace();
        testContractBasics();
        testFamilyHierarchy();
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
        RelationException noArg = new RelationException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        RelationException withMsg = new RelationException("contract-marker-RelationException");
        check("contract-marker-RelationException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(RelationException.class).getSerialVersionUID();
        check(suid == 5434016005679159613L, "serialVersionUID locked across releases: " + suid);
        RelationException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-RelationException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(RelationException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testFamilyHierarchy() throws Exception {
        check(javax.management.relation.RelationException.class.getSuperclass()
                      == javax.management.JMException.class,
              "measured chain: RelationException extends JMException directly");
        javax.management.relation.RelationException[] family = {
            new RelationNotFoundException(),
            new RelationServiceNotRegisteredException(),
            new RelationTypeNotFoundException(),
            new RoleInfoNotFoundException(),
            new RoleNotFoundException(),
            new InvalidRelationIdException(),
            new InvalidRelationServiceException(),
            new InvalidRelationTypeException(),
            new InvalidRoleInfoException(),
            new InvalidRoleValueException() };
        for (javax.management.relation.RelationException member : family) {
            check(member instanceof javax.management.relation.RelationException,
                  "family member " + member.getClass().getSimpleName() + " is a RelationException");
            check(member instanceof javax.management.JMException,
                  "family member " + member.getClass().getSimpleName() + " is a JMException");
        }
        // the root type itself can be constructed and serialized like its family
        javax.management.relation.RelationException root =
                roundTrip(new javax.management.relation.RelationException("root-marker"));
        check("root-marker".equals(root.getMessage()), "root type round-trip keeps the message");
    }

    static void testRealTrigger() throws Exception {
        setUpRelationService();
        relationService.createRelation("R1", "jck.T", counterRole(observed(1)));
        // a concrete relation failure surfaces as the root type to any caller catching RelationException
        try {
            relationService.getRole("R1", "bogusRole");
            check(false, "expected a relation failure through the Relation Service");
        } catch (javax.management.relation.RelationException e) {
            check(e instanceof RoleNotFoundException, "the concrete failure type is RoleNotFoundException");
            check("bogusRole does not exist in relation.".equals(e.getMessage()),
                  "catching through the root type keeps the original message");
        }
    }
}
