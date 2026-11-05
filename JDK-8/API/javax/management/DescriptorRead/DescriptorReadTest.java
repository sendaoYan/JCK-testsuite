/*
 * @test
 * @summary javax.management.DescriptorRead conformance: interface reflection.
 *          Deepened: single-method contract, implementers across the metadata
 *          family (MBeanFeatureInfo + MBeanInfo/Attribute/Operation/Parameter/
 *          Constructor/Notification all expose a non-null descriptor), MBeanInfo
 *          preserves a provided ImmutableDescriptor, and the interface is
 *          usable as a lambda/anonymous read view.
 * @run main DescriptorReadTest
 */

import javax.management.ImmutableDescriptor;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanConstructorInfo;
import javax.management.MBeanFeatureInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanNotificationInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.Descriptor;
import javax.management.DescriptorRead;

public class DescriptorReadTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorRead");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getDescriptor"), "declares getDescriptor");
        check(c.getDeclaredMethods().length == 1, "declares exactly one method");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c) == false,
                "DescriptorRead does not extend Serializable");

        // every metadata class in the family implements DescriptorRead and never
        // returns a null descriptor
        check(new MBeanFeatureInfo("n", "d") instanceof DescriptorRead, "MBeanFeatureInfo is DescriptorRead");
        check(new MBeanInfo("c", "d", null, null, null, null) instanceof DescriptorRead,
                "MBeanInfo is DescriptorRead");
        MBeanInfo info = new MBeanInfo("c", "d",
                new MBeanAttributeInfo[]{new MBeanAttributeInfo("a", "int", "d", true, false, false)},
                new MBeanConstructorInfo[]{new MBeanConstructorInfo("k", "d", null)},
                new MBeanOperationInfo[]{new MBeanOperationInfo("o", "d", null, "void", 0)},
                new MBeanNotificationInfo[]{new MBeanNotificationInfo(new String[]{"t"}, "n", "d")});
        check(info.getAttributes()[0].getDescriptor() != null, "attribute exposes non-null descriptor");
        check(info.getConstructors()[0].getDescriptor() != null, "constructor exposes non-null descriptor");
        check(info.getOperations()[0].getDescriptor() != null, "operation exposes non-null descriptor");
        check(info.getNotifications()[0].getDescriptor() != null, "notification exposes non-null descriptor");
        check(new MBeanParameterInfo("p", "int", "d").getDescriptor() != null,
                "parameter exposes non-null descriptor");
        // provided descriptor survives the MBeanInfo round-trip
        MBeanInfo withD = new MBeanInfo("c", "d", null, null, null, null,
                new ImmutableDescriptor("k=v"));
        check("v".equals(withD.getDescriptor().getFieldValue("k")),
                "MBeanInfo keeps the provided descriptor field");

        // the interface is a one-method read view, trivially implementable
        final Descriptor shared = new ImmutableDescriptor("x=1");
        DescriptorRead view = new DescriptorRead() {
            public Descriptor getDescriptor() { return shared; }
        };
        check(view.getDescriptor() == shared, "anonymous implementation returns the same descriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
