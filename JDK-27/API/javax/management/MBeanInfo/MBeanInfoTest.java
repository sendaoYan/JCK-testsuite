/*
 * @test
 * @summary javax.management.MBeanInfo conformance: white-box structure + black-box behavior.
 * @run main MBeanInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.management.*;

public class MBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static MBeanInfo build(String className, String desc) {
        MBeanAttributeInfo[] attrs = { new MBeanAttributeInfo("Attr", "int", "attr desc", true, true, false) };
        MBeanConstructorInfo[] ctors = { new MBeanConstructorInfo("Ctor", "ctor desc", new MBeanParameterInfo[0]) };
        MBeanOperationInfo[] ops = { new MBeanOperationInfo("Op", "op desc", new MBeanParameterInfo[0], "int", MBeanOperationInfo.INFO) };
        MBeanNotificationInfo[] notifs = { new MBeanNotificationInfo(new String[]{"type"}, "Notif", "notif desc") };
        return new MBeanInfo(className, desc, attrs, ctors, ops, notifs);
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanInfo");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(DescriptorRead.class.isAssignableFrom(c), "implements DescriptorRead");
        check(hasMethod(c, "getClassName"), "has getClassName");
        check(hasMethod(c, "getDescription"), "has getDescription");
        check(hasMethod(c, "getAttributes"), "has getAttributes");
        check(hasMethod(c, "getConstructors"), "has getConstructors");
        check(hasMethod(c, "getOperations"), "has getOperations");
        check(hasMethod(c, "getNotifications"), "has getNotifications");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MBeanInfo mi = build("java.lang.String", "desc");
        check("java.lang.String".equals(mi.getClassName()), "getClassName");
        check("desc".equals(mi.getDescription()), "getDescription");
        check(mi.getAttributes().length == 1 && "Attr".equals(mi.getAttributes()[0].getName()), "getAttributes");
        check(mi.getConstructors().length == 1 && "Ctor".equals(mi.getConstructors()[0].getName()), "getConstructors");
        check(mi.getOperations().length == 1 && "Op".equals(mi.getOperations()[0].getName()), "getOperations");
        check(mi.getNotifications().length == 1 && "Notif".equals(mi.getNotifications()[0].getName()), "getNotifications");
        check(mi.getDescriptor() != null, "getDescriptor non-null");

        // empty arrays case
        MBeanInfo empty = new MBeanInfo("c", "d",
                new MBeanAttributeInfo[0], new MBeanConstructorInfo[0],
                new MBeanOperationInfo[0], new MBeanNotificationInfo[0]);
        check(empty.getAttributes().length == 0 && empty.getConstructors().length == 0
                && empty.getOperations().length == 0 && empty.getNotifications().length == 0,
                "empty MBeanInfo arrays");

        // clone produces equal, independent object
        MBeanInfo clone = (MBeanInfo) mi.clone();
        check(clone != mi, "clone not identical");
        check(clone.equals(mi), "clone equals original");

        // equals: identical construction equals; different className/description differ
        check(mi.equals(build("java.lang.String", "desc")), "equals identical construction");
        check(!mi.equals(build("other.Class", "desc")), "different className not equal");
        check(!mi.equals(build("java.lang.String", "other")), "different description not equal");
        check(!mi.equals(null) && !mi.equals("x"), "not equal null/non-MBeanInfo");
        check(mi.hashCode() == build("java.lang.String", "desc").hashCode(), "equal hashCode");

        // descriptor variant constructor preserves descriptor
        ImmutableDescriptor d = new ImmutableDescriptor("name=n", "description=d");
        MBeanInfo withDesc = new MBeanInfo("c", "d", new MBeanAttributeInfo[0],
                new MBeanConstructorInfo[0], new MBeanOperationInfo[0],
                new MBeanNotificationInfo[0], d);
        check(withDesc.getDescriptor() != null && "n".equals(withDesc.getDescriptor().getFieldValue("name")),
                "descriptor ctor preserves descriptor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
