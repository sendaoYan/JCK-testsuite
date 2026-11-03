/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanInfo conformance: white-box structure + black-box behavior.
 * @run main OpenMBeanInfoTest
 */

import javax.management.openmbean.*;
import javax.management.*;
import java.lang.reflect.*;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
