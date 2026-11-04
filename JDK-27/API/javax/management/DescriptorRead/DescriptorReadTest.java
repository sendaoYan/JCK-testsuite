/*
 * @test
 * @summary javax.management.DescriptorRead conformance: white-box structure + light behavior.
 * @run main DescriptorReadTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class DescriptorReadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.DescriptorRead");
        check(c.isInterface(), "is interface");
        check(hasMethod(c, "getDescriptor"), "has getDescriptor");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final Descriptor d = new ImmutableDescriptor("name=n");
        DescriptorRead read = new DescriptorRead() {
            public Descriptor getDescriptor() { return d; }
        };
        check(read.getDescriptor() == d, "getDescriptor returns descriptor");

        // MBeanFeatureInfo implements DescriptorRead
        MBeanFeatureInfo fi = new MBeanFeatureInfo("n", "d");
        check(fi.getDescriptor() != null, "MBeanFeatureInfo.getDescriptor non-null");
    }

    // ---- deepened: metadata family implementers + provided descriptor --------

    static void testMetadataImplementers() {
        check(MBeanInfo.class.getInterfaces() != null, "sanity");
        check(new MBeanInfo("c", "d", null, null, null, null) instanceof DescriptorRead,
                "MBeanInfo is DescriptorRead");
        check(new MBeanAttributeInfo("a", "int", "d", true, false, false) instanceof DescriptorRead,
                "MBeanAttributeInfo is DescriptorRead");
        check(new MBeanOperationInfo("o", "d", null, "void", 0) instanceof DescriptorRead,
                "MBeanOperationInfo is DescriptorRead");
        check(new MBeanParameterInfo("p", "int", "d") instanceof DescriptorRead,
                "MBeanParameterInfo is DescriptorRead");
        check(new MBeanConstructorInfo("k", "d", null) instanceof DescriptorRead,
                "MBeanConstructorInfo is DescriptorRead");
        check(new MBeanNotificationInfo(new String[]{"t"}, "n", "d") instanceof DescriptorRead,
                "MBeanNotificationInfo is DescriptorRead");
        MBeanInfo info = new MBeanInfo("c", "d",
                new MBeanAttributeInfo[]{new MBeanAttributeInfo("a", "int", "d", true, false, false)},
                new MBeanConstructorInfo[]{new MBeanConstructorInfo("k", "d", null)},
                new MBeanOperationInfo[]{new MBeanOperationInfo("o", "d", null, "void", 0)},
                new MBeanNotificationInfo[]{new MBeanNotificationInfo(new String[]{"t"}, "n", "d")});
        check(info.getAttributes()[0].getDescriptor().getFieldNames().length == 0
                && info.getAttributes()[0].getDescriptor() != null,
                "attribute descriptor non-null and defaults to empty");
        check(info.getConstructors()[0].getDescriptor() != null, "constructor descriptor non-null");
        check(info.getOperations()[0].getDescriptor() != null, "operation descriptor non-null");
        check(info.getNotifications()[0].getDescriptor() != null, "notification descriptor non-null");
        MBeanInfo withD = new MBeanInfo("c", "d", null, null, null, null, new ImmutableDescriptor("k=v"));
        check("v".equals(withD.getDescriptor().getFieldValue("k")),
                "MBeanInfo keeps the provided descriptor field");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMetadataImplementers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
