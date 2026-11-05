/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanNotificationInfo;
import java.util.*;
public class OpenMBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfo");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getClassName"), "getClassName");
        check(hasMethod(c, "getDescription"), "getDescription");
        check(hasMethod(c, "getAttributes"), "getAttributes");
        check(hasMethod(c, "getOperations"), "getOperations");
        check(hasMethod(c, "getConstructors"), "getConstructors");
        check(hasMethod(c, "getNotifications"), "getNotifications");

    }

    static void testBlackBox() throws Exception {
        OpenMBeanAttributeInfo[] attrs = { new OpenMBeanAttributeInfoSupport("a", "attr", SimpleType.STRING, true, true, false) };
        OpenMBeanConstructorInfo[] ctors = { new OpenMBeanConstructorInfoSupport("c", "ctor", new OpenMBeanParameterInfo[0]) };
        OpenMBeanOperationInfo[] ops = { new OpenMBeanOperationInfoSupport("o", "op", new OpenMBeanParameterInfo[0], SimpleType.VOID, MBeanOperationInfo.INFO) };
        OpenMBeanInfo mi = new OpenMBeanInfoSupport("MyClass", "class", attrs, ctors, ops, new MBeanNotificationInfo[0]);
        check(mi.getClassName().equals("MyClass"), "class name");
        check(mi.getDescription().equals("class"), "description");
        check(mi.getAttributes().length == 1, "attributes");
        check(mi.getConstructors().length == 1, "constructors");
        check(mi.getOperations().length == 1, "operations");
        check(mi.getNotifications().length == 0, "notifications");
        check(mi.equals(mi), "equals");
        check(mi.hashCode() == mi.hashCode(), "hashCode");
        check(mi.toString() != null, "toString");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // ---- deepened: batch g ----
        testStructureG();
        testContractG();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= deepened (batch g) =================
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

    static void testStructureG() throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanInfo");
        check(c.isInterface(), "is interface");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");
        check(c.getFields().length == 0, "no public fields");
        checkMethodSet(c, new String[] {"getClassName/0", "getDescription/0", "getAttributes/0", "getOperations/0", "getConstructors/0", "getNotifications/0", "equals/1", "hashCode/0", "toString/0"});
    }

    static void testContractG() throws Exception {
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
        OpenMBeanInfo empty = new OpenMBeanInfoSupport("X", "d", null, null, null, null);
        check(empty.getAttributes().length == 0 && empty.getOperations().length == 0
            && empty.getConstructors().length == 0 && empty.getNotifications().length == 0, "null arrays via interface");
        check(info.getAttributes()[0] instanceof OpenMBeanAttributeInfo, "attribute element type via interface");
    }
}
