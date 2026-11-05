/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfo conformance: interface contract via Support implementation, exact method set, no public fields.
 * @run main OpenMBeanInfoTest
 */

import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.openmbean.OpenMBeanAttributeInfo;
import javax.management.openmbean.OpenMBeanAttributeInfoSupport;
import javax.management.openmbean.OpenMBeanConstructorInfo;
import javax.management.openmbean.OpenMBeanConstructorInfoSupport;
import javax.management.openmbean.OpenMBeanInfo;
import javax.management.openmbean.OpenMBeanInfoSupport;
import javax.management.openmbean.OpenMBeanOperationInfo;
import javax.management.openmbean.OpenMBeanOperationInfoSupport;
import javax.management.openmbean.OpenMBeanParameterInfo;
import javax.management.openmbean.OpenMBeanParameterInfoSupport;
import javax.management.openmbean.SimpleType;

public class OpenMBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    interface ThrowingCall { void run() throws Throwable; }
    static Object roundTrip(Object o) throws Exception {
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
        oos.writeObject(o);
        oos.close();
        return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
    }
    static java.util.Set<String> ownMethods(Class<?> iface) throws Exception {
        java.util.Set<String> names = new java.util.TreeSet<String>();
        for (java.lang.reflect.Method m : iface.getMethods()) {
            if (m.getDeclaringClass() == Object.class) continue;
            names.add(m.getName() + "/" + m.getParameterTypes().length);
        }
        return names;
    }
    static void checkMethodSet(Class<?> iface, String[] expected) {
        java.util.Set<String> got = null;
        try { got = ownMethods(iface); } catch (Exception e) { throw new AssertionError(e); }
        java.util.Set<String> exp = new java.util.TreeSet<String>();
        for (String e : expected) exp.add(e);
        if (!got.equals(exp)) {
            java.util.Set<String> missing = new java.util.TreeSet<String>(exp); missing.removeAll(got);
            java.util.Set<String> extra = new java.util.TreeSet<String>(got); extra.removeAll(exp);
            throw new AssertionError(iface.getName() + " method set mismatch, missing=" + missing + " extra=" + extra);
        }
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getClassName"), "declares getClassName");

        // ---- deepened: batch g ----
        testStructure();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepened: exact structure (method set from javap, no public fields) ----
    static void testStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getClassName/0", "getDescription/0", "getAttributes/0", "getOperations/0", "getConstructors/0", "getNotifications/0", "equals/1", "hashCode/0", "toString/0"});

    }

    // ---- deepened: every method called through the interface reference on the Support impl ----
    static void testContract() throws Exception {
        OpenMBeanAttributeInfo[] attrs = { new OpenMBeanAttributeInfoSupport("a", "ad", SimpleType.STRING, true, true, false) };
        OpenMBeanConstructorInfo[] ctors = { new OpenMBeanConstructorInfoSupport("C", "cd", null) };
        OpenMBeanOperationInfo[] ops = { new OpenMBeanOperationInfoSupport("op", "od", null, SimpleType.INTEGER, MBeanOperationInfo.INFO) };
        MBeanNotificationInfo[] notifs = { new MBeanNotificationInfo(new String[] {"t"}, "n", "d") };
        OpenMBeanInfo info = new OpenMBeanInfoSupport("MyClass", "desc", attrs, ctors, ops, notifs);
        check("MyClass".equals(info.getClassName()), "getClassName via interface");
        check("desc".equals(info.getDescription()), "getDescription via interface");
        check(info.getAttributes().length == 1 && "a".equals(info.getAttributes()[0].getName()), "getAttributes via interface");
        check(info.getOperations().length == 1 && "op".equals(info.getOperations()[0].getName()), "getOperations via interface");
        check(info.getConstructors().length == 1 && "C".equals(info.getConstructors()[0].getName()), "getConstructors via interface");
        check(info.getNotifications().length == 1, "getNotifications via interface");
        check(info.equals(new OpenMBeanInfoSupport("MyClass", "desc", attrs, ctors, ops, notifs)), "equals via interface");
        check(!info.equals(new OpenMBeanInfoSupport("Other", "desc", attrs, ctors, ops, notifs)), "equals false via interface");
        check(info.hashCode() == new OpenMBeanInfoSupport("MyClass", "desc", attrs, ctors, ops, notifs).hashCode(), "hashCode via interface");
        check(info.toString().contains("MyClass"), "toString via interface");
        // null arrays normalize through the interface too
        OpenMBeanInfo empty = new OpenMBeanInfoSupport("X", "d", null, null, null, null);
        check(empty.getAttributes().length == 0 && empty.getOperations().length == 0
            && empty.getConstructors().length == 0 && empty.getNotifications().length == 0, "null arrays via interface");
        // open attribute elements remain OpenMBeanAttributeInfo through the interface view
        check(info.getAttributes()[0] instanceof OpenMBeanAttributeInfo, "attribute element type via interface");

    }
}
