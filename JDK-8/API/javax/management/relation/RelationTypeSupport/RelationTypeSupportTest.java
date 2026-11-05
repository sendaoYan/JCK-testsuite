/*
 * @test
 * @summary javax.management.relation.RelationTypeSupport conformance: concrete class reflection.
 * @run main RelationTypeSupportTest
 */

import java.util.List;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.relation.InvalidRelationTypeException;
import javax.management.relation.RelationService;
import javax.management.relation.RoleInfo;
import javax.management.relation.RelationTypeSupport;
import javax.management.relation.RoleInfoNotFoundException;
public class RelationTypeSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationTypeSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("javax.management.relation.RelationType").isAssignableFrom(c), "implements/extends javax.management.relation.RelationType");
        check(hasMethod(c, "getRelationTypeName"), "declares getRelationTypeName");
        check(hasMethod(c, "getRoleInfo"), "declares getRoleInfo");
        check(hasMethod(c, "getRoleInfos"), "declares getRoleInfos");
        testDefensiveCopies();
        testSubclassPath();
        testRelationServiceDeclarationLock();
        testSerializationRoundTrip();
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

    // ---- deepened (batch k): defensive copies, subclass addRoleInfo path, declaration lock,
    //      serialization round-trip, serialVersionUID ----

    public static class SubRelationType extends RelationTypeSupport {
        public SubRelationType(String name) { super(name); }
        public void exposeAddRoleInfo(RoleInfo ri) throws Exception { addRoleInfo(ri); }
    }

    static RoleInfo info(String name) throws Exception {
        return new RoleInfo(name, "java.lang.String", true, true, 0, 10, "role " + name);
    }

    static void testDefensiveCopies() throws Exception {
        RoleInfo given = info("guarded");
        RelationTypeSupport rts = new RelationTypeSupport("jck.copy.T", new RoleInfo[]{ given });
        check(rts.getRelationTypeName().equals("jck.copy.T"), "getRelationTypeName round-trip");
        // every getRoleInfos call returns a fresh list
        java.util.List<RoleInfo> first = rts.getRoleInfos();
        java.util.List<RoleInfo> second = rts.getRoleInfos();
        check(first != second, "getRoleInfos returns a new list per call");
        // mutating a returned list never leaks into the relation type
        first.clear();
        check(rts.getRoleInfos().size() == 1,
              "clearing a returned getRoleInfos list leaves the relation type intact");
        // stored RoleInfo objects are copies: never the instances handed to the constructor
        RoleInfo stored = rts.getRoleInfo("guarded");
        check(stored != given, "getRoleInfo returns a copy, not the constructor-supplied instance");
        check("guarded".equals(stored.getName()) && stored.isReadable() && stored.isWritable()
                && stored.getMinDegree() == 0 && stored.getMaxDegree() == 10
                && "java.lang.String".equals(stored.getRefMBeanClassName())
                && "role guarded".equals(stored.getDescription()),
              "the stored copy preserves every RoleInfo attribute");
        check(rts.getRoleInfos().get(0) != given, "getRoleInfos elements are copies too");
    }

    static void testSubclassPath() throws Exception {
        SubRelationType sub = new SubRelationType("jck.sub.T");
        check("jck.sub.T".equals(sub.getRelationTypeName()), "protected (String) constructor sets the type name");
        check(sub.getRoleInfos().isEmpty(), "protected constructor starts with no role infos");
        sub.exposeAddRoleInfo(info("alpha"));
        check(sub.getRoleInfos().size() == 1, "addRoleInfo grows the role count to 1");
        sub.exposeAddRoleInfo(info("beta"));
        check(sub.getRoleInfos().size() == 2, "addRoleInfo grows the role count to 2");
        check(sub.getRoleInfo("beta").getName().equals("beta"), "added role info is retrievable by name");
        checkThrowsMsg(InvalidRelationTypeException.class, () -> sub.exposeAddRoleInfo(info("alpha")),
                "addRoleInfo with an already used name", "Two role infos provided for role alpha");
        check(sub.getRoleInfos().size() == 2, "a rejected addRoleInfo leaves the role count unchanged");
        checkThrows(java.lang.IllegalArgumentException.class, () -> sub.exposeAddRoleInfo(null),
                "addRoleInfo(null) throws IllegalArgumentException");
        check(sub.getRoleInfos().size() == 2, "a null addRoleInfo leaves the role count unchanged");
        try {
            new SubRelationType(null);
            check(false, "protected constructor with a null name must throw");
        } catch (IllegalArgumentException e) {
            check("Invalid parameter.".equals(e.getMessage()), "protected constructor null-name message");
        }
    }

    static void testRelationServiceDeclarationLock() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName svcName = new ObjectName("jck.rel:type=RelationService");
        RelationService svc = new RelationService(true);
        server.registerMBean(svc, svcName);
        SubRelationType sub = new SubRelationType("jck.lock.T");
        sub.exposeAddRoleInfo(info("only"));
        svc.addRelationType(sub);
        check(svc.getAllRelationTypeNames().contains("jck.lock.T"),
              "the declared type is visible through the Relation Service");
        try {
            sub.exposeAddRoleInfo(info("late"));
            check(false, "updating a relation type declared in the Relation Service must fail");
        } catch (RuntimeException e) {
            check("Relation type cannot be updated as it is declared in the Relation Service."
                    .equals(e.getMessage()), "declared-type lock message: " + e.getMessage());
        }
        // a type that was never declared stays freely extendable
        SubRelationType free = new SubRelationType("jck.free.T");
        free.exposeAddRoleInfo(info("any"));
        check(free.getRoleInfos().size() == 1, "an undeclared type remains extendable");
    }

    static void testSerializationRoundTrip() throws Exception {
        long suid = java.io.ObjectStreamClass.lookup(RelationTypeSupport.class).getSerialVersionUID();
        check(suid == 4611072955724144607L, "serialVersionUID locked across releases: " + suid);
        RelationTypeSupport rts = new RelationTypeSupport("jck.ser.T",
                new RoleInfo[]{ info("one"), info("two"), info("three") });
        RelationTypeSupport back = roundTrip(rts);
        check("jck.ser.T".equals(back.getRelationTypeName()), "round-trip keeps the type name");
        check(back.getRoleInfos().size() == 3, "round-trip keeps every role info");
        check("two".equals(back.getRoleInfo("two").getName()), "round-trip role lookup still works");
        checkThrows(javax.management.relation.RoleInfoNotFoundException.class,
                () -> back.getRoleInfo("missing"),
                "round-trip instance keeps the unknown-role contract");
    }
}
