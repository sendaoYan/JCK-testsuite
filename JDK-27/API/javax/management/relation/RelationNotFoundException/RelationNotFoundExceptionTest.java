/*
 * @test
 * @summary javax.management.relation.RelationNotFoundException conformance: white-box structure + black-box behavior.
 * @run main RelationNotFoundExceptionTest
 */
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.RelationNotFoundException;
import javax.management.relation.RelationService;
import javax.management.relation.Role;
import javax.management.relation.RoleInfo;
import javax.management.relation.RoleList;
public class RelationNotFoundExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationNotFoundException");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "class must be public");
        check(!c.isInterface(), "must be a class, not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "must be concrete (not abstract)");
        check(c.getSuperclass() == Class.forName("javax.management.relation.RelationException"),
              "must extend javax.management.relation.RelationException");
        check(Exception.class.isAssignableFrom(c), "must be an Exception");

        java.lang.reflect.Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 2, "must expose exactly 2 public constructors");
        boolean noArg = false, strArg = false;
        for (java.lang.reflect.Constructor<?> ct : ctors) {
            Class<?>[] pt = ct.getParameterTypes();
            if (pt.length == 0) noArg = true;
            else if (pt.length == 1 && pt[0] == String.class) strArg = true;
        }
        check(noArg, "must have a no-arg constructor");
        check(strArg, "must have a (String) constructor");

        java.lang.reflect.Field suid = c.getDeclaredField("serialVersionUID");
        check(suid.getType() == long.class, "serialVersionUID must be long");
        check(java.lang.reflect.Modifier.isStatic(suid.getModifiers()) &&
              java.lang.reflect.Modifier.isFinal(suid.getModifiers()) &&
              java.lang.reflect.Modifier.isPrivate(suid.getModifiers()),
              "serialVersionUID must be private static final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // default constructor: message must be null
        javax.management.relation.RelationNotFoundException e1 = new javax.management.relation.RelationNotFoundException();
        check(e1.getMessage() == null, "default constructor must leave message null");

        // (String) constructor: message preserved verbatim
        javax.management.relation.RelationNotFoundException e2 = new javax.management.relation.RelationNotFoundException("boom-42");
        check("boom-42".equals(e2.getMessage()), "(String) constructor must preserve message");
        check(e2 instanceof javax.management.relation.RelationException, "chain: instanceof RelationException");
        check(e2 instanceof javax.management.JMException, "chain: instanceof JMException");
        check(e2 instanceof Exception, "chain: instanceof Exception");

        // the checked exception keeps no stack-trace-dependent state; toString contains the class name
        check(e2.toString().equals("javax.management.relation.RelationNotFoundException: boom-42"), "toString() must be <fqcn>: <message>");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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
        RelationNotFoundException noArg = new RelationNotFoundException();
        check(noArg.getMessage() == null, "no-arg constructor leaves the message null");
        check(noArg.getCause() == null, "no-arg constructor leaves the cause null");
        RelationNotFoundException withMsg = new RelationNotFoundException("contract-marker-RelationNotFoundException");
        check("contract-marker-RelationNotFoundException".equals(withMsg.getMessage()), "(String) constructor preserves the message verbatim");
        check(withMsg.getCause() == null, "the class declares no cause-initializing constructor: cause stays null");
        check(withMsg instanceof javax.management.relation.RelationException, "measured chain: instanceof RelationException");
        check(withMsg instanceof javax.management.JMException, "measured chain: instanceof JMException");
        check(withMsg instanceof Exception, "measured chain: instanceof Exception");
        long suid = java.io.ObjectStreamClass.lookup(RelationNotFoundException.class).getSerialVersionUID();
        check(suid == -3793951411158559116L, "serialVersionUID locked across releases: " + suid);
        RelationNotFoundException back = roundTrip(withMsg);
        check(back != withMsg && "contract-marker-RelationNotFoundException".equals(back.getMessage()),
              "serialization round-trip keeps the message");
        check(RelationNotFoundException.class == back.getClass(), "serialization round-trip keeps the concrete class");
    }

    static void testRealTrigger() throws Exception {
        setUpRelationService();
        relationService.createRelation("R1", "jck.T", counterRole(observed(1)));
        checkThrowsMsg(RelationNotFoundException.class, () -> relationService.removeRelation("bogus"),
                "removeRelation on an unknown relation id", "No relation associated to relation id bogus");
        checkThrowsMsg(RelationNotFoundException.class, () -> relationService.isRelationMBean("bogus"),
                "isRelationMBean on an unknown relation id", "No relation associated to relation id bogus");
        checkThrowsMsg(RelationNotFoundException.class, () -> relationService.getRole("bogus", "counterRole"),
                "getRole on an unknown relation id", "No relation associated to relation id bogus");
        checkThrowsMsg(RelationNotFoundException.class,
                () -> relationService.sendRelationCreationNotification("bogus"),
                "sendRelationCreationNotification on an unknown id",
                "No relation associated to relation id bogus");
        // hasRelation is the non-throwing probe: FALSE, never an exception
        check(Boolean.FALSE.equals(relationService.hasRelation("bogus")),
              "hasRelation returns Boolean.FALSE for an unknown id instead of throwing");
        check(Boolean.TRUE.equals(relationService.hasRelation("R1")),
              "hasRelation returns Boolean.TRUE for an existing relation");
        // failed lookups left the real relation untouched
        check(relationService.getRole("R1", "counterRole").size() == 1,
              "the real relation still resolves its role after failed lookups");
    }
}
