/*
 * @test
 * @summary javax.management.relation.RelationType conformance: interface reflection.
 * @run main RelationTypeTest
 */

import java.util.List;
import javax.management.relation.RelationType;
import javax.management.relation.RelationTypeSupport;
import javax.management.relation.RoleInfo;
public class RelationTypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.relation.RelationType");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "getRelationTypeName"), "declares getRelationTypeName");
        check(hasMethod(c, "getRoleInfo"), "declares getRoleInfo");
        check(hasMethod(c, "getRoleInfos"), "declares getRoleInfos");
        testMethodSetContract();
        testInstanceViaInterface();
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

    // ---- deepened (batch k): full method-set contract + behavior through the interface ----

    static void testMethodSetContract() throws Exception {
        Class<?> c = RelationType.class;
        check(c.getDeclaredMethods().length == 3,
              "RelationType declares exactly 3 methods, got " + c.getDeclaredMethods().length);
        java.lang.reflect.Method gtn = c.getMethod("getRelationTypeName");
        check(gtn.getReturnType() == String.class, "getRelationTypeName returns String");
        java.lang.reflect.Method gris = c.getMethod("getRoleInfos");
        check(gris.getReturnType() == java.util.List.class, "getRoleInfos returns List");
        check("java.util.List<javax.management.relation.RoleInfo>"
                      .equals(gris.getGenericReturnType().getTypeName()),
              "getRoleInfos declares RoleInfo as its element type: " + gris.getGenericReturnType().getTypeName());
        java.lang.reflect.Method gri = c.getMethod("getRoleInfo", String.class);
        check(gri.getReturnType() == RoleInfo.class, "getRoleInfo returns RoleInfo");
        boolean declaresRoleInfoNotFound = false;
        for (Class<?> e : gri.getExceptionTypes()) {
            if (e == javax.management.relation.RoleInfoNotFoundException.class) declaresRoleInfoNotFound = true;
        }
        check(declaresRoleInfoNotFound, "getRoleInfo declares RoleInfoNotFoundException in its throws clause");
        check(javax.management.relation.RelationTypeSupport.class.isAssignableFrom(c) == false,
              "RelationType is not a superclass of RelationTypeSupport (interface, assignability is the other way)");
    }

    static void testInstanceViaInterface() throws Exception {
        RoleInfo ri = new RoleInfo("primary", "java.lang.String", true, false, 1, 1, "primary role");
        RelationType t = new RelationTypeSupport("jck.iface.T", new RoleInfo[]{ ri });
        check("jck.iface.T".equals(t.getRelationTypeName()), "interface view: getRelationTypeName");
        java.util.List<RoleInfo> infos = t.getRoleInfos();
        check(infos.size() == 1 && "primary".equals(infos.get(0).getName()), "interface view: getRoleInfos");
        check("primary".equals(t.getRoleInfo("primary").getName()), "interface view: getRoleInfo hit");
        try {
            t.getRoleInfo("absent");
            check(false, "interface view: getRoleInfo miss must throw");
        } catch (javax.management.relation.RoleInfoNotFoundException e) {
            check("No role info for role absent".equals(e.getMessage()), "interface view: miss message");
        }
        // serialization through the interface-typed reference keeps the full contract
        RelationType back = roundTrip(t);
        check(back instanceof RelationTypeSupport, "round-trip keeps the implementing class");
        check("jck.iface.T".equals(back.getRelationTypeName()), "round-trip keeps the type name");
        check(back.getRoleInfo("primary").isReadable(), "round-trip keeps the role info contract");
    }
}
